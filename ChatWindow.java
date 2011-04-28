import java.awt.event.ActionListener;
import java.awt.Component;

interface ChatWindow {

	public static enum Type { STATUS, CHANNEL, QUERY };

	public void setName(String name);

	public String getName();
	
	public void setType(Type t);

	public Type getType();

	public void put(String msg);

	public void addActionListener(ActionListener e);

	public Component getContentPane();
}
