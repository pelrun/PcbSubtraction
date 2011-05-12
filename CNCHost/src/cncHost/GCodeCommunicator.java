/**
 * GCodeCommunicator.java
 * 
 * GCodeCommunicator class for CNCHost
 * @author gavilan
 * This is a varient of "G-code for RepRap" from author Chris Meighan. 
 *
 * This software is for communicating with mills using RepRap electronics.
 * 
 * Released under GPL 3.0.
 * See <http://www.gnu.org/licenses/> for more license information.
 */
package cncHost;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class GCodeCommunicator {

	private SerialPort port;
	private OutputStream writeStream;
	private InputStream readStream;
	
	public GCodeCommunicator(String portName) throws NoSuchPortException,
			PortInUseException, IOException, UnsupportedCommOperationException {
		CommPortIdentifier commId = CommPortIdentifier
				.getPortIdentifier(portName);
		
		// Open, close, open port to reinitialise Arduino
		port = (SerialPort)commId.open(portName, 30000);
		port.close();
		port = (SerialPort)commId.open(portName, 30000);
		
		int baudRate = 19200;
	
		// Workround for javax.comm bug.
		// See http://forum.java.sun.com/thread.jspa?threadID=673793
		// FIXME: jvandewiel: is this workaround also needed when using the RXTX library?
		try {
			port.setSerialPortParams(baudRate,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
		}
		catch (Exception e) {
			
		}
			 
		port.setSerialPortParams(baudRate,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
		
		// End of workround
		
		try {
			port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
		} catch (Exception e) {
			// Um, Linux USB ports don't do this. What can I do about it?
		}
		
		writeStream = port.getOutputStream();
		readStream = port.getInputStream();
	}
	
	/**
	 * Close port
	 */
	public void close()
	{
		if (port != null)
			port.close();
		port = null;
	}
	
	/**
	 * Dispose of communicator
	 */
	public void dispose() {
		close();
	}
	
	/**
	 * Enable receive timeout
	 * @param timeout
	 */
	public void enableReceiveTimeout( int timeout ) throws UnsupportedCommOperationException {
		port.enableReceiveTimeout( timeout );
	}
	
	public void disableReceiveTimeout() {
		port.disableReceiveTimeout();
	}
	
	/**
	 * Write line to device
	 * @param line
	 */
	public String writeLine( String line ) {

		byte[] buffer = new byte[1024];
		int len;
		String inputString = "", returnString = "";
		try {
			writeStream.write(line.getBytes());
			writeStream.write(10); // newline to terminate command
			while ( !(inputString = readLine()).equals("ok") ) {
				returnString = returnString.concat(inputString);
				if ( inputString.length() > 0 ) System.out.println(inputString);
			}
		} catch (IOException e) {}
		return returnString;
	}
	
	public String readLine() {
		long t0 = System.currentTimeMillis();
		int c = -1;
		String out = "";
		char[] readChar = new char[1];
		try {
			c = readStream.read();
			if ( c != -1 ) {
				while ( c != 13 && c != 10 && c != -1 ) {
					readChar[0] = (char)c;
					out = out.concat(new String(readChar));
					c = readStream.read();
				}
				c = readStream.read(); // Ignore newline after carriage return
			}
		} catch ( Exception e) {}
		return out;
	}
}
