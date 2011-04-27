import java.awt.BorderLayout;
import javax.swing.JFrame;

class Client extends JFrame {

	private Irc irc;

	private GUIConsole console;

	public static void main(String[] argv) {
		new Client();
	}


	/**
	 * **BASIC** prototype.
	 */
	private Client() {
		getContentPane().setLayout(new BorderLayout());

		irc = new Irc("irc.jaundies.com", 6667, "foobar");
		
		setSize(800,800);

		setTitle("Irc client");

		setVisible(true);

		try {
			//@TODO
			irc.connect();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}

		console = new GUIConsole();


		add(console,BorderLayout.CENTER);
	

		console.addActionListener(commandListener);
		
		//prototyping purposes, just receive ALL Pms
		irc.addMessageHandler("PRIVMSG", messageHandler);

		//a Channel I think might be active
		irc.send("JOIN", "#divinelunacy");
	}

	/**
	 * for prototyping, just send all privmsgs to a window...
	 */
	private IrcMessageHandler messageHandler = new IrcMessageHandler() {
		//and put all PMS whether channel or private in one window...
		public void handle(IrcMessage msg) {
			console.out().println( "<" + msg.getSource() + "> " + msg.getMessage() );
		}
	};

	/*
	 * TEMP to provide working chatting...
	 */
	private java.awt.event.ActionListener commandListener = new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			irc.send("PRIVMSG #divinelunacy", e.getActionCommand());
			console.out().println("<" + irc.getNick() + "> " + e.getActionCommand());
		}
	};
}
