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
package plugins.nherve.toolbox.image.feature.descriptor;

import java.awt.Shape;

import plugins.nherve.toolbox.image.feature.Descriptor;
import plugins.nherve.toolbox.image.feature.Segmentable;
import plugins.nherve.toolbox.image.feature.Signature;
import plugins.nherve.toolbox.image.feature.SupportRegion;
import plugins.nherve.toolbox.image.feature.region.Pixel;
import plugins.nherve.toolbox.image.feature.signature.SignatureException;


/**
 * The Interface LocalDescriptor.
 * 
 * @param <T>
 *            the generic type
 * @param <S>
 *            the generic type
 * @author Nicolas HERVE - nicolas.herve@pasteur.fr
 */
@SuppressWarnings("rawtypes")
public interface LocalDescriptor<T extends Segmentable, S extends Signature, P extends Pixel> extends Descriptor<T> {
	
	/**
	 * Extract local signature.
	 * 
	 * @param img
	 *            the img
	 * @param reg
	 *            the reg
	 * @return the s
	 * @throws SignatureException
	 *             the signature exception
	 */
	S extractLocalSignature(T img, SupportRegion<P> reg) throws SignatureException;
	
	/**
	 * Extract local signature.
	 * 
	 * @param img
	 *            the img
	 * @param shp
	 *            the shp
	 * @return the s
	 * @throws SignatureException
	 *             the signature exception
	 */
	S extractLocalSignature(T img, Shape shp) throws SignatureException;
}
