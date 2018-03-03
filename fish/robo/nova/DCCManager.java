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

import fish.robo.nova.netlinks.DCCLinker;
import fish.robo.nova.guis.*;
import javax.swing.*;
import java.io.*;
import java.util.Vector;

/**
  * This class is used for DCC (Direct Client-to-Client) communication.
  * It stores all information needed for DCC CHAT, DCC SEND and DCC RESUME sessions.
  * @author Kai Berk Oezer
  * @version June 1999
  */
public class DCCManager
{
	private NovaManager manager;
	private DCCLinker linker;

	private String fileName, // name of the file to be transferred
	               absoluteFileName, // full pathname of the file
	               nickname, // the nickname of the DCC partner
	               ipAddress; // IP adress of the DCC partner

	private long fileSize, // size (in bytes) of the file to be transferred
	             position; // position in the file (in bytes) to resume transfer from; used for DCC Resume

	private int port; // the TCP port, dedicated for this transfer operation, at the other user's side

	private boolean chat, // indicates whether the constructed object is used for a CHAT session
	                resume, // indicates whether the constructed object is used for a RESUME session
	                locallyInitiated; // indicates whether DCC was initiated by the local user or the remote partner

	public static Vector<DCCManager> SENDRegister = new Vector<DCCManager>(); // all outgoing DCC SEND managers are registered here
	public static Vector<DCCManager> RESUMERegister = new Vector<DCCManager>(); // all outgoing DCC RESUME managers are registered here

	private OneToOneChatFrame chatInterface;
	private DCCProgressIndicator fileInterface;


	/**
	  * The constructor for locally initiated DCC CHAT sessions.
	  * @param nickname the nickname of the DCC chat partner
	  * @param ipAddress the IP adress of the DCC chat partner
	  * @param port the logical TCP port which is connected to the IRC application of the chat partner
	  */
	public DCCManager(String nickname, String ipAddress, int port, NovaManager manager)
		{
			this.nickname = nickname;
			this.ipAddress = ipAddress;
			this.port = port;
			this.manager = manager;
			chat = true;
			locallyInitiated = true;
		}


	/**
	  * The constructor for locally initiated DCC SEND sessions.
	  * Stored in the fish.robo.nova.NovaManager DCC register to be looked up when a DCC RESUME is received.
	  * @param nickname The nickname of the DCC partner.
	  * @param fileName The name of the file to be sent.
	  * @param ipAdress The IP address of the DCC partner.
	  * @param port The logical TCP port which is connected to the IRC application of the DCC partner.
	  * @param fileSize The size of the file to be sent. The unit is bytes (octets).
	  */
	public DCCManager(String nickname, String fileName, String ipAddress, int port, long fileSize, NovaManager manager)
		{
			this.nickname = nickname;
			this.fileName = fileName;
			this.ipAddress = ipAddress;
			this.port = port;
			this.fileSize = fileSize;
			this.manager = manager;
			chat = false;
			resume = false;
			locallyInitiated = true;
		}


	/**
	  * The constructor for locally initiated DCC RESUME requests.
	  * Stored in the fish.robo.nova.NovaManager DCC register to be looked up when a DCC ACCEPT is received
	  * @param nickname The nickname of the DCC partner.
	  * @param fileName The name of the file to be received.
	  * @param ipAdress The IP address of the DCC partner.
	  * @param port The logical TCP port which is connected to the IRC application of the DCC partner.
	  * @param fileSize The size of the file to be received. The unit is bytes (octets).
	  * @param position The position in the file, where transmission shall start from. The unit is bytes (octets).
	  */
	public DCCManager(String nickname, String fileName, String ipAddress, int port, long fileSize, long position, NovaManager manager)
		{
			this.nickname = nickname;
			this.fileName = fileName;
			this.ipAddress = ipAddress;
			this.port = port;
			this.fileSize = fileSize;
			this.position = position;
			this.manager = manager;
			chat = false;
			resume = true;
			locallyInitiated = false;
		}


	/**
	  * Called via fish.robo.nova.guis.NovaInterface to initiate a DCC CHAT or DCC SEND session.
	  * @param name the nickname of the DCC partner
	  * @param manager reference to the IRC managing object of type fish.robo.nova.NovaManager
	  * @param isChat true if DCC CHAT, false if DCC SEND is initiated
	  */
	public DCCManager(String name, NovaManager manager, boolean isChat)
		{
			nickname = name;
			this.manager = manager;
			chat = isChat;
			locallyInitiated = true;
			if (chat) chatInterface = new OneToOneChatFrame(this, manager);
			else
				{
					JFileChooser fc = new JFileChooser();
					fc.setDialogTitle("Choose a file to send...");
					fc.showOpenDialog(manager.getInterface());
					File chosenFile = fc.getSelectedFile();
					absoluteFileName = chosenFile.getAbsolutePath();
					fileName = chosenFile.getName();
					fileSize = chosenFile.length();
					SENDRegister.addElement(this);
				}
			(linker = new DCCLinker(this, manager)).start();
		}


	/**
	  * The constructor for incoming DCC sessions requests.
	  * This is a convenient constructor for use by fish.robo.nova.NovaManager
	  * @param nick the nickname of the DCC partner.
	  * @param allInOne the incoming DCC message (without the heading "DCC " string) as received from the IRC server.
	  * @param manager reference to the main IRC manager object
	  */
	public DCCManager(String nick, String allInOne, NovaManager manager)
		{
			this.manager = manager;
			nickname = nick;
			if (allInOne.indexOf("CHAT chat") == 0) // partner wants to chat with us
				{
					locallyInitiated = false;
					chat = true;
					ipAddress = dcc2ip(IRCProtocolDroid.getNth(allInOne, 3)); // extract IP address
					port = Integer.parseInt(IRCProtocolDroid.getNth(allInOne, 4)); // extract port number
					//Ask user
					new DCCPromptDialog(this, manager);
				}
			else if (allInOne.indexOf("SEND ") == 0) // partner wants to send us a file
				{
					locallyInitiated = false;
					chat = false;
					fileName = IRCProtocolDroid.getSecond(allInOne); // extract file name
					ipAddress = dcc2ip(IRCProtocolDroid.getNth(allInOne, 3)); // extract IP address
					port = Integer.parseInt(IRCProtocolDroid.getNth(allInOne, 4)); // extract port number
					fileSize = Long.parseLong(IRCProtocolDroid.getNth(allInOne, 5)); // extract file size (in bytes)
					new DCCPromptDialog(this, manager);
				}
			else if (allInOne.indexOf("RESUME ") == 0) // partner wants us to resume transmission from given position
				{
					locallyInitiated = true;
					chat = false;
					// look up DCC SEND registry to check if this response is valid
					int k = SENDRegister.size();
					if (k == 0) return; // erroneous DCC RESUME received
					port = Integer.parseInt(IRCProtocolDroid.getSecond(allInOne)); // extract port number
					boolean foundIt = false;
					/*
						According to mIRC, nickname and port number are sufficient for a unique identification.
						I don't quite agree, but I will follow that standard here.
					*/
					int i;
					for (i = 0; i < k; ++i)
						{
							if ((SENDRegister.elementAt(i)).getNickname().equals(nick))
								if ((SENDRegister.elementAt(i)).getPort() == port)
									{
										foundIt = true;
										break;
									}
						}
					if (foundIt)
						{
							DCCManager tmp = SENDRegister.elementAt(i);
							tmp.setResume(true);
							manager.sendMessage("PRIVMSG " + nickname + " :\001DCC ACCEPT " + allInOne.substring(7) + '\001');
							(new DCCLinker(tmp, manager)).start();
						}
				}
			else if (allInOne.indexOf("ACCEPT ") == 0) // partner accepts our request to resume transmission from given position
				{
					locallyInitiated = false;
					chat = false;
					resume = true;
					// look up the DCC RESUME register to get the partner's IP address and file size
					int k = RESUMERegister.size();
					if (k == 0) return; // erroneous DCC ACCEPT received
					port = Integer.parseInt(IRCProtocolDroid.getSecond(allInOne)); // extract port number
					boolean foundIt = false;
					/*
						According to mIRC, nickname and port number are sufficient for a unique identification.
						I don't quite agree, but I will follow that standard here.
					*/
					int i;
					for (i = 0; i < k; ++i)
						{
							if ((RESUMERegister.elementAt(i)).getNickname().equals(nick))
								if ((RESUMERegister.elementAt(i)).getPort() == port)
									{
										foundIt = true;
										break;
									}
						}
					if (foundIt)
						{
							DCCManager tmp = SENDRegister.elementAt(i);
							tmp.setResume(true);
							(new DCCLinker(tmp, manager)).start();
						}
				}
		}


	public String getNickname() {return nickname; }
	public String getFileName() {return fileName; }
	public void setFileName(String new_name) {fileName = new_name; }
	public String getAbsoluteFileName() {return absoluteFileName; }
	public void setAbsoluteFileName(String new_name) {absoluteFileName = new_name; }
	public long getFileSize() {return fileSize; }
	public long getPosition() {return position; }
	public int getPort() {return port; }
	public String getIPAddress() {return ipAddress; }
	public boolean isChat() {return chat; }
	public boolean isResume() {return resume; }
	public void setResume(boolean new_state) {resume = new_state; }
	public boolean isLocallyInitiated() {return locallyInitiated; }
	public javax.swing.JInternalFrame getInterface() {if (chat) return chatInterface; else return fileInterface; }
	public DCCLinker getLinker() {return linker; }
	public void setLinker(DCCLinker linker) {this.linker = linker; }


	/**
	  * Appends a line of text to the graphical chat interface.
	  * @param message the text to be appended to the graphical chat interface
	  */
	public void displayInternalMessage(String message) {if (chat) chatInterface.appendMessage(message); }


	/**
	  * Appends a line of text, coming from the chat partner, to the graphical chat interface.
	  * @param message the text to be appended to the graphical chat interface
	  */
	public void displayIncomingMessage(String message) {if (chat) chatInterface.appendMessage('<' + nickname + "> " + message); }


	/**
	  * Called by DCCLinker to create a progress indicator for file transfers.
	  * Creates a new fish.robo.nova.guis.DCCProgressIndicator
	  */
	public void makeFileInterface() {fileInterface = new DCCProgressIndicator(this, locallyInitiated, manager.getInterface()); }

	/**
	  * Update the file transfer interface to give the user progress information.
	  * @param transferred the total data amount transferred so far
	  */
	public void updateFileInterface(long transferred) {if (!chat) fileInterface.update(transferred); }

	/**
	  * Called by the graphical chat interface to send the typed message out to the chat partner.
	  * @param message the message to be sent to the chat partner
	  */
	public void sendOut(String message) {if (linker != null) linker.sendMessage(message); }

	/**
	  * Used to transform the IP adress from DCC format to normal (dotted) format
	  * It is an IPv4-specific procedure and won't work for IPv6
	  * @param adress the IP adress in DCC format (a block of numbers)
	  * @return the IP adress in normal (dotted) format
	  */
	public static synchronized String dcc2ip(String address)
		{
			long tmp;
			try {tmp = Long.valueOf(address).longValue(); }
			catch (NumberFormatException nfe)
				{
					System.out.println(nfe);
					return address;
				}
			String ipAddress = "";
			long part;
			for (short j = 0; j < 3; ++j)
				{
					part = tmp - ((tmp / 256) * 256); // extract the last three decimals
					ipAddress = "." + String.valueOf(part) + ipAddress;
					tmp = (tmp - part) / 256;
				}
			ipAddress = String.valueOf(tmp) + ipAddress;
			return ipAddress;
		}


	/**
	  * Used to transform the IP adress from normal (dotted) format to DCC format
	  * It is an IPv4-specific procedure and won't work for IPv6
	  * @param adress the IP adress in normal (dotted) format
	  * @return the IP adress in DCC format (a block of numbers)
	  */
	public static synchronized String ip2dcc(String address)
		{
			long tmp = 0;
			String part;
			int k;
			for (short j = 0; j < 3; ++j)
				{
					k = address.indexOf(".");
					part = address.substring(0, k);
					address = address.substring(k + 1);
					tmp = (tmp + Long.parseLong(part)) * 256;
				}
			tmp += Long.parseLong(address);
			return Long.toString(tmp);
		}


	/**
	  * Called by fish.robo.nova.guis.DCCPromptDialog.
	  * @param dccm the manager object of the DCC session
	  * @param nm the manager object of the IRC environment
	  */
	public static void startLinker(DCCManager dccm, NovaManager nm)
		{
			(new DCCLinker(dccm, nm)).start();
		}


	public void shutDown()
		{
			if (chatInterface != null) chatInterface.dispose();
			if (fileInterface != null) fileInterface.dispose();
			if (linker != null) linker.shutDown();
		}
}
