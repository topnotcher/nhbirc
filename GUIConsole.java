import javax.swing.JPanel;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.SpringLayout;

import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;

/**
 * Contains a WrappableTextComponent, and a labled text field.
 * @TODO Currently proxies actionPerformed to the text field. 
 */
public class GUIConsole extends BufferedPanel {


	/**
	 * The textarea for the output of the console.
	 */
	protected WrappedTextComponent area;

	/**
	 * Textfield for the input of the console.
	 */
	protected JTextField field;


	/**
	 * Maintains command line history
	 */
	private History history = new History(10);

	/**
	 * Construct with a blank name
	 */
	public GUIConsole() {
		this("");
	}

	/**
	 * @param name String to put in the label next to the text field.
	 */
	public GUIConsole( String name ) {

		//main layout is border layout
		//with the output in the CENTER
		//and the input/label in SOUTH
		setLayout(new BorderLayout());
	
		area = new WrappedTextComponent();

		/**
		 * For now, when the output area is clicked,
		 * set focus to the input field.
		 */
		area.addMouseListener( new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				field.requestFocusInWindow();
			}	
		});


		//an InputStream that gathers input from a textfield.
		field = new JTextField();

		/**
		 * Whenever an action is performed (e.g. ENTER),
		 * the text should be cleared from the fie.d.
		 */
		field.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				history.add( e.getActionCommand());
				field.setText("");
			}
		});

		/**
		 * Change the context of the textfield when
		 * up or down is pressed.
		 * 
		 */
		field.addKeyListener( new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_UP:
						field.setText( history.up());
						break;

					case KeyEvent.VK_DOWN:
						field.setText(history.down());
						break;
				}
			}
		});

		/**
		 * @TODO need a better way to handle UI stuff like font/
		 * colors. Currently hardcoded in far too many places.
		 * (one is too many)
		 */

		//Green on black is the ONLY color for a terminal.
		setBackground(Color.black);
		setForeground(Color.green);

		field.setBackground(Color.black);
		field.setForeground(Color.green);

		setFont(new Font("Monospaced", Font.PLAIN, 14));

		this.add( area , BorderLayout.CENTER);

		SpringLayout layout = new SpringLayout() ;
	
		JPanel input = new JPanel( layout );

		javax.swing.JLabel label = new javax.swing.JLabel( name );

		input.add(label);
		input.add(field);

		/**
		 * Sets up the textfield and the label such that:
		 * - both are 5 pixels from the top/bottom.
		 * - label is 5 pixels right of the left side of the window.
		 * - field is 5 pixels right of label
		 * - field is 5 pixels left of the right side of the window.
		 */

		layout.putConstraint(SpringLayout.WEST, label, 5, SpringLayout.WEST, input);
		layout.putConstraint(SpringLayout.NORTH, label, 5, SpringLayout.NORTH, input);

		layout.putConstraint(SpringLayout.WEST, field, 5, SpringLayout.EAST, label);
		layout.putConstraint(SpringLayout.NORTH, field, 5, SpringLayout.NORTH, input);

		layout.putConstraint(SpringLayout.EAST, input, 5, SpringLayout.EAST, field);
		layout.putConstraint(SpringLayout.SOUTH, input, 5, SpringLayout.SOUTH, field);


		this.add(input, BorderLayout.SOUTH);


		field.requestFocusInWindow();
	}
	
	/**
	 * Add an action listener to the text field.
	 * @param l an action listener to receive events from the text field.
	 */
	public void addActionListener(ActionListener l) {
		field.addActionListener(l);
	}

	/**
	 * Get the textfield managed by this GUIConsole.
	 * @return the text field.
	 */
	public JTextField getField() {
		return field;
	}

	/**
	 * Append a line of text to the console.
	 * @param text line to append.
	 */
	public void append( String text ) {
		area.append(text);
	}

	/**
	 * Append text to the console's output.
	 * @param text a PaintableText object to draw in the console
	 */
	public void append( PaintableText text ) {
		area.append(text);
	}

	/**
	 * Sets the font of the input/output fields
	 * @TODO a better way of handling this.
	 */
	private void setFontReal(Font f) {
		area.setFont(f);
		field.setFont(f);
	}
}
