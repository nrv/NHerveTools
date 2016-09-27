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
package plugins.nherve.toolbox.image.db;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import plugins.nherve.toolbox.Algorithm;
import plugins.nherve.toolbox.image.feature.FeatureException;
import plugins.nherve.toolbox.image.feature.SegmentableImage;
import plugins.nherve.toolbox.image.feature.learning.ClassifierException;
import plugins.nherve.toolbox.image.feature.learning.LearningAlgorithm;
import plugins.nherve.toolbox.image.feature.signature.SignatureException;
import plugins.nherve.toolbox.image.feature.signature.DefaultVectorSignature;


/**
 * The Class ImageDatabaseSplit.
 * 
 * @author Nicolas HERVE - nicolas.herve@pasteur.fr
 */
public class ImageDatabaseSplit<T extends SegmentableImage> extends Algorithm {
	
	/** The lrn entries. */
	private ImageDatabase<T> lrnEntries;
	
	/** The tst entries. */
	private ImageDatabase<T> tstEntries;

	/** The sig size. */
	private int sigSize;

	/**
	 * Instantiates a new image database split.
	 */
	public ImageDatabaseSplit() {
		super();
		lrnEntries = new ImageDatabase<T>();
		tstEntries = new ImageDatabase<T>();
		sigSize = -1;
	}

	/**
	 * Split.
	 * 
	 * @param lrn
	 *            the lrn
	 * @param tst
	 *            the tst
	 * @return the image database split
	 * @throws FeatureException
	 *             the feature exception
	 */
	public static <T extends SegmentableImage> ImageDatabaseSplit<T> split(ImageDatabase<T> lrn, ImageDatabase<T> tst) throws FeatureException {
		ImageDatabaseSplit<T> split = new ImageDatabaseSplit<T>();
		split.lrnEntries = lrn;
		split.tstEntries = tst;

		split.lrnEntries.updateAvailableDescriptors();
		split.lrnEntries.updateClassesEntries();
		split.tstEntries.updateAvailableDescriptors();
		split.tstEntries.updateClassesEntries();

		return split;
	}

	/**
	 * Split random.
	 * 
	 * @param db
	 *            the db
	 * @param cls
	 *            the cls
	 * @param lrnPct
	 *            the lrn pct
	 * @return the image database split
	 * @throws FeatureException
	 *             the feature exception
	 */
	public static <T extends SegmentableImage> ImageDatabaseSplit<T> splitRandom(ImageDatabase<T> db, String cls, double lrnPct) throws FeatureException {
		if (!db.containsClass(cls)) {
			throw new FeatureException("Unable to find class " + cls + " in " + db.getName());
		}

		throw new FeatureException("Implementation not ready yet !");
	}

	/**
	 * Split random.
	 * 
	 * @param db
	 *            the db
	 * @param lrnPct
	 *            the lrn pct
	 * @param forceAtLeastOnePosAndNeg
	 *            the force at least one pos and neg
	 * @param cls
	 *            the cls
	 * @param groundTruthPerturbation
	 *            the ground truth perturbation
	 * @param pctPerturbation
	 *            the pct perturbation
	 * @return the image database split
	 */
	public static <T extends SegmentableImage> ImageDatabaseSplit<T> splitRandom(ImageDatabase<T> db, double lrnPct, boolean forceAtLeastOnePosAndNeg, String cls, boolean groundTruthPerturbation, double pctPerturbation) {
		Random rd = new Random(System.currentTimeMillis());

		ImageDatabaseSplit<T> split = new ImageDatabaseSplit<T>();
		split.lrnEntries.setName(db.getName() + " (lrn)");
		split.tstEntries.setName(db.getName() + " (tst)");

		int lrnSize = (int) (db.size() * lrnPct);

		if (forceAtLeastOnePosAndNeg && (lrnSize < 2)) {
			lrnSize = 2;
		}

		if (forceAtLeastOnePosAndNeg) {
			ImageEntry<T> e = null;

			do {
				e = db.get(rd.nextInt(db.size()));
			} while (!e.containsClass(cls));
			split.lrnEntries.add(e);

			do {
				e = db.get(rd.nextInt(db.size()));
			} while (e.containsClass(cls));
			split.lrnEntries.add(e);
		}

		while (split.lrnEntries.size() < lrnSize) {
			ImageEntry<T> e = db.get(rd.nextInt(db.size()));
			if (!split.lrnEntries.contains(e)) {
				split.lrnEntries.add(e);
			}
		}

		for (ImageEntry<T> e : db) {
			if (!split.lrnEntries.contains(e)) {
				split.tstEntries.add(e);
			}
		}

		if (groundTruthPerturbation) {
			int nbToChange = (int) ((double) lrnSize * pctPerturbation);
			boolean[] alreadyChanged = new boolean[lrnSize];
			Arrays.fill(alreadyChanged, false);
			for (int i = 0; i < nbToChange; i++) {
				int change = -1;
				do {
					change = rd.nextInt(lrnSize);
				} while (alreadyChanged[change]);
				
				ImageEntry<T> e = split.lrnEntries.get(change);
				ImageEntry<T> e2 = null;
				e2 = e.cloneForSplit();
				if (e2.containsClass(cls)) {
					e2.removeClass(cls);
				} else {
					e2.putClass(cls);
				}
				split.lrnEntries.set(change, e2);
				alreadyChanged[change] = true;
			}
		}

		split.lrnEntries.updateAvailableDescriptors();
		split.lrnEntries.updateClassesEntries();
		split.tstEntries.updateAvailableDescriptors();
		split.tstEntries.updateClassesEntries();

		return split;
	}

	/**
	 * Lrn size.
	 * 
	 * @return the int
	 */
	public int lrnSize() {
		return lrnEntries.size();
	}

	/**
	 * Tst size.
	 * 
	 * @return the int
	 */
	public int tstSize() {
		return tstEntries.size();
	}

	/**
	 * Size.
	 * 
	 * @param tdb
	 *            the tdb
	 * @param cls
	 *            the cls
	 * @param pos
	 *            the pos
	 * @return the int
	 */
	private int size(ImageDatabase<T> tdb, String cls, boolean pos) {
		if (tdb.containsClass(cls)) {
			return tdb.getEntries(cls, pos).size();
		} else {
			return 0;
		}
	}

	/**
	 * Gets the global signatures.
	 * 
	 * @param tdb
	 *            the tdb
	 * @param descs
	 *            the descs
	 * @return the global signatures
	 * @throws FeatureException
	 *             the feature exception
	 */
	private List<DefaultVectorSignature> getGlobalSignatures(ImageDatabase<T> tdb, String descs) throws FeatureException {
		return getGlobalSignatures(tdb, tdb.getEntries(), descs);
	}

	/**
	 * Gets the global signatures.
	 * 
	 * @param tdb
	 *            the tdb
	 * @param entries
	 *            the entries
	 * @param descs
	 *            the descs
	 * @return the global signatures
	 * @throws FeatureException
	 *             the feature exception
	 */
	private List<DefaultVectorSignature> getGlobalSignatures(ImageDatabase<T> tdb, List<ImageEntry<T>> entries, String descs) throws FeatureException {
		List<DefaultVectorSignature> res = tdb.getGlobalSignatures(entries, descs);

		if (sigSize < 0) {
			sigSize = res.get(0).getSize();
			log("Signature size : " + sigSize);
		}

		return res;
	}

	/**
	 * Gets the global signatures.
	 * 
	 * @param tdb
	 *            the tdb
	 * @param cls
	 *            the cls
	 * @param pos
	 *            the pos
	 * @param descs
	 *            the descs
	 * @return the global signatures
	 * @throws FeatureException
	 *             the feature exception
	 */
	private List<DefaultVectorSignature> getGlobalSignatures(ImageDatabase<T> tdb, String cls, boolean pos, String descs) throws FeatureException {
		List<ImageEntry<T>> entries = tdb.getEntries(cls, pos);

		return getGlobalSignatures(tdb, entries, descs);
	}

	/**
	 * Gets the lrn signatures.
	 * 
	 * @param cls
	 *            the cls
	 * @param pos
	 *            the pos
	 * @param desc
	 *            the desc
	 * @return the lrn signatures
	 * @throws FeatureException
	 *             the feature exception
	 */
	public List<DefaultVectorSignature> getLrnSignatures(String cls, boolean pos, String desc) throws FeatureException {
		return getGlobalSignatures(lrnEntries, cls, pos, desc);
	}

	/**
	 * Lrn size.
	 * 
	 * @param cls
	 *            the cls
	 * @param pos
	 *            the pos
	 * @return the int
	 */
	public int lrnSize(String cls, boolean pos) {
		return size(lrnEntries, cls, pos);
	}

	/**
	 * Tst size.
	 * 
	 * @param cls
	 *            the cls
	 * @param pos
	 *            the pos
	 * @return the int
	 */
	public int tstSize(String cls, boolean pos) {
		return size(tstEntries, cls, pos);
	}

	/**
	 * Signature used size.
	 * 
	 * @param desc
	 *            the desc
	 * @return the double
	 * @throws SignatureException
	 *             the signature exception
	 * @throws FeatureException
	 *             the feature exception
	 */
	public double signatureUsedSize(String desc) throws SignatureException, FeatureException {
		double totalNonZero = 0;
		for (DefaultVectorSignature s : getGlobalSignatures(lrnEntries, desc)) {
			totalNonZero += s.getNonZeroBins();
		}
		for (DefaultVectorSignature s : getGlobalSignatures(tstEntries, desc)) {
			totalNonZero += s.getNonZeroBins();
		}
		double avgNonZero = totalNonZero / (lrnSize() + tstSize());
		return avgNonZero;
	}

	/**
	 * Predict.
	 * 
	 * @param model
	 *            the model
	 * @param desc
	 *            the desc
	 * @return the prediction
	 * @throws FeatureException
	 *             the feature exception
	 * @throws ClassifierException
	 *             the classifier exception
	 */
	public Prediction predict(LearningAlgorithm model, String desc) throws FeatureException, ClassifierException {
		List<DefaultVectorSignature> sigs = getGlobalSignatures(tstEntries, desc);

		Prediction pred = new Prediction(model.getModelInfo());
		pred.setLogEnabled(model.isLogEnabled());
		int idx = 0;
		for (ImageEntry<T> e : tstEntries) {
			double sco = model.score(sigs.get(idx));
			pred.add(e, sco);
			idx++;
		}

		return pred;
	}

	/**
	 * Available lrn classes.
	 * 
	 * @return the sets the
	 */
	public Set<String> availableLrnClasses() {
		return lrnEntries.getAvailableClasses();
	}

}
