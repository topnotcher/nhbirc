
package com.coldsteelstudios.irc.client;

import com.coldsteelstudios.irc.*;

import java.util.List;
import java.util.StringTokenizer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class SyncManager implements MessageHandler {

	protected Connection irc;

	protected static Map<String,Channel> channels = new ConcurrentHashMap<String,Channel>();

	protected static Map<String,User> users = new ConcurrentHashMap<String,User>();


	public SyncManager( Connection irc ) {
		this.irc = irc;

		irc.addMessageHandler(this)
			.addType( MessageType.NICKCHANGE )
			.addType( MessageType.JOIN )
			.addType( MessageType.QUIT )
			.addType( MessageType.PART )
			.addType( MessageType.NAME )
			.addType( MessageType.KICK )
			.addType( MessageType.TOPIC )
			.addType( MessageType.CHANNELMODE )
			.addType( MessageType.TOPICCHANGE )
		;

	}

	public void handle( MessageEvent e ) {

		Message m = e.getMessage();

		User user;

		switch ( m.getType() ) {
			
			case JOIN:
				join( getUserFromTarget( m.getSource() ), getChannel(m.getTarget().getChannel()) );
				break;

			case NAME:
				handleNames(m);
				break;

			case NICKCHANGE:
				nick( getUserFromTarget( m.getSource() ), m.getTarget().getNick() );
				break;

			case QUIT:
				quit(getUserFromTarget(m.getSource()));
				break;

			//@TODO: when I part, remove the channel, then loop through the channels users
			//and remove all the users from that channel.
			//if a user is in 0 channels, then remove the user from the hash map...
			case PART:
				user = getUserFromTarget(m.getSource());
				part( user, getChannel( m.getTarget().getChannel() ) );

				if ( m.isFromMe() ) 
					getChannel( m.getTarget().getChannel() ).destroy();

				break;

			case KICK:
				user = getUser(m.getArg(2));
				part( user, getChannel( m.getTarget().getChannel() ) );

				//@TODO
				if ( user.getNick().toLowerCase() == irc.nick().toLowerCase() ) 
					getChannel( m.getTarget().getChannel() ).destroy();

				break;





			case TOPIC:
				getChannel( m.getArg(2) ).setTopic(m.getMessage());
				break;

			case TOPICCHANGE:
				//#:m!topnotcher@13.37 TOPIC #foo :this is a new topic
				getChannel( m.getTarget().getChannel() ).setTopic(m.getMessage());
				break;

			case CHANNELMODE:
				//@TODO 
				//This is dirty and inefficient, but dealing with mode changes
				//is incredibly difficult as different servers
				//support different modes. This is the most reliable way
				//to keep the user list synched (well as far as the ops go)
				e.getSource().send(Priority.LOW, "NAMES" , m.getTarget().getChannel());
				break;

			default:
				//@TODO
				System.err.println("Received unknown message type: "+m.getType());
				break;
		}

	}

	private void handleNames(Message m) {

		if ( m.numArgs() < 3) return;

		/**
		 * On bulk add operations,
		 * the Channel relies on the SyncManager
		 * to call usersChanged() at the end of the users list
		 */
		if ( m.getCode() == MessageCode.RPL_ENDOFNAMES ) {
			getChannel( m.getArg(2) ).usersChanged();
			return;
		}

		//NAMES fubar = #channel :list
		//check # of arguments, and verify that there is not a * in the channel arg, which means this is
		//a names list "without a channel"
		if ( m.numArgs() < 5 || m.get(3).charAt(0) == '*' )  return;
	
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
		String lowernick = nick.toLowerCase();

		User ret = users.get(lowernick);

		if (ret == null) {
			ret = new User(nick);
			users.put(lowernick,ret);
		}
			

		return ret;
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
		Channel ret = channels.get(name);

		if (ret == null) {
			ret = new Channel(name);
			channels.put(name,ret);
		}
	

		return ret;
	}


	/**
 	 * Handle a nick change.
	 * Old user, new nick.
 	 */
	private void nick(User user, String nick) {

		String oldnick = user.getNick();
		
		user.nick(nick);
		users.put(nick, user);
		users.remove(oldnick);

		for (Channel channel: user.getChannels())
			//current user, old nick.
			channel.nick(user,oldnick);
	}
	
	/** 
 	 * handle a join.
 	 */
	private void join(User u, Channel c) {
		c.addUser(u);
		u.join(c);
	}

	/**
	 * Something decided this user doesn't need to be 
	 * synched anymore.
	 * (should probably remove user from all channels as a precaution....)
	 */ 
	private void removeUser(User u) {
		users.remove(u.getNick());
		u.getChannels().clear();
	}

	private void quit(User u) {
		for (Channel channel : u.getChannels()) 
			channel.delUser(u);

		removeUser(u);
	}


	private void part(User u, Channel c) {
		c.delUser(u);
		u.part(c);

		if ( u.numChannels() == 0 )
			removeUser(u);
	}



	/**
	 * In theory SyncManager makes reaonable decisions regarding `destroying
	 * channel objects.  It is potentially possible for a channel to be destroyed()
	 * while the chanel window remains open, which would cause the window to be 
	 * "disconnected" from the channel's state. All of the client UI code needs some rethinking...
	 */
	public void destroyChannel(Channel c) {
		channels.remove(c.getName());

		for (User u : c) 
			part(u,c);

		c.destroy();
	}


}
