package com.tchvu3.capvoicerecorder;

import com.getcapacitor.JSObject;

import java.io.Serializable;

public class RecordData implements Serializable {

    private String recordDataBase64;
    private String mimeType;
    private int msDuration;

    public RecordData() {
    }

    public RecordData(String recordDataBase64, int msDuration, String mimeType) {
        this.recordDataBase64 = recordDataBase64;
        this.msDuration = msDuration;
        this.mimeType = mimeType;
    }

    public String getRecordDataBase64() {
        return recordDataBase64;
    }

    public void setRecordDataBase64(String recordDataBase64) {
        this.recordDataBase64 = recordDataBase64;
    }

    public int getMsDuration() {
        return msDuration;
    }

    public void setMsDuration(int msDuration) {
        this.msDuration = msDuration;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public JSObject toJSObject() {
        JSObject toReturn = new JSObject();
        toReturn.put("recordDataBase64", recordDataBase64);
        toReturn.put("msDuration", msDuration);
        toReturn.put("mimeType", mimeType);
        return toReturn;
    }
}
