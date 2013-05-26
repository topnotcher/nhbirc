import com.coldsteelstudios.irc.*;
import com.coldsteelstudios.irc.client.*;
import java.util.Map;
import java.util.HashMap;

public class Seen {

	Connection irc;
	SyncManager sync;

	Map<String,Seen.LastAction> acts;

	public Seen(Connection irc, SyncManager sync) {
		this.irc = irc;
		this.sync = sync;
		acts = new HashMap<String,LastAction>();

		irc.addMessageHandler(lastSeenHandler)
			.addType( MessageType.PART )
			.addType( MessageType.NICKCHANGE )
			.addType( MessageType.QUIT )
		;

		irc.addMessageHandler(commandHandler)
			.addType( MessageType.QUERY )	
			.addPattern( java.util.regex.Pattern.compile("!seen.*") );
	}

	public MessageHandler commandHandler = new MessageHandler() { public void handle(MessageEvent e) {
		String chan = e.getMessage().getTarget().getChannel();
		String nick = "";
		String msg = e.getMessage().getMessage();

		int idx = msg.indexOf(" ");

		if ( idx > 0 )
			nick = msg.substring(idx+1);
		else {
			irc.msg(chan, "Usage: !seen nickname");
			return;
		}
		
		Channel schan = sync.getChannel(chan);

		for ( User u : schan ) {
			if ( u.getNick().equals(nick) ) {
				irc.msg(chan, nick + " is on the channel right now!");
				return;
			}
		}
		

		if ( ! acts.containsKey(nick) ) {
			irc.msg(chan, "I have not seen " + nick);
		} else {
			Seen.LastAction l = acts.get(nick);

			irc.msg(chan,"I last saw " + nick + " " + (System.currentTimeMillis() - l.time)/60 + " seconds ago.");
		}

	}};

	public MessageHandler lastSeenHandler = new MessageHandler() { public void handle(MessageEvent e) {
//		System.out.println("Source: " + e.getMessage().getSource());
//		System.out.println("Target: " + e.getMessage().getTarget());

		acts.put(e.getMessage().getSource().getNick(),new Seen.LastAction(System.currentTimeMillis(), "derp"));

	}};

	private static class LastAction {
		
		public long time;
		public String act;

		public LastAction(long time, String act) {
			this.time = time;
			this.act = act;
		}
	}

}
