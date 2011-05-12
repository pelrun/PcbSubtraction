package cncHost;
import java.util.ArrayList;
public class Path{
	public ArrayList<Point> points;
	public Point currentPosition;
	public Path(){
		this.points = new ArrayList<Point>();
		this.currentPosition = new Point(0,0,0);
	}
	public void addPoint(Point a){
		this.points.add(a);
	}
	public String toString(){
		StringBuffer r = new StringBuffer();
		for(int i=0;i<this.points.size();i++){
			r.append(this.points.get(i));
			r.append("\n");
		}
		return r.toString();
	}
}
