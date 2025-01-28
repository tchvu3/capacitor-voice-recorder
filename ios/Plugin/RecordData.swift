import Foundation

public struct RecordData {
    public let recordDataBase64: String?
    public let uri: String?
    public let mimeType: String
    public let msDuration: Int

    public func toDictionary() -> [String: Any] {
        var dict: [String: Any] = [
            "mimeType": mimeType,
            "msDuration": msDuration,
            "recordDataBase64": recordDataBase64 ?? ""
        ]
        if let uri = uri {
            dict["uri"] = uri
        }
        return dict
    }
}
