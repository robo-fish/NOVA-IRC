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
package fish.robo.nova;

import java.util.*;
import fish.robo.nova.netlinks.*;
import fish.robo.nova.guis.*;
import javax.swing.event.*;

/**
  * Manages the whole IRC client environment.
  * Contains numerous methods which serve other classes of Nova.
  * Backbone class of the whole program.
  * @author Kai Berk Oezer
  * @version June 1999
  */
public class NovaManager implements ChangeListener
{
  private static int instanceCounter = 0;
  private NovaEnvironment environment;
  private NovaInterface mainScreen;
  private NovaLinker netLink;
  private NovaIdentd idServer;
  private Thread netThread;
  private Vector<ChannelFrame> chatChannels; // list of public chat channels
  private Vector<OneToOneChatFrame> privateChats; // list of one-to-one chat boxes
  private Vector<NicknameListener> nicknameListeners; // list of components to be notified of nick changes

  private boolean connected = false,
                    case_sensitive_filter = false,
                    debugging = false;

  private ChannelListFrame theList;
  private String filter, nickname;


  /** Instantiates and links the main components of the client. */
  public NovaManager(String environment_file_name)
    {
      this(new NovaEnvironment(environment_file_name));
    }


  /** Instantiates and links the main components of the client. */
  public NovaManager(NovaEnvironment env)
    {
      instanceCounter++;
      environment = env;

      new NovaTextStyles();
      mainScreen = new NovaInterface(this);
      mainScreen.setVisible(true);
      displayMessage(" ", NovaTextStyles.getMicroIcon2());
      displayMessage(" Java environment: " + System.getProperty("java.vendor") + " version " + System.getProperty("java.version") + "\n", NovaTextStyles.getServerStyle());

      netLink = new NovaLinker(this);

      chatChannels = new Vector<ChannelFrame>();
      privateChats = new Vector<OneToOneChatFrame>();
      nicknameListeners = new Vector<NicknameListener>();

      idServer = new NovaIdentd(this);
      idServer.start();

      nickname = environment.getFirstTagLine("user ").substring(5);
      nickname = nickname.substring(0, nickname.indexOf(" "));
    }



  //_____________________________________________________________________________
  // functional methods


  public String getNick() {return nickname; }

  public void setNick(String nick)
    {
      nickname = nick;
			// notify all NicknameListeners of change
			for (int i = 0; i < nicknameListeners.size(); ++i) ((NicknameListener) nicknameListeners.elementAt(i)).nickChanged(nickname);
    }

	public NovaEnvironment getEnvironment() {return environment; }


	/** @return the mask string which is used to list channels with special names */
	public String getNameFilter() {return filter; }


	public boolean getNameFilterSensitivity() {return case_sensitive_filter; }


	/** @param mask the new mask to be used to list channels whose name contains this string */
	public void setNameFilter(String filter, boolean sensitive)
		{
			if (filter == null) this.filter = null;
			else this.filter = sensitive ? filter : filter.toLowerCase();
			case_sensitive_filter = sensitive;
		}


	/** Used by function <b>translate()</b> to filter the list of channels. */
	private void addChannelToList(String newChannel)
		{

			if (filter == null) theList.addToList(newChannel);
			else
				{
					String nc = case_sensitive_filter ? newChannel : newChannel.toLowerCase();
					if (nc.indexOf(filter) != -1) theList.addToList(newChannel);
				}
		}


	/**
	  * Called by class <b>IRCListFrame</b> to stop listing channels.
	  * Only an active <b>IRCListFrame</b> can call this method.
	  */
	public void stopChannelList()
		{
			//theList.setTitle("Channel List - stopped");
			theList = null;
		}


	/**
    * Sends the new nickname to the IRC server.
    * Called by NovaInterface when new nickname is entered
      via menu item "Change Nickname".
    * @param newNick the new nickname of the user
    */
	public void changeUserNick()
		{
      // notify IRC server of change
			netLink.sendOut("NICK " + nickname);
		}


  public final void addNicknameListener(NicknameListener nl)
    {
      nicknameListeners.add(nl);
    }


	/** @return the main IRC window */
	public NovaInterface getInterface()
      {
        return mainScreen;
      }


	/**
	  * Shows the given text on the main IRC window.
	  * @param newMessage the message to be displayed
	  */
	public final void displayMessage(String newMessage)
      {
        mainScreen.appendMessage(newMessage);
      }


	/**
	  * Shows the given text, with given style, on the main IRC window.
	  * @param newMessage the message to be displayed
	  * @param style the text style (color, font, etc.) of the message
	  */
	public final void displayMessage(String newMessage, javax.swing.text.Style style)
      {
        mainScreen.appendMessage(newMessage, style);
      }


	/**
	  * Sends a message to the IRC server.
	  * @param message the message to be sent
	  */
	public void sendMessage(String messageToServer)
      {
        if (debugging) displayMessage("\nNOVADEBUG>>> " + messageToServer, NovaTextStyles.getDebugStyle());
        netLink.sendOut(messageToServer);
      }


	/**
	  * Adds a chat channel, represented by a ChannelFrame object, to the internal list of channels.
	  * @param newChatChannel the channel to be added to the internal list
	  */
	public void addChatChannel(ChannelFrame newChatChannel)
		{
			chatChannels.add(newChatChannel);
      nicknameListeners.add(newChatChannel);
			mainScreen.addToInterface(newChatChannel);
			newChatChannel.appendMessage(" ", NovaTextStyles.getMicroIcon2());
			newChatChannel.appendMessage(" Waiting for incoming messages...", NovaTextStyles.getServerStyle());
		}


	/**
	  * Removes a chat channel, represented by a ChannelFrame object, from the internal list.
	  * @param deadChatChannel the channel to be removed from the internal list
	  */
	public void removeChatChannel(ChannelFrame deadChatChannel)
      {
        chatChannels.remove(deadChatChannel);
        nicknameListeners.remove(deadChatChannel);
      }


	public void addPrivateChat(OneToOneChatFrame newChat)
      {
        privateChats.add(newChat);
        nicknameListeners.add(newChat);
      }


	public void removePrivateChat(OneToOneChatFrame deadChat)
      {
        privateChats.remove(deadChat);
        nicknameListeners.remove(deadChat);
      }


  /**
    * Connects to an IRC server and logs in.
    * @param address the IP address of the IRC server
    * @param port the port number of the IRC server
    * @param password the password (if used) for the nickname
    * @param nick the nickname of the user
    * @param realname the real name of the user
    */
  public void connectToServer(String address, int port, String password, String nick, String realname)
    {
      netLink.setAddress(address);
      netLink.setPort(port);
      netLink.setPassword(password);
      netLink.setNickname(nick);
      netLink.setRealname(realname);
      netThread = new Thread(netLink);
      netThread.start();
      mainScreen.setTitle(Nova.title + " " + address);
      mainScreen.setConnected(true);
      connected = true;
    }


	/** Closes the network connection to the IRC server. */
	public void disconnectFromServer()
		{
			displayMessage("\n");
			displayMessage(" ", NovaTextStyles.getMicroIcon1());
			displayMessage(" Disconnecting...", NovaTextStyles.getNovaStyle());
			connected = false;
			mainScreen.disableIRC();
    		netLink.disconnect();
		}


  /** Used by the network link class to notify the manager that the network link has been closed. */
  public void hasDisconnectedFromServer()
    {
      int i;
      for (i = 0; i < chatChannels.size(); ++i) ((ChannelFrame) chatChannels.elementAt(i)).shutDown();
      for (i = 0; i < privateChats.size(); ++i) ((OneToOneChatFrame) privateChats.elementAt(i)).shutDown();
      mainScreen.setTitle(Nova.title);
      mainScreen.setConnected(false);
      connected = false;
      displayMessage("\n");
      displayMessage(" ", NovaTextStyles.getMicroIcon1());
      displayMessage(" Connection terminated.", NovaTextStyles.getNovaStyle());
    }


  /** Used by the network link class to notify the manager that the network link has been set up. */
  private void connectionCompleted()
    {
      displayMessage("\nEND of MESSAGE OF THE DAY\n\n");
      mainScreen.enableIRC();
      if (IRCProtocolDroid.getSecond(environment.getFirstTagLine("invisible_on_connect ")).equals("true"))
        {
          String myNick = environment.getFirstTagLine("user ");
          myNick = IRCProtocolDroid.getSecond(myNick);
          sendMessage("MODE " + myNick + " +i");
        }
    }


  /** Closes the IRC connection, stops the identity server, and exits the Java runtime. */
  public void shutDown()
    {
      if (connected) netLink.disconnect();
      idServer.cease();
      environment.updateEnvironment();
      instanceCounter--;
      if (instanceCounter == 0) System.exit(0);
    }


	/**
	  * Used by other methods of this class to find a ChannelFrame object with the given channel name.
	  * In IRC, channel names are case-insensitive.
	  * @param channelName the name of the channel, e.g. "#hottub"
	  * @return the ChannelFrame object representing that channel or null if the channel was not found
	  */
	private final ChannelFrame findChannel(String channelName)
		{
			ChannelFrame temp;
			for (int i = 0; i < chatChannels.size(); ++i)
				{
					temp = (ChannelFrame) chatChannels.elementAt(i);
					if (channelName.equalsIgnoreCase(temp.getChannelName())) return temp;
				}
			return null;
		}


	/**
	  * Called by method translate() when a message coming from a channel is to be shown.
	  * Creates a private chat window if the message is not directed to any registered channel.
	  * @param incoming the incoming message from the IRC server
	  */
	private void relay(String incoming)
		{
			String senderNick = incoming.substring(1, incoming.indexOf("!"));
			String message = IRCProtocolDroid.getRest(incoming, 4).substring(1);
			if (message == null) return;
			ChannelFrame temp = findChannel(IRCProtocolDroid.getNth(incoming, 3));
			// check channels
			if (temp != null)
				{
          // It is required to write appendMessage("\n") separately because of the user nickname highlighting feature.
					// handle ACTIONs
					if (message.startsWith("ACTION "))
            {
              temp.appendMessage("\n");
              temp.appendMessage(senderNick + " " + message.substring(7), NovaTextStyles.getActionStyle());
            }
					// handle highlighted nicknames
					else if (temp.getHighlightedNick().equals(senderNick))
            {
              temp.appendMessage("\n");
              temp.appendMessage("<" + senderNick + "> " + message, NovaTextStyles.getHighlightStyle());
            }
					else
            {
              temp.appendMessage("\n");
              temp.appendMessage("<" + senderNick + "> " + message);
            }
				}
			// if no channel found, check private chats
			else
				{
					String the_nick = environment.getFirstTagLine("user ");
					the_nick = IRCProtocolDroid.getSecond(the_nick);
					if (!the_nick.equals(IRCProtocolDroid.getNth(incoming, 3))) return;
					OneToOneChatFrame tmp = null;
					for (int j = 0; j < privateChats.size(); ++j)
						{
							tmp = (OneToOneChatFrame) privateChats.elementAt(j);
							if (senderNick.equals(tmp.getNickname()))
								{
									tmp.appendMessage("<" + senderNick + "> " + message);
									return;
								}
						}
					// at this point no existing window has been found -> create new one
					tmp = new OneToOneChatFrame(senderNick, this);
					privateChats.addElement(tmp);
					tmp.appendMessage("<" + senderNick + "> " + message);
				}
		}


	/**
	  * Processes and shows an incoming notice on the main screen.
	  * @param incoming the whole NOTICE line coming from the server
	  */
	private void showNotice(String notice_line)
		{
			String sender_nick = IRCProtocolDroid.extractNick(IRCProtocolDroid.getFirst(notice_line));
			if (sender_nick == null || sender_nick.equals("")) displayMessage('\n' + IRCProtocolDroid.getRest(notice_line, 4).substring(1), NovaTextStyles.getServerStyle());
			else displayMessage('\n' + sender_nick + ": " + IRCProtocolDroid.getRest(notice_line, 4).substring(1), NovaTextStyles.getServerStyle());
		}


	/**
	  * displays an error message (resulting from a channel action) in the appropriate channel
	  * @param error_message name of channel in which error occured followed by an error message
	  */
	private void displayChannelError(String error_message)
		{
			ChannelFrame chan = findChannel(IRCProtocolDroid.getFirst(error_message));
			if (chan != null)
				{
					chan.appendMessage("\n");
					chan.appendMessage(" ", NovaTextStyles.getMicroIcon2());
					chan.appendMessage(" ERROR: " + IRCProtocolDroid.getRest(error_message, 2).substring(1), NovaTextStyles.getServerStyle());
				}
			else
				{
					displayMessage("\n");
					displayMessage(" ",NovaTextStyles.getMicroIcon2());
					displayMessage(" ERROR! " + error_message, NovaTextStyles.getServerStyle());
				}
		}


	/**
	  * Called by method translate() when the people of a channel are to be shown.
	  * @param incoming the incoming message from the IRC server
	  */
	private void setPeopleOfChannel(String incoming)
		{
			String peopleNames = IRCProtocolDroid.getRest(incoming, 7).trim();
			ChannelFrame temp = findChannel(IRCProtocolDroid.getNth(incoming, 5));
			if (temp != null)
				{
					String restTemp = peopleNames;
					while (true)
						{
							// from now on peopleNames is used to store one name only
							peopleNames = IRCProtocolDroid.getFirst(restTemp).trim();
							restTemp = IRCProtocolDroid.getRest(restTemp, 2);
							temp.addToPeople(peopleNames);
							if (restTemp == null) break;
						}
					temp.checkOpStatus();
				}
			else displayMessage("\nPeople in channel " + IRCProtocolDroid.getNth(incoming, 5) + " are: " + peopleNames + "\n");
		}


	/**
	  * Called by method translate() when somebody joins a channel the user is on.
	  * @param message the incoming message from the IRC server
	  */
	private void somebodyJoined(String message)
		{
			// remember, a user can join multiple channels with one command
			String current;
			ChannelFrame temp;
			String senderName = IRCProtocolDroid.extractNick(IRCProtocolDroid.getFirst(message));
			String allChannels = IRCProtocolDroid.getRest(message, 3).substring(1);

			while (allChannels != null)
				{
					current = IRCProtocolDroid.getFirst(allChannels);
					allChannels = IRCProtocolDroid.getRest(allChannels, 2);
					temp = findChannel(current);
					if (temp != null)
						{
							temp.addToPeople(senderName);
							temp.appendMessage('\n' + senderName + " has joined.", NovaTextStyles.getServerStyle());
						}
				}
		}


	/**
	  * Called by method translate() when somebody leaves a channel the user is on.
	  * @param message the incoming message from the IRC server
	  */
	private void somebodyLeft(String message)
		{
			// a user can only part from one channel with one command
			String senderName = IRCProtocolDroid.extractNick(IRCProtocolDroid.getFirst(message));
			ChannelFrame tmp = findChannel(IRCProtocolDroid.getNth(message, 3));
			if (tmp != null)
				{
					tmp.removeFromPeople(senderName);
					tmp.appendMessage('\n' + senderName + " has left.", NovaTextStyles.getServerStyle());
				}
		}


	/**
	  * Called by method translate() when somebody, who is on the same channel the user is, quits IRC.
	  * @param message the incoming message from the IRC server
	  */
	private void somebodyQuitted(String message)
		{
			// remove this person from all channels
			String senderName = IRCProtocolDroid.extractNick(IRCProtocolDroid.getFirst(message));
			String reason = IRCProtocolDroid.getRest(message, 3).substring(1);
			ChannelFrame tmp;
			for (int i = 0; i < chatChannels.size(); ++i)
				{
					tmp = (ChannelFrame) chatChannels.elementAt(i);
					if (tmp.removeFromPeople(senderName)) tmp.appendMessage('\n' + senderName + " has quit IRC.", NovaTextStyles.getServerStyle());
				}
		}


	/**
	  * Called by method translate() when the mode of a channel, which the user is on, changes.
	  * @param message the incoming message from the IRC server
	  */
	private void setChannelMode(String message)
		{
			ChannelFrame temp = findChannel(IRCProtocolDroid.getNth(message, 3));
			if (temp != null)
				{
					String senderName = IRCProtocolDroid.extractNick(IRCProtocolDroid.getFirst(message));
					String modeChange = IRCProtocolDroid.getRest(message, 4);
					temp.appendMessage('\n' + senderName + " changed mode to " + modeChange, NovaTextStyles.getServerStyle());

					// check which mode has been changed
					int pos = 0;
					if ((pos = modeChange.indexOf("+o ")) != -1) temp.opPerson(IRCProtocolDroid.getFirst(modeChange.substring(pos + 3)));
					else if ((pos = modeChange.indexOf("-o ")) != -1) temp.deopPerson(IRCProtocolDroid.getFirst(modeChange.substring(pos + 3)));
					else if ((pos = modeChange.indexOf("+v ")) != -1) temp.giveVoice(IRCProtocolDroid.getFirst(modeChange.substring(pos + 3)));
					else if ((pos = modeChange.indexOf("-v ")) != -1) temp.takeVoice(IRCProtocolDroid.getFirst(modeChange.substring(pos + 3)));

					// if there is no support for that mode we may display a note on the channel screen
					//else temp.appendMessage("This mode change is not recognized by Nova, i.e. no change of state will be reflected.");
				}
		}


	/**
	  * Called by method translate() when the topic of a channel, which the user is on, changes.
	  * @param message the incoming message from the IRC server
	  */
	private void setChannelTopic(String message, boolean first_time)
		{
			String newTopic = IRCProtocolDroid.getRest(message, first_time?5:4).substring(1);
			ChannelFrame temp = findChannel(IRCProtocolDroid.getNth(message, first_time?4:3));
			if (temp == null) return;
			String newTitle = temp.getTitle();
			newTitle = newTitle.substring(0, newTitle.indexOf("topic: ") + 7) + newTopic;
			temp.setTitle(newTitle);
			if (first_time) return;
			// re-using variable newTitle to inform user of the change
			newTitle = IRCProtocolDroid.extractNick(IRCProtocolDroid.getFirst(message));
			temp.appendMessage('\n' + newTitle + " has changed the topic.", NovaTextStyles.getServerStyle());
		}


	/**
	  * Called by method translate() when somebody on the same channel as the user changes his/her nickname.
	  * @param message the incoming message from the IRC server
	  */
	private void somebodyChangedNick(String message)
		{
			String oldNick = IRCProtocolDroid.extractNick(IRCProtocolDroid.getFirst(message));
			String newNick = IRCProtocolDroid.getNth(message, 3).substring(1);
			ChannelFrame tmp;
			for (int i = 0; i < chatChannels.size(); ++i)
				{
					tmp = (ChannelFrame) chatChannels.elementAt(i);
					if(tmp.replacePerson(oldNick, newNick))
						tmp.appendMessage('\n' + oldNick + " changed nickname to " + newNick, NovaTextStyles.getServerStyle());
				}
		}


	/**
	  * Called by method translate() when somebody in the same channel as the user kicks someone else.
	  * This is also how the user is notified that he/she has been kicked from a channel.
	  * @param message the incoming message from the IRC server
	  */
	private void somebodyKickedSomeone(String message)
		{
			ChannelFrame tmp = findChannel(IRCProtocolDroid.getNth(message, 3));
			if (tmp != null)
				{
					String kicker = IRCProtocolDroid.extractNick(IRCProtocolDroid.getFirst(message));
					String victim = IRCProtocolDroid.getNth(message, 4);
					tmp.removeFromPeople(victim);
					tmp.appendMessage('\n' + kicker + " kicked " + victim, NovaTextStyles.getServerStyle());
				}
		}


	/**
	  * Filters incoming CTCP-encoded messages.
	  * Implementation is as described in the CTCP coding protocol published by Klaus Zeuge on October 27th, 1991.
	  * It detects some of the frequently used CTCP commands and pocesses them.
	  * @param message the PRIVMSG message coming from the IRC server
	  */
	private void filterCTCP(String message)
		{
			// extract actual message
			String temp = (IRCProtocolDroid.getRest(message, 4)).substring(1);
			// do low-level de-quoting with the tag '\020'
			int pos = -1;
			try
				{
					while ((pos = temp.indexOf('\020', pos + 1)) != -1)
						{
							// Check character after tag and replace the tag and that character
							// with the appropriate single character if that character is '0' or 'n' or 'r'.
							if (temp.charAt(pos + 1) == 'n') temp = temp.substring(0, pos) + "\n" + temp.substring(pos + 2);
							else if (temp.charAt(pos + 1) == 'r') temp = temp.substring(0, pos) + "\r" + temp.substring(pos + 2);
							else if (temp.charAt(pos + 1) == '0') temp = temp.substring(0, pos) + "\000" + temp.substring(pos + 2);
							// Otherwise the tag is misplaced. Remove the tag.
							else temp = temp.substring(0, pos) + temp.substring(pos + 1);
						}
				}
			catch (StringIndexOutOfBoundsException sioobe) {sioobe.printStackTrace(); /* CORRECTION NEEDED */ }
			// now check if there is a CTCP command included in the message
			pos = 0;
			int pos2 = 0;
			String ctcp = null;
			while ((pos = temp.indexOf('\001')) != -1)
				{
					try
						{
							pos2 = temp.indexOf('\001', pos + 1);
							ctcp = temp.substring(pos + 1, pos2);
							temp = temp.substring(0, pos) + temp.substring(pos2 + 1);
						}
					catch (StringIndexOutOfBoundsException sioobe) {sioobe.printStackTrace(); break; /* CORRECTION NEEDED */ }
					// De-quote the tag '\134' + 'a' into '\001'.
					// That way, '\001' is allowed to appear inside the CTCP command
					int pos3 = -1;
					try
						{
							while ((pos3 = ctcp.indexOf('\134', pos3 + 1)) != -1)
								{
									if (temp.charAt(pos3 + 1) == 'a') ctcp = temp.substring(0, pos3) + "\001" + ctcp.substring(pos3 + 2);
									else ctcp = ctcp.substring(0, pos3) + ctcp.substring(pos3 + 1);
								}
						}
					catch (StringIndexOutOfBoundsException sioobe) {sioobe.printStackTrace(); /* CORRECTION NEEDED */ }
					// extract sender's nickname
					String nick = IRCProtocolDroid.extractNick(message);
					// check for mIRC's sound extension
					if (ctcp.indexOf("SOUND ") == 0)
						{
							String fileName = ctcp.substring(6);
							/* IMPLEMENTATION MISSING */
						}
					// check for DCC
					if (ctcp.indexOf("DCC ") == 0)
						{
							new DCCManager(nick, ctcp = ctcp.substring(4), this);
						}
					else if (ctcp.equals("VERSION"))
						{
							sendMessage("NOTICE " + nick + " :\001VERSION " + Nova.title + " version " + Nova.version + "\001");
						}
					else if (ctcp.equals("USERINFO"))
						{
							String info = environment.getFirstTagLine("userinfo ");
							if (info == null) info = "userinfo no user information available";
							sendMessage("NOTICE " + nick + " :\001USERINFO :" + info.substring(9) + "\001");
						}
					else if (ctcp.indexOf("PING ") == 0)
						{
							sendMessage("NOTICE " + nick + " :\001" + ctcp + '\001');
						}
					else if (ctcp.equals("CLIENTINFO"))
						{
							sendMessage("NOTICE " + nick + " :\001CLIENTINFO :Supported queries are VERSION, SOURCE, USERINFO, PING and ERRMSG.\001");
						}
					else if (ctcp.equals("SOURCE"))
						{
							sendMessage("NOTICE " + nick + " :\001SOURCE " + Nova.URL + "\001");
						}
					else if (ctcp.indexOf("ACTION ") == 0)
						{
							temp = ctcp + temp; // ACTIONs are processed in method relay(java.lang.String)
						}
					else if (ctcp.indexOf("ERRMSG ") == 0)
						{
							sendMessage("NOTICE " + nick + " :\001ERRMSG " + ctcp.substring(7) + ":Your client sent an error message but no error has occured.\001");
						}
					else sendMessage("NOTICE " + nick + " :\001ERRMSG " + ctcp + ":Query unknown or unrecognizable.\001");
				}
			// No CTCP commands left. Display rest of the de-coded message.
			if (temp.trim().equals("")) return;
			message = message.substring(0, message.indexOf(" :")) + " :" + temp;
			relay(message);
		}


	/**
	  * This method processes incoming commands and messages.
	  * Note: The if...else block is structured so that frequently used commands be checked first.
	  * @param incoming the whole message coming from the IRC server
	  */
	public synchronized void translate(String incoming)
		{
      if (debugging) displayMessage("\nNOVADEBUG<<< " + incoming, NovaTextStyles.getDebugStyle());
			String cache = IRCProtocolDroid.getSecond(incoming).toLowerCase();
			// translate incoming message into action
			if (cache.equals("privmsg")) filterCTCP(incoming);
			else if (cache.equals("notice")) showNotice(incoming);
			else if (cache.equals("322")) {if (theList != null) addChannelToList(IRCProtocolDroid.getRest(incoming, 4)); }
			else if (cache.equals("join")) somebodyJoined(incoming);
			else if (cache.equals("part")) somebodyLeft(incoming);
			else if (cache.equals("353")) setPeopleOfChannel(incoming);
			else if (cache.equals("372")) displayMessage("\n" + IRCProtocolDroid.getRest(incoming, 4).substring(1));
			else if (cache.equals("mode")) setChannelMode(incoming);
			else if (cache.equals("nick")) somebodyChangedNick(incoming);
			else if (cache.equals("321")) {if (theList != null) theList.shutDown(); theList = new ChannelListFrame(this); }
      else if (cache.equals("323")) {if (theList != null) {theList.complete(); theList = null; }}
			else if (cache.equals("quit")) somebodyQuitted(incoming);
			else if (cache.equals("kick")) somebodyKickedSomeone(incoming);
			else if (cache.equals("topic")) setChannelTopic(incoming, false);
			else if (cache.equals("332")) setChannelTopic(incoming, true);
			else if (incoming.toLowerCase().indexOf("ping") == 0) {sendMessage("PONG " + IRCProtocolDroid.getRest(incoming, 2)); displayMessage("\nPing - Pong");	}
      else if (cache.equals("433"))
        {
          displayMessage("\n");
			    displayMessage(" ", NovaTextStyles.getMicroIcon1());
          displayMessage("You're nickname is already used by somebody else. Choose a new one.", NovaTextStyles.getNovaStyle());
        }
      else if (cache.equals("436"))
        {
          displayMessage("\n");
			    displayMessage(" ", NovaTextStyles.getMicroIcon1());
          displayMessage("Nickname collision with other server. Choose a new nickname.", NovaTextStyles.getNovaStyle());
        }
      else if (cache.equals("442"))
        {
          String dummyString = IRCProtocolDroid.getRest(incoming, 4);
          dummyString = dummyString.substring(0, dummyString.indexOf(" "));
          ChannelFrame dummy = findChannel(dummyString);
          if (dummy != null)
            {
              dummy.appendMessage("\n");
              dummy.appendMessage(" ", NovaTextStyles.getMicroIcon1());
              dummy.appendMessage("You're not on this channel anymore.", NovaTextStyles.getNovaStyle());
            }
        }
			else if (cache.equals("467") || cache.equals("471") || cache.equals("473") || cache.equals("474") || cache.equals("475") || cache.equals("482")) displayChannelError(IRCProtocolDroid.getRest(incoming, 4));
			else if (cache.equals("375")) displayMessage("\nMESSAGE OF THE DAY\n");
			else if (cache.equals("376")) connectionCompleted();
			else if (cache.equals("394") || cache.equals("368") || cache.equals("366") || cache.equals("318") || cache.equals("369") || cache.equals("315")) return;
			// for now, all other messages are shown on main screen
			else
				{
					try
						{
							Integer.parseInt(cache);
							// numeric replies are shown in shortened form
							displayMessage("\n" + IRCProtocolDroid.getRest(incoming, 3));
						}
					// non-numeric replies are shown in original form
					catch (NumberFormatException nfe) {displayMessage("\n" + incoming); }
				}
		}


  public void stateChanged(ChangeEvent ce)
    {
      // check if the "Debugging" option has been changed
      if (ce.getSource() instanceof javax.swing.JCheckBoxMenuItem)
        {
          // debugging on or off?
          debugging = ((javax.swing.JCheckBoxMenuItem) ce.getSource()).isSelected();
        }
    }
}
