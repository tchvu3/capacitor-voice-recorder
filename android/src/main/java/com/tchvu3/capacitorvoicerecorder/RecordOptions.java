package com.tchvu3.capacitorvoicerecorder;

public class RecordOptions {

    private String directory;
    private String subDirectory;

    public RecordOptions(String directory, String subDirectory) {
        this.directory = directory;
        this.subDirectory = subDirectory;
    }

    public String getDirectory() {
        return directory;
    }

    public String getSubDirectory() {
        return subDirectory;
    }
}
