package irc;

public class MessageTarget {

	public enum Scope { CHANNEL, NICK, USER, SERVER, NONE; }

	private Scope scope;

	private String target;

	/**
	 * package private
	 */
	MessageTarget( String target ) {
		this.target = target;

		if ( target.startsWith("#") )
			scope = Scope.CHANNEL;

		else if ( target.indexOf('@') != -1 )
			scope = Scope.USER;

		else if ( target.indexOf('.') != -1 )
			this.scope = Scope.SERVER;

		else 
			this.scope = Scope.NICK;
	}

	MessageTarget() {
		this.scope = Scope.NONE;
	}

	public boolean scope(Scope s) {
		return this.scope == s;
	}

	//@TODO exception types

	public String getChannel() {

		if ( this.scope != Scope.CHANNEL )
			throw new RuntimeException("Cannot get channel that scope.");

		return this.target;
	}

	public String getNick() {
		if ( this.scope == Scope.USER )
			return target.substring( 0, target.indexOf('!') );

		if ( this.scope == Scope.NICK )
			return target;

		throw new RuntimeException("Cannot get nick from that scope.");
	}

	public String getUser() {
		if ( this.scope != Scope.USER ) 
			throw new RuntimeException("Cannot get user from that scope.");


		return target.substring( target.indexOf('!')+1, target.indexOf('@') ); 
	}

	public String getHost() {

		if ( this.scope == Scope.SERVER )
			return this.target;

		if  ( this.scope == Scope.USER ) 
			return target.substring( target.indexOf('@') + 1 );
	
		throw new RuntimeException("Cannot get host from that scope.");
	}
}
