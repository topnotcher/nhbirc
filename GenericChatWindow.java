import java.awt.BorderLayout;

/**
 * Generic chat window, suitable for use as a PM or Status window
 */
class GenericChatWindow extends ChatWindowAbstract {
	public GenericChatWindow(String name, ChatWindow.Type type) {
		super(name,type);
		
		setLayout( new BorderLayout() );

		//Just one giant console
		add( console, BorderLayout.CENTER );
	}
}
