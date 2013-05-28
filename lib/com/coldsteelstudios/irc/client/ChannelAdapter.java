package com.coldsteelstudios.irc.client;

abstract public class ChannelAdapter implements ChannelListener {

	public void topicChanged(Channel c) {}

	public void usersChanged(Channel c) {}
}
