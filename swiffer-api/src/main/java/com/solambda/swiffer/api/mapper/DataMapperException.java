package com.solambda.swiffer.api.mapper;

/**
 * Exception to be thrown from implementations of {@link DataMapper}
 * to signal that error occurred during serialization/deserialization.
 */
public class DataMapperException extends RuntimeException{

    private static final long serialVersionUID = 4757028072296262172L;

    public DataMapperException() {
    }

    public DataMapperException(String message) {
        super(message);
    }

    public DataMapperException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataMapperException(Throwable cause) {
        super(cause);
    }

    public DataMapperException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
