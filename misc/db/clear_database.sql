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

TRUNCATE 
 codespace,
 composite_phenomenon,
 feature_of_interest,
 feature_of_interest_type,
 feature_relation,
 observable_property,
 observation,
 observation_constellation,
 blob_value,
 boolean_value,
 category_value,
 count_value,
 geometry_value,
 numeric_value,
 text_value,
 observation_relates_to_offering,
 observation_type,
 offering,
 offering_has_allowed_feature_of_interest_type,
 offering_has_allowed_observation_type,
 offering_has_related_feature,
 procedure,
 procedure_description_format,
 related_feature,
 related_feature_has_related_feature_role,
 related_feature_role,
 result_template,
 sensor_system,
 unit,
 valid_procedure_time
RESTART IDENTITY CASCADE;