
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
	public void handle( MessageEvent e ) {

		Message m = e.getMessage();

		User user;

		switch ( m.getType() ) {
			
			case JOIN:
				getUserFromTarget( m.getSource() ).join( getChannel(m.getTarget().getChannel()) );
				break;

			case NAME:
				handleNames(m);
				break;

			case NICKCHANGE:
				user = getUserFromTarget( m.getSource() );

				//change the nick, notify the channels
				user.nick( m.getTarget().getNick() );
		
				break;

			case QUIT:

				user = getUserFromTarget(m.getSource());
				user.quit();

				break;

			//@TODO: when I part, remove the channel, then loop through the channels users
			//and remove all the users from that channel.
			//if a user is in 0 channels, then remove the user from the hash map...
			case PART:

				user = getUserFromTarget(m.getSource());
				user.part( getChannel( m.getTarget().getChannel() ) );

				if ( m.isFromMe() ) 
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
				break;
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
