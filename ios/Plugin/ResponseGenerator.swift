import Foundation

struct ResponseGenerator {
    
    private static let VALUE_RESPONSE_KEY = "value"
    private static let STATUS_RESPONSE_KEY = "status"
    
    public static func fromBoolean(_ value: Bool) -> Dictionary<String, Bool> {
        return value ? successResponse() : failResponse()
    }
    
    public static func successResponse() -> Dictionary<String, Bool> {
        return [VALUE_RESPONSE_KEY: true]
    }
    
    public static func failResponse() -> Dictionary<String, Bool> {
        return [VALUE_RESPONSE_KEY: false]
    }
    
    public static func dataResponse(_ data: Any) -> Dictionary<String, Any> {
        return [VALUE_RESPONSE_KEY: data]
    }
    
    public static func statusResponse(_ data: CurrentRecordingStatus) -> Dictionary<String, String> {
        return [STATUS_RESPONSE_KEY: data.rawValue]
    }
    
}
