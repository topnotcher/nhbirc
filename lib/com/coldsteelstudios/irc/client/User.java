
package com.coldsteelstudios.irc.client;

import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * NOTE: In general, references to this object shouldn't be kept;
 * they should be obtained through SyncManager.getUser() each time they are needed.
 */
public class User implements Comparable<User>, Iterable<Channel> {

	private String nick;

	private String host = null;

	private String user;

	//list of joined channels...(not currently used)
	private List<Channel> channels;

	public User(String nick) {
		nick(nick);
		channels = java.util.Collections.synchronizedList( new LinkedList<Channel>());
	//	channels = new LinkedList<Channel>();
	}
	
	public void nick(String nick) {
		this.nick = nick;
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
		addChannel(c);
	}

	public void addChannel(Channel c) {

		for (Channel channel : channels) {
			if (c.equals(channel))
				return;
		}
				
		channels.add(c);
	}

	public int numChannels() {
		return channels.size();
	}

	public void part(Channel c) {
		removeChannel(c);
	}

	private void removeChannel(Channel c) {
		for (Channel channel : channels)  {
			if (c.equals(channel)) {
				channels.remove(c);
				return;
			}
		}

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

	public Iterator<Channel> iterator() {
		return channels.iterator();
	}
}
