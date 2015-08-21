package plugins.nherve.toolbox.image.feature.clustering;

public class AgglomerativeClusteringSingleDistance implements Comparable<AgglomerativeClusteringSingleDistance> {
	int i;
	int j;
	double d;
	
	@Override
	public int compareTo(AgglomerativeClusteringSingleDistance o) {
		return Double.compare(d, o.d);
	}
}
