/*
 * Copyright 2010, 2011 Institut Pasteur.
 * Copyright 2012 Institut National de l'Audiovisuel.
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

import plugins.nherve.toolbox.Algorithm;
import plugins.nherve.toolbox.image.feature.signature.SignatureException;

/**
 * The Class SignatureDistance.
 * 
 * @param <T>
 *            the generic type
 * @author Nicolas HERVE - nherve@ina.fr
 */
public abstract class SignatureDistance<T extends Signature> extends Algorithm implements Distance<T> {
	
	/* (non-Javadoc)
	 * @see plugins.nherve.toolbox.image.feature.Distance#computeDistance(java.lang.Object, java.lang.Object)
	 */
	public abstract double computeDistance(T s1, T s2) throws SignatureException;
}
