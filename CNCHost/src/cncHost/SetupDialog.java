/**
 * SetupDialog.java
 * 
 * SetupDialog class for CNCHost
 * @author gavilan
 * This is a varient of "G-code for RepRap" from author Chris Meighan. 
 *
 * This software is for communicating with mills using RepRap electronics.
 * 
 * Released under GPL 3.0.
 * See <http://www.gnu.org/licenses/> for more license information.
 *
 */
package cncHost;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JFormattedTextField;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.BoxLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SetupDialog extends JDialog implements ActionListener,
		DocumentListener {
	JFrame frame;
	Main parent;

	JTextField portNameField;
	JFormattedTextField jogIncrementField, jogFeedRateField;
	JButton cancelButton, OKButton;
	JRadioButton mmRadioButton, inchesRadioButton, relativeRadioButton, absoluteRadioButton;

	String units, coordMode;

	Double jogIncrement, jogFeedRate;

	Preferences p;

	boolean watchFieldChanges = true;

	public SetupDialog(JFrame frame, Main parent) {
		super(frame, "Setup", true);
		this.parent = parent;

		NumberFormat decimalFormat = NumberFormat.getNumberInstance();

		p = new Preferences();
		p.load();

		JPanel options = new JPanel();

		// Preferences
		options.setLayout(new BoxLayout(options, BoxLayout.PAGE_AXIS));
		
		// Port name
		options.add(new JLabel("Port(name)"));
		portNameField = new JTextField(p.getProperty("Port(name)"), 20);
		options.add(portNameField);

		// Units
		options.add(new JLabel("Units"));
		units = p.getProperty("Units");
		mmRadioButton = new JRadioButton("mm");
		mmRadioButton.addActionListener(this);
		if (units.equals("mm"))
			mmRadioButton.setSelected(true);
		inchesRadioButton = new JRadioButton("inches");
		inchesRadioButton.addActionListener(this);
		if (units.equals("inches"))
			inchesRadioButton.setSelected(true);
		ButtonGroup unitRadioButtonGroup = new ButtonGroup();
		unitRadioButtonGroup.add(mmRadioButton);
		unitRadioButtonGroup.add(inchesRadioButton);
		options.add(mmRadioButton);
		options.add(inchesRadioButton);

		// Jog increment
		options.add(new JLabel("Jog buttons increment (units)"));
		jogIncrement = Double.valueOf(p.getProperty("JogIncrement"));
		jogIncrementField = new JFormattedTextField(decimalFormat);
		jogIncrementField.setValue(jogIncrement);
		jogIncrementField.getDocument().addDocumentListener(this);
		jogIncrementField.getDocument().putProperty("name", "jogIncrement");
		options.add(jogIncrementField);
		
		// Jog feed rate
		options.add(new JLabel("Jog feed rate (units/minute)"));
		jogFeedRate = Double.valueOf(p.getProperty("JogFeedRate"));
		jogFeedRateField = new JFormattedTextField(decimalFormat);
		jogFeedRateField.setValue(jogFeedRate);
		jogFeedRateField.getDocument().addDocumentListener(this);
		jogFeedRateField.getDocument().putProperty("name", "jogFeedRate");
		options.add(jogFeedRateField);
		
		// Unit mode
		options.add(new JLabel("Coordinate mode"));
		coordMode = p.getProperty("CoordMode");
		relativeRadioButton = new JRadioButton("relative");
		if (coordMode.equals("relative"))
			relativeRadioButton.setSelected(true);
		absoluteRadioButton = new JRadioButton("absolute");
		if (coordMode.equals("absolute"))
			absoluteRadioButton.setSelected(true);
		ButtonGroup coordModeRadioButtonGroup = new ButtonGroup();
		coordModeRadioButtonGroup.add(relativeRadioButton);
		coordModeRadioButtonGroup.add(absoluteRadioButton);
		options.add(relativeRadioButton);
		options.add(absoluteRadioButton);

		// Action buttons
		JPanel actionButtons = new JPanel();
		actionButtons.setLayout(new BoxLayout(actionButtons,
				BoxLayout.LINE_AXIS));
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		actionButtons.add(cancelButton);
		OKButton = new JButton("OK");
		OKButton.addActionListener(this);
		actionButtons.add(OKButton);
		options.add(actionButtons);

		add(options);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();

		// Centre on screen
		Dimension dim = getToolkit().getScreenSize();
		Rectangle abounds = getBounds();
		setLocation((dim.width - abounds.width) / 2,
				(dim.height - abounds.height) / 2);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == mmRadioButton
				|| evt.getSource() == inchesRadioButton) {
			double multFactor = 1.0;
			if (evt.getSource() == mmRadioButton) {
				multFactor = (units.equals("inches")) ? 25.4 : 1.0;
				units = "mm";
			}
			if (evt.getSource() == inchesRadioButton) {
				multFactor = (units.equals("mm")) ? 1.0 / 25.4 : 1.0;
				units = "inches";
			}

			// Values are stored at a higher precision than the display so as to
			// avoid rounding errors when switching
			// between unit types
			jogIncrement = multFactor * jogIncrement;
			jogFeedRate = multFactor * jogFeedRate;

			// Round to two decimal places for display
			watchFieldChanges = false;
			jogIncrementField
					.setValue((double) Math.round(jogIncrement * 100) / 100);
			jogFeedRateField
			.setValue((double) Math.round(jogFeedRate * 100) / 100);
			watchFieldChanges = true;
		}

		// OK clicked, so save preferences
		if (evt.getSource() == OKButton) {
			p.setProperty("Port(name)", portNameField.getText());
			p.setProperty("Units", mmRadioButton.isSelected() ? "mm"
							: "inches");
			p.setProperty("CoordMode", relativeRadioButton.isSelected() ? "relative"
					: "absolute");
			p.setProperty("JogIncrement", Double
					.toString(((Number) jogIncrementField.getValue())
							.doubleValue()));
			p.setProperty("JogFeedRate", Double
					.toString(((Number) jogFeedRateField.getValue())
							.doubleValue()));
			p.save();
			try {
				parent.loadPreferences();
			} catch (Exception e) {
			}
		}
		if (evt.getSource() == OKButton || evt.getSource() == cancelButton) {
			dispose();
		}
	}

	public void insertUpdate(DocumentEvent evt) {
		fieldChanged(evt);
	}

	public void removeUpdate(DocumentEvent evt) {
		fieldChanged(evt);
	}

	public void changedUpdate(DocumentEvent evt) {
	}

	/*
	 * Value of field changed, so update internal variable
	 */
	private void fieldChanged(DocumentEvent evt) {
		if (evt.getDocument().getProperty("name") == null || !watchFieldChanges)
			return;
		try {
			if (evt.getDocument().getProperty("name").equals("jogIncrement"))
				jogIncrement = ((Number) jogIncrementField.getValue())
						.doubleValue();
			if (evt.getDocument().getProperty("name").equals("jogFeedRate"))
				jogFeedRate = ((Number) jogFeedRateField.getValue())
						.doubleValue();
		} catch (NumberFormatException e) {

		}
	}
}
