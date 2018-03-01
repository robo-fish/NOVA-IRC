/* NovaInputBar.java */

package fish.robo.nova.guis;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


/**
  * The main text input component shared by all windows.
  */
public class NovaInputBar extends JToolBar implements KeyListener, ActionListener
{
  private JLabel nick;
  private JPanel inputPanel, modifications;

  private JTextPane inputField;
  private Style inputStyle;

  private JToggleButton toggler, bold, underline;
  private JButton colors;
  private JPopupMenu colorPopup;
  private JMenuItem items[];
  private static final Color itemColors[] = {Color.black, Color.red, Color.blue, Color.green, Color.yellow, Color.cyan, Color.magenta};
  private Vector<Document> inputDocumentBuffer;
  private Vector<String> inputMessageBuffer;
  private String message;
  private int bufferIndex, caretPosition;
  private ImageIcon expandIcon = new ImageIcon("fish/robo/nova/images/expand.gif");
  private ImageIcon collapseIcon = new ImageIcon("fish/robo/nova/images/collapse.gif");
  private NovaInputListener inputListener;
  private static final Style caret = NovaTextStyles.getCaret();

  NovaInputBar()
    {
      setFloatable(true);
      setBorderPainted(true);
      setBorder(BorderFactory.createEtchedBorder(Color.white, Color.black));
      setMargin(new Insets(10, 4, 4, 4));
      //addSeparator();
      inputPanel = new JPanel();
      nick = new JLabel("dummy", SwingConstants.LEFT);
      modifications = new JPanel();
      modifications.setLayout(new GridLayout(1, 3));
      //modifications.add(plain = new JButton(new ImageIcon("fish/robo/nova/images/plain.gif")));
      //plain.setToolTipText("plain");
      //plain.addActionListener(this);
      modifications.add(bold = new JToggleButton(new ImageIcon("fish/robo/nova/images/bold.gif")));
      bold.setToolTipText("bold");
      bold.addActionListener(this);
      modifications.add(underline = new JToggleButton(new ImageIcon("fish/robo/nova/images/underline.gif")));
      underline.setToolTipText("underline");
      underline.addActionListener(this);
      modifications.add(colors = new JButton(new ImageIcon("fish/robo/nova/images/colors.gif")));
      colors.setToolTipText("colors");
      colors.addActionListener(this);
      colorPopup = new JPopupMenu();
      items = new JMenuItem[8];
      colorPopup.add(items[0] = new JMenuItem("default"));
      colorPopup.add(items[1] = new JMenuItem("black", new ImageIcon("fish/robo/nova/images/black.gif")));
      colorPopup.add(items[2] = new JMenuItem("red", new ImageIcon("fish/robo/nova/images/red.gif")));
      colorPopup.add(items[3] = new JMenuItem("blue", new ImageIcon("fish/robo/nova/images/blue.gif")));
      colorPopup.add(items[4] = new JMenuItem("green", new ImageIcon("fish/robo/nova/images/green.gif")));
      colorPopup.add(items[5] = new JMenuItem("yellow", new ImageIcon("fish/robo/nova/images/yellow.gif")));
      colorPopup.add(items[6] = new JMenuItem("cyan", new ImageIcon("fish/robo/nova/images/cyan.gif")));
      colorPopup.add(items[7] = new JMenuItem("magenta", new ImageIcon("fish/robo/nova/images/magenta.gif")));
      for (int k = 0; k < 8; ++k)
        {
          items[k].addActionListener(this);
          items[k].setActionCommand("" + k);
        }
      toggler = new JToggleButton(expandIcon);
      toggler.addActionListener(this);
      inputField = new JTextPane();
      inputField.setBorder(BorderFactory.createEtchedBorder(Color.white, Color.black));
      inputField.addKeyListener(this);
      inputField.setEditable(false);

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints constraints = new GridBagConstraints();
      inputPanel.setLayout(gridbag);
      constraints.anchor = GridBagConstraints.WEST;
      constraints.fill = GridBagConstraints.BOTH;
      constraints.gridx = 0;
      constraints.gridy = 0;
      constraints.weightx = 0.0;
      constraints.weighty = 1.0;
      constraints.gridheight = GridBagConstraints.REMAINDER;
      gridbag.setConstraints(nick, constraints);
      inputPanel.add(nick);
      constraints.gridx = 1;
      constraints.weightx = 1.0;
      gridbag.setConstraints(inputField, constraints);
      inputPanel.add(inputField);
      constraints.gridx = 2;
      constraints.weightx = 0.0;
      constraints.gridwidth = GridBagConstraints.RELATIVE;
      gridbag.setConstraints(modifications, constraints);
      //inputPanel.add(modifications); // IS ADDED AFTER STARTUP
      constraints.gridx = 3;
      constraints.gridwidth = GridBagConstraints.REMAINDER;
      gridbag.setConstraints(toggler, constraints);
      inputPanel.add(toggler);

      add(inputPanel);

      inputDocumentBuffer = new Vector<Document>();
      inputMessageBuffer = new Vector<String>();
      message = "";
      bufferIndex = -1;
      try {inputField.getDocument().insertString(caretPosition = 0, " ", caret); }
      catch (BadLocationException ble) {}
      inputStyle = (new StyleContext()).addStyle("input", null);
      StyleConstants.setForeground(inputStyle, Color.black);
      StyleConstants.setFontFamily(inputStyle, "DialogInput");
      StyleConstants.setFontSize(inputStyle, 12);
    }


  void setNick(String newNick)
    {
      nick.setText(newNick);
      validate();
    }


  void setInputListener(NovaInputListener listener)
    {
      inputListener = listener;
    }


  void setTextBackground(Color color)
    {
      inputField.setBackground(color);
    }


  void setTextForeground(Color color)
    {
      inputField.setForeground(color);
      StyleConstants.setForeground(inputStyle, color);
    }


  //_______________________________________________________________________
  // implementation of listener interfaces


  public void keyTyped(final java.awt.event.KeyEvent ke) {}
  public void keyReleased(final java.awt.event.KeyEvent ke) {}

  public void keyPressed(final java.awt.event.KeyEvent ke)
    {
      int code = ke.getKeyCode();
      if (code == KeyEvent.VK_ENTER)
        {
          // store document and message in buffer
          try
            {
              inputField.getDocument().remove(caretPosition, 1);
              inputDocumentBuffer.addElement(inputField.getDocument());
              inputMessageBuffer.addElement(message);
              // process message
              if (inputListener != null) inputListener.processInput(message);
              // clear the text field
              inputField.setDocument(new DefaultStyledDocument());
              bufferIndex = inputDocumentBuffer.size() - 1;
              inputField.getDocument().insertString(caretPosition = 0, " ", caret);
              // reset the message string
              message = "";
            }
          catch (BadLocationException ble) {}
        }
      else if (code == KeyEvent.VK_BACK_SPACE)
        {
          if (caretPosition > 0)
            {
              try
                {
                  message = message.substring(0, caretPosition - 1) + message.substring(caretPosition);
                  // REWRITE THE LINE ABOVE TO ALSO REMOVE FORMATTING CHARACTERS
                  Document doc = inputField.getDocument();
                  doc.remove(caretPosition - 1, 1);
                  caretPosition--;
                }
              catch (BadLocationException ble) {}
            }
        }
      else if (code == KeyEvent.VK_UP)
        {
          if (inputDocumentBuffer.size() > 0)
            {
              try
                {
                  inputField.getDocument().remove(caretPosition, 1);
                  inputField.setDocument((Document) inputDocumentBuffer.elementAt(bufferIndex));
                  message = (String) inputMessageBuffer.elementAt(bufferIndex);
                  caretPosition = ((Document) inputDocumentBuffer.elementAt(bufferIndex)).getLength();
                  inputField.getDocument().insertString(caretPosition, " ", caret);
                  if (bufferIndex > 0) --bufferIndex;
                }
              catch (BadLocationException ble) {}
            }
        }
      else if (code == KeyEvent.VK_DOWN)
        {
          try
            {
              inputField.getDocument().remove(caretPosition, 1);
              if (bufferIndex < inputDocumentBuffer.size() - 1)
                {
                  ++bufferIndex;
                  inputField.setDocument((Document) inputDocumentBuffer.elementAt(bufferIndex));
                  message = (String) inputMessageBuffer.elementAt(bufferIndex);
                  caretPosition = ((Document) inputDocumentBuffer.elementAt(bufferIndex)).getLength();
                }
              else if (bufferIndex == inputDocumentBuffer.size() - 1)
                {
                  inputField.setDocument(new DefaultStyledDocument());
                  message = "";
                  caretPosition = 0;
                }
              inputField.getDocument().insertString(caretPosition, " ", caret);
            }
          catch (BadLocationException ble) {}
        }
      else if (code == KeyEvent.VK_RIGHT)
        {
          if (caretPosition < inputField.getDocument().getLength() - 1)
            {
              try
                {
                  inputField.getDocument().remove(caretPosition++, 1);
                  inputField.getDocument().insertString(caretPosition, " ", caret);
                }
              catch (BadLocationException ble) {}
            }
        }
      else if (code == KeyEvent.VK_LEFT)
        {
          if (caretPosition > 0)
            {
              try
                {
                  inputField.getDocument().remove(caretPosition--, 1);
                  inputField.getDocument().insertString(caretPosition, " ", caret);
                }
              catch (BadLocationException ble) {}
            }
        }
      else if (code == KeyEvent.VK_DELETE)
        {
          if (caretPosition < inputField.getDocument().getLength())
            {
              try
                {
                  message = message.substring(0, caretPosition) + message.substring(caretPosition + 1);
                  // REWRITE THE LINE ABOVE TO ALSO REMOVE FORMATTING CHARACTERS
                  Document doc = inputField.getDocument();
                  doc.remove(caretPosition + 1, 1);
                }
              catch (BadLocationException ble) {}
            }
        }
      else
        {
          DefaultStyledDocument doc = (DefaultStyledDocument) inputField.getDocument();
          try
            {
              doc.insertString(caretPosition, ke.getKeyChar() + "", inputStyle);
              message = message.substring(0, caretPosition) + ke.getKeyChar() + message.substring(caretPosition);
              caretPosition++;
            }
          catch (BadLocationException ble)
            {
              caretPosition = inputField.getDocument().getLength();
            }
        }
  }



  // uses "X-chat"-style character modification
  public void actionPerformed(final java.awt.event.ActionEvent ae)
    {
      if (ae.getSource() == toggler)
        {
          if (toggler.isSelected())
            {
              inputPanel.add(modifications, 2);
              toggler.setIcon(collapseIcon);
            }
          else
            {
              inputPanel.remove(2);
              toggler.setIcon(expandIcon);
            }
          validate();
        }
      else if (ae.getSource() == colors)
        {
          colorPopup.show(colors, 0, 0 - colorPopup.getHeight());
        }
      //else if (ae.getSource() == plain)
      //  {
      //    //inputField.setText(inputField.getText() + "%O");
      //    StyleConstants.setBold(inputStyle, false);
      //    StyleConstants.setUnderline(inputStyle, false);
      //  }
      else if (ae.getSource() == bold)
        {
          //if (bold.isSelected()) message = message + "%B"; // X-Chat style
          StyleConstants.setBold(inputStyle, bold.isSelected());
        }
      else if (ae.getSource() == underline)
        {
          //if (underline.isSelected()) message = message + "%U"; // X-Chat style
          StyleConstants.setUnderline(inputStyle, underline.isSelected());
        }
      else if (ae.getSource() instanceof JMenuItem)
        {
          try
            {
              int index = Integer.parseInt(ae.getActionCommand());
              if (index == 0) StyleConstants.setForeground(inputStyle, inputField.getForeground());
              else StyleConstants.setForeground(inputStyle, itemColors[index - 1]);
            }
          catch (NumberFormatException nfe) {}
        }
    }
}
