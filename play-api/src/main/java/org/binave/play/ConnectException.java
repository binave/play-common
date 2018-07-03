package org.binave.play;

/**
 * 带有状态码的链接异常
 *
 * @author by bin jin on 2017/7/27.
 * @since 1.8
 */
public class ConnectException extends RuntimeException {

    private int statusCode;

    public ConnectException(int statusCode) {
        super();
        this.statusCode = statusCode;
    }

    public ConnectException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public ConnectException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
