//
//  CustomMediaRecorder.swift
//  Plugin
//
//  Created by Avihu Harush on 17/01/2020.
//  Copyright © 2020 Max Lynch. All rights reserved.
//

import Foundation
import AVFoundation

class CustomMediaRecorder {
    var recordingSession: AVAudioSession!
    var audioRecorder: AVAudioRecorder!
    
    init() {
        do {
            self.recordingSession = AVAudioSession.sharedInstance()
//            try self.recordingSession.setCategory(.playAndRecord, mode: .default)
            try self.recordingSession.setActive(true)
        } catch {
            
        }
    }
    
}
