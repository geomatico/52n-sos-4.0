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

DELETE FROM text_value WHERE value LIKE 'test_text%';

DELETE FROM category_value WHERE value LIKE 'test_category%';

DELETE FROM numeric_value AS ohnv
USING   observation AS o, 
        feature_of_interest AS f,
        procedure AS p,
        unit AS u,
        observable_property AS op
WHERE   ohnv.observation_id = o.observation_id AND
        f.feature_of_interest_id = o.feature_of_interest_id AND
        u.unit_id = o.unit_id AND
        p.procedure_id = o.procedure_id AND
        o.observable_property_id = op.observable_property_id AND
        (
            p.identifier LIKE 'http://www.52north.org/test/procedure/%' OR
            f.identifier LIKE 'http://www.52north.org/test/featureOfInterest/%' OR
            op.identifier LIKE 'http://www.52north.org/test/observableProperty/%' OR
            u.unit LIKE 'test_unit_%'
            
        );
    
DELETE FROM boolean_value AS ohbv
USING   observation AS o, 
        feature_of_interest AS f,
        procedure AS p,
        unit AS u,
        observable_property AS op
WHERE   ohbv.observation_id = o.observation_id AND
        f.feature_of_interest_id = o.feature_of_interest_id AND
        u.unit_id = o.unit_id AND
        p.procedure_id = o.procedure_id AND
        o.observable_property_id = op.observable_property_id AND
        (
            p.identifier LIKE 'http://www.52north.org/test/procedure/%' OR
            f.identifier LIKE 'http://www.52north.org/test/featureOfInterest/%' OR
            op.identifier LIKE 'http://www.52north.org/test/observableProperty/%' OR
            u.unit LIKE 'test_unit_%'
            
        );

DELETE FROM blob_value AS ohbv
USING   observation AS o, 
        feature_of_interest AS f,
        procedure AS p,
        unit AS u,
        observable_property AS op
WHERE   ohbv.observation_id = o.observation_id AND
        f.feature_of_interest_id = o.feature_of_interest_id AND
        u.unit_id = o.unit_id AND
        p.procedure_id = o.procedure_id AND
        o.observable_property_id = op.observable_property_id AND
        (
            p.identifier LIKE 'http://www.52north.org/test/procedure/%' OR
            f.identifier LIKE 'http://www.52north.org/test/featureOfInterest/%' OR
            op.identifier LIKE 'http://www.52north.org/test/observableProperty/%' OR
            u.unit LIKE 'test_unit_%'
            
        );

DELETE FROM count_value AS ohcv
USING   observation AS o, 
        feature_of_interest AS f,
        procedure AS p,
        unit AS u,
        observable_property AS op
WHERE   ohcv.observation_id = o.observation_id AND
        f.feature_of_interest_id = o.feature_of_interest_id AND
        u.unit_id = o.unit_id AND
        p.procedure_id = o.procedure_id AND
        o.observable_property_id = op.observable_property_id AND
        (
            p.identifier LIKE 'http://www.52north.org/test/procedure/%' OR
            f.identifier LIKE 'http://www.52north.org/test/featureOfInterest/%' OR
            op.identifier LIKE 'http://www.52north.org/test/observableProperty/%' OR
            u.unit LIKE 'test_unit_%'
            
        );

DELETE FROM observation_relates_to_offering AS ortooff
USING   observation  AS o,
	procedure AS p,
	offering AS off
WHERE ortooff.offering_id = off.offering_id AND
	o.procedure_id = p.procedure_id AND
	(
            p.identifier LIKE 'http://www.52north.org/test/procedure/%' OR
            off.identifier LIKE 'http://www.52north.org/test/offering/%'
        );

DELETE FROM result_template WHERE identifier LIKE 'http://www.52north.org/test/procedure/%';

DELETE FROM observation_constellation AS oc
USING	procedure AS p,
	offering AS off
WHERE
	oc.offering_id = off.offering_id AND
	oc.procedure_id = p.procedure_id AND
	(
            p.identifier LIKE 'http://www.52north.org/test/procedure/%' OR
            off.identifier LIKE 'http://www.52north.org/test/offering/%'
        );

DELETE FROM observation AS o
USING   feature_of_interest AS f,
        procedure AS p,
        unit AS u,
        observable_property AS op
WHERE   f.feature_of_interest_id = o.feature_of_interest_id AND
        u.unit_id = o.unit_id AND
        p.procedure_id = o.procedure_id AND
        o.observable_property_id = op.observable_property_id AND
        (
            p.identifier LIKE 'http://www.52north.org/test/procedure/%' OR
            f.identifier LIKE 'http://www.52north.org/test/featureOfInterest/%' OR
            op.identifier LIKE 'http://www.52north.org/test/observableProperty/%' OR
            u.unit LIKE 'test_unit_%'
            
        ) ;

DELETE FROM observation_constellation AS oc
USING   procedure AS p,
        observable_property AS op
WHERE   p.procedure_id = oc.procedure_id AND
        oc.observable_property_id = op.observable_property_id AND
        (
            p.identifier LIKE 'http://www.52north.org/test/procedure/%' OR
            op.identifier LIKE 'http://www.52north.org/test/observableProperty/%'
        );

DELETE FROM valid_procedure_time AS vt
USING   procedure AS p
WHERE   p.procedure_id = vt.procedure_id AND 
        p.identifier LIKE 'http://www.52north.org/test/procedure/%';

DELETE FROM offering_has_allowed_feature_of_interest_type AS t
USING   offering AS o
WHERE   o.offering_id = t.offering_id AND 
        o.identifier LIKE 'http://www.52north.org/test/offering/%'; 

DELETE FROM offering_has_allowed_observation_type AS t
USING   offering AS o
WHERE   o.offering_id = t.offering_id AND 
        o.identifier LIKE 'http://www.52north.org/test/offering/%'; 

DELETE FROM procedure WHERE identifier LIKE 'http://www.52north.org/test/procedure/%';
DELETE FROM offering WHERE identifier LIKE 'http://www.52north.org/test/offering/%';
DELETE FROM feature_of_interest WHERE identifier LIKE 'http://www.52north.org/test/featureOfInterest/%';
DELETE FROM observable_property WHERE identifier LIKE 'http://www.52north.org/test/observableProperty/%';
DELETE FROM unit WHERE unit LIKE 'test_unit_%';
