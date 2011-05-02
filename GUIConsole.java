import javax.swing.JPanel;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.SpringLayout;

/**
 * These components use swing for two important reasons:
 * (1) swing allows the components to be drawn without border/scrollbar
 * (2) the swing textarea makes it MUCH easier to calculate the number of rows
 * 	   that will fit in a textarea with a given size.
 */
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 * Whent he textarea is clicked, we want to set focus to the prompt.
 */
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;

import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;

public class GUIConsole extends BufferedPanel {


	/**
	 * The textarea for the output stream of the console.
	 */
	//protected JTextArea area;
	protected WrappedTextComponent area;

	/**
	 * Textfield for the input stream of the console.
	 */
	protected JTextField field;


	//has a single actionlistener for now.
	private ActionListener listener;


	//command lien history
	private History history = new History(10);

	public GUIConsole() {
		this("");
	}

	public GUIConsole( String name ) {

		setLayout(new BorderLayout());
	
		area = new WrappedTextComponent();

		//listen to the mouse
		area.addMouseListener( new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				field.requestFocusInWindow();
			}	
		});


		//an InputStream that gathers input from a textfield.
		field = new JTextField();

		field.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				history.add( e.getActionCommand());
				field.setText("");
			}
		});

		field.addKeyListener( new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_UP:
						field.setText(history.up());
						break;

					case KeyEvent.VK_DOWN:
						field.setText(history.down());
						break;
				}
			}
		});

		this.addFocusListener( new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				field.requestFocusInWindow();

			}
		});

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

		layout.putConstraint(SpringLayout.WEST, label, 5, SpringLayout.WEST, input);
		layout.putConstraint(SpringLayout.NORTH, label, 5, SpringLayout.NORTH, input);

		layout.putConstraint(SpringLayout.WEST, field, 5, SpringLayout.EAST, label);
		layout.putConstraint(SpringLayout.NORTH, field, 5, SpringLayout.NORTH, input);

		layout.putConstraint(SpringLayout.EAST, input, 5, SpringLayout.EAST, field);
		layout.putConstraint(SpringLayout.SOUTH, input, 5, SpringLayout.SOUTH, field);


		this.add(input, BorderLayout.SOUTH);

		field.requestFocusInWindow();
	}
	
	public void addActionListener(ActionListener l) {
		field.addActionListener(l);
	}

	public JTextField getField() {
		return field;
	}

	public void append( String text ) {
		area.append(text);
	}

	public void append( PaintableText text ) {
		area.append(text);
	}

	private void setFontReal(Font f) {
		area.setFont(f);
		field.setFont(f);
	}
}
