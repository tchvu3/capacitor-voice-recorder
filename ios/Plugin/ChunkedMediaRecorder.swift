import Foundation
import AVFoundation

class ChunkedMediaRecorder {

    var options: RecordOptions!
    private var recordingSession: AVAudioSession!
    private var audioRecorder: AVAudioRecorder!
    private var currentChunkPath: URL!
    private var originalRecordingSessionCategory: AVAudioSession.Category!
    private var status = CurrentRecordingStatus.NONE
    private var chunkIntervalMs: Int = 30000
    private weak var voiceRecorder: VoiceRecorder?
    private var chunkTimer: Timer?
    private var chunkIndex: Int = 0
    private var interruptionObserver: NSObjectProtocol?
    var onInterruptionBegan: (() -> Void)?
    var onInterruptionEnded: (() -> Void)?

    private let settings = [
        AVFormatIDKey: Int(kAudioFormatMPEG4AAC),
        AVSampleRateKey: 44100,
        AVNumberOfChannelsKey: 1,
        AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue
    ]

    private func getDirectoryToSaveAudioFile() -> URL {
        if let directory = getDirectory(directory: options.directory),
           var outputDirURL = FileManager.default.urls(for: directory, in: .userDomainMask).first {
            if let subDirectory = options.subDirectory?.trimmingCharacters(in: CharacterSet(charactersIn: "/")) {
                options.setSubDirectory(to: subDirectory)
                outputDirURL = outputDirURL.appendingPathComponent(subDirectory, isDirectory: true)

                do {
                    if !FileManager.default.fileExists(atPath: outputDirURL.path) {
                        try FileManager.default.createDirectory(at: outputDirURL, withIntermediateDirectories: true)
                    }
                } catch {
                    print("[VoiceRecorder] Error creating directory: \(error)")
                }
            }

            return outputDirURL
        }

        return URL(fileURLWithPath: NSTemporaryDirectory(), isDirectory: true)
    }

    private func makeChunkPath() -> URL {
        let timestamp = Int(Date().timeIntervalSince1970 * 1000)
        return getDirectoryToSaveAudioFile().appendingPathComponent("chunked-recording-\(timestamp)-\(chunkIndex).aac")
    }

    func startRecording(recordOptions: RecordOptions, chunkIntervalMs: Int, voiceRecorder: VoiceRecorder) -> Bool {
        do {
            self.options = recordOptions
            self.chunkIntervalMs = chunkIntervalMs
            self.voiceRecorder = voiceRecorder
            self.chunkIndex = 0

            recordingSession = AVAudioSession.sharedInstance()
            originalRecordingSessionCategory = recordingSession.category
            try recordingSession.setCategory(AVAudioSession.Category.playAndRecord)
            try recordingSession.setActive(true)

            currentChunkPath = makeChunkPath()
            audioRecorder = try AVAudioRecorder(url: currentChunkPath, settings: settings)

            setupInterruptionHandling()

            audioRecorder.record()
            status = CurrentRecordingStatus.RECORDING

            scheduleNextChunk()
            return true
        } catch {
            return false
        }
    }

    func stopRecording() {
        chunkTimer?.invalidate()
        chunkTimer = nil

        removeInterruptionHandling()

        // Stop the recorder and emit the last chunk synchronously.
        if audioRecorder != nil {
            audioRecorder.stop()
            emitChunk(path: currentChunkPath, isFinalChunk: true)
        }

        do {
            if recordingSession != nil {
                try recordingSession.setActive(false)
                if originalRecordingSessionCategory != nil {
                    try recordingSession.setCategory(originalRecordingSessionCategory)
                }
            }
        } catch {
            print("[VoiceRecorder] Error tearing down session: \(error)")
        }

        originalRecordingSessionCategory = nil
        audioRecorder = nil
        recordingSession = nil
        currentChunkPath = nil
        status = CurrentRecordingStatus.NONE
    }

    func pauseRecording() -> Bool {
        if status == CurrentRecordingStatus.RECORDING {
            chunkTimer?.invalidate()
            chunkTimer = nil
            audioRecorder.pause()
            status = CurrentRecordingStatus.PAUSED
            return true
        }
        return false
    }

    func resumeRecording() -> Bool {
        if status == CurrentRecordingStatus.PAUSED || status == CurrentRecordingStatus.INTERRUPTED {
            do {
                try recordingSession.setActive(true)

                // If resuming from interruption, the previous recorder was stopped.
                // Rotate to a new chunk file before resuming.
                if status == CurrentRecordingStatus.INTERRUPTED {
                    rotateChunkFile()
                }

                audioRecorder.record()
                status = CurrentRecordingStatus.RECORDING
                scheduleNextChunk()
                return true
            } catch {
                return false
            }
        }
        return false
    }

    func getCurrentStatus() -> CurrentRecordingStatus {
        return status
    }

    private func scheduleNextChunk() {
        guard status == CurrentRecordingStatus.RECORDING else { return }

        let interval = TimeInterval(chunkIntervalMs) / 1000.0
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }
            self.chunkTimer?.invalidate()
            self.chunkTimer = Timer.scheduledTimer(withTimeInterval: interval, repeats: false) { [weak self] _ in
                self?.onChunkTimerFired()
            }
        }
    }

    private func onChunkTimerFired() {
        guard status == CurrentRecordingStatus.RECORDING else { return }

        // Stop current recording, emit the file as a chunk, then start a new recorder.
        let finishedPath = currentChunkPath
        audioRecorder.stop()
        emitChunk(path: finishedPath, isFinalChunk: false)

        // Rotate to a new chunk file and resume recording immediately.
        do {
            rotateChunkFile()
            audioRecorder.record()
            scheduleNextChunk()
        } catch {
            print("[VoiceRecorder] Failed to rotate chunk recorder: \(error)")
            status = CurrentRecordingStatus.NONE
        }
    }

    private func rotateChunkFile() {
        do {
            currentChunkPath = makeChunkPath()
            audioRecorder = try AVAudioRecorder(url: currentChunkPath, settings: settings)
        } catch {
            print("[VoiceRecorder] Failed to create new AVAudioRecorder: \(error)")
        }
    }

    private func emitChunk(path: URL?, isFinalChunk: Bool) {
        guard let path = path, FileManager.default.fileExists(atPath: path.path) else { return }

        guard let base64 = readFileAsBase64(path) else { return }
        let duration = getMsDurationOfAudioFile(path)
        if duration <= 0 && !isFinalChunk {
            // Skip empty non-final chunks (e.g. timer fires before any audio captured).
            try? FileManager.default.removeItem(at: path)
            return
        }

        let chunk = AudioChunk(
            recordDataBase64: base64,
            msDuration: max(duration, 0),
            mimeType: "audio/aac",
            chunkIndex: chunkIndex,
            isFinalChunk: isFinalChunk
        )

        voiceRecorder?.emitAudioChunk(chunk)
        chunkIndex += 1

        // Emitted chunk data is now held by the JS side. Reclaim disk.
        try? FileManager.default.removeItem(at: path)
    }

    private func setupInterruptionHandling() {
        interruptionObserver = NotificationCenter.default.addObserver(
            forName: AVAudioSession.interruptionNotification,
            object: AVAudioSession.sharedInstance(),
            queue: .main
        ) { [weak self] notification in
            self?.handleInterruption(notification: notification)
        }
    }

    private func removeInterruptionHandling() {
        if let observer = interruptionObserver {
            NotificationCenter.default.removeObserver(observer)
            interruptionObserver = nil
        }
    }

    private func handleInterruption(notification: Notification) {
        guard let userInfo = notification.userInfo,
              let interruptionTypeValue = userInfo[AVAudioSessionInterruptionTypeKey] as? UInt,
              let interruptionType = AVAudioSession.InterruptionType(rawValue: interruptionTypeValue) else {
            return
        }

        switch interruptionType {
        case .began:
            if status == CurrentRecordingStatus.RECORDING {
                chunkTimer?.invalidate()
                chunkTimer = nil
                let finishedPath = currentChunkPath
                audioRecorder.stop()
                // Emit what we have so the user doesn't lose audio captured before the call.
                emitChunk(path: finishedPath, isFinalChunk: false)
                status = CurrentRecordingStatus.INTERRUPTED
                onInterruptionBegan?()
            }
        case .ended:
            if status == CurrentRecordingStatus.INTERRUPTED {
                onInterruptionEnded?()
            }
        @unknown default:
            break
        }
    }

    private func readFileAsBase64(_ filePath: URL) -> String? {
        do {
            let fileData = try Data(contentsOf: filePath)
            return fileData.base64EncodedString(options: NSData.Base64EncodingOptions(rawValue: 0))
        } catch {
            return nil
        }
    }

    private func getMsDurationOfAudioFile(_ filePath: URL) -> Int {
        return Int(CMTimeGetSeconds(AVURLAsset(url: filePath).duration) * 1000)
    }

    func getDirectory(directory: String?) -> FileManager.SearchPathDirectory? {
        if let directory = directory {
            switch directory {
            case "CACHE":
                return .cachesDirectory
            case "LIBRARY":
                return .libraryDirectory
            default:
                return .documentDirectory
            }
        }
        return nil
    }
}
