/**
 * Main.java
 * 
 * Main class for CNCHost
 * @author gavilan
 * This is a varient of "G-code for RepRap" from author Chris Meighan. 
 *
 * This software is for communicating with mills using RepRap electronics.
 * 
 * Released under GPL 3.0.
 * See <http://www.gnu.org/licenses/> for more license information.
 */
package cncHost;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.Dimension;
import java.awt.Color;
import java.io.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.JScrollPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.text.*;
import javax.swing.BorderFactory; 
import javax.swing.border.Border;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import java.awt.event.KeyEvent;

import java.lang.StringBuffer;
import java.io.*;
import java.util.*;

public class Main extends javax.swing.JFrame implements ChangeListener,
		ActionListener, PropertyChangeListener {

	// GUI elements
	private JFrame frame;
	private JMenuBar menuBar;
	private JMenu fileMenu, transformMenu, helpMenu;
	private JMenuItem quitMenuItem, probeZMenuItem, aboutMenuItem;
	private JButton fileOpenButton;
	private JFileChooser fc;
	private JTextField filePath;
	private JTextPane gcodeTextPane;
	private StyledDocument gcodePaneStyledDocument;
	private Style gcodePaneDefaultStyle, gcodePaneProcessedLineStyle, gcodePaneCurrentLineStyle;
	private JTextField statusField, temperatureField;
	private JButton probeZButton;
	private JButton runButton;
	private JButton emergencyStopButton;
	private JButton resetButton;
	private JButton setupButton;
	private JButton decX;
	private JButton incX;
	private JButton decY;
	private JButton incY;
	private JButton decZ;
	private JButton incZ;
	private JButton getTemperatureButton;
	private JTextField gcodeInputField;
	private JButton gcodeInputOK;

	// Class to read G-code files
	private GCodeReader gcodeReader;
	
	// Class to communicate with machine
	private GCodeCommunicator communicator;
	
	private String portName;

	// Values read from properties file
	private double Xinc, Yinc, Zinc;

	// Communicate with reprap - set false to run headless
	private boolean wantComms = true;

	// Flag to indicate if comms set up OK
	private boolean commsOK = false;

	// Flag used to tell processing thread to stop
	private boolean programRunning = false;

	// Preferences
	private Preferences p;
    
	private ProgressMonitor progressMonitor;
	private GCodeLoader loader;

	public static void main(String[] args) throws Exception {
		Thread.currentThread().setName("CNCHost");
		JFrame frame = new JFrame();

		try {
			Main inst = new Main(frame);
			inst.setVisible(true);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			return;
		}
	}

	public Main(JFrame frame) throws Exception {
		this.frame = frame;

		p = new Preferences();

		loadPreferences();

		gcodeReader = new GCodeReader();

		initGUI();
		
		// Centre on screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((screenSize.width - this.getSize().width) / 2,
					(screenSize.height - this.getSize().height) / 2);

	}

	/**
	 * Release all resources
	 */
	public void dispose() {
		super.dispose();
		if ( communicator != null ) communicator.dispose();
		System.exit(0);
	}

	/**
	 * Initialise comms
	 */
	private void initialiseComms() throws Exception {
		String err = "";
		if (wantComms) {
			commsOK = false;
			
			// First initialise port
			try {
				if ( communicator != null ) communicator.dispose();
				communicator = new GCodeCommunicator(portName);
			} catch (Exception e) {
				err = "There was an error opening " + portName + ".\n\n";
				err += "Check to make sure that is the right path.\n";
				err += "Check that you have your serial connector plugged in.";
			}
			
			// Then check that we have G-code Arduino connected to it
			if ( communicator != null ) {
				
				// Set timeout so program will not hang if we don't have
				// the correct hardware connected
				communicator.enableReceiveTimeout(3000);
				
				if ( !communicator.readLine().equals("start")) {
					err = "Did not receive acknowledgement from Arduino.\n";
					err += "Did you load the G-code firmware?";
				} else {
					
					// Set default units
					communicator.writeLine(p.getProperty("Units").equals("mm") ? "G21" : "G20");
					
					// Set default coordinate mode
					communicator.writeLine(p.getProperty("CoordMode").equals("absolute") ? "G90" : "G91");
				}
				
				// We don't want timeout from now on because we
				// want the port to block while waiting for a move
				// to finish
				communicator.disableReceiveTimeout();
			}
			
			if (err.length() > 0)
				JOptionPane.showMessageDialog(frame, err);
			else
				commsOK = true;
		}
	}

	/**
	 * Initialise GUI
	 * 
	 * @throws Exception
	 */
	private void initGUI() throws Exception {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Configuration config = Configuration.getInstance();
		setTitle(config.name);
		setResizable(false);
		
		// Menus
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		transformMenu = new JMenu("Transform");
		transformMenu.setMnemonic(KeyEvent.VK_T);
		helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(fileMenu);
		menuBar.add(transformMenu);
		menuBar.add(helpMenu);
		quitMenuItem = new JMenuItem("Quit",KeyEvent.VK_Q);
		quitMenuItem.addActionListener(this);
		fileMenu.add(quitMenuItem);
		probeZMenuItem = new JMenuItem("Probe Z",KeyEvent.VK_P);
		probeZMenuItem.addActionListener(this);
		transformMenu.add(probeZMenuItem);
		aboutMenuItem = new JMenuItem("About",KeyEvent.VK_A);
		aboutMenuItem.addActionListener(this);
		helpMenu.add(aboutMenuItem);
		setJMenuBar(menuBar);
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.FIRST_LINE_START;
		
		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);

		// File open
		c.gridwidth = 2;
		JPanel fileOpenPanel = new JPanel(new FlowLayout());
		fileOpenButton = new JButton("Open..");
		fileOpenButton.addActionListener(this);
		fileOpenPanel.add(fileOpenButton, c);
		filePath = new JTextField(17);
		filePath.setEditable(false);
		fileOpenPanel.add(filePath, c);
		panel.add(fileOpenPanel, c);

		// G-code pane
		c.gridx = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		gcodeTextPane = new JTextPane();
		gcodeTextPane.setEditable(false);
		gcodeTextPane.setMinimumSize(new Dimension(270, 300));
		JScrollPane editorScrollPane = new JScrollPane(gcodeTextPane);
		editorScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(270, 300));
		editorScrollPane.setMinimumSize(new Dimension(10, 10));
		panel.add(editorScrollPane, c);
		gcodePaneStyledDocument = gcodeTextPane.getStyledDocument();
		Style def = StyleContext.getDefaultStyleContext().getStyle(
				StyleContext.DEFAULT_STYLE);
		gcodePaneDefaultStyle = gcodePaneStyledDocument
				.addStyle("regular", def);
		StyleConstants.setFontFamily(gcodePaneDefaultStyle, "Courier");
		StyleConstants.setFontSize(gcodePaneDefaultStyle, 11);
		StyleConstants.setForeground(gcodePaneDefaultStyle, Color.black);
		StyleConstants.setItalic(gcodePaneDefaultStyle, false);
		gcodePaneProcessedLineStyle = gcodePaneStyledDocument.addStyle(
				"processed", gcodePaneDefaultStyle);
		StyleConstants.setItalic(gcodePaneProcessedLineStyle, true);
		gcodePaneCurrentLineStyle = gcodePaneStyledDocument.addStyle("current",
				gcodePaneDefaultStyle);
		StyleConstants.setBold(gcodePaneCurrentLineStyle, true);
		gcodePaneStyledDocument.insertString(0, gcodeReader.getGCodeAsString(),
				gcodePaneDefaultStyle);

		// Run, stop, reset, setup buttons
		c.weighty = 0.0;
		c.gridx = 0;
		c.gridwidth = 2;
		c.gridy = 3;
		JPanel actionButtonsPanel = new JPanel(new FlowLayout());
		runButton = new JButton("Go");
		runButton.addActionListener(this);
		actionButtonsPanel.add(runButton);
		emergencyStopButton = new JButton("STOP");
		emergencyStopButton.addActionListener(this);
		actionButtonsPanel.add(emergencyStopButton);
		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		actionButtonsPanel.add(resetButton);
		setupButton = new JButton("Setup");
		setupButton.addActionListener(this);
		actionButtonsPanel.add(setupButton);
		panel.add(actionButtonsPanel, c);

		// G-code input field and OK button
		c.gridx = 1;
		c.gridy = 4;
		JPanel gcodeInputPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c3 = new GridBagConstraints();
		c3.anchor = GridBagConstraints.FIRST_LINE_START;
		c3.gridwidth = 2;
		c3.gridy = 1;
		gcodeInputPanel.add(new JLabel(" "), c3);
		c3.gridy = 2;
		gcodeInputPanel.add(new JLabel("Manual G-code Entry:"), c3);
		c3.gridy = 3;
		c3.gridwidth = 1;
		gcodeInputField = new JTextField(19);
		gcodeInputField.addActionListener(this);
		gcodeInputPanel.add(gcodeInputField, c3);
		c3.gridx = 1;
		gcodeInputOK = new JButton("run");
		gcodeInputPanel.add(gcodeInputOK, c3);
		gcodeInputOK.addActionListener(this);
		panel.add(gcodeInputPanel, c);

		setContentPane(panel);
		
		pack();
	}

	/**
	 * GUI state changed event handler
	 */
	public void stateChanged(ChangeEvent evt) {
	}

	/**
	 * GUI action event handler
	 */
	public void actionPerformed(ActionEvent evt) {
		String incCoord = "";
		try {
			if (evt.getSource() == quitMenuItem ) {
				dispose();
			}
			if (evt.getSource() == aboutMenuItem ) {
				Configuration config = Configuration.getInstance();
				JOptionPane.showMessageDialog(frame, config.name+" version " + config.version + "\n" +
						"by gavilan \n\n" +
						"Extention of Chris Meighan's \nGCode for Reprap \n\n" +
						"License: GPL 3.0","About "+config.name,JOptionPane.INFORMATION_MESSAGE);
			}
			if (evt.getSource() == fileOpenButton) { // Open... button
				String lastDir = p.getProperty("LastDir");
				if (fc == null) {
					if (lastDir == null ) fc = new JFileChooser();
					else fc = new JFileChooser(lastDir);
					fc.setFileFilter(new GCodeFileFilter());
				}
				int returnVal = fc.showDialog(Main.this, "Open");
				
				// Store last used directory
				File dir = fc.getCurrentDirectory();
				p.setProperty("LastDir",dir.getAbsolutePath());
				p.save();

				// File selected
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					this.loadFile(fc.getSelectedFile().getAbsolutePath());
				}

				// Reset the file chooser for the next time it's shown.
				fc.setSelectedFile(null);
			} else if (evt.getSource() == runButton) { // Run button
				runProgram();
			} else if (evt.getSource() == emergencyStopButton) { // Emergency stop button
				stopProgram();
			} else if (evt.getSource() == resetButton) { // Reset button
				resetProgram();
			} else if (evt.getSource() == setupButton && !programRunning ) { // Setup button
				JFrame frame = new JFrame("Setup");
				new SetupDialog(frame, this);
			} else if ( 
					(evt.getSource() == gcodeInputOK || evt.getSource() == gcodeInputField ) 
					&& !programRunning ) {
				
				// OK clicked or enter pressed for manual input
				GCodeExecutionThread executionThread = new GCodeExecutionThread(
						gcodeInputField.getText());
				programRunning = true;
				executionThread.start();
			} else if(evt.getSource() == probeZMenuItem){
				loader.paths.breakUpPaths();
				loader.paths.generateSuperPoints();
				Visualize vis = new Visualize();
				vis.visualizeSuperPoints(loader.paths);
				int fileFolderCharSeperator = loader.inputFile.lastIndexOf("\\")+1;
				String probeFileLocation = loader.inputFile.substring(0, fileFolderCharSeperator) 
						+ "probeZ_" 
						+ loader.inputFile.substring(fileFolderCharSeperator);
				GCodeOperator gco = new GCodeOperator(probeFileLocation);
				gco.addProbeZInfo(loader.gCodeLines, loader.paths);
				gco.save();
				this.loadFile(probeFileLocation);
			}
			
			// Increment button pressed
			if ( !incCoord.equals("") && commsOK && !programRunning  ) {
				communicator.writeLine("G91");
				communicator.writeLine("G1 " + incCoord + " F" + p.getProperty("JogFeedRate"));
				
				// Return to absolute mode if this is the default
				if (p.getProperty("CoordMode").equals("absolute")) {
					communicator.writeLine("G90");
				}
			}
		} catch (Exception e) {
		}
	}
	private void loadFile(String inputFile){
		filePath.setText(inputFile);
		try {
			progressMonitor = new ProgressMonitor(Main.this, 
					"Loading G-code file","", 0, 100);
			progressMonitor.setProgress(0);
			loader = new GCodeLoader(inputFile);
			loader.addPropertyChangeListener(this);
			loader.execute();
		} catch (Exception e) {
		}
	}
	
	/**
	* Invoked when task's progress property changes.
	*/
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName() ) {
			int progress = (Integer) evt.getNewValue();
			progressMonitor.setProgress(progress);
			if (progressMonitor.isCanceled() || loader.isDone()) {
				Toolkit.getDefaultToolkit().beep();
				if (progressMonitor.isCanceled()) {
					loader.cancel(true);
				} else {
				}
			}
		}
	}

	/**
	 * Start running the G-code
	 */
	public void runProgram() {
		GCodeExecutionThread executionThread = new GCodeExecutionThread();
		programRunning = true;
		executionThread.start();
	}

	/**
	 * Stop the G-code
	 */
	public void stopProgram() {
		logMessage("Program stopped");
		programRunning = false;
	}

	/**
	 * Reset the G-code program and rewind to beginning
	 */
	public void resetProgram() {
		gcodeReader.setPointer(0);
		programRunning = false;
		gcodePaneStyledDocument.setCharacterAttributes(0, gcodeTextPane
				.getText().length(), gcodePaneDefaultStyle, true);
		statusField.setText("");
	}
	
	/**
	 * Filter for G-code files (.nc or .gcode)
	 */
	public class GCodeFileFilter extends FileFilter {
		public String getDescription() {
			return "Filter for G-code files";
		}
		
		public boolean accept( File file ) {
			if (file.isDirectory()) return true;
			String extension = getExtension(file);
			if ( extension == null ) return false;
			return extension.equals("nc") || extension.equals("ngc") || extension.equals("gcode");
		}
		
	}
	
	/**
	 * Get extension of a file
	 * @param f
	 * @return
	 */
	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 &&  i < s.length() - 1) {
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}

	/**
	 * Load preferences
	 */
	public void loadPreferences() throws Exception {

		p.load();
		
		// Port name
		portName = p.getProperty("Port(name)");

		// Jog increment
		Xinc = Double.valueOf(p.getProperty("JogIncrement"));
		Yinc = Double.valueOf(p.getProperty("JogIncrement"));
		Zinc = Double.valueOf(p.getProperty("JogIncrement"));
		
		initialiseComms();
	}

	/**
	 * Log a message
	 * 
	 * @param message
	 */
	private void logMessage(String message) {
		System.out.println(message);
	}
	
	/**
	 * Deal with text fed back from the Arduino
	 * @param input
	 */
	private void processInput(String input) {
		// print out registers
		if (input.length() >= 2 && input.substring(0,2).equals("R:")) {
			String[] registers = input.substring(2).split(" ");
			String fileLocation =p.getProperty("LastDir")+"\\registers.txt";
			System.out.println("Writing register values to: "+fileLocation);
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(fileLocation));
				for(int i=0;i<registers.length;i++){
					writer.write(registers[i]);
					writer.newLine();
				}
				writer.close();
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	/**
	 * Thread to run G-code program
	 * 
	 * @author chris
	 * 
	 */
	public class GCodeExecutionThread extends Thread {
		private int mode;
		public static final int LINE_MODE = 0;
		public static final int FILE_MODE = 1;
		private String line;

		/**
		 * Create thread to run a single line of G-code
		 */
		GCodeExecutionThread(String line) {
			mode = LINE_MODE;
			this.line = line;
		}

		/**
		 * Create thread to run the currently loaded G-code program
		 */
		GCodeExecutionThread() {
			mode = FILE_MODE;
		}

		/**
		 * Run the currently loaded G-code program
		 */
		public void run() {
			String returnString = "";
			logMessage("Program started");
			if (mode == FILE_MODE) { // Run currently loaded G-code file
				while ( programRunning && (line = gcodeReader.readLine()) != null) {
					int filePosition = gcodeReader.getFilePosition();
					int lineLength = gcodeReader.getCurrentLineText().length();
					gcodePaneStyledDocument.setCharacterAttributes(
							filePosition, lineLength,
							gcodePaneCurrentLineStyle, true);
					gcodeTextPane.getCaret().setDot(filePosition);
					if (commsOK) {
						if(line.indexOf("M06") != -1)
							pause(line.substring(4));
						else
							processInput( communicator.writeLine(line) );
					}
					gcodePaneStyledDocument.setCharacterAttributes(
							filePosition, lineLength,
							gcodePaneProcessedLineStyle, true);
				}
				if (programRunning) logMessage("Program finished");
			} else if (mode == LINE_MODE) { // Run a single line of G-code
				if (commsOK){
					if(line.indexOf("M06") != -1)
						pause(line.substring(4));
					else
						processInput( communicator.writeLine(line) );
				}
				logMessage("Ran code "+line);
			}
			programRunning = false;
		}
		public void pause(String line){
			JOptionPane.showMessageDialog(
					frame, 
					"Pausing for tool change.\n\n"+line+"\n\nClick ok when done.",
					"pausing...",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	/**
	 * G-Code loader thread class
	 * @author chris
	 *
	 */
	class GCodeLoader extends SwingWorker<Void, Void> {
		String inputFile;
		String fileAsText;
		Paths paths;
		GCodeLines gCodeLines;
		
		public GCodeLoader(String inputFile) {
			this.inputFile = inputFile;
		}
		public Void doInBackground() {
			setProgress(0);
			String err = "";
			fileAsText = "";
			paths = new Paths();
			gCodeLines = new GCodeLines();

			StringBuffer sb = new StringBuffer();
			try {
				BufferedReader inputStream = new BufferedReader(new FileReader( inputFile ));
				gcodePaneStyledDocument.insertString(0, inputStream.toString(), gcodePaneDefaultStyle);				
				gcodeReader.clear();
				gcodeTextPane.setText("");
				String line;
				int pos = 0;
				File f = new File( inputFile );
				long length = f.length();
				int i = 0;
				while ((line = inputStream.readLine()) != null && !isCancelled()) {
					gcodeReader.loadLine(line,pos);
					sb.append(line+"\n");
					pos += line.length() + 1;
					setProgress((int)((float)pos/length*100));

					paths.processLine(line, i);
					gCodeLines.addLine(line);
					
					Thread.sleep(0);
					i++;
				}
				Visualize vis = new Visualize();
				vis.visualizePaths(paths);
				fileAsText = sb.toString();
				gcodePaneStyledDocument.insertString(0, fileAsText, gcodePaneDefaultStyle);
			} catch (FileNotFoundException e) {
				err = "File '" + inputFile + "' not found";
			} catch (IOException e) {
				err = "IO Error";
			}catch (InterruptedException ignore) {}
			catch (BadLocationException ignore) {}
			gcodeTextPane.getCaret().setDot(0);    		
			return null;
		}
		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			progressMonitor.setProgress(0);
			progressMonitor.close();
		}
	}
}
