/*
 * Copyright 2013 Institut National de l'Audiovisuel.
 * 
 * This file is part of NHerveTools.
 * 
 * NHerveTools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NHerveTools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with NHerveTools. If not, see <http://www.gnu.org/licenses/>.
 */
package plugins.nherve.toolbox.image.feature;

import java.awt.image.BufferedImage;

/**
 * The Interface SegmentableImage.
 * 
 * @author Nicolas HERVE - nherve@ina.fr
 */
public abstract class SegmentableImage implements Segmentable {
	private String name;

	public abstract BufferedImage getImage();

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
}
