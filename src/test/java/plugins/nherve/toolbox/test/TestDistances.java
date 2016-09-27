package plugins.nherve.toolbox.test;

import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

import plugins.nherve.toolbox.image.feature.SignatureDistance;
import plugins.nherve.toolbox.image.feature.signature.DenseVectorSignature;
import plugins.nherve.toolbox.image.feature.signature.DynamicSparseVectorSignature;
import plugins.nherve.toolbox.image.feature.signature.L1Distance;
import plugins.nherve.toolbox.image.feature.signature.L2Distance;
import plugins.nherve.toolbox.image.feature.signature.SignatureException;
import plugins.nherve.toolbox.image.feature.signature.SparseVectorSignature;
import plugins.nherve.toolbox.image.feature.signature.VectorAngleDistance;
import plugins.nherve.toolbox.image.feature.signature.DefaultVectorSignature;
import plugins.nherve.toolbox.image.feature.signature.VectorSignature;

public class TestDistances {

	public Random rd = new Random(1586412l);

	public double[] randomData(int dim, double sparsity, boolean negValues) {
		double[] data = new double[dim];
		for (int d = 0; d < dim; d++) {
			if (rd.nextDouble() >= sparsity) {
				if (negValues) {
					data[d] = 1 - (2 * rd.nextDouble());
				} else {
					data[d] = rd.nextDouble();
				}
			} else {
				data[d] = 0.;
			}
		}
		return data;
	}

	@Test
	public void runAllTests() {
		try {
			for (int dim = 2; dim < 103; dim += 10) {
				for (double sparsity = 0; sparsity < 0.9; sparsity += 0.2) {
					test(dim, sparsity, 10, true, true);
					test(dim, sparsity, 10, false, true);
					test(dim, sparsity, 10, true, false);
					test(dim, sparsity, 10, false, false);
				}
			}
		} catch (SignatureException e) {
			Assert.fail(e.getClass().getName() + " : " + e.getMessage());
		}
	}

	public void test(int dim, double sparsity, int nb, boolean negValues, boolean norm) throws SignatureException {
		System.out.println("test(" + dim + ", " + sparsity + ", " + nb + ", " + negValues + ", " + norm + ")");
		final int sigType = 3;
		final int distType = 4;

		DefaultVectorSignature[][] sigs = new DefaultVectorSignature[sigType][nb];

		for (int n = 0; n < nb; n++) {
			double[] data = randomData(dim, sparsity, negValues);
			DenseVectorSignature dense = new DenseVectorSignature(dim);
			SparseVectorSignature sparse = new SparseVectorSignature(dim);
			DynamicSparseVectorSignature dynamic = new DynamicSparseVectorSignature();
			for (int d = 0; d < dim; d++) {
				dense.set(d, data[d]);
				sparse.set(d, data[d]);
				dynamic.set(d, data[d]);
			}
			if (norm) {
				dense.normalizeL2(false);
				sparse.normalizeL2(false);
				dynamic.normalizeL2(false);
			}
			sigs[0][n] = dense;
			sigs[1][n] = sparse;
			sigs[2][n] = dynamic;
		}

		SignatureDistance<VectorSignature>[] dist = new SignatureDistance[distType];
		dist[0] = new L1Distance();
		dist[1] = new L2Distance();
		dist[2] = new VectorAngleDistance();
		dist[3] = new VectorAngleDistance(true);

		for (int i = 0; i < nb; i++) {
			for (int j = 0; j < nb; j++) {
				for (int d = 0; d < distType; d++) {
					int idx = 0;
					double previous = 0;
					for (int m = 0; m < sigType; m++) {
						for (int n = 0; n < sigType; n++) {
							double computed = dist[d].computeDistance(sigs[m][i], sigs[n][j]);
							if (idx > 0) {
								Assert.assertEquals(previous, computed);
								// if (computed != previous) {
								// System.err.println("Error (" + dist[d].getClass().getName() + ") : " + previous + " / " + computed);
								// System.err.println(" - sigs[" + m + "][" + i + "] : " + sigs[m][i]);
								// System.err.println(" - sigs[" + n + "][" + j + "] : " + sigs[n][j]);
								// System.err.println("");
								// }
							}
							idx++;
							previous = computed;
						}
					}
				}
			}
		}
	}
}
