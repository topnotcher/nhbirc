import java.nio.channels.SocketChannel;
import java.net.InetSocketAddress;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.SynchronousQueue;
import java.util.Queue;

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

		sendQ = new SynchronousQueue<String>();
		recvQ = new SynchronousQueue<String>();
	

		conn = new IrcConnection();
		
		//Cause the connection to enter the main loop.
		conn.run();

		register();
	}
 
	private void register() {
		sendRaw("NICK " +nick);
		sendRaw("USER " + user + " 0 * : " + real);
	}

	public void send(String cmd) {

	}

	private void sendRaw(String cmd) {
		//@TODO error checking
		//offer returns bool.
		sendQ.offer(cmd);
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

		private PrintWriter out;

		private boolean connected = false;

		private IrcConnection() throws java.io.IOException {
			conn  = SocketChannel.open();

			conn.connect(new InetSocketAddress(host,port));

			//block until connected...
			//@TODO WTF happens if there is a timeout?
//			while ( !conn.finishConnect() ) 
//				Thread.sleep(100);


			//socket is connected, set to non-blocking.
			conn.configureBlocking(false);

			
			in = new BufferedReader( new InputStreamReader(conn.socket().getInputStream()) );
			out = new PrintWriter( conn.socket().getOutputStream(), true );

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
					while ( sendQ.peek() != null )
						send( sendQ.poll() );
				
					while (  in.ready() )
						recv( in.readLine() );

					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						//not really relevant.
					}

				} catch (java.io.IOException e) {
					//@TODO
					System.out.println(e);
				}
			}
		}

		private void send(String msg) {
			System.out.println("SEND :" + msg);
			out.print( msg );
		}

		private void recv(String msg) {
			System.out.println("RECV :" +msg);
			recvQ.offer( msg );
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
