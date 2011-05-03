package client;

import java.util.List;
import java.util.Iterator;
import java.util.HashMap;


//reflection
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Synchs basic information about a channel.
 */
public class Channel implements Iterable<User> {

	/**
	 * This is necessarily set via the constructor.
	 */
	private String name;

	/**
	 * Channel topic
	 */
	private String topic = null;

	/**
	 * list of users on the channel
	 * ChannelUser encapsulates User...
	 */
	private List<ChannelUser> users;

	/**
	 * List of ChannelListeners wanting 
	 * to receive updats when this channel's state changes
	 * (user list, topic, etc)
	 */
	private List<ChannelListener> subs;


	private static HashMap<String,Channel> channels = new HashMap<String,Channel>();

	/**
	 * Create an empty channel with a name...
	 */
	private Channel(String name) {
		this.name = name;

		//pre-instantiate this as it should always contain
		//at least one element: the current client...
		users = new util.LinkedList<ChannelUser>();

		channels.put(name,this);
	}

	/**
	 * Getter for the channel registry.
	 * package-private: should only be called via SyncManager
	 */
	synchronized static Channel get(String name) {
		Channel ret = channels.get(name);

		if (ret == null)
			ret = new Channel(name);

		return ret;
	}

	/**
	 * Get the channel's name.
	 *
	 * Note there is no setter for name:
	 * It must be set on instantiation, 
	 * and cannot be changed.
	 *
	 * @return the channel's name.
	 */
	public String getName() {
		return name;
	}


	/**
	 * Get the channel's topic, if it is synched.
	 *
	 * @return the topic if it is synched, or "" if it is not.
	 */
	public String getTopic() {

		if (topic == null) return "";

		return topic;
	}


	/**
	 * Intended to be called by SyncManager when the topic is
	 * received/changed.
	 *
	 * @string topic the new topic.
	 */
	void setTopic(String topic) {
		this.topic = topic;
		topicChanged();
	}

	/**
	 * Get a channel user given a user.
	 *
	 * @param u a User
	 *
	 * @return a ChannelUser encapsulating u, or null if there isn't one on this channel.
	 */
	private ChannelUser getChannelUser(User u) {

		//if the user is on the channel, return the user...
		for (ChannelUser user : users) if ( user.equals(u) ) return user;

			return null;
	}

	/**
	 * Handle the dirty work of adding a user to a channel...
	 * This is for internal use: it Does*Not*Fire*usersChanged*
	 */
	synchronized void addUserToList(User user) {
		

		if ( getChannelUser(user) != null )
			return;

		//@TODO: synchronize access to the users list...
		users.add( new ChannelUser(user) );
	}

	//Add a single user.
	public void addUser(User u) {

		addUserToList( u );

		usersChanged();
	}


	synchronized void delUser(User user) {

		ChannelUser cuser = getChannelUser( user );

		//if the user was found on the channel,
		//remove from the list and fire a change...
		if ( cuser != null ) {
			users.remove( cuser );	
			usersChanged();
		}
	}

	/**
	 * Self-destruct when something
	 * decides this channel is no longer needed.
	 *
	 * In theory SyncManager makes reaonable decisions regarding destroying
	 * channel objects.  It is potentially possible for a channel to be destroyed()
	 * while the chanel window remains open, which would cause the window to be 
	 * "disconnected" from the channel's state. All of the client UI code needs some rethinking...
	 */
	synchronized void destroy() {
		channels.remove(name);

		for (User u : this) 
			u.removeChannel(this);

		users.clear();
		subs.clear();
		name = "";
	}

	synchronized void setUserMode(User user, ChannelUser.Mode mode) {
		ChannelUser cuser = getChannelUser(user);

		///@TODO
		if (cuser == null)
			throw new RuntimeException("Trying to set mode on a user who isn't in the channel!!!!");

		cuser.setMode(mode);
	}

	public int numUsers() {
		return (users == null) ? 0 : users.size();
	}

	public ChannelUser getUser(int idx) {
		return users.get(idx);
	}

	public Iterator<ChannelUser> iterChannelUsers() {
		return users.iterator();
	}

	public Iterator<User> iterator() {
		return new Iterator<User>() {
			
			private Iterator<ChannelUser> it;

			{ it = users.iterator(); }
		
			public boolean hasNext() {
				return it.hasNext();
			}

			public User next() {
				return it.next().getUser();
			}

			public void remove() throws UnsupportedOperationException {
				throw new UnsupportedOperationException("Remove not supported.");
			}
		};
	}

	public boolean equals(Channel c) {
		return c.name.equals(name);
	}




//////NOTIFICATION STUFF BELOW ///////


	void usersChanged() {
		//@TODO make this better :/
		util.ListSorter.sort(users );

		notifyListeners("usersChanged");
	}

	void topicChanged() {
		notifyListeners("topicChanged");
	}

	private void notifyListeners(String event) {
		
		if ( subs == null ) return;

		Class[] paramtypes = {this.getClass()};
		Object[] params = {this};

		for (ChannelListener l : subs) {
			try {
				//FIND the method (or exception if not defined)
				Method command = Class.forName("client.ChannelListener").getDeclaredMethod(event,paramtypes);
		
				//invoke the method
				command.invoke(l,params);
			} catch (NoSuchMethodException e) {

				//won't happen.
			} catch (IllegalAccessException e) {

			} catch (InvocationTargetException e) {

			} catch (ClassNotFoundException e) {

			}
		}
	}


	public void addChannelListener(ChannelListener c) {
		if (subs == null)
			subs = new util.LinkedList<ChannelListener>();

		subs.add(c);
	}

	public void removeChannelListener(ChannelListener c) {
		if (subs == null) return;

		subs.remove(c);
	}

	/**
	 * @TODO remove listener
	 * VERY important: ChannelWindows need to void the subscription when they go away...
	 */
}
