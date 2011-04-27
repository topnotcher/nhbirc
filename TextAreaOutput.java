import java.io.OutputStream;


/**
 * An outputstream that uses a JTextArea to print text
 * to the screen.
 */
public class TextAreaOutput extends OutputStream {
	public TextArea area;

	public TextAreaOutput() {
		area = new TextArea();
	}

	public void write(int b) throws java.io.IOException {
		byte[] bytes = {(byte)b};
		write(bytes);
	}

	public void write(byte[] bytes, int offset, int len) {
		area.append(new String(bytes,offset,len));
	}


	/**
	 * The inner TextArea class is designed to be used ONLY
	 * with the TextArea outputstream.
	 *
	 * This modifies JTextArea by wraping lines and making sure the
	 * number of rows in the TextArea is always equal to the number of
	 * rows the text area can hold at the given size/font.
	 */
	public class TextArea extends javax.swing.JTextArea {

		public TextArea() {
			//don't want the output to be editable...
			setEditable(false);

			//i'll wrap my own damn lines.
			setLineWrap(false);
		}
		
		/**
		 * This splits a given string into an array of lines, breaking
		 * the lines if they are > cols characters wide.  I could not use
		 * the  line wrapping in JTextArea because it would have interfered
		 * with fitting the rows in the text area.
		 *
		 * @param text The text to split into an array.
		 * @param cols The number of columns to  wrap lines at.
		 */
		private String[] getRowArray(String text, int cols) {


			//an array containing the lines in text.
			String[] lines = text.split("\n");

			//a vector to hold the new lines.
			java.util.Vector<String> rows = new java.util.Vector<String>(lines.length);

			//foreach line...
			for (int i = 0; i < lines.length; i++) {

				//if this line fits, add it to the vector.
				if (lines[i].length() <= cols)
					rows.add(lines[i]);

				else {

					//while the line is too long...
					while (lines[i].length() > cols) {

						//cut it down, and add the first part to the vector...
						rows.add(lines[i].substring(0,cols));

						lines[i] = lines[i].substring(cols+1);
					}

					//then add what remains...
					rows.add(lines[i]);
				}

			}

			//then return an array containing the rows...
			return rows.toArray(new String[0]);
		}

		/** 
		 * This method attempts to append the parameter to the text in the textarea,
		 * ensuring that the textarea has exactly as many rows as it can fit, therefore
		 * scrolling text off of the top of the text area when the number of rows printed
		 * exceed's the textarea's capacity.
		 *
		 * TODO: This is not remotely efficient.
		 *
		 * There is a small bug here in that text will not refit when the window is resized.
		 * 	(This happens some of the time when the window maximizes after being 800x600)
		 *
		 * @param text Text to append to the TextArea.
		 */
		public void append(String text) {

			//the height of a row of text in the current font.
			int height = getRowHeight();

			//the height of the TextArea
			int compheight = getHeight();
			
			/**
			 * TODO: cache this. Only changes when font or bounds change.
			 * This gets the number of rows/cols this textarea can hold..
			 */
			int rows = getHeight()/getRowHeight();
			int cols = getWidth()/getColumnWidth();

			//This doesn't really seem to have any effect on anything.
			//I think it's just for show.
			setRows(rows);
			setColumns(cols);

			//The "new content" of the textarea (old+new)
			String[] append = new String[rows];

			//the content being added to the textarea (new)
			String[] newrows = getRowArray(text,cols);

			//these are the rows currently in the textarea.
			//this breaks horribly in resize scenarios.
			String[] currentrows = getText().split("\n");

			//we need to put rows rows in the textbox
			//newrows.length of those are new
			//so reamining is how many we need to keep...
			int remaining = rows - newrows.length;

			//for the number of rows the area can hold...
			for (int i = 0; i < rows; ++i) {
		
				//if there is an OLD row to take.
				if (i+newrows.length < currentrows.length) 
						//add it
						append[i] = currentrows[i+newrows.length];
					else
						//append a blank one...
						append[i] = "";


			}
	
			//add the new text to the textarea bottom up.
			for (int i = 1; i <= newrows.length && i <= rows; i++) 
				append[rows-i] = newrows[newrows.length - i];


			//concatenate the output
			String out = "";
			for (int i = 0; i < rows; i++)
				out = out  + append[i] + "\n";

			//and put it in the textarea
			setText(out);
		}
	}
}
