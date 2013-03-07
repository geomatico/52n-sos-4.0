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
 blob_value,
 category_value,
 composite_phenomenon,
 composite_phenomenon_has_observable_property,
 feature_of_interest,
 feature_of_interest_type,
 feature_relation,
 geometry_value,
 numeric_value,
 observable_property,
 observation,
 observation_constellation,
 observation_constellation_offering_observation_type,
 observation_has_blob_value,
 observation_has_boolean_value,
 observation_has_category_value,
 observation_has_count_value,
 observation_has_geometry_value,
 observation_has_numeric_value,
 observation_has_quality,
 observation_has_spatial_filtering_profile,
 observation_has_text_value,
 observation_relates_to_obs_const_off_obs_type,
 observation_template,
 observation_type,
 offering,
 offering_has_allowed_feature_of_interest_type,
 offering_has_allowed_observation_type,
 offering_has_related_feature,
 procedure,
 procedure_description_format,
 quality,
 related_feature,
 related_feature_has_related_feature_role,
 related_feature_role,
 request,
 request_has_composite_phenomenon,
 request_has_observable_property,
 result_template,
 sensor_system,
 spatial_filtering_profile,
 swe_type,
 text_value,
 unit,
 valid_procedure_time
RESTART IDENTITY CASCADE;
