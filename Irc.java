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
	
	private Queue<IrcMessage> recvQ;


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
		recvQ = new ConcurrentLinkedQueue<IrcMessage>();
	
		System.out.println("Creating a new IRC connection...");

		conn = new IrcConnection();

		System.out.println("DONE Connecting; Enterint main loop.");

		
		//Cause the connection to enter the main loop.
		(new Thread( conn )).start();

		(new Thread( new MessageHandler() )).start();

		System.out.println("Run returned...");

		register();
	}
 
	private void register() {
		sendRaw("NICK " +nick);
		sendRaw("USER " + user + " 0 * : " + real);
	}

	private void handleRaw(String raw) {

		if (raw.length() == 0) return;

		IrcMessage msg = new IrcMessage(raw);

		if ( msg.getCommand().equals("PING") )
			sendRaw("PONG :" + msg.getMessage());
		else
			recvQ.offer(msg);
	}

	public void send(String cmd) {
		//@TODO
		sendRaw(cmd);
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
				
				while ( recvQ.peek() != null ) 
					System.out.println("HANDLE: " + recvQ.poll() );
					
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {	
					//done.
				}
			}
		}
	}

	private class IrcConnection implements Runnable {
	
		//BLEH
		private static final int MSG_SIZE = 512;

		private SocketChannel conn = null;

		private boolean connected = false;

		Selector selector;

		CharBuffer msg;

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
					selector.select();
					
					Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

					while ( keys.hasNext() ) {

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
					e.printStackTrace();
				}
			}
		}

		private void send(SocketChannel channel, String msg) {
			System.out.println("SEND :" + msg);

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
