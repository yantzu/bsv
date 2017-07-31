package com.github.yantzu.bsv;

public class BsvException extends Exception {
    
    private static final long serialVersionUID = -4507828788021905915L;

    public BsvException() {
        super();
    }

    public BsvException(String s) {
        super(s);
    }

    public BsvException(String message, Throwable cause) {
        super(message, cause);
    }

    public BsvException(Throwable cause) {
        super(cause);
    }
}
