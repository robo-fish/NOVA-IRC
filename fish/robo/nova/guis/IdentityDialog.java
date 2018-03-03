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
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import fish.robo.nova.*;

/**
  * This dialog appears when the user wants to change his identity.
  * The user identity is saved in this format: "user" <nickname> <real name>
  * The real name may contain blanks.
  * @author Kai Berk Oezer
  */
class IdentityDialog extends JDialog implements MouseListener, WindowListener, KeyListener
{
	// GUI components
	private JTextField nameField, nickField, infoField;
  private JPasswordField passwordField;
	private NovaEnvironment env;
	private JButton save, cancel;

	/**
	  * Builds the interface and extracts user info from the file 'environment'.
	  * @param parent the window from which this dialog box originated
	  */
	IdentityDialog(JFrame parent)
		{
			super(parent, "edit identity", true);
			env = ((NovaInterface) parent).getManager().getEnvironment();
			Point loc = parent.getLocation();
			Dimension parentSize = parent.getSize();
			setLocation(loc.x + parentSize.width/2 - 145, loc.y + parentSize.height/2 - 90);
			setSize(200, 200);
			setResizable(true);
			addWindowListener(this);

			// components
			JLabel l1 = new JLabel("real name:", SwingConstants.RIGHT);
			JLabel l2 = new JLabel("nickname:", SwingConstants.RIGHT);
      JLabel l3 = new JLabel("password:", SwingConstants.RIGHT);
			JLabel l4 = new JLabel("user info:", SwingConstants.RIGHT);
			nameField = new JTextField(20);
			nameField.addKeyListener(this);
			nickField = new JTextField(20);
			nickField.addKeyListener(this);
      passwordField = new JPasswordField(20);
      passwordField.addKeyListener(this);
			infoField = new JTextField(20);
			infoField.addKeyListener(this);
			save = new JButton("Save");
			save.addMouseListener(this);
			cancel = new JButton("Cancel");
			cancel.addMouseListener(this);
			JPanel buttonPanel = new JPanel();
			JPanel dataPanel = new JPanel();

			// layout
			GridBagLayout gbl = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(4, 4, 4, 4);
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weighty = 0.33;
			gbc.weightx = 0.0;
			gbc.gridwidth = GridBagConstraints.RELATIVE;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbl.setConstraints(l1, gbc);
			gbc.gridy = 1;
			gbl.setConstraints(l2, gbc);
			gbc.gridy = 2;
			gbl.setConstraints(l3, gbc);
      gbc.gridy = 3;
      gbl.setConstraints(l4, gbc);
			gbc.weightx = 1.0;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbl.setConstraints(nameField, gbc);
			gbc.gridy = 1;
			gbl.setConstraints(nickField, gbc);
			gbc.gridy = 2;
      gbl.setConstraints(passwordField, gbc);
      gbc.gridy = 3;
			gbl.setConstraints(infoField, gbc);
			gbc.weighty = 1.0;
			gbc.weightx = 1.0;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridheight = GridBagConstraints.RELATIVE;
			gbl.setConstraints(dataPanel, gbc);
			gbc.weighty = 0.0;
			gbc.gridy = 1;
			gbc.gridheight = GridBagConstraints.REMAINDER;
			gbl.setConstraints(buttonPanel, gbc);

			buttonPanel.setLayout(new GridLayout(1, 2, 10, 4));
			buttonPanel.add(save);
			buttonPanel.add(cancel);
			dataPanel.setLayout(gbl);
			dataPanel.setBorder(BorderFactory.createTitledBorder("User Data"));
			dataPanel.add(l1);
			dataPanel.add(nameField);
			dataPanel.add(l2);
			dataPanel.add(nickField);
			dataPanel.add(l3);
			dataPanel.add(infoField);
      dataPanel.add(l4);
      dataPanel.add(passwordField);
			getContentPane().setLayout(gbl);
			getContentPane().add(dataPanel);
			getContentPane().add(buttonPanel);

			showCurrentIdentity();

			pack();
			setVisible(true);
		}

	//____________________________________________________________________________
	// Listener interfaces

	public void mouseReleased(MouseEvent me)
		{
			if (me.getComponent() == save) process();
			dispose();
		}

	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}
	public void mousePressed(MouseEvent me) {}
	public void mouseClicked(MouseEvent me) {}

	public void keyPressed(KeyEvent ke)
		{
			if (ke.getKeyCode() == KeyEvent.VK_ENTER)
				{
					if (ke.getComponent() == nameField) nickField.requestFocus();
					else if (ke.getComponent() == nickField) passwordField.requestFocus();
					else if (ke.getComponent() == passwordField) infoField.requestFocus();
					else
						{
							process();
							dispose();
						}
				}
		}

	public void keyTyped(KeyEvent ke) {}
	public void keyReleased(KeyEvent ke) {}

	public void windowActivated(WindowEvent we) {}
	public void windowClosed(WindowEvent we) {}
	public void windowDeactivated(WindowEvent we) {}
	public void windowDeiconified(WindowEvent we) {}
	public void windowIconified(WindowEvent we) {}
	public void windowOpened(WindowEvent we) {}

	public void windowClosing(WindowEvent we)
		{
			dispose();
		}

	//_______________________________________________________________________________
	// functional methods

	/**
	  * Scans the file 'environment' and extracts user information to be displayed.
	  * Called by the constructor only.
	  */
	private void showCurrentIdentity()
		{
			String user_info = env.getFirstTagLine("user ");
			user_info = user_info.substring(5);
			nickField.setText(user_info.substring(0, user_info.indexOf(" ")));
			user_info = user_info.substring(user_info.indexOf(" ") + 1).trim();
      if (user_info.substring(0, user_info.indexOf(" ")).equals("<nopass>"))
        passwordField.setText("");
      else
			  passwordField.setText(user_info.substring(0, user_info.indexOf(" ")));
      nameField.setText(user_info.substring(user_info.indexOf(" ") + 1).trim());
			infoField.setText(env.getFirstTagLine("userinfo ").substring(9).trim());
		}

	/**
	  * Called when user decides to save the new entries.
	  * The old information is effectively removed by overwriting the keyword 'user'.
	  * The new information is then appended.
	  */
	private void process()
		{
      String pass = (new String(passwordField.getPassword())).trim();
      if (pass.equals("")) pass = "<nopass>";
			env.replaceTagLine("user ", "user " + nickField.getText().trim() + " " + pass + " " + nameField.getText().trim());
			env.replaceTagLine("userinfo ", "userinfo " + infoField.getText());
		}
}
