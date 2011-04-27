import java.util.StringTokenizer;

public class IrcMessage {
	private String source = "";
	private String target = "";
	private String command = "";
	private String msg = "";

/**
 * SEND :NICK mario
 * SEND :USER mario 0 * : mario
 * RECV: :spree.jaundies.com NOTICE AUTH :*** Looking up your hostname...
 * RECV: :spree.jaundies.com NOTICE AUTH :*** Checking ident...
 * RECV: :spree.jaundies.com NOTICE AUTH :*** Couldn't resolve your hostname; using your IP address instead
 * RECV: :spree.jaundies.com NOTICE AUTH :*** No ident response; username prefixed with ~
 * RECV: :spree.jaundies.com 433 * mario :Nickname is already in use.
 * RECV: :spree.jaundies.com 372 fubar :- of the Network will be deemed as irrevocable acceptance of any revisions.
 * :fubar!~fubar@33AFAADF.7A30E887.CC04845D.IP JOIN :#fooooo
 * PING :foo
 *
 */

	public IrcMessage(String msg) {

		System.out.println("PARSE " + msg);
		int pos = msg.indexOf(':');
		int start = 0;

		if ( pos == 0 )
			pos = msg.indexOf(':',1);

		//@TODO
		if ( pos == -1 ) 
			throw new RuntimeException("Invalid message: " + msg);

		String hdr = msg.substring(0, pos - 1 );
		this.msg = msg.substring(pos + 1);
		
		StringTokenizer toks = new StringTokenizer(hdr," ");

		for (int i = 0; toks.hasMoreTokens(); ++i) {

			if (i == 0) 
				source = toks.nextToken();

			else if (i == 1)
				command = toks.nextToken();

			else if (i == 2)
				target = toks.nextToken();
		}			  
	}

	public String getCommand() {
		return command;
	}
	public String getMessage() {
		return msg;
	}

	public String toString() {
		return source + " " + command + " " + target + " :"	+msg;
	}
}
