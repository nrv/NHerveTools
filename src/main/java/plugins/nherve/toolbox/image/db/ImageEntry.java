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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import plugins.nherve.toolbox.image.ImageLoader;
import plugins.nherve.toolbox.image.feature.Segmentable;
import plugins.nherve.toolbox.image.feature.SegmentableImage;
import plugins.nherve.toolbox.image.feature.signature.BagOfSignatures;
import plugins.nherve.toolbox.image.feature.signature.DefaultVectorSignature;

/**
 * The Class ImageEntry.
 * 
 * @author Nicolas HERVE - nicolas.herve@pasteur.fr
 */
public class ImageEntry<T extends SegmentableImage> implements Segmentable {

	/** The classes. */
	private Map<String, Double> classes;

	private transient Throwable error;

	/** The file. */
	private String file;

	/** The global signatures. */
	private Map<String, DefaultVectorSignature> globalSignatures;

	/** The height. */
	private transient int height;

	/** The id. */
	private long id;

	/** The image. */
	private transient T image;

	/** The local signatures. */
	private Map<String, BagOfSignatures<DefaultVectorSignature>> localSignatures;

	/** The width. */
	private transient int width;

	/**
	 * Instantiates a new image entry.
	 */
	public ImageEntry() {
		super();

		classes = new HashMap<String, Double>();
		globalSignatures = new HashMap<String, DefaultVectorSignature>();
		localSignatures = new HashMap<String, BagOfSignatures<DefaultVectorSignature>>();

		width = 0;
		height = 0;
		id = -1l;
	}

	/**
	 * Instantiates a new image entry.
	 * 
	 * @param file
	 *            the file
	 */
	public ImageEntry(String file) {
		this();
		setFile(file);
	}

	/**
	 * Clone for split.
	 * 
	 * @return the image entry
	 */
	protected ImageEntry<T> cloneForSplit() {
		ImageEntry<T> e = new ImageEntry<T>();

		e.id = this.id;
		e.file = this.file;
		e.width = this.width;
		e.height = this.height;
		e.image = this.image;
		e.classes = new HashMap<String, Double>(this.classes);
		e.globalSignatures = this.globalSignatures;
		e.localSignatures = this.localSignatures;

		return e;
	}

	/**
	 * Contains class.
	 * 
	 * @param key
	 *            the key
	 * @return true, if successful
	 */
	public boolean containsClass(String key) {
		return classes.containsKey(key);
	}

	/**
	 * Gets the classes.
	 * 
	 * @return the classes
	 */
	public Map<String, Double> getClasses() {
		return classes;
	}

	public Throwable getError() {
		return error;
	}

	/**
	 * Gets the file.
	 * 
	 * @return the file
	 */
	public String getFile() {
		return file;
	}

	/**
	 * Gets the global signatures.
	 * 
	 * @return the global signatures
	 */
	public Map<String, DefaultVectorSignature> getGlobalSignatures() {
		return globalSignatures;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plugins.nherve.toolbox.image.feature.Segmentable#getHeight()
	 */
	@Override
	public int getHeight() {
		return height;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Gets the image.
	 * 
	 * @return the image
	 */
	public T getImage() {
		return image;
	}

	/**
	 * Gets the local signatures.
	 * 
	 * @return the local signatures
	 */
	public Map<String, BagOfSignatures<DefaultVectorSignature>> getLocalSignatures() {
		return localSignatures;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plugins.nherve.toolbox.image.feature.Segmentable#getWidth()
	 */
	@Override
	public int getWidth() {
		return width;
	}

	public boolean isIndexedBy(String d) {
		return getGlobalSignatures().containsKey(d) || getLocalSignatures().containsKey(d);
	}

	public void loadImage(String root, ImageLoader<T> imageLoader) throws IOException {
		if (image == null) {
			image = imageLoader.load(new File(root + "/" + file));
			width = image.getWidth();
			height = image.getHeight();
		}
	}

	/**
	 * Put class.
	 * 
	 * @param key
	 *            the key
	 */
	public void putClass(String key) {
		putClass(key, 1d);
	}

	/**
	 * Put class.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void putClass(String key, Double value) {
		classes.put(key, value);
	}

	/**
	 * Put signature.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void putSignature(String key, BagOfSignatures<DefaultVectorSignature> value) {
		synchronized (localSignatures) {
			localSignatures.put(key, value);
		}
	}

	/**
	 * Put signature.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void putSignature(String key, DefaultVectorSignature value) {
		synchronized (globalSignatures) {
			globalSignatures.put(key, value);
		}
	}

	/**
	 * Removes the class.
	 * 
	 * @param key
	 *            the key
	 */
	public void removeClass(String key) {
		classes.remove(key);
	}

	/**
	 * Removes the signature.
	 * 
	 * @param key
	 *            the key
	 */
	public void removeSignature(String key) {
		synchronized (globalSignatures) {
			globalSignatures.remove(key);
		}
		synchronized (localSignatures) {
			localSignatures.remove(key);
		}
	}

	/**
	 * Removes the signatures.
	 */
	public void removeSignatures() {
		synchronized (globalSignatures) {
			globalSignatures.clear();
		}
		synchronized (localSignatures) {
			localSignatures.clear();
		}
	}

	/**
	 * Sets the classes.
	 * 
	 * @param classes
	 *            the classes
	 */
	public void setClasses(Map<String, Double> classes) {
		this.classes = classes;
	}

	public void setError(Throwable error) {
		this.error = error;
	}

	/**
	 * Sets the file.
	 * 
	 * @param file
	 *            the new file
	 */
	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * Sets the global signatures.
	 * 
	 * @param globalSignatures
	 *            the global signatures
	 */
	public void setGlobalSignatures(Map<String, DefaultVectorSignature> globalSignatures) {
		this.globalSignatures = globalSignatures;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Sets the image.
	 * 
	 * @param i
	 *            the new image
	 */
	public void setImage(T i) {
		image = i;
	}

	/**
	 * Sets the local signatures.
	 * 
	 * @param localSignatures
	 *            the local signatures
	 */
	public void setLocalSignatures(Map<String, BagOfSignatures<DefaultVectorSignature>> localSignatures) {
		this.localSignatures = localSignatures;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ImageEntry [file=" + file + ", id=" + id + "]";
	}

	/**
	 * Unload image.
	 */
	public void unloadImage() {
		image = null;
	}

}
