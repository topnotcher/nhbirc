import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.net.InetSocketAddress;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.nio.CharBuffer;
import java.util.List;

public class Irc {
	
	/**
	 * Server for the IRC connection
	 */
	private String host;

	/**
	 * Port of the IRCd on host.
	 */
	private int port;

	//nickname, username, realname.
	private String nick,user,real;

	//queue of messages to send.
	private Queue<String> sendQ;
	
	//queue of messages received.
	private Queue<IrcMessage> recvQ;

	//Irc connection
	private IrcConnection conn;

	//message handlers (subscribers)
	private List<IrcMessageHandler> handlers = new LinkedList<IrcMessageHandler>();
	
	//whether or not registration phase of connection is complete
	//(no non-registration commands can be sent until this is done)
	private boolean registered = false;

	public Irc(String host, int port, String nick) {
		this(host,port,nick,nick);
	}

	public Irc(String host, int port, String nick, String user) {
		this(host,port,nick,user,nick);
	}

	public Irc(String host, int port, String nick, String user, String real) {
		this.user = user;
		this.host = host;
		this.port = port;
		this.nick = nick;
		this.user = user;
		this.real = real;
	}

	//attempt to connect
	public void connect() throws java.io.IOException {

		//will replace these with own implementation of queue/prioirty queue later.
		sendQ = new ConcurrentLinkedQueue<String>();
		recvQ = new ConcurrentLinkedQueue<IrcMessage>();
	
//		System.out.println("Creating a new IRC connection...");

		//connect
		//(blocks until connected)
		conn = new IrcConnection();

		
		//Cause the connection to enter the main loop.
		(new Thread( conn, "Connection" )).start();

		//handle messages in a separate thread
		//so the socket I/O never pauses
		//(This can be important in the case of a flood of messages which require
		//some potentially long, synchonous operation to handle. In this case,
		//a PING could be left unread which would cause a pint timeout.
		//)
		(new Thread( new MessageHandler(), "Message Handler" )).start();

		//initiatite registration
		register();

		//listen for IRC welcome message
		addMessageHandler("001", this.internalHandler);

		//This blocks while the connection is registering
		//@TODO timeout...
		while (!registered) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			
			}
		}

		//the connection is registered, so the client is now in a usable state and can execute commands.
	}

	//handle a single command
	public void addMessageHandler(String cmd, IrcMessageHandler handler) {
		String[] cmds = {cmd};
		addMessageHandler(cmds, handler);
	}
	
	//handle an array of commands
	public void addMessageHandler(String[] cmds, IrcMessageHandler handler) {
		handlers.add( new IrcMessageSubscription(cmds,handler) );
	}

	//@TODO wildcard handler?

	//initiate registration
	private void register() {
		sendRaw("NICK " +nick);
		sendRaw("USER " + user + " 0 * : " + real);
	}


	//handle a raw received message
	private void handleRaw(String raw) {
		if (raw.length() == 0) return;

		IrcMessage msg = new IrcMessage(raw);
	
		//temp ping impl
		//@TODO: parser with priorities
		if ( msg.getCommand().equals("PING") ) {
			send("PONG", msg.getMessage());
		}
		//if it isn't a ping queue it
		else
			recvQ.offer(msg);

//		System.out.println("RECV " + raw);
	}

	//public send
	public void send(String cmd, String msg) {
		//@TODO
		sendRaw(cmd + " :" + msg);
	}

	private void sendRaw(String cmd) {
		//@TODO error checking
		//offer returns bool.
		if (!sendQ.offer(cmd))
			throw new RuntimeException("Failed to queue message: " + cmd);

//		System.out.println("QUEUE: " + cmd);
	}

	/**
	 * Issue the nick command...
	 */
	public void setNick(String nick) {
		//send the nick command...	
		sendRaw("NICK :"+nick);
		this.nick = nick;

		//@TODO monitor for failed nick changes.
		//only set the nick on a successful reply...
	}

	public String getNick() {
		return this.nick;
	}
	public void quit() {
		quit("Client exited.");
	}

	public void quit(String msg) {
		send("QUIT", msg);
		conn.close();
		conn = null;
	}

	//a subscription to irc messages
	private class IrcMessageSubscription implements IrcMessageHandler {
		private String[] cmds;
		
		private IrcMessageHandler handler;
			
		public IrcMessageSubscription(String[] cmds, IrcMessageHandler handler) {
			this.cmds = cmds;
			this.handler = handler;
		}

		//tests if this subscription matches, calls the handlers handle if it does.
		public void handle(IrcMessage msg) {
			for ( String cmd : cmds ) 
				if (cmd.equals(msg.getCommand())) 
					handler.handle(msg);
				
		}
	}
	
	//handler for some internal stuff.
	private IrcMessageHandler internalHandler = new IrcMessageHandler() {

		public void handle(IrcMessage msg) {
			if ( msg.getCommand().equals("001"))
				registered = true;
		}
	};

	//message handler thread
	private class MessageHandler implements Runnable {

		public MessageHandler() {
	
		}

		public void run() {

			while(true) {
				
				while ( recvQ.peek() != null ) { 

					Iterator<IrcMessageHandler> it = handlers.iterator();
						
					while (it.hasNext()) 
						it.next().handle( recvQ.peek() );

					recvQ.poll();
				}
						
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {	
					//done.
				}
			}
		}
	}

	//connection thread
	private class IrcConnection implements Runnable {

		//per RFC, max size of irc message...
		private static final int MSG_SIZE = 512;

		private SocketChannel conn = null;

		private boolean connected = false;

		private Selector selector;

		private CharBuffer msg;

		ByteBuffer out;

		private IrcConnection() throws java.io.IOException {
			conn  = SocketChannel.open();

			conn.connect(new InetSocketAddress(host,port));	

			//socket is connected, set to non-blocking.
			conn.configureBlocking(false);
			
			selector = Selector.open();

			conn.register( selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

			msg = CharBuffer.allocate(MSG_SIZE);

			connected = true;
		}

		/**
		 * @TODO exception handling
		 */
		public void run() {
			
			if (connected == false) 
				throw new RuntimeException("Trying to loop over non connection???");

			while(connected) {
			
				try {	

					//block until a socket is selected...
//					System.out.println("select()");
					selector.select();
					
					Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

					while ( keys.hasNext() ) {

//						System.out.println("keys..");
						SelectionKey key = keys.next();
						SocketChannel keyChannel = (SocketChannel)key.channel();

						keys.remove();

						if (sendQ.peek() != null)
//							System.out.println("There are commands to send");

			
						if ( key.isWritable() ) 
							while ( sendQ.peek() != null )
								send( keyChannel, sendQ.poll() );

						if ( key.isReadable() ) 
							recv( keyChannel );
					}

					try {
						Thread.sleep(150);
					} catch (InterruptedException e) {
						//not really relevant.
					}
			
				} catch (Exception e) {
					//@TODO
					e.printStackTrace();
				}

			}

//			System.out.println("STopped Running");
		}

		private void send(SocketChannel channel, String msg) {
//			System.out.println("SEND :" + msg);

			msg += "\n";
			
			//@TODO
			if (msg.length() > MSG_SIZE)
				throw new RuntimeException("Command too large...");

			ByteBuffer out = ByteBuffer.allocate(MSG_SIZE);
			out.put(msg.getBytes());
			out.flip();

			try { //@TODO
				while (out.hasRemaining())
					channel.write(out);
			} catch (java.io.IOException e) {
				e.printStackTrace();
			}

		}

		/**
		 * Receives/buffers any data on the channel
		 */
		private void recv(SocketChannel channel) {

			char tmp;

			//@TODO fix all of this.
			ByteBuffer in = ByteBuffer.allocate(MSG_SIZE);

			java.nio.charset.Charset charset = java.nio.charset.Charset.forName("ISO-8859-1");
			java.nio.charset.CharsetDecoder decoder = charset.newDecoder();
			java.nio.CharBuffer cBuf;

			try {
				channel.read(in);
				in.flip();
				
				//create a character buffer from the bytes read.
				cBuf = decoder.decode(in);

				//for each character in the buffer
				while ( cBuf.remaining() > 0 &&  ( tmp = cBuf.get() ) != '\0' ) {
					
					//if it is a 'newline' (RFC says either of these suffices), end the command.
					if (tmp == '\r' || tmp == '\n') {

						int pos = msg.position();
						msg.clear();

						//because clearing the buffer just resets the position, so if we 
						//don't take the correct subseq, old data from the last command will be passed.
						handleRaw( msg.subSequence(0,pos).toString() );
					}
					else {
						///otherwise, put teh character on the buffer.
						msg.put(tmp);
					}
				}

			} catch (java.io.IOException e) {
				e.printStackTrace();
			}

		}
		public void close() {
			
			//stop the thread from looping.
			connected = false;

			try {
				//should socket be set to block first?
				conn.socket().close();
			} catch (Exception e) {
				//@TODO
				e.printStackTrace();
			}
		}

		protected void finalize() {
			close();
		}
	}
}
