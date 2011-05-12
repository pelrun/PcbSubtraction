package cncHost;
import java.util.ArrayList;
public class GCodeLines{
	public ArrayList<String> lines;
	public GCodeLines(){
		this.lines = new ArrayList<String>();
	}
	public void addLine(String line){
		this.lines.add(line);
	}
}

