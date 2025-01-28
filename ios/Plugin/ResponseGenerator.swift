import Foundation

struct ResponseGenerator {

    private static let VALUE_RESPONSE_KEY = "value"
    private static let STATUS_RESPONSE_KEY = "status"

    static func fromBoolean(_ value: Bool) -> [String: Bool] {
        return value ? successResponse() : failResponse()
    }

    static func successResponse() -> [String: Bool] {
        return [VALUE_RESPONSE_KEY: true]
    }

    static func failResponse() -> [String: Bool] {
        return [VALUE_RESPONSE_KEY: false]
    }

    static func dataResponse(_ data: Any) -> [String: Any] {
        return [VALUE_RESPONSE_KEY: data]
    }

    static func statusResponse(_ data: CurrentRecordingStatus) -> [String: String] {
        return [STATUS_RESPONSE_KEY: data.rawValue]
    }

}
