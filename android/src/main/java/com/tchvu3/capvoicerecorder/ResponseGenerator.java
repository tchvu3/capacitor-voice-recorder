package com.tchvu3.capvoicerecorder;

import com.getcapacitor.JSObject;


public abstract class ResponseGenerator {

    private static final String RESPONSE_KEY = "value";

    public static JSObject fromBoolean(boolean value) {
        return value ? successResponse() : failResponse();
    }

    public static JSObject successResponse() {
        JSObject success = new JSObject();
        success.put(RESPONSE_KEY, true);
        return success;
    }

    public static JSObject failResponse() {
        JSObject success = new JSObject();
        success.put(RESPONSE_KEY, false);
        return success;
    }

    public static JSObject dataResponse(Object data) {
        JSObject success = new JSObject();
        success.put(RESPONSE_KEY, data);
        return success;
    }
}
