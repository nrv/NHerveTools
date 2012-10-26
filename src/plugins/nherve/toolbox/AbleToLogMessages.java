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
 * The Interface AbleToLogMessages.
 * 
 * @author Nicolas HERVE - nherve@ina.fr
 */
public interface AbleToLogMessages {

	public boolean isLogEnabled();
	public void setLogEnabled(boolean logEnabled);

	/**
	 * Log.
	 * 
	 * @param message
	 *            the message
	 */
	public void log(String message);
	
	/**
	 * Log warning.
	 * 
	 * @param message
	 *            the message
	 */
	public void logWarning(String message);
	
	/**
	 * Log error.
	 * 
	 * @param message
	 *            the message
	 */
	public void logError(String message);
	
	public void logError(Throwable e);

	
	public boolean isUIDisplayEnabled();
	public void setUIDisplayEnabled(boolean uiDisplay);
	public void clearDisplay();
	public void displayMessage(String message);

}