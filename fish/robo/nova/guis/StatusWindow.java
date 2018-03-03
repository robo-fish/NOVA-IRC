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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import fish.robo.nova.*;

/**
  * Defines the user interface and actions of the status window.
  * @version June 1999
  * @author Kai Berk Oezer
  */
public class StatusWindow extends JInternalFrame implements ActionListener, NovaInputListener, InternalFrameListener
{
  /** the main text area where all messages are displayed */
  private JTextPane main;
  private JButton shortcut;
  private DefaultStyledDocument doc;

  /** scroll bar for the main text area */
  private JScrollPane scroller;

  /** reference to the manager of the whole IRC client environment */
  private NovaManager manager;

  /** Builds the user interface. */
  public StatusWindow(NovaManager my_IRC_Manager, NovaInterface parent)
    {
      // call constructor of the parent class JFrame
      super("Status window", true, false, true, true);
      manager = my_IRC_Manager;
      setFrameIcon(parent.NovaIcon);

      // components
      main = new JTextPane(doc = new DefaultStyledDocument()); // main chat area
      //main = new JTextArea();
      main.setDocument(doc = new DefaultStyledDocument());
      main.setEditable(false);
      main.setCursor(Cursor.getDefaultCursor());
      //main.setLineWrap(true);
      //main.setWrapStyleWord(true);
      main.setFont(new Font("SansSerif", Font.PLAIN, 14));
      scroller = new JScrollPane(main);
      scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

      // layout
      GridBagLayout gbl = new GridBagLayout();
      getContentPane().setLayout(gbl);
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.weightx = 1.0;
      gbc.weighty = 1.0;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.anchor = GridBagConstraints.NORTHWEST;
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      gbc.gridheight = GridBagConstraints.REMAINDER;
      gbc.insets = new Insets(2, 4, 2, 4);
      gbc.fill = GridBagConstraints.BOTH;
      gbl.setConstraints(scroller, gbc);

      // other frame properties
      setSize(400, 450);

      // adding all components to the frame
      getContentPane().add(scroller);

      shortcut = parent.addShortcutButton(new JButton("Status", parent.smallNovaIcon));
      shortcut.addActionListener(this);

      giveColors();
    }


  //________________________________________________________________________________
  // Listener interfaces


  /**
    * Linked to the button in the NovaInterface toolbar
    */
  public void actionPerformed(ActionEvent ae)
    {
      toFront();
      try {setSelected(true); }
      catch(java.beans.PropertyVetoException pve) {}
      manager.getInterface().getInputBar().setInputListener(this);
    }

  public void processInput(String input)
    {
      manager.sendMessage(input);
      // if (input.toLowerCase().indexOf("join") == 0) ...
    }

  public void internalFrameClosed(InternalFrameEvent ife) {}
  public void internalFrameDeiconified(InternalFrameEvent ife) {}
  public void internalFrameIconified(InternalFrameEvent ife) {}
  public void internalFrameOpened(InternalFrameEvent ife) {}
  public void internalFrameDeactivated(InternalFrameEvent ife) {}
  public void internalFrameClosing(InternalFrameEvent ife) {}

  public void internalFrameActivated(InternalFrameEvent ife)
    {
      manager.getInterface().getInputBar().setInputListener(this);
    }


  //________________________________________________________________________________
  // functional methods

  /**
    * Appends a character string to the text shown in text area 'main'.
    * It is synchronized so that multiple access from various objects is done in a controlled way.
    */
  public synchronized void appendMessage(String message)
    {
      appendMessage(message, NovaTextStyles.getNovaStyle());
    }

  /**
    * Appends a string of text, with given style, to the JTextPane 'main'.
    */
  public synchronized void appendMessage(String message, Style style)
    {
      Document doc = main.getDocument();
      try {doc.insertString(doc.getLength(), message, style); }
      catch (BadLocationException ble) { /* ignore */ }
      main.setCaretPosition(doc.getLength());
    }


  public void giveColors()
    {
      NovaEnvironment n_env = manager.getEnvironment();
      Color a = n_env.getFirstBackColor();
      Color b = n_env.getFirstForeColor();
      Color c = n_env.getSecondBackColor();
      Color d = n_env.getSecondForeColor();
      getContentPane().setBackground(a);
      getContentPane().setForeground(b);
      main.setBackground(a);
      main.setForeground(b);
      shortcut.setBackground(a);
      shortcut.setForeground(b);
    }
}
