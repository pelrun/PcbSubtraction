/**
 * Preferences.java
 * 
 * Preferences class for CNCHost
 * @author gavilan
 * This is a varient of "G-code for RepRap" from author Chris Meighan. 
 *
 * This software is for communicating with mills using RepRap electronics.
 * 
 * Released under GPL 3.0.
 * See <http://www.gnu.org/licenses/> for more license information.
 */

package cncHost;

import java.io.File;
import java.util.Properties;
import java.io.OutputStream;
import java.io.FileOutputStream;

/**
 * Preferences
 * @author gavilan
 *
 */
public class Preferences {
	private Properties p;
	private String path;
	
	public Preferences() {
		path = new String(System.getProperty("user.home") + File.separatorChar + ".repgcode");
		p = new Properties();
		load();
	}
	
	/**
	 * Read a property
	 * @param key
	 * @return
	 */
	public String getProperty(String key) {
		return p.getProperty(key);
	}
	
	/**
	 * Set a property
	 * @param key
	 * @param value
	 */
	public void setProperty(String key, String value) {
		p.setProperty(key, value);
	}
	
	/**
	 * Save preferences
	 */
	public void save() {
		try {
			// Make directory if necessary
			File pathFile = new File(path);
			if (!pathFile.isDirectory()) pathFile.mkdirs();
			
			// Save file
			File propsFile = new File(path + File.separatorChar + "repgcode.properties");
			OutputStream output = new FileOutputStream(propsFile);
			p.store(output,"Properties");
		} catch ( Exception e ) {
		}
	}
	
	/** 
	 * Load preferences, saving defaults if file not present
	 */
	public void load() {
		File propsFile = new File(path + File.separatorChar + "repgcode.properties");
		if (propsFile.exists() ) {
			try {
				p.load(propsFile.toURL().openStream());
			} catch ( Exception e) {
			}
		}
			
		// Try and intelligently guess port name
		String osName = System.getProperty("os.name").toLowerCase();
		String defaultPort;
		if ( osName.length() >= 7 && osName.substring(0, 7).equals("windows")) {
			defaultPort = "COM1";
		} else {
			defaultPort = "/dev/ttyS0";
		}
		
		p.setProperty("Port(name)", p.getProperty("Port(name)", defaultPort) );
		p.setProperty("Units", p.getProperty("Units", "mm" ) );
		p.setProperty("JogIncrement", p.getProperty("JogIncrement", "1.0" ) );
		p.setProperty("JogFeedRate", p.getProperty("JogFeedRate", "300.0" ) );
		p.setProperty("CoordMode", p.getProperty("CoordMode", "absolute" ) );
		save();
	}
}
