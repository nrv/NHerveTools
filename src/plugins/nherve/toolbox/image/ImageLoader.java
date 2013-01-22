package plugins.nherve.toolbox.image;

import java.io.File;
import java.io.IOException;

import plugins.nherve.toolbox.image.feature.SegmentableImage;

public abstract class ImageLoader<T extends SegmentableImage> {
	public abstract T load(File f) throws IOException;
}
