import com.coldsteelstudios.irc.*;
import com.coldsteelstudios.irc.client.*;

import java.util.Random;
import java.util.regex.Pattern;

import java.util.Vector;

public class Bans {

	protected String chan = "#foo";

	protected final Connection irc;
	protected SyncManager sync;

	protected int joke = 0;

	protected Vector<KLine> glines;

	public Bans(Connection irc, SyncManager sync) {
		this.irc = irc;
		this.sync = sync;

		irc.addMessageHandler(new MessageHandler(){
			public void handle(MessageEvent e) {
				parseLine(e);
			}
		}).addCode(MessageCode.RPL_STATSGLINE);


		irc.addMessageHandler(glinesHandler)
			.addType( MessageType.QUERY )	
			.addPattern( java.util.regex.Pattern.compile("!glines.*") );

		irc.addMessageHandler(delHandler)
			.addType( MessageType.QUERY )	
			.addPattern( java.util.regex.Pattern.compile("!del.*") );

		irc.addMessageHandler(zlineHandler)
			.addType( MessageType.QUERY )	
			.addPattern( java.util.regex.Pattern.compile("!zline.*") );

		irc.addMessageHandler(showHandler)
			.addType( MessageType.QUERY )	
			.addPattern( java.util.regex.Pattern.compile("!show.*") );

		irc.addMessageHandler(setexpHandler)
			.addType( MessageType.QUERY )
			.addPattern( java.util.regex.Pattern.compile("!setexp.*") );

	}

	protected void parseLine(MessageEvent e) {
		Message msg = e.getMessage();

		KLine k = new KLine();

		k.setType(msg.getArg(2).charAt(0));
		k.setMask(msg.getArg(3));
		k.setTtl(Integer.parseInt(msg.getArg(4)));
		k.setAge(Integer.parseInt(msg.getArg(5)));

		String path = msg.getArg(6);
		int idx = path.indexOf('!');

		if ( idx > 0 )
			k.setOp(path.substring(0,idx));
		else 
			k.setOp(path);

		k.setReason(msg.getArg(7));
		
		glines.add(k);

		idx = glines.indexOf(k);

		show(idx);
	}

	protected void show(int idx) {

		KLine k = glines.get(idx);

		String exp = "";
		if ( k.getTtl() == 0 ) {
			exp = "permanent";
		} else {
			exp = timeDisplay(k.getTtl());
		}

		irc.msg(chan, idx + " | "+ k.getType() + " | " + k.getMask() + " set by " + k.getOp() + " " + timeDisplay(k.getAge()) + "ago; exp: " + exp + "; " + k.getReason());
	}

	protected String timeDisplay(int time) {
		if ( time < 60 ) { 
			return time + " seconds";
		}

		time /= 60;

		if ( time < 60 ) 
			return time + " minutes";

		time /= 60;

		if ( time < 24 ) 
			return time + " hours";

		time /= 24;

		return time + " days";

	}

	private MessageHandler glinesHandler = new MessageHandler() {
		public void handle(MessageEvent e) {
			glines = new Vector<KLine>();
			irc.send("stats", "g");
		}
	};

	private MessageHandler showHandler = new MessageHandler() {
		public void handle(MessageEvent e) {
			String msg = e.getMessage().getMessage().trim();

			int idx = msg.indexOf(" ");

			if ( idx <= 0 ) {
				irc.msg(chan, "Usage: !show number");
				return;
			} 

			int ban = Integer.parseInt(msg.substring(idx+1));

			if ( glines == null || ban >= glines.size() ) {
				irc.msg(chan, "invalid number");
				return;
			}

			show(ban);
		}
	};

	private void delBan(int idx, String reason) {
		KLine k = glines.get(idx);
		String cmd = (k.getType() == 'G') ? "gline" : "gzline";

		irc.msg(chan, "removing "+cmd+" on " + k.getMask() + " (exp: "+k.getTtl()+"s); by: " + k.getOp() + " " + timeDisplay(k.getAge()) + " ["+k.getReason()+"]");
		irc.msg(chan, "reason for removal: " + reason);

		//@TODO
		irc.send(/*"PRIVMSG",chan,*/cmd,"-"+k.getMask());
	}

	private void ban(char type, String mask, int ttl, String reason) {
		String cmd = (type == 'G') ? "gline" : "gzline";

		//hopefully stop me from fucking myself
		if ( mask.length() <= 10 ) return;

		irc.msg(chan, "adding "+cmd+" on " + mask +" (exp: "+ttl+"s); " + reason);

		irc.send(/*"PRIVMSG", chan, */cmd, mask, ttl+"s", reason);
	}

	private MessageHandler delHandler = new MessageHandler() {
		public void handle(MessageEvent e) {
			String msg = e.getMessage().getMessage().trim();

			int idx = msg.indexOf(" ");

			if ( idx <= 0 ) {
				irc.msg(chan, "Usage: !del number reason");
				return;
			} 
			
			msg = msg.substring(idx+1);

			idx = msg.indexOf(" ");
			
			//no reason
			if ( idx <= 0 ) {
				irc.msg(chan, "Usage: !del number reason");
				return;
			}

			int ban = Integer.parseInt(msg.substring(0,idx));
			String reason = msg.substring(idx+1);

			if ( glines == null || ban >= glines.size() ) {
				irc.msg(chan, "invalid number");
				return;
			} else if ( reason.trim().length() < 10 ) {
				irc.msg(chan, "invalid reason");
				return;
			}

			delBan(ban,reason); 	
		}
	};

	private MessageHandler zlineHandler = new MessageHandler() {
		public void handle(MessageEvent e) {
			String msg = e.getMessage().getMessage().trim();

			int idx = msg.indexOf(" ");

			if ( idx <= 0 ) {
				irc.msg(chan, "Usage: !zline number");
				return;
			}

			int ban = Integer.parseInt(msg.substring(idx+1));

			if ( glines == null || ban >= glines.size() ) {
				irc.msg(chan, "invalid number");
				return;
			}
			
			KLine k = glines.get(ban);

			delBan(ban, "moving to Z:Line");
			ban('Z',k.getMask(), k.getTtl(), "[" + k.getOp() + "] " + k.getReason());
		}
	};


	private MessageHandler setexpHandler = new MessageHandler() {
		public void handle(MessageEvent e) {
			String msg = e.getMessage().getMessage().trim();

			int idx = msg.indexOf(" ");

			if ( idx <= 0 ) {
				irc.msg(chan, "Usage: !setexp number");
				return;
			}

			int ban = Integer.parseInt(msg.substring(idx+1));

			if ( glines == null || ban >= glines.size() ) {
				irc.msg(chan, "invalid number");
				return;
			}
			
			KLine k = glines.get(ban);

			ban(k.getType(), k.getMask(), 500*86400, "");
		}
	};

}
