package com.tchvu3.capacitorvoicerecorder;

import com.getcapacitor.JSObject;

public class AudioChunk {

    private final String recordDataBase64;
    private final int msDuration;
    private final String mimeType;
    private final int chunkIndex;
    private final boolean isFinalChunk;

    public AudioChunk(String recordDataBase64, int msDuration, String mimeType, int chunkIndex, boolean isFinalChunk) {
        this.recordDataBase64 = recordDataBase64;
        this.msDuration = msDuration;
        this.mimeType = mimeType;
        this.chunkIndex = chunkIndex;
        this.isFinalChunk = isFinalChunk;
    }

    public JSObject toJSObject() {
        JSObject obj = new JSObject();
        obj.put("recordDataBase64", recordDataBase64);
        obj.put("msDuration", msDuration);
        obj.put("mimeType", mimeType);
        obj.put("chunkIndex", chunkIndex);
        obj.put("isFinalChunk", isFinalChunk);
        return obj;
    }
}
