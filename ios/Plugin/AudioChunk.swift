import Foundation

struct AudioChunk {
    let recordDataBase64: String
    let msDuration: Int
    let mimeType: String
    let chunkIndex: Int
    let isFinalChunk: Bool

    func toDictionary() -> [String: Any] {
        return [
            "recordDataBase64": recordDataBase64,
            "msDuration": msDuration,
            "mimeType": mimeType,
            "chunkIndex": chunkIndex,
            "isFinalChunk": isFinalChunk
        ]
    }
}
