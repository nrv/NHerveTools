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
 * along with NHerveTools. If not, see <http://www.gnu.org/licenses/>.
 */
package plugins.nherve.toolbox;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 * @author Nicolas HERVE - nherve@ina.fr
 */
public class ArrayIterator<T> implements Iterator<T> {
	private T[] array;
	private int index = 0;

	public ArrayIterator(T[] array) {
		this.array = array;
	}

	public boolean hasNext() {
		return (index < array.length);
	}

	public T next() throws NoSuchElementException {
		if (index >= array.length)
			throw new NoSuchElementException("Array index: " + index);
		return array[index++];
	}

	public void remove() {
		// not implemented
	}

}
