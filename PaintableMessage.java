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

	public String substring( int begin, int end) {

		int idx = 0;
		int x = 0;
		String p = "";

		for ( PaintableString string : strings ) {
				
			//we need to seek.
			if (idx + string.getText().length() < begin ) 
				continue;

			//we know that idx + length() >= begin.
			int first = 0;
			int last = string.getText().length();

			if ( idx < begin )
				first = begin-idx;

			if ( idx + string.getText().length() > end )
				last = end - idx;

			System.out.println("substr " + first + ", " + last);
			p += string.getText().substring(first,last);


			idx += string.getText().length();				
			if ( idx >= end ) break;
		}

		return p;
	}

	public int  paint(Graphics g, int begin, int end) {

		int idx = 0;
		int x = 0;
		int width;
		int height = g.getFontMetrics().getHeight();

		for ( PaintableString string : strings ) {
				
			//we need to seek.
			if (idx + string.getText().length() < begin ) 
				continue;

			//we know that idx + length() >= begin.
			int first = 0;
			int last = string.getText().length();

			if ( idx < begin )
				first = begin-idx;

			if ( idx + string.getText().length() > end )
				last = end - idx;

			width = g.getFontMetrics().stringWidth(string.getText().substring(first,last));


			x += string.paint( g.create(x, 0, width, height) , first, last);
					
			if ( idx > end ) break;
		}

		return x;
	}
}
