/*
 * Copyright 2010, 2011 Institut Pasteur.
 * Copyright 2012 Institut National de l'Audiovisuel.
 *
 * This file is part of NHerveTools.
 *
 * NHerveTools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NHerveTools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NHerveTools. If not, see <http://www.gnu.org/licenses/>.
 */
package plugins.nherve.toolbox.image.feature.signature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The Class SparseVectorSignature.
 *
 * @author Nicolas HERVE - nherve@ina.fr
 */
public class SparseVectorSignature extends DefaultVectorSignature {

	/** The size. */
	private int size;

	/** The data. */
	private Map<Integer, Double> data;

	/**
	 * Instantiates a new sparse vector signature.
	 *
	 * @param size
	 *            the size
	 */
	public SparseVectorSignature(int size) {
		super();
		this.size = size;
		data = new TreeMap<Integer, Double>();
	}

	// public SparseVectorSignature() {
	// this(0);
	// }

	@Override
	public void add(VectorSignature other) throws SignatureException {
		if (other instanceof SparseVectorSignature) {
			int idx = 0;
			Double v = null;
			SparseVectorSignature o = (SparseVectorSignature) other;
			for (Entry<Integer, Double> e : o.data.entrySet()) {
				idx = e.getKey();
				v = data.get(idx);
				data.put(idx, (v == null ? 0 : v) + e.getValue());
			}
		} else {
			super.add(other);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plugins.nherve.toolbox.image.feature.signature.VectorSignature#clone()
	 */
	@Override
	public SparseVectorSignature clone() throws CloneNotSupportedException {
		SparseVectorSignature ns = new SparseVectorSignature(getSize());
		try {
			for (int d : this) {
				ns.set(d, get(d));
			}
		} catch (SignatureException e) {
			throw new CloneNotSupportedException("SignatureException : " + e.getMessage());
		}
		return ns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plugins.nherve.toolbox.image.feature.signature.VectorSignature#concat (plugins.nherve.toolbox.image.feature.signature.VectorSignature)
	 */
	@Override
	public void concat(DefaultVectorSignature other) throws SignatureException {
		int newSize = size + other.getSize();
		Map<Integer, Double> newData = new TreeMap<Integer, Double>();

		int d = 0;
		for (int i = 0; i < size; i++) {
			if (data.containsKey(i)) {
				newData.put(d, data.get(i));
			}
			d++;
		}
		for (int i = 0; i < other.getSize(); i++) {
			if (other.get(i) != 0) {
				newData.put(d, other.get(i));
			}
			d++;
		}

		size = newSize;
		data = newData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plugins.nherve.toolbox.image.feature.signature.VectorSignature#get(int)
	 */
	@Override
	public double get(int idx) throws SignatureException {
		Double res = data.get(idx);

		if (res != null) {
			return res;
		}

		if ((idx < 0) || (idx >= size)) {
			throw new SignatureException("Invalid signature index (" + idx + ")");
		}

		return 0;
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	public Map<Integer, Double> getData() {
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plugins.nherve.toolbox.image.feature.signature.VectorSignature#getNonZeroBins ()
	 */
	@Override
	public int getNonZeroBins() throws SignatureException {
		return data.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plugins.nherve.toolbox.image.feature.signature.VectorSignature#getSize()
	 */
	@Override
	public int getSize() {
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Integer> iterator() {
		return data.keySet().iterator();
	}

	public void keepTopK(int k) {
		if (k < data.size()) {
			List<Entry<Integer, Double>> sorted = new ArrayList<Map.Entry<Integer, Double>>();
			sorted.addAll(data.entrySet());
			Collections.sort(sorted, new Comparator<Entry<Integer, Double>>() {

				@Override
				public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
					return Double.compare(o1.getValue(), o2.getValue());
				}
			});

			Set<Integer> toRemove = new TreeSet<Integer>();
			for (Entry<Integer, Double> e : sorted.subList(k, sorted.size())) {
				toRemove.add(e.getKey());
			}
			for (int idx : toRemove) {
				data.remove(idx);
			}
		}
	}

	@Override
	public void multiply(double coef) throws SignatureException {
		for (int idx : this) {
			data.put(idx, data.get(idx) * coef);
		}
	}

	@Override
	public double norm() throws SignatureException {
		double norm = 0;
		for (Entry<Integer, Double> e : data.entrySet()) {
			double v = e.getValue();
			norm += v * v;
		}

		return Math.sqrt(norm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plugins.nherve.toolbox.image.feature.signature.VectorSignature#set(int, double)
	 */
	@Override
	public void set(int idx, double val) throws SignatureException {
		if ((idx < 0) || (idx >= size)) {
			throw new SignatureException("Invalid signature index (" + idx + ")");
		}

		if (val == 0) {
			data.remove(idx);
		} else {
			data.put(idx, val);
		}
	}

	@Override
	public void setSize(int s) {
		size = s;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (int d : data.keySet()) {
			if (first) {
				first = false;
			} else {
				sb.append("  ");
			}
			sb.append(d + ":" + df.format(data.get(d)));
		}

		try {
			return "SparseVectorSignature [size=" + size + ", sum=" + df.format(sum()) + "] " + sb.toString();
		} catch (SignatureException e) {
			return e.getClass().getName() + " : " + e.getMessage();
		}
	}

}
