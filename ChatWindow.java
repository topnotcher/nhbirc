import java.awt.event.ActionListener;
import java.awt.Component;

interface ChatWindow {

	/**
	 * This is, in a sense, anti-inheritance:
	 * the chat window is limited to being one of these 'types'
	 */
	public static enum Type { STATUS, CHANNEL, QUERY };

	/**
	 * set the "name" of the chat window
	 * 
	 * @param name The name of the chat window.
	 */
	public void setName(String name);

	/**
	 * Get the name of the chat window
	 *
	 * @return the name of the window.
	 */
	public String getName();
	
	/**
	 * Set the type of window
	 *
	 * @param t Type.STATUS, Type.CHANNEL, Type.QUERY
	 */
	public void setType(Type t);

	/**
	 * Get the type of the chat window.
	 *
	 * @return The type of window. @see ChatWindow.Type
	 */
	public Type getType();

	/**
	 * "append" a string to the window (whatever that means)
	 *
	 * @param msg string to append.
	 */
	public void put(String msg);


	/**
	 * Append a paintable text object onto the window.
	 *
	 * @param msg a PaintableText object to add to the window.
	 */
	public void put(PaintableText msg);

	/**
	 * Add an action(command) listener to the ChatWindow.
	 *
	 * @param e ActionListener to add.
	 */
	public void addActionListener(ActionListener e);

	/**
	 * Remove an action(command) listener from the window.
	 *
	 * @param e ActionListener to remove.
	 */
	public void removeActionListener(ActionListener e);


	/**
	 * Get the java.awt.component to draw the content of this window.
	 *
	 * @return Something suitable for component.add(...);
	 */
	public Component getContentPane();
}
