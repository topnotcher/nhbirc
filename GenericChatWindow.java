import java.awt.BorderLayout;

class GenericChatWindow extends ChatWindowAbstract {
	public GenericChatWindow(String name, ChatWindow.Type type) {
		super(name,type);
		
		setLayout( new BorderLayout() );

		//Just one giant console
		add( console, BorderLayout.CENTER );
	}
}
