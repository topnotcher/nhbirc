package com.coldsteelstudios.irc;

class Driver {
	public static void main(String argv[]) {
		System.out.println("Starting...");

		Connection irc = new Connection("irc.jaundies.com", 6667, "fubar");

		System.out.println("Created new Irc object...");

		try {
			irc.connect();

			System.out.println("Connected");

		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
