--#######################
--## 52n SOS TEST DATA    ###
--#######################


-- sample phenomenon
INSERT INTO phenomenon VALUES ('urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel', 'gauge height', 'cm','numericType');
INSERT INTO phenomenon VALUES ('urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed', 'water speed', 'm/s','textType');

--sample offering
INSERT INTO offering VALUES ('GAUGE_HEIGHT','The water level in a river');
INSERT INTO offering VALUES ('WATER_SPEED','The waterspeed at a gage in a river');
INSERT INTO offering VALUES ('WATER_VALUES','The water level in a river');

-- sample featureofinterest
INSERT INTO feature_of_interest (feature_of_interest_id, feature_of_interest_name, feature_of_interest_description, geom, feature_type, schema_link) VALUES ('foi_1001', 'ALBER', 'Albersloh', GeometryFromText('POINT(7.52 52.90)', 4326),'sa:SamplingPoint', 'http://xyz.org/reference-url2.html');
INSERT INTO feature_of_interest (feature_of_interest_id, feature_of_interest_name, feature_of_interest_description, geom, feature_type, schema_link) VALUES ('foi_2001', 'PADER', 'Paderborn', GeometryFromText('POINT(8.76667 51.7167)', 4326),'sa:SamplingPoint', 'http://xyz.org/reference-url2.html');

-- sample procedure
INSERT INTO procedure VALUES ('urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-1', 'standard/ifgi-sensor-1.xml', 'text/xml;subtype="SensorML/1.0.1"');
INSERT INTO procedure VALUES ('urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-2', 'standard/ifgi-sensor-2.xml', 'text/xml;subtype="SensorML/1.0.1"');


-------------------------- sample relationships between phenomena, procedures and features of interest
--sample phen_off relationship
INSERT INTO phen_off VALUES ('urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','GAUGE_HEIGHT');
INSERT INTO phen_off VALUES ('urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_SPEED');
INSERT INTO phen_off VALUES ('urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','WATER_VALUES');
INSERT INTO phen_off VALUES ('urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_VALUES');

--sample foi_off relationship
INSERT INTO foi_off VALUES ('foi_1001','GAUGE_HEIGHT');
INSERT INTO foi_off VALUES ('foi_2001','WATER_SPEED');
INSERT INTO foi_off VALUES ('foi_1001','WATER_VALUES');
INSERT INTO foi_off VALUES ('foi_2001','WATER_VALUES');

-- sample proc_phen relationship
INSERT INTO proc_phen VALUES ('urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-1','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel');
INSERT INTO proc_phen VALUES ('urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-2','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed');

-- sample proc_foi relationship
INSERT INTO proc_foi VALUES ('urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-1','foi_1001');
INSERT INTO proc_foi VALUES ('urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-2','foi_2001');

--sample proc_off relationship
INSERT INTO proc_off VALUES ('urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-1','GAUGE_HEIGHT');
INSERT INTO proc_off VALUES ('urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-2','WATER_SPEED');
INSERT INTO proc_off VALUES ('urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-1','WATER_VALUES');
INSERT INTO proc_off VALUES ('urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-2','WATER_VALUES');


-- gauge height values and observations
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value) values ('2008-04-01 17:44', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-1', 'foi_1001','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','GAUGE_HEIGHT','50.0'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','category', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value) values ('2008-04-01 17:45', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-1', 'foi_1001','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','GAUGE_HEIGHT', '40.2'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','text', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value) values ('2008-04-01 17:46', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-1', 'foi_1001','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','GAUGE_HEIGHT', '70.4'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value) values ('2008-04-01 17:47', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-1', 'foi_1001','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','GAUGE_HEIGHT', '60.5'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value) values ('2008-04-01 17:48', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-1', 'foi_1001','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','GAUGE_HEIGHT', '45.456'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value) values ('2008-04-01 17:49', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-1', 'foi_1001','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','GAUGE_HEIGHT', '110.1213'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');

--water speed values and observations
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,text_value) values ('2008-04-01 17:44', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-2', 'foi_2001','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_SPEED', '2.1'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,text_value) values ('2008-04-01 17:45', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-2', 'foi_2001','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_SPEED', '4.0'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,text_value) values ('2008-04-01 17:46', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-2', 'foi_2001','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_SPEED', '0.5'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,text_value) values ('2008-04-01 17:47', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-2', 'foi_2001','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_SPEED', '2.2'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,text_value) values ('2008-04-01 17:51', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-2', 'foi_2001','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_SPEED', null); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,text_value) values ('2008-04-01 17:43', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-2', 'foi_2001','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_SPEED', 10.2); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');

-- values for offering WATER_VALUES
-- gauge height values and observations
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value) values ('2008-04-01 17:44', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-1', 'foi_1001','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','WATER_VALUES','50.0'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','category', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value) values ('2008-04-01 17:45', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-1', 'foi_1001','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','WATER_VALUES', '40.2'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','text', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value) values ('2008-04-01 17:46', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-1', 'foi_1001','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','WATER_VALUES', '70.4'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value) values ('2008-04-01 17:47', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-1', 'foi_1001','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','WATER_VALUES', '60.5'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value) values ('2008-04-01 17:48', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-1', 'foi_1001','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','WATER_VALUES', '45.456'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value) values ('2008-04-01 17:49', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-1', 'foi_1001','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','WATER_VALUES', '110.1213'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');

--water speed values and observations
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,text_value) values ('2008-04-01 17:44', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-2', 'foi_2001','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_VALUES', '2.1'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,text_value) values ('2008-04-01 17:45', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-2', 'foi_2001','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_VALUES', '4.0'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,text_value) values ('2008-04-01 17:46', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-2', 'foi_2001','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_VALUES', '0.5'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,text_value) values ('2008-04-01 17:47', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-2', 'foi_2001','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_VALUES', '2.2'); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,text_value) values ('2008-04-01 17:51', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-2', 'foi_2001','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_VALUES', null); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,text_value) values ('2008-04-01 17:43', 'urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-2', 'foi_2001','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_VALUES', 10.2); 
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'mm', '1','quantity', 'accuracy');
INSERT INTO quality(observation_id, quality_unit, quality_value, quality_type, quality_name) values (currval(pg_get_serial_sequence('observation','observation_id')),'percent', '10','quantity', 'completeness');



