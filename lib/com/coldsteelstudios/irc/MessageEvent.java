package com.coldsteelstudios.irc;

/**
 * A MessageEvent is a message, combined with a 
 * source and a subscription.
 */
public class MessageEvent {

	private Message msg;
	private Connection irc;
	private EventManager  man;

	MessageEvent(Connection irc, Message msg) {
		this.irc = irc;
		this.man = man;
		this.msg = msg;
	}

	public Connection getSource() {
		return irc;
	}

	public Message getMessage() {
		return msg;
	}
}
