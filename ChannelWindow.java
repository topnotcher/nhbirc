import javax.swing.JSplitPane;
import javax.swing.JList;
import java.awt.BorderLayout;

class ChannelWindow extends ChatWindowAbstract {
	
	public ChannelWindow(String channel) {
		super(channel, ChannelWindow.Type.CHANNEL);

		setLayout(new BorderLayout());

		//a channel window is a split pane...
		JSplitPane chan = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT ) ;

		//put the console on the left...
		chan.setLeftComponent( console );

		String[] test = {"User1", "user2", "user3","reallylongname"};

		//the user list on the right.
		chan.setRightComponent(new JList(test));

		//favor the left side???
		chan.setResizeWeight(1.0);


		add(chan,BorderLayout.CENTER);

		//@TODO topic
		add(new javax.swing.JTextField(), BorderLayout.NORTH);
	}

}
