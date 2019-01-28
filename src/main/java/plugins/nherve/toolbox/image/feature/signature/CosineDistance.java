/*
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

import plugins.nherve.toolbox.image.feature.SignatureDistance;

/**
 * The Class VectorAngleDistance.
 *
 * @author Nicolas HERVE - nherve@ina.fr
 */
public class CosineDistance extends SignatureDistance<VectorSignature> {
	private boolean similarity;

	public CosineDistance() {
		this(false);
	}

	public CosineDistance(boolean similarity) {
		super();
		setSimilarity(similarity);
	}

	@Override
	public double computeDistance(VectorSignature vs1b, VectorSignature vs2b) throws SignatureException {
		if ((vs1b == null) || (vs2b == null)) {
			throw new SignatureException("Null signature in VectorAngleDistance.compute()");
		}

		if (vs1b.getSize() != vs2b.getSize()) {
			throw new SignatureException("VectorSignature dimensions mismatch");
		}

		try {
			VectorSignature s1 = (VectorSignature) vs1b.clone();
			VectorSignature s2 = (VectorSignature) vs2b.clone();
			
			s1.normalizeL2(false);
			s2.normalizeL2(false);

			double dot = 0.0;

			if ((s1 instanceof SparseVectorSignature) && (s2 instanceof SparseVectorSignature)) {
				SparseVectorSignature sv1 = (SparseVectorSignature) s1;
				SparseVectorSignature sv2 = (SparseVectorSignature) s2;

				Iterator<Integer> idx1it = sv1.iterator();
				Iterator<Integer> idx2it = sv2.iterator();

				int idx1 = Integer.MAX_VALUE;
				int idx2 = Integer.MAX_VALUE;

				if (idx1it.hasNext()) {
					idx1 = idx1it.next();
				}

				if (idx2it.hasNext()) {
					idx2 = idx2it.next();
				}

				double v1 = 0;
				double v2 = 0;
				while ((idx1 < Integer.MAX_VALUE) || (idx2 < Integer.MAX_VALUE)) {
					if (idx1 == idx2) {
						v1 = sv1.get(idx1);
						v2 = sv2.get(idx2);
						dot += v1 * v2;

						if (idx1it.hasNext()) {
							idx1 = idx1it.next();
						} else {
							idx1 = Integer.MAX_VALUE;
						}

						if (idx2it.hasNext()) {
							idx2 = idx2it.next();
						} else {
							idx2 = Integer.MAX_VALUE;
						}
					} else if (idx1 > idx2) {
						if (idx2it.hasNext()) {
							idx2 = idx2it.next();
						} else {
							idx2 = Integer.MAX_VALUE;
						}
					} else {
						if (idx1it.hasNext()) {
							idx1 = idx1it.next();
						} else {
							idx1 = Integer.MAX_VALUE;
						}
					}
				}

			} else if ((s1 instanceof DenseVectorSignature) && (s2 instanceof DenseVectorSignature)) {
				double[] as1 = ((DenseVectorSignature) s1).getData();
				double[] as2 = ((DenseVectorSignature) s2).getData();

				for (int i = 0; i < as1.length; i++) {
					dot += as1[i] * as2[i];
				}
			} else {
				double v1 = 0;
				double v2 = 0;
				for (int i = 0; i < s1.getSize(); i++) {
					v1 = s1.get(i);
					v2 = s2.get(i);
					dot += v1 * v2;
				}
			}

			return similarity ? dot : 1.0 - dot;
		} catch (CloneNotSupportedException e) {
			throw new SignatureException(e);
		}
	}

	public boolean isSimilarity() {
		return similarity;
	}

	public void setSimilarity(boolean similarity) {
		this.similarity = similarity;
	}

}
