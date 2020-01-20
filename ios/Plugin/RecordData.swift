//
//  Created by Avihu Harush on 20/01/2020
//

import Foundation

struct RecordData {
    public let recordDataBase64: String?
    public let msDuration: Int
    
    public func toDictionary() -> Dictionary<String, Any> {
        return [ "recordDataBase64": recordDataBase64 ?? "", "msDuration": msDuration ]
    }
}
