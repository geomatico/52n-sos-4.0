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

DELETE FROM textValue WHERE value LIKE 'test_text%';

DELETE FROM categoryValue WHERE value LIKE 'test_category%';

DELETE FROM numericValue AS ohnv
USING   observation AS o, 
        featureOfInterest AS f,
        procedure AS p,
        unit AS u,
        observableProperty AS op
WHERE   ohnv.observationId = o.observationId AND
        f.featureOfInterestId = o.featureOfInterestId AND
        u.unitId = o.unitId AND
        p.procedureId = o.procedureId AND
        o.observablePropertyId = op.observablePropertyId AND
        (
            p.identifier LIKE 'http://www.52north.org/test/procedure/%' OR
            f.identifier LIKE 'http://www.52north.org/test/featureOfInterest/%' OR
            op.identifier LIKE 'http://www.52north.org/test/observableProperty/%' OR
            u.unit LIKE 'test_unit_%'
            
        );
    
DELETE FROM booleanValue AS ohbv
USING   observation AS o, 
        featureOfInterest AS f,
        procedure AS p,
        unit AS u,
        observableProperty AS op
WHERE   ohbv.observationId = o.observationId AND
        f.featureOfInterestId = o.featureOfInterestId AND
        u.unitId = o.unitId AND
        p.procedureId = o.procedureId AND
        o.observablePropertyId = op.observablePropertyId AND
        (
            p.identifier LIKE 'http://www.52north.org/test/procedure/%' OR
            f.identifier LIKE 'http://www.52north.org/test/featureOfInterest/%' OR
            op.identifier LIKE 'http://www.52north.org/test/observableProperty/%' OR
            u.unit LIKE 'test_unit_%'
            
        );

DELETE FROM blobValue AS ohbv
USING   observation AS o, 
        featureOfInterest AS f,
        procedure AS p,
        unit AS u,
        observableProperty AS op
WHERE   ohbv.observationId = o.observationId AND
        f.featureOfInterestId = o.featureOfInterestId AND
        u.unitId = o.unitId AND
        p.procedureId = o.procedureId AND
        o.observablePropertyId = op.observablePropertyId AND
        (
            p.identifier LIKE 'http://www.52north.org/test/procedure/%' OR
            f.identifier LIKE 'http://www.52north.org/test/featureOfInterest/%' OR
            op.identifier LIKE 'http://www.52north.org/test/observableProperty/%' OR
            u.unit LIKE 'test_unit_%'
            
        );

DELETE FROM countValue AS ohcv
USING   observation AS o, 
        featureOfInterest AS f,
        procedure AS p,
        unit AS u,
        observableProperty AS op
WHERE   ohcv.observationId = o.observationId AND
        f.featureOfInterestId = o.featureOfInterestId AND
        u.unitId = o.unitId AND
        p.procedureId = o.procedureId AND
        o.observablePropertyId = op.observablePropertyId AND
        (
            p.identifier LIKE 'http://www.52north.org/test/procedure/%' OR
            f.identifier LIKE 'http://www.52north.org/test/featureOfInterest/%' OR
            op.identifier LIKE 'http://www.52north.org/test/observableProperty/%' OR
            u.unit LIKE 'test_unit_%'
            
        );

DELETE FROM observationHasOffering AS ortooff
USING   observation  AS o,
	procedure AS p,
	offering AS off
WHERE ortooff.offeringId = off.offeringId AND
	o.procedureId = p.procedureId AND
	(
            p.identifier LIKE 'http://www.52north.org/test/procedure/%' OR
            off.identifier LIKE 'http://www.52north.org/test/offering/%'
        );

DELETE FROM resultTemplate WHERE identifier LIKE 'http://www.52north.org/test/procedure/%';

DELETE FROM observationConstellation AS oc
USING	procedure AS p,
	offering AS off
WHERE
	oc.offeringId = off.offeringId AND
	oc.procedureId = p.procedureId AND
	(
            p.identifier LIKE 'http://www.52north.org/test/procedure/%' OR
            off.identifier LIKE 'http://www.52north.org/test/offering/%'
        );

DELETE FROM observation AS o
USING   featureOfInterest AS f,
        procedure AS p,
        unit AS u,
        observableProperty AS op
WHERE   f.featureOfInterestId = o.featureOfInterestId AND
        u.unitId = o.unitId AND
        p.procedureId = o.procedureId AND
        o.observablePropertyId = op.observablePropertyId AND
        (
            p.identifier LIKE 'http://www.52north.org/test/procedure/%' OR
            f.identifier LIKE 'http://www.52north.org/test/featureOfInterest/%' OR
            op.identifier LIKE 'http://www.52north.org/test/observableProperty/%' OR
            u.unit LIKE 'test_unit_%'
            
        ) ;

DELETE FROM observationConstellation AS oc
USING   procedure AS p,
        observableProperty AS op
WHERE   p.procedureId = oc.procedureId AND
        oc.observablePropertyId = op.observablePropertyId AND
        (
            p.identifier LIKE 'http://www.52north.org/test/procedure/%' OR
            op.identifier LIKE 'http://www.52north.org/test/observableProperty/%'
        );

DELETE FROM validProcedureTime AS vt
USING   procedure AS p
WHERE   p.procedureId = vt.procedureId AND 
        p.identifier LIKE 'http://www.52north.org/test/procedure/%';

DELETE FROM offeringAllowedFeatureType AS t
USING   offering AS o
WHERE   o.offeringId = t.offeringId AND 
        o.identifier LIKE 'http://www.52north.org/test/offering/%'; 

DELETE FROM offeringAllowedObservationType AS t
USING   offering AS o
WHERE   o.offeringId = t.offeringId AND 
        o.identifier LIKE 'http://www.52north.org/test/offering/%'; 

DELETE FROM procedure WHERE identifier LIKE 'http://www.52north.org/test/procedure/%';
DELETE FROM offering WHERE identifier LIKE 'http://www.52north.org/test/offering/%';
DELETE FROM featureOfInterest WHERE identifier LIKE 'http://www.52north.org/test/featureOfInterest/%';
DELETE FROM observableProperty WHERE identifier LIKE 'http://www.52north.org/test/observableProperty/%';
DELETE FROM unit WHERE unit LIKE 'test_unit_%';
