
import java.awt.BorderLayout;
//import javax.swing.JFrame;
import javax.swing.*;

import irc.*;

class Client extends JFrame {

	private Connection irc;

	private GUIConsole console;
	
	private ChatWindow status;

	private JTabbedPane tabs;

	public static void main(String[] argv) {
		new Client();
	}

	private final String CHAN = "#divinelunacy";

	/**
	 * **BASIC** prototype.
	 */
	private Client() {
		irc = new Connection("irc.jaundies.com", 6667, "fubar");
		
		setSize(800,800);

		setTitle("Irc client");

		setVisible(true);

		//tabs is the main viewport.
		tabs = new JTabbedPane();
		add(tabs);

		//tabs contain a status window.
	//	status = new GUIConsole("Status");
		tabs.setTabPlacement(JTabbedPane.BOTTOM);

		add( status = new GenericChatWindow("Status", ChatWindow.Type.STATUS) );
		

		//prototyping purposes, just receive ALL Pms
		irc.addMessageHandler(messageHandler);

		new ClientServices(irc);

		try {
			//@TODO
			irc.connect();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}


		//a channel window is a split pane...
		JSplitPane chan = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT) ;


		//the left of the split pane is the console
		console = new GUIConsole(CHAN);
		console.addActionListener(commandListener);

		chan.setLeftComponent(console);

		String[] test = {"User1", "user2", "user3","reallylongname"};
		chan.setRightComponent(new JList(test));

		//favor the left side???
		chan.setResizeWeight(1.0);

	
		//add the channel to the tabs.
		tabs.addTab(CHAN, chan);

		irc.join( CHAN );
	}

	private void add(ChatWindow c) {
		tabs.addTab(c.getName(), c.getContentPane());
	}

	/**
	 * for prototyping, just send all privmsgs to a window...
	 */
	private MessageHandler messageHandler = new MessageHandler() {

		//and put all PMS whether channel or private in one window...
		public void handle(Message msg) {
			if ( msg.getType() == MessageType.CHANNEL )
				console.out().println( "<" + msg.getSource().getNick() + "> " + msg.getMessage() );

			status.put( msg.getRaw() );	
		}
	};

	/*
	 * TEMP to provide working chatting...
	 */
	private java.awt.event.ActionListener commandListener = new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			irc.msg( CHAN, e.getActionCommand() );
			console.out().println("<" + irc.nick() + "> " + e.getActionCommand());
		}
	};
}
