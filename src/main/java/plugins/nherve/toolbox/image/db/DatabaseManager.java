package plugins.nherve.toolbox.image.db;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import plugins.nherve.toolbox.Algorithm;
import plugins.nherve.toolbox.image.ImageLoader;
import plugins.nherve.toolbox.image.feature.FeatureException;
import plugins.nherve.toolbox.image.feature.SegmentableImage;
import plugins.nherve.toolbox.image.feature.signature.BagOfSignatures;
import plugins.nherve.toolbox.image.feature.signature.DefaultVectorSignature;

public class DatabaseManager<T extends SegmentableImage> extends Algorithm {

	public DatabaseManager() {
		super();
	}

	public DatabaseManager(boolean log) {
		super(log);
	}

	public static long getUniqueId(long imageId, int localDescriptorId) {
		return imageId * 100000000l + localDescriptorId;
	}

	public ImageDatabase<T> create(final DatabaseConfiguration conf) throws IOException {
		info("Creating a new database : " + conf);
		ImageDatabase<T> db = new ImageDatabase<T>(conf.getName(), conf.getRoot(), conf.getPictures(), conf.getSignatures());

		File imagesDirectory = new File(db.getRootImageDirectory());
		if (!imagesDirectory.exists()) {
			throw new IOException("Unknown images directory " + imagesDirectory.getAbsolutePath());
		}
		
		Path rootPath = imagesDirectory.toPath();
		
		List<Path> images = new ArrayList<>();
		Files.find(rootPath, Integer.MAX_VALUE, (filePath, fileAttr) -> filePath.getFileName().toString().endsWith(conf.getExtension())).forEach(images::add);

		for (Path image : images) {
			ImageEntry<T> e = new ImageEntry<T>(rootPath.relativize(image).toString());
			db.add(e);
		}

		info(" - found " + db.size() + " pictures");

		return db;
	}

	public void save(final ImageDatabase<T> db) throws IOException {
		ImageDatabasePersistence<T> ptv = new ImageDatabasePersistence<T>(db);
		ptv.dump();
	}

	public ImageDatabase<T> load(final DatabaseConfiguration conf) throws IOException {
		return load(conf, false);
	}

	public ImageDatabase<T> load(final DatabaseConfiguration conf, boolean headersOnly) throws IOException {
		info("Loading database " + conf.getName());
		ImageDatabasePersistence<T> ptv = new ImageDatabasePersistence<T>(conf.getRoot() + "/" + conf.getSignatures());
		ptv.setLogEnabled(isLogEnabled());
		if (headersOnly) {
			ptv.loadHeaders();
		} else {
			ptv.load(false);
		}
		ImageDatabase<T> db = ptv.getDb();
		db.setRootDirectory(conf.getRoot());
		db.setImageDirectory(conf.getPictures());
		db.setSignatureDirectory(conf.getSignatures());
		info("Loading done");
		return db;
	}

	public void index(final ImageDatabase<T> db, final ImageLoader<T> imageLoader, final IndexingConfiguration<T> conf, final boolean partialDump, final double waitMinutesBetweenEachDump, final boolean doOnlyMissingStuff) {
		if (!doOnlyMissingStuff) {
			db.clearDescriptors();
		}

		ImageDatabaseIndexer<T> idxr = new ImageDatabaseIndexer<T>(db, imageLoader);
		idxr.setDoPartialDump(partialDump);
		idxr.setPartialDumpSleep((long) (waitMinutesBetweenEachDump * 60 * 1000));
		idxr.setDoOnlyMissingStuff(doOnlyMissingStuff);
		idxr.setLogEnabled(isLogEnabled());
		
		conf.populate(idxr);

		if (doOnlyMissingStuff) {
			info("Launching missing signatures extraction");
		} else {
			info("Launching signatures extraction");
		}

		idxr.launch();

		db.updateAvailableDescriptors();
	}

	public void textDump(final ImageDatabase<T> db, String desc) throws IOException, FeatureException {
		File f = new File(db.getRootDirectory(), db.getName() + "_" + desc + ".export");
		info("Dumping database " + db.getName() + " to " + f.getAbsolutePath());
		BufferedWriter w = new BufferedWriter(new FileWriter(f));

		int nbNonNullSignatures = 0;
		int sigSize = -1;

		if (db.containsGlobalDescriptor(desc)) {
			for (ImageEntry<T> e : db) {
				DefaultVectorSignature s = db.getGlobalSignature(e, desc);
				if (s != null) {
					if (sigSize < 0) {
						sigSize = s.getSize();
					}
					nbNonNullSignatures++;
				}
			}
		} else if (db.containsLocalDescriptor(desc)) {
			for (ImageEntry<T> e : db) {
				BagOfSignatures<DefaultVectorSignature> bag = db.getLocalSignature(e, desc);
				if (bag != null) {
					for (DefaultVectorSignature s : bag) {
						if (sigSize < 0) {
							sigSize = s.getSize();
						}
						nbNonNullSignatures++;
					}
				}
			}
		}

		w.write(db.getName());
		w.newLine();
		w.write(desc);
		w.newLine();
		w.write(Integer.toString(nbNonNullSignatures));
		w.newLine();
		w.write(Integer.toString(sigSize));
		w.newLine();

		if (db.containsGlobalDescriptor(desc)) {
			for (ImageEntry<T> e : db) {
				DefaultVectorSignature s = db.getGlobalSignature(e, desc);
				if (s != null) {
					w.write(Long.toString(e.getId()));
					for (int d = 0; d < s.getSize(); d++) {
						w.write(" " + s.get(d));
					}
					w.newLine();
				}
			}
		} else if (db.containsLocalDescriptor(desc)) {
			for (ImageEntry<T> e : db) {
				BagOfSignatures<DefaultVectorSignature> bag = db.getLocalSignature(e, desc);
				if (bag != null) {
					int lid = 0;
					for (DefaultVectorSignature s : bag) {
						w.write(Long.toString(getUniqueId(e.getId(), lid)));
						for (int d = 0; d < s.getSize(); d++) {
							w.write(" " + s.get(d));
						}
						w.newLine();
						lid++;
					}
				}
			}
		}

		w.close();
	}
}
