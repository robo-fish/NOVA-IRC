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
import java.util.Vector;
import fish.robo.nova.*;

/**
* This dialog is called when the user is connected to an IRC server and opted for a favorite-channels dialog box.
* The user is presented with a list of channels stored in the environment.
* @author Kai Berk Oezer
*/
class FavoritesDialog extends JDialog implements ActionListener, ListSelectionListener, WindowListener, KeyListener
{
	// GUI components
	private NovaInterface parent;
	private JList<String> channels;
	private DefaultListModel<String> favorites_list = new DefaultListModel<String>();
	private JButton join, addChannel, deleteChannel;
	private JTextField entry;

	/** reference to the manager of the IRC client */
	private NovaManager manager;

	/**
	* @param parent the Frame object which this dialog box originated
	* @param manager the manager of the IRC client
	*/
	FavoritesDialog(NovaInterface parent, NovaManager manager)
		{
			super(parent, "favorite IRC channels", false);
			this.parent = parent;
			this.manager = manager;

			// components
			channels = new JList<String>(favorites_list);
			channels.addListSelectionListener(this);
			channels.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			JScrollPane scroller = new JScrollPane(channels);
			scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			entry = new JTextField();
			entry.addKeyListener(this);
			join = new JButton("Join");
			if (parent.isIRCEnabled()) join.addActionListener(this);
			else join.setEnabled(false);
			addChannel = new JButton("Add");
			addChannel.addActionListener(this);
			deleteChannel = new JButton("Remove");
			deleteChannel.addActionListener(this);

			// layout
			GridBagLayout gbl = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.insets = new Insets(6, 6, 6, 6);
			gbc.gridwidth = 3;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbl.setConstraints(scroller, gbc);
			gbc.weighty = 0.0;
			gbc.gridy = 1;
			gbc.gridheight = GridBagConstraints.RELATIVE;
			gbl.setConstraints(entry, gbc);
			gbc.gridy = 2;
			gbc.gridwidth = 1;
			gbc.gridheight = GridBagConstraints.REMAINDER;
			gbc.weightx = 0.33;
			gbc.insets = new Insets(4, 6, 4, 6);
			gbl.setConstraints(addChannel, gbc);
			gbc.gridx = 1;
			gbc.gridwidth = GridBagConstraints.RELATIVE;
			gbl.setConstraints(deleteChannel, gbc);
			gbc.gridx = 2;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbl.setConstraints(join, gbc);

			getContentPane().setLayout(gbl);
			getContentPane().add(scroller);
			getContentPane().add(entry);
			getContentPane().add(addChannel);
			getContentPane().add(deleteChannel);
			getContentPane().add(join);

			addWindowListener(this);
			setFont(new Font("Helvetica", Font.PLAIN, 14));
			setSize(260, 260);
			Point loc = parent.getLocation();
			Dimension parentSize = parent.getSize();
			setLocation(loc.x + parentSize.width/2 - 130, loc.y + parentSize.height/2 - 130);
			setResizable(true);

			giveColors();

			pack();
			setVisible(true);

			// get favorite channel names from environment
			String favs = manager.getEnvironment().getFirstTagLine("favorites").substring(9).trim();
			int pos = 0;
			while ((pos = favs.indexOf(" ")) != -1)
				{
					favorites_list.addElement(favs.substring(0, pos));
					favs = favs.substring(pos + 1);
				}
			if (!favs.trim().equals("<empty>")) favorites_list.addElement(favs);
		}

	//_________________________________________________________________
	// Listener interfaces

	public void actionPerformed(ActionEvent ae)
		{
			if (ae.getSource() == addChannel)
				{
					if (!entry.getText().trim().equals(""))
						{
							favorites_list.addElement(entry.getText());
							entry.setText("");
						}
				}
			else if (ae.getSource() == deleteChannel)
				{
					if (channels.getSelectedIndex() != -1) favorites_list.removeElementAt(channels.getSelectedIndex());
					entry.setText("");
				}

			else // join channel
				{
					String ch = (String)(favorites_list.elementAt(channels.getSelectedIndex()));
					if (ch != null) (new Thread(new ChannelFrame("channel: " + ch + " - topic: ?", manager))).start();
				}
		}

	public void valueChanged(ListSelectionEvent lse)
		{
			entry.setText((String)(favorites_list.elementAt(channels.getSelectedIndex())));
			/* ON DOUBLE-CLICKS
			// join selected channel
			(new Thread(new ChannelFrame("channel: " + (String)(favorites_list.elementAt(channels.getSelectedIndex())) + " - topic: ?", manager))).start();
			*/
		}

	public void windowActivated(WindowEvent we) {}
	public void windowClosed(WindowEvent we) {}
	public void windowDeactivated(WindowEvent we) {}
	public void windowDeiconified(WindowEvent we) {}
	public void windowIconified(WindowEvent we) {}
	public void windowOpened(WindowEvent we) {}

	public void windowClosing(WindowEvent we) {getOut(); }

	public void keyPressed(KeyEvent ke)
		{
			if ((ke.getKeyCode() == KeyEvent.VK_ENTER) && !entry.getText().trim().equals(""))
				{
					favorites_list.addElement(entry.getText());
					entry.setText("");
				}
		}

	public void keyReleased(KeyEvent ke) {}
	public void keyTyped(KeyEvent ke) {}

	//___________________________________________________________________________________

	private void getOut()
		{
			int limit = favorites_list.size();
			if (limit == 0) manager.getEnvironment().replaceTagLine("favorites ", "favorites <empty>");
			else
				{
					String favs = "favorites";
					for (int i = 0; i < limit; ++i) favs += " " + favorites_list.elementAt(i);
					manager.getEnvironment().replaceTagLine("favorites", favs);
				}
			dispose();
		}

	private void giveColors()
		{
			NovaEnvironment ne = manager.getEnvironment();
			Color a = ne.getFirstBackColor();
			Color b = ne.getFirstForeColor();
			Color c = ne.getSecondBackColor();
			Color d = ne.getSecondForeColor();
			getContentPane().setBackground(a);
			getContentPane().setForeground(b);
			channels.setBackground(a);
			channels.setForeground(b);
			entry.setBackground(c);
			entry.setForeground(d);
		}
}
