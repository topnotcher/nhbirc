package com.coldsteelstudios.irc;

public enum Priority {
	LOW(3),
	MEDIUM(2),
	HIGH(1),
	CRITICAL(0);

	private int value;

	Priority(int n) {
		value = n;
	}

	public int getValue() {
		return value;
	}
}
