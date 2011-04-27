class Test {
	public static void main(String args[]) {

		String[] msgs = {
			":spree.jaundies.com NOTICE AUTH :*** No ident response; username prefixed with ~",
			":spree.jaundies.com 433 * mario :Nickname is already in use.",
			":spree.jaundies.com 372 fubar :- of the Network will be deemed as irrevocable acceptance of any revisions.",
			":fubar!~fubar@33AFAADF.7A30E887.CC04845D.IP JOIN :#fooooo",
			"PING :foo",
			":trogdor.jaundies.com 353 fubar = #fooooo :@fubar",
			":trogdor.jaundies.com 366 fubar #fooooo :End of /NAMES list.",
			":mario!topnotcher@13.37 PRIVMSG #fooooooooooo :This is a test message"
		};

		for (String raw : msgs)  {
			System.out.println("");
			new IrcMessage(raw);
		}

//		System.out.println (msg.getCommand());
	}
}
