package com.volt.gen.exception;

public class VoltGenException extends Exception {
    public VoltGenException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public VoltGenException(String msg) {
        super(msg);
    }

    public VoltGenException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getMessage());
        Throwable curr = getCause();
        while (null != curr) {
            sb.append(": ");
            sb.append(curr.getMessage());
            curr = curr.getCause();
        }
        return sb.toString();
    }
}
