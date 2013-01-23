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

DELETE FROM observation_has_text_value AS ohtv USING text_value AS v
WHERE ohtv.text_value_id = v.text_value_id AND v.value LIKE 'test_text%';
DELETE FROM text_value WHERE value LIKE 'test_text%';


DELETE FROM observation_has_category_value AS ohcv USING category_value AS v
WHERE ohcv.category_value_id = v.category_value_id AND v.value LIKE 'test_category%';

DELETE FROM category_value WHERE value LIKE 'test_category%';

DELETE FROM observation_has_numeric_value AS ohnv
USING   observation AS o, 
        observation_constellation  AS oc,
        feature_of_interest AS f,
        procedure AS p,
        unit AS u,
        observable_property AS op,
        offering AS of
WHERE   ohnv.observation_id = o.observation_id AND
        o.observation_constellation_id = oc.observation_constellation_id AND
        f.feature_of_interest_id = o.feature_of_interest_id AND
        u.unit_id = o.unit_id AND
        p.procedure_id = oc.procedure_id AND
        of.offering_id = oc.offering_id AND
        oc.observable_property_id = op.observable_property_id AND
        (
            p.identifier LIKE 'http://www.example.org/sensors/%' OR
            f.identifier LIKE 'test_feature%' OR
            of.identifier LIKE 'test_offering%' OR
            op.identifier LIKE 'test_observable_property%' OR
            u.unit LIKE 'test_unit%'
            
        );
    
DELETE FROM observation_has_boolean_value AS ohbv
USING   observation AS o, 
        observation_constellation  AS oc,
        feature_of_interest AS f,
        procedure AS p,
        unit AS u,
        observable_property AS op,
        offering AS of
WHERE   ohbv.observation_id = o.observation_id AND
        o.observation_constellation_id = oc.observation_constellation_id AND
        f.feature_of_interest_id = o.feature_of_interest_id AND
        u.unit_id = o.unit_id AND
        p.procedure_id = oc.procedure_id AND
        of.offering_id = oc.offering_id AND
        oc.observable_property_id = op.observable_property_id AND
        (
            p.identifier LIKE 'http://www.example.org/sensors/%' OR
            f.identifier LIKE 'test_feature%' OR
            of.identifier LIKE 'test_offering%' OR
            op.identifier LIKE 'test_observable_property%' OR
            u.unit LIKE 'test_unit%'
            
        );

DELETE FROM observation_has_count_value AS ohcv
USING   observation AS o, 
        observation_constellation  AS oc,
        feature_of_interest AS f,
        procedure AS p,
        unit AS u,
        observable_property AS op,
        offering AS of
WHERE   ohcv.observation_id = o.observation_id AND
        o.observation_constellation_id = oc.observation_constellation_id AND
        f.feature_of_interest_id = o.feature_of_interest_id AND
        u.unit_id = o.unit_id AND
        p.procedure_id = oc.procedure_id AND
        of.offering_id = oc.offering_id AND
        oc.observable_property_id = op.observable_property_id AND
        (
            p.identifier LIKE 'http://www.example.org/sensors/%' OR
            f.identifier LIKE 'test_feature%' OR
            of.identifier LIKE 'test_offering%' OR
            op.identifier LIKE 'test_observable_property%' OR
            u.unit LIKE 'test_unit%'
            
        );


DELETE FROM observation AS o
USING   observation_constellation  AS oc,
        feature_of_interest AS f,
        procedure AS p,
        unit AS u,
        observable_property AS op,
        offering AS of
WHERE   o.observation_constellation_id = oc.observation_constellation_id AND
        f.feature_of_interest_id = o.feature_of_interest_id AND
        u.unit_id = o.unit_id AND
        p.procedure_id = oc.procedure_id AND
        of.offering_id = oc.offering_id AND
        oc.observable_property_id = op.observable_property_id AND
        (
            p.identifier LIKE 'http://www.example.org/sensors/%' OR
            f.identifier LIKE 'test_feature%' OR
            of.identifier LIKE 'test_offering%' OR
            op.identifier LIKE 'test_observable_property%' OR
            u.unit LIKE 'test_unit%'
            
        );

DELETE FROM result_template WHERE identifier LIKE 'http://www.example.org/sensors/%';

DELETE FROM observation_constellation AS oc
USING   procedure AS p,
        observable_property AS op,
        offering AS of
WHERE   p.procedure_id = oc.procedure_id AND
        of.offering_id = oc.offering_id AND
        oc.observable_property_id = op.observable_property_id AND
        (
            p.identifier LIKE 'http://www.example.org/sensors/%' OR
            of.identifier LIKE 'test_offering%' OR
            op.identifier LIKE 'test_observable_property%'
        );

DELETE FROM valid_procedure_time AS vt
USING   procedure AS p
WHERE   p.procedure_id = vt.procedure_id AND 
        p.identifier LIKE 'http://www.example.org/sensors/%';

DELETE FROM procedure_has_feature_of_interest_type AS t
USING   procedure AS p
WHERE   p.procedure_id = t.procedure_id AND 
        p.identifier LIKE 'http://www.example.org/sensors/%'; 

DELETE FROM procedure_has_observation_type AS t
USING   procedure AS p
WHERE   p.procedure_id = t.procedure_id AND 
        p.identifier LIKE 'http://www.example.org/sensors/%'; 

DELETE FROM offering_has_allowed_observation_type AS ohawt
USING   offering AS o
WHERE   o.offering_id = ohawt.offering_id AND
        o.identifier LIKE 'test_offering%';

DELETE FROM procedure WHERE identifier LIKE 'http://www.example.org/sensors/%';
DELETE FROM offering WHERE identifier LIKE 'test_offering%';
DELETE FROM feature_of_interest WHERE identifier LIKE 'test_feature%';
DELETE FROM observable_property WHERE identifier LIKE 'test_observable_property%';
