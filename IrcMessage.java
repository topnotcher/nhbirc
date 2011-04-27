import java.util.StringTokenizer;
import java.util.Vector;

public class IrcMessage {

	private String source = "";
	private String target = "";
	private String command = "";
	private String msg = "";

	private String[] args;

	/**
	 * @TODO this is all messed up.
	 */
	public IrcMessage(String msg) {


		if (msg.length() == 0)
			throw new RuntimeException("Empty message");


		Vector<String> args = new Vector<String>(2);
		String trailing = null;

		int pos = -1;

		//check for a leading :, which indicates the meessage has a source
		if ( msg.charAt(0) == ':' ) {
			pos = msg.indexOf(' ',1);
			source = msg.substring(1, pos);
			msg = msg.substring(pos+1);
		}

		//check for a " :" delimiter, which separates the message "header" from the body.
		if ( (pos = msg.indexOf(" :")) != -1 ) {
			trailing = msg.substring(pos+2);
//			System.out.println("TRAILING " + trailing);
			
			//when there is a "header/body",
			//we only care about parsing 
			//the header.
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

//		for (String arg: this.args)
//			System.out.println("ARG: " + arg);
//
//		System.out.println("MESSAGE: " + this.msg);
///		System.out.println("PREFIX: " + this.source);
//		System.out.println("COMMAND: " + this.command);

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
