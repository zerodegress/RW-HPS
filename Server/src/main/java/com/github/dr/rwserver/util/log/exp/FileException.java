package com.github.dr.rwserver.util.log.exp;

public class FileException extends Exception {
    public FileException(String type) {
        super(com.github.dr.rwserver.util.log.ErrorCode.valueOf(type).getError());
    }
}