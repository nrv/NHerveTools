package plugins.nherve.toolbox.image.feature.clustering;

public class AgglomerativeClusteringSingleDistance implements Comparable<AgglomerativeClusteringSingleDistance> {
	public int i;
	public int j;
	public double d;
	
	@Override
	public int compareTo(AgglomerativeClusteringSingleDistance o) {
		return Double.compare(d, o.d);
	}
}
