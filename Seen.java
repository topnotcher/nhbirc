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
			.addType( MessageType.KICK )
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
 
		if ( idx > 0 ) {
			nick = msg.substring(idx+1);
			
			if ( (idx = nick.indexOf(" ")) != -1 )
				nick = nick.substring(0,idx);

		} else {
			irc.msg(chan, "Usage: !seen nickname");
			return;
		}
		
		Channel schan = sync.getChannel(chan);

		for ( User u : schan ) {
			if ( u.getNick().toLowerCase().equals(nick.toLowerCase()) ) {
				irc.msg(chan, nick + " is on the channel right now!");
				return;
			}
		}
		

		if ( ! acts.containsKey(nick.toLowerCase()) ) {
			irc.msg(chan, "I have not seen " + nick);
		} else {
			Seen.LastAction l = acts.get(nick.toLowerCase());
			
			irc.msg(chan,"I last saw " + nick + " " + (System.currentTimeMillis()/1000 - l.time) + " seconds ago. (" + l.act + ").");
		}

	}};

	public MessageHandler lastSeenHandler = new MessageHandler() { public void handle(MessageEvent e) {
//		System.out.println("Source: " + e.getMessage().getSource());
//		System.out.println("Target: " + e.getMessage().getTarget());
//	
		String nick = e.getMessage().getSource().getNick();
		String act = "derp";

		switch (e.getMessage().getType()) {
			case PART:
				act = "left the channel";
				break;
			case KICK:
				//in kicks, the source is the kicker and the target is the channel!
				nick = e.getMessage().getArg(2);
				act = "kicked from the channel";
				break;
			case NICKCHANGE:
				act = "changed nickname to " + e.getMessage().getTarget().getNick();
				break;
			case QUIT:
				act = "quit";
				break;
			default: act = "I don't know what happened";
		}
	
		acts.put(nick.toLowerCase(),new Seen.LastAction(System.currentTimeMillis()/1000, act));

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
