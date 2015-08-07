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
import plugins.nherve.toolbox.image.feature.signature.VectorSignature;

public class AgglomerativeClustering extends DefaultClusteringAlgorithmImpl<Signature> {
	private class SingleDistance {
		int i;
		int j;
		double d;
	}
	
	private class ComputeDistancesWorker implements Callable<List<SingleDistance>> {
		public ComputeDistancesWorker(List<Signature> points, int start, int end) {
			super();
			this.points = points;
			this.start = start;
			this.end = end;
		}

		private List<Signature> points;
		private int start;
		private int end;

		@Override
		public List<SingleDistance> call() throws Exception {
			List<SingleDistance> distances = new ArrayList<SingleDistance>();
			for (int i = start; i < end; i++) {
				for (int j = i + 1; j < points.size(); j++) {
					SingleDistance sd = new SingleDistance();
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
	public void compute(List<Signature> points) throws ClusteringException {
		List<SingleDistance> distances = new ArrayList<SingleDistance>();
		
		log("AgglomerativeClustering - Distances cache");
		TaskManager tm = TaskManager.getSecondLevelInstance();
		int nbSplit = tm.getCorePoolSize();
		int nb = points.size() * points.size() / 2;
		int splitSize = nb / nbSplit;
		
		int done = 0;
		int start = 0;
		List<Future<List<SingleDistance>>> results = new ArrayList<Future<List<SingleDistance>>>();
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
		for (Future<List<SingleDistance>> f : results) {
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
		SingleDistance[] dt = new SingleDistance[s * (s + 1) / 2];
		for (SingleDistance sd : distances) {
			dt[idx(sd.i, sd.j, s)] = sd; 
		}
		
		log("AgglomerativeClustering - Sorting distances");
		
		Collections.sort(distances, new Comparator<SingleDistance>() {

			@Override
			public int compare(SingleDistance o1, SingleDistance o2) {
				return (int)Math.signum(o1.d - o2.d);
			}
		});
		
		log("AgglomerativeClustering - Agglomerating");
		affectation = new int[points.size()];
		Arrays.fill(affectation, 0);
		nbClasses = 0;
		for (SingleDistance sd : distances) {
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
	public List<Signature> getCentroids() throws ClusteringException {
		return null;
	}

	@Override
	public int[] getAffectations(List<Signature> points) throws ClusteringException {
		return null;
	}
	
	public int[] getAffectations() throws ClusteringException {
		return affectation;
	}

	public void setDistance(SignatureDistance<VectorSignature> distance) {
		this.distance = distance;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

}
