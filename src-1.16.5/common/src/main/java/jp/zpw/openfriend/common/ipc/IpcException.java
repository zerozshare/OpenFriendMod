/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.ipc;

public class IpcException extends Exception {
    private final int code;

    public IpcException(String message) {
        super(message);
        this.code = 0;
    }

    public IpcException(String message, int code) {
        super(message);
        this.code = code;
    }

    public IpcException(String message, Throwable cause) {
        super(message, cause);
        this.code = 0;
    }

    public int code() { return code; }

    public boolean isNotAuthenticated() { return code == -32001; }
    public boolean isAlreadyRunning()   { return code == -32002; }
    public boolean isNotRunning()       { return code == -32003; }
    public boolean isNotFound()         { return code == -32011; }
}
