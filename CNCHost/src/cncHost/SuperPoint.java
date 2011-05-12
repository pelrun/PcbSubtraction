package cncHost;
import java.util.ArrayList;
public class SuperPoint{
	public Point primaryPoint;
	public ArrayList<Point> points;
	public int index;
	Configuration c = Configuration.getInstance();
	public SuperPoint(Point dPrimaryPoint, int dIndex){
		this.primaryPoint = dPrimaryPoint;
		this.points = new ArrayList<Point>();
		this.index = dIndex;
	}
	public void addPoint(Point point){
		this.points.add(point);
	}
	public double distance(Point point){
		return this.primaryPoint.distance(point);
	}
	public String toString(){
		String r="";
		r += "G01 Z"+c.probeHigh+" F"+c.feedrate+"\n";
		r += "G01 X"+this.primaryPoint.x+" Y"+this.primaryPoint.y+" Z"+c.probeHigh+" F"+c.feedrate+"\n";
		r += "G38 Z-10 F"+c.probeFeedrate+"\n";
		r += "#"+(this.index+1)+"=#0";
		return r;
	}
}
