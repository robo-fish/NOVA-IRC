/* NovaInterface.java */

package fish.robo.nova.guis;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import fish.robo.nova.*;

/**
  * Defines the user interface and actions of the main Nova IRC window.
  * @version June 1999
  * @author Kai Berk Oezer
  */
public class NovaInterface extends JFrame implements ActionListener, WindowListener, ItemListener
{
  private StatusWindow status;

  private NovaHelp novaHelp;

  private int children = 1;

  /** the manager of the whole IRC client environment */
  private NovaManager manager;

  private JMenuItem[] menuItems = new JMenuItem[18];
  /*
     menu items: New, connection, dccchat, dccsend, newNick,
                 list, join, version, clientinfo, privchat,
                 favorites, about, eXit, help, identity,
                 colors, nameFilter, dccBlockSize
  */

  private JMenu[] menus = new JMenu[8];
  /* mainMenu, edit, clients, channels, ctcp, dcc, helpMenu, debug */

  private JMenuBar controlBar;

  private JCheckBoxMenuItem[] checkMenus = new JCheckBoxMenuItem[4];
  /* invisible, autoget, debug_on, debug_off */

  private JDesktopPane desktop;

  private JToolBar shortcutButtons;

  private NovaInputBar inputBar;

  private boolean IRCenabled = false, isConnected = false;

  /** used to call TextLineInputDialog boxes */
  final static Class[] string_class = {"".getClass() };

  /** the icon used by all Nova containers */
  public final static ImageIcon NovaIcon = new ImageIcon("fish/robo/nova/images/nova.gif");
  public final static ImageIcon smallNovaIcon = new ImageIcon("fish/robo/nova/images/nova_small.gif");

  public static final Font MenuFont = new Font("SansSerif", Font.PLAIN, 11);

  /** Builds the user interface. */
  public NovaInterface(NovaManager my_IRC_Manager)
    {
      // call constructor of the parent class Frame
      super("Nova 1.5J2-beta");
      javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(new NovaTheme()); // has effect as long as 'Metal' is the Java L&F
      manager = my_IRC_Manager;
      addWindowListener(this);
      makeMenus();
      makeShortcutButtons();
      layoutMenus();
      inputBar = new NovaInputBar();
      inputBar.setNick(manager.getNick());
      // determine invisibility
      String isInvisible = manager.getEnvironment().getFirstTagLine("invisible_on_connect ");
      isInvisible = IRCProtocolDroid.getSecond(isInvisible);
      if (isInvisible != null && isInvisible.equals("true")) checkMenus[2].setState(true);
      // set location
      String tempo = manager.getEnvironment().getFirstTagLine("position ");
      tempo = tempo.substring(9);
      int x_pos, y_pos;
      try
        {
          int m = tempo.indexOf(" ");
          x_pos = Integer.parseInt(tempo.substring(0, m));
          y_pos = Integer.parseInt(tempo.substring(m + 1).trim());
        }
      catch (Exception sie) {x_pos = 0; y_pos = 0; }
      setLocation(x_pos, y_pos);
      // set size - some variables are re-used
      tempo = manager.getEnvironment().getFirstTagLine("size ");
      tempo = tempo.substring(5);
      try
        {
          int n = tempo.indexOf(" ");
          x_pos = Integer.parseInt(tempo.substring(0, n));
          y_pos = Integer.parseInt(tempo.substring(n + 1).trim());
        }
      catch (Exception sie) {x_pos = 500; y_pos = 520; }
      setSize(x_pos, y_pos);
      // other frame properties
      setResizable(true);
      setJMenuBar(controlBar);
      setIconImage(NovaIcon.getImage());

      desktop = new JDesktopPane();
      desktop.setBorder(BorderFactory.createEtchedBorder(Color.black, Color.white));
      desktop.putClientProperty("JDesktopPane.dragMode", "outline"); // Internal frames can be dragged in outline mode only.
      JPanel internal = new JPanel();
      internal.setLayout(new BorderLayout());
      internal.add(desktop, "Center");
      internal.add(inputBar, "South");
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(internal, "Center");
      getContentPane().add(shortcutButtons, "South");
      status = new StatusWindow(manager, this);
      inputBar.setInputListener(status);
      desktop.add(status, JLayeredPane.DEFAULT_LAYER);
      try {status.setMaximum(true); }
      catch (Exception e) {}
      status.setVisible(true);
      giveColors();
    }


  private final void makeShortcutButtons()
    {
      shortcutButtons = new JToolBar(SwingConstants.HORIZONTAL);
      shortcutButtons.setFloatable(true);
      shortcutButtons.setBorderPainted(true);
      shortcutButtons.setBorder(BorderFactory.createEtchedBorder(Color.white, Color.black));
      shortcutButtons.setMargin(new Insets(10, 4, 4, 4));
      shortcutButtons.addSeparator();
    }


  private final void makeMenus()
    {
      menuItems[0] = new JMenuItem("New Nova");
      menuItems[1] = new JMenuItem("Connect...");
      menuItems[2] = new JMenuItem("Change Nickname...");
      menuItems[3] = new JMenuItem("List Channels");
      menuItems[3].setEnabled(false);
      menuItems[4] = new JMenuItem("Join Channel...");
      menuItems[4].setEnabled(false);
      menuItems[5] = new JMenuItem("About Nova");
      menuItems[6] = new JMenuItem("Exit");
      menuItems[7] = new JMenuItem("Nova Help");
      checkMenus[0] = new JCheckBoxMenuItem("On");
      checkMenus[0].addChangeListener(manager);
      checkMenus[1] = new JCheckBoxMenuItem("Off");
      checkMenus[1].setSelected(true);
      ButtonGroup debugging = new ButtonGroup();
      debugging.add(checkMenus[0]);
      debugging.add(checkMenus[1]);
      menuItems[8] = new JMenuItem("Identity...");
      menuItems[9] = new JMenuItem("Colors...");
      menuItems[10] = new JMenuItem("Channel Name Filter...");
      menuItems[11] = new JMenuItem("DCC Block Size...");
      checkMenus[2] = new JCheckBoxMenuItem("Invisible On Connect", false);
      checkMenus[2].addItemListener(this);
      checkMenus[3] = new JCheckBoxMenuItem("AutoGet", false);
      checkMenus[3].addItemListener(this);
      menuItems[12] = new JMenuItem("Chat...");
      menuItems[13] = new JMenuItem("Send...");
      menuItems[14] = new JMenuItem("private chat");
      menuItems[15] = new JMenuItem("Favorites");
      menuItems[16] = new JMenuItem("client version");
      menuItems[17] = new JMenuItem("client info");
      for (int k = 12; k < 18; k++) if (k != 15) menuItems[k].setEnabled(false);

      menus[0] = new JMenu("Main");
      menus[1] = new JMenu("Edit");
      menus[2] = new JMenu("Clients");
      menus[3] = new JMenu("Channels");
      menus[4] = new JMenu("CTCP");
      menus[5] = new JMenu("DCC");
      menus[6] = new JMenu("Help");
      menus[7] = new JMenu("Debugging");

      for (int k = 0; k < menuItems.length; k++)
        {
          menuItems[k].addActionListener(this);
          menuItems[k].setActionCommand("mi_ac" + k);
          menuItems[k].setFont(MenuFont);
        }
      for (int k = 0; k < checkMenus.length; k++) checkMenus[k].setFont(MenuFont);
      for (int k = 0; k < menus.length; k++) menus[k].setFont(MenuFont);
      controlBar = new JMenuBar();
      controlBar.setOpaque(true);
      controlBar.setFont(new Font("SansSerif", Font.BOLD, 16));
      controlBar.setBorder(BorderFactory.createEtchedBorder(Color.white, Color.black));
    }


  private final void layoutMenus()
    {
      menus[0].add(menuItems[0]);
      menus[0].add(menuItems[1]);
      menus[0].add(menuItems[2]);
      menus[0].addSeparator();
      menus[0].add(menuItems[6]);
      menus[7].add(checkMenus[0]);
      menus[7].add(checkMenus[1]);
      menus[6].add(menuItems[7]);
      menus[6].add(menus[7]);
      menus[6].add(menuItems[5]);
      menus[1].add(menuItems[8]);
      menus[1].add(menuItems[10]);
      menus[1].add(menuItems[9]);
      menus[1].add(menuItems[11]);
      menus[1].add(checkMenus[2]);
      menus[4].add(menuItems[16]);
      menus[4].add(menuItems[17]);
      menus[5].add(menuItems[12]);
      menus[5].add(menuItems[13]);
      menus[5].add(checkMenus[3]);
      menus[2].add(menus[5]);
      menus[2].add(menus[4]);
      menus[2].add(menuItems[14]);
      menus[3].add(menuItems[3]);
      menus[3].add(menuItems[4]);
      menus[3].add(menuItems[15]);
      controlBar.add(menus[0]);
      controlBar.add(menus[1]);
      controlBar.add(menus[3]);
      controlBar.add(menus[2]);
      controlBar.add(menus[6]);
      //controlBar.setHelpMenu(menus[6]);
    }

  //_______________________________________________________________________________
  // property accessors


  public NovaManager getManager() {return manager; }

  public NovaInputBar getInputBar() {return inputBar; }

  public boolean isIRCEnabled() {return IRCenabled; }


  //________________________________________________________________________________
  // LISTENER INTERFACES


  /** intercepts changes to invisibility mode */
  public void itemStateChanged(ItemEvent ie)
    {
      if (ie.getStateChange() == ItemEvent.SELECTED) manager.getEnvironment().replaceTagLine("invisible_on_connect ", "invisible_on_connect true");
      else manager.getEnvironment().replaceTagLine("invisible_on_connect ", "invisible_on_connect false");
    }


  /** Intercepts menu bar selections. */
  public void actionPerformed(ActionEvent ae)
    {
      String com = ae.getActionCommand();
      if (com.equals("mi_ac3")) {if (manager != null) manager.sendMessage("LIST"); }
      else if (com.equals("mi_ac1")) new ConnectionDialog(this, manager);
      else if (com.equals("mi_ac1!")) manager.disconnectFromServer();
      else if (com.equals("mi_ac10")) new NameFilterDialog(this);
      else if (com.equals("mi_ac8")) new IdentityDialog(this);
      else if (com.equals("mi_ac15")) new FavoritesDialog(this, manager);
      else if (com.equals("mi_ac9")) new ColorsDialog(this);
      else if (com.equals("mi_ac7")) showHelp();
      else if (com.equals("mi_ac5")) new AboutWindow((Frame)this);
      else if (com.equals("mi_ac6")) shutDown();
      else if (com.equals("mi_ac0")) new NovaManager(manager.getEnvironment());
      else try
        {
          if (com.equals("mi_ac4")) new TextLineInputDialog(this, "join a chat group", "group name:", "Join", getClass().getMethod("joinProcess", string_class));
          else if (com.equals("mi_ac12")) new TextLineInputDialog(this, "DCC Chat", "nickname: ", "Send Request", getClass().getMethod("dccChatInitProcess", string_class));
          else if (com.equals("mi_ac14")) new TextLineInputDialog(this, "IRC private chat", "nickname:", "Open", getClass().getMethod("privateChatProcess", string_class));
          else if (com.equals("mi_ac13")) new TextLineInputDialog(this, "DCC File Transfer", "nickname: ", "Send Request", getClass().getMethod("dccFileInitProcess", string_class));
          else if (com.equals("mi_ac2")) new TextLineInputDialog(this, "change nickname", "new nickname:", "Accept", getClass().getMethod("nickProcess", string_class));
          else if (com.equals("mi_ac16")) new TextLineInputDialog(this, "query version", "nickname:", "Send Query", getClass().getMethod("queryVersionProcess", string_class));
          else if (com.equals("mi_ac17")) new TextLineInputDialog(this, "query clientinfo", "nickname:", "Send Query", getClass().getMethod("queryClientInfoProcess", string_class));
          else if (com.equals("mi_ac11")) new TextLineInputDialog(this, "set DCC block size", "size:", "Set", getClass().getMethod("packetSizeProcess", string_class));
        }
      catch (NoSuchMethodException ignore) {}
    }


  /** Closes the interface window and exits the program. */
  public void windowClosing(WindowEvent we) {shutDown(); }

  public void windowClosed(WindowEvent we) {}
  public void windowDeiconified(WindowEvent we) {}
  public void windowIconified(WindowEvent we) {}
  public void windowOpened(WindowEvent we) {}
  public void windowDeactivated(WindowEvent we) {}
  public void windowActivated(WindowEvent we) {}



  //________________________________________________________________________________
  // FUNCTIONAL METHODS


  public void addToInterface(JInternalFrame newWindow)
    {
      desktop.add(newWindow, JLayeredPane.DEFAULT_LAYER /*(children++) + 6*/);
      newWindow.toFront();
      newWindow.grabFocus();
    }

  /**
    * Appends a character string to the text area in the status window.
    * It is synchronized so that multiple access from various objects is done in a controlled way.
    * @param message the text to be added
    */
  public synchronized void appendMessage(String message) {status.appendMessage(message); }

  /**
    * Appends a character string, with given style, to the text area in the status window.
    * It is synchronized so that multiple access from various objects is done in a controlled way.
    * @param message the text to be added
    * @param style the text style (color, font, etc.) of the message
    */
  public synchronized void appendMessage(String message, javax.swing.text.Style style) {status.appendMessage(message, style); }


  public final void giveColors()
    {
      status.giveColors();
      if (novaHelp != null) novaHelp.giveColors();
      NovaEnvironment ne = manager.getEnvironment();
      Color a = ne.getFirstBackColor();
      Color b = ne.getFirstForeColor();
      Color c = ne.getSecondBackColor();
      Color d = ne.getSecondForeColor();
      desktop.setBackground(c);
      controlBar.setBackground(a);
      for (int k = 0; k < menuItems.length; k++)
        {
          menuItems[k].setBackground(a);
          menuItems[k].setForeground(b);
        }
      for (int k = 0; k < checkMenus.length; k++)
        {
          checkMenus[k].setBackground(a);
          checkMenus[k].setForeground(b);
        }
      for (int k = 0; k < menus.length; k++)
        {
          menus[k].setBackground(a);
          menus[k].setForeground(b);
        }
      shortcutButtons.setBackground(a);
      shortcutButtons.setForeground(b);
      inputBar.setBackground(a);
      inputBar.setForeground(b);
      inputBar.setTextBackground(d);
      inputBar.setTextForeground(c);
    }


  public void setConnected(boolean cnnct)
    {
      isConnected = cnnct;
      if (isConnected)
        {
          menuItems[1].setActionCommand("mi_ac1!");
          menuItems[1].setText("Disconnect");
        }
      else
        {
          menuItems[1].setActionCommand("mi_ac1");
          menuItems[1].setText("Connect...");
        }
    }


  public void enableIRC()
    {
      IRCenabled = true;
      menuItems[12].setEnabled(true);
      menuItems[13].setEnabled(true);
      menuItems[14].setEnabled(true);
      menuItems[3].setEnabled(true);
      menuItems[4].setEnabled(true);
      menuItems[16].setEnabled(true);
      menuItems[17].setEnabled(true);
    }


  public void disableIRC()
    {
      IRCenabled = false;
      menuItems[12].setEnabled(false);
      menuItems[13].setEnabled(false);
      menuItems[14].setEnabled(false);
      menuItems[3].setEnabled(false);
      menuItems[4].setEnabled(false);
      menuItems[16].setEnabled(false);
      menuItems[17].setEnabled(false);
    }


  /** This method is called when the user exits the whole program. */
  private void shutDown()
    {
      Dimension dim = getSize();
      Point place = getLocation();
      // destroy window
      dispose();
      // save last window position into environment file
      manager.getEnvironment().replaceTagLine("position", "position " + String.valueOf(place.x) + " " + String.valueOf(place.y));
      manager.getEnvironment().replaceTagLine("size", "size " + String.valueOf(dim.width) + " " + String.valueOf(dim.height));
      // shut down whole application
      if (manager != null) manager.shutDown();
      else System.exit(0);
    }


  /**
    * Adds a shortcut button to the toolbar. When pressed, the corresponding internal frame is raised.
    * @param shortcut The button to be added.
    */
  public JButton addShortcutButton(JButton shortcut)
    {
      shortcut.setFont(MenuFont);
      return (JButton) shortcutButtons.add(shortcut);
    }


  /**
    * Removes a shortcut button to the toolbar.
    * @param shortcut The button to be removed.
    */
  public void removeShortcutButton(JButton shortcut)
    {
      shortcutButtons.remove(shortcut);
      shortcutButtons.repaint();
    }


  private final void showHelp()
    {
      //if (novaHelp == null) addToInterface(novaHelp = new NovaHelp("Nova Help", "fish/robo/nova/docs/NovaHelp.hs", manager));
      //else if (!novaHelp.isVisible()) novaHelp.activate();
      addToInterface(novaHelp = new NovaHelp("Nova Help", "fish/robo/nova/docs/NovaHelp.hs", manager));
      try {novaHelp.setMaximum(true); }
      catch (java.beans.PropertyVetoException pve) {}
    }


  public Dimension getMinimumSize()
  {
    return new Dimension(320, 280);
  }


  /* DISABLED SINCE VERSION 20000624 BECAUSE NON-STANDARD LOOK&FEELs ARE IMPLEMENTED BADLY
  private void setLAF(String lafType)
    {
      try
        {
          UIManager.setLookAndFeel(lafType);
          SwingUtilities.updateComponentTreeUI(this);
        }
      catch (Exception ex) {appendMessage("\nFailed loading new Look & Feel."); }
    }
  */


  //_______________________________________________________________________
  // TextLineInputDialog processes

  /** method which is called by TextLineInputDialog when changing the nickname */
  public void nickProcess(String input)
    {
      /*
      // change nickname in Nova environment
      NovaEnvironment environment = manager.getEnvironment();
      String tmp = environment.getFirstTagLine("user ");
      tmp = tmp.substring(5);
      try {tmp = tmp.substring(tmp.indexOf(" ")).trim(); }
      catch (StringIndexOutOfBoundsException ignore) {}
      environment.replaceTagLine("user ", "user " + input + " " + tmp);
      manager.changeUserNick();
      */
      manager.setNick(input.trim());
      manager.changeUserNick();
    }


  /** method which is called by TextLineInputDialog when wanting to join a channel */
  public void joinProcess(String input)
    {
      (new Thread(new ChannelFrame("channel: " + input + " - topic: ?", manager))).start();
    }


  public void privateChatProcess(String input)
    {
      manager.addPrivateChat(new OneToOneChatFrame(input, manager));
    }


  public void queryClientInfoProcess(String input)
    {
      manager.sendMessage("PRIVMSG " + input + " :\001CLIENTINFO\001");
    }


  public void queryVersionProcess(String input)
    {
      manager.sendMessage("PRIVMSG " + input + " :\001VERSION\001");
    }


  public void dccFileInitProcess(String input)
    {
      new DCCManager(input, manager, false);
    }


  public void dccChatInitProcess(String input)
    {
      new DCCManager(input, manager, true);
    }


  public void packetSizeProcess(String input)
    {
      manager.getEnvironment().replaceTagLine("packet-size ", "packet-size " + input);
    }

}
