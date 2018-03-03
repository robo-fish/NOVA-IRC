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

import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import fish.robo.nova.*;

/**
  * Provides a standard interface for features that require a single-line text input.
  * @author Kai Berk Oezer
  */
class TextLineInputDialog extends JDialog implements Runnable, WindowListener, MouseListener, KeyListener
{
	private JTextField txtfield;
	private JButton inputButton;
	private String l1, l2;
	private JFrame parent;
	private Method processor;

	/**
	  * @param parent the Frame which this dialog was called from
	  * @param label the label of the dialog box
	  * @param inputLabel the descriptive text left of the text input field
	  * @param buttonLabel the text appearing on the button below the input field
	  * @param toBeInvoked the Method to be called with the received input String
	  */
	TextLineInputDialog(JFrame parentFrame, String label, String inputLabel, String buttonLabel, Method toBeInvoked)
		{
			super(parentFrame, label, true);
			parent = parentFrame;
			l1 = inputLabel;
			l2 = buttonLabel;
			processor = toBeInvoked;
			// the line below is meant to allow child classes to set an initial value
			txtfield = new JTextField(20);
			// For correct operation, the body has to run in its own thread. Don't ask me why.
			(new Thread(this)).start();
		}

	public void run()
		{
			addWindowListener(this);

			setSize(230, 160);
			Point loc = parent.getLocation();
			Dimension parentSize = parent.getSize();
			setLocation(loc.x + parentSize.width / 2 - 155, loc.y + parentSize.height / 2 - 65);
			setResizable(true);
			setFont(new Font("SansSerif", Font.PLAIN, 14));
			setBackground(Color.lightGray);

			// components
			JLabel textLabel = new JLabel(l1, SwingConstants.RIGHT);
			txtfield.setBackground(Color.white);
			txtfield.setForeground(Color.black);
			txtfield.setEditable(true);
			txtfield.addKeyListener(this);
			inputButton = new JButton(l2);
			inputButton.addMouseListener(this);
			JButton cancel = new JButton("Cancel");
			cancel.addMouseListener(this);
			JPanel buttonsPanel = new JPanel();

			// layout
			GridBagLayout gbl = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(8, 6, 8, 6);
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weighty = 0.5;
			gbc.weightx = 0.0;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = GridBagConstraints.RELATIVE;
			gbl.setConstraints(textLabel, gbc);
			gbc.weightx = 1.0;
			gbc.gridx = 1;
			gbc.gridwidth = 2;
			gbl.setConstraints(txtfield, gbc);
			gbc.gridheight = GridBagConstraints.REMAINDER;
			gbc.gridwidth = 3;
			gbc.weightx = 1.0;
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbl.setConstraints(buttonsPanel, gbc);
			buttonsPanel.setLayout(new GridLayout(1, 2, 10, 0));
			//
			buttonsPanel.add(inputButton);
			buttonsPanel.add(cancel);
			getContentPane().setLayout(gbl);
			getContentPane().add(textLabel);
			getContentPane().add(txtfield);
			getContentPane().add(buttonsPanel);

			pack();
			setVisible(true);
		}

	//_________________________________________________________________________________
	// Listener interfaces

	public void mouseReleased(MouseEvent me)
		{
			if (me.getComponent() == inputButton) process(txtfield.getText());
			else dispose();
		}

	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}
	public void mousePressed(MouseEvent me) {}

	public void mouseClicked(MouseEvent me) {}

	public void keyPressed(KeyEvent ke)
		{
			if (ke.getKeyCode() == KeyEvent.VK_ENTER) process(txtfield.getText());
		}

	public void keyTyped(KeyEvent ke) {}
	public void keyReleased(KeyEvent ke) {}

	public void windowClosing(WindowEvent we) {dispose(); }
	public void windowActivated(WindowEvent we) {txtfield.requestFocus(); }
	public void windowClosed(WindowEvent we) {}
	public void windowDeactivated(WindowEvent we) {}
	public void windowDeiconified(WindowEvent we) {}
	public void windowIconified(WindowEvent we) {}
	public void windowOpened(WindowEvent we) {}

	//__________________________________________________________________________________

	/**
	  * This method is provided to be overwritten.
	  * Called when the button is pressed or the ENTER key is hit.
	  * @param input the user input into the text field of this dialog box
	  */
	protected void process(String input)
		{
			Object[] parameters = {input};
			try {processor.invoke(parent, parameters); }
			catch (Exception e) {e.printStackTrace(); }
			dispose();
		}
}
