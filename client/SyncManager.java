
package client;

import irc.*;

import java.util.List;
import java.util.HashMap;
import java.util.StringTokenizer;

public class SyncManager implements MessageHandler {

	Connection irc;

	HashMap<String,User> users;
	HashMap<String,Channel> channels;
	
	public SyncManager( Connection irc ) {
		this.irc = irc;

		channels = new HashMap<String,Channel>();
		users = new HashMap<String,User>();

		irc.addMessageHandler(this)
			.addType( MessageType.NICKCHANGE )
			.addType( MessageType.JOIN )
			.addType( MessageType.QUIT )
			.addType( MessageType.PART )
			.addType( MessageType.NAME )
	//		.addType( MessageType.WHO )
		;
	}

	public void handle( Message m ) {

		System.out.println(m.getRaw());

		switch ( m.getType() ) {

			case JOIN:
				handleJoin(m);
				break;

			case NAME:
				handleNames(m);
				break;

			case NICKCHANGE:

				for ( Channel channel : getUserFromTarget( m.getSource() ).getChannels() ) 
					channel.usersChanged();

				print( m.getSource() +" CHANGES NICK TO "+ m.getTarget() );
				break;

			case QUIT:
				print(m.getSource() + " QUITS ("+m.getMessage()+")");
			
				User user = getUserFromTarget(m.getSource());

				for (Channel channel : user.getChannels() )
					channel.delUser(user);

				break;

			case PART:
				print(m.getSource() + " PARTS " + m.getTarget() + " (" + m.getMessage() + ")" );

				getChannel( m.getTarget().getChannel() ).delUser( getUserFromTarget(m.getSource()) );
				break;

			default:
				print("????");

		}

	}

	private void handleNames(Message m) {

		if ( m.getCode() == MessageCode.RPL_ENDOFNAMES ) {
			getChannel( m.getArg(2) ).usersChanged();
			return;
		}

		Channel c = getChannel( m.getArg(3) );

		StringTokenizer st = new StringTokenizer( m.getMessage(), " ");

		List<User> names = new util.LinkedList<User>();

		User u;
		String nick;
		while ( st.hasMoreTokens() ) {
			nick = st.nextToken();

			switch (nick.charAt(0)) {
				case '~':
				case '&':
				case '@':
				case '%':
				case '+':
					nick = nick.substring(1);
			}

			u = getUser( nick );
			u.addChannel(c);
			names.add(u);
		}

		c.addUsers(names);
	}

	private void handleJoin(Message m) {
		print( m.getSource() + " JOINS " + m.getTarget() );

		Channel c = getChannel(m.getTarget().getChannel());

		getUserFromTarget( m.getSource() ).join(c);
			
	}

	public User getUser(String nick) {
		User user = users.get(nick);

		if ( user == null ) {
		 	user = newUser(nick, null, null);
		}

		return user;
	}

	private User newUser(String nick, String user, String host) {
		User u = new User(nick,user,host);
		users.put(nick,u);

		return u;
	}

	private User getUserFromTarget(MessageTarget tg) {


		String nick = tg.getNick();
		String host = null, ident = null;

		User user = getUser(nick);;


		if ( tg.scope( MessageTarget.Scope.USER ) ) {
			host = tg.getHost();
			ident = tg.getUser();
		}
						
		if ( user == null ) {
			user = newUser(nick,ident,host);
		} else if (host != null) {
			user.setUser(ident);
			user.setHost(host);
		}
	
		return user;
	}

	public Channel getChannel(String name) {
		Channel c;

		if ( (c = _getChannel(name)) != null )
			return c;

		else {
			c = new Channel(name);
			addChannel(c);
			return c;
		}
	}


	private Channel _getChannel(String name) {

		return channels.get(name);
	}

	private void addChannel(Channel c) {
		channels.put(c.getName(), c);
	}

	public void print(String ln) {
		System.out.println(ln);
	}
}
