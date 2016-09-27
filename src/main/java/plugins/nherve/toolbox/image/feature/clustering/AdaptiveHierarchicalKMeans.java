/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of NHerve Main Toolbox, which is an ICY plugin.
 * 
 * NHerve Main Toolbox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NHerve Main Toolbox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with NHerve Main Toolbox. If not, see <http://www.gnu.org/licenses/>.
 */
package plugins.nherve.toolbox.image.feature.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import plugins.nherve.toolbox.image.feature.DefaultClusteringAlgorithmImpl;
import plugins.nherve.toolbox.image.feature.Distance;
import plugins.nherve.toolbox.image.feature.signature.SignatureException;
import plugins.nherve.toolbox.image.feature.signature.DefaultVectorSignature;


/**
 * The Class AdaptiveHierarchicalKMeans.
 * 
 * @author Nicolas HERVE - nicolas.herve@pasteur.fr
 */
public class AdaptiveHierarchicalKMeans extends DefaultClusteringAlgorithmImpl<DefaultVectorSignature> implements Distance<DefaultVectorSignature> {
	
	/** The sub display enabled. */
	private boolean subDisplayEnabled;
	
	/** The each level nb classes. */
	private int eachLevelNbClasses;
	
	/** The each level nb max iterations. */
	private int eachLevelNbMaxIterations;
	
	/** The each level stabilization criterion. */
	private double eachLevelStabilizationCriterion;
	
	/** The final nb cluster. */
	private int finalNbCluster;
	
	/** The kmeans. */
	private KMeans kmeans;

	/**
	 * Instantiates a new adaptive hierarchical k means.
	 * 
	 * @param finalNbCluster
	 *            the final nb cluster
	 * @param eachLevelNbClasses
	 *            the each level nb classes
	 * @param eachLevelNbMaxIterations
	 *            the each level nb max iterations
	 * @param eachLevelStabilizationCriterion
	 *            the each level stabilization criterion
	 */
	public AdaptiveHierarchicalKMeans(int finalNbCluster, int eachLevelNbClasses, int eachLevelNbMaxIterations, double eachLevelStabilizationCriterion) {
		super(false);
		this.finalNbCluster = finalNbCluster;
		this.eachLevelNbClasses = eachLevelNbClasses;
		this.eachLevelNbMaxIterations = eachLevelNbMaxIterations;
		this.eachLevelStabilizationCriterion = eachLevelStabilizationCriterion;
		setSubDisplayEnabled(false);
	}

	/* (non-Javadoc)
	 * @see plugins.nherve.toolbox.image.feature.ClusteringAlgorithm#compute(java.util.List)
	 */
	@Override
	public void compute(List<DefaultVectorSignature> points) throws ClusteringException {
		Map<DefaultVectorSignature, List<DefaultVectorSignature>> data = new HashMap<DefaultVectorSignature, List<DefaultVectorSignature>>();
		data.put(null, points);

		List<DefaultVectorSignature> unableTocluster = new ArrayList<DefaultVectorSignature>();

		int iter = 0;
		List<DefaultVectorSignature> current = null;
		while (data.size() + unableTocluster.size() < finalNbCluster) {
			DefaultVectorSignature biggest = null;
			int biggestSize = 0;
			for (DefaultVectorSignature c : data.keySet()) {
				if (data.get(c).size() > biggestSize) {
					biggestSize = data.get(c).size();
					biggest = c;
				}
			}

			current = data.get(biggest);
			data.remove(biggest);

			kmeans = new KMeans(eachLevelNbClasses, eachLevelNbMaxIterations, eachLevelStabilizationCriterion);
			kmeans.setLogEnabled(isSubDisplayEnabled());
			String dbg = "";
			try {
				kmeans.sanityCheck(current);
				kmeans.compute(current);

				int[] aff = kmeans.getAffectations();
				List<DefaultVectorSignature> ct = kmeans.getCentroids();
				ArrayList<ArrayList<DefaultVectorSignature>> ppc = new ArrayList<ArrayList<DefaultVectorSignature>>();
				for (int i = 0; i < ct.size(); i++) {
					ppc.add(new ArrayList<DefaultVectorSignature>());
				}
				int a = 0;
				for (DefaultVectorSignature vs : current) {
					ppc.get(aff[a]).add(vs);
					a++;
				}

				for (int i = 0; i < ct.size(); i++) {
					dbg += ppc.get(i).size() + " ";
					data.put(ct.get(i), ppc.get(i));
				}
			} catch (SignatureException e) {
				unableTocluster.add(biggest);
			}
			log("[" + iter + "] " + biggestSize + " : " + dbg);
			iter++;
		}

		kmeans = new KMeans(data.size(), 0, 0);
		kmeans.setLogEnabled(isLogEnabled());
		kmeans.setInitialCentroidsType(KMeans.PROVIDED_INTITAL_CENTROIDS);
		for (DefaultVectorSignature ct : data.keySet()) {
			kmeans.addInitialCentroid(ct, false);
		}
		for (DefaultVectorSignature ct : unableTocluster) {
			kmeans.addInitialCentroid(ct, false);
		}
		kmeans.compute(points);
	}

	/* (non-Javadoc)
	 * @see plugins.nherve.toolbox.image.feature.ClusteringAlgorithm#getAffectations(java.util.List)
	 */
	@Override
	public int[] getAffectations(List<DefaultVectorSignature> points) throws ClusteringException {
		return kmeans.getAffectations(points);
	}

	/* (non-Javadoc)
	 * @see plugins.nherve.toolbox.image.feature.ClusteringAlgorithm#getCentroids()
	 */
	@Override
	public List<DefaultVectorSignature> getCentroids() throws ClusteringException {
		return kmeans.getCentroids();
	}

	/* (non-Javadoc)
	 * @see plugins.nherve.toolbox.image.feature.ClusteringAlgorithm#getNbClasses()
	 */
	@Override
	public int getNbClasses() {
		return kmeans.getNbClasses();
	}

	/* (non-Javadoc)
	 * @see plugins.nherve.toolbox.image.feature.Distance#computeDistance(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double computeDistance(DefaultVectorSignature s1, DefaultVectorSignature s2) throws SignatureException {
		return kmeans.computeDistance(s1, s2);
	}

	/**
	 * Checks if is sub display enabled.
	 * 
	 * @return true, if is sub display enabled
	 */
	public boolean isSubDisplayEnabled() {
		return subDisplayEnabled;
	}

	/**
	 * Sets the sub display enabled.
	 * 
	 * @param subDisplayEnabled
	 *            the new sub display enabled
	 */
	public void setSubDisplayEnabled(boolean subDisplayEnabled) {
		this.subDisplayEnabled = subDisplayEnabled;
	}

}
