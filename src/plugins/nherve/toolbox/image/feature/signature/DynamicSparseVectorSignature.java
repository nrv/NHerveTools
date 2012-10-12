/*
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
 * along with NHerve Main Toolbox. If not, see <http://www.gnu.org/licenses/>.
 */

package plugins.nherve.toolbox.image.feature.signature;

 /**
  * 
  * @author Nicolas HERVE - nherve@ina.fr
  */
public class DynamicSparseVectorSignature extends SparseVectorSignature {

	public DynamicSparseVectorSignature() {
		super(0);
	}

	@Override
	public void set(int idx, double val) throws SignatureException {
		if (idx >= size) {
			size = idx + 1;
		}
		super.set(idx, val);
	}
	
	public void setSize(int s) {
		size = s;
	}

}
