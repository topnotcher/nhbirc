import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

class PaintableMessage implements PaintableText {

	private List<PaintableString> strings;

	public PaintableMessage() {
		strings = new util.LinkedList<PaintableString>();
	}

	public PaintableMessage append(String text, Color color) {
		strings.add(new PaintableString(text,color));
		return this;
	}
	public PaintableMessage append(String text) {
		return append(text,null);
	}

	public String getText() {
		String text = "";

		for ( PaintableString string : strings )
			text += string.getText();

		return text;
	}

	public int paint(Graphics g) {

		int x = 0;
		int width = 0;
		int height = g.getFontMetrics().getHeight();

		for ( PaintableString string : strings ) {
			width = g.getFontMetrics().stringWidth(string.getText());

			x += string.paint( g.create(x, 0, width, height) );
	
		}

		return x;
	}

	public int  paint(Graphics g, int begin, int end) {

		int idx = 0;
		int x = 0;
		int width;
		int height = g.getFontMetrics().getHeight();

		for ( PaintableString string : strings ) {
			int len = string.getText().length();

			//we need to seek.
			if (idx + len < begin ) {
				idx += len;
				continue;
			}

			//we know that idx + length() >= begin.
			int first = 0;
			int last = len;

			if ( idx < begin )
				first = begin-idx;

			if ( idx + len > end )
				last = end - idx;

			width = g.getFontMetrics().stringWidth(string.getText().substring(first,last));


			x += string.paint( g.create(x, 0, width, height) , first, last);
					

			idx += len;

			if ( idx >= end ) break;
		}

		return x;
	}
}
