/**
 * GCodeOperator.java
 * 
 * GCodeOperator class for CNCHost
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

import java.io.*;
import java.util.*;

public class GCodeOperator {
	public String fileLocation;
	public ArrayList<String> lines;
	Configuration c = Configuration.getInstance();
	GCodeOperator(String dFileLocation){
		this.fileLocation = dFileLocation;
		lines = new ArrayList<String>();
	}
	public void addProbeZInfo(GCodeLines dGCodeLines, Paths paths){
		int i, j, k;
		this.lines.add("G92 X0 Y0 Z0");
		for(i=0;i<paths.superPoints.size();i++){
			this.lines.add(paths.superPoints.get(i).toString());
		}
		this.lines.add("G01 Z"+c.probeHigh+" F"+c.feedrate);
		this.lines.add("G01 X0 Y0 Z"+(c.probeHigh+c.probeDepth)+" F"+c.feedrate);
		this.lines.add("G92 X0 Y0 Z"+c.probeHigh);
		this.lines.add("M06 (remove wires)");
		int lineN = 0;
		for(i=0;i<paths.paths.size();i++){
			Path path = paths.paths.get(i);
			for(j=0;j<path.points.size();j++){
				Point point = path.points.get(j);				
				if(point.lineNumber != -1){
					for(k=lineN;k<point.lineNumber;k++){
						this.lines.add(dGCodeLines.lines.get(k));
					}
					lineN = point.lineNumber+1;
				}
				if(i!=0 || j != 0 || point.x != 0 || point.y != 0)
					this.lines.add(point.toString());
			}
		}
		for(i=lineN;i<dGCodeLines.lines.size();i++){
			this.lines.add(dGCodeLines.lines.get(i));
		}
	}
	public void save(){
		try {
			System.out.println("Writing ProbeZ file to: "+fileLocation);
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileLocation));
			for(int i=0;i<this.lines.size();i++){//REWRITE, just an example
				writer.write(this.lines.get(i));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			System.err.println(e);
		}
		
	}
}

