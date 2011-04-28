import javax.swing.JPanel;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


abstract class ChatWindowAbstract extends JPanel implements ActionListener, ChatWindow {
	
	private List<ActionListener> listeners = null;

	private String name = "";

	protected GUIConsole console;

	protected ChatWindow.Type type;

	public ChatWindowAbstract() {
		this( "", ChatWindow.Type.STATUS );
	}

	public ChatWindowAbstract(String name, ChatWindow.Type type) {
		setName(name);
		setType(type);
		console = new GUIConsole(name);
		console.addActionListener(this);
	}

	/**
	 * Begin ChatWindow
	 */

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
	
	public void setType(ChatWindow.Type type) {
		this.type = type;
	}

	public ChatWindow.Type getType() {
		return this.type;
	}

	public void put(String msg) {
		console.out().println(msg);
	}

	public java.awt.Component getContentPane() {
		return this;
	}

	public void addActionListener(ActionListener l) {

		//prevent null pointers later on...
		if (l == null)
			throw new NullPointerException("ActionListeners should NOT be null");

		//lazily instantiate list. 
		if (listeners == null) 
			listeners = new util.LinkedList<ActionListener>();
	

		listeners.add(l);
	}

	public void actionPerformed(ActionEvent e) {
		
		//if no one is listening..
		if ( listeners == null )
			return;


		//NEW event...
		ActionEvent event = new ActionEvent( this, 0, e.getActionCommand() );

		for (ActionListener l : listeners) 
			l.actionPerformed(event);

	}
}
