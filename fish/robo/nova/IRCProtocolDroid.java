/*
IRCProtocolDroid.java
March 1998
*/

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
