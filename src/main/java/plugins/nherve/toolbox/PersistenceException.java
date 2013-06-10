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
package plugins.nherve.toolbox;

/**
 * The Class PersistenceException.
 * 
 * @author Nicolas HERVE - nherve@ina.fr
 */
public class PersistenceException extends Exception {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -372510840499883182L;

	/**
	 * Instantiates a new persistence exception.
	 */
	public PersistenceException() {
		super();
	}

	/**
	 * Instantiates a new persistence exception.
	 * 
	 * @param message
	 *            the message
	 */
	public PersistenceException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new persistence exception.
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public PersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new persistence exception.
	 * 
	 * @param cause
	 *            the cause
	 */
	public PersistenceException(Throwable cause) {
		super(cause);
	}

}
