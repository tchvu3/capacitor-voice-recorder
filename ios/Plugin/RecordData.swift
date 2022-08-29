import Foundation

struct RecordData {
    
    public let recordDataBase64: String?
    public let mimeType: String
    public let msDuration: Int
    
    public func toDictionary() -> Dictionary<String, Any> {
        return [
            "recordDataBase64": recordDataBase64 ?? "",
            "msDuration": msDuration,
            "mimeType": mimeType
        ]
    }
    
}
