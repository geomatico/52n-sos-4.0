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


-- script to create the schema for the SQLite 
-- configuration database and inserts default settings

DROP TABLE IF EXISTS "administrator_user";
DROP TABLE IF EXISTS "settings";
DROP TABLE IF EXISTS "boolean_settings";
DROP TABLE IF EXISTS "file_settings";
DROP TABLE IF EXISTS "integer_settings";
DROP TABLE IF EXISTS "numeric_settings";
DROP TABLE IF EXISTS "string_settings";
DROP TABLE IF EXISTS "uri_settings";

CREATE TABLE administrator_user (
	id  integer,
	password varchar,
	username varchar unique,
	primary key (id)
);

CREATE TABLE settings (
	identifier varchar not null,
	primary key (identifier)
);

CREATE TABLE boolean_settings (
	value integer,
	identifier varchar not null,
	primary key (identifier)
);

CREATE TABLE file_settings (
	value varchar,
	identifier varchar not null,
	primary key (identifier)
);

CREATE TABLE integer_settings (
	value integer,
	identifier varchar not null,
	primary key (identifier)
);

CREATE TABLE numeric_settings (
	value double,
	identifier varchar not null,
	primary key (identifier)
);

CREATE TABLE string_settings (
	value varchar,
	identifier varchar not null,
	primary key (identifier)
);

CREATE TABLE uri_settings (
	value varchar,
	identifier varchar not null,
	primary key (identifier)
);

INSERT INTO "settings" VALUES('misc.characterEncoding');
INSERT INTO "settings" VALUES('misc.decimalSeperator');
INSERT INTO "settings" VALUES('misc.defaultOfferingPrefix');
INSERT INTO "settings" VALUES('misc.defaultProcedurePrefix');
INSERT INTO "settings" VALUES('misc.gmlDateFormat');
INSERT INTO "settings" VALUES('misc.srsNamePrefixSosV1');
INSERT INTO "settings" VALUES('misc.srsNamePrefixSosV2');
INSERT INTO "settings" VALUES('misc.switchCoordinatesForEpsgCodes');
INSERT INTO "settings" VALUES('misc.tokenSeperator');
INSERT INTO "settings" VALUES('misc.tupleSeperator');

INSERT INTO "settings" VALUES('service.cacheThreadCount');
INSERT INTO "settings" VALUES('service.capabilitiesCacheUpdateInterval');
INSERT INTO "settings" VALUES('service.configurationFiles');
INSERT INTO "settings" VALUES('service.defaultEpsg');
INSERT INTO "settings" VALUES('service.lease');
INSERT INTO "settings" VALUES('service.maxGetObservationResults');
INSERT INTO "settings" VALUES('service.minimumGzipSize');
INSERT INTO "settings" VALUES('service.sensorDirectory');
INSERT INTO "settings" VALUES('service.skipDuplicateObservations');
INSERT INTO "settings" VALUES('service.sosUrl');
INSERT INTO "settings" VALUES('service.supportsQuality');

INSERT INTO "settings" VALUES('serviceIdentification.abstract');
INSERT INTO "settings" VALUES('serviceIdentification.accessConstraints');
INSERT INTO "settings" VALUES('serviceIdentification.fees');
INSERT INTO "settings" VALUES('serviceIdentification.file');
INSERT INTO "settings" VALUES('serviceIdentification.keywords');
INSERT INTO "settings" VALUES('serviceIdentification.serviceType');
INSERT INTO "settings" VALUES('serviceIdentification.title');

INSERT INTO "settings" VALUES('serviceProvider.address');
INSERT INTO "settings" VALUES('serviceProvider.city');
INSERT INTO "settings" VALUES('serviceProvider.country');
INSERT INTO "settings" VALUES('serviceProvider.email');
INSERT INTO "settings" VALUES('serviceProvider.file');
INSERT INTO "settings" VALUES('serviceProvider.individualName');
INSERT INTO "settings" VALUES('serviceProvider.name');
INSERT INTO "settings" VALUES('serviceProvider.phone');
INSERT INTO "settings" VALUES('serviceProvider.positionName');
INSERT INTO "settings" VALUES('serviceProvider.postalCode');
INSERT INTO "settings" VALUES('serviceProvider.site');
INSERT INTO "settings" VALUES('serviceProvider.state');

INSERT INTO "administrator_user" VALUES(1,'$2a$10$y1TfEacanLJHkC0mqtkpy.KSt7r6DjdebUdbTn2kpqfwbiVRgnWsa','admin');

INSERT INTO "boolean_settings" VALUES(1,'service.supportsQuality');
INSERT INTO "boolean_settings" VALUES(1,'service.skipDuplicateObservations');

INSERT INTO "file_settings" VALUES(NULL,'serviceIdentification.file');
INSERT INTO "file_settings" VALUES(NULL,'serviceProvider.file');

INSERT INTO "integer_settings" VALUES(600,'service.lease');
INSERT INTO "integer_settings" VALUES(0,'service.maxGetObservationResults');
INSERT INTO "integer_settings" VALUES(4326,'service.defaultEpsg');
INSERT INTO "integer_settings" VALUES(1048576,'service.minimumGzipSize');
INSERT INTO "integer_settings" VALUES(5,'service.cacheThreadCount');
INSERT INTO "integer_settings" VALUES(5,'service.capabilitiesCacheUpdateInterval');

INSERT INTO "string_settings" VALUES('NONE','serviceIdentification.accessConstraints');
INSERT INTO "string_settings" VALUES('52North','serviceProvider.name');
INSERT INTO "string_settings" VALUES(',','misc.tokenSeperator');
INSERT INTO "string_settings" VALUES('Martin-Luther-King-Weg 24','serviceProvider.address');
INSERT INTO "string_settings" VALUES(NULL,'service.sensorDirectory');
INSERT INTO "string_settings" VALUES(';','misc.tupleSeperator');
INSERT INTO "string_settings" VALUES('OFFERING_','misc.defaultOfferingPrefix');
INSERT INTO "string_settings" VALUES('+49(0)251/396 371-0','serviceProvider.phone');
INSERT INTO "string_settings" VALUES('http://www.opengis.net/def/crs/EPSG/0/','misc.srsNamePrefixSosV2');
INSERT INTO "string_settings" VALUES(NULL,'misc.gmlDateFormat');
INSERT INTO "string_settings" VALUES('urn:ogc:def:crs:EPSG::','misc.srsNamePrefixSosV1');
INSERT INTO "string_settings" VALUES('2044-2045;2081-2083;2085-2086;2093;2096-2098;2105-2132;2169-2170;2176-2180;2193;2200;2206-2212;2319;2320-2462;2523-2549;2551-2735;2738-2758;2935-2941;2953;3006-3030;3034-3035;3058-3059;3068;3114-3118;3126-3138;3300-3301;3328-3335;3346;3350-3352;3366;3416;4001-4999;20004-20032;20064-20092;21413-21423;21473-21483;21896-21899;22171;22181-22187;22191-22197;25884;27205-27232;27391-27398;27492;28402-28432;28462-28492;30161-30179;30800;31251-31259;31275-31279;31281-31290;31466-31700','misc.switchCoordinatesForEpsgCodes');
INSERT INTO "string_settings" VALUES(NULL,'serviceIdentification.keywords');
INSERT INTO "string_settings" VALUES('52North Sensor Observation Service - Data Access for the Sensor Web','serviceIdentification.abstract');
INSERT INTO "string_settings" VALUES('52N SOS','serviceIdentification.title');
INSERT INTO "string_settings" VALUES('Germany','serviceProvider.country');
INSERT INTO "string_settings" VALUES('Münster','serviceProvider.city');
INSERT INTO "string_settings" VALUES('OGC:SOS','serviceIdentification.serviceType');
INSERT INTO "string_settings" VALUES('NONE','serviceIdentification.fees');
INSERT INTO "string_settings" VALUES('.','misc.decimalSeperator');
INSERT INTO "string_settings" VALUES('info@52north.org','serviceProvider.email');
INSERT INTO "string_settings" VALUES('UTF-8','misc.characterEncoding');
INSERT INTO "string_settings" VALUES('urn:ogc:object:feature:Sensor:','misc.defaultProcedurePrefix');
INSERT INTO "string_settings" VALUES('North Rhine-Westphalia','serviceProvider.state');
INSERT INTO "string_settings" VALUES(NULL,'service.configurationFiles');
INSERT INTO "string_settings" VALUES('TBA','serviceProvider.individualName');
INSERT INTO "string_settings" VALUES('TBA','serviceProvider.positionName');
INSERT INTO "string_settings" VALUES('48155','serviceProvider.postalCode');

INSERT INTO "uri_settings" VALUES('http://52north.org/swe','serviceProvider.site');
INSERT INTO "uri_settings" VALUES('http://localhost:8080/52n-sos-webapp/sos','service.sosUrl');