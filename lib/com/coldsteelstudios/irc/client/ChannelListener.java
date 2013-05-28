package com.coldsteelstudios.irc.client;

public interface ChannelListener {

	public void topicChanged(Channel c);

	public void usersChanged(Channel c);
}
