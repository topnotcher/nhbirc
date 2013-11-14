import java.io.IOException;


import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

//import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
//import com.coldsteelstudios.util.PriorityBlockingQueue;
//import com.coldsteelstudios.util.BlockingQueue;




//connection thread
private class Connection {

	private static enum State { DISCONNECTED, CONNECTED; }

	private State state;

	//per RFC, max size of irc message...
	private static final int MSG_SIZE = 512;

	private Socket conn = null;

	private PrintWriter out;

	private BufferedReader in;

	private long last_tx;
	private long last_rx;

	/**
	 * Priority Queue of outgoing messages.
	 */
	private BlockingQueue<OutgoingMessage> sendQ;
	
	/**
	 * Priority Queue of Incoming messages.
	 */
	private BlockingQueue<Message> recvQ;

	private Connection(String host, String port) throws IOException {
		state = State.DISCONNECTED;

		conn = new Socket(host,port);

		out = new PrintWriter( conn.getOutputStream(), false );
		in = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );

		last_tx = last_rx = System.currentTimeMillis();

		sendQ = new LinkedBlockingQueue<String>();
		recvQ = new LinkedBlockingQueue<Message>();
		
		//setting state = connected starts all the fun loops..
		setState(state.CONNECTED);
	
		(new Thread( readerThread, "Socket Read" )).start();
		(new Thread( writerThread, "Socket Write" )).start();
	}

	private Runnable readerThread = new Runnable() {

		/**
		 * @TODO exception handling
		 */
		public void run() {
		
			if ( state == State.DISCONNECTED ) 
				throw new ConnectionStateException("Trying to run wihout being connected???");

			while ( state != State.DISCONNECTED ) try {	
				/**
				 * NOTE: this *should* work.  
				 * Not 100% sure in the case of CTCPS
				 * I believe CR/LF characters must be quoted in in a CTCP (by the server, presumably),
				 * but in any case, this should be changed to a character-by-character read for two reasons:
				 *
				 * (1) enforce the MSG_SIZE
				 * (2) An unquoted embedded CR\LF inside a CTCP would cause the message to be split into two
				 * 	thus allowing message injection. Probably not a good idea to rely on the server to do the quoting...
				 */
				recv( in.readLine() );
	
			} catch (java.io.IOException e) {
				//@TODO
				e.printStackTrace();
				close();
			}
		}
	};

	private Runnable writerThread = new Runnable() {

		/**
		 * @TODO exception handling
		 */
		public void run() {
		
			if ( state == State.DISCONNECTED ) 
				throw new ConnectionStateException("Trying to run wihout being connected???");


			while( state != State.DISCONNECTED ) try {
				sendRaw( sendQ.poll(5, java.util.concurrent.TimeUnit.MILLISECONDS) );
			} catch (InterruptedException e) {
				//DGAF
			}
		}
	};

	public void send(String msg) {

		if ( state == state.DISCONNECTED ) 
			//@TODO
			throw new RuntimeException("Trying to execute commands in a disconnected state...?");

		//@TODO error checking
		//offer returns bool.
		if (!sendQ.offer(msg))
			throw new RuntimeException("Failed to queue message: " + cmd);

	}

	private void sendRaw(String msg) {

		if (msg == null) return;

		//connection could have died while waiting for something
		//to be put onto the outgoing queue.
		if (state == State.DISCONNECTED) return;

		last_tx = System.currentTimeMillis();

		try { 
			out.print( msg );
			out.print( "\r\n" );
		//	conn.setSendBufferSize(msg.getMessage().length()+2);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			close();
		}
	}

	/**
	 * Receives/buffers any data on the channel
	 */
	private void recv(String msg) {

		if (msg == null) return;

		//in case the connection died/was killed while waiting to read 
		//(which doesn't make sense...)
		if (state == State.DISCONNECTED) return;

		last_rx = System.currentTimeMillis();

		handleRaw(msg);
	}

	//handle a raw received message
	private void handleRaw(String raw) {
		if (raw.length() == 0) return;
	
		try {
			Message msg = MessagParser.parse( this, raw);

			if ( msg.getType() == MessageType.PING ) 
				sendRaw( "PONG " + msg.getMessage() ) 
			else if ( msg.getCommand().equals("ERROR") )
				close();

			recvQ.offer(msg);

		//probably best if incoming messages can't kill the client.
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public void close() {
		//stop everything from looping
		setState(State.DISCONNECTED);
		
		sendQ.clear();

		try {
			conn.close();
		} catch (Exception e) {
			//not really anyhing we CAN do...
		}
	}

	protected void finalize() {
		close();
	}
}
