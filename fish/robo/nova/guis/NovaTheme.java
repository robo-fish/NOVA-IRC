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

import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.Font;
import java.awt.Color;

/**
  * Color descriptions for Nova 1.5J2 Metal Look & Feel
  * 'Shades of Gray' is best scheme because it is neutral and does not interfere with the user's own Nova colors.
  * @author Kai Berk Oezer
  */
class NovaTheme extends MetalTheme
{
	private final FontUIResource controlFont = new FontUIResource("SansSerif", Font.PLAIN, 11);
	private final FontUIResource systemFont =  new FontUIResource("SansSerif", Font.PLAIN, 11);
	private final FontUIResource userFont =  new FontUIResource("SansSerif", Font.PLAIN, 11);
	private final FontUIResource smallFont = new FontUIResource("SansSerif", Font.PLAIN, 10);

	// active JInternalFrame frame + JLabel foreground
	private final ColorUIResource primary1 = new ColorUIResource(Color.black);
	// inactive JInternalFrame frame + component border lines
	private final ColorUIResource secondary1 = new ColorUIResource(160, 160, 160);
	// menu highlighting + button focus indicator + scrollbar
	private final ColorUIResource primary2 = new ColorUIResource(175, 176, 187);
	// active JInternalFrame title bar + text highlighting background
	private final ColorUIResource primary3 = new ColorUIResource(207, 207, 217);
	// inactive JInternalFrame title bar + dialog backgrounds
	private final ColorUIResource secondary3 = new ColorUIResource(206, 206, 206);
	// inactive menu item foreground
	private final ColorUIResource secondary2 = new ColorUIResource(187, 185, 174);

	//private final ColorUIResource titleInactiveBackground = new ColorUIResource(110, 110, 110);
	//private final ColorUIResource titleActiveBackground = new ColorUIResource(150, 150, 150);
	private final ColorUIResource titleInactiveForeground = new ColorUIResource(225, 225, 225);

	//__________________________________________________________________


	public String getName() {return "Nova 1.5J2 Metal Color Scheme"; }

	protected ColorUIResource getPrimary1() {return primary1; }
	protected ColorUIResource getPrimary2() {return primary2; }
	protected ColorUIResource getPrimary3() {return primary3; }

	protected ColorUIResource getSecondary1() {return secondary1; }
	protected ColorUIResource getSecondary2() {return secondary2; }
	protected ColorUIResource getSecondary3() {return secondary3; }

	public FontUIResource getControlTextFont() {return controlFont; }
	public FontUIResource getSystemTextFont() {return systemFont; }
	public FontUIResource getUserTextFont() {return userFont; }
	public FontUIResource getMenuTextFont() {return controlFont; }
	public FontUIResource getWindowTitleFont() {return controlFont; }
	public FontUIResource getSubTextFont() {return smallFont; }

	//public ColorUIResource getWindowTitleBackground() {return titleActiveBackground; }
	//public ColorUIResource getWindowTitleForeground() {return getWhite(); }
	//public ColorUIResource getWindowTitleInactiveBackground() {return titleInactiveBackground; }
	public ColorUIResource getWindowTitleInactiveForeground() {return titleInactiveForeground; }
}
