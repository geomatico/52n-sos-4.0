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
-- SELECT insertOffering();	
--
-- wont work:
-- SELECT insertOffering(); -- offering
--

CREATE OR REPLACE FUNCTION getObservationType(text) RETURNS bigint AS
$$
	SELECT observationTypeId FROM public.observationType 
	WHERE observationType = 'http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_'::text || $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION getProcedure(text) RETURNS bigint AS
$$
	SELECT procedureId FROM public.procedure WHERE identifier = $1; 
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION getFeatureOfInterestType(text) RETURNS bigint AS
$$
	SELECT featureOfInterestTypeId 
	FROM public.featureOfInterestType 
	WHERE featureOfInterestType = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION getSpatialSamplingFeatureType(text) RETURNS bigint AS
$$
	SELECT getFeatureOfInterestType('http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_Sampling'::text || $1);
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION getFeatureOfInterest(text) RETURNS bigint AS
$$
	SELECT featureOfInterestId FROM public.featureOfInterest WHERE identifier = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION getSensor_mlDescriptionFormat() RETURNS bigint AS
$$
	SELECT procedureDescriptionFormatId FROM public.procedureDescriptionFormat 
	WHERE procedureDescriptionFormat = 'http://www.opengis.net/sensorML/1.0.1'::text;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION getOffering(text) RETURNS bigint AS
$$
	SELECT offeringId FROM public.offering WHERE identifier = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION getObservableProperty(text) RETURNS bigint AS
$$ 
	SELECT observablePropertyId FROM public.observableProperty WHERE identifier = $1;
$$
LANGUAGE 'sql';

---- INSERTION FUNCTIONS
CREATE OR REPLACE FUNCTION insertObservationType(text) RETURNS bigint AS
$$
	INSERT INTO public.observationType(observationTypeId, observationType) SELECT nextval('public.observationTypeId_seq'),$1 WHERE $1 NOT IN (SELECT observationType FROM public.observationType);
	SELECT observationTypeId FROM public.observationType WHERE observationType = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertFeatureOfInterestType(text) RETURNS bigint AS
$$
	INSERT INTO public.featureOfInterestType(featureOfInterestTypeId, featureOfInterestType) SELECT nextval('public.featureOfInterestTypeId_seq'),$1 WHERE $1 NOT IN (SELECT featureOfInterestType FROM public.featureOfInterestType);
	SELECT featureOfInterestTypeId FROM public.featureOfInterestType WHERE featureOfInterestType = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertOffering(text) RETURNS bigint AS
$$
	INSERT INTO public.offering(offeringId,hibernateDiscriminator, identifier, name) 
		SELECT nextval('public.offeringId_seq'),'T',$1, $1 || ' name'::text
		WHERE $1 NOT IN (
			SELECT identifier FROM public.offering);
	SELECT offeringId FROM public.offering WHERE identifier = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertAllowedObservationTypesForOffering(bigint, bigint) RETURNS VOID AS
$$
	INSERT INTO public.offeringAllowedObservationType (offeringId, observationTypeId) 
	SELECT $1, $2
	WHERE $1 NOT IN (
		SELECT offeringId 
		FROM public.offeringAllowedObservationType 
		WHERE offeringId = $1 
		  AND observationTypeId = $2
	);
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertAllowedObservationTypesForOffering(text, text) RETURNS VOID AS
$$
	SELECT insertAllowedObservationTypesForOffering(getOffering($1), getObservationType($2));
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertAllowedFeatureOfInterestTypesForOffering(bigint, bigint) RETURNS VOID AS
$$
	INSERT INTO public.offeringAllowedFeatureType (offeringId, featureOfInterestTypeId) 
	SELECT $1, $2 
	WHERE $1 NOT in (
		SELECT offeringId 
		FROM public.offeringAllowedFeatureType 
		WHERE offeringId = $1 
		  AND featureOfInterestTypeId = $2
		);
	
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertAllowedFeatureOfInterestTypesForOffering(text, text) RETURNS VOID AS
$$
	SELECT insertAllowedFeatureOfInterestTypesForOffering(getOffering($1), getSpatialSamplingFeatureType($2));
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertUnit(text) RETURNS bigint AS
$$
	INSERT INTO public.unit(unitId,unit) SELECT nextval('public.unitId_seq'),$1 WHERE $1 NOT IN (SELECT unit FROM public.unit);
	SELECT unitId FROM public.unit WHERE unit = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertProcedureDescriptionFormat(text) RETURNS bigint AS
$$
	INSERT INTO public.procedureDescriptionFormat(procedureDescriptionFormatId,procedureDescriptionFormat) SELECT nextval('public.procDescFormatId_seq'),$1 WHERE $1 NOT IN (SELECT procedureDescriptionFormat FROM public.procedureDescriptionFormat);
	SELECT procedureDescriptionFormatId FROM public.procedureDescriptionFormat WHERE procedureDescriptionFormat = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertFeatureOfInterest(text, numeric, numeric, text) RETURNS bigint AS
$$
	INSERT INTO public.featureOfInterest(featureOfInterestId, hibernateDiscriminator,featureOfInterestTypeId, identifier, names, geom, descriptionXml) 
	SELECT nextval('public.featureOfInterestId_seq'),'T', getSpatialSamplingFeatureType('Point'), $1, $4, ST_GeomFromText('POINT(' || $2 || ' ' || $3 || ')', 4326), 
'<sams:SFSpatialSamplingFeature 
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:sams="http://www.opengis.net/samplingSpatial/2.0" 
	xmlns:sf="http://www.opengis.net/sampling/2.0" 
	xmlns:gml="http://www.opengis.net/gml/3.2" gml:id="ssf_'::text || $1 || '">
	<gml:identifier codeSpace="">'::text || $1 || '</gml:identifier>
	<gml:name>'::text || $4 || '</gml:name>
	<sf:type xlink:href="http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint"/>
	<sf:sampledFeature xlink:href="http://www.opengis.net/def/nil/OGC/0/unknown"/>
	<sams:shape>
		<gml:Point gml:id="pSsf_'::text || $1 || '">
			<gml:pos srsName="http://www.opengis.net/def/crs/EPSG/0/4326">'::text|| $3 || ' '::text || $2 || '</gml:pos>
		</gml:Point>
	</sams:shape>
</sams:SFSpatialSamplingFeature>'::text
	WHERE $1 NOT IN (SELECT identifier FROM public.featureOfInterest);
	SELECT featureOfInterestId FROM public.featureOfInterest WHERE identifier = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertObservableProperty(text) RETURNS bigint AS
$$
	INSERT INTO public.observableProperty(observablePropertyId,hibernateDiscriminator,identifier, description) SELECT nextval('public.observablePropertyId_seq'),'T',$1, $1
	WHERE $1 NOT IN (SELECT identifier FROM public.observableProperty WHERE identifier = $1);
	SELECT observablePropertyId FROM public.observableProperty WHERE identifier = $1
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION createSensorDescription(text, text, numeric, numeric, numeric, text, text) RETURNS text AS
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
              <swe:uom code="NOTDEFINED"/>
            </swe:Quantity>
          </sml:output>
        </sml:OutputList>
      </sml:outputs>
    </sml:System>
  </sml:member>
</sml:SensorML>'::text;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertProcedure(text,timestamp with time zone,text,numeric,numeric,numeric, text, text) RETURNS bigint AS
$$
	INSERT INTO public.procedure(procedureId, hibernateDiscriminator, identifier, procedureDescriptionFormatId, deleted) SELECT 
		nextval('public.procedureId_seq'),'T',$1, getSensor_mlDescriptionFormat(), 'F' WHERE $1 NOT IN (
			SELECT identifier FROM public.procedure WHERE identifier = $1);
	INSERT INTO public.validProcedureTime(validProcedureTimeId,procedureId, startTime, descriptionXml) 
		SELECT nextval('public.validProcedureTimeId_seq'),getProcedure($1), $2, createSensorDescription($1, $3, $4, $5, $6, $7, $8)
		WHERE getProcedure($1) NOT IN (
			SELECT procedureId FROM public.validProcedureTime WHERE procedureId = getProcedure($1));
	SELECT getProcedure($1);
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION getObservationConstellation(bigint,bigint,bigint,bigint) RETURNS bigint AS
$$
	SELECT observationConstellationId FROM public.observationConstellation WHERE procedureId = $1 AND observablePropertyId = $2 AND offeringId = $3 AND observationTypeId = $4;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION getObservationConstellation(text,text,text,text) RETURNS bigint AS
$$
	SELECT getObservationConstellation(getProcedure($1), getObservableProperty($2), getOffering($3), getObservationType($4));
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertObservationConstellation(bigint,bigint,bigint,bigint) RETURNS VOID AS
$$
	INSERT INTO public.observationConstellation (observationConstellationId,procedureId, observablePropertyId, offeringId, observationTypeId) VALUES (nextval('public.observationConstellationId_seq'),$1, $2, $3, $4);
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertObservationConstellation(text,text,text,text) RETURNS VOID AS
$$
	SELECT insertObservationConstellation(getProcedure($1), getObservableProperty($2), getOffering($3), getObservationType($4));
$$
LANGUAGE 'sql';

-- UNIT
CREATE OR REPLACE FUNCTION getUnit(text) RETURNS bigint AS
$$
	SELECT unitId FROM public.unit WHERE unit = $1;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertNumericObservation(bigint, numeric) RETURNS VOID AS
$$
	INSERT INTO public.numericValue(observationId, value) VALUES ($1,$2);
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertObservationOffering(bigint, bigint) RETURNS VOID AS
$$
	INSERT INTO public.observationHasOffering (observationId, offeringId) VALUES ($1, $2);
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertObservation(text, text, text, text, timestamp with time zone, text) RETURNS bigint AS
$$ 
	INSERT INTO public.observation(observationId, procedureId, observablePropertyId, featureOfInterestId, unitId, phenomenonTimeStart, phenomenonTimeEnd, resultTime)
	SELECT nextval('public.observationId_seq'),getProcedure($1), getObservableProperty($2), getFeatureOfInterest($3), getUnit($4), $5, $5, $5 WHERE getProcedure($1) NOT IN (
		SELECT procedureId FROM public.observation
		WHERE procedureId = getProcedure($1) AND observablePropertyId = getObservableProperty($2) AND featureOfInterestId = getFeatureOfInterest($3) 
				AND unitId = getUnit($4) AND phenomenonTimeStart = $5 AND phenomenonTimeEnd = $5 AND resultTime = $5)
		AND getObservableProperty($2) NOT IN (SELECT observablePropertyId FROM public.observation
		WHERE procedureId = getProcedure($1) AND observablePropertyId = getObservableProperty($2) AND featureOfInterestId = getFeatureOfInterest($3) 
				AND unitId = getUnit($4) AND phenomenonTimeStart = $5 AND phenomenonTimeEnd = $5 AND resultTime = $5);
				
	SELECT insertObservationOffering((SELECT observationId FROM public.observation 
	WHERE featureOfInterestId = getFeatureOfInterest($3)
		AND procedureId = getProcedure($1) AND observablePropertyId = getObservableProperty($2) AND unitId = getUnit($4) 
		AND phenomenonTimeStart = $5), getOffering($6));

	SELECT observationId FROM public.observation 
	WHERE featureOfInterestId = getFeatureOfInterest($3)
		AND procedureId = getProcedure($1) AND observablePropertyId = getObservableProperty($2) AND unitId = getUnit($4) 
		AND phenomenonTimeStart = $5;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION getObservation(bigint, bigint, text, text, timestamp with time zone) RETURNS bigint AS
$$ 
	SELECT observationId FROM public.observation 
	WHERE featureOfInterestId = getFeatureOfInterest($3)
		AND procedureId = $1 AND observablePropertyId = $2 AND unitId = getUnit($4) 
		AND phenomenonTimeStart = $5;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertBooleanObservation(bigint, char) RETURNS VOID AS
$$ 
 	INSERT INTO public.booleanValue(observationId, value)
	VALUES ($1,$2);
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertCountObservation(bigint, int) RETURNS VOID AS
$$ 
 	INSERT INTO public.countValue(observationId, value) VALUES ($1, $2);
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertTextObservation(bigint, text) RETURNS VOID AS
$$ 
 	INSERT INTO public.textValue(observationId, value) VALUES ($1,$2);
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertCategoryObservation(bigint, text) RETURNS VOID AS
$$ 
 	INSERT INTO public.categoryValue(observationId, value) VALUES ($1, $2);
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertResultTemplate(bigint,bigint,bigint,bigint,text,text,text) RETURNS bigint AS
$$ 
 	INSERT INTO public.resultTemplate(resultTemplateId,procedureId,observablePropertyId, offeringId, featureOfInterestId, identifier, resultStructure, resultEncoding)
 	SELECT  nextval('public.resultTemplateId_seq'),$1, $2, $3, $4, $5, $6, $7 WHERE $5 NOT IN (
 		SELECT identifier FROM public.resultTemplate 
 		WHERE procedureId = $1 
 			AND observablePropertyId = $2 
 			AND offeringId = $3
	 		AND featureOfInterestId = $4 
	 		AND identifier = $5 
	 		AND resultStructure = $6 
	 		AND resultEncoding = $7);
 	SELECT resultTemplateId FROM public.resultTemplate WHERE identifier = $5;
$$
LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION insertResultTemplate(text,text,text,text,text) RETURNS bigint AS
$$ 
 	SELECT insertResultTemplate(getProcedure($1), getObservableProperty($2), getOffering($3), getFeatureOfInterest($4),
		$1 || '/template/1'::text,
		'<swe:DataRecord xmlns:swe="http://www.opengis.net/swe/2.0" xmlns:xlink="http://www.w3.org/1999/xlink">
			<swe:field name="phenomenonTime">
				<swe:Time definition="http://www.opengis.net/def/property/OGC/0/PhenomenonTime">
					<swe:uom xlink:href="http://www.opengis.net/def/uom/ISO-8601/0/Gregorian"/>
				</swe:Time>
			</swe:field>
			<swe:field name="'::text || $2 || '">
				<swe:Quantity definition="'::text || $2 || '">
					<swe:uom code="'::text || $5 || '"/>
				</swe:Quantity>
			</swe:field>
		</swe:DataRecord>'::text,
		'<swe:TextEncoding xmlns:swe="http://www.opengis.net/swe/2.0" tokenSeparator="#" blockSeparator="@"/>'::text);
$$
LANGUAGE 'sql';

--
-- NOTE: in table observation: the column identifier can be null but is in the unique constraint....
--

---- OBSERVATIONTYPE
SELECT insertObservationType('http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CountObservation');
SELECT insertObservationType('http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement');
SELECT insertObservationType('http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_SWEArrayObservation');
SELECT insertObservationType('http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TruthObservation');
SELECT insertObservationType('http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CategoryObservation');
SELECT insertObservationType('http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TextObservation');

---- FEATUREOFINTERESTTYPE
SELECT insertFeatureOfInterestType('http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingCurve');
SELECT insertFeatureOfInterestType('http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingSurface');
SELECT insertFeatureOfInterestType('http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint');
SELECT insertFeatureOfInterestType('http://www.opengis.net/def/nil/OGC/0/unknown');

---- PROCEDUREDESCRIPTIONFORMAT
SELECT insertProcedureDescriptionFormat('http://www.opengis.net/sensorML/1.0.1');

---- OFFERING
SELECT insertOffering('http://www.52north.org/test/offering/1');
SELECT insertOffering('http://www.52north.org/test/offering/2');
SELECT insertOffering('http://www.52north.org/test/offering/3');
SELECT insertOffering('http://www.52north.org/test/offering/4');
SELECT insertOffering('http://www.52north.org/test/offering/5');
SELECT insertOffering('http://www.52north.org/test/offering/6');
SELECT insertOffering('http://www.52north.org/test/offering/7');
SELECT insertOffering('http://www.52north.org/test/offering/8');

SELECT insertAllowedObservationTypesForOffering('http://www.52north.org/test/offering/1', 'Measurement');
SELECT insertAllowedObservationTypesForOffering('http://www.52north.org/test/offering/2', 'CountObservation');
SELECT insertAllowedObservationTypesForOffering('http://www.52north.org/test/offering/3', 'TruthObservation');
SELECT insertAllowedObservationTypesForOffering('http://www.52north.org/test/offering/4', 'CategoryObservation');
SELECT insertAllowedObservationTypesForOffering('http://www.52north.org/test/offering/5', 'TextObservation');
SELECT insertAllowedObservationTypesForOffering('http://www.52north.org/test/offering/6', 'SWEArrayObservation');
SELECT insertAllowedObservationTypesForOffering('http://www.52north.org/test/offering/7', 'Measurement');
SELECT insertAllowedObservationTypesForOffering('http://www.52north.org/test/offering/8', 'Measurement');

SELECT insertAllowedFeatureOfInterestTypesForOffering('http://www.52north.org/test/offering/1', 'Point');
SELECT insertAllowedFeatureOfInterestTypesForOffering('http://www.52north.org/test/offering/2', 'Point');
SELECT insertAllowedFeatureOfInterestTypesForOffering('http://www.52north.org/test/offering/3', 'Point');
SELECT insertAllowedFeatureOfInterestTypesForOffering('http://www.52north.org/test/offering/4', 'Point');
SELECT insertAllowedFeatureOfInterestTypesForOffering('http://www.52north.org/test/offering/5', 'Point');
SELECT insertAllowedFeatureOfInterestTypesForOffering('http://www.52north.org/test/offering/6', 'Point');
SELECT insertAllowedFeatureOfInterestTypesForOffering('http://www.52north.org/test/offering/7', 'Point');
SELECT insertAllowedFeatureOfInterestTypesForOffering('http://www.52north.org/test/offering/8', 'Point');

---- FEATUREOFINTEREST
-- con terra
SELECT insertFeatureOfInterest('http://www.52north.org/test/featureOfInterest/1', 7.727958, 51.883906, 'con terra');
-- ESRI
SELECT insertFeatureOfInterest('http://www.52north.org/test/featureOfInterest/2', -117.1957110000000, 34.056517, 'ESRI');
-- Kisters
SELECT insertFeatureOfInterest('http://www.52north.org/test/featureOfInterest/3', 6.1320144042060925, 50.78570661296184, 'Kisters');
-- IfGI
SELECT insertFeatureOfInterest('http://www.52north.org/test/featureOfInterest/4', 7.593655600000034, 51.9681661, 'IfGI');
-- TU-D
SELECT insertFeatureOfInterest('http://www.52north.org/test/featureOfInterest/5', 13.72375999999997, 51.02881, 'TU-Dresden');
-- HBO
SELECT insertFeatureOfInterest('http://www.52north.org/test/featureOfInterest/6', 7.270806, 51.447722, 'Hochschule Bochum');
-- ITC
SELECT insertFeatureOfInterest('http://www.52north.org/test/featureOfInterest/7', 4.283393599999954, 52.0464393, 'ITC');
-- DLZ-IT
SELECT insertFeatureOfInterest('http://www.52north.org/test/featureOfInterest/8', 10.94306000000006, 50.68606, 'DLZ-IT');

---- UNIT
SELECT insertUnit('test_unit_1');
SELECT insertUnit('test_unit_2');
SELECT insertUnit('test_unit_3');
SELECT insertUnit('test_unit_4');
SELECT insertUnit('test_unit_5');
SELECT insertUnit('test_unit_6');
SELECT insertUnit('test_unit_7');
SELECT insertUnit('test_unit_8');

---- OBSERVABLEPROPERTY
SELECT insertObservableProperty('http://www.52north.org/test/observableProperty/1');
SELECT insertObservableProperty('http://www.52north.org/test/observableProperty/2');
SELECT insertObservableProperty('http://www.52north.org/test/observableProperty/3');
SELECT insertObservableProperty('http://www.52north.org/test/observableProperty/4');
SELECT insertObservableProperty('http://www.52north.org/test/observableProperty/5');
SELECT insertObservableProperty('http://www.52north.org/test/observableProperty/6');
SELECT insertObservableProperty('http://www.52north.org/test/observableProperty/7');
SELECT insertObservableProperty('http://www.52north.org/test/observableProperty/8');

---- PROCEDURES

-- con terra
SELECT insertProcedure('http://www.52north.org/test/procedure/1', '2012-11-19 13:00', 'http://www.52north.org/test/observableProperty/1', 7.727958, 51.883906, 0.0, 'con terra GmbH (www.conterra.de)', 'con terra');
-- ESRI
SELECT insertProcedure('http://www.52north.org/test/procedure/2', '2012-11-19 13:00', 'http://www.52north.org/test/observableProperty/2', -117.1957110000000, 34.056517, 0.0, 'ESRI (www.esri.com)', 'ESRI');
-- Kisters
SELECT insertProcedure('http://www.52north.org/test/procedure/3', '2012-11-19 13:00', 'http://www.52north.org/test/observableProperty/3', 6.1320144042060925, 50.78570661296184, 0.0, 'Kisters AG (www.kisters.de)', 'Kisters');
-- IfGI
SELECT insertProcedure('http://www.52north.org/test/procedure/4', '2012-11-19 13:00', 'http://www.52north.org/test/observableProperty/4', 7.593655600000034, 51.9681661, 0.0, 'Institute for Geoinformatics (http://ifgi.uni-muenster.de/en)', 'IfGI');
-- TU-D
SELECT insertProcedure('http://www.52north.org/test/procedure/5', '2012-11-19 13:00', 'http://www.52north.org/test/observableProperty/5', 13.72375999999997, 51.02881, 0.0, 'Technical University Dresden (http://tu-dresden.de/en)', 'TU-Dresden');
-- HBO
SELECT insertProcedure('http://www.52north.org/test/procedure/6', '2012-11-19 13:00', 'http://www.52north.org/test/observableProperty/6', 7.270806, 51.447722, 0.0, 'Hochschule Bochum - Bochum University of Applied Sciences (http://www.hochschule-bochum.de/en/)', 'Hochschule Bochum');
-- ITC
SELECT insertProcedure('http://www.52north.org/test/procedure/7', '2012-11-19 13:00', 'http://www.52north.org/test/observableProperty/7', 4.283393599999954, 52.0464393, 0.0, 'ITC - University of Twente (http://www.itc.nl/)', 'ITC');
-- DLZ-IT
SELECT insertProcedure('http://www.52north.org/test/procedure/8', '2012-11-19 13:00', 'http://www.52north.org/test/observableProperty/8', 10.94306000000006, 50.68606, 0.0, 'Bundesanstalt für IT-Dienstleistungen im Geschäftsbereich des BMVBS (http://www.dlz-it.de)', 'DLZ-IT');

-- OBSERVATIONCONSTELLATION
SELECT insertObservationConstellation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/offering/1', 'Measurement');
SELECT insertObservationConstellation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/offering/2', 'CountObservation');
SELECT insertObservationConstellation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/offering/3', 'TruthObservation');
SELECT insertObservationConstellation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/offering/4', 'CategoryObservation');
SELECT insertObservationConstellation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/offering/5', 'TextObservation');
SELECT insertObservationConstellation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/offering/6', 'SWEArrayObservation');
SELECT insertObservationConstellation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/offering/7', 'Measurement');
SELECT insertObservationConstellation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/offering/8', 'Measurement');

-- INSERT OBSERVATIONS
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:00Z', 'http://www.52north.org/test/offering/1'), 1.2);	
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:01Z', 'http://www.52north.org/test/offering/1'), 1.3); 
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:02Z', 'http://www.52north.org/test/offering/1'), 1.4);
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:03Z', 'http://www.52north.org/test/offering/1'), 1.5);
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:04Z', 'http://www.52north.org/test/offering/1'), 1.6);	
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:05Z', 'http://www.52north.org/test/offering/1'), 1.7);	
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:06Z', 'http://www.52north.org/test/offering/1'), 1.8);	
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:07Z', 'http://www.52north.org/test/offering/1'), 1.9);	
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:08Z', 'http://www.52north.org/test/offering/1'), 2.0);
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/1', 'http://www.52north.org/test/observableProperty/1', 'http://www.52north.org/test/featureOfInterest/1', 'test_unit_1', '2012-11-19 13:09Z', 'http://www.52north.org/test/offering/1'), 2.1);	
	
SELECT insertCountObservation(insertObservation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:00Z', 'http://www.52north.org/test/offering/2'), 1);
SELECT insertCountObservation(insertObservation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:01Z', 'http://www.52north.org/test/offering/2'), 2);
SELECT insertCountObservation(insertObservation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:02Z', 'http://www.52north.org/test/offering/2'), 3);
SELECT insertCountObservation(insertObservation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:03Z', 'http://www.52north.org/test/offering/2'), 4);
SELECT insertCountObservation(insertObservation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:04Z', 'http://www.52north.org/test/offering/2'), 5);
SELECT insertCountObservation(insertObservation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:05Z', 'http://www.52north.org/test/offering/2'), 6);
SELECT insertCountObservation(insertObservation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:06Z', 'http://www.52north.org/test/offering/2'), 7);
SELECT insertCountObservation(insertObservation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:07Z', 'http://www.52north.org/test/offering/2'), 8);
SELECT insertCountObservation(insertObservation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:08Z', 'http://www.52north.org/test/offering/2'), 9);
SELECT insertCountObservation(insertObservation('http://www.52north.org/test/procedure/2', 'http://www.52north.org/test/observableProperty/2', 'http://www.52north.org/test/featureOfInterest/2', 'test_unit_2', '2012-11-19 13:09Z', 'http://www.52north.org/test/offering/2'), 10);

SELECT insertBooleanObservation(insertObservation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:00Z', 'http://www.52north.org/test/offering/3'), 'T');
SELECT insertBooleanObservation(insertObservation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:01Z', 'http://www.52north.org/test/offering/3'), 'F');
SELECT insertBooleanObservation(insertObservation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:02Z', 'http://www.52north.org/test/offering/3'), 'F');
SELECT insertBooleanObservation(insertObservation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:03Z', 'http://www.52north.org/test/offering/3'), 'T');
SELECT insertBooleanObservation(insertObservation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:04Z', 'http://www.52north.org/test/offering/3'), 'F');
SELECT insertBooleanObservation(insertObservation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:05Z', 'http://www.52north.org/test/offering/3'), 'T');
SELECT insertBooleanObservation(insertObservation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:06Z', 'http://www.52north.org/test/offering/3'), 'T');
SELECT insertBooleanObservation(insertObservation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:07Z', 'http://www.52north.org/test/offering/3'), 'F');
SELECT insertBooleanObservation(insertObservation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:08Z', 'http://www.52north.org/test/offering/3'), 'F');
SELECT insertBooleanObservation(insertObservation('http://www.52north.org/test/procedure/3', 'http://www.52north.org/test/observableProperty/3', 'http://www.52north.org/test/featureOfInterest/3', 'test_unit_3', '2012-11-19 13:09Z', 'http://www.52north.org/test/offering/3'), 'T');

SELECT insertCategoryObservation(insertObservation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:00Z', 'http://www.52north.org/test/offering/4'), 'test_category_1');
SELECT insertCategoryObservation(insertObservation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:01Z', 'http://www.52north.org/test/offering/4'), 'test_category_2');
SELECT insertCategoryObservation(insertObservation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:02Z', 'http://www.52north.org/test/offering/4'), 'test_category_1');
SELECT insertCategoryObservation(insertObservation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:03Z', 'http://www.52north.org/test/offering/4'), 'test_category_5');
SELECT insertCategoryObservation(insertObservation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:04Z', 'http://www.52north.org/test/offering/4'), 'test_category_4');
SELECT insertCategoryObservation(insertObservation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:05Z', 'http://www.52north.org/test/offering/4'), 'test_category_3');
SELECT insertCategoryObservation(insertObservation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:06Z', 'http://www.52north.org/test/offering/4'), 'test_category_1');
SELECT insertCategoryObservation(insertObservation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:07Z', 'http://www.52north.org/test/offering/4'), 'test_category_2');
SELECT insertCategoryObservation(insertObservation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:08Z', 'http://www.52north.org/test/offering/4'), 'test_category_1');
SELECT insertCategoryObservation(insertObservation('http://www.52north.org/test/procedure/4', 'http://www.52north.org/test/observableProperty/4', 'http://www.52north.org/test/featureOfInterest/4', 'test_unit_4', '2012-11-19 13:09Z', 'http://www.52north.org/test/offering/4'), 'test_category_6');

SELECT insertTextObservation(insertObservation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:00Z', 'http://www.52north.org/test/offering/5'), 'test_text_0');
SELECT insertTextObservation(insertObservation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:01Z', 'http://www.52north.org/test/offering/5'), 'test_text_1');
SELECT insertTextObservation(insertObservation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:02Z', 'http://www.52north.org/test/offering/5'), 'test_text_3');
SELECT insertTextObservation(insertObservation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:03Z', 'http://www.52north.org/test/offering/5'), 'test_text_4');
SELECT insertTextObservation(insertObservation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:04Z', 'http://www.52north.org/test/offering/5'), 'test_text_5');
SELECT insertTextObservation(insertObservation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:05Z', 'http://www.52north.org/test/offering/5'), 'test_text_6');
SELECT insertTextObservation(insertObservation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:06Z', 'http://www.52north.org/test/offering/5'), 'test_text_7');
SELECT insertTextObservation(insertObservation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:07Z', 'http://www.52north.org/test/offering/5'), 'test_text_7');
SELECT insertTextObservation(insertObservation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:08Z', 'http://www.52north.org/test/offering/5'), 'test_text_8');
SELECT insertTextObservation(insertObservation('http://www.52north.org/test/procedure/5', 'http://www.52north.org/test/observableProperty/5', 'http://www.52north.org/test/featureOfInterest/5', 'test_unit_5', '2012-11-19 13:09Z', 'http://www.52north.org/test/offering/5'), 'test_text_10');

SELECT insertResultTemplate('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/offering/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6');

SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:00Z', 'http://www.52north.org/test/offering/6'), 1.2);
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:01Z', 'http://www.52north.org/test/offering/6'), 1.3);
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:02Z', 'http://www.52north.org/test/offering/6'), 1.4);
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:03Z', 'http://www.52north.org/test/offering/6'), 1.5);
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:04Z', 'http://www.52north.org/test/offering/6'), 1.6);
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:05Z', 'http://www.52north.org/test/offering/6'), 1.7);
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:06Z', 'http://www.52north.org/test/offering/6'), 1.8);
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:07Z', 'http://www.52north.org/test/offering/6'), 1.9);
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:08Z', 'http://www.52north.org/test/offering/6'), 2.0);
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/6', 'http://www.52north.org/test/observableProperty/6', 'http://www.52north.org/test/featureOfInterest/6', 'test_unit_6', '2012-11-19 13:09Z', 'http://www.52north.org/test/offering/6'), 2.1);

INSERT INTO public.observation(observationId,procedureId, observablePropertyId, featureOfInterestId, unitId, phenomenonTimeStart, phenomenonTimeEnd, resultTime, identifier)
	SELECT  nextval('public.observationId_seq'),getProcedure('http://www.52north.org/test/procedure/1'), getObservableProperty('http://www.52north.org/test/observableProperty/1'),
			getFeatureOfInterest('http://www.52north.org/test/featureOfInterest/1'), getUnit('test_unit_1'), '2012-11-19 13:10Z', '2012-11-19 13:15Z', '2012-11-19 13:16Z', 'http://www.52north.org/test/observation/1'
	WHERE 'http://www.52north.org/test/observation/1' NOT IN (SELECT identifier FROM public.observation WHERE identifier = 'http://www.52north.org/test/observation/1');

INSERT INTO public.observationHasOffering (observationId, offeringId) VALUES ((SELECT observationId FROM public.observation WHERE identifier = 'http://www.52north.org/test/observation/1'), getOffering('http://www.52north.org/test/offering/1'));
	
INSERT INTO public.numericValue(observationId, value) 
		SELECT o.observationId, 3.5
		FROM public.observation AS o 
		WHERE o.identifier = 'http://www.52north.org/test/observation/1'
			AND o.observationId NOT IN (SELECT observationId FROM public.numericValue AS ohnv
											WHERE ohnv.observationId = o.observationId);

INSERT INTO public.observation(observationId,procedureId, observablePropertyId, featureOfInterestId, unitId, phenomenonTimeStart, phenomenonTimeEnd, resultTime, identifier)
	SELECT  nextval('public.observationId_seq'),getProcedure('http://www.52north.org/test/procedure/1'), getObservableProperty('http://www.52north.org/test/observableProperty/1'),
			getFeatureOfInterest('http://www.52north.org/test/featureOfInterest/1'), getUnit('test_unit_1'), '2012-11-19 13:15Z', '2012-11-19 13:20Z', '2012-11-19 13:21Z', 'http://www.52north.org/test/observation/2'
	WHERE 'http://www.52north.org/test/observation/2' NOT IN (SELECT identifier FROM public.observation WHERE identifier = 'http://www.52north.org/test/observation/2');

INSERT INTO public.observationHasOffering (observationId, offeringId) VALUES ((SELECT observationId FROM public.observation WHERE identifier = 'http://www.52north.org/test/observation/2'), getOffering('http://www.52north.org/test/offering/1'));	
	
INSERT INTO public.numericValue(observationId, value) 
		SELECT o.observationId, 4.2
		FROM public.observation AS o 
		WHERE o.identifier = 'http://www.52north.org/test/observation/2'
			AND o.observationId NOT IN (SELECT observationId FROM public.numericValue AS ohnv
											WHERE ohnv.observationId = o.observationId);
											
-- INSERT OBSERVATIONS
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:00Z', 'http://www.52north.org/test/offering/7'), 1.2);	
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:01Z', 'http://www.52north.org/test/offering/7'), 1.3); 
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:02Z', 'http://www.52north.org/test/offering/7'), 1.4);
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:03Z', 'http://www.52north.org/test/offering/7'), 1.5);
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:04Z', 'http://www.52north.org/test/offering/7'), 1.6);	
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:05Z', 'http://www.52north.org/test/offering/7'), 1.7);	
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:06Z', 'http://www.52north.org/test/offering/7'), 1.8);	
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:07Z', 'http://www.52north.org/test/offering/7'), 1.9);	
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:08Z', 'http://www.52north.org/test/offering/7'), 2.0);
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/7', 'http://www.52north.org/test/observableProperty/7', 'http://www.52north.org/test/featureOfInterest/7', 'test_unit_7', '2012-11-19 13:09Z', 'http://www.52north.org/test/offering/7'), 2.1);

-- INSERT OBSERVATIONS
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:00Z', 'http://www.52north.org/test/offering/8'), 1.2);	
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:01Z', 'http://www.52north.org/test/offering/8'), 1.3); 
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:02Z', 'http://www.52north.org/test/offering/8'), 1.4);
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:03Z', 'http://www.52north.org/test/offering/8'), 1.5);
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:04Z', 'http://www.52north.org/test/offering/8'), 1.6);	
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:05Z', 'http://www.52north.org/test/offering/8'), 1.7);	
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:06Z', 'http://www.52north.org/test/offering/8'), 1.8);	
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:07Z', 'http://www.52north.org/test/offering/8'), 1.9);	
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:08Z', 'http://www.52north.org/test/offering/8'), 2.0);
SELECT insertNumericObservation(insertObservation('http://www.52north.org/test/procedure/8', 'http://www.52north.org/test/observableProperty/8', 'http://www.52north.org/test/featureOfInterest/8', 'test_unit_8', '2012-11-19 13:09Z', 'http://www.52north.org/test/offering/8'), 2.1);

DROP FUNCTION getFeatureOfInterest(text);
DROP FUNCTION getFeatureOfInterestType(text);
DROP FUNCTION getObservableProperty(text);
DROP FUNCTION getObservationConstellation(bigint,bigint,bigint,bigint);
DROP FUNCTION getObservationConstellation(text,text,text,text);
DROP FUNCTION getObservationType(text);
DROP FUNCTION getOffering(text);
DROP FUNCTION getProcedure(text);
DROP FUNCTION getSensor_mlDescriptionFormat();
DROP FUNCTION getSpatialSamplingFeatureType(text);
DROP FUNCTION getUnit(text);
DROP FUNCTION insertBooleanObservation(bigint, char);
DROP FUNCTION insertCategoryObservation(bigint, text);
DROP FUNCTION insertCountObservation(bigint, int);
DROP FUNCTION insertFeatureOfInterest(text, numeric, numeric, text);
DROP FUNCTION insertFeatureOfInterestType(text);
DROP FUNCTION insertNumericObservation(bigint, numeric);
DROP FUNCTION insertObservableProperty(text);
DROP FUNCTION insertObservation(text, text, text, text, timestamp with time zone, text);
DROP FUNCTION insertObservationConstellation(bigint,bigint,bigint,bigint);
DROP FUNCTION insertObservationConstellation(text,text,text,text);
DROP FUNCTION insertObservationType(text);
DROP FUNCTION insertOffering(text);
DROP FUNCTION insertObservationOffering(bigint, bigint);
DROP FUNCTION insertProcedureDescriptionFormat(text);
DROP FUNCTION insertProcedure(text,timestamp with time zone,text,numeric,numeric,numeric, text, text);
DROP FUNCTION insertTextObservation(bigint, text);
DROP FUNCTION insertUnit(text);
DROP FUNCTION insertResultTemplate(bigint,bigint,bigint,bigint,text,text,text);
DROP FUNCTION insertResultTemplate(text,text,text,text,text);
DROP FUNCTION insertAllowedObservationTypesForOffering(text,text);
DROP FUNCTION insertAllowedObservationTypesForOffering(bigint,bigint);
DROP FUNCTION insertAllowedFeatureOfInterestTypesForOffering(text,text);
DROP FUNCTION insertAllowedFeatureOfInterestTypesForOffering(bigint,bigint);
DROP FUNCTION getObservation(bigint, bigint, text, text, timestamp with time zone);
DROP FUNCTION createSensorDescription(text,text,numeric,numeric,numeric, text, text);