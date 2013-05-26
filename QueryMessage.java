import com.coldsteelstudios.irc.*;

import java.awt.Color;

/**
 * Abstract painting a "query type" message to the screen..
 * @TODO configurable colors.
 */
public class QueryMessage extends PaintableMessage {

	/**
	 * Represents the direction of the message being painted.
	 */
	public static enum Dir { INCOMING, OUTGOING }

	/**
	 * Create a message for painting.
	 * @param msg an in [incoming] irc message
	 */
	public QueryMessage(MessageEvent e) { 

		Message msg = e.getMessage();
	
		//some networks are offensive, and they'll send notices (especially AUTH
		//with no prefix. IMO, that's just god damn annoying.  
		//Use the extra bandwidth and put SOMETHING in there
		//
		//ANYWHO - we'll assume the target is interesting in those cases... until a network breaks that.

		String src = msg.getSource().scope(MessageTarget.Scope.NONE) ? msg.getTarget().getNick() : msg.getSource().getNick();
		MessageType type = msg.getType();

		//servers always notice. that's just how it is.
		if ( msg.getSource().scope(MessageTarget.Scope.NONE) || msg.getSource().scope(MessageTarget.Scope.SERVER) )
			type = MessageType.NOTICE;

		//the message contains the nickname of the current connection
		if ( msg.getMessage().toLowerCase().indexOf( e.getSource().nick().toLowerCase() ) != -1)
			init(type,src,msg.getMessage(), Dir.INCOMING,  Color.blue);
		else
			init(type, src, msg.getMessage(), Dir.INCOMING,null);
	}

	/**
	 * Create a string to paint. This makes outgoing messages look like incoming ones.
	 * 
	 * @param type the type of message being painted.
	 * @param src the source (nickname) of the message
	 * @param msg the message being painted
	 * @param dir the direction of the message: Dir.INCOMING/OUTGOING
	 */
	public QueryMessage(MessageType type, String src, String msg, Dir dir) {
		init(type, src, msg, dir,null);
	}

	private void init(MessageType type, String src, String msg, Dir dir, Color msgColor) {

		if (msgColor == null && dir == Dir.OUTGOING)
			msgColor = Color.white;


		switch ( type ) {

			//ACTION CTCPs
			case ACTION:
				append("*",Color.red).append(src,Color.orange).append("*",Color.red).append(" " +msg, msgColor);
				break;

			//Notice
			case NOTICE:
				//outgoing prints like >destnick<
				String s = (dir == Dir.OUTGOING) ? ">" : "-";

				//incoming prints like -srcnick-
				String e = (dir == Dir.OUTGOING) ? "<" : "-";

				append(s,Color.darkGray).append(src, Color.magenta ).append(e+" ",Color.darkGray).append(msg, msgColor);
				break;

			//PMs and Channel Messages
			default:
				append( "<").append( src, Color.yellow).append("> ").append(msg, msgColor);
				break;
		}

		//in any case, we want every line to indent AFTER the nickname and separating space.
		indent(src.length()+3);
	}
}
