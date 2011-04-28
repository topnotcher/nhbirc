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

import java.io.PrintStream;
import java.io.InputStream;

/**
 * Whent he textarea is clicked, we want to set focus to the prompt.
 */
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import java.awt.event.ActionListener;

public class GUIConsole extends JPanel implements MouseListener {

	/**
	 * Output(Print)Stream associated with this console.
	 */
	private PrintStream out;

	/**
	 * InputStrem associated with this console.
	 */
	private TextFieldPrompt in;

	/**
	 * The textarea for the output stream of the console.
	 */
	protected JTextArea area;

	/**
	 * Textfield for the input stream of the console.
	 */
	protected JTextField field;

	private ActionListener listener;


	public GUIConsole() {
		this("");
	}

	public GUIConsole( String name ) {

		//BorderLayout makes for an easy console layout
		//although it's probably a little bloated for this simple purpose
		super(new BorderLayout());
	
		//OutputStream that appends output to a textarea
		TextAreaOutput textout = new TextAreaOutput();

		//the textarea being appended to
		area = textout.area;

		//listen to the mouse
		area.addMouseListener(this);

		//a printstream build around the textarea
		out = new PrintStream( textout );

		//an InputStream that gathers input from a textfield.
		in = new TextFieldPrompt();

		//the textfield
		field = in.field;

		//add/position both inpupt components to this panel
		this.add(textout.area, BorderLayout.CENTER);

		SpringLayout layout = new SpringLayout() ;
	
		JPanel input = new JPanel( layout );

		javax.swing.JLabel label = new javax.swing.JLabel( name );

		input.add(label);
		input.add(in.field);

		layout.putConstraint(SpringLayout.WEST, label, 5, SpringLayout.WEST, input);
		layout.putConstraint(SpringLayout.NORTH, label, 5, SpringLayout.NORTH, input);

		layout.putConstraint(SpringLayout.WEST, in.field, 5, SpringLayout.EAST, label);
		layout.putConstraint(SpringLayout.NORTH, in.field, 5, SpringLayout.NORTH, input);

		layout.putConstraint(SpringLayout.EAST, input, 5, SpringLayout.EAST, in.field);
		layout.putConstraint(SpringLayout.SOUTH, input, 5, SpringLayout.SOUTH, in.field);


		this.add(input, BorderLayout.SOUTH);
		
		//Green on black is the ONLY color for a terminal.
		textout.area.setBackground(Color.black);
		textout.area.setForeground(Color.green);

//		in.field.setBackground(Color.black);
//		in.field.setForeground(Color.green);

		//and of course monospace...
		setFontReal(new Font("Monospaced", Font.PLAIN, 12));
	}
	
	public void addActionListener(ActionListener l) {
		in.addActionListener(l);
	}

	public void setOut(PrintStream out) {
		//not implemented
	}

	public InputStream in() {
		return in;
	}

	public PrintStream out() {
		return out;
	}

	private void setFontReal(Font f) {
		area.setFont(f);
		field.setFont(f);
	}
	public void prompt(String prompt) {
		in.setPrompt(prompt);
		field.requestFocusInWindow();
	}

	public void mouseClicked(MouseEvent e) {
		//focus the textfield when the console is clicked.
		field.requestFocusInWindow();
	}

	/**
	 * Empty methods for the interface
	 */
	public void mouseEntered(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
}
