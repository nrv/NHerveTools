package plugins.nherve.toolbox.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import plugins.nherve.toolbox.image.feature.DefaultSegmentableImage;

public class DefaultImageLoader extends ImageLoader<DefaultSegmentableImage> {

	@Override
	public DefaultSegmentableImage load(File f) throws IOException {
		BufferedImage i = ImageIO.read(f);
		return new DefaultSegmentableImage(i);
	}

}
