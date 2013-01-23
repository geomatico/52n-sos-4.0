/**
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.sos.util;

import java.io.File;
import org.n52.sos.service.ConfigurationException;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class SettingsHelper {

	private static final Logger log = LoggerFactory.getLogger(SettingsHelper.class);

	public static File parseFile(Setting setting, String value, boolean canBeNull) throws ConfigurationException {
		if (value == null || value.isEmpty()) {
			return null;
		}
		String fileName = parseString(setting, value, canBeNull);
		if (fileName == null) {
			return null;
		}
		File f = new File(fileName);
		if (f.exists()) {
			return f;
		} else {
			f = new File(Configurator.getInstance().getBasePath() + fileName);
			if (f.exists()) {
				return f;
			} else {
				String exceptionText = String.format("Can not find file '(%s)%s'!", Configurator.getInstance().getBasePath(), fileName);
				log.error(exceptionText.toString());
				throw new ConfigurationException(exceptionText.toString());
			}
		}
	}

	public static int parseInteger(Setting setting, String value) throws ConfigurationException {
		Integer val = null;
		try {
			if (value != null && !value.isEmpty()) {
				val = Integer.valueOf(value);
			}
		} catch (NumberFormatException e) {
			log.error("Value \"{}\" expected to be an integer could not be parsed!", value);
			log.debug("Exception thrown", e);
		}

		if (val == null) {
			String exceptionText =
					"'" + setting.name() + "' is not properly defined! Please set '" + setting.name()
					+ "' property to an integer value!";
			log.error(exceptionText);
			throw new ConfigurationException(exceptionText);
		} else {
			return val.intValue();
		}
	}

	public static boolean parseBoolean(Setting setting, String value) throws ConfigurationException {
		Boolean val = null;
		if (value != null && !value.isEmpty()) {
			val =
					value.equalsIgnoreCase("true") ? Boolean.TRUE : value.equalsIgnoreCase("false") ? Boolean.FALSE
					: null;
		}
		if (val == null) {
			String exceptionText =
					"'" + setting.name() + "' is not properly defined! Please set '" + setting.name()
					+ "' property to an boolean value!";
			log.error(exceptionText);
			throw new ConfigurationException(exceptionText);
		} else {
			return val.booleanValue();
		}
	}

	public static String parseString(Setting setting, String value, boolean canBeEmpty) throws ConfigurationException {
		if (value == null || (value.isEmpty() && !canBeEmpty)) {
			String exceptionText = "String property '" + setting.name() + "' is not defined!";
			log.error(exceptionText);
			throw new ConfigurationException(exceptionText);
		}
		return value;
	}
}
