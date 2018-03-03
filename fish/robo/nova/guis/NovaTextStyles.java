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
package fish.robo.nova.guis;

import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.awt.Color;

/**
  * Contains the text styles (colors, fonts, etc.) for some of Nova's text displays.
  * mIRC's (popular IRC client for MS Windows) color scheme is also included.
  * Used by the text displays in fish.robo.nova.guis.StatusWindow and fish.robo.nova.guis.ChannelFrame.
  * @author Kai Berk Oezer
  * @version June 1999
  */
public class NovaTextStyles
{
	private static StyleContext novaStyleContext;

	private static Style server,
	                       nova,
	                       custom,
	                       chat,
	                       highlight,
	                       action,
                         debug,
                         caret,
	                       micro1,
	                       micro2;


	public NovaTextStyles()
		{
			novaStyleContext = new StyleContext();

			// Nova's text styles
			//
			// #1: Messages from the server
			server = novaStyleContext.addStyle("Server Info", null);
			StyleConstants.setForeground(server, Color.blue);
			StyleConstants.setFontFamily(server, "DialogInput");
			StyleConstants.setFontSize(server, 12);
			//
			// #2: Messages from Nova (mainly error messages)
			nova = novaStyleContext.addStyle("Nova Info", null);
			StyleConstants.setForeground(nova, new Color(0, 128, 0));
			StyleConstants.setFontFamily(nova, "SansSerif");
			StyleConstants.setFontSize(nova, 12);
			//
			// #3: Custom colored text
			custom = novaStyleContext.addStyle("Custom", null);
			StyleConstants.setForeground(custom, Color.black);
			StyleConstants.setFontFamily(custom, "DialogInput");
			StyleConstants.setFontSize(custom, 12);
			//
			// #4: Default chat channel messages style
			chat = novaStyleContext.addStyle("Chat", null);
			StyleConstants.setForeground(chat, Color.black);
			StyleConstants.setFontFamily(chat, "SansSerif");
			StyleConstants.setFontSize(chat, 12);
			//
			// #5: Nickname highlighting style
			highlight = novaStyleContext.addStyle("Highlight", null);
			StyleConstants.setForeground(highlight, mirc06);
			StyleConstants.setBold(highlight, true);
			StyleConstants.setFontFamily(highlight, "SansSerif");
			StyleConstants.setFontSize(highlight, 12);
            //
			// #6: Ctcp ACTION style
			action = novaStyleContext.addStyle("Action", null);
			StyleConstants.setForeground(action, mirc10);
			StyleConstants.setFontFamily(action, "SansSerif");
			StyleConstants.setFontSize(action, 12);
            //
			// #7: Debug style
			debug = novaStyleContext.addStyle("Debug", null);
			StyleConstants.setForeground(debug, Color.red);
			StyleConstants.setFontFamily(debug, "MonoSpaced");
			StyleConstants.setFontSize(debug, 12);


			// Images that can be inserted into the text panes:
			// Use with a blank String (i.e., " ") in method javax.swing.text.Document.insertString
			//
			// #1: Nova micro icon
			micro1 = novaStyleContext.addStyle("Nova micro icon", null);
			StyleConstants.setIcon(micro1, new ImageIcon("fish/robo/nova/images/micro1.gif"));
			//
			// #2: Info micro icon
			micro2 = novaStyleContext.addStyle("Info micro icon", null);
			StyleConstants.setIcon(micro2, new ImageIcon("fish/robo/nova/images/micro2.gif"));
      //
      // #3: text caret
      caret = novaStyleContext.addStyle("Nova text caret", null);
      StyleConstants.setIcon(caret, new ImageIcon("fish/robo/nova/images/caret.gif"));
    }

	/**
	  * @param c foreground color of the custom style
	  */
	static synchronized public Style makeCustomColoredStyle(Color c)
		{
			StyleConstants.setForeground(custom, c);
			return custom;
		}

	/**
	  * @param fg foreground color of the custom style
	  * @param bg background color of the custom style
	  */
	static synchronized public Style makeCustomColoredStyle(Color f, Color b)
		{
			StyleConstants.setBackground(custom, b);
			StyleConstants.setForeground(custom, f);
			return custom;
		}

	static public Style getServerStyle() {return server; }
	static public Style getNovaStyle() {return nova; }
	static public Style getChatStyle() {return chat; }
	static public Style getHighlightStyle() {return highlight; }
	static public Style getActionStyle() {return action; }
  static public Style getDebugStyle() {return debug;}
	static public Style getMicroIcon1() {return micro1; }
	static public Style getMicroIcon2() {return micro2; }
  static public Style getCaret() {return caret; }


	/* Nova's implementation of mIRC's color coding format */
	public final static Color mirc00 = Color.white,
	                          mirc01 = Color.black,
	                          mirc02 = new Color(0, 0, 128), // Navy Blue
	                          mirc03 = Color.green,
	                          mirc04 = Color.red,
	                          mirc05 = new Color(139, 69, 19), // brown (also specified as 'maroon', but maroon doesn't look like brown to me)
	                          mirc06 = new Color(160, 32, 240), // purple
	                          mirc07 = new Color(255, 165, 0), // orange
	                          mirc08 = Color.yellow,
	                          mirc09 = new Color(144, 238, 144), // light green
	                          mirc10 = new Color(0, 139, 139), // teal (dark cyan)
	                          mirc11 = new Color(224, 255, 255), // light cyan
	                          mirc12 = Color.blue,
	                          mirc13 = Color.pink,
	                          mirc14 = Color.gray,
	                          mirc15 = Color.lightGray;

	/* Called by ChannelFrame.appendMessage(String) */
	public static synchronized Color getmIRCColor(int i)
		{
			Color c = null;
			// mIRC allows color numbers in mode 16
			switch (i % 16)
				{
					case 0: c = mirc00; break;
					case 1: c = mirc01; break;
					case 2: c = mirc02; break;
					case 3: c = mirc03; break;
					case 4: c = mirc04; break;
					case 5: c = mirc05; break;
					case 6: c = mirc06; break;
					case 7: c = mirc07; break;
					case 8: c = mirc08; break;
					case 9: c = mirc09; break;
					case 10: c = mirc10; break;
					case 11: c = mirc11; break;
					case 12: c = mirc12; break;
					case 13: c = mirc13; break;
					case 14: c = mirc14; break;
					case 15: c = mirc15;
				}
			return c;
		}
}
