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

import java.text.DecimalFormat;

/**
 * The Class VectorSignature.
 *
 * @author Nicolas HERVE - nherve@ina.fr
 */
public abstract class DefaultVectorSignature implements VectorSignature {

	/** The Constant DENSE_VECTOR_SIGNATURE. */
	public final static int DENSE_VECTOR_SIGNATURE = 1;

	/** The Constant df. */
	private final static DecimalFormat df = new DecimalFormat("0.000");

	/** The Constant SPARSE_VECTOR_SIGNATURE. */
	public final static int SPARSE_VECTOR_SIGNATURE = 2;

	/**
	 * Gets the empty signature.
	 *
	 * @param type
	 *            the type
	 * @param size
	 *            the size
	 * @return the empty signature
	 */
	public static DefaultVectorSignature getEmptySignature(int type, int size) {
		switch (type) {
		case DENSE_VECTOR_SIGNATURE:
			return new DenseVectorSignature(size);
		case SPARSE_VECTOR_SIGNATURE:
			return new SparseVectorSignature(size);
		default:
			return null;
		}
	}

	/** The additional information. */
	private Object additionalInformation;

	/**
	 * Instantiates a new vector signature.
	 */
	public DefaultVectorSignature() {
		super();
		setAdditionalInformation(null);
	}

	/**
	 * Adds the.
	 *
	 * @param other
	 *            the other
	 * @throws SignatureException
	 *             the signature exception
	 */
	@Override
	public void add(VectorSignature other) throws SignatureException {
		for (int d = 0; d < getSize(); d++) {
			addTo(d, other.get(d));
		}
	}

	/**
	 * Adds the.
	 *
	 * @param other
	 *            the other
	 * @param mult
	 *            the mult
	 * @throws SignatureException
	 *             the signature exception
	 */
	public void add(VectorSignature other, double mult) throws SignatureException {
		for (int d = 0; d < getSize(); d++) {
			addTo(d, other.get(d) * mult);
		}
	}

	/**
	 * Adds the to.
	 *
	 * @param idx
	 *            the idx
	 * @param val
	 *            the val
	 * @throws SignatureException
	 *             the signature exception
	 */
	public void addTo(int idx, double val) throws SignatureException {
		set(idx, get(idx) + val);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public abstract DefaultVectorSignature clone() throws CloneNotSupportedException;

	/**
	 * Concat.
	 *
	 * @param other
	 *            the other
	 * @throws SignatureException
	 *             the signature exception
	 */
	public abstract void concat(DefaultVectorSignature other) throws SignatureException;

	public double dot(VectorSignature other) throws SignatureException {
		double dot = 0;
		for (int d = 0; d < getSize(); d++) {
			dot += get(d) * other.get(d);
		}
		return dot;
	}

	/**
	 * Gets the additional information.
	 *
	 * @return the additional information
	 */
	public Object getAdditionalInformation() {
		return additionalInformation;
	}

	/**
	 * Multiply.
	 *
	 * @param coef
	 *            the coef
	 * @throws SignatureException
	 *             the signature exception
	 */
	public void multiply(double coef) throws SignatureException {
		for (int d = 0; d < getSize(); d++) {
			multiply(d, coef);
		}
	}

	/**
	 * Multiply.
	 *
	 * @param idx
	 *            the idx
	 * @param coef
	 *            the coef
	 * @throws SignatureException
	 *             the signature exception
	 */
	public void multiply(int idx, double coef) throws SignatureException {
		set(idx, get(idx) * coef);
	}

	public double norm() throws SignatureException {
		double norm = 0;
		for (int d = 0; d < getSize(); d++) {
			double v = get(d);
			norm += v * v;
		}

		return Math.sqrt(norm);
	}

	@Override
	public void normalizeL2(boolean force) throws SignatureException {
		double norm = norm();

		if (norm != 0.0) {
			multiply(1.0 / norm);
		} else if (force) {
			setAll(1.0 / getSize());
		}
	}

	/**
	 * Normalize sum to.
	 *
	 * @param n
	 *            the n
	 * @param force
	 *            the force
	 * @throws SignatureException
	 *             the signature exception
	 */
	public void normalizeSumTo(double n, boolean force) throws SignatureException {
		double sum = sum();

		if (sum != 0.0) {
			multiply(n / sum);
		} else if (force) {
			setAll(n / getSize());
		}
	}

	/**
	 * Normalize sum to one.
	 *
	 * @param force
	 *            the force
	 * @throws SignatureException
	 *             the signature exception
	 */
	public void normalizeSumToOne(boolean force) throws SignatureException {
		normalizeSumTo(1.0, force);
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
	public abstract void set(int idx, double val) throws SignatureException;

	/**
	 * Sets the additional information.
	 *
	 * @param additionalInformation
	 *            the new additional information
	 */
	public void setAdditionalInformation(Object additionalInformation) {
		this.additionalInformation = additionalInformation;
	}

	/**
	 * Sets the all.
	 *
	 * @param val
	 *            the new all
	 * @throws SignatureException
	 *             the signature exception
	 */
	public void setAll(double val) throws SignatureException {
		for (int d = 0; d < getSize(); d++) {
			set(d, val);
		}
	}

	public abstract void setSize(int s);

	/**
	 * Sum.
	 *
	 * @return the double
	 * @throws SignatureException
	 *             the signature exception
	 */
	public double sum() throws SignatureException {
		double sum = 0.0;

		for (int d = 0; d < getSize(); d++) {
			sum += get(d);
		}

		return sum;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		try {
			String str = getClass().getSimpleName() + "(" + getSize() + " - " + df.format(sum()) + ")[";
			boolean first = true;
			for (int d = 0; d < getSize(); d++) {
				if (first) {
					first = false;
				} else {
					str += "  ";
				}
				str += df.format(get(d));
			}
			str += "]";
			return str;
		} catch (SignatureException e) {
			return "SignatureException : " + e.getMessage();
		}
	}

}
