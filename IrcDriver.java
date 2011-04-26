class IrcDriver {
	public static void main(String argv[]) {
		System.out.println("Starting...");

		Irc irc = new Irc("irc.jaundies.com", 6667, "fubar");

		System.out.println("Created new Irc object...");

		try {
			irc.connect();

			System.out.println("Connected");

//			while(true) try { Thread.sleep(500); } catch (Exception e) { }

		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
