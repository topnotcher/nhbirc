package irc;

class ConnectionStateException extends RuntimeException {
	public ConnectionStateException(String msg) {
		super(msg);
	}
}
