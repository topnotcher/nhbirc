import java.util.StringTokenizer;
import java.util.Vector;

public class IrcMessage {
	private String source = "";
	private String target = "";
	private String command = "";
	private String msg = "";

	private String[] args;


/**
 * SEND :NICK mario
 * SEND :USER mario 0 * : mario
 * RECV: :spree.jaundies.com NOTICE AUTH :*** Looking up your hostname...
 * RECV: :spree.jaundies.com NOTICE AUTH :*** Checking ident...
 * RECV: :spree.jaundies.com NOTICE AUTH :*** Couldn't resolve your hostname; using your IP address instead
 * RECV: :spree.jaundies.com NOTICE AUTH :*** No ident response; username sourceed with ~
 * RECV: :spree.jaundies.com 433 * mario :Nickname is already in use.
 * RECV: :spree.jaundies.com 372 fubar :- of the Network will be deemed as irrevocable acceptance of any revisions.
 * :fubar!~fubar@33AFAADF.7A30E887.CC04845D.IP JOIN :#fooooo
 * PING :foo
 *
 */
/*def parsemsg(s):
    """Breaks a message from an IRC server into its source, command, and arguments.
    """
    source = ''
    trailing = []
    if not s:
       raise IRCBadMessage("Empty line.")
    if s[0] == ':':
        source, s = s[1:].split(' ', 1)
    if s.find(' :') != -1:
        s, trailing = s.split(' :', 1)
        args = s.split()
        args.append(trailing)
    else:
        args = s.split()
    command = args.pop(0)
    return source, command, args
*/

	/**
	 * @TODO this is all messed up.
	 */
	public IrcMessage(String msg) {


		if (msg.length() == 0)
			throw new RuntimeException("Empty message");


		Vector<String> args = new Vector<String>(2);
		String trailing = null;

		int pos = -1;

		if ( msg.charAt(0) == ':' ) {
			pos = msg.indexOf(' ',1);
			source = msg.substring(1, pos);
			msg = msg.substring(pos+1);
		}

		if ( (pos = msg.indexOf(" :")) != -1 ) {
			trailing = msg.substring(pos+2);
			System.out.println("TRAILING " + trailing);
			msg = msg.substring(0,pos); 	
		}

		for (String arg : msg.split(" "))
			args.add(arg);

		if (trailing != null) {
			args.add(trailing);
			this.msg = trailing;
		} else {
			this.msg = msg;
		}
		
		command = args.get(0);

		this.args = args.toArray(new String[args.size()]);

		for (String arg: this.args)
			System.out.println("ARG: " + arg);

		System.out.println("MESSAGE: " + this.msg);
		System.out.println("PREFIX: " + this.source);
		System.out.println("COMMAND: " + this.command);

	}

	public String getCommand() {
		return command;
	}
	public String getMessage() {
		return msg;
	}

	/**
	 * @TODO make a class to encapsulate source
	 * so it can parse a full hostmask
	 */
	public String getSource() {
		return source;
	}
	public String toString() {
		return source + " " + command + " " + target + " :"	+msg;
	}
}
