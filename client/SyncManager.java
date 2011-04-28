
package client;

import irc.*;

public class SyncManager implements MessageHandler {

	Connection irc;

	public SyncManager( Connection irc ) {
		this.irc = irc;
		
		irc.addMessageHandler(this)
			.addType( MessageType.NICKCHANGE )
			.addType( MessageType.JOIN )
			.addType( MessageType.QUIT )
			.addType( MessageType.PART )
		;
	}

	public void handle( Message m ) {
		switch ( m.getType() ) {
	
			case JOIN:
				print( m.getSource() + " JOINS " + m.getTarget() );
				break;

			case NICKCHANGE:
				print( m.getSource() +" CHANGES NICK TO "+ m.getTarget());
				break;

			case QUIT:
				print(m.getSource() + " QUITS ("+m.getMessage()+")");
				break;

			case PART:
				print(m.getSource() + " PARTS " + m.getTarget() + " (" + m.getMessage() + ")" );
				break;

			default:
				print("????", m);

		}

	}
/*
 *  Cl	mario@yoda ~/dev/csc212/programs/05 $ java Client
RECV :fubar!~fubar@33AFAADF.7A30E887.CC04845D.IP JOIN :#divinelunacy
	fubar!~fubar@33AFAADF.7A30E887.CC04845D.IP JOIN -------
RECV :mario!topnotcher@13.37 PART #divinelunacy :<<< You are now an IRC whore.
	mario!topnotcher@13.37 PART #divinelunacy
RECV :mario!topnotcher@13.37 JOIN :#divinelunacy
	mario!topnotcher@13.37 JOIN -------
RECV :the_insane_irishman!~the_insan@Jaundies-2F0EF383.res-cmts.sefg.ptd.net QUIT :Quit: I eat babies.
	the_insane_irishman!~the_insan@Jaundies-2F0EF383.res-cmts.sefg.ptd.net QUIT -------
*/
	public void print(String cmd, Message m) {
		System.out.println("RECV " + m.getRaw());
		System.out.println("\t" + m.getSource() + " " + cmd + " " + m.getTarget());
	}

	public void print(String ln) {
		System.out.println(ln);
	}
}
