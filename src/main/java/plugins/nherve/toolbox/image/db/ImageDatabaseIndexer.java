/*
 * Copyright 2010, 2011 Institut Pasteur.
 * Copyright 2012, 2013 Institut National de l'Audiovisuel.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import plugins.nherve.toolbox.Algorithm;
import plugins.nherve.toolbox.concurrent.TaskException;
import plugins.nherve.toolbox.concurrent.TaskManager;
import plugins.nherve.toolbox.image.ImageLoader;
import plugins.nherve.toolbox.image.feature.SegmentableImage;
import plugins.nherve.toolbox.image.feature.SupportRegion;
import plugins.nherve.toolbox.image.feature.SupportRegionFactory;
import plugins.nherve.toolbox.image.feature.descriptor.GlobalDescriptor;
import plugins.nherve.toolbox.image.feature.descriptor.LocalDescriptor;
import plugins.nherve.toolbox.image.feature.region.Pixel;
import plugins.nherve.toolbox.image.feature.signature.BagOfSignatures;
import plugins.nherve.toolbox.image.feature.signature.DefaultVectorSignature;
import plugins.nherve.toolbox.image.feature.signature.SignatureException;

/**
 * The Class ImageDatabaseIndexer.
 * 
 * @author Nicolas HERVE - nherve@ina.fr
 */
public class ImageDatabaseIndexer<T extends SegmentableImage> extends Algorithm {

	private class PartialDumpProcess implements Runnable {
		@Override
		public void run() {
			info("PartialDumpProcess started");
			ImageDatabasePersistence<T> ptv = new ImageDatabasePersistence<T>(db);
			ptv.setLogEnabled(isLogEnabled());

			info("processed : " + countProcessed + " - ignored : " + countIgnored);

			while (!readyToDumpHeaders && running) {
				try {
					Thread.sleep(getPartialDumpSleep() / 10);
				} catch (InterruptedException e) {
					// ignore
				}
			}

			try {
				info("processed : " + countProcessed + " - ignored : " + countIgnored);
				ptv.dumpHeaders();
			} catch (IOException e1) {
				err(e1);
			}

			while (running) {
				try {
					Thread.sleep(getPartialDumpSleep());
					try {
						info("processed : " + countProcessed + " - ignored : " + countIgnored);
						ptv.dumpSignatures();
					} catch (IOException e) {
						err(e);
					}
				} catch (InterruptedException e) {
					// ignore
				}
			}

			info("PartialDumpProcess stopped");
		}
	}

	/**
	 * The Class SingleImageWorker.
	 * 
	 * @author Nicolas HERVE - nicolas.herve@pasteur.fr
	 */
	private class SingleImageWorker implements Callable<Integer> {

		/** The e. */
		private ImageEntry<T> e;
		private T sbi;
		private boolean imageLoaded;

		/**
		 * Instantiates a new single image worker.
		 * 
		 * @param e
		 *            the e
		 */
		public SingleImageWorker(ImageEntry<T> e) {
			super();
			this.e = e;
			sbi = null;
			imageLoaded = false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public Integer call() throws Exception {
			try {
				if ((globalDescriptors.size() + localDescriptors.size() + entryDescriptors.size()) > 0) {
					Map<String, List<SupportRegion>> srCache = new HashMap<String, List<SupportRegion>>();
					for (String name : localDescriptors.keySet()) {
						if (!isDoOnlyMissingStuff() || !e.getLocalSignatures().containsKey(name)) {
							loadImage();
							List<SupportRegion> sr = null;
							String srn = factoryForLocalDescriptor.get(name);
							if (srn != null) {
								if (srCache.containsKey(srn)) {
									sr = srCache.get(srn);
								} else {
									sr = regionFactories.get(srn).extractRegions(sbi);
									srCache.put(srn, sr);
								}
							}
							LocalDescriptor<T, DefaultVectorSignature, Pixel> desc = localDescriptors.get(name);
							desc.preProcess(sbi);
							BagOfSignatures<DefaultVectorSignature> bag = new BagOfSignatures<DefaultVectorSignature>();
							for (SupportRegion reg : sr) {
								DefaultVectorSignature sig = desc.extractLocalSignature(sbi, reg);
								bag.add(sig);
							}
							desc.postProcess(sbi);
							e.putSignature(name, bag);
						}
					}
					srCache.clear();
					srCache = null;

					for (String name : globalDescriptors.keySet()) {
						if (!isDoOnlyMissingStuff() || !e.getGlobalSignatures().containsKey(name)) {
							loadImage();
							GlobalDescriptor<T, DefaultVectorSignature> desc = globalDescriptors.get(name);
							desc.preProcess(sbi);
							DefaultVectorSignature sig = desc.extractGlobalSignature(sbi);
							desc.postProcess(sbi);
							e.putSignature(name, sig);
							countProcessed++;
						} else {
							countIgnored++;
						}
					}

					for (String name : entryDescriptors.keySet()) {
						if (!isDoOnlyMissingStuff() || !e.getGlobalSignatures().containsKey(name)) {
							loadImage();
							GlobalDescriptor<ImageEntry<T>, DefaultVectorSignature> desc = entryDescriptors.get(name);
							desc.preProcess(e);
							DefaultVectorSignature sig = desc.extractGlobalSignature(e);
							desc.postProcess(e);
							e.putSignature(name, sig);
						}
					}

					unloadImage();
				}
				readyToDumpHeaders = true;
				return 0;
			} catch (Throwable error) {
				e.setError(error);
				return 1;
			}
		}

		private void loadImage() throws IOException {
			if (loadImages && !imageLoaded) {
				try {
					db.loadImage(e, imageLoader);
					imageLoaded = true;
				} catch (Throwable t) {
					throw new IOException(t);
				}
			}
			if (sbi == null) {
				sbi = e.getImage();
				if (sbi != null) {
					sbi.setName(e.getFile());
				}
			}
		}

		private void unloadImage() {
			if (imageLoaded) {
				db.unloadImage(e);
				sbi = null;
			}
		}

	}

	/** The db. */
	private ImageDatabase<T> db;

	/** The global descriptors. */
	private Map<String, GlobalDescriptor<T, DefaultVectorSignature>> globalDescriptors;

	/** The region factories. */
	private Map<String, SupportRegionFactory> regionFactories;

	/** The local descriptors. */
	private Map<String, LocalDescriptor<T, DefaultVectorSignature, Pixel>> localDescriptors;

	/** The factory for local descriptor. */
	private Map<String, String> factoryForLocalDescriptor;

	/** The entry descriptors. */
	private Map<String, GlobalDescriptor<ImageEntry<T>, DefaultVectorSignature>> entryDescriptors;
	/** The load images. */
	private boolean loadImages;
	private boolean doPartialDump;
	private long partialDumpSleep;
	private boolean readyToDumpHeaders;

	private boolean running;
	private boolean doOnlyMissingStuff;

	private int countIgnored;

	private int countProcessed;

	private ImageLoader<T> imageLoader;

	/**
	 * Instantiates a new image database indexer.
	 * 
	 * @param db
	 *            the db
	 */
	public ImageDatabaseIndexer(ImageDatabase<T> db, ImageLoader<T> imageLoader) {
		super();
		this.db = db;
		this.loadImages = true;
		this.globalDescriptors = new HashMap<String, GlobalDescriptor<T, DefaultVectorSignature>>();
		this.regionFactories = new HashMap<String, SupportRegionFactory>();
		this.localDescriptors = new HashMap<String, LocalDescriptor<T, DefaultVectorSignature, Pixel>>();
		this.factoryForLocalDescriptor = new HashMap<String, String>();
		this.entryDescriptors = new HashMap<String, GlobalDescriptor<ImageEntry<T>, DefaultVectorSignature>>();

		setImageLoader(imageLoader);
		setDoPartialDump(false);
		setPartialDumpSleep(5 * 60 * 1000);
		setDoOnlyMissingStuff(false);
		running = false;
	}

	/**
	 * Instantiates a new image database indexer.
	 * 
	 * @param db
	 *            the db
	 * @param name
	 *            the name
	 * @param desc
	 *            the desc
	 */
	public ImageDatabaseIndexer(ImageDatabase<T> db, ImageLoader<T> imageLoader, String name, GlobalDescriptor<T, DefaultVectorSignature> desc) {
		this(db, imageLoader);
		addGlobalDescriptor(name, desc);
	}

	/**
	 * Adds the entry descriptor.
	 * 
	 * @param name
	 *            the name
	 * @param desc
	 *            the desc
	 */
	public void addEntryDescriptor(String name, GlobalDescriptor<ImageEntry<T>, DefaultVectorSignature> desc) {
		entryDescriptors.put(name, desc);
	}

	/**
	 * Adds the global descriptor.
	 * 
	 * @param name
	 *            the name
	 * @param desc
	 *            the desc
	 */
	public void addGlobalDescriptor(String name, GlobalDescriptor<T, DefaultVectorSignature> desc) {
		globalDescriptors.put(name, desc);
	}

	/**
	 * Adds the local descriptor.
	 * 
	 * @param name
	 *            the name
	 * @param rf
	 *            the rf
	 * @param desc
	 *            the desc
	 */
	public void addLocalDescriptor(String name, String rf, LocalDescriptor<T, DefaultVectorSignature, Pixel> desc) {
		factoryForLocalDescriptor.put(name, rf);
		localDescriptors.put(name, desc);
	}

	/**
	 * Adds the region factory.
	 * 
	 * @param name
	 *            the name
	 * @param rf
	 *            the rf
	 */
	public void addRegionFactory(String name, SupportRegionFactory rf) {
		regionFactories.put(name, rf);
	}

	public ImageLoader<T> getImageLoader() {
		return imageLoader;
	}

	public long getPartialDumpSleep() {
		return partialDumpSleep;
	}

	public boolean isDoOnlyMissingStuff() {
		return doOnlyMissingStuff;
	}

	public boolean isDoPartialDump() {
		return doPartialDump;
	}

	/**
	 * Launch.
	 */
	public synchronized void launch() {
		running = true;
		readyToDumpHeaders = false;

		TaskManager tm = TaskManager.getMainInstance();

		loadImages = !regionFactories.isEmpty();
		if (!loadImages) {
			for (LocalDescriptor<T, DefaultVectorSignature, Pixel> ld : localDescriptors.values()) {
				if (ld.needToLoadSegmentable()) {
					loadImages = true;
					break;
				}
			}
			if (!loadImages) {
				for (GlobalDescriptor<T, DefaultVectorSignature> gd : globalDescriptors.values()) {
					if (gd.needToLoadSegmentable()) {
						loadImages = true;
						break;
					}
				}
				if (!loadImages) {
					for (GlobalDescriptor<ImageEntry<T>, DefaultVectorSignature> bd : entryDescriptors.values()) {
						if (bd.needToLoadSegmentable()) {
							loadImages = true;
							break;
						}
					}
				}
			}
		}

		try {
			for (LocalDescriptor<T, DefaultVectorSignature, Pixel> d : localDescriptors.values()) {
				d.initForDatabase(db);
			}
			for (GlobalDescriptor<T, DefaultVectorSignature> d : globalDescriptors.values()) {
				d.initForDatabase(db);
			}
			for (GlobalDescriptor<ImageEntry<T>, DefaultVectorSignature> d : entryDescriptors.values()) {
				d.initForDatabase(db);
			}
		} catch (SignatureException e1) {
			e1.printStackTrace();
		}

		countIgnored = 0;
		countProcessed = 0;

		Thread partialDumpProcess = null;
		if (doPartialDump) {
			partialDumpProcess = new Thread(new PartialDumpProcess());
			partialDumpProcess.start();
		}

		List<Future<Integer>> results = new ArrayList<Future<Integer>>();
		for (ImageEntry<T> e : db) {
			results.add(tm.submit(new SingleImageWorker(e)));
		}

		try {
			tm.waitResults(results, "ImageDatabaseIndexer", 5000);
		} catch (TaskException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		running = false;

		for (ImageEntry<T> e : db) {
			if (e.getError() != null) {
				err(e.getFile() + " : " + e.getError().getClass().getName() + " - " + e.getError().getMessage());
			}
		}

		if (doPartialDump) {
			try {
				partialDumpProcess.interrupt();
				partialDumpProcess.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void setDoOnlyMissingStuff(boolean doOnlyMissingStuff) {
		this.doOnlyMissingStuff = doOnlyMissingStuff;
	}

	public void setDoPartialDump(boolean doPartialDump) {
		this.doPartialDump = doPartialDump;
	}

	public void setImageLoader(ImageLoader<T> imageLoader) {
		this.imageLoader = imageLoader;
	}

	public void setPartialDumpSleep(long partialDumpSleep) {
		this.partialDumpSleep = partialDumpSleep;
	}

}
