import java.text.Format;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

class TimestampedMessage extends PaintableMessage {
	
	private static Format formatter;

	static {
		formatter = new SimpleDateFormat("HH:mm:ss  ");
	}

	public TimestampedMessage(String text) {
		this(new PaintableString(text));
	}

	public TimestampedMessage(PaintableText text) {
		append( formatter.format(new Date()),  Color.darkGray);
		append(text);
	}
}
