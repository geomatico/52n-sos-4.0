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

--
-- DO NOT USE END-OF-LINE COMMNETS! because of the quite poor parser the installer uses to excute the SQL file...
--
-- ok:
-- -- offering
-- SELECT insert_offering();	
--
-- wont work:
-- SELECT insert_offering(); -- offering
--

CREATE OR REPLACE FUNCTION get_observation_type(text) RETURNS bigint AS
$$
	SELECT observation_type_id FROM observation_type 
	WHERE observation_type = 'http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_'::text || $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION get_procedure(text) RETURNS bigint AS
$$
	SELECT procedure_id FROM procedure WHERE identifier = $1; 
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION get_feature_of_interest_type(text) RETURNS bigint AS
$$
	SELECT feature_of_interest_type_id 
	FROM feature_of_interest_type 
	WHERE feature_of_interest_type = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION get_spatial_sampling_feature_type(text) RETURNS bigint AS
$$
	SELECT get_feature_of_interest_type('http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_Sampling'::text || $1);
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION get_feature_of_interest(text) RETURNS bigint AS
$$
	SELECT feature_of_interest_id FROM feature_of_interest WHERE identifier = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION get_sensor_ml_description_format() RETURNS bigint AS
$$
	SELECT procedure_description_format_id FROM procedure_description_format 
	WHERE procedure_description_format = 'http://www.opengis.net/sensorML/1.0.1'::text;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION get_offering(text) RETURNS bigint AS
$$
	SELECT offering_id FROM offering WHERE identifier = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION get_observable_property(text) RETURNS bigint AS
$$ 
	SELECT observable_property_id FROM observable_property WHERE identifier = $1;
$$
LANGUAGE 'sql';

---- INSERTION FUNCTIONS
CREATE OR REPLACE FUNCTION insert_category_value(text) RETURNS bigint AS
$$
	INSERT INTO category_value(value) SELECT $1 WHERE $1 NOT IN (SELECT value FROM category_value);
	SELECT category_value_id FROM category_value WHERE value = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_numeric_value(numeric) RETURNS bigint AS
$$
	INSERT INTO numeric_value(value) SELECT $1 WHERE $1 NOT IN (SELECT value FROM numeric_value);
	SELECT numeric_value_id FROM numeric_value WHERE value = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_text_value(text) RETURNS bigint AS
$$
	INSERT INTO text_value(value) SELECT $1 WHERE $1 NOT IN (SELECT value FROM text_value);
	SELECT text_value_id FROM text_value WHERE value = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_geometry_value(geometry) RETURNS bigint AS
$$
	INSERT INTO geometry_value(value) SELECT $1 WHERE $1 NOT IN (SELECT value FROM geometry_value);
	SELECT geometry_value_id FROM geometry_value WHERE value = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_observation_type(text) RETURNS bigint AS
$$
	INSERT INTO observation_type(observation_type) SELECT $1 WHERE $1 NOT IN (SELECT observation_type FROM observation_type);
	SELECT observation_type_id FROM observation_type WHERE observation_type = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_feature_of_interest_type(text) RETURNS bigint AS
$$
	INSERT INTO feature_of_interest_type(feature_of_interest_type) SELECT $1 WHERE $1 NOT IN (SELECT feature_of_interest_type FROM feature_of_interest_type);
	SELECT feature_of_interest_type_id FROM feature_of_interest_type WHERE feature_of_interest_type = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_offering(text) RETURNS bigint AS
$$
	INSERT INTO offering(identifier, name) 
		SELECT $1, $1 || '_name'::text
		WHERE $1 NOT IN (
			SELECT identifier FROM offering);
	SELECT offering_id FROM offering WHERE identifier = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_allowed_observation_types_for_offering(bigint, bigint) RETURNS VOID AS
$$
	INSERT INTO offering_has_allowed_observation_type (offering_id, observation_type_id) 
	SELECT $1, $2
	WHERE $1 NOT IN (
		SELECT offering_id 
		FROM offering_has_allowed_observation_type 
		WHERE offering_id = $1 
		  AND observation_type_id = $2
	);
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_allowed_observation_types_for_offering(text, text) RETURNS VOID AS
$$
	SELECT insert_allowed_observation_types_for_offering(get_offering($1), get_observation_type($2));
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_allowed_feature_of_interest_types_for_offering(bigint, bigint) RETURNS VOID AS
$$
	INSERT INTO offering_has_allowed_feature_of_interest_type (offering_id, feature_of_interest_type_id) 
	SELECT $1, $2 
	WHERE $1 NOT in (
		SELECT offering_id 
		FROM offering_has_allowed_feature_of_interest_type 
		WHERE offering_id = $1 
		  AND feature_of_interest_type_id = $2
		);
	
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_allowed_feature_of_interest_types_for_offering(text, text) RETURNS VOID AS
$$
	SELECT insert_allowed_feature_of_interest_types_for_offering(get_offering($1), get_spatial_sampling_feature_type($2));
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_unit(text) RETURNS bigint AS
$$
	INSERT INTO unit(unit) SELECT $1 WHERE $1 NOT IN (SELECT unit FROM unit);
	SELECT unit_id FROM unit WHERE unit = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_procedure_description_format(text) RETURNS bigint AS
$$
	INSERT INTO procedure_description_format(procedure_description_format) SELECT $1 WHERE $1 NOT IN (SELECT procedure_description_format FROM procedure_description_format);
	SELECT procedure_description_format_id FROM procedure_description_format WHERE procedure_description_format = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_feature_of_interest(text, numeric, numeric, text) RETURNS bigint AS
$$
	INSERT INTO feature_of_interest(feature_of_interest_type_id, identifier, name, geom, description_xml) 
	SELECT get_spatial_sampling_feature_type('Point'), $1, $1, ST_GeomFromText('POINT(' || $2 || ' ' || $3 || ')', 4326), 
'<sams:SF_SpatialSamplingFeature 
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:sams="http://www.opengis.net/samplingSpatial/2.0" 
	xmlns:sf="http://www.opengis.net/sampling/2.0" 
	xmlns:gml="http://www.opengis.net/gml/3.2" gml:id="ssf_'::text || $1 || '">
	<gml:identifier codeSpace="">'::text || $1 || '</gml:identifier>
	<gml:name>'::text || $4 || '</gml:name>
	<sf:type xlink:href="http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint"/>
	<sf:sampledFeature xlink:href="http://www.opengis.net/def/nil/OGC/0/unknown"/>
	<sams:shape>
		<gml:Point gml:id="p_ssf_'::text || $1 || '">
			<gml:pos srsName="http://www.opengis.net/def/crs/EPSG/0/4326">'::text|| $3 || ' '::text || $2 || '</gml:pos>
		</gml:Point>
	</sams:shape>
</sams:SF_SpatialSamplingFeature>'::text
	WHERE $1 NOT IN (SELECT identifier FROM feature_of_interest);
	SELECT feature_of_interest_id FROM feature_of_interest WHERE identifier = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_observable_property(text) RETURNS bigint AS
$$
	INSERT INTO observable_property(identifier, description) SELECT $1, $1
	WHERE $1 NOT IN (SELECT identifier FROM observable_property WHERE identifier = $1);
	SELECT observable_property_id FROM observable_property WHERE identifier = $1
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION create_sensor_description(text, text, numeric, numeric, numeric, text, text) RETURNS text AS
$$
	SELECT 
'<sml:SensorML version="1.0.1"
  xmlns:sml="http://www.opengis.net/sensorML/1.0.1"
  xmlns:gml="http://www.opengis.net/gml"
  xmlns:swe="http://www.opengis.net/swe/1.0.1"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <sml:member>
    <sml:System >
      <sml:identification>
        <sml:IdentifierList>
          <sml:identifier name="uniqueID">
            <sml:Term definition="urn:ogc:def:identifier:OGC:1.0:uniqueID">
              <sml:value>'::text || $1 || '</sml:value>
            </sml:Term>
          </sml:identifier>
          <sml:identifier name="longName">
            <sml:Term definition="urn:ogc:def:identifier:OGC:1.0:longName">
              <sml:value>'::text || $6 || '</sml:value>
            </sml:Term>
          </sml:identifier>
          <sml:identifier name="shortName">
            <sml:Term definition="urn:ogc:def:identifier:OGC:1.0:shortName">
              <sml:value>'::text || $7 || '</sml:value>
            </sml:Term>
          </sml:identifier>
        </sml:IdentifierList>
      </sml:identification>
      <sml:position name="sensorPosition">
        <swe:Position referenceFrame="urn:ogc:def:crs:EPSG::4326">
          <swe:location>
            <swe:Vector gml:id="STATION_LOCATION">
              <swe:coordinate name="easting">
                <swe:Quantity axisID="x">
                  <swe:uom code="degree"/>
                  <swe:value>'::text || $3 || '</swe:value>
                </swe:Quantity>
              </swe:coordinate>
              <swe:coordinate name="northing">
                <swe:Quantity axisID="y">
                  <swe:uom code="degree"/>
                  <swe:value>'::text || $4 || '</swe:value>
                </swe:Quantity>
              </swe:coordinate>
              <swe:coordinate name="altitude">
                <swe:Quantity axisID="z">
                  <swe:uom code="m"/>
                  <swe:value>'::text || $5 || '</swe:value>
                </swe:Quantity>
              </swe:coordinate>
            </swe:Vector>
          </swe:location>
        </swe:Position>
      </sml:position>
      <sml:inputs>
        <sml:InputList>
          <sml:input name="">
            <swe:ObservableProperty definition="'::text || $2 || '"/>
          </sml:input>
        </sml:InputList>
      </sml:inputs>
      <sml:outputs>
        <sml:OutputList>
          <sml:output name="">
            <swe:Quantity  definition="'::text || $2 || '">
              <swe:uom code="NOT_DEFINED"/>
            </swe:Quantity>
          </sml:output>
        </sml:OutputList>
      </sml:outputs>
    </sml:System>
  </sml:member>
</sml:SensorML>'::text;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_procedure(text,timestamp with time zone,text,numeric,numeric,numeric, text, text) RETURNS bigint AS
$$
	INSERT INTO procedure(identifier, procedure_description_format_id, deleted) SELECT 
		$1, get_sensor_ml_description_format(), false WHERE $1 NOT IN (
			SELECT identifier FROM procedure WHERE identifier = $1);
	INSERT INTO valid_procedure_time(procedure_id, start_time, description_xml) 
		SELECT get_procedure($1), $2, create_sensor_description($1, $3, $4, $5, $6, $7, $8)
		WHERE get_procedure($1) NOT IN (
			SELECT procedure_id FROM valid_procedure_time WHERE procedure_id = get_procedure($1));
	SELECT get_procedure($1);
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION get_observation_constellation(bigint,bigint,bigint,bigint) RETURNS bigint AS
$$
	SELECT observation_constellation_id FROM observation_constellation WHERE procedure_id = $1 AND observable_property_id = $2 AND offering_id = $3 AND observation_type_id = $4;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION get_observation_constellation(text,text,text,text) RETURNS bigint AS
$$
	SELECT get_observation_constellation(get_procedure($1), get_observable_property($2), get_offering($3), get_observation_type($4));
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_observation_constellation(bigint,bigint,bigint,bigint) RETURNS VOID AS
$$
	INSERT INTO observation_constellation (procedure_id, observable_property_id, offering_id, observation_type_id) VALUES ($1, $2, $3, $4);
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_observation_constellation(text,text,text,text) RETURNS VOID AS
$$
	SELECT insert_observation_constellation(get_procedure($1), get_observable_property($2), get_offering($3), get_observation_type($4));
$$
LANGUAGE 'sql';

-- UNIT
CREATE OR REPLACE FUNCTION get_unit(text) RETURNS bigint AS
$$
	SELECT unit_id FROM unit WHERE unit = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_numeric_observation(bigint, numeric) RETURNS VOID AS
$$
	INSERT INTO observation_has_numeric_value(observation_id, numeric_value_id) 
		SELECT $1, insert_numeric_value($2) WHERE $1 NOT IN (
			SELECT observation_id FROM observation_has_numeric_value 
			WHERE observation_id = $1 AND numeric_value_id = insert_numeric_value($2));
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_observation_offering(bigint, bigint) RETURNS VOID AS
$$
	INSERT INTO observation_relates_to_offering (observation_id, offering_id) VALUES ($1, $2);
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_observation(text, text, text, text, timestamp with time zone, text) RETURNS bigint AS
$$ 
	INSERT INTO observation(procedure_id, observable_property_id, feature_of_interest_id, unit_id, phenomenon_time_start, phenomenon_time_end, result_time)
	SELECT get_procedure($1), get_observable_property($2), get_feature_of_interest($3), get_unit($4), $5, $5, $5 WHERE get_procedure($1) NOT IN (
		SELECT procedure_id FROM observation
		WHERE procedure_id = get_procedure($1) AND observable_property_id = get_observable_property($2) AND feature_of_interest_id = get_feature_of_interest($3) 
				AND unit_id = get_unit($4) AND phenomenon_time_start = $5 AND phenomenon_time_end = $5 AND result_time = $5)
		AND get_observable_property($2) NOT IN (SELECT observable_property_id FROM observation
		WHERE procedure_id = get_procedure($1) AND observable_property_id = get_observable_property($2) AND feature_of_interest_id = get_feature_of_interest($3) 
				AND unit_id = get_unit($4) AND phenomenon_time_start = $5 AND phenomenon_time_end = $5 AND result_time = $5);
				
	SELECT insert_observation_offering((SELECT observation_id FROM observation 
	WHERE feature_of_interest_id = get_feature_of_interest($3)
		AND procedure_id = get_procedure($1) AND observable_property_id = get_observable_property($2) AND unit_id = get_unit($4) 
		AND phenomenon_time_start = $5), get_offering($6));

	SELECT observation_id FROM observation 
	WHERE feature_of_interest_id = get_feature_of_interest($3)
		AND procedure_id = get_procedure($1) AND observable_property_id = get_observable_property($2) AND unit_id = get_unit($4) 
		AND phenomenon_time_start = $5;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION get_observation(bigint, bigint, text, text, timestamp with time zone) RETURNS bigint AS
$$ 
	SELECT observation_id FROM observation 
	WHERE feature_of_interest_id = get_feature_of_interest($3)
		AND procedure_id = $1 AND observable_property_id = $2 AND unit_id = get_unit($4) 
		AND phenomenon_time_start = $5;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_boolean_observation(bigint, boolean) RETURNS VOID AS
$$ 
 	INSERT INTO observation_has_boolean_value(observation_id, value)
	SELECT $1,$2 WHERE $1 NOT IN (
		SELECT observation_id
		FROM observation_has_boolean_value
		WHERE observation_id = $1
		  AND value = $2
	);
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_count_observation(bigint, int) RETURNS VOID AS
$$ 
 	INSERT INTO observation_has_count_value(observation_id, value)
 	SELECT $1, $2 WHERE $1 NOT IN (
		SELECT observation_id
		FROM observation_has_count_value
		WHERE observation_id = $1
		  AND value = $2
		);
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_text_observation(bigint, text) RETURNS VOID AS
$$ 
 	INSERT INTO observation_has_text_value(observation_id, text_value_id) SELECT $1, insert_text_value($2)
 	WHERE $1 NOT IN (SELECT observation_id FROM observation_has_text_value 
			WHERE observation_id = $1 AND text_value_id = insert_text_value($2));
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_category_observation(bigint, text) RETURNS VOID AS
$$ 
 	INSERT INTO observation_has_category_value(observation_id, category_value_id) SELECT $1, insert_category_value($2)
	WHERE $1 NOT IN (SELECT observation_id FROM observation_has_category_value 
		WHERE observation_id = $1 AND category_value_id = insert_category_value($2));
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_result_template(bigint,bigint,text,text,text) RETURNS bigint AS
$$ 
 	INSERT INTO result_template(observation_constellation_id, feature_of_interest_id, identifier, result_structure, result_encoding)
 	SELECT  $1, $2, $3, $4, $5 WHERE $3 NOT IN (
 		SELECT identifier FROM result_template 
 		WHERE observation_constellation_id = $1 
	 		AND feature_of_interest_id = $2 
	 		AND identifier = $3 
	 		AND result_structure = $4 
	 		AND result_encoding = $5);
 	SELECT result_template_id FROM result_template WHERE identifier = $3;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insert_result_template(text,text,text,text,text,text) RETURNS bigint AS
$$ 
 	SELECT insert_result_template(get_observation_constellation($1, $2, $3, $4), get_feature_of_interest($5),
		$1 || '/template/1'::text,
		'<swe:DataRecord xmlns:swe="http://www.opengis.net/swe/2.0" xmlns:xlink="http://www.w3.org/1999/xlink">
			<swe:field name="phenomenonTime">
				<swe:Time definition="http://www.opengis.net/def/property/OGC/0/PhenomenonTime">
					<swe:uom xlink:href="http://www.opengis.net/def/uom/ISO-8601/0/Gregorian"/>
				</swe:Time>
			</swe:field>
			<swe:field name="'::text || $2 || '">
				<swe:Quantity definition="'::text || $2 || '">
					<swe:uom code="'::text || $6 || '"/>
				</swe:Quantity>
			</swe:field>
		</swe:DataRecord>'::text,
		'<swe:TextEncoding xmlns:swe="http://www.opengis.net/swe/2.0" tokenSeparator="#" blockSeparator="@"/>'::text);
$$
LANGUAGE 'sql';

--
-- NOTE: in table observation: the column identifier can be null but is in the unique constraint....
--

---- OBSERVATION_TYPE
SELECT insert_observation_type('http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CountObservation');
SELECT insert_observation_type('http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement');
SELECT insert_observation_type('http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_SWEArrayObservation');
SELECT insert_observation_type('http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TruthObservation');
SELECT insert_observation_type('http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CategoryObservation');
SELECT insert_observation_type('http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TextObservation');

---- FEATURE_OF_INTEREST_TYPE
SELECT insert_feature_of_interest_type('http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingCurve');
SELECT insert_feature_of_interest_type('http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingSurface');
SELECT insert_feature_of_interest_type('http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint');
SELECT insert_feature_of_interest_type('http://www.opengis.net/def/nil/OGC/0/unknown');

---- PROCEDURE_DESCRIPTION_FORMAT
SELECT insert_procedure_description_format('http://www.opengis.net/sensorML/1.0.1');

---- OFFERING
SELECT insert_offering('http://www.52north.org/test/offering/1');
SELECT insert_offering('http://www.52north.org/test/offering/2');
SELECT insert_offering('http://www.52north.org/test/offering/3');
SELECT insert_offering('http://www.52north.org/test/offering/4');
SELECT insert_offering('http://www.52north.org/test/offering/5');
SELECT insert_offering('http://www.52north.org/test/offering/6');
SELECT insert_offering('http://www.52north.org/test/offering/7');
SELECT insert_offering('http://www.52north.org/test/offering/8');

SELECT insert_allowed_observation_types_for_offering('http://www.52north.org/test/offering/1', 'Measurement');
SELECT insert_allowed_observation_types_for_offering('http://www.52north.org/test/offering/2', 'CountObservation');
SELECT insert_allowed_observation_types_for_offering('http://www.52north.org/test/offering/3', 'TruthObservation');
SELECT insert_allowed_observation_types_for_offering('http://www.52north.org/test/offering/4', 'CategoryObservation');
SELECT insert_allowed_observation_types_for_offering('http://www.52north.org/test/offering/5', 'TextObservation');
SELECT insert_allowed_observation_types_for_offering('http://www.52north.org/test/offering/6', 'SWEArrayObservation');
SELECT insert_allowed_observation_types_for_offering('http://www.52north.org/test/offering/7', 'Measurement');
SELECT insert_allowed_observation_types_for_offering('http://www.52north.org/test/offering/8', 'Measurement');

SELECT insert_allowed_feature_of_interest_types_for_offering('http://www.52north.org/test/offering/1', 'Point');
SELECT insert_allowed_feature_of_interest_types_for_offering('http://www.52north.org/test/offering/2', 'Point');
SELECT insert_allowed_feature_of_interest_types_for_offering('http://www.52north.org/test/offering/3', 'Point');
SELECT insert_allowed_feature_of_interest_types_for_offering('http://www.52north.org/test/offering/4', 'Point');
SELECT insert_allowed_feature_of_interest_types_for_offering('http://www.52north.org/test/offering/5', 'Point');
SELECT insert_allowed_feature_of_interest_types_for_offering('http://www.52north.org/test/offering/6', 'Point');
SELECT insert_allowed_feature_of_interest_types_for_offering('http://www.52north.org/test/offering/7', 'Point');
SELECT insert_allowed_feature_of_interest_types_for_offering('http://www.52north.org/test/offering/8', 'Point');

---- FEATURE_OF_INTEREST
-- con terra
SELECT insert_feature_of_interest('http://www.52north.org/test/featureOfInterest/1', 7.727958, 51.883906, 'con terra');
-- ESRI
SELECT insert_feature_of_interest('http://www.52north.org/test/featureOfInterest/2', -117.1957110000000, 34.056517, 'ESRI');
-- Kisters
SELECT insert_feature_of_interest('http://www.52north.org/test/featureOfInterest/3', 6.1320144042060925, 50.78570661296184, 'Kisters');
-- IfGI
SELECT insert_feature_of_interest('http://www.52north.org/test/featureOfInterest/4', 7.593655600000034, 51.9681661, 'IfGI');
-- TU-D
SELECT insert_feature_of_interest('http://www.52north.org/test/featureOfInterest/5', 13.72375999999997, 51.02881, 'TU-Dresden');
-- HBO
SELECT insert_feature_of_interest('http://www.52north.org/test/featureOfInterest/6', 7.270806, 51.447722, 'Hochschule Bochum');
-- ITC
SELECT insert_feature_of_interest('http://www.52north.org/test/featureOfInterest/7', 4.283393599999954, 52.0464393, 'ITC');
-- DLZ-IT
SELECT insert_feature_of_interest('http://www.52north.org/test/featureOfInterest/8', 10.94306000000006, 50.68606, 'DLZ-IT');

---- UNIT
SELECT insert_unit('test_unit_1');
SELECT insert_unit('test_unit_2');
SELECT insert_unit('test_unit_3');
SELECT insert_unit('test_unit_4');
SELECT insert_unit('test_unit_5');
SELECT insert_unit('test_unit_6');
SELECT insert_unit('test_unit_7');
SELECT insert_unit('test_unit_8');

---- OBSERVABLE_PROPERTY
SELECT insert_observable_property('http://www.52north.org/test/observableProperty/1');
SELECT insert_observable_property('http://www.52north.org/test/observableProperty/2');
SELECT insert_observable_property('http://www.52north.org/test/observableProperty/3');
SELECT insert_observable_property('http://www.52north.org/test/observableProperty/4');
SELECT insert_observable_property('http://www.52north.org/test/observableProperty/5');
SELECT insert_observable_property('http://www.52north.org/test/observableProperty/6');
SELECT insert_observable_property('http://www.52north.org/test/observableProperty/7');
SELECT insert_observable_property('http://www.52north.org/test/observableProperty/8');

---- PROCEDURES

-- con terra
SELECT insert_procedure('http://www.52north.org/test/procedure/1', '2012-11-19 13:00', 'http://www.52north.org/test/observableProperty/1', 7.727958, 51.883906, 0.0, 'con terra GmbH (www.conterra.de)', 'con terra');
-- ESRI
SELECT insert_procedure('http://www.52north.org/test/procedure/2', '2012-11-19 13:00', 'http://www.52north.org/test/observableProperty/2', -117.1957110000000, 34.056517, 0.0, 'ESRI (www.esri.com)', 'ESRI');
-- Kisters
SELECT insert_procedure('http://www.52north.org/test/procedure/3', '2012-11-19 13:00', 'http://www.52north.org/test/observableProperty/3', 6.1320144042060925, 50.78570661296184, 0.0, 'Kisters AG (www.kisters.de)', 'Kisters');
-- IfGI
SELECT insert_procedure('http://www.52north.org/test/procedure/4', '2012-11-19 13:00', 'http://www.52north.org/test/observableProperty/4', 7.593655600000034, 51.9681661, 0.0, 'Institute for Geoinformatics (http://ifgi.uni-muenster.de/en)', 'IfGI');
-- TU-D
SELECT insert_procedure('http://www.52north.org/test/procedure/5', '2012-11-19 13:00', 'http://www.52north.org/test/observableProperty/5', 13.72375999999997, 51.02881, 0.0, 'Technical University Dresden (http://tu-dresden.de/en)', 'TU-Dresden');
-- HBO
SELECT insert_procedure('http://www.52north.org/test/procedure/6', '2012-11-19 13:00', 'http://www.52north.org/test/observableProperty/6', 7.270806, 51.447722, 0.0, 'Hochschule Bochum - Bochum University of Applied Sciences (http://www.hochschule-bochum.de/en/)', 'Hochschule Bochum');
-- ITC
SELECT insert_procedure('http://www.52north.org/test/procedure/7', '2012-11-19 13:00', 'http://www.52north.org/test/observableProperty/7', 4.283393599999954, 52.0464393, 0.0, 'ITC - University of Twente (http://www.itc.nl/)', 'ITC');
-- DLZ-IT
SELECT insert_procedure('http://www.52north.org/test/procedure/8', '2012-11-19 13:00', 'http://www.52north.org/test/observableProperty/8', 10.94306000000006, 50.68606, 0.0, 'Bundesanstalt für IT-Dienstleistungen im Geschäftsbereich des BMVBS (http://www.dlz-it.de)', 'DLZ-IT');

-- OBSERVATION_CONSTELLATION
SELECT insert_observation_constellation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/offering/1', 'Measurement');
SELECT insert_observation_constellation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/offering/2', 'CountObservation');
SELECT insert_observation_constellation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/offering/3', 'TruthObservation');
SELECT insert_observation_constellation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/offering/4', 'CategoryObservation');
SELECT insert_observation_constellation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/offering/5', 'TextObservation');
SELECT insert_observation_constellation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/offering/6', 'SWEArrayObservation');
SELECT insert_observation_constellation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/offering/7', 'Measurement');
SELECT insert_observation_constellation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/offering/8', 'Measurement');

-- INSERT OBSERVATIONS
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:00Z', 'http://www.52north.org/test/offering/1'), 1.2);	
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:01Z', 'http://www.52north.org/test/offering/1'), 1.3); 
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:02Z', 'http://www.52north.org/test/offering/1'), 1.4);
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:03Z', 'http://www.52north.org/test/offering/1'), 1.5);
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:04Z', 'http://www.52north.org/test/offering/1'), 1.6);	
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:05Z', 'http://www.52north.org/test/offering/1'), 1.7);	
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:06Z', 'http://www.52north.org/test/offering/1'), 1.8);	
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:07Z', 'http://www.52north.org/test/offering/1'), 1.9);	
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:08Z', 'http://www.52north.org/test/offering/1'), 2.0);
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:09Z', 'http://www.52north.org/test/offering/1'), 2.1);	
	
SELECT insert_count_observation(insert_observation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:00Z', 'http://www.52north.org/test/offering/2'), 1);
SELECT insert_count_observation(insert_observation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:01Z', 'http://www.52north.org/test/offering/2'), 2);
SELECT insert_count_observation(insert_observation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:02Z', 'http://www.52north.org/test/offering/2'), 3);
SELECT insert_count_observation(insert_observation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:03Z', 'http://www.52north.org/test/offering/2'), 4);
SELECT insert_count_observation(insert_observation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:04Z', 'http://www.52north.org/test/offering/2'), 5);
SELECT insert_count_observation(insert_observation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:05Z', 'http://www.52north.org/test/offering/2'), 6);
SELECT insert_count_observation(insert_observation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:06Z', 'http://www.52north.org/test/offering/2'), 7);
SELECT insert_count_observation(insert_observation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:07Z', 'http://www.52north.org/test/offering/2'), 8);
SELECT insert_count_observation(insert_observation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:08Z', 'http://www.52north.org/test/offering/2'), 9);
SELECT insert_count_observation(insert_observation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:09Z', 'http://www.52north.org/test/offering/2'), 10);

SELECT insert_boolean_observation(insert_observation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:00Z', 'http://www.52north.org/test/offering/3'), true);
SELECT insert_boolean_observation(insert_observation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:01Z', 'http://www.52north.org/test/offering/3'), false);
SELECT insert_boolean_observation(insert_observation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:02Z', 'http://www.52north.org/test/offering/3'), false);
SELECT insert_boolean_observation(insert_observation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:03Z', 'http://www.52north.org/test/offering/3'), true);
SELECT insert_boolean_observation(insert_observation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:04Z', 'http://www.52north.org/test/offering/3'), false);
SELECT insert_boolean_observation(insert_observation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:05Z', 'http://www.52north.org/test/offering/3'), true);
SELECT insert_boolean_observation(insert_observation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:06Z', 'http://www.52north.org/test/offering/3'), true);
SELECT insert_boolean_observation(insert_observation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:07Z', 'http://www.52north.org/test/offering/3'), false);
SELECT insert_boolean_observation(insert_observation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:08Z', 'http://www.52north.org/test/offering/3'), false);
SELECT insert_boolean_observation(insert_observation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:09Z', 'http://www.52north.org/test/offering/3'), true);

SELECT insert_category_observation(insert_observation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:00Z', 'http://www.52north.org/test/offering/4'), 'test_category_1');
SELECT insert_category_observation(insert_observation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:01Z', 'http://www.52north.org/test/offering/4'), 'test_category_2');
SELECT insert_category_observation(insert_observation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:02Z', 'http://www.52north.org/test/offering/4'), 'test_category_1');
SELECT insert_category_observation(insert_observation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:03Z', 'http://www.52north.org/test/offering/4'), 'test_category_5');
SELECT insert_category_observation(insert_observation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:04Z', 'http://www.52north.org/test/offering/4'), 'test_category_4');
SELECT insert_category_observation(insert_observation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:05Z', 'http://www.52north.org/test/offering/4'), 'test_category_3');
SELECT insert_category_observation(insert_observation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:06Z', 'http://www.52north.org/test/offering/4'), 'test_category_1');
SELECT insert_category_observation(insert_observation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:07Z', 'http://www.52north.org/test/offering/4'), 'test_category_2');
SELECT insert_category_observation(insert_observation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:08Z', 'http://www.52north.org/test/offering/4'), 'test_category_1');
SELECT insert_category_observation(insert_observation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:09Z', 'http://www.52north.org/test/offering/4'), 'test_category_6');

SELECT insert_text_observation(insert_observation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:00Z', 'http://www.52north.org/test/offering/5'), 'test_text_value_0');
SELECT insert_text_observation(insert_observation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:01Z', 'http://www.52north.org/test/offering/5'), 'test_text_value_1');
SELECT insert_text_observation(insert_observation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:02Z', 'http://www.52north.org/test/offering/5'), 'test_text_value_3');
SELECT insert_text_observation(insert_observation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:03Z', 'http://www.52north.org/test/offering/5'), 'test_text_value_4');
SELECT insert_text_observation(insert_observation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:04Z', 'http://www.52north.org/test/offering/5'), 'test_text_value_5');
SELECT insert_text_observation(insert_observation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:05Z', 'http://www.52north.org/test/offering/5'), 'test_text_value_6');
SELECT insert_text_observation(insert_observation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:06Z', 'http://www.52north.org/test/offering/5'), 'test_text_value_7');
SELECT insert_text_observation(insert_observation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:07Z', 'http://www.52north.org/test/offering/5'), 'test_text_value_7');
SELECT insert_text_observation(insert_observation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:08Z', 'http://www.52north.org/test/offering/5'), 'test_text_value_8');
SELECT insert_text_observation(insert_observation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:09Z', 'http://www.52north.org/test/offering/5'), 'test_text_value_10');

SELECT insert_result_template('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/offering/6', 'SWEArrayObservation', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6');

SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:00Z', 'http://www.52north.org/test/offering/6'), 1.2);
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:01Z', 'http://www.52north.org/test/offering/6'), 1.3);
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:02Z', 'http://www.52north.org/test/offering/6'), 1.4);
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:03Z', 'http://www.52north.org/test/offering/6'), 1.5);
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:04Z', 'http://www.52north.org/test/offering/6'), 1.6);
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:05Z', 'http://www.52north.org/test/offering/6'), 1.7);
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:06Z', 'http://www.52north.org/test/offering/6'), 1.8);
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:07Z', 'http://www.52north.org/test/offering/6'), 1.9);
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:08Z', 'http://www.52north.org/test/offering/6'), 2.0);
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:09Z', 'http://www.52north.org/test/offering/6'), 2.1);

INSERT INTO observation(procedure_id, observable_property_id, feature_of_interest_id, unit_id, phenomenon_time_start, phenomenon_time_end, result_time, identifier)
	SELECT  get_procedure('http://www.52north.org/test/procedure/1'), get_observable_property('http://www.52north.org/test/observableProperty/1'),
			get_feature_of_interest('http://www.52north.org/test/featureOfInterest/1'), get_unit('test_unit_1'), '2012-11-19 13:10Z', '2012-11-19 13:15Z', '2012-11-19 13:16Z', 'http://www.52north.org/test/observation/1'
	WHERE 'http://www.52north.org/test/observation/1' NOT IN (SELECT identifier FROM observation WHERE identifier = 'http://www.52north.org/test/observation/1');

INSERT INTO observation_relates_to_offering (observation_id, offering_id) VALUES ((SELECT observation_id FROM observation WHERE identifier = 'http://www.52north.org/test/observation/1'), get_offering('http://www.52north.org/test/offering/1'));
	
INSERT INTO observation_has_numeric_value(observation_id, numeric_value_id) 
		SELECT o.observation_id, insert_numeric_value(3.5) 
		FROM observation AS o 
		WHERE o.identifier = 'http://www.52north.org/test/observation/1'
			AND o.observation_id NOT IN (SELECT observation_id FROM observation_has_numeric_value AS ohnv
											WHERE ohnv.observation_id = o.observation_id);

INSERT INTO observation(procedure_id, observable_property_id, feature_of_interest_id, unit_id, phenomenon_time_start, phenomenon_time_end, result_time, identifier)
	SELECT  get_procedure('http://www.52north.org/test/procedure/1'), get_observable_property('http://www.52north.org/test/observableProperty/1'),
			get_feature_of_interest('http://www.52north.org/test/featureOfInterest/1'), get_unit('test_unit_1'), '2012-11-19 13:15Z', '2012-11-19 13:20Z', '2012-11-19 13:21Z', 'http://www.52north.org/test/observation/2'
	WHERE 'http://www.52north.org/test/observation/2' NOT IN (SELECT identifier FROM observation WHERE identifier = 'http://www.52north.org/test/observation/2');

INSERT INTO observation_relates_to_offering (observation_id, offering_id) VALUES ((SELECT observation_id FROM observation WHERE identifier = 'http://www.52north.org/test/observation/2'), get_offering('http://www.52north.org/test/offering/1'));	
	
INSERT INTO observation_has_numeric_value(observation_id, numeric_value_id) 
		SELECT o.observation_id, insert_numeric_value(4.2) 
		FROM observation AS o 
		WHERE o.identifier = 'http://www.52north.org/test/observation/2'
			AND o.observation_id NOT IN (SELECT observation_id FROM observation_has_numeric_value AS ohnv
											WHERE ohnv.observation_id = o.observation_id);
											
-- INSERT OBSERVATIONS
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:00Z', 'http://www.52north.org/test/offering/7'), 1.2);	
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:01Z', 'http://www.52north.org/test/offering/7'), 1.3); 
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:02Z', 'http://www.52north.org/test/offering/7'), 1.4);
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:03Z', 'http://www.52north.org/test/offering/7'), 1.5);
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:04Z', 'http://www.52north.org/test/offering/7'), 1.6);	
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:05Z', 'http://www.52north.org/test/offering/7'), 1.7);	
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:06Z', 'http://www.52north.org/test/offering/7'), 1.8);	
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:07Z', 'http://www.52north.org/test/offering/7'), 1.9);	
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:08Z', 'http://www.52north.org/test/offering/7'), 2.0);
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:09Z', 'http://www.52north.org/test/offering/7'), 2.1);

-- INSERT OBSERVATIONS
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:00Z', 'http://www.52north.org/test/offering/8'), 1.2);	
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:01Z', 'http://www.52north.org/test/offering/8'), 1.3); 
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:02Z', 'http://www.52north.org/test/offering/8'), 1.4);
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:03Z', 'http://www.52north.org/test/offering/8'), 1.5);
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:04Z', 'http://www.52north.org/test/offering/8'), 1.6);	
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:05Z', 'http://www.52north.org/test/offering/8'), 1.7);	
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:06Z', 'http://www.52north.org/test/offering/8'), 1.8);	
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:07Z', 'http://www.52north.org/test/offering/8'), 1.9);	
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:08Z', 'http://www.52north.org/test/offering/8'), 2.0);
SELECT insert_numeric_observation(insert_observation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:09Z', 'http://www.52north.org/test/offering/8'), 2.1);

DROP FUNCTION get_feature_of_interest(text);
DROP FUNCTION get_feature_of_interest_type(text);
DROP FUNCTION get_observable_property(text);
DROP FUNCTION get_observation_constellation(bigint,bigint,bigint,bigint);
DROP FUNCTION get_observation_constellation(text,text,text,text);
DROP FUNCTION get_observation_type(text);
DROP FUNCTION get_offering(text);
DROP FUNCTION get_procedure(text);
DROP FUNCTION get_sensor_ml_description_format();
DROP FUNCTION get_spatial_sampling_feature_type(text);
DROP FUNCTION get_unit(text);
DROP FUNCTION insert_boolean_observation(bigint, boolean);
DROP FUNCTION insert_category_observation(bigint, text);
DROP FUNCTION insert_category_value(text);
DROP FUNCTION insert_count_observation(bigint, int);
DROP FUNCTION insert_feature_of_interest(text, numeric, numeric, text);
DROP FUNCTION insert_feature_of_interest_type(text);
DROP FUNCTION insert_geometry_value(geometry);
DROP FUNCTION insert_numeric_observation(bigint, numeric);
DROP FUNCTION insert_numeric_value(numeric);
DROP FUNCTION insert_observable_property(text);
DROP FUNCTION insert_observation(text, text, text, text, timestamp with time zone, text);
DROP FUNCTION insert_observation_constellation(bigint,bigint,bigint,bigint);
DROP FUNCTION insert_observation_constellation(text,text,text,text);
DROP FUNCTION insert_observation_type(text);
DROP FUNCTION insert_offering(text);
DROP FUNCTION insert_observation_offering(bigint, bigint);
DROP FUNCTION insert_procedure_description_format(text);
DROP FUNCTION insert_procedure(text,timestamp with time zone,text,numeric,numeric,numeric, text, text);
DROP FUNCTION insert_text_observation(bigint, text);
DROP FUNCTION insert_text_value(text);
DROP FUNCTION insert_unit(text);
DROP FUNCTION insert_result_template(text,text,text,text,text,text);
DROP FUNCTION insert_result_template(bigint,bigint,text,text,text);
DROP FUNCTION insert_allowed_observation_types_for_offering(text,text);
DROP FUNCTION insert_allowed_observation_types_for_offering(bigint,bigint);
DROP FUNCTION insert_allowed_feature_of_interest_types_for_offering(text,text);
DROP FUNCTION insert_allowed_feature_of_interest_types_for_offering(bigint,bigint);
DROP FUNCTION get_observation(bigint, bigint, text, text, timestamp with time zone);
DROP FUNCTION create_sensor_description(text,text,numeric,numeric,numeric, text, text);