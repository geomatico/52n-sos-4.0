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

DROP FUNCTION create_sensor_description(text, text, numeric, numeric, numeric);
DROP FUNCTION get_boolean_value(boolean);
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
DROP FUNCTION insert_count_value(int);
DROP FUNCTION insert_feature_of_interest(text, numeric, numeric);
DROP FUNCTION insert_feature_of_interest_type(text);
DROP FUNCTION insert_geometry_value(geometry);
DROP FUNCTION insert_numeric_observation(bigint, numeric);
DROP FUNCTION insert_numeric_value(numeric);
DROP FUNCTION insert_observable_property(text);
DROP FUNCTION insert_observation(bigint,text, text,timestamp);
DROP FUNCTION insert_observation_constellation(bigint,bigint,bigint,bigint);
DROP FUNCTION insert_observation_constellation(text,text,text,text);
DROP FUNCTION insert_observation_type(text);
DROP FUNCTION insert_offering(text);
DROP FUNCTION insert_procedure_description_format(text);
DROP FUNCTION insert_procedure(text,timestamp,text,numeric,numeric,numeric,bigint,bigint);
DROP FUNCTION insert_procedure(text,timestamp,text,numeric,numeric,numeric,text,text);
DROP FUNCTION insert_text_observation(bigint, text);
DROP FUNCTION insert_text_value(text);
DROP FUNCTION insert_unit(text);
DROP FUNCTION insert_result_template(text,text,text,text,text,text);
DROP FUNCTION insert_result_template(bigint,bigint,text,text,text);