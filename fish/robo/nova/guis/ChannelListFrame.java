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
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableModel;
import java.util.*;
import fish.robo.nova.*;

/**
  * This class is used to show a list of IRC channels.
  * Double-clicking on a channel joins it.
  * @author Kai Berk Oezer
  * @version June 1999
  */
public class ChannelListFrame extends JInternalFrame implements InternalFrameListener, ActionListener, TableModel, Runnable//, MouseListener, ListSelectionListener
{
	// GUI components
  private JScrollPane scroller;
  private JTable channelTable;
  private JButton openChannel,
	                 closeList,
	                 stopListing,
	                 shortcut;

	private JPanel buttonPanel;
	private static ImageIcon bigListIcon = new ImageIcon("fish/robo/nova/images/list_big.gif");
	private static ImageIcon smallListIcon = new ImageIcon("fish/robo/nova/images/list_small.gif");

	/** reference to the manager of the IRC client */
	private NovaManager manager;

	/** holds the number of channels in the list */
	private int channelCounter = 0;

	/** layer information used while dragging the window */
	private int my_layer;

	/** the buffer used to avoid flooding */
	private Vector<String> items = new Vector<String>();

  /** the elements of this Vector are Vectors themself */
  private Vector<String> columns = new Vector<String>(3);

	/** the thread used to enable sorted listing with a buffer */
	private Thread listing;
	private boolean go_on = false;

	private boolean completed = false;

	/**
	* Builds the user interface.
	* @param manager reference to the manager of the whole IRC client environment
	*/
	public ChannelListFrame(NovaManager manager)
		{
			super("Channel List - receiving names...", true, true, true, true);
			this.manager = manager;
			setFrameIcon(bigListIcon);
			manager.getInterface().addToInterface(this);
			addInternalFrameListener(this);

			// properties
      Dimension parentSize = manager.getInterface().getSize();
			setSize(parentSize.width * 2/3, parentSize.height * 2/3);
      setLocation(parentSize.width / 6, parentSize.height / 6);
			setFont(new Font("Arial", Font.PLAIN, 14));

			// components
      columns.addElement("Channel");
      columns.addElement("Population");
      columns.addElement("Topic");
      channelTable = new JTable();
      scroller = new JScrollPane(channelTable);
			scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			buttonPanel = new JPanel();
			openChannel = new JButton("Go");
			openChannel.setToolTipText("join selected channel");
			openChannel.addActionListener(this);
      openChannel.setEnabled(false);
			closeList = new JButton("Close");
			closeList.addActionListener(this);
			closeList.setToolTipText("close this window");
			stopListing = new JButton("Stop");
			stopListing.addActionListener(this);
			stopListing.setToolTipText("stop listing!");

			// layout
			GridBagLayout gbl = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(2, 4, 2, 4);
			gbc.gridwidth = 3;
			gbc.gridheight = GridBagConstraints.RELATIVE;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbl.setConstraints(scroller, gbc);
			gbc.weighty = 0.0;
			gbc.weightx = 0.333;
			gbc.gridy = 1;
			gbc.gridwidth = 1;
			gbc.gridheight = GridBagConstraints.REMAINDER;
			gbl.setConstraints(openChannel, gbc);
			gbc.gridx = 1;
			gbc.gridwidth = GridBagConstraints.RELATIVE;
			gbl.setConstraints(stopListing, gbc);
			gbc.gridx = 2;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbl.setConstraints(closeList, gbc);

			getContentPane().setLayout(gbl);
			getContentPane().add(scroller);
			getContentPane().add(openChannel);
			getContentPane().add(stopListing);
			getContentPane().add(closeList);

			shortcut = manager.getInterface().addShortcutButton(new JButton("Channel List", smallListIcon));
			shortcut.addActionListener(this);
			giveColors();
			setVisible(true);
      toFront();

			listing = new Thread(this);
			go_on = true;
			listing.start();
		}

	//________________________________________________________
	// interfaces

	public void actionPerformed(ActionEvent me)
		{
			JButton compo = (JButton) me.getSource();
			if (compo == openChannel) joinChannel();
			else if (compo == stopListing)
				{
					manager.stopChannelList();
          completed = true;
				}
			else if (compo == closeList) shutDown();
			else if (compo == shortcut)
        {
          toFront();
          try {setSelected(true); }
          catch(java.beans.PropertyVetoException pve) {}
          grabFocus();
        }
		}



	public void internalFrameActivated(InternalFrameEvent we)
		{
			my_layer = getLayer();
			setLayer(JLayeredPane.DRAG_LAYER);
		}

	public void internalFrameDeactivated(InternalFrameEvent we)
		{
			setLayer(my_layer);
		}

	public void internalFrameClosing(InternalFrameEvent we)
		{
			shutDown();
		}

	public void internalFrameClosed(InternalFrameEvent we) {}
	public void internalFrameDeiconified(InternalFrameEvent we) {}
	public void internalFrameIconified(InternalFrameEvent we) {}
	public void internalFrameOpened(InternalFrameEvent we) {}

	/** adds new items to the GUI list component (in alphabetical order) */
	public void run()
		{
      Vector<String> dummy = new Vector<String>();
      int lower, upper, mid;

      while(go_on)
				{
					if (items.isEmpty())
						{
							if (completed)
								{
                  items = dummy;
									// make GUI changes
                  stopListing.setEnabled(false);
		         	    channelTable.setModel(this);
                  channelTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                  channelTable.setShowVerticalLines(false);
                  channelTable.setCellSelectionEnabled(false);
                  channelTable.setColumnSelectionAllowed(false);
                  channelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                  openChannel.setEnabled(true);
                  setTitle("Channel List - completed with " + items.size() + " entries");
									return;
								}
							try {Thread.sleep(10); }
							catch (InterruptedException ignore) {}
							continue;
						}
					String newChannelString = (String) items.firstElement();
					items.removeElementAt(0);

          // do alphabetical sorting
          lower = 0;
          upper = dummy.size() - 1;
          mid = 0;

          while (lower < upper)
            {
              mid = (lower + upper) / 2;
              if (newChannelString.compareToIgnoreCase((String) dummy.get(mid)) > 0) lower = mid + 1;
              else upper = mid;
            }
          dummy.insertElementAt(newChannelString, lower);

          // update frame title
          if ((++channelCounter % 20) == 0) setTitle("Channel List - receiving names...(" + channelCounter + ")");
				}
		}


  public void addTableModelListener(TableModelListener tml) {}
  public void removeTableModelListener(TableModelListener tml) {}
  public void setValueAt(Object something, int row, int column) {}
  public Class getColumnClass(int i) {return " ".getClass(); }
  public int getRowCount() {return items.size(); }
  public int getColumnCount() {return 3; }
  public String getColumnName(int i)
    {
      switch (i)
        {
          case 0: return "Name";
          case 1: return "Population";
          case 2: return "Topic";
          default: return "";
        }
    }

  public Object getValueAt(int row, int column)
    {
      String temp = (String) items.get(row);
      if (column == 0)
        {
          return temp.substring(0, temp.indexOf(' '));
        }
      else if (column == 1)
        {
          temp = temp.substring(temp.indexOf(' ') + 1);
          return temp.substring(0, temp.indexOf(' '));
        }
      else // column == 2
        {
          temp = temp.substring(temp.indexOf(' ') + 1);
          try {return temp.substring(temp.indexOf(' ') + 2); }
          catch (Exception e) {return "<No Topic>"; }
        }
    }

  public boolean isCellEditable(int row, int column) {return false; }

  //_________________________________________________________________________________________________________________
	// functional methods

	/**
	* Adds an item to the list of channels.
	* Called by method translate() in QCIRCManager.
	* @param newChannelName the new entry to be added
	*/
	public synchronized void addToList(String newChannelName)
		{
			items.addElement(newChannelName);
		}

	/** Opens a new channel window for the selected chat channel from the list. */
	private void joinChannel()
		{
      try
        {
          int select = channelTable.getSelectedRow();
          String tmp = "channel: " + channelTable.getValueAt(select, 0) + " - topic: " + channelTable.getValueAt(select, 2);
          (new Thread(new ChannelFrame(tmp, manager))).start();
        }
      catch (Exception e) {return; }
		}

	/** Called by NovaManager when list is completed. */
	public void complete()
		{
			// notify thread
			completed = true;
		}


	private void giveColors()
		{
			Color a = manager.getEnvironment().getFirstBackColor();
			Color b = manager.getEnvironment().getFirstForeColor();
			Color c = manager.getEnvironment().getSecondBackColor();
			Color d = manager.getEnvironment().getSecondForeColor();
			setBackground(a);
			//channelList.setBackground(a);
			//channelList.setForeground(b);
			channelTable.setBackground(a);
			channelTable.setForeground(b);
			buttonPanel.setBackground(c);
			openChannel.setBackground(c);
			openChannel.setForeground(d);
			closeList.setBackground(c);
			closeList.setForeground(d);
			stopListing.setBackground(c);
			stopListing.setForeground(d);
      shortcut.setBackground(a);
      shortcut.setForeground(b);
		}


	public void shutDown()
		{
			if (listing.isAlive()) go_on = false;
			manager.getInterface().removeShortcutButton(shortcut);
			this.dispose();
		}
}
