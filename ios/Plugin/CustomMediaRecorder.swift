import Foundation
import AVFoundation

class CustomMediaRecorder {

    public struct RecordOptions {
        let directory: String?
        let subDirectory: String?

        public init(directory: String? = nil, subDirectory: String? = nil) {
            self.directory = directory
            self.subDirectory = subDirectory
        }
    }

    private var recordingSession: AVAudioSession!
    private var audioRecorder: AVAudioRecorder!
    private var audioFilePath: URL!
    private var originalRecordingSessionCategory: AVAudioSession.Category!
    private var status = CurrentRecordingStatus.NONE
    private var wasRecordedWithOptions: Bool = false

    private let settings = [
        AVFormatIDKey: Int(kAudioFormatMPEG4AAC),
        AVSampleRateKey: 44100,
        AVNumberOfChannelsKey: 1,
        AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue
    ]

    private func getDirectoryToSaveAudioFile(options: RecordOptions? = nil) -> URL {
        if let options = options,
           let directoryString = options.directory,
           let searchPath = getDirectory(directory: directoryString) {
            let baseURL = FileManager.default.urls(for: searchPath, in: .userDomainMask).first!

            if let subDirectory = options.subDirectory {
                let fullPath = baseURL.appendingPathComponent(subDirectory)

                // Create subdirectory if it doesn't exist
                do {
                    if !FileManager.default.fileExists(atPath: fullPath.path) {
                        try FileManager.default.createDirectory(at: fullPath, withIntermediateDirectories: true)
                    }
                    return fullPath
                } catch {
                    print("Error creating directory: \(error)")
                }
            }

            return baseURL
        }

        return URL(fileURLWithPath: NSTemporaryDirectory(), isDirectory: true).appendingPathComponent("\(UUID().uuidString).aac")
    }

    func startRecording(options: RecordOptions? = nil) -> Bool {
        wasRecordedWithOptions = options != nil
        do {
            recordingSession = AVAudioSession.sharedInstance()
            originalRecordingSessionCategory = recordingSession.category
            try recordingSession.setCategory(AVAudioSession.Category.playAndRecord)
            try recordingSession.setActive(true)
            audioFilePath = getDirectoryToSaveAudioFile(options: options)
            audioRecorder = try AVAudioRecorder(url: audioFilePath, settings: settings)
            audioRecorder.record()
            status = CurrentRecordingStatus.RECORDING
            return true
        } catch {
            return false
        }
    }

    func stopRecording() {
        do {
            audioRecorder.stop()
            try recordingSession.setActive(false)
            try recordingSession.setCategory(originalRecordingSessionCategory)
            originalRecordingSessionCategory = nil
            audioRecorder = nil
            recordingSession = nil
            status = CurrentRecordingStatus.NONE
        } catch {}
    }

    func getOutputFile() -> URL {
        return audioFilePath
    }

    func wasRecordedToFile() -> Bool {
        return wasRecordedWithOptions
    }

    func getDirectory(directory: String?) -> FileManager.SearchPathDirectory? {
        if let directory = directory {
            switch directory {
            case "CACHE":
                return .cachesDirectory
            case "DATA", "LIBRARY":
                return .libraryDirectory
            default:
                return .documentDirectory
            }
        }
        return nil
    }

    func pauseRecording() -> Bool {
        if status == CurrentRecordingStatus.RECORDING {
            audioRecorder.pause()
            status = CurrentRecordingStatus.PAUSED
            return true
        } else {
            return false
        }
    }

    func resumeRecording() -> Bool {
        if status == CurrentRecordingStatus.PAUSED {
            audioRecorder.record()
            status = CurrentRecordingStatus.RECORDING
            return true
        } else {
            return false
        }
    }

    func getCurrentStatus() -> CurrentRecordingStatus {
        return status
    }

}
