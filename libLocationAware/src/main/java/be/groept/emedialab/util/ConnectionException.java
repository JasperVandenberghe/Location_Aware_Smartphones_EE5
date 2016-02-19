package be.groept.emedialab.util;

public class ConnectionException extends Exception{
    public ConnectionException(){
        super();
    }

    public ConnectionException(String message, Throwable exception) {
        super(message, exception);
    }

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(Throwable exception) {
        super(exception);
    }
}
