import java.util.ListIterator;
import java.util.List;
import util.FixedStack;

/**
 * Holds a fixed number of commands in the "history buffer"
 * when adding a command causes the buffer to exceed the maximum size,
 * the command that has been in the buffer for longest is dropped.
 */
public class History {

	/**
	 * Buffer to hold the last n history items
	 */
	private List<String> buffer;

	/**
	 * THis is basically the index of buffer last shown to the user
	 */
	private ListIterator<String> it;

	/**
	 * Create a new history object with a size of 10 commands
	 */
	public History() {
		//default to ten items.
		this(10);
	}

	/**
	 * @param size Number of commands to store
	 */
	public History(int size) {
		buffer = new FixedStack<String>(size);
		reset();
	}

	/**
	 * Remove all items from history
	 */
	public void clear() {
		buffer.clear();
		reset();
	}

	/**
	 * Resets the "internal pointer" to the first item.
	 */
	public void reset() {
		it = buffer.listIterator();
	}

	/**
	 * Add a line to the history.
	 *
	 * @param line the command to add to history.
	 */
	public void add(String line) {

		//reset();

		/**
		 * Don't add empty lines to the stack
		 */
		if (line.length() == 0)
			return;


		buffer.add(line);
	}
	
	/**
	 * Move up in history.
	 * 
	 * If there are no more items, it returns the last item returned, or "" if there are no items period.
	 */
	public String up() {
		return it.hasNext() ? it.next() : (it.hasPrevious() ? it.previous() : "");
	}

	/**
	 * Move down in history.
	 * 
	 * If there are no more items, it returns the last item returned, or "" if there are no items period.
	 */
	public String down() {
		return it.hasPrevious() ? it.previous() : (it.hasNext() ? it.next() : "");
	}
}
