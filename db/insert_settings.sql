--
-- Copyright (C) 2013
-- by 52 North Initiative for Geospatial Open Source Software GmbH
--
-- Contact: Andreas Wytzisk
-- 52 North Initiative for Geospatial Open Source Software GmbH
-- Martin-Luther-King-Weg 24
-- 48155 Muenster, Germany
-- info@52north.org
--
-- This program is free software; you can redistribute and/or modify it under
-- the terms of the GNU General Public License version 2 as published by the
-- Free Software Foundation.
--
-- This program is distributed WITHOUT ANY WARRANTY; even without the implied
-- WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
-- General Public License for more details.
--
-- You should have received a copy of the GNU General Public License along with
-- this program (see gnu-gpl v2.txt). If not, write to the Free Software
-- Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
-- visit the Free Software Foundation web page, http://www.fsf.org.
--

DROP TABLE IF EXISTS global_settings CASCADE;

CREATE TABLE global_settings (
  key VARCHAR(256) NOT NULL,
  value TEXT,
  PRIMARY KEY(key)
);

INSERT INTO global_settings(key,value) VALUES
	('admin_password', '$2a$10$vbp9aXCDMP/fXwEsqe/1.eon44mMdUyC4ub2JfOrkPfaer5ciLOly'),
	('admin_username', 'admin'),
	('CACHE_THREAD_COUNT', '5'),
	('CAPABILITIES_CACHE_UPDATE_INTERVAL', '5'),
	('CHARACTER_ENCODING', 'UTF-8'),
	('CHILD_PROCEDURES_ENCODED_IN_PARENTS_DESCRIBE_SENSOR', 'false'),
	('CONFIGURATION_FILES', ''),
	('DECIMAL_SEPARATOR', '.'),
	('DEFAULT_EPSG', '4326'),
	('DEFAULT_OFFERING_PREFIX', 'OFFERING_'),
	('DEFAULT_PROCEDURE_PREFIX', 'http://www.example.org/sensors/'),
	('FOI_LISTED_IN_OFFERINGS', 'true'),
	('GML_DATE_FORMAT', ''),
	('LEASE', '600'),
	('MAX_GET_OBSERVATION_RESULTS', '0'),
	('MINIMUM_GZIP_SIZE', '1048576'),
	('NO_DATA_VALUE', 'noData'),
	('SENSOR_DIRECTORY', ''),
	('SERVICE_IDENTIFICATION_ABSTRACT', '52North Sensor Observation Service - Data Access for the Sensor Web'),
	('SERVICE_IDENTIFICATION_ACCESS_CONSTRAINTS', 'NONE'),
	('SERVICE_IDENTIFICATION_FEES', 'NONE'),
	('SERVICE_IDENTIFICATION_FILE', ''),
	('SERVICE_IDENTIFICATION_KEYWORDS', ''),
	('SERVICE_IDENTIFICATION_SERVICE_TYPE', 'OGC:SOS'),
	('SERVICE_IDENTIFICATION_TITLE', '52N SOS'),
	('SERVICE_PROVIDER_ADDRESS', 'Martin-Luther-King-Weg 24'),
	('SERVICE_PROVIDER_CITY', 'Münster'),
	('SERVICE_PROVIDER_COUNTRY', 'Germany'),
	('SERVICE_PROVIDER_EMAIL', 'info@52north.org'),
	('SERVICE_PROVIDER_FILE', ''),
	('SERVICE_PROVIDER_INDIVIDUAL_NAME', 'TBA'),
	('SERVICE_PROVIDER_NAME', '52North'),
	('SERVICE_PROVIDER_PHONE', '+49(0)251/396 371-0'),
	('SERVICE_PROVIDER_POSITION_NAME', 'TBA'),
	('SERVICE_PROVIDER_SITE', 'http://52north.org/swe'),
	('SERVICE_PROVIDER_STATE', 'North Rhine-Westphalia'),
	('SERVICE_PROVIDER_ZIP', '48155'),
	('SHOW_FULL_OPERATIONS_METADATA', 'true'),
	('SHOW_FULL_OPERATIONS_METADATA_FOR_OBSERVATIONS', 'true'),
	('SKIP_DUPLICATE_OBSERVATIONS', 'true'),
	('SOS_URL', 'http://localhost:8080/52n-sos-webapp-4.0.0-SNAPSHOT/sos'),
	('SRS_NAME_PREFIX_SOS_V1', 'urn:ogc:def:crs:EPSG::'),
	('SRS_NAME_PREFIX_SOS_V2', 'http://www.opengis.net/def/crs/EPSG/0/'),
	('SUPPORTS_QUALITY', 'true'),
	('SWITCH_COORDINATES_FOR_EPSG_CODES', '2044-2045;2081-2083;2085-2086;2093;2096-2098;2105-2132;2169-2170;2176-2180;2193;2200;2206-2212;2319;2320-2462;2523-2549;2551-2735;2738-2758;2935-2941;2953;3006-3030;3034-3035;3058-3059;3068;3114-3118;3126-3138;3300-3301;3328-3335;3346;3350-3352;3366;3416;4001-4999;20004-20032;20064-20092;21413-21423;21473-21483;21896-21899;22171;22181-22187;22191-22197;25884;27205-27232;27391-27398;27492;28402-28432;28462-28492;30161-30179;30800;31251-31259;31275-31279;31281-31290;31466-31700'),
	('TOKEN_SEPERATOR', ','),
	('TUPLE_SEPERATOR', ';'),
	('VERSION', '${project.version}');
