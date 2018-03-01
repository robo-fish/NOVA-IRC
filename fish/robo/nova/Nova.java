/* Nova.java */

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
