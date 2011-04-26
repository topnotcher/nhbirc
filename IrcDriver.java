class IrcDriver {
	public static void main(String argv[]) {
		Irc irc = new Irc("irc.jaundies.com", 6667, "fubar");

		irc.connect();
	}
}
