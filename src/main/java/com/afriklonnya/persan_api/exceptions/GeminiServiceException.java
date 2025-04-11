// GeminiServiceException.java
package com.afriklonnya.persan_api.exceptions;

public class GeminiServiceException extends RuntimeException {
    public GeminiServiceException(String message) {
        super(message);
    }

    public GeminiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}