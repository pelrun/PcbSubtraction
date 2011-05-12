package cncHost;
import java.util.Comparator;
class SuperPointComparator implements Comparator<SuperPoint>{
	@Override
	public int compare(SuperPoint a, SuperPoint b){
		if((a.primaryPoint.x > b.primaryPoint.x) || (a.primaryPoint.x == b.primaryPoint.x && a.primaryPoint.y > b.primaryPoint.y)){
			return 1;
		}
		return -1;
	}
}
