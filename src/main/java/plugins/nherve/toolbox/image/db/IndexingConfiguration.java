package plugins.nherve.toolbox.image.db;

import plugins.nherve.toolbox.image.feature.SegmentableImage;


public interface IndexingConfiguration<T extends SegmentableImage> {
	void populate(ImageDatabaseIndexer<T> idxr);
	String getName();
}
