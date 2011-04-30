package irc;

import java.util.Vector;

class MessageParser {
	
	public static Message parse( String msg ) {

		if (msg.length() == 0)
			throw new RuntimeException("Empty message");


		Message message = new Message();

		message.setRaw(msg);

		Vector<String> args = new Vector<String>(2);

		String trailing = null;
		String prefix = null;

		int pos = -1;

		//check for a leading :, which indicates the meessage has a source
		if ( msg.charAt(0) == ':' ) {
			pos = msg.indexOf(' ',1);
			prefix = msg.substring(1, pos);
			msg = msg.substring(pos+1);
		}

		//check for a " :" delimiter, which separates the message "header" from the body.
		if ( (pos = msg.indexOf(" :")) != -1 ) {
			trailing = msg.substring(pos+2);
			
			//when there is a "header/body",
			//we only care about parsing 
			//the header.
			msg = msg.substring(0,pos); 	
		}
	
		for (String arg : msg.split(" "))
			args.add(arg);

		if ( prefix != null ) 
			message.setSource( new MessageTarget( prefix ) );
		else 
			message.setSource( new MessageTarget() );

//353 fubar = #divinelunacy :
		if ( args.size() >= 2 ) {
			String t = args.get(1);
	
/*			if ( args.size() >= 4 ) {
				String tmp = args.get(2);

				if (tmp.equals("=") || tmp.equals("*"))
					t = args.get(3);
			}
*/

			message.setTarget( new MessageTarget( t ) );
		}
	
		else 
			message.setTarget( new MessageTarget() );

		if (trailing != null) {
			args.add(trailing);
			
			message.setMessage( trailing );
		}
		else
			message.setMessage( msg );

		String command;

		message.setCommand( command = args.get(0) );

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

				} else if (args.size() >= 2 && args.get(1).charAt(0) == '#') 
					type = MessageType.CHANNEL;

				else 
					type = MessageType.QUERY;
			}

			else if ( command.equals("JOIN") ) {
				type = MessageType.JOIN;
				
				//make the channel the target for JOIN
				//VIA rfc2812, servers shouldn't use a CSV list
				//when sending JOINs to clients (e.g. this is valid)
				message.setTarget( new MessageTarget( message.getMessage() ) );

				priority = Priority.HIGH;

			}

			else if ( command.equals("TOPIC") )
				type = MessageType.TOPICCHANGE;

			else if ( command.equals("NICK") ) {
				type = MessageType.NICKCHANGE;
	
				//similar to JOIN
				message.setTarget( new MessageTarget( message.getMessage() ) );
			}

			else if ( command.equals("KICK") )
				type = MessageType.PART;

			else if ( command.equals("PART") )
				type = MessageType.PART;

			else if ( command.equals("MODE") )
				type = MessageType.MODECHANGE;

			else if ( command.equals("QUIT") )
				type = MessageType.QUIT;

			else if ( command.equals("ERROR") )
				type = MessageType.ERROR;

			else 
				type = MessageType.UNKNOWN;
		}

		if ( type == MessageType.ERROR )
			priority = Priority.HIGH;

		else if ( type == MessageType.PING )
			priority = Priority.CRITICAL;

		else if ( type ==  MessageType.UNKNOWN )
			priority = Priority.LOW;

		message.setPriority(priority);
		message.setType(type);

		return message;
	}
}
