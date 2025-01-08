package com.tchvu3.capacitorvoicerecorder;

import com.getcapacitor.JSObject;

public class RecordData {

  private String recordDataBase64;
  private String mimeType;
  private int msDuration;
  private String path;

  public RecordData(String recordDataBase64, int msDuration, String mimeType, String path) {
        this.recordDataBase64 = recordDataBase64;
        this.msDuration = msDuration;
        this.mimeType = mimeType;
        this.path = path;
  }

    public String getPath() { return path; }

    public void setPath(String path) { this.path = path; }

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
        toReturn.put("path", this.path);
        return toReturn;
    }
}
