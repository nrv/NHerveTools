package plugins.nherve.toolbox.image.db;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import plugins.nherve.toolbox.Algorithm;
import plugins.nherve.toolbox.image.feature.FeatureException;
import plugins.nherve.toolbox.image.feature.SegmentableImage;
import plugins.nherve.toolbox.image.feature.SignatureDistance;
import plugins.nherve.toolbox.image.feature.signature.BagOfSignatures;
import plugins.nherve.toolbox.image.feature.signature.DefaultVectorSignature;
import plugins.nherve.toolbox.image.feature.signature.L1Distance;
import plugins.nherve.toolbox.image.feature.signature.VectorSignature;

public class QueryManager<T extends SegmentableImage> extends Algorithm {
	public class Response implements Iterable<ResponseUnit> {
		private double currentMin;
		private String queryId;
		private List<ResponseUnit> internal;

		public Response(String queryId) {
			super();
			internal = new ArrayList<ResponseUnit>();
			this.queryId = queryId;
			currentMin = Double.MAX_VALUE;
		}

		public boolean add(ResponseUnit e) {
			return internal.add(e);
		}

		public void dump(BufferedWriter w) throws IOException {
			w.write("# " + queryId);
			w.newLine();
			for (ResponseUnit ru : internal) {
				if (ru.lid >= 0) {
					w.write(DatabaseManager.getUniqueId(ru.entry.getId(), ru.lid) + " " + ru.distanceToQuery);
				} else {
					w.write(ru.entry.getId() + " " + ru.distanceToQuery);
				}
				w.newLine();
			}
		}

		public double getCurrentMin() {
			return currentMin;
		}

		@Override
		public Iterator<ResponseUnit> iterator() {
			return internal.iterator();
		}

		protected void setCurrentMin(double currentMin) {
			this.currentMin = currentMin;
		}

		public int size() {
			return internal.size();
		}

		public void sortAndTruncate(int k) {
			Collections.sort(internal);
			if (k < internal.size()) {
				internal = new ArrayList<ResponseUnit>(internal.subList(0, k));
			}
			setCurrentMin(internal.get(internal.size() - 1).getDistanceToQuery());
		}

		@Override
		public String toString() {
			String r = "Response (" + queryId + ") : \n";
			for (ResponseUnit ru : internal) {
				r += " - " + ru.toString() + "\n";
			}
			return r;
		}
	}

	public class ResponseUnit implements Comparable<ResponseUnit> {
		private ImageEntry<T> entry;
		private int lid;
		private double distanceToQuery;

		public ResponseUnit() {
			super();
			lid = -1;
		}

		@Override
		public int compareTo(ResponseUnit o) {
			return (int) Math.signum(distanceToQuery - o.distanceToQuery);
		}

		public double getDistanceToQuery() {
			return distanceToQuery;
		}

		public ImageEntry<T> getEntry() {
			return entry;
		}

		public int getLid() {
			return lid;
		}

		public void setDistanceToQuery(double distanceToQuery) {
			this.distanceToQuery = distanceToQuery;
		}

		public void setEntry(ImageEntry<T> entry) {
			this.entry = entry;
		}

		public void setLid(int lid) {
			this.lid = lid;
		}

		@Override
		public String toString() {
			return "ResponseUnit [entry=" + entry.getId() + " (" + lid + "), distanceToQuery=" + distanceToQuery + "]";
		}
	}

	private SignatureDistance<VectorSignature> distance;

	public QueryManager(boolean display) {
		super(display);

		distance = new L1Distance();
	}

	public Response knnQuery(final String queryId, final ImageDatabase<T> db, final String desc, final ImageEntry<T> query, final int k) throws FeatureException {
		DefaultVectorSignature vs = db.getGlobalSignature(query, desc);
		return knnQuery(queryId, db, desc, vs, k);
	}

	public Response knnQuery(final String queryId, final ImageDatabase<T> db, final String desc, final DefaultVectorSignature query, final int k) throws FeatureException {
		Response result = new Response(queryId);
		if (db.containsGlobalDescriptor(desc)) {
			for (ImageEntry<T> e : db) {
				DefaultVectorSignature s = db.getGlobalSignature(e, desc);
				if (s != null) {
					ResponseUnit ru = new ResponseUnit();
					ru.entry = e;
					ru.distanceToQuery = distance.computeDistance(s, query);
					result.add(ru);
				}
			}
		} else if (db.containsLocalDescriptor(desc)) {
			for (ImageEntry<T> e : db) {
				BagOfSignatures<DefaultVectorSignature> bag = db.getLocalSignature(e, desc);
				if (bag != null) {
					int lid = 0;
					for (DefaultVectorSignature s : bag) {
						ResponseUnit ru = new ResponseUnit();
						ru.entry = e;
						ru.lid = lid;
						ru.distanceToQuery = distance.computeDistance(s, query);
						result.add(ru);
						lid++;
					}
				}
			}
		}

		result.sortAndTruncate(k);

		return result;
	}

	public Response randomQuery(final String queryId, final ImageDatabase<T> db, int n) throws FeatureException {
		Response result = new Response(queryId);

		if (n > db.size()) {
			n = db.size();
		}

		Set<ImageEntry<T>> choosen = new HashSet<ImageEntry<T>>();

		Random rd = new Random(System.currentTimeMillis());

		while (result.size() < n) {
			ImageEntry<T> e = db.get(rd.nextInt(db.size()));
			if (!choosen.contains(e)) {
				ResponseUnit u = new ResponseUnit();
				u.entry = e;
				u.distanceToQuery = 0;
				result.add(u);
				choosen.add(e);
			}
		}

		return result;
	}

	public void setDistance(SignatureDistance<VectorSignature> distance) {
		this.distance = distance;
	}

}
