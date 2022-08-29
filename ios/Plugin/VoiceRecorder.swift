import Foundation

@objc public class VoiceRecorder: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
