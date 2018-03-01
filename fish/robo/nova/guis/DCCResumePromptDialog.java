/* DCCResumePromptDialog.java */

package fish.robo.nova.guis;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import fish.robo.nova.*;

/**
  * This is the dialog box which appears when a file, to be received over DCC, already exists.
  * @version June 1999
  * @author Kai Berk Oezer
  */
public class DCCResumePromptDialog extends JDialog implements ActionListener, ChangeListener
{
	private JButton yes;
	private JTextField nameField;
	private NovaManager manager;
	private DCCManager dccManager;
	private JRadioButton resume,
	                     rename,
	                     overwrite;

	public DCCResumePromptDialog(DCCManager dccManager, NovaManager manager)
		{
			super(manager.getInterface(), "Append? Overwrite? Rename?", false);
			this.manager = manager;
			this.dccManager = dccManager;

			// properties
			setSize(260, 240);
			setResizable(true);
			setFont(new Font("SansSerif", Font.PLAIN, 12));
			Point loc = manager.getInterface().getLocation();
			Dimension parentSize = manager.getInterface().getSize();
			setLocation(loc.x + parentSize.width/2 - 130, loc.y + parentSize.height/2 - 120);

			// components
			JLabel infoLabel = new JLabel("The file " + dccManager.getFileName() + " already exists.", SwingConstants.CENTER);
			JPanel infoPanel = new JPanel();
			infoPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(Color.white, Color.black), "Options"));
			infoPanel.setLayout(new GridLayout(4, 1, 0, 2));
			ButtonGroup group = new ButtonGroup();
			group.add(resume = new JRadioButton("Append"));
			group.add(overwrite = new JRadioButton("Overwrite"));
			overwrite.setSelected(true);
			group.add(rename = new JRadioButton("Rename"));
			rename.addChangeListener(this);
			nameField = new JTextField(dccManager.getFileName());
			nameField.setEnabled(false);
			nameField.setEditable(true);
			yes = new JButton("Continue");
			yes.addActionListener(this);
			JButton no = new JButton("Cancel");
			no.addActionListener(this);

			//layout
			GridBagLayout gbl = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(2, 6, 2, 6);
			gbc.fill = GridBagConstraints.BOTH;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbl.setConstraints(infoLabel, gbc);
			gbc.weighty = 1.0;
			gbc.gridy = 1;
			gbc.gridheight = GridBagConstraints.RELATIVE;
			gbl.setConstraints(infoPanel, gbc);
			gbc.gridy = 2;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.fill = GridBagConstraints.NONE;
			gbc.anchor = GridBagConstraints.SOUTHEAST;
			gbc.gridwidth = GridBagConstraints.RELATIVE;
			gbc.gridheight = GridBagConstraints.REMAINDER;
			gbl.setConstraints(yes, gbc);
			gbc.gridx = 1;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbl.setConstraints(no, gbc);

			infoPanel.add(resume);
			infoPanel.add(overwrite);
			infoPanel.add(rename);
			infoPanel.add(nameField);
			getContentPane().setLayout(gbl);
			getContentPane().add(infoLabel);
			getContentPane().add(infoPanel);
			getContentPane().add(yes);
			getContentPane().add(no);

			setVisible(true);
		}


	public void actionPerformed(ActionEvent ae)
		{
			if (ae.getSource() == yes)
				{
					if  (resume.isSelected())
						{
							DCCManager.RESUMERegister.addElement(dccManager);
							String position = String.valueOf((new File(dccManager.getAbsoluteFileName())).length());
							manager.sendMessage("PRIVMSG " + dccManager.getNickname() + " :\001DCC RESUME " + dccManager.getFileName() + " " + String.valueOf(dccManager.getPort()) + " " + position + '\001');
						}
					else if (overwrite.isSelected())
						{
							try {(new File(dccManager.getAbsoluteFileName())).delete(); }
							catch (SecurityException se)
								{
									System.out.println(se);
									return;
								}
							DCCManager.startLinker(dccManager, manager);
						}
					else if (rename.isSelected() && nameField.getText().trim() != null)
						{
							if (nameField.getText().trim() == null) return;
							dccManager.setFileName(nameField.getText());
							dccManager.setAbsoluteFileName(nameField.getText());
							DCCManager.startLinker(dccManager, manager);
						}
				}
			dispose();
		}


	public void stateChanged(ChangeEvent ce)
		{
			if (rename.isSelected()) nameField.setEnabled(true);
			else nameField.setEnabled(false);
		}
}
