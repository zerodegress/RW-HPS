package com.github.dr.rwserver.util.log.exp;

/**
 * @author Dr
 */
public class VariableException extends RuntimeException {
    public VariableException(String type) {
        super(com.github.dr.rwserver.util.log.ErrorCode.valueOf(type).getError());
    }

    public static class ArrayRuntimeException extends RuntimeException {
        public ArrayRuntimeException(String type) {
            super(type);
        }
    }

    public static class MapRuntimeException extends RuntimeException {
        public MapRuntimeException(String type) {
            super(type);
        }
    }
}