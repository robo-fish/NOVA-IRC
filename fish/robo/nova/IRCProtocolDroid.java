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
  * Parses an IRC message to return data like the name of sender, numeric code of the command, etc.
  * Called mostly by class NovaManager.
  * @author Kai Berk Oezer
  */
public class IRCProtocolDroid
{
	/**
	  * It is assumed that this method is used to extract the sender's name.
	  * The leading semicolon (:) is omitted.
	  * @param IRCmessage the message to be parsed
	  * @return the leading part of the message up to the first space character or the message itself (whichever comes first).
	  */
	public static String getFirst(String IRCmessage)
		{
			if (IRCmessage == null) return null;
			else
				{
					int tmp = IRCmessage.indexOf(" ");
					if (tmp == -1) return IRCmessage;
					else return IRCmessage.substring(0, tmp);
				}
		}

	/**
	  * @param IRCmessage the message to be parsed
	  * @return the second part of the message, separated by space characters, or the last part (whichever comes first).
	  */
	public static String getSecond(String IRCmessage)
		{
			if (IRCmessage == null) return null;
			else
				{
					if (IRCmessage.indexOf(" ") == -1) return IRCmessage;
					else IRCmessage = IRCmessage.substring(IRCmessage.indexOf(" ") + 1);
					if (IRCmessage.indexOf(" ") == -1) return IRCmessage;
					else return IRCmessage.substring(0, IRCmessage.indexOf(" "));
				}
		}

	/**
	  * Use only for N > 2.
	  * @param IRCmessage the message to be parsed
	  * @param N the index specifying which part of the message, separated by space characters, is to be extracted.
	  * @return the Nth part or the last part of the message (whichever comes first).
	  */
	public static String getNth(String IRCmessage, int N)
		{
			if (IRCmessage == null) return null;
			else
				{
					for (int i = 0; i < N-1; ++i)
						{
							if (IRCmessage.indexOf(" ") == -1) return IRCmessage;
							else IRCmessage = IRCmessage.substring(IRCmessage.indexOf(" ") + 1);
						}
					if (IRCmessage.indexOf(" ") == -1) return IRCmessage;
					else return IRCmessage.substring(0, IRCmessage.indexOf(" "));
				}
		}

	/**
	  * This method cuts the parameter string into two halves starting at the Nth part and returns the ending half (including the Nth part).
	  * @param IRCmessage the string to be cut in two
	  * @param N the index of the part of the message, separated by space characters, where the message will be cut.
	  * @return the second half of the resulting string or null if the parameter string consisted of less than N parts
	  */
	public static String getRest(String IRCmessage, int N)
		{
			for (int i = 0; i < N-1; ++i)
				{
					if (IRCmessage.indexOf(" ") == -1) return null;
					else IRCmessage = IRCmessage.substring(IRCmessage.indexOf(" ") + 1);
				}
			return IRCmessage;
		}

	/**
	  * Extracts the nickname from the full user description of the sender.
	  * @param fullDescription the full user description of the sender, e.g. jimmy!jclark@srv1.bignet.com
	  */
	public static String extractNick(String fullDescription)
		{
			if (fullDescription == null) return null;
			int index = fullDescription.indexOf("!");
			// the full description is prefixed with a semicolon - remove it!
			if (index != -1) return fullDescription.substring(1, index);
			else return null;
		}
}
