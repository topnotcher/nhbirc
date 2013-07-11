import com.coldsteelstudios.irc.*;
import com.coldsteelstudios.irc.client.*;


public class Echo extends SimplePlugin {
	public Echo(Connection irc, SyncManager sync) {
		super(irc,sync);
	}

	protected String channel(String nick, String channel, String text, Message msg) {
		if ( text.equals("!derp") ) 
			return "Hello, " + nick;

		return null;
	}
}
