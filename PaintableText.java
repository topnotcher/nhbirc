import java.awt.Graphics;
	
/**
 * Encapsulates a string that can be painted in WrappableTextComponent.
 */
interface PaintableText {

	/**
	 * Paint the whole text on the Graphics context.
	 *
	 * Note that this method assumes it has been given its own grahpics context.
	 * That is: 0,0 should be the TOP LEFT of where this string will be painted.
	 *
	 * @return width of the painted string.
	 */
	public int paint(Graphics g);

	/**
	 * Paint a substring of this PaintableText
	 * @see paint(Graphics)
	 *
	 * @return width of the painted string
	 */
	public int paint(Graphics g, int start, int end);

	/**
	 * The string stored by this PaintableText
	 */
	public String getText();

	/**
	 * If this next needs to be wrapped,
	 * lines after the first will be indented by this many spaces.
	 *
	 * @return number of spaces to indent
	 */
	public int getIndent();
}
