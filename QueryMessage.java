import java.awt.Color;

public class QueryMessage extends PaintableMessage {

	public QueryMessage(irc.Message msg) {
		String src = msg.getSource().getNick();

		switch ( msg.getType() ) {
			case ACTION:
				append("*",Color.red).append(src,Color.orange).append("*",Color.red).append(" " +msg.getMessage() );
				break;
			case NOTICE:
				append("-",Color.darkGray).append(src, java.awt.Color.magenta ).append("- ",Color.darkGray).append(msg.getMessage() );
				break;
			default:
				append( "<").append( src, java.awt.Color.yellow).append( "> " + msg.getMessage() );
				break;
		}
		indent(src.length()+3);
	}
}
