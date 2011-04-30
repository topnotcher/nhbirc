
package client;

import irc.*;

import java.util.List;
import java.util.StringTokenizer;

public class SyncManager implements MessageHandler {

	Connection irc;

	public SyncManager( Connection irc ) {
		this.irc = irc;

		irc.addMessageHandler(this)
			.addType( MessageType.NICKCHANGE )
			.addType( MessageType.JOIN )
			.addType( MessageType.QUIT )
			.addType( MessageType.PART )
			.addType( MessageType.NAME )
			.addType( MessageType.TOPIC )
			.addType( MessageType.TOPICCHANGE )
	//		.addType( MessageType.WHO )
		;
	}
	private void print(String m) {
		System.out.println(m);
	}
	public void handle( Message m ) {

		System.out.println(m.getRaw());

		User user;

		switch ( m.getType() ) {
			
			case JOIN:
				print( m.getSource() + " JOINS " + m.getTarget() );
				getUserFromTarget( m.getSource() ).join( getChannel(m.getTarget().getChannel()) );
				break;

			case NAME:
				handleNames(m);
				break;

			case NICKCHANGE:
				user = getUserFromTarget( m.getSource() );

				//change the nick, notify the channels
				user.nick( m.getTarget().getNick() );
		
				print( m.getSource() +" CHANGES NICK TO "+ m.getTarget() );
				break;

			case QUIT:
				print(m.getSource() + " QUITS ("+m.getMessage()+")");

				user = getUserFromTarget(m.getSource());
				user.quit();

				break;

			//@TODO: when I part, remove the channel, then loop through the channels users
			//and remove all the users from that channel.
			//if a user is in 0 channels, then remove the user from the hash map...
			case PART:
				print(m.getSource() + " PARTS " + m.getTarget() + " (" + m.getMessage() + ")" );

				user = getUserFromTarget(m.getSource());
				user.part( getChannel( m.getTarget().getChannel() ) );

				if ( user.getNick().equals( irc.nick() ) ) 
					getChannel( m.getTarget().getChannel() ).destroy();

				break;

			case TOPIC:
				getChannel( m.getArg(2) ).setTopic(m.getMessage());
				break;

			case TOPICCHANGE:
				//#:m!topnotcher@13.37 TOPIC #foo :this is a new topic
				getChannel( m.getTarget().getChannel() ).setTopic(m.getMessage());
				break;

			default:
				print("????");

		}

	}

	private void handleNames(Message m) {

		/**
		 * On bulk add operations,
		 * the Channel relies on the SyncManager
		 * to call usersChanged() at the end of the users list
		 */
		if ( m.getCode() == MessageCode.RPL_ENDOFNAMES ) {
			getChannel( m.getArg(2) ).usersChanged();
			return;
		}

		Channel c = getChannel( m.getArg(3) );

		StringTokenizer st = new StringTokenizer( m.getMessage(), " ");

		User u;
		String nick;
		while ( st.hasMoreTokens() ) {
			nick = st.nextToken();

			ChannelUser.Mode mode = ChannelUser.Mode.getMode( nick.charAt(0) );

			//if there is a mode...
			if ( mode != ChannelUser.Mode.NONE )
				nick = nick.substring(1);

			u = getUser( nick );
			u.addChannel(c);

			c.addUserToList( u );

			c.setUserMode( u, mode );
		}
	}

	public User getUser(String nick) {
		return User.get(nick);
	}

	private User getUserFromTarget(MessageTarget tg) {


		String nick = tg.getNick();
		String host = null, ident = null;

		User user = getUser(nick);

		user.setHost(host);
		user.setUser(ident);
	
		return user;
	}

	public Channel getChannel(String name) {
		return Channel.get(name);
	}
}
