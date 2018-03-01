/*
IRCServerItem.java
March 1998
*/

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
