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

import java.net.*;
import java.io.*;
import fish.robo.nova.*;
import fish.robo.nova.guis.NovaTextStyles;

/**
  * This class provides an Identity Server (also known as "identd").
  * Used to connect to IRC servers that require this service.
  * @author Kai Berk Oezer
  * @version June 1999
  */
public class NovaIdentd extends Thread
{
	/** the local identity daemon */
	private ServerSocket identityServer;
	/** the manager of the IRC chat environment */
	private NovaManager manager;
	/** the TCP socket connecting to the IRC server */
	private Socket IRCServer = null;
	/** the input stream from the server */
	private BufferedReader fromIRCServer = null;
	/** used to indicate the status of this daemon */
	private boolean started = true;
	/** controls the status */
	private boolean up = true;

	/**
	  * Stores parameter and constructs a ServerSocket to start listening.
	  * The default port for the Identd server is 113.
	  * @param manager the manager of the whole IRC client
	  */
	public NovaIdentd(NovaManager manager)
		{
			this.manager = manager;
			try {identityServer = new ServerSocket(113); }
			catch (IOException ioe) {started = false; }
		}

	/**
	  * Continuously listens to the Identd port.
	  * When somebody connects a line of data is read and then the connection is closed.
	  * Then the Identd server waits for a new connection.
	  * This goes on until the Identd server is stopped by the manager.
	  */
	public void run()
		{
			if (!started)
				{
					manager.displayMessage(" ", NovaTextStyles.getMicroIcon1());
					manager.displayMessage(" WARNING: Identd server could not start! Another process could be using the same port.\n");
					return;
				}

			while(up)
				{
					try
						{
							// wait for someone to connect
							IRCServer = identityServer.accept();
							// build I/O streams
							fromIRCServer = new BufferedReader(new InputStreamReader(IRCServer.getInputStream()));
						}
					catch (IOException ioe)
						{
							manager.displayMessage("\nERROR: Could not establish communication with server.");
							cleanUp();
							return;
						}
					catch (ThreadDeath td)
						{
							cleanUp();
							throw(td);
						}

					// wait for incoming messages and process them
					try
						{
							// read one line and disconnect immediately
							String tempo = fromIRCServer.readLine();
							// manager.displayMessage("\nNova Identd Server receives: " + tempo + "\n");
						}
					catch (IOException ignore) {}
					finally {cleanUp(); }
				}
		}

	/** stops the Thread */
	public void cease() {up = false; }

	/** Closes input/output streams and the socket connection. */
	private void cleanUp()
		{
			try
				{
					if (fromIRCServer != null) fromIRCServer.close();
					if (IRCServer != null) IRCServer.close();
				}
			catch (IOException ignore) {}
		}
}
