package com.coldsteelstudios.irc;

/**
 * A MessageEvent is a message, combined with a 
 * source and a subscription.
 */
public class MessageEvent {

	private Message msg;
	private Connection irc;
	private MessageSubscription sub;

	MessageEvent(Connection irc, MessageSubscription sub, Message msg) {
		this.irc = irc;
		this.sub = sub;
		this.msg = msg;
	}

	public Connection getSource() {
		return irc;
	}

	public Message getMessage() {
		return msg;
	}

	/**
	 * Unregister the handler that that received this event.
	 */
	public void unregister() {
		sub.unregister();
	}
}
