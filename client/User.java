
package client;

import java.util.List;

public class User {

	private String nick;
	private String host;
	private String user;

	private List<Channel> channels;

	/**
	 * at least we'll have a nick...
	 */
	public User(String nick) {
		this(nick,null,null);
	}

	public User(String nick, String user, String host) {
		this.nick = nick;
		this.user = user;
		this.host = host;

		//assume user is always on one channel (otherwise why are we synching???)
		channels = new util.LinkedList<Channel>();
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

	public String setNick() {
		return nick;	
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void join(Channel c) {
		c.addUser(this);
		addChannel(c);
	}

	void addChannel(Channel c) {
		this.channels.add(c);
	}

	public int numChannels() {
		return channels.size();
	}

	public void part(Channel c) {
		c.delUser(this);

		channels.remove(c);
	}
	
	public boolean equals(User u) {
		return u.nick.equals(nick);
	}

	public String toString() {
		return nick;
	}
}
