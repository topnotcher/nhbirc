import java.io.InputStream;
import java.io.IOException;
import javax.swing.JTextField;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Scanner;


/**
 * @TODO strip this down, remove InputStream, make it extend Text field...
 * also remove echo from this. 
 */
class TextFieldPrompt extends InputStream implements KeyListener {
	
	/**
	 * The textfield that acts as the input source
	 */
	public JTextField field;

	/**
	 * History object to hold command line history
	 */
	private History history = new History(10);

	/**
	 * Text that has been read from the textfield,
	 * but not yet read with read(...).
	 */
	byte[] buffer = new byte[0];

	/**
	 * The index of buffer being read.
	 */
	int ptr = 0;

	/**
	 * The default prompt. BORING
	 */
	String prompt = "$ ";

	/**
	 * Whether or not to echo the input back to system.out.
	 * (this should be revised and done better)
	 */
	final private boolean echo = true;

	//@TODO a single actionlistener
	private ActionListener listener = null;

	public TextFieldPrompt() {
		//creat the textfield
		field = new TextFieldPrompt.TextField();

		//listen for keys
		field.addKeyListener(this);

		//prompt for nothing.
		prompt();
	}


	public void addActionListener(ActionListener l) {
		listener = l;
	}

	public void keyReleased(KeyEvent e) {
		switch(e.getKeyCode()) {

//			case KeyEvent.VK_BACK_SPACE:
//				disable_backspace();

//				break;

			case KeyEvent.VK_ENTER:
				readField();
				//@TODO
		
				if ( listener != null ) {
					Scanner sc = new Scanner( this );
					listener.actionPerformed( new ActionEvent( this, 0, sc.nextLine()) );
				}

				break;

			case KeyEvent.VK_UP:
				prompt( history.up() );
				break;

			case KeyEvent.VK_DOWN:
				prompt(history.down());
				break;

			default:
				break;
		}
	}

	/**
	 * This is a shotty attempt at disallowing the user from backspacing over the prompt.
	 * It doesn't prevent selecting it, and overwriting it or anything.
	 *
	 * At any rate, the prompt SHOULD recover from people messing with the prompt...
	 */
	private void disable_backspace() {
		String text = field.getText();
		
		//the first two "parts" of this just fix StringIndexOutofBoundsExceptions
		//the second part makes sure the prompt is still there.
		//this is by no means fool proof...
		if ( text.length() <= prompt.length() || prompt.length() < 1 || !text.substring(0, prompt.length()).equals(prompt) ) {
			history.reset();
			prompt();
		}
	}
	private void prompt() {
		prompt("");
	}

	private void prompt(String append) {

		if (append == null) return;

		prompt = append;

		int len = (append == null || append.length() == 0) ? 0 : append.length();

		field.setText(prompt + ((len == 0) ? "" : append));
		field.setCaretPosition(prompt.length()+len);
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
	}	

	/**
	 * This reads the text in the textfield, adds it to the buffer,
	 * and adds the command typed to the command-line history.
	 *
	 * It should be noted, that text is only read from the field and
	 * appended to the buffer when the user hits the enter key.
	 */
	public void readField() {
		String text = field.getText();
		
		//terminal history -- fix this
//		if (echo) 
//			System.out.println(text);
		
		if (text.length() < prompt.length())  {
			prompt("");
			return;
		}

		text = text.substring(prompt.length());

		history.add(text);

		text = text + "\n";

		synchronized(this) {
			appendBuffer(text.getBytes());
			prompt();
		}
	}

	/**
	 * This is what ACTUALLY appends text to the buffer
	 */
	private void appendBuffer(byte[] buf) {
		//buffer is empty or comlpetely read
		if (buffer.length < 1 || ptr >= buffer.length) {
			buffer = buf;
			ptr = 0;
			return;
		}

		byte[] tmp = new byte[buffer.length+buf.length];

		try {
		//this could be moe efficient by not copying the items before ptr.
			for (int i = ptr; i < buffer.length+buf.length; i++) {
				if (i < buffer.length)
					tmp[i] = buffer[i];

				else
					tmp[i] = buf[i];


			}	
			buffer = tmp;

		//tmp[i] = buf[i] is causing an exception i need to figure out...
		//only seems to happen when the buffer isn't empty before appending..
		} catch (ArrayIndexOutOfBoundsException e) {
			//at any rate, don't die over one command....
			return;
		}

	}
	
	/**
	 * This reads bytes from buffer (stuff already read from field)
	 */
	public int read(byte[] b, int off, int len) {
	
		waitForInput();

		int i;
		for (i = 0; i < len && ptr < buffer.length; i++,ptr++) {
			b[i+off] = buffer[ptr];
		}

		//If we're at the end of the buffer
		if (ptr >= buffer.length) {
			synchronized(this) {
				//reset it
				ptr = 0;
				buffer = new byte[0];
			}
		}

		//return the characters read
		return i-off;
	}

	/**
	 * Block while waiting for input.
	 */
	private void waitForInput() {
		while (buffer.length < 1) {
			Thread.yield();

			try {
				Thread.sleep(50);
			} catch (Exception e) {
				//doo nothing.
			}
		}

	}

	/** 
	 * read the (next) single byte.
	 *
	 * This defers to read(byte[], int, int) for efficiency.
	 * (by default the other read() defers to this one)
	 */
	public int read()  {

		byte[] buf = new byte[1];

		int b = read(buf,ptr,1);

		return (int)buf[0];
	}

	/**
	 * set the text to show in the prompt
	 */
	public void setPrompt(String prompt) {
		prompt();
	}

	public class TextField extends JTextField {
		public TextField() {
		}
		public void paintBorder(java.awt.Graphics g) {
			//disable painting the textbox's border
			//this is really all this clas does at the moment (gasp)
		}
	}
}
