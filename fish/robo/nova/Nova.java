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
  * Starts Nova and contains version information.
  * There can be one argument: The name of the environment file.
  * The default name of the environment file is "environment".
  * @author Kai Berk Oezer
  * @version June 2000
*/
public class Nova
{
  public static void main(String[] arguments)
    {
      // check Java version
      if (System.getProperty("java.version").compareTo("1.2") < 0)
        {
          System.out.print("\nNova needs Java 2 version 1.2 or better.\nThe running version is " + System.getProperty("java.version") + ".\nPlease visit java.sun.com to upgrade.");
          return;
        }
      try {new NovaManager(arguments[0]); }
      catch (ArrayIndexOutOfBoundsException aioobe) {new NovaManager("environment"); }
    }

  public static String title = "Nova 1.5J2-beta";
  public static String version = "20000702";
  public static String authors = "Kai Oezer";
  public static String URL = "http://nova-irc.sourceforge.net/";
  public static String minimum = "Java 2 Standard Edition version 1.2";
  public static String license = "GNU General Public License sversion 3";
}
