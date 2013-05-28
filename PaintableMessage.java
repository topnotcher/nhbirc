import java.awt.Color;
import java.awt.Graphics;
import java.util.List;
import java.util.LinkedList;

import com.coldsteelstudios.util.FixedStack;

/**
 * A Concrete implementation of PaintableText that
 * abstracts painting a list of PaintableTexts as a single PaintableText
 * This is especially useful when a single "Message" must consist of
 * multiple pieces painted in different colors.
 * In this case, each colored section would be stored as a PaintableText in 
 * the PaintableMessage's list...
 *
 * This class abstracts some otherwise fairly complicated color changes/indents...
 */
class PaintableMessage implements PaintableText {

	/**
	 * List of PaintableTexts that comprise this message.
	 */
	private List<PaintableText> strings;

	/**
	 * Number of spaces to indent if this message is wrapped
	 */
	private int indent = 4;

	/**
	 * The maximum indent of the PaintableTexts in strings.
	 */
	private int	maxIndent = 0;

	/**
	 * Create an empty message
	 */
	public PaintableMessage() {
		strings = new LinkedList<PaintableText>();
	}
	
	/**
	 * Append a PaintableText to the message
	 *
	 * @param text a PaintableText to append
	 *
	 * @return provides a fluent interface.
	 */
	public PaintableMessage append(PaintableText text) {
		if (text.getIndent() > maxIndent)
			maxIndent = text.getIndent();

		strings.add(text);
		return this;
	}

	/**
	 * Append a PaintableText to the message as a colored string.
	 *
	 * @param text a String to append
	 * @param color color to use when painting this text
	 *
	 * @return provides a fluent interface.
	 */
	public PaintableMessage append(String text, Color color) {
		append(new PaintableString(text,color));
		return this;
	}

	/**
	 * Append a string to this message. Use the default color.
	 * 
	 * @param text string to append.
	 * 
	 * @return provides a fluent interface.
	 */
	public PaintableMessage append(String text) {
		return append(text,null);
	}

	/**
	 * Set the indentation size of this message
	 * 
	 * @param indent the new indentation size
	 * 
	 * @return provides a fluent interface
	 */
	public PaintableMessage indent(int indent) {
		this.indent = indent;
		return this;
	}

	/**
	 * Return the # of spaces to use when indenting wrapped lines of this message
	 *
	 * @return This PaintableText's indent + the indent of the largest subtext.
	 */
	public int getIndent() {
		return maxIndent+indent;
	}

	/**
	 * Return the string represented by this object.
	 *
	 * @return concatenation of all "subtexts" stored in the object.
	 */
	public String getText() {
		String text = "";

		for ( PaintableText string : strings )
			text += string.getText();

		return text;
	}

	/**
	 * @override
	 */
	public int paint(Graphics g) {

		//total width painted
		int x = 0;

		//width of a substring being painted.
		int width = 0;

		//height of the current font.
		int height = g.getFontMetrics().getHeight();

		for ( PaintableText string : strings ) {
			width = g.getFontMetrics().stringWidth(string.getText());

			x += string.paint( g.create(x, 0, width, height) );
	
		}

		return x;
	}

	/**
	 * @override
	 *
	 * @param begin the ABSOLUTE index to start painting at. Absolute meaning the index
	 * that would be used in a call to getText().substring(...)
	 * @param end likewise...
	 */
	public int  paint(Graphics g, int begin, int end) {

		//the absolute starting index of the current block in the
		//whole message
		int idx = 0;

		//# of pixels wide painted
		int x = 0;
		
		//width of a block
		int width;

		//height of this font.
		int height = g.getFontMetrics().getHeight();

		//for each subtext...
		for ( PaintableText string : strings ) {
			int len = string.getText().length();

			/**
			 * important note to make here:
			 * The calling context doesn't realize that this isn't a single string,
			 * but rather a list of strings.
			 * so a call to paint(g,x,y) is especially difficult because
			 * x and y are in some subtext in a list.
			 * We need to seek to the appropriate place to start taking substrings
			 * then take the appropriate substring from each block until reaching end...
			 */
			if (idx + len < begin ) {
				idx += len;
				continue;
			}

			//we know that idx + length() >= begin.
			//Where to start taking a substring from this block
			int first = 0;

			//and where to stop taking the substring
			int last = len;

			//this means that we need to start painting
			//part way through the current block
			if ( idx < begin )
				//this is where to start
				first = begin-idx;

			//need to STOP painting partway through the current block.
			if ( idx + len > end )
				last = end - idx;

			//The width (pixels) of thes tring being painted
			//used to create a graphics context....
			width = g.getFontMetrics().stringWidth(string.getText().substring(first,last));

			//paint the string on a new graphcis context, and
			//add the width of the painted string to the total width painted.
			x += string.paint( g.create(x, 0, width, height) , first, last);
					

			//advance to the next block.
			idx += len;

			//if the next block brings us out of the substring
			//requested for painting, then we're done...
			if ( idx >= end ) break;
		}

		//and return the total width painted!
		return x;
	}
}
