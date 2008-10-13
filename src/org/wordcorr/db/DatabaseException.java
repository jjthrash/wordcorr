package org.wordcorr.db;

/**
 * DatabaseException wraps database related exceptions.
 * @author Keith Hamasaki
 **/
public class DatabaseException extends Exception {

    public DatabaseException(String msg) {
        this(msg, null);
    }

    public DatabaseException(Throwable rootCause) {
        this(rootCause.toString(), rootCause);
    }

    public DatabaseException(String msg, Throwable rootCause) {
        super(msg);
        _rootCause = rootCause;
    }

    public final Throwable getRootCause() {
        return _rootCause;
    }

    public void printStackTrace() {
        super.printStackTrace();
        System.err.println("----- Root Cause -----");
        _rootCause.printStackTrace();
    }

    public void printStackTrace(java.io.PrintWriter wrt) {
        super.printStackTrace(wrt);
        wrt.write("----- Root Cause -----\n");
        _rootCause.printStackTrace(wrt);
    }

    public void printStackTrace(java.io.PrintStream wrt) {
        super.printStackTrace(wrt);
        wrt.println("----- Root Cause -----");
        _rootCause.printStackTrace(wrt);
    }

    private final Throwable _rootCause;
}
