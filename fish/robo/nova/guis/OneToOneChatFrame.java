/* OneToOneChatFrame.java */

package fish.robo.nova.guis;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import fish.robo.nova.*;

/**
  * Defines the user interface for DCC chats and private chats of Nova IRC.
  * @version June 1999
  * @author Kai Berk Oezer
  */
public class OneToOneChatFrame extends JInternalFrame implements ActionListener, NovaInputListener, InternalFrameListener, MouseListener, NicknameListener
{
	/** the main text area where all messages are displayed */
	private JTextArea main = null;
	/** the text field where the user types the outgoing text before sending it */
  private JScrollPane scroller;

	// logging-related objects
	private JPopupMenu popup;
	private JMenuItem log;
	private boolean logging = false;
	private BufferedWriter logWriter = null;
	private JButton shortcut;

	/** reference to the manager of the DCC environment */
	private DCCManager manager_dcc = null;
	/** reference to the manager of the IRC environment */
	private NovaManager manager_irc = null;
	/** indicator of the type of chat */
	private boolean isDCC;
	/** the nickname of the chat partner - used by the 'private chat' mode */
	private String partnerNick;
  /** nickname of user */
  private String nickname;
	/** the environment handler */
	private NovaEnvironment env;
	static final ImageIcon bigChatIcon = new ImageIcon("fish/robo/nova/images/chat_big.gif");
	static final ImageIcon smallChatIcon = new ImageIcon("fish/robo/nova/images/chat_small.gif");


	/**
	  * @param name the nickname of the chat partner
	  * @param manager the IRC managing class
	  */
	public OneToOneChatFrame(String nameOfPartner, NovaManager manager)
		{
			super(null, true, true, true, true);
			if (nameOfPartner == null) return;
			setTitle("private chat with ");
			manager.getInterface().addToInterface(this);
			manager_irc = manager;
			partnerNick = nameOfPartner;
      nickname = manager.getNick();
			isDCC = false;
			env = manager.getEnvironment();
			build();
		}



	/**
	  * @param nameOfPartner the nickname of the chat partner
	  * @param dcc_manager The calling class, responsible for coordinating communication and message displaying.
	  * @param manager The NovaManager of this IRC session. Needed primarily to hook this InternalFrame to NovaInterface.
	  */
	public OneToOneChatFrame(DCCManager dcc_manager, NovaManager manager)
		{
			super(null, true, true, true, true);
			setTitle("DCC chat with ");
			manager.getInterface().addToInterface(this);
			manager_dcc = dcc_manager;
			manager_irc = manager;
			partnerNick = dcc_manager.getNickname();
      nickname = manager.getNick();
			isDCC = true;
			env = manager.getEnvironment();
			build();
		}



	/** Builds the whole one-to-one-chat user interface */
	private void build()
		{
			if (nickname.charAt(0) == '@' || nickname.charAt(0) == '+') nickname = nickname.substring(1);
			setTitle(getTitle() + partnerNick);
			addInternalFrameListener(this);
			setFrameIcon(bigChatIcon);

			// main components of the chat screen
			main = new JTextArea();
			main.setEditable(false);
			main.setLineWrap(true);
			main.setWrapStyleWord(true);
			main.setCursor(Cursor.getDefaultCursor());
			main.setFont(new Font("SansSerif", Font.PLAIN, 14));
			main.addMouseListener(this);
			scroller = new JScrollPane(main);
			scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			log = new JMenuItem("Start Logging...");
			log.addActionListener(this);
			popup = new JPopupMenu();
			popup.add(log);

			getContentPane().add(scroller);
      //pack();
      setSize(350, 350);
			shortcut = manager_irc.getInterface().addShortcutButton(new JButton(partnerNick, smallChatIcon));
			shortcut.addActionListener(this);
			giveColors();
			setVisible(true);
      toFront();
		}



	//______________________________________________________________________________
	// functional methods



	/** @return the nickname of the chat partner */
	public String getNickname() {return partnerNick; }



	//______________________________________________________________________________
	// interface methods



  /** Intercepts mouse clicks on the buttons.	*/
  public void actionPerformed(ActionEvent ae)
    {
      if (ae.getSource() == shortcut) // NovaInterface toolbar button pressed
        {
          toFront();
          try {setSelected(true); }
          catch(java.beans.PropertyVetoException pve) {}
          manager_irc.getInterface().getInputBar().setInputListener(this);
        }
      else if (logging) stopLogger();
      else startLogger();
    }



  // Implementation of the NovaInputListener interface
  public void processInput(String input)
    {
      appendMessage("<" + nickname + "> " + input);
      if (isDCC) manager_dcc.sendOut(input);
      else manager_irc.sendMessage("PRIVMSG " + partnerNick + " :" + input);
    }


  /** Closes the interface window and exits the program. */
  public void internalFrameClosing(InternalFrameEvent ife)
    {
      shutDown();
    }

  public void internalFrameActivated(InternalFrameEvent we)
    {
      manager_irc.getInterface().getInputBar().setInputListener(this);
    }

  public void internalFrameClosed(InternalFrameEvent ife) {}
  public void internalFrameDeactivated(InternalFrameEvent ife) {}
  public void internalFrameDeiconified(InternalFrameEvent ife) {}
  public void internalFrameIconified(InternalFrameEvent ife) {}
  public void internalFrameOpened(InternalFrameEvent ife) {}



	public void mousePressed(MouseEvent me) {}
	public void mouseReleased(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}
	
	public void mouseClicked(MouseEvent me)
		{
			if (SwingUtilities.isRightMouseButton(me))
				if (me.getComponent() == main) popup.show(main, me.getX(), me.getY());
		}



  // implementing the NicknameListener interface
  public void nickChanged(String newNick)
    {
      nickname = newNick;
    }



  //_________________________________________________________________________________


	/**
	  * Appends a character string to the text shown in text area 'main'.
	  * @param message the String to be appended to the text area
	  */
	public void appendMessage(String message)
		{
			main.append(message + "\n");
      main.setCaretPosition(main.getText().length());
			if (logging && logWriter != null)
				{
					try
						{
							logWriter.write(message);
							logWriter.newLine();
						}
					catch (IOException ioe)
						{
							main.append("ERROR occured while writing to log file.\nStopping logging action.\n");
							stopLogger();
						}
				}
		}



	private void giveColors()
		{
			getContentPane().setBackground(env.getFirstBackColor());
			main.setForeground(env.getFirstForeColor());
			main.setBackground(env.getFirstBackColor());
			shortcut.setForeground(env.getFirstForeColor());
			shortcut.setBackground(env.getFirstBackColor());
		}



	private void startLogger()
		{
			JFileChooser fc = new JFileChooser();
			fc.showDialog(this, "Start Logging");
			try {logWriter = new BufferedWriter(new FileWriter(fc.getSelectedFile())); }
			catch (Exception e)
				{
					main.append("ERROR while preparing log file.\nChat will not be logged.\n");
					return;
				}
			Calendar greg = new GregorianCalendar();
			String date = (greg.get(Calendar.DAY_OF_MONTH) + 1) + "." + (greg.get(Calendar.MONTH) + 1) + "." + greg.get(Calendar.YEAR);
			int hr = greg.get(Calendar.HOUR_OF_DAY);
			int min = greg.get(Calendar.MINUTE);
			String time = ((hr < 10) ? "0" : "") + hr + ":" + ((min < 10) ? "0" : "") + min;
			try
				{
					logWriter.write("Nova Log File - chat with " + partnerNick + " - Date: " + date + " - Time: " + time);
					logWriter.newLine();
				}
			catch (IOException ignore) {}
			log.setText("Stop Logging!");
			logging = true;
		}



	private void stopLogger()
		{
			logging = false;
			log.setText("Start Logging...");
			if (logWriter == null) return;
			try {logWriter.close(); }
			catch (IOException ioe) {main.append("ERROR while closing log file."); }
		}


	/**
	  * Removes this private (or DCC) chat window from the IRC environment.
	  */
	public void shutDown()
		{
			try {if (logging) logWriter.close(); }
			catch (IOException ignore) {}
			if (isDCC) manager_dcc.shutDown();
			else manager_irc.removePrivateChat(this);
			manager_irc.getInterface().removeShortcutButton(shortcut);
			dispose();
		}
}
