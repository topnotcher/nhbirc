import java.awt.Graphics;
import java.awt.Color;

/**
 * A concrete implementation of PaintableText.
 * Encapsulates printing  a string with (optionally) a color.
 */
class PaintableString implements PaintableText {

	/**
	 * The text encapsulated by this PaintableString
	 */
	private String text;

	/**
	 * Color to paint the text.
	 */
	private Color color;

	/**
	 * Specify a string and a color.
	 *
	 * @param text the text to paint
	 * @param the color to use when painting. NULL to use the default color.
	 * @throws NullPointerException is text is null
	 */
	public PaintableString(String text, Color color) {
		if (text == null) throw new NullPointerException("text cannot be null");
		this.text = text;
		this.color = color;
	}

	/**
	 * Paint using the default color.
	 * @see PaintableString(String, Color)
	 */
	public PaintableString(String text) {
		this(text,null);
	}

	/**
	 * Return the string encapsulated by this object.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Paint the specified substring.
	 *
	 * @param g private graphics context.
	 * @param start start index.
	 * @param end end index
	 * @see String.substring(int, int)
	 * @see draw(Graphics,String)
	 *
	 * @return the width of the painted string.
	 */
	public int paint(Graphics g,int start,int end) {
		return draw( g, text.substring(start, end) );
	}

	/**
	 * Paint the whole string.
	 *
	 * @param g private graphics context.
	 * @return width of painted string
	 * @see draw(Graphics, String)
	 */
	public int paint(Graphics g) {
		return draw(g, text);
	}

	/**
	 * Paint the specified string
	 *
	 * @param g private graphics context.
	 * @return width of painted string.
	 */
	private int  draw(Graphics g, String text) {

		Color def = g.getColor();

		//Note again that the graphics context g was
		//created just to paint this string, so setting the color doesn't 
		//change anything else :).
		if ( color != null ) g.setColor(color);

		//draw string at x = 0
		//the y-coordinate of the drawing is the baseline of the text
		//hence, we need to find the bottom of this graphics context, 
		//then go up by the maxDescent() (# of pixels below the base line of the lowest character)
		g.drawString(text,0, g.getFontMetrics().getHeight() - g.getFontMetrics().getMaxDescent() );

		//return the width of the string painted
		return g.getFontMetrics().stringWidth(text);
	}

	/**
	 * # of pixels to indent if this is split onto multiple liens.
	 * Always returns 0 for this simple implementation of PaintableText
	 *
	 * @return 0.
	 */
	public int getIndent() {
		return 0;
	}
}
