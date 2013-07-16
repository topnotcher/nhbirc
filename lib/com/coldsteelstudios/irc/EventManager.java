package com.coldsteelstudios.irc;

import java.util.Set;
import java.util.List;
import java.util.regex.Pattern;

import java.util.LinkedList;
import java.util.ListIterator;

class EventManager {
	/**
	 * Subscribed message handlers
	 */
	protected List<MessageSubscription> subs;
	
	protected Connection irc;
	
	private ListIterator<MessageSubscription> it;

	public EventManager(Connection irc) {
		this.irc = irc;
		subs = java.util.Collections.synchronizedList( new LinkedList<MessageSubscription>());
		//subs = new com.coldsteelstudios.util.LinkedList<MessageSubscription>();
	
		it = subs.listIterator();
	}

	public MessageSubscription register(MessageHandler handler) {
		return register(new MessageSubscription(handler));
	}

	public MessageSubscription register(MessageSubscription sub) {
		it.add(sub);
		return sub;
	}

	public void unregister(MessageSubscription sub) {
		subs.remove(sub);
	}

	public void unregister(MessageHandler handler) {
		for (MessageSubscription sub : subs)
			if ( sub.getHandler() == handler )
				unregister(sub);
	}

	//tests if this subscription matches, calls the irc.handlers handle if it does.
	public void dispatch(Message msg) {
		
		//this hopefully prevents a concurrent modification exception
		//(this is why I did all modification through the iterator)
		synchronized(this) {
			it = subs.listIterator();
		}

		while ( it.hasNext() ) try {
			dispatch(it.next(),msg);
		} catch (Exception e) {
			//@TODO
			e.printStackTrace();
		}

	}

	protected void dispatch(MessageSubscription sub, Message msg) {
		
		Set<MessageType> types = sub.getTypes();
		Set<MessageCode> codes = sub.getCodes();
		List<String> cmds = sub.getCommands();
		List<Pattern> patterns = sub.getPatterns();

		//Type must ALWAYS match...
		//msg.getType() should NEVER return null.
		if ( types != null && !types.contains( msg.getType() ) )
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
		if ( cmds == null && codes == null ) {
			match = true;

		//there are codes to match, so the codes
		} else if ( cmds == null ) {
			match = codeMatch;

		//likewise, but commands
		} else if ( codes == null ) {
			match = cmdMatch;

		//BOTH are non null. 
		} else {
			match = (cmdMatch||codeMatch);
		}

		//if we didn't find a command or code match...
		if ( !match ) 
			return;

		//Patterns must always match....
		if ( patterns != null ) {
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
		sub.getHandler().handle(new MessageEvent(irc, msg));
	}
}
