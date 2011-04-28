package irc;

import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.net.InetSocketAddress;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
//import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

import java.util.Queue;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.nio.CharBuffer;
import java.util.List;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class Connection {
	
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
	private Queue<OutgoingMessage> sendQ;
	
	//queue of messages received.
	private Queue<Message> recvQ;

	//Irc connection
	private IrcConnection conn;

	//message handlers (subscribers)
	private List<IrcMessageSubscription> handlers = new util.LinkedList<IrcMessageSubscription>();
	
	//whether or not registration phase of connection is complete
	//(no non-registration commands can be sent until this is done)
	private boolean registered = false;

	//how the server identifies itself in the 001
	private String hostname = "none";

	private long last_tx = 0;

	private long last_rx = 0;

	private long last_ping;

	//minimum time between pings = 30 seconds...
	private static final int PING_TIMEOUT = 30000;

	//max idle of 1.5 minutes...
	private static final int MAX_IDLE = 1000*60*3;


	public Connection(String host, int port, String nick) {
		this(host,port,nick,nick);
	}

	public Connection(String host, int port, String nick, String user) {
		this(host,port,nick,user,nick);
	}

	public Connection(String host, int port, String nick, String user, String real) {
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
		sendQ = new PriorityBlockingQueue<OutgoingMessage>();
		recvQ = new PriorityBlockingQueue<Message>();
	
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
		(new Thread( new Worker(), "Message Handler" )).start();

		//initiatite registration
		register();

		//listen for IRC welcome message
		String[] subs = { MessageCode.RPL_WELCOME.getCode() , "PING"};

		addMessageHandler(this.internalHandler)
			.addType( MessageType.LOGIN )
			.addType( MessageType.PING )
			.addCode( MessageCode.RPL_WELCOME )
			.addCommand( "PING" )
		;

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

	/**
	 * Provides a fluent interface...
	 */
	public IrcMessageSubscription addMessageHandler(MessageHandler handler) {

		IrcMessageSubscription sub = new IrcMessageSubscription(handler); 
		handlers.add( sub );

		return sub;
	}

	//initiate registration
	private void register() {
		sendRaw("NICK " +nick);
		sendRaw("USER " + user + " 0 * : " + real);
	}

	private void ping() {

		if ( System.currentTimeMillis() - last_ping <= PING_TIMEOUT )
			return;

		last_ping = System.currentTimeMillis();
			
		send("PING", hostname, Priority.CRITICAL);
	}

	//handle a raw received message
	private void handleRaw(String raw) {
		if (raw.length() == 0) return;
	
		recvQ.offer( MessageParser.parse( raw ) );

	}

	//public send
	public void send(String cmd, String msg) {
		send(cmd,msg,Priority.MEDIUM);
	}

	public void send(String cmd, String msg, Priority p) {
		sendRaw(cmd + " :" + msg, p);
	}

	private void sendRaw(String cmd) {
		sendRaw(cmd, Priority.MEDIUM);
	}

	private void sendRaw(String cmd, Priority p) {

		//@TODO error checking
		//offer returns bool.
		if (!sendQ.offer( new OutgoingMessage(cmd,p) ))
			throw new RuntimeException("Failed to queue message: " + cmd);

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
	public class IrcMessageSubscription {
			
		private Set<MessageType> types = null;
		private Set<MessageCode> codes = null;

		private List<String> cmds = null;
		private List<Pattern> patterns = null;

		private MessageHandler handler;
			
		private IrcMessageSubscription(MessageHandler handler) {
			this.handler = handler;
		}

		/**
		 * Add a type to the subscription
		 * @return provides a fluent interface
		 */
		public IrcMessageSubscription addType(MessageType type) {
			
			if  ( types == null ) 
				types = new TreeSet<MessageType>();

			types.add(type);

			return this;
		}

		/**
		 * Add a code to the subscription
		 * @return provides a fluent interface
		 */
		public IrcMessageSubscription addCode(MessageCode code) {

			if  ( codes == null ) 
				codes = new TreeSet<MessageCode>();

			codes.add(code);

			return this;
		}

		public IrcMessageSubscription addCommand(String cmd) {
			
			if ( cmds == null ) 
				cmds = new util.LinkedList<String>();

			cmds.add(cmd);

			return this;
		}

		public IrcMessageSubscription addPattern(Pattern p) {
			
			if ( patterns == null )
				patterns = new util.LinkedList<Pattern>();

			patterns.add(p);

			return this;
		}

		//tests if this subscription matches, calls the handlers handle if it does.
		private void handle(Message msg) {

			//Type must ALWAYS match...
			if ( this.types != null && !types.contains( msg.getType() ) )
				return;

			//code or pattern must match...
			boolean codeMatches = true,
					cmdMatches = true;

			if ( this.codes != null && !codes.contains( msg.getCode() ) )
				codeMatches = false;

		
			//if the code DOESN'T match and there are commands to match.
			if ( !codeMatches && this.cmds != null ) {
				cmdMatches = false;

				for (String cmd : cmds) {
					if ( cmd.equals(msg.getCommand()) ) {
						cmdMatches = true;
						break;
					}
				}
			}

			//if we didn't find a command or code match...
			if ( !(cmdMatches || codeMatches) ) 
				return;

			//Patterns must always match....
			if ( this.patterns != null ) {
				boolean found = false;

				for (Pattern p : patterns) {
					if ( p.matcher( msg.getMessage() ).matches() ) {
						found = true;
						break;
					}
				}

				if (!found) return;
			}

			//if we haven't returne by this point, the message must be a match...
			handler.handle(msg);		
		}
	}
	
	//handler for some internal stuff.
	private MessageHandler internalHandler = new MessageHandler() {

		public void handle(Message msg) {
			if ( msg.getCode() == MessageCode.RPL_WELCOME ) {
				registered = true;
				hostname = msg.getSource().getHost();
			} else if ( msg.getCommand().equals("PING") )

				//preempt!
				send( "PONG", msg.getMessage(), Priority.CRITICAL );
		}
	};

	//message handler thread
	private class Worker implements Runnable {

		private Worker() {
	
		}

		public void run() {

			while(true) {

				if ( (System.currentTimeMillis() - last_tx > MAX_IDLE/2) || (System.currentTimeMillis() - last_rx) > MAX_IDLE/2 ) 
					ping();

				while ( recvQ.peek() != null ) { 

					Iterator<IrcMessageSubscription> it = handlers.iterator();
						
					while (it.hasNext()) try {
						it.next().handle( recvQ.peek() );
					} catch (Exception e) {
						//TODO need a way to handle these...
						e.printStackTrace();
					}

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

	private static class OutgoingMessage implements Comparable<OutgoingMessage> {
		
		Priority priority;
		String msg;

		private OutgoingMessage(String msg) {
			this(msg,Priority.MEDIUM);
		}

		private OutgoingMessage(String msg, Priority priority) {
			this.priority = priority;
			this.msg = msg + "\n";
		}

		public String getMessage() {
			return msg;
		}

		public int compareTo( OutgoingMessage msg ) {
			return this.priority.getValue() - msg.priority.getValue();
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

		private ByteBuffer out;

		private IrcConnection() throws java.io.IOException {
			conn  = SocketChannel.open();

			conn.connect(new InetSocketAddress(host,port));	

			//socket is connected, set to non-blocking.
			conn.configureBlocking(false);
			
			selector = Selector.open();

			conn.register( selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

			msg = CharBuffer.allocate(MSG_SIZE);
	
			last_tx = last_rx = System.currentTimeMillis();

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

			
						if ( key.isWritable() ) {
		
							//rudementary keepalive.
							//@TODO

							while ( sendQ.peek() != null )
								send( keyChannel, sendQ.poll() );
						}

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

		private void send(SocketChannel channel, OutgoingMessage msg) {
//			System.out.println("SEND :" + msg);


			//@TODO
			if (msg.getMessage().length() > MSG_SIZE)
				throw new RuntimeException("Command too large...");

			ByteBuffer out = ByteBuffer.allocate(msg.getMessage().length());
			out.put(msg.getMessage().getBytes());
			out.flip();

			try { //@TODO
				while (out.hasRemaining())
					channel.write(out);

				last_tx = System.currentTimeMillis();

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

						last_rx = System.currentTimeMillis();

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
