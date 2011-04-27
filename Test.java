class Test {
	public static void main(String args[]) {
		IrcMessage msg = new IrcMessage("PING: foo");

		System.out.println (msg.getCommand());
	}
}
