import java.text.Format;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Creates a PaintableText preceeded by a timestamp.
 * I believe this suffices for the non-trivial inheritance...
 */
class TimestampedMessage extends PaintableMessage {

	//timestamp formatter...
	private static Format formatter;

	static {
		//init the formatter
		formatter = new SimpleDateFormat("HH:mm:ss  ");
	}

	/**
	 * Create a timestamped message consisting of just a string
	 * 
	 * @param text string to timestamp.
	 */
	public TimestampedMessage(String text) {
		this(new PaintableString(text));
	}

	/**
	 * Create a timestamped message consisting of a PaintableText
	 * 
	 * This actually makes for some very interesting/confusing stuff.
	 * The text passed to this message could itself be a PaintableMessage (kind of recursive),
	 * which makes for some really cool indenting features
	 *
	 * For example: new TimestampedMessage( new QueryMessage() )
	 * The query message specifies an indent past the nickname in the message,
	 * while this timestamped message specifies an indent past the timestamp.
	 * The indent of this message as whole is the indent of this message
	 * added to the maximum indent of the submessages. Effictively, this means
	 * a query indent will indent past the timestamp and past the nickname, giving a
	 * nice columnized effect. (Sounds more simple when I write it out...)
	 *
	 * @param text PaintableText to timestamp.
	 */
	public TimestampedMessage(PaintableText text) {
		String date = formatter.format(new Date());

		//always indent past the date...
		indent(date.length());
		append( date,  Color.darkGray);
		append(text);
	}
}
