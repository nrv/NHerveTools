package plugins.nherve.toolbox.image.feature.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import plugins.nherve.toolbox.concurrent.TaskManager;
import plugins.nherve.toolbox.image.feature.DefaultClusteringAlgorithmImpl;
import plugins.nherve.toolbox.image.feature.Signature;
import plugins.nherve.toolbox.image.feature.SignatureDistance;
import plugins.nherve.toolbox.image.feature.signature.L2Distance;
import plugins.nherve.toolbox.image.feature.signature.SignatureException;

public class AgglomerativeClustering<T extends Signature> extends DefaultClusteringAlgorithmImpl<T> {
	private class ComputeDistancesWorker implements Callable<List<AgglomerativeClusteringSingleDistance>> {
		public ComputeDistancesWorker(List<T> points, int start, int end) {
			super();
			this.points = points;
			this.start = start;
			this.end = end;
		}

		private List<T> points;
		private int start;
		private int end;

		@Override
		public List<AgglomerativeClusteringSingleDistance> call() throws Exception {
			List<AgglomerativeClusteringSingleDistance> distances = new ArrayList<AgglomerativeClusteringSingleDistance>();
			for (int i = start; i < end; i++) {
				for (int j = i + 1; j < points.size(); j++) {
					AgglomerativeClusteringSingleDistance sd = new AgglomerativeClusteringSingleDistance();
					sd.i = i;
					sd.j = j;
					try {
						sd.d = distance.computeDistance(points.get(i), points.get(j));
					} catch (SignatureException e) {
						throw new ClusteringException(e);
					}
					distances.add(sd);
				}
			}
			return distances;
		}
	}
	
	private SignatureDistance distance;
	private int[] affectation;
	private int nbClasses;
	private double threshold;
	
	private int idx(int i, int j, int n) {
		return j + i * (2 * n - 1 - i) / 2;
	}
	
	public AgglomerativeClustering(boolean display) {
		super(display);
		
		distance = new L2Distance();
		threshold = 2;
	}

	@Override
	public int getNbClasses() {
		return nbClasses;
	}

	@Override
	public void compute(List<T> points) throws ClusteringException {
		List<AgglomerativeClusteringSingleDistance> distances = new ArrayList<AgglomerativeClusteringSingleDistance>();
		
		log("AgglomerativeClustering - Distances cache");
		TaskManager tm = TaskManager.getSecondLevelInstance();
		int nbSplit = tm.getCorePoolSize();
		int nb = points.size() * points.size() / 2;
		int splitSize = nb / nbSplit;
		
		int done = 0;
		int start = 0;
		List<Future<List<AgglomerativeClusteringSingleDistance>>> results = new ArrayList<Future<List<AgglomerativeClusteringSingleDistance>>>();
		for (int end = 1; end < points.size(); end++) {
			done += (points.size() - end);
			if (done > splitSize) {
				results.add(tm.submit(new ComputeDistancesWorker(points, start, end)));
				done = 0;
				start = end;
			}
		}
		if (done > 0) {
			results.add(tm.submit(new ComputeDistancesWorker(points, start, points.size())));
		}
		for (Future<List<AgglomerativeClusteringSingleDistance>> f : results) {
			try {
				distances.addAll(f.get());
			} catch (InterruptedException e) {
				throw new ClusteringException(e);
			} catch (ExecutionException e) {
				throw new ClusteringException(e);
			}
		}
		
		// log("AgglomerativeClustering - Distances : " + distances.size() + " / " + idx(points.size() - 1, points.size() - 1, points.size()));
		log("AgglomerativeClustering - Getting distances");
		int s = points.size();
//		AgglomerativeClusteringSingleDistance[] dt = new AgglomerativeClusteringSingleDistance[s * (s + 1) / 2];
//		for (AgglomerativeClusteringSingleDistance sd : distances) {
//			dt[idx(sd.i, sd.j, s)] = sd; 
//		}
		
		log("AgglomerativeClustering - Sorting distances");
		
		Collections.sort(distances, new Comparator<AgglomerativeClusteringSingleDistance>() {

			@Override
			public int compare(AgglomerativeClusteringSingleDistance o1, AgglomerativeClusteringSingleDistance o2) {
				return (int)Math.signum(o1.d - o2.d);
			}
		});
		
		double max = Collections.max(distances).d;
		double min = Collections.min(distances).d;
		
		log("AgglomerativeClustering - min = " + min);
		log("AgglomerativeClustering - max = " + max);
		
		log("AgglomerativeClustering - Agglomerating");
		affectation = new int[points.size()];
		Arrays.fill(affectation, 0);
		nbClasses = 0;
		for (AgglomerativeClusteringSingleDistance sd : distances) {
			if (sd.d <= threshold) {
				if (affectation[sd.i] > 0) {
					if (affectation[sd.j] == 0) {
						affectation[sd.j] = affectation[sd.i];
					}
				} else {
					if (affectation[sd.j] > 0) {
						affectation[sd.i] = affectation[sd.j];
					} else {
						nbClasses++;
						affectation[sd.i] = nbClasses;
						affectation[sd.j] = nbClasses;
					}
				}
			} else {
				break;
			}
		}
	}

	@Override
	public List<T> getCentroids() throws ClusteringException {
		return null;
	}

	@Override
	public int[] getAffectations(List<T> points) throws ClusteringException {
		return null;
	}
	
	public int[] getAffectations() throws ClusteringException {
		return affectation;
	}

	public void setDistance(SignatureDistance<T> distance) {
		this.distance = distance;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

}
