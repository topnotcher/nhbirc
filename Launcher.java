
import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * MAIN class - runs the program.
 */
public class Launcher {

	/**
	 * Runs the program as a "standalone" application
	 */
	public static void main(String[] argv) {
		new Application();
	}

	
	/** 
	 * Sets up a frame to run as a standalone application
	 */
	public static class Application extends JFrame {{

		setSize(1000,800);

		setTitle("NHBiRC");

		setVisible(true);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		new Client(this);
	}}

	/**
	 * Sets up an applet to run the application
	 * does not actually work due to permissions :D
	 */
	public static class Applet extends java.applet.Applet {
		public void init() {
			new Client(this);
		}
	}

	//END stuff that is not common to applet/standalone environments
	//begin common stuff.

}
