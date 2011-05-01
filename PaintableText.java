import java.awt.Graphics;
	
interface PaintableText {

	public int paint(Graphics g);

	public int paint(Graphics g, int start, int end);

	public String getText();

	public String getIndent();
}
