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
import fish.robo.nova.*;

/**
  * The "name filtering" dialog box.
  * Called when the user wants to set a mask for the channel list.
  * @author Kai Berk Oezer
  */
public class NameFilterDialog extends JDialog implements WindowListener, MouseListener, KeyListener
{
	private NovaInterface parent;
	private JTextField txtfield;
	private JButton inputButton;
	private JCheckBox case_sensitive;

	/** @param parent the window from which this dialog box originated */
	public NameFilterDialog(NovaInterface parentFrame)
		{
			super(parentFrame, "set channel name filter", true);
			parent = parentFrame;
			addWindowListener(this);

			// components
			JLabel textLabel = new JLabel("keyword:", SwingConstants.RIGHT);
			txtfield = new JTextField(20);
			txtfield.setBackground(Color.white);
			txtfield.setEditable(true);
			txtfield.addKeyListener(this);
			case_sensitive = new JCheckBox("case sensitive");
			inputButton = new JButton("OK");
			inputButton.addMouseListener(this);
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addMouseListener(this);
			JPanel filterPanel = new JPanel();
			filterPanel.setBorder(BorderFactory.createEtchedBorder(Color.white, Color.black));

			// layout
			GridBagLayout gbl = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(4, 4, 4, 4);
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridwidth = GridBagConstraints.RELATIVE;
			gbc.gridheight = GridBagConstraints.RELATIVE;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weighty = 0.5;
			gbc.weightx = 0.0;
			gbl.setConstraints(textLabel, gbc);
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridx = 1;
			gbc.weightx = 1.0;
			gbl.setConstraints(txtfield, gbc);
			gbc.gridheight = GridBagConstraints.REMAINDER;
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.fill = GridBagConstraints.NONE;
			gbl.setConstraints(case_sensitive, gbc);
			//
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridy = 0;
			gbc.weighty = 1.0;
			gbc.gridheight = GridBagConstraints.RELATIVE;
			gbl.setConstraints(filterPanel, gbc);
			gbc.gridwidth = GridBagConstraints.RELATIVE;
			gbc.weightx = 0.5;
			gbc.weighty = 0.0;
			gbc.gridy = 1;
			gbl.setConstraints(inputButton, gbc);
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridx = 1;
			gbl.setConstraints(cancelButton, gbc);

			filterPanel.setLayout(gbl);
			filterPanel.add(textLabel);
			filterPanel.add(txtfield);
			filterPanel.add(case_sensitive);
			getContentPane().setLayout(gbl);
			getContentPane().add(filterPanel);
			getContentPane().add(inputButton);
			getContentPane().add(cancelButton);

			String temp = parent.getManager().getNameFilter();
			if (temp != null) txtfield.setText(temp);
			case_sensitive.setSelected(parent.getManager().getNameFilterSensitivity());

			setSize(300, 150);
			Point loc = parent.getLocation();
			Dimension parentSize = parent.getSize();
			setLocation(loc.x + parentSize.width/2 - 150, loc.y + parentSize.height/2 - 75);
			setResizable(true);
			pack();
			setVisible(true);
		}

	//_________________________________________________________________________________
	// Listener interfaces

	public void mouseReleased(MouseEvent me)
		{
			if (me.getComponent() == inputButton) process();
			else dispose();
		}

	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}
	public void mousePressed(MouseEvent me) {}
	public void mouseClicked(MouseEvent me) {}

	public void keyPressed(KeyEvent ke)
		{
			if (ke.getKeyCode() == KeyEvent.VK_ENTER) process();
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

	private void process()
		{
			String input = txtfield.getText();
			input.trim();
			if (input.equals("")) input = null;
			parent.getManager().setNameFilter(input, case_sensitive.isSelected());
			dispose();
		}
}
