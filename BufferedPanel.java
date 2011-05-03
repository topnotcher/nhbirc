
import javax.swing.JPanel;
import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics;


/**
 * Buffers painting on a normal panel by painting to
 * an image, then painting the image to the panel
 * and by not clearing the panel on update
 */
public class BufferedPanel extends JPanel {

	/**
	 * Buffer image
	 */
	private Image buffer = null;

	/**
	 * Graphics Context for Buffer Image
	 */
	private Graphics bufG; 

	public BufferedPanel() { }

	/**
	 * @override
	 */
	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x,y,w,h);
	
		//invalidate the buffer each time
		//the size of the panel changes
		buffer = null;
	}

	/**
	 * @override
	 * Override update() to avoid clearing before calling paint. (one of the main causes of flicker)
	 */
	public void update(Graphics g) {
		paint(g);
	}

	/**
	 * @override
	 */
	public void paint(Graphics g) {

		/**
		 * If there is no valid buffer, create one the same size as the panel.
		 */
		if (buffer == null) {
			buffer = createImage(getWidth(),getHeight());
			bufG = buffer.getGraphics();
		}

		//always fill the buffer with a background color (clear the image)
		bufG.setColor(getBackground());
		bufG.fillRect(0,0,getWidth(),getHeight());
		bufG.setColor(getForeground());
	
		//the panel, and all subcomponents get painted on the buffer.
		super.paint(bufG);

		//Finally, the buffer is painted on the screen
		g.drawImage(buffer,0,0,getBackground(),this);

	}

}

