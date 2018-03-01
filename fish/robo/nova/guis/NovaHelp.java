/* NovaHelp.java */

package fish.robo.nova.guis;

import fish.robo.nova.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.help.*;

/**
  * This class provides the general help interface for Nova.
  * Sun Microsystems' "JavaHelp" help utility is used here.
  * @author Kai Berk Oezer
  * @version April 2000
  */
public class NovaHelp extends JInternalFrame implements InternalFrameListener, ActionListener
{
  private JButton shortcut;
  private NovaManager manager;
  private JHelp helper;

      /**
  * Builds the user interface and extracts all topics from the given file.
  * @param title the title of the help window
  * @param helpFileName the name of the help file, including path.
  * @param environment the properties class of Nova
  * @param x_pos window offset from parent window in x direction
  * @param y_pos window offset from parent window in y direction
  */
  public NovaHelp(String title, String helpFileName, NovaManager manager)
  {
    super(title, true, true, true, true);
    this.manager = manager;
    setFrameIcon(manager.getInterface().NovaIcon);
    addInternalFrameListener(this);
    int parentWidth = manager.getInterface().getSize().width;
    int parentHeight = manager.getInterface().getSize().height;
    setSize(parentWidth*2/3, parentHeight*2/3);
    setLocation(new Point(parentWidth/6, parentHeight/6));
    setResizable(true);
    setFont(new Font("Helvetica", Font.PLAIN, 14));

    try
    {
      HelpSet helpset = new HelpSet(null, HelpSet.findHelpSet(null, helpFileName));
      helper = new JHelp(helpset);
      getContentPane().add(helper);
      shortcut = manager.getInterface().addShortcutButton(new JButton(title, manager.getInterface().smallNovaIcon));
      shortcut.addActionListener(this);
      giveColors();
      setVisible(true);
      toFront();
    }
    catch (HelpSetException hse)
    {
      JOptionPane.showMessageDialog(manager.getInterface(), "Help file not found.", "Nova Help Error", JOptionPane.ERROR_MESSAGE);
      dispose();
    }
  }


  //______________________________________________________________________
  // Listener interfaces

  public void internalFrameActivated(InternalFrameEvent ife) {}
  public void internalFrameDeactivated(InternalFrameEvent ife) {}
  public void internalFrameClosed(InternalFrameEvent ife) {}
  public void internalFrameDeiconified(InternalFrameEvent ife) {}
  public void internalFrameIconified(InternalFrameEvent ife) {}
  public void internalFrameOpened(InternalFrameEvent ife) {}
  public void internalFrameClosing(InternalFrameEvent ife)
  {
  	manager.getInterface().removeShortcutButton(shortcut);
  	dispose();
  }


  public void actionPerformed(ActionEvent ae)
  {
  	toFront();
    try {setSelected(true); }
    catch(java.beans.PropertyVetoException pve) {}
  }

  //_________________________________________________________________________
  // functional methods


  /**
    * Sets colors of the window components.
    */
  public void giveColors()
  {
  	Color a = manager.getEnvironment().getFirstBackColor();
  	Color b = manager.getEnvironment().getFirstForeColor();
  	Color c = manager.getEnvironment().getSecondBackColor();
  	Color d = manager.getEnvironment().getSecondForeColor();
  	helper.setBackground(a);
          helper.setForeground(b);
  	shortcut.setBackground(a);
          shortcut.setForeground(b);
  }
}
