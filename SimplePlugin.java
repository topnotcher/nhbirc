import com.coldsteelstudios.irc.*;
import com.coldsteelstudios.irc.client.*;

abstract public class SimplePlugin implements MessageHandler {

	protected Connection irc;
	protected SyncManager sync;


	public SimplePlugin(Connection irc, SyncManager sync) {
		this.irc = irc;
		this.sync = sync;

		irc.addMessageHandler(this)
			.addType( MessageType.QUERY );
	}

	public void handle(MessageEvent e) {

		Message m = e.getMessage();
		MessageTarget dst = m.getTarget();
		MessageTarget src = m.getSource();

		String rplto = null;
		String rpl = null;

		if ( dst.scope(MessageTarget.Scope.CHANNEL) ) {
			rpl = channel(src.getNick(),dst.getChannel(), m.getMessage(), m);
			rplto = dst.getChannel();
		}

		if ( rpl != null && rplto != null )
			irc.msg(rplto,rpl);
	}

	protected String channel(String nick, String channel, String text, Message msg) {
		return null;
	}
}
