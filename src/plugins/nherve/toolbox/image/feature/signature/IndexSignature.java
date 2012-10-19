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

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import plugins.nherve.toolbox.image.feature.Signature;



/**
 * The Class IndexSignature.
 * 
 * @author Nicolas HERVE - nherve@ina.fr
 */
public class IndexSignature implements Signature, Iterable<Integer> {
	
	private class IndexSignatureIterator implements Iterator<Integer> {
		private int index = 0;

		private IndexSignatureIterator() {
		}

		public boolean hasNext() {
			return (index < data.length);
		}

		public Integer next() throws NoSuchElementException {
			if (index >= data.length)
				throw new NoSuchElementException("Array index: " + index);
			return data[index++];
		}

		public void remove() {
			// not implemented
		}

	}
	
	/** The data. */
	private int[] data;
	
	/** The size. */
	private int size;
	
	/**
	 * Instantiates a new index signature.
	 */
	public IndexSignature() {
		super();
		size = 0;
		data = null;
	}
	
	/**
	 * Instantiates a new index signature.
	 * 
	 * @param size
	 *            the size
	 */
	public IndexSignature(int size) {
		this(size, 0);
	}
	
	/**
	 * Instantiates a new index signature.
	 * 
	 * @param size
	 *            the size
	 * @param initialValue
	 *            the initial value
	 */
	public IndexSignature(int size, int initialValue) {
		super();
		this.size = size;
		data = new int[size];
		Arrays.fill(data, initialValue);
	}
	
	public IndexSignature(int[] data) {
		super();
		this.size = data.length;
		this.data = data;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public IndexSignature clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("Not yet needed");
	}

	/**
	 * Gets the.
	 * 
	 * @param idx
	 *            the idx
	 * @return the int
	 * @throws SignatureException
	 *             the signature exception
	 */
	public int get(int idx) throws SignatureException {
		return data[idx];
	}

	public int[] getData() {
		return data;
	}
	
	public int getNonZeroBins() throws SignatureException {
		int nz = 0;
		for (int i : data) {
			if (i != 0) {
				nz++;
			}
		}
		return nz;
	}
	
	/**
	 * Gets the size.
	 * 
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	@Override
	public Iterator<Integer> iterator() {
		return new IndexSignatureIterator();
	}

	/**
	 * Sets the.
	 * 
	 * @param idx
	 *            the idx
	 * @param val
	 *            the val
	 * @throws SignatureException
	 *             the signature exception
	 */
	public void set(int idx, int val) throws SignatureException {
		data[idx] = val;
	}

	public void setData(int[] data) {
		this.data = data;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String k = "";
		
		if (size > 0) {
			k += data[0];
			for (int i = 1; i < size; i++) {
				k += "-" + data[i];
			}
		}
		return k;
	}
}
