/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for file handling
 */
public class FileIOHelper {

	/**
	 * logger
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(FileIOHelper.class);

	/**
	 * Loads a file and returns an InputStream
	 * 
	 * @param file
	 *            File to load
	 * @return InputStream of the file
	 * @throws OwsExceptionReport
	 *             If and error occurs;
	 */
	public static InputStream loadInputStreamFromFile(File file)
			throws OwsExceptionReport {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			return is;
		} catch (FileNotFoundException fnfe) {
			OwsExceptionReport se = new OwsExceptionReport(fnfe);
			String exceptionText = "Error while loading file " + file.getName()
					+ "!";
			LOGGER.error(exceptionText, fnfe);
			se.addCodedException(OwsExceptionCode.NoApplicableCode, null,
					exceptionText);
			throw se;
		}
	}

}
