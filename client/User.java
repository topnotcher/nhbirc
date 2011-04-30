
package client;

import java.util.List;
import java.util.HashMap;


public class User implements Comparable<User> {

	private String nick;
	private String host = null;
	private String user;

	private List<Channel> channels;

	private static HashMap<String,User> users = new HashMap<String,User>();

	/**
	 * at least we'll have a nick...
	 */
	private User(String nick) {
		this.nick = nick;
		channels = new util.LinkedList<Channel>();

		users.put(nick,this);
	}

	public synchronized static User get(String nick) {
		User ret = users.get(nick);

		if (ret == null)
			ret = new User(nick);

		return ret;
	}
	
	public synchronized void suicide() {
		users.remove(nick);
		nick = null;
		channels = null;
	}

	public String getNick() {
		return this.nick;
	}

	public String getUser() {
		return this.user;
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		if (host == null) return;

		this.host = host;
	}

	public void setUser(String user) {
		if (user == null) return;
		this.user = user;
	}

	public List<Channel> getChannels() {
		return channels;
	}

	public void join(Channel c) {
		c.addUser(this);
		addChannel(c);
	}

	void addChannel(Channel c) {

		for (Channel channel : channels) 
			if (c.equals(channel))
				return;
				
		channels.add(c);
	}

	public int numChannels() {
		return channels.size();
	}

	public synchronized void nick(String nick) {

		users.remove(this.nick);

		this.nick = nick;

		users.put(this.nick,this);

		for (Channel channel: channels)
			channel.usersChanged();
	}

	public void quit() {
		for (Channel channel : channels) 
			channel.delUser(this);
		suicide();
	}

	public void part(Channel c) {
		c.delUser(this);
		removeChannel(c);

	}

	void removeChannel(Channel c) {
		for (Channel channel : channels) 
			if (c.equals(channel)) {
				channels.remove(c);
				return;
			}

		if (numChannels() == 0)
			suicide();
	}

	public boolean equals(User u) {
		return u.nick.equals(nick);
	}

	public String toString() {
		return nick;
	}

	public int compareTo(User user) {
		return this.nick.compareTo( user.nick );
	}
}
