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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import plugins.nherve.toolbox.Algorithm;
import plugins.nherve.toolbox.PersistenceToolbox;
import plugins.nherve.toolbox.image.feature.SegmentableImage;
import plugins.nherve.toolbox.image.feature.signature.BagOfSignatures;
import plugins.nherve.toolbox.image.feature.signature.DefaultVectorSignature;

/**
 * The Class ImageDatabasePersistence.
 * 
 * @author Nicolas HERVE - nicolas.herve@pasteur.fr
 */
public class ImageDatabasePersistence<T extends SegmentableImage> extends Algorithm {

	/** The Constant HEADERS_FILE. */
	public final static String HEADERS_FILE = "db_headers";

	/** The Constant SIGNATURES_FILE. */
	public final static String SIGNATURES_FILE = "sigs_";

	/** The Constant EXT. */
	public final static String EXT = ".obj";

	/** The Constant EOL. */
	public final static String EOL = "\n";

	/** The root directory. */
	private String rootDirectory;

	/** The db. */
	private ImageDatabase<T> db;

	/**
	 * Instantiates a new image database persistence.
	 * 
	 * @param db
	 *            the db
	 */
	public ImageDatabasePersistence(ImageDatabase<T> db) {
		this(db.getRootSignatureDirectory());
		this.db = db;
	}

	/**
	 * Instantiates a new image database persistence.
	 * 
	 * @param rootDirectory
	 *            the root directory
	 */
	public ImageDatabasePersistence(String rootDirectory) {
		super(true);
		this.db = null;
		this.rootDirectory = rootDirectory;

	}

	/**
	 * Dump.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void dump() throws IOException {
		info("Dumping database " + db.getName());
		dumpHeaders();
		dumpSignatures();
	}

	public void dumpSignatures() throws IOException {
		for (String d : db.getAvailableGlobalDescriptors()) {
			dumpSignatures(d);
		}
		for (String d : db.getAvailableLocalDescriptors()) {
			dumpSignatures(d);
		}
	}

	/**
	 * Dump headers.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void dumpHeaders() throws IOException {
		info("Dumping database headers for " + db.getName());
		RandomAccessFile raf = null;
		try {
			raf = getHeadersFile(true);
			FileChannel fc = raf.getChannel();
			PersistenceToolbox.dumpString(fc, ImageDatabase.VERSION);
			PersistenceToolbox.dumpString(fc, db.getName());
			PersistenceToolbox.dumpString(fc, db.getImageDirectory());
			PersistenceToolbox.dumpLong(fc, db.getNextId());

			db.updateAvailableDescriptors();
			Set<String> alldescs = db.getAllDescriptors();
			PersistenceToolbox.dumpInt(fc, alldescs.size());
			for (String d : alldescs) {
				PersistenceToolbox.dumpBoolean(fc, db.containsLocalDescriptor(d));
				PersistenceToolbox.dumpString(fc, d);
			}

			List<ImageEntry<T>> entries = db.getEntries();
			PersistenceToolbox.dumpInt(fc, entries.size());
			for (ImageEntry<T> e : entries) {
				PersistenceToolbox.dumpLong(fc, e.getId());
				PersistenceToolbox.dumpString(fc, e.getFile());
				Map<String, Double> cls = e.getClasses();
				PersistenceToolbox.dumpInt(fc, cls.size());
				for (String c : cls.keySet()) {
					double v = cls.get(c);
					PersistenceToolbox.dumpString(fc, c);
					PersistenceToolbox.dumpDouble(fc, v);
				}
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (raf != null) {
				raf.close();
			}
		}
		info("Dumping headers done");
	}

	/**
	 * Dump signatures.
	 * 
	 * @param desc
	 *            the desc
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void dumpSignatures(String desc) throws IOException {
		info("Dumping descriptor " + desc + " for " + db.getName());
		boolean global = true;

		if (db.containsLocalDescriptor(desc)) {
			global = false;
		}

		RandomAccessFile raf = null;
		int count = 0;
		try {
			raf = getSignaturesFile(desc, true);
			FileChannel fc = raf.getChannel();
			PersistenceToolbox.dumpInt(fc, db.size());
			for (ImageEntry<T> e : db.getEntries()) {
				PersistenceToolbox.dumpLong(fc, e.getId());
				if (global) {
					DefaultVectorSignature vs = e.getGlobalSignatures().get(desc);
					if (vs != null) {
						count++;
					}
					PersistenceToolbox.dumpSignature(fc, vs);
				} else {
					BagOfSignatures<DefaultVectorSignature> bs = e.getLocalSignatures().get(desc);
					if (bs != null) {
						count++;
					}
					PersistenceToolbox.dumpSignature(fc, bs);
				}
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (raf != null) {
				raf.close();
			}
		}
		info("Dumping descriptor " + desc + " done (" + count + " / " + db.size() + ")");
	}

	/**
	 * Gets the db.
	 * 
	 * @return the db
	 */
	public ImageDatabase<T> getDb() {
		return db;
	}

	/**
	 * Gets the headers file.
	 * 
	 * @param write
	 *            the write
	 * @return the headers file
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	protected RandomAccessFile getHeadersFile(boolean write) throws FileNotFoundException {
		return PersistenceToolbox.getFile(new File(rootDirectory, HEADERS_FILE + EXT), write);
	}

	/**
	 * Gets the signatures file.
	 * 
	 * @param desc
	 *            the desc
	 * @param write
	 *            the write
	 * @return the signatures file
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	protected RandomAccessFile getSignaturesFile(String desc, boolean write) throws FileNotFoundException {
		return PersistenceToolbox.getFile(new File(rootDirectory, SIGNATURES_FILE + desc + EXT), write);
	}

	public void load() throws IOException {
		load(true);
	}
	
	/**
	 * Load.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void load(boolean stopOnFirstError) throws IOException {
		loadHeaders();
		for (String d : db.getAllDescriptors()) {
			try {
				loadSignatures(d);
			} catch (IOException e) {
				if (stopOnFirstError) {
					throw e;
				} else {
					info(e.getMessage());
				}
			}
		}
		db.updateAvailableDescriptors();
	}

	/**
	 * Load headers.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void loadHeaders() throws IOException {
		info("Loading headers");
		RandomAccessFile raf = null;
		try {
			raf = getHeadersFile(false);
			FileChannel fc = raf.getChannel();

			db = new ImageDatabase<T>();
			db.setRootDirectory(rootDirectory);
			List<ImageEntry<T>> entries = db.getEntries();
			String version = PersistenceToolbox.loadString(fc);
			if (!ImageDatabase.VERSION.equals(version)) {
				throw new IOException("Incompatible data version (" + version + "/" + ImageDatabase.VERSION + ")");
			}
			db.setName(PersistenceToolbox.loadString(fc));
			db.setImageDirectory(PersistenceToolbox.loadString(fc));
			db.setNextId(PersistenceToolbox.loadLong(fc));

			Set<String> alldescs = db.getAllDescriptors();
			Set<String> localdescs = db.getAvailableLocalDescriptors();
			Set<String> globaldescs = db.getAvailableGlobalDescriptors();
			int nbDesc = PersistenceToolbox.loadInt(fc);
			for (int i = 0; i < nbDesc; i++) {
				boolean local = PersistenceToolbox.loadBoolean(fc);
				String desc = PersistenceToolbox.loadString(fc);
				if (local) {
					localdescs.add(desc);
				} else {
					globaldescs.add(desc);
				}
				alldescs.add(desc);
			}

			int nbEntries = PersistenceToolbox.loadInt(fc);
			for (int i = 0; i < nbEntries; i++) {
				ImageEntry<T> e = new ImageEntry<T>();
				e.setId(PersistenceToolbox.loadLong(fc));
				e.setFile(PersistenceToolbox.loadString(fc));
				int nbc = PersistenceToolbox.loadInt(fc);
				for (int c = 0; c < nbc; c++) {
					String cls = PersistenceToolbox.loadString(fc);
					double v = PersistenceToolbox.loadDouble(fc);
					e.putClass(cls, v);
				}
				entries.add(e);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (raf != null) {
				raf.close();
			}
			if (db != null) {
				db.updateClassesEntries();
			}
		}
	}

	/**
	 * Find matching descriptor name.
	 * 
	 * @param desc
	 *            the desc
	 * @return the list
	 */
	public List<String> findMatchingDescriptorName(String desc) {
		ArrayList<String> res = new ArrayList<String>();
		desc = desc.toUpperCase();
		for (String att : db.getAllDescriptors()) {
			if (att.toUpperCase().contains(desc)) {
				res.add(att);
			}
		}

		return res;
	}

	/**
	 * Load signatures.
	 * 
	 * @param desc
	 *            the desc
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void loadSignatures(String desc) throws IOException {
		loadSignatures(desc, false);
	}

	/**
	 * Load signatures.
	 * 
	 * @param desc
	 *            the desc
	 * @param allowPartialNameMatch
	 *            the allow partial name match
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void loadSignatures(String desc, boolean allowPartialNameMatch) throws IOException {
		info("Loading " + desc + " signatures");
		boolean global = true;

		if (allowPartialNameMatch) {
			for (String d : findMatchingDescriptorName(desc)) {
				loadSignatures(d);
			}
		} else {
			if (!db.getAllDescriptors().contains(desc)) {
				throw new IOException("Descriptor " + desc + " not available for loading on " + db.getName());
			}

			if (db.containsLocalDescriptor(desc)) {
				global = false;
			}

			RandomAccessFile raf = null;
			try {
				raf = getSignaturesFile(desc, false);
				FileChannel fc = raf.getChannel();
				int dbs = PersistenceToolbox.loadInt(fc);
				if (dbs != db.size()) {
					throw new IOException("Wrong number of signatures for " + desc + " (" + dbs + "/" + db.size() + ")");
				}
				int count = 0;
				for (ImageEntry<T> e : db.getEntries()) {
					count++;
					long id = PersistenceToolbox.loadLong(fc);
					if (id != e.getId()) {
						throw new IOException("Wrong id of entry for " + desc + " (" + id + "/" + e.getId() + ")");
					}
					if (global) {
						DefaultVectorSignature vs = PersistenceToolbox.loadVectorSignature(fc);
						if (vs != null) {
							e.putSignature(desc, vs);
						}
					} else {
						BagOfSignatures<DefaultVectorSignature> bs = PersistenceToolbox.loadBagOfSignatures(fc);
						if (bs != null) {
							e.putSignature(desc, bs);
						}
					}
					if (count % 10000 == 0) {
						info(" - " + count);
					}
				}
			} catch (IOException e) {
				throw e;
			} finally {
				if (raf != null) {
					raf.close();
				}
			}

		}
	}

}
