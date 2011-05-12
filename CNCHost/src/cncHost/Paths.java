package cncHost;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
public class Paths{
	public ArrayList<Path> paths;
	public ArrayList<SuperPoint> superPoints;
	public Point currentPosition;
	public double minX, minY, maxX, maxY;
	Configuration c = Configuration.getInstance();

	public Paths(){
		this.paths = new ArrayList<Path>();
		this.paths.add(new Path());
		this.currentPosition = new Point(0,0,0);
		this.minX = 0;
		this.minY = 0;
		this.maxX = 0;
		this.maxY = 0;
	}
	public void addPoint(Point a){
		this.paths.get(this.paths.size()-1).addPoint(a);
	}
	public void processLine(String line, int lineNumber){
		Point newPosition = new Point(this.currentPosition);
		String[] command = line.split(" ");
		if(command[0].equals("G01") || command[0].equals("G00") ){
			for(int i = 1; i< command.length; i++){
				String g = command[i].substring(0,1).toLowerCase();
				if(g.equals("x")){
					newPosition.x = Double.valueOf(command[i].substring(1).trim());
				}
				else if(g.equals("y")){
					newPosition.y = Double.valueOf(command[i].substring(1).trim());
				}
				else if(g.equals("z")){
					if(command[i].substring(1).indexOf("#") == -1){
						newPosition.z = Double.valueOf(command[i].substring(1).trim());
					}
					else{
						newPosition.z = 0;
					}
				}
			}
			if(newPosition.z == 0){
				if(newPosition.x < this.minX){
					this.minX = newPosition.x;
				}
				else if(newPosition.x > this.maxX){
					this.maxX = newPosition.x;
				}
				if(newPosition.y < this.minY){
					this.minY = newPosition.y;
				}
				else if(newPosition.y > this.maxY){
					this.maxY = newPosition.y;
				}
				this.addPoint(new Point(newPosition, lineNumber));
			}
			else{
				if(this.paths.get(this.paths.size()-1).points.size() != 0){
					this.paths.add(new Path());
				}
			}
			this.currentPosition = new Point(newPosition);
		}
	}
	public void breakUpPaths(){
		int numberOfGeneratedPoints = 0;
		for(int i=0;i<paths.size();i++){
			Path path = paths.get(i);
			for(int j=1;j<path.points.size();j++){
				Point p1 = path.points.get(j-1);
				Point p2 = path.points.get(j);
				double d = p1.distance(p2);
				if(d > c.maxPathLength){
					ArrayList<Point> generatedPoints = generateFillPoints( p1, p2, d);
					path.points.addAll(j, generatedPoints);
					j += generatedPoints.size();
					numberOfGeneratedPoints += generatedPoints.size();
				}	
			}
		}
		System.out.println("breaking up paths...");
		System.out.println("number of generated points: "+numberOfGeneratedPoints);
	}
	public void generateSuperPoints(){
		this.superPoints = new ArrayList<SuperPoint>();
		System.out.println("Generating code to probe Z...");
		int numberOfPoints = 0;
		int i,j,k;
		for(i=0;i<this.paths.size();i++){
			ArrayList<Point> points = this.paths.get(i).points;
			for(j = 0;j<points.size();j++){
				Point point = points.get(j);
				numberOfPoints++;
				boolean done = false;
				for(k=0; k<this.superPoints.size() && !done; k++){
					SuperPoint superPoint = superPoints.get(k);
					if(superPoint.distance(point) < c.superPointRadius){
						point.superPoint = superPoint;
						superPoint.addPoint(point);
						done = true;
					}
				}
				if(!done){
					this.superPoints.add(new SuperPoint(point, (this.superPoints.size())));
					point.superPoint = this.superPoints.get(this.superPoints.size()-1);
				}
			}	
		}
		System.out.println("total points: "+numberOfPoints);
		System.out.println("touchdown points: "+this.superPoints.size());
		this.tspSuperPoints();
		double totalDistance = 0;
		for(i=1;i<this.superPoints.size();i++){
			totalDistance += this.superPoints.get(i-1).distance(this.superPoints.get(i).primaryPoint);
		}
		System.out.println("Total probing path distance: "+totalDistance);
	}
	private void tspSuperPoints(){//traveling salesperson problem
		double[][] d = new double[this.superPoints.size()][this.superPoints.size()];
		int i, j;
		ArrayList<SuperPoint> newSuperPoints = new ArrayList<SuperPoint>();
		for(i=0;i<this.superPoints.size();i++){
			for(j=0;j<this.superPoints.size();j++){
				d[i][j] = this.superPoints.get(i).distance(this.superPoints.get(j).primaryPoint);
			}
		}
		Hashtable<Integer, Integer> picked = new Hashtable<Integer, Integer>();
		newSuperPoints.add(this.superPoints.get(0));
		picked.put(new Integer(0), new Integer(0));
		int v = 0;
		while(this.superPoints.size() != newSuperPoints.size()){
			double dist = -1;
			int k = -1;
			for(i=0;i<this.superPoints.size();i++){
				if(v != i && (d[v][i] < dist || dist <= 0) && picked.get(new Integer(i)) == null){
					dist = d[v][i];
					k = i;
				}
			}
			newSuperPoints.add(this.superPoints.get(k));
			picked.put(new Integer(k), new Integer(k));
			v = k;
		}
		
		this.superPoints = newSuperPoints;
	}
	public String toString(){
		StringBuffer r = new StringBuffer();
		for(int i=0;i<this.paths.size();i++){
			r.append("[");
			r.append(this.paths.get(i));
			r.append("]\n");
		}
		return r.toString();
	}
	private ArrayList<Point> generateFillPoints(Point a, Point b, double d){ //d=distance
		ArrayList<Point> r = new ArrayList<Point>();
		double distX = b.x - a.x;
		double distY = b.y - a.y;
		int numberOfPoints = (int)(d/c.maxPathLength);
		for(int i=0;i<numberOfPoints;i++){
			r.add(new Point(a.x+(distX/numberOfPoints*i), a.y+(distY/numberOfPoints*i), a.z));
		}
		return r;
	}
}
