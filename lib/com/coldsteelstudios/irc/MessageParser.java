package com.coldsteelstudios.irc;

import java.util.List;
import java.util.LinkedList;

/**
 * BASIC format of an IRC Message:
 * [:prefix ]COMMAND[ ARG1 [ARG2 ... ARGN[ :trailing]]]
 * Trailing is really just the final argument, but it needs be 
 * handled specially. The trailing part is used to allow for spaces in
 * the last argument.
 */

class MessageParser {

	public static Message parse(Connection irc, String msg) {

		if (msg.length() == 0)
			throw new RuntimeException("Empty message");

		Message message = new Message();

		message.setRaw(msg);

		List<String> args = new LinkedList<String>();

		String trailing = null;
		String prefix = null;

		int pos = -1;

		//check for a leading :, which indicates the meessage has a source
		if ( msg.charAt(0) == ':' ) {
			pos = msg.indexOf(' ',1);
			prefix = msg.substring(1, pos);
			msg = msg.substring(pos+1);

		//if there is no prefix (full origin
		//then the message came from the directly
		//connected server
		} else if ( irc.getState() == Connection.State.REGISTERED ) {
			prefix = irc.getServerName();
		}

		//check for a " :" delimiter, which separates the "trailing" portion
		//this is essentially a trick used to allow spaces in the final argument...
		if ( (pos = msg.indexOf(" :")) != -1 ) {
			trailing = msg.substring(pos+2);
			
			//when there is a "header/body",
			//we only care about parsing 
			//the header.
			msg = msg.substring(0,pos); 	
		}
	
		for (String arg : msg.split(" "))
			args.add(arg);
	
		//if there's a trailing, we call that the "message"
		if (trailing != null) {
			args.add(trailing);
			
			message.setMessage( trailing );
		}
	
		if ( args.size() >= 2 ) {
			message.setTarget( new MessageTarget(args.get(1)) );	

			//this is probably weird, but it has its uses...
			if ( prefix == null )
				prefix = args.get(1);

			//if the message is TO a nick... and that nick is the nick for the current connection
			if ( message.getTarget().scope( MessageTarget.Scope.NICK ) &&  message.getTarget().getNick().equals(irc.nick()))
				message.setToMe(true);
		}
		else 
			message.setTarget( new MessageTarget() );

		if ( prefix != null ) 
			message.setSource( new MessageTarget( prefix ) );
		else 
			message.setSource( new MessageTarget() );


		//if the message is FROM a nick... and that nick is the nick for the current connection
		//@TODO lowercase {}|[]\
		if ( message.getSource().scope( MessageTarget.Scope.NICK ) &&  message.getSource().getNick().toLowerCase().equals(irc.nick().toLowerCase()))
			message.setFromMe(true);

		String command = args.get(0).toUpperCase();

		message.setCommand( command );

		message.setArgs( args.toArray(new String[args.size()]) );

		MessageCode code = MessageCode.get( command );

		message.setCode( code );

		MessageType type;

		Priority priority = Priority.MEDIUM;

		if ( code != null )
			type = code.getType();

		else {

			if ( command.equals("PRIVMSG") || command.equals("NOTICE") ) {
				final char SOH = '\u0001';
				String pm = message.getMessage();

				//starts and ends with an SOH
				if ( pm.charAt(0) == SOH && pm.charAt( pm.length() -1)  == SOH ) {
	
					//XACTION
					if ( pm.length() >= 8 && pm.substring(1,7).equals("ACTION") ) {
						type = MessageType.ACTION;
						message.setMessage(  pm.substring( pm.indexOf(' ')+1, pm.length()-1) );

					} else {
						type = MessageType.CTCP;
						priority = Priority.LOW;
					}
		
				} else if ( command.equals("NOTICE") ) {
					type = MessageType.NOTICE;

				} else 
					type = MessageType.QUERY;
			}

			else if ( command.equals("JOIN") ) {
				type = MessageType.JOIN;
				priority = Priority.HIGH;
			}

			else if ( command.equals("TOPIC") )
				type = MessageType.TOPICCHANGE;

			else if ( command.equals("NICK") ) {
				type = MessageType.NICKCHANGE;	
			}

			else if ( command.equals("KICK") )
				type = MessageType.KICK;

			else if ( command.equals("PART") ) {
				type = MessageType.PART;
			}

			else if ( command.equals("QUIT") )
				type = MessageType.QUIT;

			//per RFC2812, error is basically only used for
			//a disconnect when being send to clients.
			else if ( command.equals("ERROR") )
				type = MessageType.DISCONNECT;

			else if ( command.equals("PING") ) {
				type = MessageType.PING;
				priority = Priority.CRITICAL;
			}

			else if ( command.equals("MODE") ) {

				if ( message.getTarget().scope( MessageTarget.Scope.CHANNEL ) )
					type = MessageType.CHANNELMODE;
				else
					type = MessageType.USERMODE;
			}


			else 
				type = MessageType.UNKNOWN;
		}

		if ( type == MessageType.ERROR )
			priority = Priority.HIGH;

		else if ( type ==  MessageType.UNKNOWN )
			priority = Priority.LOW;

		message.setPriority(priority);
		message.setType(type);

		return message;
	}
}
