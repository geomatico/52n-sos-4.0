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

DROP TABLE IF EXISTS result_type CASCADE;
DROP TABLE IF EXISTS codespace CASCADE;
DROP TABLE IF EXISTS spatial_filtering_profile CASCADE;
DROP TABLE IF EXISTS sensor_system CASCADE;
DROP TABLE IF EXISTS offering CASCADE;
DROP TABLE IF EXISTS procedure_description_format CASCADE;
DROP TABLE IF EXISTS related_feature CASCADE;
DROP TABLE IF EXISTS observation_type CASCADE;
DROP TABLE IF EXISTS swe_type CASCADE;
DROP TABLE IF EXISTS composite_phenomenon CASCADE;
DROP TABLE IF EXISTS feature_of_interest_type CASCADE;
DROP TABLE IF EXISTS geometry_value CASCADE;
DROP TABLE IF EXISTS text_value CASCADE;
DROP TABLE IF EXISTS category_value CASCADE;
DROP TABLE IF EXISTS unit CASCADE;
DROP TABLE IF EXISTS numeric_value CASCADE;
DROP TABLE IF EXISTS blob_value CASCADE;
DROP TABLE IF EXISTS request CASCADE;
DROP TABLE IF EXISTS procedure CASCADE;
DROP TABLE IF EXISTS feature_relation CASCADE;
DROP TABLE IF EXISTS feature_of_interest CASCADE;
DROP TABLE IF EXISTS observable_property CASCADE;
DROP TABLE IF EXISTS offering_has_allowed_observation_type CASCADE;
DROP TABLE IF EXISTS offering_has_allowed_feature_of_interest_type CASCADE;
DROP TABLE IF EXISTS quality CASCADE;
DROP TABLE IF EXISTS offering_has_related_feature CASCADE;
DROP TABLE IF EXISTS observation_template CASCADE;
DROP TABLE IF EXISTS request_has_composite_phenomenon CASCADE;
DROP TABLE IF EXISTS request_has_observable_property CASCADE;
DROP TABLE IF EXISTS observation_constellation CASCADE;
DROP TABLE IF EXISTS valid_procedure_time CASCADE;
DROP TABLE IF EXISTS observation_relates_to_offering CASCADE;
DROP TABLE IF EXISTS composite_phenomenon_has_observable_property CASCADE;
DROP TABLE IF EXISTS observation CASCADE;
DROP TABLE IF EXISTS observation_has_text_value CASCADE;
DROP TABLE IF EXISTS observation_has_category_value CASCADE;
DROP TABLE IF EXISTS result_template CASCADE;
DROP TABLE IF EXISTS observation_has_numeric_value CASCADE;
DROP TABLE IF EXISTS observation_has_count_value CASCADE;
DROP TABLE IF EXISTS observation_has_boolean_value CASCADE;
DROP TABLE IF EXISTS observation_has_blob_value CASCADE;
DROP TABLE IF EXISTS observation_has_quality CASCADE;
DROP TABLE IF EXISTS observation_has_spatial_filtering_profile CASCADE;
DROP TABLE IF EXISTS observation_has_geometry_value CASCADE;
DROP TABLE IF EXISTS related_feature_has_related_feature_role CASCADE;
DROP TABLE IF EXISTS related_feature_role CASCADE;

-- drop sequences
DROP SEQUENCE IF EXISTS blob_value_id_seq;
DROP SEQUENCE IF EXISTS codespace_id_seq;
DROP SEQUENCE IF EXISTS category_value_id_seq;
DROP SEQUENCE IF EXISTS composite_phenomenon_id_seq;
DROP SEQUENCE IF EXISTS feature_of_interest_id_seq;
DROP SEQUENCE IF EXISTS feature_of_interest_type_id_seq;
DROP SEQUENCE IF EXISTS geometry_value_id_seq;
DROP SEQUENCE IF EXISTS numeric_value_id_seq;
DROP SEQUENCE IF EXISTS observable_property_id_seq;
DROP SEQUENCE IF EXISTS observation_id_seq;
DROP SEQUENCE IF EXISTS observation_constellation_id_seq;
DROP SEQUENCE IF EXISTS observation_template_id_seq;
DROP SEQUENCE IF EXISTS observation_type_id_seq;
DROP SEQUENCE IF EXISTS offering_id_seq;
DROP SEQUENCE IF EXISTS procedure_description_format_id_seq;
DROP SEQUENCE IF EXISTS procedure_id_seq;
DROP SEQUENCE IF EXISTS quality_id_seq;
DROP SEQUENCE IF EXISTS related_feature_id_seq;
DROP SEQUENCE IF EXISTS related_feature_role_id_seq;
DROP SEQUENCE IF EXISTS request_id_seq;
DROP SEQUENCE IF EXISTS result_template_id_seq;
DROP SEQUENCE IF EXISTS result_type_id_seq;
DROP SEQUENCE IF EXISTS spatial_filtering_profile_id_seq;
DROP SEQUENCE IF EXISTS swe_type_id_seq;
DROP SEQUENCE IF EXISTS text_value_id_seq;
DROP SEQUENCE IF EXISTS unit_id_seq;
DROP SEQUENCE IF EXISTS valid_procedure_time_id_seq;
