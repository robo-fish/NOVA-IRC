/***************************************************************************
*
* This file is part of the Nova IRC project.
* Copyright (C) 1998-2000, 2018 Kai Berk Oezer
* https://github.com/robo-fish/NOVA-IRC
*
* Nova IRC is free software. You can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
*
****************************************************************************/
package fish.robo.nova.guis;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
  * This is the window that appears when you choose "about" from the help menu.
  * Apparently the image is too big to be loadable as an Icon. Therefore, I used a Canvas.
  * @version June 2000
  * @author Kai Berk Oezer
  */
final class AboutWindow extends JWindow implements MouseListener //ActionListener
{
	/** @param parent the window from which this dialog box originated */
	AboutWindow(Frame parent)
		{
			super(parent);
			//getContentPane().setBackground(Color.black);
            //JButton logo = new JButton(new ImageIcon("fish/robo/nova/images/about.gif"));
			//logo.addActionListener(this);
			AboutCanvas ac = new AboutCanvas();
			ac.addMouseListener(this);
			getContentPane().setLayout(null);
			getContentPane().add(ac);
			int sizeX = ac.getSize().width;
			int sizeY = ac.getSize().height;
			setSize(sizeX, sizeY);
			Point loc = parent.getLocation();
			Dimension parentSize = parent.getSize();
			setLocation(loc.x + (parentSize.width - sizeX)/2, loc.y + (parentSize.height - sizeY)/2);
			setVisible(true);
		}

	public void mouseReleased(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}
	public void mousePressed(MouseEvent me) {}
	public void mouseClicked(MouseEvent me) {dispose (); }

	//public void actionPerformed(ActionEvent ae) {dispose (); }
}



final class AboutCanvas extends Canvas
{
  private Image about;
  private boolean loaded;

  AboutCanvas()
    {
      loaded = false;
      try
        {
          about = getToolkit().getImage("fish/robo/nova/images/about.jpg");
          MediaTracker tracker = new MediaTracker(this);
          tracker.addImage(about, 0);
          tracker.waitForAll();
        }
      catch (InterruptedException ie) {return; }
      loaded = true;
      setSize(about.getWidth(this), about.getHeight(this));
    }

  public void paint(Graphics g)
    {
      g.drawImage(about, 0, 0, Color.black, this);
      g.setColor(Color.yellow);
      g.setFont(new Font("SansSerif", Font.PLAIN, 12));
      int ascent = g.getFontMetrics().getMaxAscent();
      int height = about.getHeight(this);
      while (!loaded) {}
      g.drawString("by " + fish.robo.nova.Nova.authors, 25, height - 20 - 2 * (ascent + 5));
      g.drawString("version: " + fish.robo.nova.Nova.version, 25, height - 20 - (ascent + 5));
      g.drawString(fish.robo.nova.Nova.URL, 25, height - 20);
    }
}
