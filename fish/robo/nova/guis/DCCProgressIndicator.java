/* DCCProgressIndicator.java */

package fish.robo.nova.guis;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import fish.robo.nova.*;

/**
  * Provides a user interface which shows the user the file transfer progress of DCC.
  * @version June 1999
  * @author Kai Berk Oezer
  */
public class DCCProgressIndicator extends JInternalFrame implements ActionListener
{
	/** the total size of the file in bytes (octets) */
	private long totalBytes;
	private JProgressBar progress;
	private DCCManager dccManager;

	public DCCProgressIndicator(DCCManager dccm, boolean sending, NovaInterface parent)
		{
			super("DCC - " + dccm.getFileName(), true, false, false, true);
			parent.addToInterface(this);
			totalBytes = dccm.getFileSize();
			dccManager = dccm;

			// components
			JPanel infoPanel = new JPanel();
			infoPanel.setBorder(BorderFactory.createEtchedBorder(Color.white, Color.black));;
			JLabel partner = new JLabel((sending ? "To: " : "From: ") + dccm.getNickname(), SwingConstants.LEFT);
			JLabel name = new JLabel("File Name: " + dccm.getFileName(), SwingConstants.LEFT);
			JLabel size = new JLabel("File Size: " + Long.toString(dccm.getFileSize()) + " bytes", SwingConstants.LEFT);
			progress = new JProgressBar(SwingConstants.HORIZONTAL, 0, 100);
			progress.setValue(0);
			progress.setString("0%");
			JButton abort = new JButton("Abort");
			abort.addActionListener(this);

			// layout
			GridBagLayout gbl = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.insets = new Insets(4, 4, 4, 4);
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridheight = GridBagConstraints.RELATIVE;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.anchor = GridBagConstraints.SOUTH;
			gbl.setConstraints(infoPanel, gbc);
			gbc.gridheight = GridBagConstraints.REMAINDER;
			gbc.gridy = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbl.setConstraints(abort, gbc);
			infoPanel.setLayout(new GridLayout(4, 1, 2, 2));
			infoPanel.add(partner);
			infoPanel.add(name);
			infoPanel.add(size);
			infoPanel.add(progress);
			getContentPane().setLayout(gbl);
			getContentPane().add(infoPanel);
			getContentPane().add(abort);

			// colors
			NovaEnvironment myEnv = parent.getManager().getEnvironment();
			Color c = myEnv.getSecondBackColor();
			Color d = myEnv.getSecondForeColor();
			setBackground(c);
			partner.setBackground(c);
			partner.setForeground(d);
			name.setBackground(c);
			name.setForeground(d);
			size.setBackground(c);
			size.setForeground(d);
			abort.setBackground(c);
			abort.setForeground(d);
			progress.setBackground(myEnv.getFirstBackColor());
			progress.setForeground(myEnv.getFirstForeColor());

			setSize(200, 150);
			setFrameIcon(parent.NovaIcon);

			setVisible(true);
		}

	//__________________________________________________________________________________
	// Listener interfaces


	public void actionPerformed(ActionEvent ae)
		{
			dccManager.shutDown();
		}


	public void update(long transferred_bytes)
		{
			int v = (int)(100 * transferred_bytes / totalBytes);
			progress.setValue(v);
			//progress.setString(Integer.toString(v)+'%');
		}
}
