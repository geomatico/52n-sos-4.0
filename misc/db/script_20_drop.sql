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

drop table if exists blob_value cascade;
drop table if exists boolean_value cascade;
drop table if exists category_value cascade;
drop table if exists codespace cascade;
drop table if exists composite_phenomenon cascade;
drop table if exists count_value cascade;
drop table if exists feature_of_interest cascade;
drop table if exists feature_of_interest_type cascade;
drop table if exists feature_relation cascade;
drop table if exists geometry_value cascade;
drop table if exists numeric_value cascade;
drop table if exists observable_property cascade;
drop table if exists observation cascade;
drop table if exists observation_constellation cascade;
drop table if exists observation_relates_to_offering cascade;
drop table if exists observation_type cascade;
drop table if exists offering cascade;
drop table if exists offering_has_allowed_feature_of_interest_type cascade;
drop table if exists offering_has_allowed_observation_type cascade;
drop table if exists offering_has_related_feature cascade;
drop table if exists procedure cascade;
drop table if exists procedure_description_format cascade;
drop table if exists related_feature cascade;
drop table if exists related_feature_has_related_feature_role cascade;
drop table if exists related_feature_role cascade;
drop table if exists result_template cascade;
drop table if exists sensor_system cascade;
drop table if exists text_value cascade;
drop table if exists unit cascade;
drop table if exists valid_procedure_time cascade;
drop sequence codespace_id_seq;
drop sequence feature_of_interest_id_seq;
drop sequence feature_of_interest_type_id_seq;
drop sequence observable_property_id_seq;
drop sequence observation_constellation_id_seq;
drop sequence observation_id_seq;
drop sequence observation_type_id_seq;
drop sequence offering_id_seq;
drop sequence procedure_description_format_id_seq;
drop sequence procedure_id_seq;
drop sequence related_feature_id_seq;
drop sequence related_feature_role_id_seq;
drop sequence result_template_id_seq;
drop sequence unit_id_seq;
drop sequence valid_procedure_time_id_seq;