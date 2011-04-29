
import java.awt.BorderLayout;
//import javax.swing.JFrame;
import javax.swing.*;

import irc.*;

import java.util.List;

class Client extends JFrame {

	private Connection irc;
	
	private ChatWindow status, channel;

	private JTabbedPane tabs;

	private List<ChatWindow> windows;

	private client.SyncManager sync;

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

		windows = new util.LinkedList<ChatWindow>();

		setVisible(true);

		//tabs is the main viewport.
		tabs = new JTabbedPane();
		add(tabs);

		//tabs contain a status window.
		tabs.setTabPlacement(JTabbedPane.BOTTOM);

		add( status = new GenericChatWindow("Status", ChatWindow.Type.STATUS) );

		sync = new client.SyncManager(irc);

		//prototyping purposes, just receive ALL Pms
		irc.addMessageHandler(messageHandler);

		try {
			//@TODO
			irc.connect();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}

		irc.join( CHAN );

		System.out.println("Thread is going to sleep...");

		synchronized(irc) {
			try {
				irc.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("IRC changed it's state... goodbye.");

	}

	private void remove(ChatWindow c) {
		tabs.remove(c.getContentPane());
		windows.remove(c);
	}

	private void add(ChatWindow c) {
		tabs.addTab(c.getName(), c.getContentPane());
		windows.add(c);
	}

	/**
	 * for prototyping, just send all privmsgs to a window...
	 */
	private MessageHandler messageHandler = new MessageHandler() {

		//and put all PMS whether channel or private in one window...
		public void handle(Message msg) {

			if ( msg.getType() == MessageType.CHANNEL ) {

				for (ChatWindow c : windows) {
					
					if ( c.getType() == ChatWindow.Type.CHANNEL && c.getName().equals(msg.getTarget().getChannel()) ) {
						c.put( "<" + msg.getSource().getNick() + "> " + msg.getMessage() );
						break;
					}

				}

			} else if ( msg.getType() == MessageType.PART ) {

				ChatWindow window = null;

				for (ChatWindow w : windows) {
					if ( w.getType() == ChatWindow.Type.CHANNEL && w.getName().equals(msg.getTarget().getChannel())) {
						window = w;
						break;
					}
				}

				if ( window != null ) {
			
					//this is ME leaving...
					if ( msg.getSource().getNick().equals( irc.nick() ) ) 
						remove(window);

					else 
						window.put(" <-- " + msg.getSource().getNick() + " left the channel");
				}
				
	
			} else if ( msg.getType() == MessageType.JOIN && msg.getSource().getNick().equals(irc.nick()) ) {
				ChatWindow win = new ChannelWindow( msg.getTarget().getChannel(), sync );
				win.addActionListener(commandListener);				
				add(win);
			}

			status.put( msg.getRaw() );	

		}
	};

	/*
	 * TEMP to provide working chatting...
	 */
	private java.awt.event.ActionListener commandListener = new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			
			//@TODO
			if ( ! (e.getSource() instanceof ChatWindow) )  
				throw new RuntimeException("Why am I receiving commands from a non-chat window???");

			ChatWindow src = (ChatWindow)e.getSource();
			String cmd = e.getActionCommand();

			if ( src.getType() == ChatWindow.Type.STATUS ) {
				int pos = cmd.indexOf(' ');
				
				if (pos == -1) return;

				irc.send( cmd.substring(0,pos), cmd.substring(pos+1) );

			} else {
				irc.msg( src.getName() , e.getActionCommand() );
				src.put("<" + irc.nick() + "> " + e.getActionCommand());
			}
		}
	};
}
