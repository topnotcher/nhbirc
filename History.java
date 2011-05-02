import java.util.ListIterator;
import java.util.List;
import util.FixedStack;

public class History {

	/**
	 * Buffer to hold the last n history items
	 */
	private List<String> buffer;

	/**
	 * THis is basically the index of buffer last shown to the user
	 */
	private ListIterator<String> it;

	public History() {
		//default to ten items.
		this(10);
	}

	public History(int size) {
		buffer = new FixedStack<String>(size);
		reset();
	}

	public void clear() {
		buffer.clear();
		reset();
	}

	public void reset() {
		it = buffer.listIterator();
	}

	public void add(String line) {

		//reset();

		/**
		 * Don't add empty lines to the stack
		 */
		if (line.length() == 0)
			return;


		buffer.add(line);
	}
	
	public String up() {
		return it.hasNext() ? it.next() : "";
	}

	public String down() {
		return it.hasPrevious() ? it.previous() : "";
	}
}
