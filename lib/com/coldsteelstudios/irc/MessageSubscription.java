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

	public MessageSubscription(MessageHandler handler ) {
		this.handler = handler;
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

	public MessageHandler getHandler() {
		return handler;
	}

	List<Pattern> getPatterns() {
		return patterns;
	}

	List<String> getCommands() {
		return cmds;
	}

	Set<MessageCode> getCodes() {
		return codes;
	}

	Set<MessageType> getTypes() {
		return types;
	}
}
