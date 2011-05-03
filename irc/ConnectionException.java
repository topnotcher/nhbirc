package irc;

public class ConnectionException extends Exception { 
	public ConnectionException(String msg) {
		super(msg);
	}

	public ConnectionException(Throwable e) {
		super(e);
	}
}
