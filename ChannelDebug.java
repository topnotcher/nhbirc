
import com.coldsteelstudios.irc.client.*;

public class ChannelDebug extends ChannelAdapter {

	public void topicChanged(Channel c) {
		System.out.println("Topic changed on " + c.getName());
	}

	public void usersChanged(Channel c) {
		System.out.println("Users changed on " + c.getName());

	}

	public void part(Channel c, User u) {
		System.out.println(u.getNick() + " parted " + c.getName());

	}

	public void join(Channel c, User u) {
		System.out.println(u.getNick() + " joined " + c.getName());
	}

	public void kick(Channel c, User u) {
		System.out.println(u.getNick() + " kicked from " + c.getName());
	}

	public void nick(Channel c, User u, String oldnick) {
		System.out.println(oldnick + " on channel " + c.getName() + " changed nick to " + u.getNick());
	}
}
