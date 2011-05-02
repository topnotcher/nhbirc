
import java.awt.Font;
import java.awt.Graphics;
import java.awt.FontMetrics;
import javax.swing.JComponent;

import java.util.List;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import util.FixedStack;

/**
 * A component that displays word-wrapped lines of text.
 * 
 * @TODO Convert this to a Document,View, JTextComponent, so it actually "acts like" text.
 */
class WrappedTextComponent extends JComponent {

	/**
	 * Number of lines of text to store.
	 * @TODO configurable
	 */
	private static final int BUF_SIZE = 150;

	//two pixesls between lines, on sides, from top/bottom.
	private final int PAD = 2;

	private List<PaintableText> stack = new FixedStack<PaintableText>(BUF_SIZE);

	public WrappedTextComponent() { 
	}

	public void append( String text ) {
		append( new PaintableString( text ) );
	}

	/**
	 * Append a line of text to the component
	 */
	public synchronized void append( PaintableText text ) {
		stack.add(text);
		repaint();
	}

	public void paint(Graphics g) {

		final FontMetrics METRICS = getFontMetrics(getFont());
		final int FONTHEIGHT = METRICS.getHeight() + PAD;

		final int HEIGHT = getHeight() - PAD;
		final int WIDTH = getWidth() - 2*PAD;

		final int ROWS = (int)(HEIGHT/FONTHEIGHT);

		final int INDENT = METRICS.stringWidth(" ");

		int indent;

		int remaining = ROWS;
		Iterator<PaintableText> lines = stack.iterator();

		while ( lines.hasNext() && remaining > 0 ) {

			PaintableText text = lines.next();
			String line = text.getText();
			indent = text.getIndent();

			Integer[] rows = getRows( line, indent ); 
					
			for (int i = rows.length - 1, offset = line.length(); i >= 0 && remaining > 0; offset -= rows[ i ], --i, --remaining ) {

				text.paint( g.create(
						PAD + ((i == 0)  ? 0 : indent*INDENT ), 
						(remaining)*FONTHEIGHT-METRICS.getHeight(), 
						WIDTH, 
						METRICS.getHeight()
					) , offset-rows[i], offset
				);

			}
		}
	}

	/**
	 * @TODO find a better way to organize this method.
	 * Especially find a better way to handle the four space indent
	 */
	private Integer[] getRows( String line, int indent ) {

		final FontMetrics METRICS = getFontMetrics(getFont());

		final int WIDTH = getWidth() - 2*PAD;
		final int indentsize = METRICS.stringWidth(" ")*indent;

		//hold the output in this...
		List<Integer> rows = new util.LinkedList<Integer>();

		//number of chars for current row.
		int row = 0;

		int remaining = WIDTH;

		//I use " " as an indent
		//this is a cheap hack to fix an infinite loop
		//if the window is too small
		if ( METRICS.stringWidth(" ")*indent > WIDTH )
			return new Integer[0];

		//first, we attempt to split the thinggy at spaces...
		StringTokenizer st = new StringTokenizer(line, " ",true); 

		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			int width = METRICS.stringWidth(tok);
			
			//if the current token will fit in the current row
			if ( width <= remaining ) {
				row += tok.length();
				remaining -= width;

			//in this case, the current token wn't fit in a row PERIOD,
			//so it definitely *has* to be split up.
			} else if ( width > WIDTH-indentsize ) {
	
				//now we're (lol) going to go through the same process
				//as getRows(), but where each token is a character!
				for (int i = 0; i < tok.length(); ++i) {

					char chr = tok.charAt(i);
					width = METRICS.charWidth(chr);

					if  ( width <= remaining ) {
						remaining -= width;
						row += 1;
					} else {
						rows.add(row);
						row = 1;
						remaining = WIDTH - indentsize - width;
					}
				}
			
			//in this case, the token *could* fit in a row,
			//but it won't fit in the remaining space of this one
			//so we finish the row off and start a new one.
			//note that everytime we start a new row, we indent four spaces.
			} else {
				rows.add(row);
				//in any case...rows.add(row);
				row = tok.length();
				remaining = WIDTH - indentsize - width;
			}
		}

		if (row != 0)
			rows.add(row);

		return rows.toArray(new Integer[rows.size()]);
	}
}
