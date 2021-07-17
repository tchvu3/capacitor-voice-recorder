//
//  ResponseGenerator.swift
//  Plugin
//
//  Created by Avihu Harush on 17/07/2021.
//

import Foundation

struct ResponseGenerator {
    
    private static let RESPONSE_KEY = "value"
    
    public static func fromBoolean(_ value: Bool) -> Dictionary<String, Bool> {
        return value ? successResponse() : failResponse()
    }
    
    public static func successResponse() -> Dictionary<String, Bool> {
        return [RESPONSE_KEY: true]
    }
    
    public static func failResponse() -> Dictionary<String, Bool> {
        return [RESPONSE_KEY: false]
    }
    
    public static func dataResponse(_ data: Any) -> Dictionary<String, Any> {
        return [RESPONSE_KEY: data]
    }
}
