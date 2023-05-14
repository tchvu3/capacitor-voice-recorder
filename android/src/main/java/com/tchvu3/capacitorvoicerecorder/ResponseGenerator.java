package com.tchvu3.capacitorvoicerecorder;

import com.getcapacitor.JSObject;

public class ResponseGenerator {

    private static final String VALUE_RESPONSE_KEY = "value";
    private static final String STATUS_RESPONSE_KEY = "status";

    public static JSObject fromBoolean(boolean value) {
        return value ? successResponse() : failResponse();
    }

    public static JSObject successResponse() {
        JSObject success = new JSObject();
        success.put(VALUE_RESPONSE_KEY, true);
        return success;
    }

    public static JSObject failResponse() {
        JSObject success = new JSObject();
        success.put(VALUE_RESPONSE_KEY, false);
        return success;
    }

    public static JSObject dataResponse(Object data) {
        JSObject success = new JSObject();
        success.put(VALUE_RESPONSE_KEY, data);
        return success;
    }

    public static JSObject statusResponse(CurrentRecordingStatus status) {
        JSObject success = new JSObject();
        success.put(STATUS_RESPONSE_KEY, status.name());
        return success;
    }
}
