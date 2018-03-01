/* ColorsDialog.java */

package fish.robo.nova.guis;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import fish.robo.nova.*;

/**
  * This dialog appears when the user wants to change the program colors.
  * @author Kai Berk Oezer
  */
public class ColorsDialog extends JDialog implements WindowListener, ActionListener
{
	// GUI components
	private ColorDemoPanel demonstrator;
	private NovaEnvironment env;
	private NovaInterface theParent;
	private JButton apply, cancel;
	private Color fb, ff, sb, sf;

	/**
	  * Builds the interface and extracts color info from the file 'environment'.
	  * @param parent the window from which this dialog box originated
	  */
	public ColorsDialog(NovaInterface parent)
		{
			super((Frame) parent, "Color Editor", false);
			theParent = parent;
			env = parent.getManager().getEnvironment();
			Point loc = parent.getLocation();
			Dimension myParentSize = parent.getSize();
			setLocation(loc.x + myParentSize.width/2 - 150, loc.y + myParentSize.height/2 - 150);
			setSize(280, 275);
			setResizable(false);
			addWindowListener(this);

			// load current colors
			fb = env.getFirstBackColor();
			ff = env.getFirstForeColor();
			sb = env.getSecondBackColor();
			sf = env.getSecondForeColor();

			demonstrator = new ColorDemoPanel(this, ff, fb, sf, sb);
			demonstrator.setBorder(BorderFactory.createEtchedBorder(Color.white, Color.black));
                        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);

			// layout
			GridBagLayout gbl = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(4, 4, 4, 4);
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weighty = 1.0;
			gbc.weightx = 1.0;
			gbc.gridheight = 1;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbl.setConstraints(demonstrator, gbc);
                        gbc.gridy = 1;
			gbc.weighty = 0.0;
			gbc.gridheight = GridBagConstraints.RELATIVE;
                        gbl.setConstraints(sep, gbc);
			gbc.gridy = 2;
			gbc.weightx = 0.5;
			gbc.gridwidth = GridBagConstraints.RELATIVE;
			gbc.gridheight = GridBagConstraints.REMAINDER;			
			gbl.setConstraints(apply = new JButton("Apply"), gbc);
			gbc.gridx = 1;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbl.setConstraints(cancel = new JButton("Quit"), gbc);

			apply.addActionListener(this);
			cancel.addActionListener(this);

			getContentPane().setLayout(gbl);
			getContentPane().add(demonstrator);
                        getContentPane().add(sep);
			getContentPane().add(apply);
			getContentPane().add(cancel);

			pack();
			setVisible(true);
		}

	//____________________________________________________________________________
	// Property accessors

	void setPrimaryFront(Color c) {ff = c; }
	void setPrimaryBack(Color c) {fb = c; }
	void setSecondaryFront(Color c) {sf = c; }
	void setSecondaryBack(Color c) {sb = c; }

	//____________________________________________________________________________
	// Listener interfaces

	public void actionPerformed(ActionEvent ae)
		{
			// process input here
			if (ae.getSource() == cancel) dispose();
			else process();
		}

	public void windowActivated(WindowEvent we) {}
	public void windowClosed(WindowEvent we) {}
	public void windowDeactivated(WindowEvent we) {}
	public void windowDeiconified(WindowEvent we) {}
	public void windowIconified(WindowEvent we) {}
	public void windowOpened(WindowEvent we) {}

	public void windowClosing(WindowEvent we)
		{
			dispose();
		}


	//_______________________________________________________________________________
	// functional methods


	/**
	  * Called when user decides to save the new entries.
	  */
	private void process()
		{
			env.replaceTagLine("FirstBack ", "FirstBack " + fb.getRed() + "," + fb.getGreen() + "," + fb.getBlue());
			env.replaceTagLine("FirstFore ", "FirstFore " + ff.getRed() + "," + ff.getGreen() + "," + ff.getBlue());
			env.replaceTagLine("SecondBack ", "SecondBack " + sb.getRed() + "," + sb.getGreen() + "," + sb.getBlue());
			env.replaceTagLine("SecondFore ", "SecondFore " + sf.getRed() + "," + sf.getGreen() + "," + sf.getBlue());
			env.refreshColors();
			theParent.giveColors();
		}
}

