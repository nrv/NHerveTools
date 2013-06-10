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
package plugins.nherve.toolbox.concurrent;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * The Class SingleDataTask.
 * 
 * @param <Input>
 *            the generic type
 * @param <Output>
 *            the generic type
 * @author Nicolas HERVE - nherve@ina.fr
 */
public abstract class SingleDataTask<Input, Output> implements Callable<Output> {
	
	/** The data. */
	private Input data;
	
	/** The idx. */
	private int idx;
	
	/**
	 * Instantiates a new single data task.
	 * 
	 * @param allData
	 *            the all data
	 * @param idx
	 *            the idx
	 */
	public SingleDataTask(List<Input> allData, int idx) {
		super();
		
		this.data = allData.get(idx);
		this.idx = idx;
	}

	/**
	 * Gets the data.
	 * 
	 * @return the data
	 */
	public Input getData() {
		return data;
	}

	/**
	 * Gets the idx.
	 * 
	 * @return the idx
	 */
	public int getIdx() {
		return idx;
	}

}
