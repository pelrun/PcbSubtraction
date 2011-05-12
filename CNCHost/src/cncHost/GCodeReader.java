/**
 * GCodeReader.java
 * 
 * GCodeReader class for CNCHost
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

import java.util.ArrayList;

public class GCodeReader {
	
	private int pointer = 0;
	
	private int filePosition;
	
	private GCodeLine currentLine;
	
	private ArrayList<GCodeLine> GCode;
	
	public GCodeReader() {
		GCode = new ArrayList<GCodeLine>();
	}
	
	/**
	 * Clear G-code file
	 */
	public void clear() {
		pointer = 0;
		GCode.clear();
	}
	
	/**
	 * Load a line of G-code
	 * @param inputFile
	 * @throws Exception
	 */
	public void loadLine(String line, int pos) {
		GCode.add(new GCodeLine(line,pos,line.length()));
	}
	
	/**
	 * Set file position pointer
	 */
	public void setPointer(int pointer) {
		this.pointer = pointer;
	}

	/**
	 * Read next line from G-code file
	 * @return line
	 */
	public String readLine() {
		
		// Check not at/past end of file
		if ( pointer >= GCode.size() ) return null;
		String line;

		// Get next line and increment pointer, skipping any blank lines
		while ( ( currentLine = GCode.get(pointer++)).getText() == "" && pointer < GCode.size()) {}

		filePosition = currentLine.getStartPos();
		
		line = currentLine.getText();
		
		// If no more non-blank lines, return
		if ( line == "" ) return null;
		
		return line;
	}
	
	/**
	 * Get current position in file
	 * @return
	 */
	public int getFilePosition() { return filePosition; }
	
	/**
	 * Return current line as text
	 * @return
	 */
	public String getCurrentLineText() { return currentLine.getText(); }
	
	/**
	 * Return the current G-code file as a string
	 * @return
	 */
	public String getGCodeAsString() {
		String GCodeString = "";
		int i;
		for (i = 0; i < GCode.size(); i++) {
			GCodeString = GCodeString.concat(GCode.get(i).getText()) + "\n";
		}
		return GCodeString;
	}
	
	/**
	 * Line of G-code
	 * @author chris
	 *
	 */
	public class GCodeLine {
		private String text;
		private int startPos;
		private int endPos;
		
		public GCodeLine( String text, int startPos, int endPos) {
			this.text = text;
			this.startPos = startPos;
			this.endPos = endPos;
		}
		
		public String getText() {
			return text;
		}
		
		public int getStartPos() {
			return startPos;
		}
		
		public int getEndPos() {
			return endPos;
		}
	}
}
