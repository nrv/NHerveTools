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

import java.util.Iterator;


/**
 * The Class DenseVectorSignature.
 * 
 * @author Nicolas HERVE - nherve@ina.fr
 */
public class DenseVectorSignature extends VectorSignature {
	
	/**
	 * The Class DVSIterator.
	 * 
	 * @author Nicolas HERVE - nicolas.herve@pasteur.fr
	 */
	private class DVSIterator implements Iterator<Integer> {
		
		/** The d. */
		private int d;

		/** The data. */
		private final double[] data;
		
		/**
		 * Instantiates a new dVS iterator.
		 * 
		 * @param data
		 *            the data
		 */
		public DVSIterator(final double[] data) {
			super();
			this.data = data;
			this.d = 0;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			while (d < data.length) {
				if (data[d] != 0) {
					return true;
				}
				d++;
			}
			
			return false;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Integer next() {
			int r = d;
			d++;
			return r;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			// not used
		}
	}
	
	/** The data. */
	private double[] data;
	
	/**
	 * Instantiates a new dense vector signature.
	 */
	public DenseVectorSignature() {
		super();
		data = null;
	}
	
	/**
	 * Instantiates a new dense vector signature.
	 * 
	 * @param data
	 *            the data
	 */
	public DenseVectorSignature(double[] data) {
		super();
		this.data = data;
	}
	
	public DenseVectorSignature(float[] data) {
		super();
		this.data = new double[data.length];
		for (int i = 0; i < data.length; i++) {
			this.data[i] = data[i];
		}
	}
	
	/**
	 * Instantiates a new dense vector signature.
	 * 
	 * @param size
	 *            the size
	 */
	public DenseVectorSignature(int size) {
		this(size, 0.0);
	}
	
	/**
	 * Instantiates a new dense vector signature.
	 * 
	 * @param size
	 *            the size
	 * @param initialValue
	 *            the initial value
	 */
	public DenseVectorSignature(int size, double initialValue) {
		super();
		data = new double[size];
		try {
			setAll(initialValue);
		} catch (SignatureException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see plugins.nherve.toolbox.image.feature.signature.VectorSignature#clone()
	 */
	@Override
	public DenseVectorSignature clone() throws CloneNotSupportedException {
		DenseVectorSignature ns = new DenseVectorSignature(getSize());
		for (int d = 0; d < getSize(); d++) {
			try {
				ns.set(d, get(d));
			} catch (SignatureException e) {
				throw new CloneNotSupportedException("SignatureException : " + e.getMessage());
			}
		}
		return ns;
	}

	/* (non-Javadoc)
	 * @see plugins.nherve.toolbox.image.feature.signature.VectorSignature#concat(plugins.nherve.toolbox.image.feature.signature.VectorSignature)
	 */
	@Override
	public void concat(VectorSignature other) throws SignatureException {
		int newSize = data.length + other.getSize();
		double[] newData = new double[newSize];
		
		int d = 0;
		for (int i = 0; i < data.length; i++) {
			newData[d] = data[i];
			d++;
		}
		for (int i = 0; i < other.getSize(); i++) {
			newData[d] = other.get(i);
			d++;
		}
		
		data = newData;
	}

	/* (non-Javadoc)
	 * @see plugins.nherve.toolbox.image.feature.signature.VectorSignature#get(int)
	 */
	@Override
	public double get(int idx) throws SignatureException {
		return data[idx];
	}

	/**
	 * Gets the data.
	 * 
	 * @return the data
	 */
	public double[] getData() {
		return data;
	}

	/* (non-Javadoc)
	 * @see plugins.nherve.toolbox.image.feature.signature.VectorSignature#getNonZeroBins()
	 */
	@Override
	public int getNonZeroBins() throws SignatureException {
		int res = 0;
		for (int d = 0; d < getSize(); d++) {
			if (get(d) != 0) {
				res ++;
			}
		}
		return res;
	}

	/* (non-Javadoc)
	 * @see plugins.nherve.toolbox.image.feature.signature.VectorSignature#getSize()
	 */
	@Override
	public int getSize() {
		return data == null ? 0 : data.length;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Integer> iterator() {
		return new DVSIterator(data);
	}
	
	/* (non-Javadoc)
	 * @see plugins.nherve.toolbox.image.feature.signature.VectorSignature#set(int, double)
	 */
	@Override
	public void set(int idx, double val) throws SignatureException {
		data[idx] = val;
	}

	@Override
	public void setSize(int s) {
		// ignored
	}
}
