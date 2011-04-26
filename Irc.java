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

public class Irc {
	
	/**
	 * Server for the IRC connection
	 */
	private String host;

	/**
	 * Port of the IRCd on host.
	 */
	private int port;

	private String nick,user,real;

	private Queue<String> sendQ;
	
	private Queue<String> recvQ;


	private IrcConnection conn;
	
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

		sendQ = new ConcurrentLinkedQueue<String>();
		recvQ = new ConcurrentLinkedQueue<String>();
	
		System.out.println("Creating a new IRC connection...");

		conn = new IrcConnection();

		System.out.println("DONE Connecting; Enterint main loop.");

		
		//Cause the connection to enter the main loop.
		(new Thread( conn )).start();

		System.out.println("Run returned...");

		register();
	}
 
	private void register() {
		System.out.println("Starting registration");
		sendRaw("NICK " +nick);
		sendRaw("USER " + user + " 0 * : " + real);
		System.out.println("Commands Queued");

		for ( String foo : sendQ.toArray( new String[sendQ.size()] ) )
			System.out.println("Queued: " +foo);

		System.out.println("Peek returns: "+sendQ.peek());
	}

	private void handle(String msg) {
		System.out.println("RECV: " + msg);
	}
	public void send(String cmd) {

	}

	private void sendRaw(String cmd) {
		//@TODO error checking
		//offer returns bool.
		if (!sendQ.offer(cmd))
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

	public void quit() {
		quit("Client exited.");
	}

	public void quit(String msg) {
		sendRaw("QUIT :"+msg);
		conn.close();
		conn = null;
	}

	private class MessageHandler implements Runnable {

		public MessageHandler() {
	
		}

		public void run() {
			while(true) {


			}
		}
	}

	private class IrcConnection implements Runnable {
		private SocketChannel conn = null;

		private BufferedReader in;

//		private PrintWriter out;

		private boolean connected = false;

		Selector selector;

		ByteBuffer buf;

		CharBuffer msg;

		private IrcConnection() throws java.io.IOException {
			conn  = SocketChannel.open();

			conn.connect(new InetSocketAddress(host,port));	

			conn.finishConnect();

			//socket is connected, set to non-blocking.
			conn.configureBlocking(false);
		
	//		in = new BufferedReader( new InputStreamReader(conn.socket().getInputStream()) );
	//		out = new PrintWriter( conn.socket().getOutputStream(), true );

			selector = Selector.open();

			conn.register( selector, /*SelectionKey.OP_READ | SelectionKey.OP_WRITE */ conn.validOps());

			//allocate charbufffer

			msg = CharBuffer.allocate(512);

			connected = true;
		}

		/**
		 * @TODO exception handling
		 */
		public void run() {
			
			if (connected == false) 
				throw new RuntimeException("Trying to loop over non connection???");

			buf = ByteBuffer.allocate(512);

			while(connected) {

				try {	

				
					selector.select();
					
//					System.out.println("Select returned");

					Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

					while ( keys.hasNext() ) {

//						System.out.println("Iter keys");
						SelectionKey key = keys.next();
						SocketChannel keyChannel = (SocketChannel)key.channel();

						keys.remove();

						if ( key.isWritable() ) while ( sendQ.peek() != null )
							send( keyChannel, sendQ.poll() );

						if ( key.isReadable() ) 
							recv( keyChannel );
					}

					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						//not really relevant.
					}
			
				} catch (Exception e) {
					//@TODO
					System.out.println(e);
				}
			}
		}

		private void send(SocketChannel channel, String msg) {
			System.out.println("SEND :" + msg);

			msg = msg+"\n";

			buf.clear();
			buf.put(msg.getBytes());
			buf.flip();

			try {
				while (buf.hasRemaining())
					channel.write(buf);
			} catch (java.io.IOException e) {
				System.out.println(e);
			}


		}

		private void recv(SocketChannel channel) {

			char tmp;

			//@TODO fix all of this.
			ByteBuffer in = ByteBuffer.allocate(1024);

			java.nio.charset.Charset charset = java.nio.charset.Charset.forName("ISO-8859-1");
			java.nio.charset.CharsetDecoder decoder = charset.newDecoder();
			java.nio.CharBuffer cBuf;
			in.clear();

			try {
				channel.read(in);
				in.flip();
				cBuf = decoder.decode(in);

				while ( cBuf.remaining() >= 2 && ( tmp = cBuf.get() ) != '\0' ) {
					
					if (tmp == '\r' || tmp == '\n') {
						handle(msg.toString());
						msg.clear();
					}
					else {
						msg.put(tmp);
					}
				}

			} catch (java.io.IOException e) {
				System.out.println(e);	
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
				System.out.println(e);
			}
		}

		protected void finalize() {
			close();
		}
	}
}
