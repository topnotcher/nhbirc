package com.coldsteelstudios.irc.client;

public interface ChannelListener {

	public void topicChanged(Channel c);

	public void usersChanged(Channel c);

	public void part(Channel c, User u);

	public void join(Channel c, User u);

	public void kick(Channel c, User u);

	public void nick(Channel c, User u, String nick);

}
