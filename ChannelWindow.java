import javax.swing.JSplitPane;
import javax.swing.JList;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import javax.swing.ListModel;
import javax.swing.AbstractListModel;
import javax.swing.event.ListDataListener;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import client.*;


/**
 * ChannelWindow differs from GenericChatWindow in that it
 * requires a nick list to be displayed. It uses the same console, but 
 * it's content is stored in a split pane.
 */
public class ChannelWindow extends ChatWindowAbstract {
	
	/**
	 * List model to abstract getting the user
	 * list from the channel and into the JList
	 */
	private ChannelListModel list;

	private Channel channel;

	/**
	 * Dislpays the topic...
	 */
	private JTextField topic;

	public ChannelWindow(String channel_name, client.SyncManager sync) {
		super(channel_name, ChannelWindow.Type.CHANNEL);

		//note that the sync manager will create the channel
		//on the fly if it doesn't exist
		//the methods are synchronized, so when the syncmanager gets the
		//JOIN for the channel, it will pull back the same channel instance.
		//This means that the order in which the message handlers are run is irrelevant
		channel = sync.getChannel(getName());
		
		channel.addChannelListener( channelListener );

		list = new ChannelListModel();	
	
		//layout for the ChannelWindowAbstract
		setLayout(new BorderLayout());

		//a channel window is a split pane...
		JSplitPane chan = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT ) ;

		//put the console on the left...
		chan.setLeftComponent( console );

		//the user list on the right.
		
		JList userlist = new JList(list);
		
		/**
		 * @TODO don't hardcode this...
		 */
		userlist.setBackground(Color.black);
		userlist.setForeground(Color.white);

		/**
		 * @TODO don't hardcode this either.
		 */
		userlist.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 13));

	
		/**
		 * Make the user list scrollable.
		 */
		JScrollPane scroll = new JScrollPane(userlist);
		scroll.setMinimumSize( new Dimension(200,100) );
		chan.setRightComponent( scroll );


		//favor the left side???
		chan.setResizeWeight(1.0);

		add(chan,BorderLayout.CENTER);

		//@TODO topic
		add(topic = new JTextField(channel.getTopic()), BorderLayout.NORTH);

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

	private ChannelListener channelListener = new ChannelAdapter() {
		public void topicChanged(Channel c) {
			topic.setText(c.getTopic());
		}

		public void usersChanged(Channel c) {
			list.update();
		}
	};
}
