
import java.util.Map;
import java.util.HashMap;

public class UserSyncManager {

	protected SyncManager sync;


	//registry of all user objects.
	private static Map<String,User> users = new HashMap<String,User>();

	public UserSyncManager(SyncManager sync) {
		this.sync = sync;
	}

	public synchronized User get(String nick) {
		User ret = users.get(nick);

		if (ret == null) {
			ret = new User(nick);
			users.put(ret);
		}
			

		return ret;
	}

	public void put(String nick, User user) {
		synchronized(users) {
			users.put(nick,user);
		}
	}

	public void remove() {

	}

	public User get(String nick) {

	}
	
}
