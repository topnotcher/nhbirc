import java.awt.BorderLayout;
import javax.swing.JFrame;

class Client extends JFrame {

	private Irc irc;

	private GUIConsole console;

	public static void main(String[] argv) {
		new Client();
	}


	private Client() {
		getContentPane().setLayout(new BorderLayout());

		irc = new Irc("irc.jaundies.com", 6667, "fubar_test");
		
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

		irc.addMessageHandler("PRIVMSG", messageHandler);

		irc.send("JOIN", "#fooooooooooo");
	}

	private IrcMessageHandler messageHandler = new IrcMessageHandler() {
		public void handle(IrcMessage msg) {
			console.out().println( msg.getMessage() );
		}
	};
}
