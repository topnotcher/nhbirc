package client;

import java.util.List;

//reflection used to handle ActionEvents.
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;


public class Channel {


	private String name;
	private String topic;
	private List<User> users;

	private List<ChannelListener> subs;

	public Channel(String name) {
		this.name = name;

		//pre-instantiate this as it should always contain
		//at least one element: the current client...
		users = new util.LinkedList<User>();
	}

	private void addUserToList(User user) {
	
		//very ugly.
		for (User u : users) 
			if (u.equals(user)) {
				System.out.println("Trying to add equals user");

				if (user != u)
					System.out.println("THey are not the same object.");
				return;
			}
		
		users.add(user);
	}

	void addUsers(List<User> list) {

		//we need to do a contains....
		for (User user : list) 
			addUserToList(user);

		util.ListSorter.sort( users );
	}

	void usersChanged() {
		notifyListeners("usersChanged");
	}

	void addUser(User u) {

		addUserToList( u );

		util.ListSorter.sort(users);

		notifyListeners("usersChanged");
	}

	void delUser(User user) {
		boolean change = true;

		users.remove(user);
/*		for (User u : users) {
			if ( u.equals(user) )
				users.remove(u);
		}*/
		

		if (change) notifyListeners("usersChanged");
	}

	public String getTopic() {

		if (topic == null) return "";

		return topic;
	}

	public String getName() {
		return name;
	}

	public int numUsers() {
		return users.size();
	}

	public User getUser(int idx) {
		return users.get(idx);
	}

	public boolean equals(Channel c) {
		return c.name.equals(name);
	}

	private void notifyListeners(String event) {
		
		if ( subs == null ) return;

		Class[] paramtypes = {this.getClass()};
		Object[] params = {this};

		System.out.println("FIRE EVENT: "+ event);

		for (ChannelListener l : subs) {
			try {
				//FIND the method (or exception if not defined)
				Method command = Class.forName("client.ChannelListener").getDeclaredMethod(event,paramtypes);
		
				//invoke the method
				command.invoke(l,params);
			} catch (NoSuchMethodException e) {
				//won't happen.
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		}

		//method invoked, but threw exception
	//	} catch (InvocationTargetException err) {

	//	}
	}


	public void addChannelListener(ChannelListener c) {
		if (subs == null)
			subs = new util.LinkedList<ChannelListener>();

		subs.add(c);
	}
}
