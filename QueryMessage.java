import java.awt.Color;

/**
 * Abstract painting a "query type" message to the screen..
 */
public class QueryMessage extends PaintableMessage {

	public static enum Dir { INCOMING, OUTGOING }

	public QueryMessage(irc.Message msg) { 
	
		//some networks are offensive, and they'll send notices (especially AUTH
		//with no prefix. IMO, that's just god damn annoying.  
		//Use the extra bandwidth and put SOMETHING in there
		//
		//ANYWHO - we'll assume the target is interesting in those cases... until a network breaks that.

		String src = msg.getSource().scope(irc.MessageTarget.Scope.NONE) ? msg.getTarget().getNick() : msg.getSource().getNick();
		irc.MessageType type = msg.getType();

		//servers always notice. that's just how it is.
		if ( msg.getSource().scope(irc.MessageTarget.Scope.NONE) || msg.getSource().scope(irc.MessageTarget.Scope.SERVER) )
			type = irc.MessageType.NOTICE;

		init(type, src, msg.getMessage(), Dir.INCOMING);
	}

	public QueryMessage(irc.MessageType type, String src, String msg, Dir dir) {
		init(type, src, msg, dir);
	}

	public void init(irc.MessageType type, String src, String msg, Dir dir) {


		Color msgColor = null;
		
		if (dir == Dir.OUTGOING)
			msgColor = Color.white;

		switch ( type ) {
			case ACTION:
				append("*",Color.red).append(src,Color.orange).append("*",Color.red).append(" " +msg, msgColor);
				break;
			case NOTICE:
				String s = (dir == Dir.OUTGOING) ? ">" : "-";
				String e = (dir == Dir.OUTGOING) ? "<" : "-";

				append(s,Color.darkGray).append(src, Color.magenta ).append(e+" ",Color.darkGray).append(msg, msgColor);
				break;
			default:
				append( "<").append( src, java.awt.Color.yellow).append( "> " + msg, msgColor);
				break;
		}

		//in any case, we want every line to indent AFTER the nickname and separating space.
		indent(src.length()+3);
	}
}
