/* ConnectionDialog.java */

package fish.robo.nova.guis;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;
import fish.robo.nova.*;

/**
  * This dialog is called when the user wants to connect to an IRC server.
  * The user is presented with a list of servers saved in the file 'environment'.
  * @author Kai Berk Oezer
  */
public class ConnectionDialog extends JDialog implements ActionListener, ListSelectionListener, WindowListener
{
	// GUI components
	private JFrame parentFrame;
	private JList<String> servers;
	private JScrollPane scroller;
	private JButton connect, addServer, deleteServer, up, down, close;
	private JTextField name, address, port;

	/** stores IRCServerItem objects that hold info about a server (IP, port #, name) */
	private Vector<IRCServerItem> serverList = new Vector<IRCServerItem>();
	/** String representations of IRCServerItem objects */
	private DefaultListModel<String> serverRepresentations = new DefaultListModel<String>();

	// connection data
	private String user, nickname, password;

	/** reference to the manager of the IRC client */
	private NovaManager manager;

	/**
	  * @param parent the Frame object which this dialog box originated
	  * @param manager the manager of the IRC client
	  */
	public ConnectionDialog(JFrame parent, NovaManager manager)
		{
			super(parent, "choose IRC server", false);
			parentFrame = parent;
			this.manager = manager;

			addWindowListener(this);
			setFont(new Font("Helvetica", Font.PLAIN, 14));
			setSize(500, 250);
			Point loc = parentFrame.getLocation();
			Dimension parentSize = parentFrame.getSize();
			setLocation(loc.x + parentSize.width/2 - 250, loc.y + parentSize.height/2 - 125);
			setResizable(true);
			setBackground(Color.lightGray);

			// components
      JPanel listPanel = new JPanel();
			servers = new JList<String>();
			servers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			servers.addListSelectionListener(this);
			servers.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			servers.setBackground(Color.white);
			servers.setForeground(Color.black);
			servers.setMinimumSize(new Dimension(40,60));
      up = new JButton(new ImageIcon("fish/robo/nova/images/up_arrow.gif"));
      up.addActionListener(this);
      up.setToolTipText("Move selection up in the list.");
      up.setBorderPainted(false);
      up.setEnabled(false);
      down = new JButton(new ImageIcon("fish/robo/nova/images/down_arrow.gif"));
      down.addActionListener(this);
      down.setToolTipText("Move selection down in the list.");
      down.setBorderPainted(false);
      down.setEnabled(false);
			scroller = new JScrollPane(servers);
			scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      JPanel editPanel = new JPanel();
      JPanel dataPanel = new JPanel();
      JPanel add_deletePanel = new JPanel();
      addServer = new JButton("Save");
			addServer.addActionListener(this);
			deleteServer = new JButton("Delete");
			deleteServer.addActionListener(this);
      close = new JButton("Close");
      close.addActionListener(this);
      JSeparator sep = new JSeparator();
			connect = new JButton("Connect");
			connect.addActionListener(this);
      JPanel buttonsPanel = new JPanel() {public Insets getInsets() {return new Insets(2, 5, 2, 5); } };
			JLabel lab1 = new JLabel("Name:", SwingConstants.RIGHT);
			name = new JTextField();
			JLabel lab2 = new JLabel("Address:", SwingConstants.RIGHT);
			address = new JTextField();
			JLabel lab3 = new JLabel("Port:", SwingConstants.RIGHT);
			port = new JTextField();

			// layout
			GridBagLayout gbl = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.insets = new Insets(2, 2, 2, 2);
			gbc.gridheight = GridBagConstraints.RELATIVE;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbl.setConstraints(scroller, gbc);
			gbc.fill = GridBagConstraints.NONE;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.gridheight = GridBagConstraints.REMAINDER;
			gbc.gridwidth = GridBagConstraints.RELATIVE;
			gbc.gridx = 0;
      gbc.gridy = 1;
 			gbl.setConstraints(up, gbc);
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridx = 1;
 			gbl.setConstraints(down, gbc);
      //
			gbc.insets = new Insets(8, 8, 8, 8);
			gbc.gridheight = 1;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weighty = 0.0;
			gbc.weightx = 0.0;
			gbc.gridwidth = GridBagConstraints.RELATIVE;
			gbl.setConstraints(lab1, gbc);
			gbc.gridy = 1;
			gbl.setConstraints(lab2, gbc);
			gbc.gridy = 2;
			gbl.setConstraints(lab3, gbc);
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.weightx = 1.0;
			gbl.setConstraints(name, gbc);
			gbc.gridy = 1;
			gbl.setConstraints(address, gbc);
			gbc.gridy = 2;
			gbl.setConstraints(port, gbc);
			gbc.gridheight = GridBagConstraints.REMAINDER;
			gbc.gridwidth = 2;
			gbc.gridx = 0;
			gbc.gridy = 3;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.anchor = GridBagConstraints.SOUTHEAST;
			gbc.insets = new Insets(2, 5, 2, 5);
			gbl.setConstraints(buttonsPanel, gbc);
      gbc.gridy = 0;
			gbc.gridheight = 1;
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      gbl.setConstraints(add_deletePanel, gbc);
      gbc.gridwidth = 2;
      gbc.gridx = 0;
      gbc.gridy = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.gridheight = GridBagConstraints.RELATIVE;
      gbl.setConstraints(sep, gbc);
      gbc.gridy = 2;
      gbc.fill = GridBagConstraints.NONE;
      gbc.gridheight = GridBagConstraints.REMAINDER;
      gbl.setConstraints(connect, gbc);
      //
      add_deletePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
      add_deletePanel.add(deleteServer);
      add_deletePanel.add(addServer);
      add_deletePanel.add(close);
      listPanel.setLayout(gbl);
      listPanel.add(scroller);
      listPanel.add(up);
      listPanel.add(down);
			buttonsPanel.setLayout(gbl);
			buttonsPanel.add(add_deletePanel);
      buttonsPanel.add(sep);
			buttonsPanel.add(connect);
      dataPanel.setLayout(gbl);
			dataPanel.add(lab1);
			dataPanel.add(name);
			dataPanel.add(lab2);
		  dataPanel.add(address);
			dataPanel.add(lab3);
			dataPanel.add(port);
      editPanel.setLayout(new GridLayout(2, 1, 10, 2));
      editPanel.add(dataPanel);
			editPanel.add(buttonsPanel);
      getContentPane().add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, listPanel, editPanel));

			loadDataFromEnvironment();
			showServers();
			servers.setModel(serverRepresentations);

			setVisible(true);
		}


	//_________________________________________________________________
	// Listener interfaces


	public void actionPerformed(ActionEvent ae)
		{
			Object compo = ae.getSource();
      if (compo == up)
        {
          int item = servers.getSelectedIndex();
          if (item > 0)
            {
              serverList.setElementAt(serverList.set(item, serverList.get(item - 1)), item - 1);
              serverRepresentations.setElementAt(serverRepresentations.set(item, serverRepresentations.get(item - 1)), item - 1);
              servers.setSelectedIndex(item - 1);
            }
        }
      else if (compo == down)
        {
          int item = servers.getSelectedIndex();
          if (item < serverList.size() - 1)
            {
              servers.setSelectedIndex(item + 1);
              serverList.setElementAt(serverList.set(item, serverList.get(item + 1)), item + 1);
              serverRepresentations.setElementAt(serverRepresentations.set(item, serverRepresentations.get(item + 1)), item + 1);
            }
        }
			else if (compo == connect)
				{
					String server_address = address.getText().trim();
					if (server_address.equals("")) return;
					int server_port = 6667;
					try {server_port = Integer.parseInt(port.getText()); }
					catch (NumberFormatException ignore) {}
					manager.connectToServer(server_address, server_port, password, nickname, user);
          updateServers();
					dispose();
				}
			else if (compo == addServer)
				{
					String tmp = name.getText();
					// check whether there is a server name
					if (tmp.trim().equals(""))
            {
              JOptionPane.showMessageDialog(this, "You forgot entering the server's name.", "Nova Error", JOptionPane.ERROR_MESSAGE);
              return;
            }
					// check whether the server name already exists
					IRCServerItem tmpItem;
					boolean found = false;
					for (int i = 0; i < serverList.size(); i++)
						{
							if (serverList.get(i).getName().equals(tmp))
								{
									// change data fields of the server item
									tmpItem = serverList.get(i);
									tmpItem.setAddress(address.getText());
									int prt = 6667;
									try {prt = Integer.parseInt(port.getText()); }
									catch (NumberFormatException ignore) {}
									tmpItem.setPort(prt);
									found = true;
									manager.getEnvironment().replaceTagLine("server " + tmp, "server " + tmp + " " + address.getText() + " " + prt);
									break;
								}
						}
					if (!found)
						{
							// construct new IRCServerItem and add new server to list and environment
							String n = name.getText().trim();
							String adr = address.getText().trim();
              if (adr.equals(""))
                {
                  JOptionPane.showMessageDialog(this, "You forgot entering the server's Internet address.", "Nova Error", JOptionPane.ERROR_MESSAGE);
                  return;
                }
							int prt = 6667;
							try {prt = Integer.parseInt(port.getText().trim()); }
							catch (NumberFormatException ignore)
                {
                  JOptionPane.showMessageDialog(this, "Missing or invalid server port number.", "Nova Error", JOptionPane.ERROR_MESSAGE);
                  return;
                }
							//
							serverList.addElement(new IRCServerItem(n, adr, prt));
							serverRepresentations.addElement(n);
							manager.getEnvironment().append("server " + n + " " + adr + " " + Integer.toString(prt));
              name.setText("");
              address.setText("");
              port.setText("");
						}
				}
      else if (compo == close)
        {
          updateServers();
          dispose();
        }
			else if (compo == deleteServer)
				{
          int index = servers.getSelectedIndex();
          String tmp = (String)(serverRepresentations.elementAt(index));
          if (tmp == null) return;
          serverRepresentations.removeElementAt(index);
          serverList.removeElementAt(index);
          /*
             The server will be removed from the environment
             when a call to updateServer() is made upon exit.
          */
          // clear text fields
          name.setText("");
          address.setText("");
          port.setText("");
        }
		}


  public void valueChanged(ListSelectionEvent lse)
		{
      /*
			String tmp = (String)(serverRepresentations.elementAt(lse.getFirstIndex()));
			IRCServerItem tmpItem = new IRCServerItem();
			name.setText(tmp);
			boolean found = false;
			for (int i = 0; i < serverList.size(); i++)
				{
					tmpItem = serverList.elementAt(i);
					if (tmpItem.getName().equals(tmp)) break;
				}
			address.setText(tmpItem.getAddress());
			port.setText(Integer.toString(tmpItem.getPort()));
      */
      IRCServerItem dummy = serverList.elementAt(lse.getFirstIndex());
      name.setText(dummy.getName());
			address.setText(dummy.getAddress());
			port.setText(Integer.toString(dummy.getPort()));
      if (!up.isEnabled())
        {
          up.setEnabled(true);
          down.setEnabled(true);
        }
		}


	public void windowActivated(WindowEvent we) {}
	public void windowClosed(WindowEvent we) {}
	public void windowDeactivated(WindowEvent we) {}
	public void windowDeiconified(WindowEvent we) {}
	public void windowIconified(WindowEvent we) {}
	public void windowOpened(WindowEvent we) {}

	public void windowClosing(WindowEvent we)
    {
      updateServers();
      dispose();
    }


	//___________________________________________________________________________________


	/**
	  * Scans the file 'environment' for the keywords "server" and "user".
	  * Server information is used to create IRCServerItem objects.
	  * IRCServerItem objects are then stored in a Vector.
	  * User information is extracted and stored to be used during login.
	  */
	private void loadDataFromEnvironment()
		{
			// extract user name and nickname
			user = manager.getEnvironment().getFirstTagLine("user ");
			user = user.substring(5);
			nickname = user.substring(0, user.indexOf(" "));
			user = user.substring(user.indexOf(" ") + 1).trim();
      password = user.substring(0, user.indexOf(" "));
			user = user.substring(user.indexOf(" ") + 1).trim();
			// extract server list
			Vector v1 = manager.getEnvironment().getAllTagLines("server");
			String temp, tempName, tempAddress;
      int tempPort;
			for (int i = 0; i < v1.size(); ++i)
				{
					try
						{
							temp = (String) v1.get(i);
							// get rid of the 'server' tag
							temp = temp.substring(7);
							// get server name
							tempName = temp.substring(0, temp.indexOf(" "));
							// get rid of the name
							temp = temp.substring(temp.indexOf(" ") + 1);
							// get server address
							tempAddress = temp.substring(0, temp.indexOf(" "));
							// get rid of the address - the remaining string is the port
							try {tempPort = Integer.parseInt(temp.substring(temp.indexOf(" ") + 1).trim()); }
							catch (NumberFormatException ignore) {tempPort = 6667; }
							// add this server to the list
							serverList.add(new IRCServerItem(tempName, tempAddress, tempPort));
						}
					// there is a corrupt entry -> entry is skipped
					catch (StringIndexOutOfBoundsException sioobe) {continue; }
				}
		}


	/** The contents of the server list established by method loadServerNames() are shown. */
	private final void showServers()
		{
			int limit = serverList.size();
			for (int i = 0; i < limit; ++i)
			{
				serverRepresentations.addElement((serverList.elementAt(i)).getName());
			}
		}


  private final void updateServers()
    {
      Vector<String> newServers = new Vector<String>();
      IRCServerItem dummy;
      int limit = serverList.size();
      for (int k = 0; k < limit; k++)
        {
          dummy = serverList.get(k);
          newServers.add("server " + dummy.getName() + " " + dummy.getAddress() + " " + Integer.toString(dummy.getPort()));
        }
      manager.getEnvironment().replaceAllTagLines("server ", newServers);
    }
}
