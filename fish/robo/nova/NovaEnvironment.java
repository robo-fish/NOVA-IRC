/* NovaEnvironment.java */

package fish.robo.nova;

import java.io.*;
import java.util.*;

/**
  * This class provides Nova with user-specific properties.
  * Many of the classes of Nova use those settings and store updated information here.
  * @version June 1999
  * @author Kai Berk Oezer
  */
public class NovaEnvironment
{
	private Vector<String> lines;
	private String name;
	private boolean revived = false;
	private java.awt.Color fb, ff, sb, sf;

	public NovaEnvironment(String fileName)
		{
			name = (fileName == null) ? "environment" : fileName;
			BufferedReader environmentReader = null;
			try {environmentReader = new BufferedReader(new FileReader(name)); }
			catch (FileNotFoundException fnfe) {revived = true; }
			lines = new Vector<String>();
			if (!revived)
				{
					String temp;
					try
						{
							while (true)
								{
									temp = environmentReader.readLine();
									if (temp == null) break;
									lines.add(temp.trim());
								}
							environmentReader.close();
						}
					catch (IOException ioe) {System.out.println("NOVA: Error while handling file: '" + name + "'."); }
				}
			else
				{
					// If the environment file can not be found in the Nova directory,
					// a new environment file will be created with the following entries:
					lines.add("[This File stores preferences. Created by " + Nova.title + " version " + Nova.version + ".]");
					lines.add("[PLEASE DO NOT ALTER THE CONTENT.]");
					lines.add(" ");
					lines.add("[window]");
					lines.add("position 0 0");
					lines.add("size 500 500");
					lines.add(" ");
					lines.add("[colors]");
					lines.add("FirstBack 214,212,207");
					lines.add("FirstFore 51,0,51");
					lines.add("SecondBack 45,100,132");
					lines.add("SecondFore 230,230,252");
					lines.add(" ");
					lines.add("[user information]");
					lines.add("user johnny <nopass> John Doe");
					lines.add("userinfo Enter information about yourself.");
					lines.add("invisible_on_connect false");
					lines.add(" ");
					lines.add("[dcc]");
					lines.add("packet-size 1024");
					lines.add(" ");
					lines.add("[favorite channels]");
					lines.add("favorites <empty>");
					lines.add(" ");
					lines.add("[known IRC servers]");
					lines.add("server TR-net-EU-TR-Ankara irc.dominet.com.tr 6667");
				}
			refreshColors();
		}

	/**
	  * Updates and closes the environment file.
	  * Called when Nova shuts down.
	  */
	public final void updateEnvironment()
		{
			if (!revived)
				{
					try {(new File(name)).delete(); }
					catch (SecurityException se) {System.out.println("NOVA: Environment file is protected. Can't update it."); return; }
				}
			BufferedWriter environmentWriter;
			try {environmentWriter = new BufferedWriter(new FileWriter(name)); }
			catch (IOException ioe)
				{
					System.out.println("NOVA Error: Couldn't write to file: " + name + "'.");
					return;
				}
			try
				{
					for (int k = 0; k < lines.size(); ++k)
						{
							environmentWriter.write((String) lines.elementAt(k));
							environmentWriter.newLine();
						}
				}
			catch (IOException ioe) {System.out.println("NOVA: Error while writing to file '" + name + "'."); }
			try {environmentWriter.close(); }
			catch (IOException ioe) {System.out.println("NOVA Warning: Could not close file: '" + name + "'."); }
		}

	/**
	  * adds a new line to the end of the file
	  * @param new_info the new line to be added
	  */
	public final void append(String new_info) {lines.add(new_info); }

	/**
	  * finds first line which starts with the given tag and returns it
	  * @param tag the tag that starts and identifies the searched text line
	  */
	public synchronized final String getFirstTagLine(String tag)
		{
			String temp = null;
			int limit = lines.size();
			int k = 0;
			for (; k < limit; ++k)
				{
					temp = (String) lines.elementAt(k);
					if (temp.indexOf(tag) == 0) break;
				}
			if ((k == limit - 1) && (temp.indexOf(tag) !=  0)) return null;
			else return temp;
		}

	/**
	  * lists all lines that start with the given tag and returns the list as a Vector
	  * @param tag the tag that starts and identifies the searched text line(s)
	  */
	public synchronized final Vector<String> getAllTagLines(String tag)
		{
			Vector<String> tagLines = new Vector<String>();
			String temp;
			int limit = lines.size();
			for (int k = 0; k < limit; ++k)
				{
					temp = lines.elementAt(k);
					if (temp.indexOf(tag) == 0) tagLines.add(temp);
				}
			return tagLines;
		}

	/**
	  * Finds first line that matches tag and replaces it with the given string.
	  * @param tag the tag that starts and identifies the searched text line
	  * @param new_line the new text line which will replace the found line
	  */
	public synchronized final void replaceTagLine(String tag, String new_line)
		{
			String temp;
			int limit = lines.size();
			for (int k = 0; k < limit; ++k)
				{
					temp = (String) lines.elementAt(k);
					if (temp.indexOf(tag) == 0)
						{
							lines.setElementAt(new_line, k);
							break;
						}
				}
		}


  /**
    * Removes all lines starting with a specific string (tag) and appends
      new lines to the list.
    * @parameter tag The string which marks the beginning of all the lines
      to be deleted.
    * @parameter replacement A Vector of strings which will be added to
      the list at its end.
    */
  public synchronized final void replaceAllTagLines(String tag, Vector<String> replacement)
    {
			String temp;
			for (int k = 0; k < lines.size(); ++k)
				{
					temp = lines.elementAt(k);
					if (temp.indexOf(tag) == 0) lines.removeElementAt(k--);
				}
      for (int k = 0; k < replacement.size(); k++) lines.add(replacement.get(k));
    }



	/**
	  * Finds first line that matches tag and deletes it.
	  * @param tag the tag that starts and identifies the searched text line
	  */
	public synchronized final void removeTagLine(String tag)
		{
			String temp;
			int limit = lines.size();
			for (int k = 0; k < limit; ++k)
				{
					temp = (String) lines.elementAt(k);
					if (temp.indexOf(tag) == 0)
						{
							lines.removeElementAt(k);
							break;
						}
				}
		}

	public final void refreshColors()
		{
			String temp;
			int sw, c1, c2, c3;
			int limit = lines.size();
			for (int k = 0; k < limit; ++k)
				{
					temp = (String) lines.elementAt(k);
					if (temp.indexOf("FirstBack ") == 0)
						{
							temp = temp.substring(10);
							sw = 0;
						}
					else if (temp.indexOf("FirstFore ") == 0)
						{
							temp = temp.substring(10);
							sw = 1;
						}
					else if (temp.indexOf("SecondBack ") == 0)
						{
							temp = temp.substring(11);
							sw = 2;
						}
					else if (temp.indexOf("SecondFore ") == 0)
						{
							temp = temp.substring(11);
							sw = 3;
						}
					else continue;
					try
						{
							c1 = Integer.parseInt(temp.substring(0, temp.indexOf(",")));
							temp = temp.substring(temp.indexOf(",") + 1);
							c2 = Integer.parseInt(temp.substring(0, temp.indexOf(",")));
							c3 = Integer.parseInt(temp.substring(temp.indexOf(",") + 1).trim());
							switch (sw)
								{
									case 0: fb = new java.awt.Color(c1, c2, c3); break;
									case 1: ff = new java.awt.Color(c1, c2, c3); break;
									case 2: sb = new java.awt.Color(c1, c2, c3); break;
									case 3: sf = new java.awt.Color(c1, c2, c3);
								}
						}
					catch (Exception e)
						{
							//reset to default colors
							fb = new java.awt.Color(64,95,164);
							fb = new java.awt.Color(255, 255, 255);
							fb = new java.awt.Color(255, 255, 0);
							fb = new java.awt.Color(0, 0, 0);
						}
				}
		}

	public synchronized java.awt.Color getFirstBackColor() {return fb; }
	public synchronized java.awt.Color getFirstForeColor() {return ff; }
	public synchronized java.awt.Color getSecondBackColor() {return sb; }
	public synchronized java.awt.Color getSecondForeColor() {return sf; }
}
