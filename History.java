public class History {


	/**
	 * Buffer to hold the last n history items
	 */
	private String[] buffer;

	/**
	 * THis is basically the index of buffer last shown to the user
	 */
	private int idx;


	public History() {

		//default to ten items.
		this(10);
	}

	public History(int size) {
		buffer = new String[size];
		reset();
	}

	public void reset() {
		idx = buffer.length;
	}

	public void add(String line) {
		reset();

		/**
		 * Don't add empty lines to the stack
		 */
		if (line.length() == 0)
			return;

		/**
		 * Move everything in the stack up 1 index,
		 * dropping the first item
		 */
		for ( int i = 1; i < buffer.length; i++)
			buffer[i-1] = buffer[i];

		//Append the last item to the bottom fo the stack.
		buffer[buffer.length-1] = line;

	}
	
	public String up() {
		return doHistory(-1);
	}
	public String down() {
		return doHistory(1);
	}


	private String doHistory(int dir) {

		int newIdx = idx + dir;
		String prompt = "";


		if (newIdx >= buffer.length) {
			newIdx = buffer.length;
		}
	
		else if (newIdx <= 0) {
			newIdx = 0;
			prompt = buffer[0];
		}
		else 	
			prompt = buffer[newIdx];

		/**
		 * don't go down to unfilled entries.
		 * This doesn't matter after > buffer.length
		 * commands are entered.
		 */
		if (dir == -1 && buffer[newIdx] == null)
			newIdx++;

		
		//update the index.
		idx = newIdx;

		//return the item
		return prompt;
	}


}
