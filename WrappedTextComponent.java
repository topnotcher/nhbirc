
import java.awt.Font;
import java.awt.Graphics;
import java.awt.FontMetrics;
import javax.swing.JComponent;

import java.util.List;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;


/**
 * A component that displays word-wrapped lines of text.
 */
class WrappedTextComponent extends JComponent implements Iterable<String> {

	/**
	 * Number of lines of text to store.
	 */
	private static final int BUF_SIZE = 250;

	//two pixesls between lines, on sides, from top/bottom.
	private final int PAD = 2;

	/**
	 * First node in a linked data structure containing lines of text
	 */
	private TextNode head = null;

	/**
	 * Last node
	 * Could use linkedlist class here, but making a new 
	 * data structure allows me to keep track of the last node, 
	 * which allows for easy removing of the last node when BUF_SIZE is exceeded
	 */
	private TextNode foot = null;

	/**
	 * # of lines of text currently stored.
	 */
	private int size = 0;


	public WrappedTextComponent() { 
	}

	/**
	 * Append a line of text to the component
	 */
	public void append( String text ) {

		TextNode add = new TextNode( text );

		if ( head != null) {
			add.prev = null;
			add.next = head;

			head.prev = add;

			head = add;
		} else {
			head = add;
			foot = add;
		}

		//if the message > the buffer size,
		//we drop the last item...
		if ( ++size > BUF_SIZE ) {
			
			//set a new foot for the list
			foot = foot.prev;
			
			//for the hell of it, remove the old foot's reference to the list
			//(Just in case it is left hanging around or something)
			foot.next.prev = null;

			//then mark the foot as the end of the list;
			foot.next = null;
		}

		repaint();
	}

	public void paint(Graphics g) {

		final FontMetrics METRICS = getFontMetrics(getFont());
		final int FONTHEIGHT = METRICS.getHeight() + PAD;

		final int HEIGHT = getHeight() - PAD;
		final int WIDTH = getWidth() - 2*PAD;

		final int ROWS = (int)(HEIGHT/FONTHEIGHT);

		int remaining = ROWS;
		Iterator<String> lines = iterator();

		while ( lines.hasNext() && remaining > 0 ) {
			String[] rows = getRows( lines.next() ); 

			for (int i = rows.length - 1; i >= 0 && remaining > 0; --i,--remaining) 
				g.drawString( rows[ i ], PAD, remaining*FONTHEIGHT);
		}
	}

	/**
	 * @TODO find a better way to organize this method.
	 * Especially find a better way to handle the four space indent
	 */
	private String[] getRows( String line ) {

		final FontMetrics METRICS = getFontMetrics(getFont());
		final int WIDTH = getWidth() - 2*PAD;

		//hold the output in this...
		List<String> rows = new util.LinkedList<String>();

		String row = "";
		int remaining = WIDTH;

		//I use "    " as an indent
		//this is a cheap hack to fix an infinite loop
		//if the window is too small
		if ( METRICS.stringWidth("    ") > WIDTH ) 
			return new String[0];

		//first, we attempt to split the thinggy at spaces...
		StringTokenizer st = new StringTokenizer(line, " ",true); 

		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			int width = METRICS.stringWidth(tok);
			
			//if the current token will fit in the current row
			if ( width <= remaining ) {
				row += tok;
				remaining -= width;
				continue;

			//in this case, the current token wn't fit in a row PERIOD,
			//so it definitely *has* to be split up.
			} else if ( width > WIDTH) {
	
				//now we're (lol) going to go through the same process
				//as getRows(), but where each token is a character!
				for (int i = 0; i < tok.length(); ++i) {

					char chr = tok.charAt(i);
					width = METRICS.charWidth(chr);

					if  ( width <= remaining ) {
						remaining -= width;
						row += chr;
						continue;
					} else {
						rows.add(row);
						row = "    ";
						remaining = WIDTH - METRICS.stringWidth(row);
					}
				}
				continue;
			
			//in this case, the token *could* fit in a row,
			//but it won't fit in the remaining space of this one
			//so we finish the row off and start a new one.
			//note that everytime we start a new row, we indent four spaces.
			} else {
		
				rows.add(row);
				//in any case...rows.add(row);
				row = "    ";
				remaining = WIDTH - METRICS.stringWidth(row);
			}		
		}


		//this is a bit dirty
		//
		if (!row.equals("    "))
			rows.add(row);


		return rows.toArray(new String[rows.size()]);
	}

	public Iterator<String> iterator() {
		return new Iterator<String>() {
			private TextNode cur = null;

			public boolean hasNext() {
				return ( cur == null && head != null ) || ( cur != null && cur.next != null );
			}

			public String next() throws NoSuchElementException {
				
				if ( cur == null && head != null)
					cur = head;

				else if ( cur.next != null )
					cur = cur.next;

				else
					throw new NoSuchElementException( "You should have called hasNext()" );

				return cur.text;
			}
		
			public void remove() throws UnsupportedOperationException {
				throw new UnsupportedOperationException( "remove() not supported" );
			}
		};
	}

	private class TextNode {
		private String text;

		private TextNode next = null;

		//keeping track of previous means
		//the last node always has a pointer
		//to the next-to-last node
		//so when BUF_SIZE is exceeded,
		//it is trivial to set next-to-last to last
		//and drop the last node from the list.
		private TextNode prev = null;
 
		private TextNode( String text ) {
			this.text = text;
		}
	}
}