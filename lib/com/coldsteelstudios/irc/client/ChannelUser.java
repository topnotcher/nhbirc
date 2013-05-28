package com.coldsteelstudios.irc.client;

/**
 * A user is just a user...
 *
 * A user on a channel can have modes (well the modes are really the channel's)
 * This object ties a user to a set of modes.
 */
public class ChannelUser implements Comparable<ChannelUser> {

	/**
	 * Mapping between mode chars, access level.
	 */
	public enum Mode {

		FOUNDER	(0,'~'),
		ADMIN	(1,'&'),
		OP		(2,'@'),
		HOP		(3,'%'),
		VOICE	(4,'+'),
		NONE	(5,' ');
		
		private int value;
		private char id;

		Mode(int m,char id) {
			this.value = m;
			this.id = id;
		}
		
		public static Mode getMode(char id) {
			for (Mode mode : Mode.values())
				if (mode.id == id)
					return mode;

			return Mode.NONE;
		}
	}

	private User user;

	private Mode mode;

	public ChannelUser(User user) {
		this.user = user;
		this.mode = Mode.NONE;
	}

	public User getUser() {
		return this.user;
	}

	public void setMode(Mode m) {
		this.mode = m;
	}

	public String toString() {
		return Character.toString(mode.id) + " " + user.getNick();
	}

	/**
	 * @TODO: take mode into account.
	 */
	public int compareTo(ChannelUser u) {
		
		int diff = this.mode.value - u.mode.value;

		if (diff != 0) return diff;


		return this.user.compareTo( u.user );	
	}

	public boolean equals(User u) {
		return this.user.equals(u);
	}

	public boolean equals(ChannelUser u) {
		return u.user.equals(user);
	}
}
