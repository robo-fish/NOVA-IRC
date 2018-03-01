/* NovaLinker.java */

package fish.robo.nova.netlinks;

import java.io.*;
import java.net.*;
import fish.robo.nova.*;

/**
  * This class provides the client-server connection for Nova.
  * @author Kai Berk Oezer
  * @version June 1999
  */
public class NovaLinker extends Thread
{
	/** the manager of the whole IRC client environment */
	private NovaManager manager;
	/** the TCP socket connecting to the IRC server */
	private Socket IRCServer = null;
	/** the input stream from the server */
	private BufferedReader fromIRCServer = null;
	/** the output stream to the server */
	private DataOutputStream toIRCServer = null;
	/** holds the IP address of the IRC server */
	private String serverAddress = "invalid_address";
	/** holds the port number of the IRC server */
	private int portNumber = 6667;
  /** password for the nick name */
  private String password = "<nopass>";
	/** holds the nickname of the user */
	private String nickname = "johnny";
	/** holds the real name of the user */
	private String realname = "John Doe";
	/** controls the link status: link to IRC server is alive as long as check_link is true */
	private boolean check_link;

	/** @param my_IRC_Manager the manager of the IRC environment */
	public NovaLinker(NovaManager my_IRC_Manager) {manager = my_IRC_Manager; }

	/**
	  * Connects to the IRC server.
	  * This method must be called after the server address and port number are set.
	  * It also waits for input from the server and relays it to the managers "incomingMessage" method.
	  */
	public void run()
		{
			try
				{
					// establish network connection and build I/O streams
					try
						{
							IRCServer = new Socket(serverAddress, portNumber);
							fromIRCServer = new BufferedReader(new InputStreamReader(IRCServer.getInputStream()));
							toIRCServer = new DataOutputStream(IRCServer.getOutputStream());
						}
					catch (UnknownHostException uhe)
						{
							display("\nError: Host " + serverAddress + " is unknown.");
							cleanUp();
							return;
						}
					catch (IOException ioe)
						{
							if (ioe instanceof BindException) display("\nERROR: Can't create a socket for connection!");
							else if (ioe instanceof ConnectException) display("\nERROR: " + serverAddress + "is not listening!");
							else display("\nERROR: Could not establish communication with " + serverAddress + "!");
							cleanUp();
							return;
						}
					display("\nConnection established. Logging in...");

					// login procedure
					try
						{
							// no passworded login supported for now
              if (!password.equals("<nopass>")) toIRCServer.writeBytes("PASS " + password + "\n");
							toIRCServer.writeBytes("NICK " + nickname + "\n");
							String localHost = IRCServer.getLocalAddress().getHostAddress();
							toIRCServer.writeBytes("USER " + nickname.toLowerCase() + " " + localHost + " " + serverAddress + " :" + realname + "\n");
							toIRCServer.flush();
						}
					catch (IOException ioe)
						{
							display("\nCommunications-error while trying to log in.");
							display("\nDisconnecting...");
							cleanUp();
							return;
						}

					// wait for incoming messages and process them
					check_link = true; // only a call to disconnect() can set it to false
					try
						{
							while(check_link)
								{
									String tempo = fromIRCServer.readLine();
									if (tempo != null) manager.translate(tempo);
								}
						}
					catch (IOException ignore) {display("\nError: Connection to server broke down!"); }
				}
			catch (ThreadDeath td)
				{
					cleanUp();
					throw(td);
				}
		}


	//_______________________________________________________________________________
	// property accessors


	public NovaManager getManager() {return manager; }

	public void setPort(int newPortNumber) {portNumber = newPortNumber; }

	public int getPort() {return portNumber; }

	public void setAddress(String newServerAddress) {serverAddress = newServerAddress; }

	public String getAddress() {return serverAddress; }

	public void setNickname(String newNick) {nickname = newNick; }

	public void setRealname(String newReal) {realname = newReal; }
  
  public void setPassword(String newPassword) {password = newPassword; }


	//_______________________________________________________________________________
	// functional methods


	/**
	  * Disconnects gracefully from the server.
	  * Then, all streams and the socket are closed.
	  */
	public void disconnect()
		{
			check_link = false;
			try {if (toIRCServer != null) toIRCServer.writeBytes("QUIT :leaving\r\n"); }
			catch (IOException ioe) {display("\nError! Could not disconnect properly."); }
			cleanUp();
		}


	/**
	  * Sends a message to the server.
	  * A CRLF (carriage return + line feed) is appended automatically.
	  * @param message the message to be sent to the IRC server
	  */
	public void sendOut(String message)
		{
			if (toIRCServer == null)
				{
					display("\n");
					display(" ", fish.robo.nova.guis.NovaTextStyles.getMicroIcon1());
					display(" ERROR: You are not connected to a server.");
					return;
				}
			try {toIRCServer.writeBytes(message + "\r\n"); }
			catch (IOException ioe) {display("\nError occured while sending message."); }
		}


	/**
	  * Displays text on the main IRC window.
	  * Used when errors occur or connections are established or closed.
	  * @param notice the text to be displayed
	  */
	private void display(String notice) {if (manager != null) manager.displayMessage(notice);	}


	/**
	  * Displays text, with given style, on the main IRC window.
	  * Used when errors occur or connections are established or closed.
	  * @param notice the text to be displayed
	  */
	private void display(String notice, javax.swing.text.Style s) {manager.displayMessage(notice, s); }


	/** Closes input/output streams and the socket connection to the server. */
	private void cleanUp()
		{
			try
				{
					if (fromIRCServer != null)
						{
							fromIRCServer.close();
							fromIRCServer = null;
						}
					if (toIRCServer != null)
						{
							toIRCServer.close();
							toIRCServer = null;
						}
					if (IRCServer != null)
						{
							IRCServer.close();
							IRCServer = null;
						}
					manager.hasDisconnectedFromServer();
				}
			catch (IOException ioe) {display("\nError! Could not close connection to server."); }
		}
}
