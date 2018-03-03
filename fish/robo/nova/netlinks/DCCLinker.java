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
package fish.robo.nova.netlinks;

import java.io.*;
import java.net.*;
import fish.robo.nova.*;
import fish.robo.nova.guis.NovaTextStyles;

/**
  * Called by DCCManager to establish a DCC network connection for chatting or file transfer.
  * @author Kai Berk Oezer
  * @version June 1999
  */
public class DCCLinker extends Thread
{
	private DCCManager dccManager;
	private NovaManager manager;
	private Socket dccSocket;
	private DataOutputStream toChatPartner; // output stream to chat partner


	public DCCLinker(DCCManager dccManager, NovaManager manager)
		{
			this.dccManager = dccManager;
			this.manager = manager;
			dccManager.setLinker(this);
		}


	public void run()
		{
			if (dccManager.isChat()) runChatLink();
			else runFileLink();
		}


	/**
	  * Continuously listens for input from the other side.
	  * Displays incoming messages on user interface or relays incoming bytes to manager.
	  */
	private void runChatLink()
		{
			BufferedReader fromChatPartner = null; // input stream from chat partner
			DataInputStream byteFromChatPartner = null; // input byte stream from chat partner

			if (dccManager.isLocallyInitiated())
				{
					ServerSocket dccServerSocket;

					// set up DCC server
					try {dccServerSocket = new ServerSocket(0); }
					catch (Exception e)
						{
							dccManager.displayInternalMessage("Error: DCC CHAT server setup failed.");
							return;
						}
					// send PRIVMSG to notify partner over IRC
					dccManager.displayInternalMessage("Asking " + dccManager.getNickname() + " for DCC CHAT...");
					try {manager.sendMessage("PRIVMSG " + dccManager.getNickname() + " :\001DCC CHAT chat "
					                      + DCCManager.ip2dcc(dccServerSocket.getInetAddress().getLocalHost().getHostAddress())
										  + " " + Integer.toString(dccServerSocket.getLocalPort()) + "\001"); }
					catch (Exception e) {return; }
					// NOW WAIT FOR A CONNECTION
					try {dccSocket = dccServerSocket.accept(); }
					catch (IOException ioe) {return; }
					// server's job is finished -> shut down
					try {dccServerSocket.close(); }
					catch (IOException ioe) {if (dccManager.isChat()) dccManager.displayInternalMessage("WARNING: Local DCC server could not close."); }
				}
			else
				{
					try {dccSocket = new Socket(dccManager.getIPAddress(), dccManager.getPort()); }
					catch (UnknownHostException uhe)
						{
							dccManager.displayInternalMessage("Error: Your partner's address is incorrect.");
							dccSocket = null;
						}
					catch (IOException ioe)
						{
							dccManager.displayInternalMessage("Socket i/o error.");
							dccSocket = null;
						}
				}
			if (dccSocket == null) return;
			// create I/O streams
			try
				{
					toChatPartner = new DataOutputStream(dccSocket.getOutputStream());
					fromChatPartner = new BufferedReader(new InputStreamReader(dccSocket.getInputStream()));
					dccManager.displayInternalMessage("Connection established.");
				}
			catch (IOException ioe)
				{
					dccManager.displayInternalMessage("Error: Couldn't establish I/O to partner.");
					dccSocket = null;
				}
			// continuously wait for and process incoming messages
			String temp;
			try
				{
					while (true)
						{
							if ((temp = fromChatPartner.readLine()) == null) break;
							dccManager.displayIncomingMessage(temp);
						}
				}
			catch (IOException ioe) {}
			finally
				{
					dccManager.displayInternalMessage("CONNECTION TERMINATED");
					toChatPartner = null;
					fromChatPartner = null;
					dccSocket = null;
				}
		}


	/**
	  * Continuously listens for input from the other side.
	  * Displays incoming messages on user interface or relays incoming bytes to manager.
	  */
	private void runFileLink()
		{
			DataOutputStream toPartner; // network output stream
			DataInputStream byteFromPartner; // network input stream

			// get packet size
			int packet_size;
			try {packet_size = Integer.parseInt(manager.getEnvironment().getFirstTagLine("packet-size ").substring(12).trim()); }
			catch (NumberFormatException nfe) {packet_size = 512; }

			if (dccManager.isLocallyInitiated())
				{
					ServerSocket dccServerSocket;

					// set up DCC server
					try {dccServerSocket = new ServerSocket(0); }
					catch (Exception e) {return; }
					// send PRIVMSG to notify partner
					try {manager.sendMessage("PRIVMSG " + dccManager.getNickname() + " :\001DCC SEND " + dccManager.getFileName() + " "
					                      + DCCManager.ip2dcc(dccServerSocket.getInetAddress().getLocalHost().getHostAddress())
										  + " " + Integer.toString(dccServerSocket.getLocalPort()) + " "
										  + Long.toString(dccManager.getFileSize()) + "\001"); }
					catch (Exception e) {manager.displayMessage("\nERROR while trying to make a DCC connection."); }
					// NOW WAIT FOR A CONNECTION
					try {dccSocket = dccServerSocket.accept(); }
					catch (IOException ioe) {manager.displayMessage("\nERROR while waiting for connection."); }
					// server's job is finished -> shut down
					try {dccServerSocket.close(); }
					catch (IOException ioe) {manager.displayMessage("\nWARNING: Local DCC server could not close."); }
				}
			else
				{
					try {dccSocket = new Socket(dccManager.getIPAddress(), dccManager.getPort()); }
					catch (UnknownHostException uhe) {manager.displayMessage("\nERROR: DCC host unknown."); }
					catch (IOException ioe) {manager.displayMessage("\nERROR while trying to connect to DCC host."); }
				}
			if (dccSocket == null) return;
			try
				{
					toPartner = new DataOutputStream(new BufferedOutputStream(dccSocket.getOutputStream(), packet_size));
					byteFromPartner = new DataInputStream(new BufferedInputStream(dccSocket.getInputStream(), packet_size));
				}
			catch (IOException ioe)
				{
					if(ioe instanceof BindException) {manager.displayMessage("\nDCC Error: can't create socket to " + dccManager.getNickname(), NovaTextStyles.getNovaStyle()); }
					else if(ioe instanceof ConnectException) {manager.displayMessage("\nDCC Error: " + dccManager.getNickname() + " is not listening", NovaTextStyles.getNovaStyle()); }
					else if(ioe instanceof NoRouteToHostException) {manager.displayMessage("\nDCC Error: attempt to connect to " + dccManager.getNickname() + " has timed out", NovaTextStyles.getNovaStyle()); }
					else {manager.displayMessage("\nDCC Error: connection to " + dccManager.getNickname() + " failed", NovaTextStyles.getNovaStyle()); }
					return;
				}
			// At this point a network connection has been established.
			RandomAccessFile theFile = null;
			try
				{
					dccManager.makeFileInterface();
					long fs = dccManager.getFileSize();
					byte[] raw_data = new byte[packet_size];
					int num_of_bytes = 0;
					long total = 0;
					if (dccManager.isLocallyInitiated()) // SEND
						{
							// open file for reading
							try {theFile = new RandomAccessFile(dccManager.getAbsoluteFileName(), "r"); }
							catch (Exception e)
								{
									manager.displayMessage("\nDCC Error: could not open " + dccManager.getFileName(), NovaTextStyles.getNovaStyle());
									return;
								}
							if (dccManager.isResume()) theFile.seek(dccManager.getPosition());
							while(true)
								{
									// read data from file
									if ((num_of_bytes = theFile.read(raw_data)) == -1) break;
									total += num_of_bytes;
									/* if (total > fs) break; */
									// send data packet
									toPartner.write(raw_data, 0, num_of_bytes);
									toPartner.flush();
									// update progress indicator
									dccManager.updateFileInterface(total);
									// read acknowledgment (reuse variable "num_of_bytes" for that)
                                    // ATTENTION: Some IRC clients do not send acknowledgments.
									num_of_bytes = (int) byteFromPartner.readLong();
									if (num_of_bytes != total)
										{
											manager.displayMessage("\nDCC: Unexpected acknowledgment from " + dccManager.getNickname(), NovaTextStyles.getNovaStyle());
											continue;
										}
								}
						}
					else // GET
						{
							try {theFile = new RandomAccessFile(dccManager.getFileName(), "rw"); }
							catch (Exception e)
								{
									manager.displayMessage("\nDCC Error: could not open " + dccManager.getFileName(), NovaTextStyles.getNovaStyle());
									return;
								}
							if (dccManager.isResume()) theFile.seek(dccManager.getPosition());
							while(true)
								{
									// wait for incoming bytes, then write to file
									num_of_bytes = byteFromPartner.read(raw_data);
									if (num_of_bytes == -1) break;
									theFile.write(raw_data, 0, num_of_bytes);
									total += num_of_bytes;
									// update progress indicator
									dccManager.updateFileInterface(total);
									// send acknowledgment (high byte is sent first)
									toPartner.writeLong(total);
									toPartner.flush();
								}
						}
				}
			catch (Exception e) {manager.displayMessage("\nAn error has occured while " +
			                        (dccManager.isLocallyInitiated() ? "sending " : "getting ") + '\"' +
									dccManager.getFileName() + '\"' + (dccManager.isLocallyInitiated() ? " to " : " from ") +
									dccManager.getNickname() + '.', NovaTextStyles.getNovaStyle()); e.printStackTrace(); }
			try {if (theFile != null) theFile.close(); }
			catch (IOException ignore) {}
			try {if (toPartner != null) toPartner.close(); }
			catch (IOException e1) {e1.printStackTrace(); }
			try {if (byteFromPartner != null) byteFromPartner.close(); }
			catch (IOException e2) {e2.printStackTrace(); }
			try {if (dccSocket != null) dccSocket.close(); }
			catch (IOException e3) {e3.printStackTrace(); }
			dccManager.shutDown();
		}


	/**
	  * Sends a character string to the DCC CHAT partner.
	  * @param message the message to be sent
	  */
	public void sendMessage(String message)
		{
			// check validity of output stream then send message
			if (toChatPartner == null)
				{
					dccManager.displayInternalMessage("Error: No connection to partner.");
					return;
				}
			try {toChatPartner.writeBytes(message + "\r\n"); }
			catch (IOException ioe) {dccManager.displayInternalMessage("An error occured while sending your message."); }
		}

	public void shutDown()
		{
			try
				{
					if (dccSocket != null) dccSocket.close();
					if (toChatPartner != null) toChatPartner.close();
				}
			catch (IOException ioe) {/* ignore */}
		}
}
