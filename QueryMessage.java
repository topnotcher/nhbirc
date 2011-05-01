import java.awt.Color;

/**
 * Abstract painting a "query type" message to the screen..
 */
public class QueryMessage extends PaintableMessage {

	public static enum Dir { INCOMING, OUTGOING }

	public QueryMessage(irc.Message msg) {
		this(msg.getType(), msg.getSource().getNick(), msg.getMessage(), Dir.INCOMING);
	}

	public QueryMessage(irc.MessageType type, String src, String msg, Dir dir) {

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
