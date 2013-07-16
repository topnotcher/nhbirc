package com.coldsteelstudios.irc;


import java.util.regex.Pattern;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;


//a subscription to irc messages
public class MessageSubscription {
		
	private Set<MessageType> types = null;
	private Set<MessageCode> codes = null;

	private List<String> cmds = null;
	private List<Pattern> patterns = null;

	private MessageHandler handler;
		
	private Connection irc;

	public MessageSubscription(MessageHandler handler, Connection irc) {
		this.handler = handler;
		this.irc = irc;
	}

	/**
	 * Add a type to the subscription
	 * @return provides a fluent interface
	 */
	public MessageSubscription addType(MessageType type) {
		
		if  ( types == null ) 
			types = new TreeSet<MessageType>();

		types.add(type);

		return this;
	}

	/**
	 * Add a code to the subscription
	 * @return provides a fluent interface
	 */
	public MessageSubscription addCode(MessageCode code) {

		if  ( codes == null ) 
			codes = new TreeSet<MessageCode>();

		codes.add(code);

		return this;
	}

	/**
	 * Add a command. THe command and the code are an OR match. Everything else is and.
	 * 
	 * @return provides a fluent interface.
	 */
	public MessageSubscription addCommand(String cmd) {
		
		if ( cmds == null ) 
			cmds = new LinkedList<String>();

		cmds.add(cmd);

		return this;
	}

	/**
	 * Add a regex to match on the 'message' part.
	 */
	public MessageSubscription addPattern(Pattern p) {
		
		if ( patterns == null )
			patterns = new LinkedList<Pattern>();

		patterns.add(p);

		return this;
	}

	/**
	 * Register this subscription
	 */
	public MessageSubscription register() {
		synchronized(irc.handlers) {
			irc.handlers.add(this);
		}
		return this;
	}

	/**
	 * kill this subscription
	 */
	public void unregister() {
		synchronized(irc.handlers) {
			irc.handlers.remove(this);
		}
	}

	/**
	 * add an "or" condition. Really just creates a new subscription.
	 */
	public MessageSubscription or() {
		return (new MessageSubscription(this.handler,irc)).register();
	}

	//tests if this subscription matches, calls the irc.handlers handle if it does.
	public void handle(Message msg) {

		//Type must ALWAYS match...
		//msg.getType() should NEVER return null.
		if ( this.types != null && !types.contains( msg.getType() ) )
			return;


		boolean match = false;
		boolean cmdMatch = false;
		boolean codeMatch = false;
		
		if ( codes != null && msg.getCode() != null && codes.contains( msg.getCode() ) )
			codeMatch = true;
	
		if ( cmds != null ) {
			for (String cmd : cmds) {
				if ( cmd.equals(msg.getCommand()) ) {
					cmdMatch = true;
					break;
				}
			}
		}

		//case 1: no commands or codes to match = match 
		if ( this.cmds == null && this.codes == null ) {
			match = true;

		//there are codes to match, so the codes
		} else if ( this.cmds == null ) {
			match = codeMatch;

		//likewise, but commands
		} else if ( this.codes == null ) {
			match = cmdMatch;

		//BOTH are non null. 
		} else {
			match = (cmdMatch||codeMatch);
		}

		//if we didn't find a command or code match...
		if ( !match ) 
			return;

		//Patterns must always match....
		if ( this.patterns != null ) {
			boolean found = false;

			for (Pattern p : patterns) {
				if ( p.matcher( msg.getMessage() ).matches() ) {
					found = true;
					break;
				}
			}

			if (!found) return;
		}

		//if we haven't returne by this point, the message must be a match...
		//@TODO consider creating one event each time a handler matches? 
		//this is a bit redundant.
		handler.handle(new MessageEvent(irc, this, msg));
	}
}
