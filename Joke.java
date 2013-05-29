import com.coldsteelstudios.irc.*;
import com.coldsteelstudios.irc.client.*;

import java.util.Random;
import java.util.regex.Pattern;

public class Joke {

	protected final Connection irc;
	protected SyncManager sync;

	protected int joke = 0;

	public Joke(Connection irc, SyncManager sync) {
		this.irc = irc;
		this.sync = sync;

		irc.addMessageHandler(new MessageHandler(){
			public void handle(MessageEvent e) {
				momJoke(e);
			}
		}).addType(MessageType.QUERY).addPattern( Pattern.compile(".*(mom|mother|mum).*",Pattern.CASE_INSENSITIVE) );

		irc.addMessageHandler(new MessageHandler(){
			public void handle(MessageEvent e) {
				sanctionJoke(e);
			}
		}).addType(MessageType.QUERY).addPattern( Pattern.compile(".*Sanction.*",Pattern.CASE_INSENSITIVE) );

		irc.addMessageHandler(new MessageHandler(){
			public void handle(MessageEvent e) {
				anubisJoke(e);
			}
		}).addType(MessageType.QUERY).addPattern( Pattern.compile(".*anubis.*",Pattern.CASE_INSENSITIVE) );


		irc.addMessageHandler(new MessageHandler(){
			public void handle(MessageEvent e) {
				marioJoke(e);
			}
		}).addType(MessageType.QUERY).addPattern( Pattern.compile(".*mario.*",Pattern.CASE_INSENSITIVE) );
	}

	protected void momJoke(MessageEvent e) {
	
		final String momjokes[] = {
			"Mom is so fat she plugged the hole in the ozone layer",
			"Mom is so fat she took her chin as carry on",
			"Mom is so short she does pull-ups with a staple.",
			"Mom is so dirty that when she went swimming at the beach, she left a ring around the ocean.",
			"Mom is so dirty the flies on a piece of dog shit passed out",
			"Mom is so dirty bigfoot took a photo of her.",
			"Mom is so smelly that the only dis I'm gonna give her is disinfectant.",
			"Mom is so smelly she was playing in my sand box and the cat came along and buried her.",
			"Mom is so greasy she a full time job at the 'Pancake Palace' wiping pancakes across her forhead.",
			"Mom is so stupid I asked her to buy me a pair of sneakers and she came back with 2 candy bars",
			"Mom is so stupid she got locked in Matress World and slepped on the floor",
			"Mom is so dumb that she tripped over a cordless phone",
			"Mom is so dumb she stole a free sample",
			"Mom is so fat she can't even fit in the chat room",
			"Mom is so loose it's like throwing a hotdog down a hallway",
			"Mom is so fat after sex she rolls over and smokes a ham",
			"Mom is so fat that when she got into an elevator the doors couldn't close",
			"Mom is so fat, she got more rolls then a pastry truck",
			"Mom is so fat she was stopped at the airport for having 200 pounds of crack",
			"Mom is so fat.....fat is a complement",
			"Mom is so fat when she goes to a restaurant, she doesn't get a menu, she gets an estimate",
			"Mom is so fat when she went to Tokyo everyone said its GODZILA",
			"Mom is so ugly her nickname is hairy pooter",
			"Mom is so ugly that when she went to rob a bank she didnt have to wear a mask, she just walked up and said \"Put the money in the bag\"",
		};



		String chan = e.getMessage().getTarget().getChannel();
		String nick = e.getMessage().getSource().getNick();

		irc.msg(chan, nick + "'s " + momjokes[joke]);

		if ( ++joke == momjokes.length ) joke = 0;


	}

	protected void sanctionJoke(MessageEvent e) {
		String chan = e.getMessage().getTarget().getChannel();
		irc.msg(chan, "Sanction? Isn't he that guy who tried to quote his MySQL identifiers with single quotes. LOLOL");
	}

	protected void anubisJoke(MessageEvent e) {
		String chan = e.getMessage().getTarget().getChannel();

		irc.msg(chan, "Anubis is such a horrible programmer that when he makes a coin toss, heads has a 51% chance of winning.");
	}

	protected void marioJoke(MessageEvent e) {
		String chan = e.getMessage().getTarget().getChannel();
		irc.msg(chan, "Mario's so dumb that he wrote an IRC bot and made it tell dumb jokes about him. :/");
	}

}
