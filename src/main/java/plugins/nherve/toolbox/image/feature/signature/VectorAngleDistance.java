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
public class VectorAngleDistance extends SignatureDistance<VectorSignature> {

	public VectorAngleDistance() {
		super();
	}

	@Override
	public double computeDistance(VectorSignature vs1, VectorSignature vs2) throws SignatureException {
		if ((vs1 == null) || (vs2 == null)) {
			throw new SignatureException("Null signature in VectorAngleDistance.compute()");
		}

		if (vs1.getSize() != vs2.getSize()) {
			throw new SignatureException("VectorSignature dimensions mismatch");
		}

		double dist = 0.0;
		double sqsum1 = 0.0;
		double sqsum2 = 0.0;

		if (vs1 instanceof SparseVectorSignature && vs2 instanceof SparseVectorSignature) {
			SparseVectorSignature s1 = (SparseVectorSignature) vs1;
			SparseVectorSignature s2 = (SparseVectorSignature) vs2;

			Iterator<Integer> idx1it = s1.iterator();
			Iterator<Integer> idx2it = s2.iterator();

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
					v1 = s1.get(idx1);
					v2 = s2.get(idx2);
					dist += v1 * v2;
					sqsum1 += v1 * v1;
					sqsum2 += v2 * v2;

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
					v2 = s2.get(idx2);
					sqsum2 += v2 * v2;
					if (idx2it.hasNext()) {
						idx2 = idx2it.next();
					} else {
						idx2 = Integer.MAX_VALUE;
					}
				} else {
					v1 = s1.get(idx1);
					sqsum1 += v1 * v1;
					if (idx1it.hasNext()) {
						idx1 = idx1it.next();
					} else {
						idx1 = Integer.MAX_VALUE;
					}
				}
			}

		} else if (vs1 instanceof DenseVectorSignature && vs2 instanceof DenseVectorSignature) {
			double[] s1 = ((DenseVectorSignature) vs1).getData();
			double[] s2 = ((DenseVectorSignature) vs2).getData();

			for (int i = 0; i < s1.length; i++) {
				dist += s1[i] * s2[i];
				sqsum1 += s1[i] * s1[i];
				sqsum2 += s2[i] * s2[i];
			}
		} else {
			double v1 = 0;
			double v2 = 0;
			for (int i = 0; i < vs1.getSize(); i++) {
				v1 = vs1.get(i);
				v2 = vs2.get(i);
				dist += v1 * v2;
				sqsum1 += v1 * v1;
				sqsum2 += v2 * v2;
			}
		}

		double sq = Math.sqrt(sqsum1 * sqsum2);

		if ((sq == 0.) || (dist == 0.)) {
			dist = 2.;
		} else {
			double r = dist / sq;
			if (r > 1) {
				dist = 0;
			} else {
				dist = Math.acos(r);
			}
		}

		return dist;
	}

}
