/**
 * Copyright (C) 2012
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
package org.n52.sos.service;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/* TODO modularize it to enable submodules to supply settings. Include
 * title,type,description,etc to let the client generate the UI dynamically */
public enum Setting {

	CAPABILITIES_CACHE_UPDATE_INTERVAL(Type.INTEGER),
	CHARACTER_ENCODING(Type.STRING),
	MINIMUM_GZIP_SIZE(Type.INTEGER),
	LEASE(Type.INTEGER),
	MAX_GET_OBSERVATION_RESULTS(Type.INTEGER),
	CHILD_PROCEDURES_ENCODED_IN_PARENTS_DESCRIBE_SENSOR(Type.BOOLEAN),
	SRS_NAME_PREFIX_SOS_V1(Type.STRING),
	SRS_NAME_PREFIX_SOS_V2(Type.STRING),
	DEFAULT_OFFERING_PREFIX(Type.STRING),
	DEFAULT_PROCEDURE_PREFIX(Type.STRING),
	TOKEN_SEPERATOR(Type.STRING),
	DECIMAL_SEPARATOR(Type.STRING),
	TUPLE_SEPERATOR(Type.STRING),
	NO_DATA_VALUE(Type.STRING),
	DEFAULT_EPSG(Type.INTEGER),
	FOI_LISTED_IN_OFFERINGS(Type.BOOLEAN),
	GML_DATE_FORMAT(Type.STRING),
	SHOW_FULL_OPERATIONS_METADATA(Type.BOOLEAN),
	SHOW_FULL_OPERATIONS_METADATA_FOR_OBSERVATIONS(Type.BOOLEAN),
	SENSOR_DIRECTORY(Type.FILE),
	SKIP_DUPLICATE_OBSERVATIONS(Type.BOOLEAN),
	SOS_URL(Type.STRING) {
		@Override
		public boolean isAllowedValue(String s) {
			return isUrl(s);
		}
	},
	CONFIGURATION_FILES(Type.STRING),
	SUPPORTS_QUALITY(Type.BOOLEAN),
	SWITCH_COORDINATES_FOR_EPSG_CODES(Type.STRING),
	SERVICE_PROVIDER_FILE(Type.FILE),
	SERVICE_PROVIDER_NAME(Type.STRING),
	SERVICE_PROVIDER_SITE(Type.STRING),
	SERVICE_PROVIDER_INDIVIDUAL_NAME(Type.STRING),
	SERVICE_PROVIDER_POSITION_NAME(Type.STRING),
	SERVICE_PROVIDER_PHONE(Type.STRING),
	SERVICE_PROVIDER_ADDRESS(Type.STRING),
	SERVICE_PROVIDER_CITY(Type.STRING),
	SERVICE_PROVIDER_ZIP(Type.STRING),
	SERVICE_PROVIDER_STATE(Type.STRING),
	SERVICE_PROVIDER_COUNTRY(Type.STRING),
	SERVICE_PROVIDER_EMAIL(Type.STRING),
	SERVICE_IDENTIFICATION_FILE(Type.FILE),
	SERVICE_IDENTIFICATION_KEYWORDS(Type.STRING),
	SERVICE_IDENTIFICATION_SERVICE_TYPE(Type.STRING),
	SERVICE_IDENTIFICATION_TITLE(Type.STRING),
	SERVICE_IDENTIFICATION_ABSTRACT(Type.STRING),
	SERVICE_IDENTIFICATION_FEES(Type.STRING),
	SERVICE_IDENTIFICATION_ACCESS_CONSTRAINTS(Type.STRING),
	CACHE_THREAD_COUNT(Type.INTEGER);

	public static enum Type {

		FILE, STRING, INTEGER, BOOLEAN;

		public String getName() {
			return name();
		}
	}
	private final Type type;

	private Setting(Type type) {
		this.type = type;
	}

	public Type type() {
		return this.type;
	}

	public String parse(String value) {
		Object result = null;
		switch (type()) {
			case STRING:
				result = parseString(value);
				break;
			case BOOLEAN:
				result = parseBoolean(value);
				break;
			case INTEGER:
				result = parseInteger(value);
				break;
			case FILE:
				result = parseString(value);
				break;
		}
		return result == null ? null : result.toString();
	}

	protected static boolean isTrue(String s) {
		if (s != null) {
			s = s.trim().toLowerCase();
		}
		return s != null && (s.equals("true") || s.equals("yes") || s.equals("on"));
	}

	protected static boolean isFalse(String s) {
		if (s != null) {
			s = s.trim().toLowerCase();
		}
		return s == null || s.equals("false") || s.equals("no") || s.equals("off");
	}

	protected static Boolean parseBoolean(String s) {
		if (isTrue(s)) {
			return Boolean.TRUE;
		}
		if (isFalse(s)) {
			return Boolean.FALSE;
		}
		return Boolean.FALSE;
	}

	protected static String parseString(String s) {
		return s;
	}

	protected static Integer parseInteger(String s) {
		if (s == null || s.trim().isEmpty()) {
			return null;
		}
		try {
			Integer i = Integer.valueOf(s);
			if (i.intValue() < 0) {
				return null;
			}
			return i;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public boolean isAllowedValue(String s) {
		return (s == null) ? this.type() == Type.BOOLEAN : true;
	}

	protected boolean isUrl(String s) {
		if (s == null) {
			return false;
		}
		try {
			URL url = new URL(s);
			if (url.getProtocol() == null || url.getProtocol().isEmpty()) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public String getName() {
		return name();
	}

	public Type getType() {
		return type();
	}

	public static Set<String> getNames() {
		Setting[] settings = values();
		HashSet<String> names = new HashSet<String>(settings.length);
		for (Setting s : settings) {
			names.add(s.name());
		}
		return names;
	}
}
