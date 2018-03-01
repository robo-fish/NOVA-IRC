/* DCCPromptDialog.java */

package fish.robo.nova.guis;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import fish.robo.nova.*;

/**
  * This is the dialog box which appears when a DCC request is received.
  * @version June 1999
  * @author Kai Berk Oezer
  */
public class DCCPromptDialog extends JDialog implements ActionListener
{
	private JButton yes;
	private DCCManager dccManager;
	private NovaManager manager;


	public DCCPromptDialog(DCCManager dccManager, NovaManager manager)
		{
			super(manager.getInterface(), "DCC", false);
			this.dccManager = dccManager;
			this.manager = manager;

			// properties
			setSize(240, 192);
			setResizable(true);
			getContentPane().setLayout(new GridLayout(6, 1));
			setFont(new Font("SansSerif", Font.PLAIN, 14));

			// components
			JPanel infoPanel = new JPanel();
			infoPanel.setBorder(BorderFactory.createEtchedBorder(Color.white, Color.black));
			infoPanel.setLayout(new GridLayout(5, 1, 0, 2));
			yes = new JButton("Yes");
			yes.addActionListener(this);
			JButton no = new JButton("No");
			no.addActionListener(this);

			//layout
			GridBagLayout gbl = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(2, 6, 2, 6);
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridheight = GridBagConstraints.RELATIVE;
			gbl.setConstraints(infoPanel, gbc);
			gbc.gridy = 1;
			gbc.weightx = 0.5;
			gbc.weighty = 0.0;
			gbc.gridwidth = GridBagConstraints.RELATIVE;
			gbc.gridheight = GridBagConstraints.REMAINDER;
			gbl.setConstraints(yes, gbc);
			gbc.gridx = 1;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbl.setConstraints(no, gbc);

			if (dccManager.isChat())
				{
					infoPanel.add(new JLabel());
					infoPanel.add(new JLabel(dccManager.getNickname(), SwingConstants.CENTER));
					infoPanel.add(new JLabel("wants to chat with you.", SwingConstants.CENTER));
					infoPanel.add(new JLabel());
				}
			else
				{
					infoPanel.add(new JLabel(dccManager.getNickname(), SwingConstants.CENTER));
					infoPanel.add(new JLabel("wants to send you a file.", SwingConstants.CENTER));
					infoPanel.add(new JLabel("File Name: " + dccManager.getFileName(), SwingConstants.CENTER));
					infoPanel.add(new JLabel("File Size: " + Long.toString(dccManager.getFileSize()) + " bytes", SwingConstants.CENTER));
				}
			infoPanel.add(new JLabel("Do you accept?", SwingConstants.CENTER));

			getContentPane().setLayout(gbl);
			getContentPane().add(infoPanel);
			getContentPane().add(yes);
			getContentPane().add(no);

			Point loc = manager.getInterface().getLocation();
			Dimension parentSize = manager.getInterface().getSize();
			setLocation(loc.x + parentSize.width/2 - 120, loc.y + parentSize.height/2 - 96);

			setVisible(true);
		}


	public void actionPerformed(ActionEvent ae)
		{
			if (ae.getSource() == yes)
				{
					if (dccManager.isChat()) DCCManager.startLinker(dccManager, manager);
					else
						{
							JFileChooser fc = new JFileChooser();
							fc.setDialogTitle("Save As...");
							fc.setSelectedFile(new File(dccManager.getFileName()));
							fc.showSaveDialog(manager.getInterface());
							File tmp = fc.getSelectedFile();
							dccManager.setAbsoluteFileName(tmp.getAbsolutePath());
							dccManager.setFileName(tmp.getName());
							try
								{
									if (tmp.exists()) new DCCResumePromptDialog(dccManager, manager);
									else DCCManager.startLinker(dccManager, manager);
								}
							catch (SecurityException se)
								{
									System.out.println(se);
									return;
								}
						}
				}
			dispose();
		}
}
