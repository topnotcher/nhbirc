import javax.swing.JSplitPane;
import javax.swing.JList;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import javax.swing.ListModel;
import javax.swing.AbstractListModel;
import javax.swing.event.ListDataListener;
import javax.swing.JTextField;
import java.awt.Color;

import client.*;

public class ChannelWindow extends ChatWindowAbstract {
	
	private client.Channel channel;
	private ChannelListModel list;
	private JTextField topic;

	public ChannelWindow(String channel_name, client.SyncManager sync) {
		super(channel_name, ChannelWindow.Type.CHANNEL);

		channel = sync.getChannel(getName());

		list = new ChannelListModel();	

		channel.addChannelListener( channelListener );



		setLayout(new BorderLayout());

		//a channel window is a split pane...
		JSplitPane chan = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT ) ;

		//put the console on the left...
		chan.setLeftComponent( console );

		//the user list on the right.
		
		JList userlist = new JList(list);
		
		userlist.setBackground(Color.black);
		userlist.setForeground(Color.white);

		userlist.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 13));

		chan.setRightComponent( new JScrollPane( userlist ) );


		//favor the left side???
		chan.setResizeWeight(1.0);


		add(chan,BorderLayout.CENTER);

		//@TODO topic
		add(topic = new JTextField(channel.getTopic()), BorderLayout.NORTH);
	}

	protected void finalize() {
		System.out.println("Finalize channel window");
		channel.removeChannelListener( channelListener );
	}

	private class ChannelListModel extends AbstractListModel {
		public int getSize() {
			return channel.numUsers();
		}
		public Object getElementAt(int idx) {
			return channel.getUser(idx);
		}
		private void update() {
			fireContentsChanged( this, 0, channel.numUsers() - 1);

		}
	};

	public ChannelListener channelListener = new ChannelAdapter() {
		public void topicChanged(Channel c) {
			topic.setText(c.getTopic());
		}

		public void usersChanged(Channel c) {
			list.update();

		}
	};
}
