import Foundation

struct RecordData {

    let recordDataBase64: String?
    let mimeType: String
    let msDuration: Int

    func toDictionary() -> [String: Any] {
        return [
            "recordDataBase64": recordDataBase64 ?? "",
            "msDuration": msDuration,
            "mimeType": mimeType
        ]
    }

}
