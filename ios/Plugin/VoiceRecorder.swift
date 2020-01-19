import Foundation
import Capacitor
import AVFoundation

@objc(VoiceRecorder)
public class VoiceRecorder: CAPPlugin {
    
    private var customMediaRecorder: CustomMediaRecorder? = nil
    
    @objc func canDeviceVoiceRecord(_ call: CAPPluginCall) {
        call.resolve(ResponseGenerator.successResponse())
    }
    
    @objc func requestAudioRecordingPermission(_ call: CAPPluginCall) {
        AVAudioSession.sharedInstance().requestRecordPermission { granted in
            if granted {
                call.resolve(ResponseGenerator.successResponse())
            } else {
                call.resolve(ResponseGenerator.failResponse())
            }
        }
    }
    
    @objc func hasAudioRecordingPermission(_ call: CAPPluginCall) {
        call.resolve(ResponseGenerator.fromBoolean(doesUserGaveAudioRecordingPermission()))
    }
    
    
    @objc func startRecording(_ call: CAPPluginCall) {
        if(!doesUserGaveAudioRecordingPermission()) {
            call.reject(Messages.MISSING_PERMISSION)
            return
        }
        
        if(customMediaRecorder != nil) {
            call.reject(Messages.ALREADY_RECORDING)
            return
        }
        
        customMediaRecorder = CustomMediaRecorder()
        if(customMediaRecorder == nil) {
            call.reject(Messages.CANNOT_RECORD_ON_THIS_PHONE)
            return
        }
        
        customMediaRecorder!.startRecording()
    }
    
    @objc func stopRecording(_ call: CAPPluginCall) {
        if(customMediaRecorder == nil) {
            call.reject(Messages.RECORDING_HAS_NOT_STARTED)
            return
        }
        
        customMediaRecorder?.stopRecording()
        
        let audioFileUrl = customMediaRecorder?.getOutputFile()
        if(audioFileUrl == nil) {
            customMediaRecorder = nil
            call.reject(Messages.FAILED_TO_FETCH_RECORDING)
            return
        }
        
        let base64AudioFile = readFileAsBase64(audioFileUrl)
        customMediaRecorder = nil
        if base64AudioFile == nil {
            call.reject(Messages.FAILED_TO_FETCH_RECORDING)
        } else {
            call.resolve(ResponseGenerator.dataResponse(base64AudioFile!))
        }
    }
    
    func doesUserGaveAudioRecordingPermission() -> Bool {
        return AVAudioSession.sharedInstance().recordPermission() == AVAudioSession.RecordPermission.granted
    }
    
    func readFileAsBase64(_ filePath: URL?) -> String? {
        if(filePath == nil) {
            return nil
        }
        
        do {
            let fileData = try Data.init(contentsOf: filePath!)
            let fileStream = fileData.base64EncodedString(options: NSData.Base64EncodingOptions.init(rawValue: 0))
            return fileStream
        } catch {}
        
        return nil
    }
    
}
