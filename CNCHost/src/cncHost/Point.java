package cncHost;
public class Point{
	public SuperPoint superPoint;
	public double x, y, z;
	public int lineNumber;
	Configuration c = Configuration.getInstance();
	public Point(Point a){
		this.x = a.x;
		this.y = a.y;
		this.z = a.z;
		this.lineNumber = -1;
	}
	public Point(Point a, int dLineNumber){
		this.x = a.x;
		this.y = a.y;
		this.z = a.z;
		this.lineNumber = dLineNumber;
	}
	public Point(double dX, double dY, double dZ){
		this.x = dX;
		this.y = dY;
		this.z = dZ;
		this.lineNumber = -1;
	}
	public double distance(Point point){
		double xr = this.x - point.x;
		double yr = this.y - point.y;
		return Math.sqrt( (xr*xr) + (yr*yr) );
	}
	public String toString(){
		return "G01 X"+this.x+" Y"+this.y+" Z#"+(this.superPoint.index+1)+" F"+c.feedrate;
	}
}
