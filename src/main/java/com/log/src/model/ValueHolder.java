package com.log.src.model;

public class ValueHolder {
    private boolean fileReadingCompleted;

    private boolean isErrorInRecord;

    public boolean isFileReadingCompleted() {
        return fileReadingCompleted;
    }

    public void setFileReadingCompleted(boolean fileReadingCompleted) {
        this.fileReadingCompleted = fileReadingCompleted;
    }

    public boolean isErrorInRecord() {
        return isErrorInRecord;
    }

    public void setErrorInRecord(boolean errorInRecord) {
        isErrorInRecord = errorInRecord;
    }
}
