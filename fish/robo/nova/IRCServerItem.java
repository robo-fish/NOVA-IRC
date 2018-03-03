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

/**
  * This class represents a data element for listing server objects.
  * It is used in class ConnectionDialog to list known chat servers.
  * Names must contain no space characters.
  * @author Kai Berk Oezer
  * @version June 2000
  */
public class IRCServerItem
{
	private String name;
	private String address;
	private int port;

	public IRCServerItem() {}

	public IRCServerItem(String name, String address, int port)
		{
			this.name = name;
			this.address = address;
			this.port = port;
		}

	public void setName(String newName) {name = newName; }
	public String getName() {return name; }

	public void setAddress(String newAddress) {address = newAddress; }
	public String getAddress() {return address; }

	public void setPort(int newPort) {port = newPort; }
	public int getPort() {return port; }
}
