/* ChannelFrame.java */

package fish.robo.nova.guis;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.util.*;
import java.io.*;
import fish.robo.nova.*;

/**
  * This class implements the user interface for an IRC channel.
  * The user can perform actions that will affect the chat channel.
  * For general IRC commands the user has to return to the main screen.
  * @version June 1999
  * @author Kai Berk Oezer
  */
public class ChannelFrame extends JInternalFrame implements Runnable, ActionListener, NovaInputListener, InternalFrameListener, MouseListener, ComponentListener, NicknameListener
{
	/** the name of this channel */
	private String thisChannel;

	/** nickname of the user */
	private String nick;

	/** Highlighted nickname. Used when the user wants to concentrate on the messages of a specific chat partner */
	private String highlightedNick = "";

	/** The vector which stores the list of nicknames displayed at the right-hand-side */
	private DefaultListModel<String> the_people = new DefaultListModel<String>();

	// GUI components
	private JTextPane mainArea;
	private JList<String> people;
	private JScrollPane scroller, scroller2;
	private JSplitPane splitter;
	private JMenuItem log;
	private JButton shortcut;
	private JPopupMenu channelPopup, peoplePopup;


	/** reference to the manager of the IRC client */
	private NovaManager manager;

	// logging-related objects
	private boolean logging = false;
	private BufferedWriter logWriter = null;

	/** used to call TextLineInputDialog boxes */
	final static Class[] string_class = {"".getClass() };
	final static Font channelFont = new Font("SansSerif", Font.PLAIN, 14);
	final static char ctrlC = 3; // the 'End of Transmission' character (shown as ^C on Unix)
	private DefaultStyledDocument doc;
	static final ImageIcon bigChannelIcon = new ImageIcon("fish/robo/nova/images/channel_big.gif");
	static final ImageIcon smallChannelIcon = new ImageIcon("fish/robo/nova/images/channel_small.gif");


	/**
	  * @param label the title of this window; includes the name and topic of the channel.
	  * @param manager a reference to the IRC environment manager
	  */
	public ChannelFrame(String label, NovaManager manager)
		{
			super(label, true, true, true, true);
			if (label != null) thisChannel = IRCProtocolDroid.getSecond(label);
			this.manager = manager;
			mainArea = new JTextPane(doc = new DefaultStyledDocument()); // Important. This line has to be in the constructor and not in run().
			manager.addChatChannel(this);
			addInternalFrameListener(this);
			addComponentListener(this);
		}



	/** Builds the user interface and sends the server a JOIN message. */
	public void run()
		{
			// components
			mainArea.setEditable(false);
			mainArea.setCursor(Cursor.getDefaultCursor());
			mainArea.addMouseListener(this);
			scroller = new JScrollPane(mainArea);
			scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			people = new JList<String>(the_people);
			people.setFont(channelFont);
			people.addMouseListener(this);
			scroller2 = new JScrollPane(people);
			scroller2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroller, scroller2);
			splitter.setDividerSize(8);
			splitter.setOneTouchExpandable(false);
			peoplePopup = new JPopupMenu();
			channelPopup = new JPopupMenu();
			log = new JMenuItem("Start logging...");
			log.addActionListener(this);
			log.setActionCommand("start_log");
			JMenuItem kick = new JMenuItem("Kick!");
			kick.addActionListener(this);
			kick.setActionCommand("kick");
			JMenuItem private_chat = new JMenuItem("Chat");
			private_chat.addActionListener(this);
			private_chat.setActionCommand("private");
			JMenuItem highlight = new JMenuItem("Highlight");
			highlight.addActionListener(this);
			highlight.setActionCommand("highlight");
			JMenuItem mode = new JMenuItem("Set Mode...");
			mode.addActionListener(this);
			mode.setActionCommand("mode");
			JMenuItem whois = new JMenuItem("Query");
			whois.addActionListener(this);
			whois.setActionCommand("whois");
			JMenuItem topic = new JMenuItem("Set Topic...");
			topic.addActionListener(this);
			topic.setActionCommand("topic");
			JMenuItem invite = new JMenuItem("Invite...");
			invite.addActionListener(this);
			invite.setActionCommand("invite");

      peoplePopup.add(private_chat);
			peoplePopup.add(highlight);
			peoplePopup.add(whois);
			peoplePopup.add(kick);
			channelPopup.add(log);
			channelPopup.add(mode);
			channelPopup.add(topic);
			channelPopup.add(invite);
			getContentPane().add(splitter);

			shortcut = (manager.getInterface()).addShortcutButton(new JButton(thisChannel, smallChannelIcon));
			shortcut.addActionListener(this);
			setFrameIcon(bigChannelIcon);
			setSize(650, 400);
			splitter.setDividerLocation(0.8);
			giveColors();
			setVisible(true);
      toFront();
      manager.getInterface().getInputBar().setInputListener(this);
			nick = manager.getNick();
			connectToChannel();
		}


	//_______________________________________________________________________________
	// Listener interfaces


	public void actionPerformed(ActionEvent ae)
		{
			String command = ae.getActionCommand();

			if (ae.getSource() == shortcut) // NovaInterface toolbar button pressed
				{
					toFront();
          try {setSelected(true); }
          catch(java.beans.PropertyVetoException pve) {}
          manager.getInterface().getInputBar().setInputListener(this);
				}
			else if (command.equals("start_log") && !logging) startLogger();
			else if (command.equals("stop_log") && logging) stopLogger();
			else
				try
					{
						if (command.equals("whois")) manager.sendMessage("WHOIS " + strip(the_people.elementAt(people.getSelectedIndex())));
						else if (command.equals("invite")) new TextLineInputDialog(manager.getInterface(), "invite a person", "nickname:", "Invite", getClass().getMethod("inviteProcess", string_class));
						else if (command.equals("kick")) manager.sendMessage("KICK " + thisChannel + " " + strip(the_people.elementAt(people.getSelectedIndex())) + " : ");
						else if (command.equals("mode")) new TextLineInputDialog(manager.getInterface(), "set channel mode", "mode:", "Set", getClass().getMethod("modeProcess", string_class));
						else if (command.equals("topic")) new TextLineInputDialog(manager.getInterface(), "change topic", "new topic:", "Accept", getClass().getMethod("topicProcess", string_class));
						else if (command.equals("private")) manager.addPrivateChat(new OneToOneChatFrame(strip(the_people.elementAt(people.getSelectedIndex())), manager));
						else if (command.equals("highlight")) highlightedNick = strip(the_people.elementAt(people.getSelectedIndex()));
					}
				catch (NoSuchMethodException ignore) {}
		}



	public void internalFrameClosed(InternalFrameEvent ife) {}
	public void internalFrameDeiconified(InternalFrameEvent ife) {}
	public void internalFrameIconified(InternalFrameEvent ife) {}
	public void internalFrameOpened(InternalFrameEvent ife) {}
	public void internalFrameDeactivated(InternalFrameEvent ife) {}

	public void internalFrameActivated(InternalFrameEvent ife)
    {
      manager.getInterface().getInputBar().setInputListener(this);
    }

  public void internalFrameClosing(InternalFrameEvent ife)
	  {
			shutDown();
	  }


	public void mousePressed(MouseEvent me) {}
	public void mouseReleased(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}

	public void mouseClicked(MouseEvent me)
		{
			if (SwingUtilities.isRightMouseButton(me))
        {
          if (me.getComponent() == mainArea) channelPopup.show(mainArea, me.getX(), me.getY());
			    else peoplePopup.show(people, me.getX(), me.getY());
        }
		}

	public void componentHidden(ComponentEvent ce) {}
	public void componentShown(ComponentEvent ce) {}
	public void componentMoved(ComponentEvent ce) {}
	public void componentResized(ComponentEvent ce)
		{
			splitter.setDividerLocation(0.8);
		}


  // implementing the NovaInputListener interface
  public void processInput(String input)
    {
      manager.sendMessage("PRIVMSG " + thisChannel + " :" + input);
      // using the local method below because of logging
      appendMessage("\n<" + nick + "> " + input);
    }


  // implementing the NicknameListener interface
  public void nickChanged(String newNick)
    {
      replacePerson(nick, newNick);
      nick = newNick;
    }


  //_____________________________________________________________________________________________
	// functional methods


	/**
	  * Adds a nickname to the list of people in alphabetical order.
	  * Called when somebody joins.
	  * @param userName the nickname to be added
	  */
	public void addToPeople(String userName)
		{
			if (userName == null || userName.trim().equals("")) return;
			if (the_people.size() == 0) the_people.addElement(userName);
			else if (userName.compareTo(the_people.lastElement()) > 0) the_people.addElement(userName);
			else
				{
					// Check if name is already present
					for (int k = the_people.size(); k > 0; --k) if (userName.equals(the_people.elementAt(k - 1))) return;
					// Name is not in list. Add it.
					int lower = 0;
					int upper = the_people.size() - 1;
					int mid = 0;
					while (lower < upper)
						{
							mid = (lower + upper) / 2;
							if (userName.toLowerCase().compareTo((the_people.elementAt(mid)).toLowerCase()) > 0) lower = mid + 1;
							else upper = mid;
						}
					the_people.insertElementAt(userName, lower);
				}
		}


	/**
	  * Called by method setPeopleOfChannel() in class NovaManager.
	  * Used to check if the user is the creator and operator of the channel.
	  */
	public void checkOpStatus()
		{
			if ((the_people.size() == 1) && (the_people.firstElement().charAt(0) != '@'))
			{
				the_people.setElementAt("@" + nick, 0);
			}
		}


	/**
	  * Removes a nickname from the list of people in the channel.
	  * Called when somebody leaves the channel or quits IRC.
	  * Also recognizes privileged users.
	  * @param userName the nickname to be removed
	  * @return true if the name to be removed was found in the list, false otherwise
	  */
	public boolean removeFromPeople(String userName)
		{
			String temp = "";
			try
				{
					for (int k = 0; k < the_people.size(); ++k)
						{
							temp = the_people.elementAt(k);
							if (temp.equals(userName) || temp.equals("@" + userName) || temp.equals("+" + userName))
								{
									the_people.removeElementAt(k);
									return true;
								}
						}
					return false;
				}
			catch (ArrayIndexOutOfBoundsException aioobe) {return false; }
		}


	/**
	  * Called when somebody changes nickname.
	  * Preserves the position of the old nickname.
	  * Recognizes privileged users.
	  * @param oldName the nickname to be changed
	  * @param newName the new nickname
	  */
	public boolean replacePerson(String oldName, String newName)
		{
			String temp = "";
			for (int j = 0; j < the_people.size(); ++j)
				{
					temp = the_people.elementAt(j);
					if (temp.equals(oldName))
						{
							the_people.setElementAt(newName, j);
							if (oldName.equals(nick)) nick = newName;
							return true; // no further search required
						}
					else if (temp.equals("@" + oldName))
						{
							the_people.setElementAt("@" + newName, j);
							if (oldName.equals(nick)) nick = newName;
							return true; // no further search required
						}
					else if (temp.equals("+" + oldName))
						{
							the_people.setElementAt("+" + newName, j);
							if (oldName.equals(nick)) nick = newName;
							return true; // no further search required
						}
				}
			return false;
		}


	/**
	  * Used to op a person, i.e. attaches an '@' sign in front of the name if it's not done already.
	  * @param name the nickname of the person to be opped
	  */
	public void opPerson(String name)
		{
			int limit = the_people.size();
			for (int j = 0; j < limit; ++j)
				{
					if ((the_people.elementAt(j)).equals(name))
						{
							the_people.setElementAt('@' + name, j);
							break; // no further search necessary
						}
				}
		}


	/**
	  * Removes the '@' prefix from a person if it's there.
	  * @param name the nickname of the person to be de-opped
	  */
	public void deopPerson(String name)
		{
			int limit = the_people.size();
			for (int j = 0; j < limit; ++j)
				{
					if ((the_people.elementAt(j)).equals('@' + name))
						{
							the_people.setElementAt(name, j);
							break; // no further search necessary
						}
				}
		}


	/**
	  * Used to give voice to a person in a moderated channel.
	  * Attaches a '+' sign in front of the name if it's not done already.
	  * @param name the nickname of the person to be given voice
	  */
	public void giveVoice(String name)
		{
			int limit = the_people.size();
			for (int j = 0; j < limit; ++j)
				{
					if ((the_people.elementAt(j)).equals(name))
						{
							the_people.setElementAt('+' + name, j);
							break; // no further search necessary
						}
				}
		}


	/**
	  * Removes the '+' sign from a person if it's there.
	  * @param name the nickname of the person to be silenced
	  */
	public void takeVoice(String name)
		{
			int limit = the_people.size();
			for (int j = 0; j < limit; ++j)
				{
					if (the_people.elementAt(j).equals('+' + name))
						{
							the_people.setElementAt(name, j);
							break; // no further search necessary
						}
				}
		}


	/**
	  * Appends a message, with its style, to the main text area.
	  * Used to show messages coming from other users and messages of the program.
	  * Text is not automatically appended at a new line. That is left to the user.
	  * @param newIncoming the message to be appended
	  * @param style the style (color, font, etc.) of the message
	  */
	public void appendMessage(String newIncoming, Style style)
		{
			Document doc = mainArea.getDocument();
      // check if the user's nickname appears in the text
      if ((newIncoming.indexOf(" " + nick + " ") >= 0) || (newIncoming.indexOf(nick + ": ") == 0))
        {
          appendMessage(" ", NovaTextStyles.getMicroIcon1());
        }
			try {doc.insertString(doc.getLength(), newIncoming, style); }
			catch (BadLocationException ble) { /* ignore */ }
			if (logging && logWriter != null)
				{
					try
						{
							logWriter.write(newIncoming);
							logWriter.newLine();
						}
					catch (IOException ioe)
						{
							stopLogger();
							appendMessage("\n");
							appendMessage(" ", NovaTextStyles.getMicroIcon1());
							appendMessage(" ERROR occured while writing to log file. Logging stopped.", NovaTextStyles.getNovaStyle());
							//mainArea.append("ERROR occured while writing to log file.\nStopping logging action.\n");
				    }
        }
      mainArea.setCaretPosition(doc.getLength());
		}


	/**
	  * Appends a message, in default text style, to the main text area.
	  * Color-coded messages (according to mIRC format) are handled here (with iterative method calls).
	  * Used to show messages coming from other users and messages of the program.
	  * Text is not automatically appended at a new line. That is left to the user.
	  * @param newIncoming the message to be appended
	  */
	public void appendMessage(String newIncoming)
		{
			// first check if text is color-coded
			int k = newIncoming.indexOf(ctrlC);
			if (k == 0) // there is a '^C' at the beginning
				{
					if (newIncoming.length() == 1) return;
					int N, M;
					try // foreground color specified?
						{
							N = Integer.valueOf(newIncoming.substring(k + 1, k + 3)).intValue();
						}
					catch (Exception e) // no foreground color specified
						{
							newIncoming = newIncoming.substring(1);
							int l = newIncoming.indexOf(ctrlC);
							if (l == -1) appendMessage(newIncoming, NovaTextStyles.getChatStyle());
							else
								{
									appendMessage(newIncoming.substring(0, l), NovaTextStyles.getChatStyle());
									appendMessage(newIncoming.substring(l));
								}
							return;
						}
					try // background color specified?
						{
							if (newIncoming.charAt(k + 3) == ',')
								M = Integer.valueOf(newIncoming.substring(k + 4, k + 6)).intValue();
							else throw new Exception();
						}
					catch (Exception e) // no background color specified
						{
							newIncoming = newIncoming.substring(k + 3);
							int l = newIncoming.indexOf(ctrlC);
							if (l == -1) appendMessage(newIncoming, NovaTextStyles.makeCustomColoredStyle(NovaTextStyles.getmIRCColor(N)));
							else
								{
									appendMessage(newIncoming.substring(0, l), NovaTextStyles.makeCustomColoredStyle(NovaTextStyles.getmIRCColor(N)));
									appendMessage(newIncoming.substring(l));
								}
							return;
						}
					// foreground and background colors are given
					/*
					 * ATTENTION:
					 * PROBLEM WITH javax.swing.text.StyleConstants.setBackground(Style, Color)
					 * HERE, THE BACKGROUND COLOR IS PARSED BUT NOT SHOWN
					 */
					newIncoming = newIncoming.substring(k + 6);
					int l = newIncoming.indexOf(ctrlC);
					if (l == -1) appendMessage(newIncoming, NovaTextStyles.makeCustomColoredStyle(NovaTextStyles.getmIRCColor(N)/*, NovaTextStyles.getmIRCColor(M)*/));
					else
						{
							appendMessage(newIncoming.substring(0, l), NovaTextStyles.makeCustomColoredStyle(NovaTextStyles.getmIRCColor(N)/*, NovaTextStyles.getmIRCColor(M)*/));
							appendMessage(newIncoming.substring(l));
						}
				}
			else if (k > 0) // there is a '^C' but not at the beginning
				{
					appendMessage(newIncoming.substring(0,k), NovaTextStyles.getChatStyle());
					appendMessage(newIncoming.substring(k));
				}
			else appendMessage(newIncoming, NovaTextStyles.getChatStyle()); // no color coding for this string
      mainArea.setCaretPosition(doc.getLength());
		}


	/** @return the name of the channel represented by this window */
	public String getChannelName() {return thisChannel; }

	public String getHighlightedNick() {return highlightedNick; }

	/** Calls a function of the manager to send the JOIN command. */
	private void connectToChannel() {if (manager != null) manager.sendMessage("JOIN " + thisChannel); }


	private void giveColors()
		{
			NovaEnvironment n_env = manager.getEnvironment();
			Color a = n_env.getFirstBackColor();
			Color b = n_env.getFirstForeColor();
			Color c = n_env.getSecondBackColor();
			Color d = n_env.getSecondForeColor();
			setBackground(a);
			mainArea.setBackground(a);
			mainArea.setForeground(b);
			people.setBackground(a);
			people.setForeground(b);
			shortcut.setBackground(a);
			shortcut.setForeground(b);
		}


	private void startLogger()
		{
			JFileChooser fc = new JFileChooser();
			fc.showDialog(this, "Start Logging");
			try {logWriter = new BufferedWriter(new FileWriter(fc.getSelectedFile())); }
			catch (Exception e)
				{
					appendMessage("\n");
					appendMessage(" ", NovaTextStyles.getMicroIcon1());
					appendMessage(" ERROR while preparing log file. Chat will not be logged.", NovaTextStyles.getNovaStyle());
					return;
				}
			// change button image
			log.setText("Stop Logging!");
			log.setActionCommand("stop_log");
			// add one line to log file for identification
			String window_name = getTitle();
			window_name = window_name.substring(9, window_name.indexOf(" - topic:"));
			Calendar greg = new GregorianCalendar();
			String date = (greg.get(Calendar.DAY_OF_MONTH) + 1) + "." + (greg.get(Calendar.MONTH) + 1) + "." + greg.get(Calendar.YEAR);
			int hr = greg.get(Calendar.HOUR_OF_DAY);
			int min = greg.get(Calendar.MINUTE);
			String time = ((hr < 10) ? "0" : "") + hr + ":" + ((min < 10) ? "0" : "") + min;
			try
				{
					logWriter.write("Nova Log File - Channel: " + window_name + " - Date: " + date + " - Time: " + time);
					logWriter.newLine();
				}
			catch (IOException ignore) {}
			logging = true;
		}


	/**
	  * Stops the logging action, previously invoked by startLogger().
	  */
	private void stopLogger()
		{
			logging = false;
			log.setText("Start Logging...");
			log.setActionCommand("start_log");
			if (logWriter == null) return;
			try {logWriter.close(); }
			catch (IOException ioe) {appendMessage("Warning: Log file couldn't be closed properly.\n", NovaTextStyles.getNovaStyle()); }
		}


	/**
	  * Removes the heading operator (@) and moderation (+) indicators, if present.
	  * @param nick the nickname to be stripped off its header
	  */
	private static synchronized String strip(String nick)
		{
			if (nick.charAt(0) == '@' || nick.charAt(0) == '+') return nick.substring(1);
			else return nick;
		}


	//____________________________________________________________________________
	// TextLineInputDialog processes


	public void topicProcess(String input)
		{
			manager.sendMessage("TOPIC " + thisChannel + " :" + input);
		}

	public void inviteProcess(String input)
		{
			manager.sendMessage("INVITE " + input + " " + thisChannel);
		}

	public void modeProcess(String input)
		{
			manager.sendMessage("MODE " + thisChannel + " " + input);
		}

	//_____________________________________________________________________________


	/**
	  * Makes a clean removal of this chat window.
	  */
	public void shutDown()
		{
			try {if (logging) logWriter.close(); }
			catch (IOException ignore) {}
			manager.sendMessage("PART " + thisChannel);
			manager.removeChatChannel(this);
			manager.getInterface().removeShortcutButton(shortcut);
			dispose();
		}
}
