package com.log.src.constant;

public enum Error {
    FILE_PATH_INVALID("Unable to read the file, Please check the file path."),
    LOG_FILE_INFORMAT("Error while reading log file data, File records format incorrect.");

    public final String msg;

    Error(String msg) {
        this.msg = msg;
    }
}
