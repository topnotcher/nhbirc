
import java.awt.BorderLayout;
//import javax.swing.JFrame;
import javax.swing.*;

import java.awt.Color;

import irc.*;

import java.util.List;

class Client extends JFrame {

	private Connection irc;
	
	private ChatWindow status, debug;

	private JTabbedPane tabs;

	private List<ChatWindow> windows;

	private client.9SyncManager sync;

	public static void main(String[] argv) {
		new Client();
	}

	private final String CHAN = "#foo";

	/**
	 * **BASIC** prototype.
	 */
	private Client() {
		irc = new Connection("irc.jaundies.com", 6667, "fubar");
		
		setSize(1000,800);

		setTitle("Irc client");

		windows = new util.LinkedList<ChatWindow>();

		setVisible(true);

		//tabs is the main viewport.
		tabs = new JTabbedPane();
		add(tabs);

		//tabs contain a status window.
		tabs.setTabPlacement(JTabbedPane.BOTTOM);
	
		add( debug = new GenericChatWindow("Debug", ChatWindow.Type.STATUS) );

		add( status = new GenericChatWindow("Status", ChatWindow.Type.STATUS) );

		//prototyping purposes, just receive ALL messages...
		irc.addMessageHandler(messageHandler);

		sync = new client.SyncManager(irc);

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

		System.exit(0);
	}


	private ChatWindow getWindow(String name) {
		//heh this is efficient :/
		for (ChatWindow c : windows) 
			if ( c.getName().equals(name) )
				return c;

		return null;
	}

	private void remove(ChatWindow c) {
		tabs.remove(c.getContentPane());
		windows.remove(c);
	}

	private void add(ChatWindow c) {
		tabs.addTab(c.getName(), c.getContentPane());
		windows.add(c);
		tabs.setSelectedComponent( c.getContentPane() );
	}

	/**
	 * Handles a "slash command"
	 */
	private void handleCommand(ChatWindow src, String msg) {
		Command cmd = new Command(msg);

		System.out.println("CMD: '" +cmd.cmd+"'");

		if ( cmd.equals("JOIN") && cmd.numArgs() >= 1 ) {
			irc.join( cmd.getArg(0) );


		//note thaton part, we only send a pART to teh server
		//if the part is succesful, the server will send a PART back
		//and then the channel window will close...
		} else if ( cmd.equals("PART") ) {
	
			//there's channel specified in the command.
			if ( cmd.numArgs() > 0 && cmd.getArg(0).charAt(0) == '#' ) {

				irc.part(cmd.getArg(0), cmd.getFinal(1));

			//there was no channel specified, but it was 
			//run in a channel window
			} else if ( src.getType() == ChatWindow.Type.CHANNEL ) {
				irc.part(src.getName() , cmd.getFinal(0) );
			}
			//otherwise do nothing.
		} else if ( cmd.equals("QUIT") ) {
			irc.quit( cmd.getFinal(0) );

		} else if ( cmd.equals("MSG") ) {
			if ( cmd.numArgs() < 2 ) return;
			
			String target = cmd.getArg(0);
			String message = cmd.getFinal(1);
			
			ChatWindow c = getWindow(target);
			///fss why am I doin this.
		}
	}

	private class Command {

		String cmd;
		String msg;
		String[] args;

		private Command(String msg) {
			int sp = msg.indexOf(' ');

			//it's a command like '/part' with no arguments...
			if (sp == -1) sp = msg.length();

			this.cmd = msg.substring(1,sp).toUpperCase();

			if ( msg.length() == sp )
				args = null;
			else {
				msg = msg.substring( sp + 1 );
				args = msg.split(" ");
			}
		}

		private int numArgs() {
			return (args == null) ? 0 : args.length;
		}

		private String getArg(int n) {
			return args[n];
		}

		private String getFinal(int n) {
			String ret = "";

			for ( int i = n; args != null && i < args.length; ++i) 
				ret += args[i];

			return ret;
		}

		private boolean equals(String s) {
			return cmd.equals(s);
		}
	}

	/**
	 * for prototyping, just send all privmsgs to a window...
	 */
	private MessageHandler messageHandler = new MessageHandler() {

		//and put all PMS whether channel or private in one window...
		public void handle(Message msg) { 
			switch(msg.getType()) {
			
				case CHANNEL:
				case ACTION:
				case QUERY: 
				case NOTICE:
					handlePM(msg);
					break;
				case PART:
		
					ChatWindow window = getWindow(msg.getTarget().getChannel());

					if ( window != null ) {
			
						//this is ME leaving...
						if ( msg.getSource().getNick().equals( irc.nick() ) ) 
							remove(window);

						else 
							window.put(
								(new PaintableMessage()).append("<-- ",Color.lightGray).append(msg.getSource().getNick(), Color.white)
									.append(" [", Color.darkGray).append(msg.getSource().toString(), Color.cyan).append("]",Color.darkGray).append(" left ")
									.append(msg.getTarget().toString(),Color.cyan).append(" (",Color.darkGray).append( msg.getMessage() )
									.append(")", Color.darkGray)
							);
					}

					break;
				
				case JOIN:
					ChatWindow win = null;

					if ( msg.getSource().getNick().equals(irc.nick()) ) {
						win = new ChannelWindow( msg.getTarget().getChannel(), sync );
						win.addActionListener(commandListener);
						add( win );
					}  else {
						win = getWindow( msg.getTarget().getChannel() );
					}

					//in every case:
					if (win != null)  {
						win.put((new PaintableMessage())
							.append("--> ",Color.lightGray).append(msg.getSource().getNick(), Color.white)
							.append(" [", Color.darkGray).append(msg.getSource().toString(), Color.cyan).append("]",Color.darkGray).append(" has joined ")
							.append(msg.getTarget().toString(),Color.cyan)
						);
					}

					break;
				case MOTD:
					status.put((new PaintableMessage()).append("[",Color.gray).append("MOTD",Color.blue).append("] ",Color.gray).append(msg.getMessage() ));
					break;

				case NICKCHANGE:
				case QUIT:
					break;

				default:
					debug.put( msg.getRaw() );	
					break;
			}
		}

		/**
		 * Handle PRIVMSG or NOTICE commands.
		 */
		private void handlePM(Message msg) {
	

			ChatWindow window = null;

			//notices go to the currently selected window
			//servers should only be sending notices
			//but in case a server PMs, we handle that as a notice as well
			if ( msg.getType() == MessageType.NOTICE || msg.getSource().scope(MessageTarget.Scope.SERVER) )  {
				window = getWindow( tabs.getTitleAt(tabs.getSelectedIndex()) );

				if (window == null)
					return;
			}

			//not a notice, to a channel.
			else if (  msg.getTarget().scope(MessageTarget.Scope.CHANNEL) ) {
				window = getWindow( msg.getTarget().getChannel() );

				//don't open up new windows automatically for channels
				//that should have been done anyway...
				if (window == null)
					return;

			//it is not a notice, and not a channel message...
			//so it must be a PM... from a user!
			} else {
					
				window = getWindow( msg.getSource().getNick() );

				if ( window == null ) {
					window = new GenericChatWindow( msg.getSource().getNick() , ChatWindow.Type.QUERY );
					add(window);
				}
			}
			//now we have a window to put the damn thing in...

			window.put( new QueryMessage(msg));
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

			if ( cmd.length() < 1 ) return;

			if ( cmd.charAt(0) == '/' ) {
				handleCommand(src, cmd);

			//if it's in a status window and it doesn't start with a /, do nothing
			} else if ( src.getType() == ChatWindow.Type.STATUS ) {
				System.out.println("received in status window...");

			//otherwise, it is in some form of chat window, so send a message...
			} else {
				System.out.println("some form of Query");
				irc.msg( src.getName() , cmd );
				src.put( "<" + irc.nick() + "> " + cmd );
			}
		}
	};
}
