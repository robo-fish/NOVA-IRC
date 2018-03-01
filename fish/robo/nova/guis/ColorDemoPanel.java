/* ColorDemoPanel.java */

package fish.robo.nova.guis;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
  * This class draws representative figures for the components that carry
    the four user-defined colors. Clicking on a component opens a color
    chooser dialog.
  */
class ColorDemoPanel extends JPanel implements MouseListener
{
	private Color primaryFore,
	              primaryBack,
                      secondaryFore,
                      secondaryBack;

	private ColorsDialog parentContainer;
	
	final int w = 250, h = 210; // width and height of this canvas
	final Font fnt = new Font("Monospaced", Font.PLAIN, 12);
        final String string1 = "Click on component",
                     string2 = "to select its color.",
                     string3 = "Button";

        private Point text1_upper_left, text1_lower_right,
                      text2_upper_left, text2_lower_right;

	ColorDemoPanel(ColorsDialog parent,Color pf,Color pb,Color sf,Color sb)
		{
			super();
			addMouseListener(this);
			parentContainer = parent;
			primaryFore = pf;
			primaryBack = pb;
			secondaryFore = sf;
			secondaryBack = sb;
                        text1_upper_left = new Point((w - getFontMetrics(fnt).stringWidth(string2))/2, h/3 - getFontMetrics(fnt).getMaxAscent());
                        text1_lower_right = new Point((w + getFontMetrics(fnt).stringWidth(string2))/2, h/3 + 2*getFontMetrics(fnt).getMaxAscent() + getFontMetrics(fnt).getMaxDescent() + 2);
                        text2_upper_left = new Point((w - getFontMetrics(fnt).stringWidth(string3))/2, h*2/3 + 35 - getFontMetrics(fnt).getMaxAscent()/2);
                        text2_lower_right = new Point((w + getFontMetrics(fnt).stringWidth(string3))/2, h*2/3 + 35 + getFontMetrics(fnt).getMaxAscent()/2 + getFontMetrics(fnt).getMaxDescent()/2);
			setSize(w, h);
		}


	public void paint(Graphics g)
		{
			g.setColor(primaryBack);
			g.fillRect(0, 0, w - 1, h - 1);
			g.setColor(Color.black);
			g.drawLine(20, h*2/3, 20, 20);
			g.drawLine(20, 20, w - 21, 20);
			g.setColor(Color.white);
			g.drawLine(w - 20, 20, w - 20, (h*2/3) + 1);
			g.drawLine(w - 20, (h*2/3) + 1, 20, (h*2/3) + 1);
			g.setFont(fnt);
			g.setColor(primaryFore);
			g.drawString(string1, (w - getFontMetrics(fnt).stringWidth(string1))/2, h/3);
			g.drawString(string2, (w - getFontMetrics(fnt).stringWidth(string2))/2, h/3 + getFontMetrics(fnt).getMaxAscent() + getFontMetrics(fnt).getMaxDescent() + 2);
			g.setColor(secondaryBack);
			g.fillRect(w/2 - 40, h*2/3 + 20, 80, 30);
			g.setColor(Color.white);
			g.drawLine(w/2 - 40, h*2/3 + 50, w/2 - 40, h*2/3 + 20);
			g.drawLine(w/2 - 40, h*2/3 + 20, w/2 + 40, h*2/3 + 20);
			g.setColor(Color.black);
			g.drawLine(w/2 + 40, h*2/3 + 20, w/2 + 40, h*2/3 + 50);
			g.drawLine(w/2 + 40, h*2/3 + 50, w/2 - 40, h*2/3 + 50);
			g.setColor(secondaryFore);
			g.drawString(string3, (w - getFontMetrics(fnt).stringWidth(string3))/2, h*2/3 + 35 + getFontMetrics(fnt).getMaxAscent()/2);
		}


	//_____________________________________________________________________


	public void mouseReleased(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}
	public void mousePressed(MouseEvent me) {}
	
	public void mouseClicked(MouseEvent me)
		{
			int x = me.getX();
			int y = me.getY();
                        // mouse clicked on button text (text 2).
			if (x > text2_upper_left.x && x < text2_lower_right.x && y > text2_upper_left.y && y < text2_lower_right.y)
				{
					Color C = JColorChooser.showDialog(this, "Secondary Foreground Color", secondaryFore);
					if (C == null) return;
					secondaryFore = C;
					parentContainer.setSecondaryFront(C);
					repaint();
				}
                        // mouse click on button background
			else if (x > w/2 - 40 && x < w/2 + 40 && y > h*2/3 + 20 && y < h*2/3 + 50)
				{
					Color C = JColorChooser.showDialog(this, "Secondary Background Color", secondaryBack);
					if (C == null) return;
					secondaryBack = C;
					parentContainer.setSecondaryBack(C);
					repaint();
				}
                        // mouse click on text 1
			else if (x > text1_upper_left.x && x < text1_lower_right.x && y > text1_upper_left.y && y < text1_lower_right.y)
				{
					Color C = JColorChooser.showDialog(this, "Primary Foreground Color", primaryFore); 
					if (C == null) return;
					primaryFore = C;
					parentContainer.setPrimaryFront(C);
					repaint();
				}
                        // mouse click on large background area
			else
				{
					Color C = JColorChooser.showDialog(this, "Primary Background Color", primaryBack);
					if (C == null) return;
					primaryBack = C;
					parentContainer.setPrimaryBack(C);
					repaint();
				}
		}

  public Dimension getPreferredSize()
    {
      return new Dimension(w, h);
    }
}
