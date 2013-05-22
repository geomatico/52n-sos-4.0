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

create table blob_value (observation_id int8 not null, value oid, primary key (observation_id));
create table boolean_value (observation_id int8 not null, value boolean, primary key (observation_id));
create table category_value (observation_id int8 not null, value varchar(255), primary key (observation_id));
create table codespace (codespace_id int8 not null, codespace varchar(255) not null unique, primary key (codespace_id));
create table composite_phenomenon (parent_observable_property_id int8 not null, child_observable_property_id int8 not null, primary key (child_observable_property_id, parent_observable_property_id));
create table count_value (observation_id int8 not null, value int4, primary key (observation_id));
create table feature_of_interest (feature_of_interest_id int8 not null, hibernate_discriminator boolean not null, feature_of_interest_type_id int8 not null, identifier varchar(255), codespace_id int8, names text, geom GEOMETRY, description_xml text, url varchar(255) unique, primary key (feature_of_interest_id));
create table feature_of_interest_type (feature_of_interest_type_id int8 not null, feature_of_interest_type varchar(255) not null unique, primary key (feature_of_interest_type_id));
create table feature_relation (parent_feature_id int8 not null, child_feature_id int8 not null, primary key (child_feature_id, parent_feature_id));
create table geometry_value (observation_id int8 not null, value GEOMETRY, primary key (observation_id));
create table numeric_value (observation_id int8 not null, value numeric(19, 2), primary key (observation_id));
create table observable_property (observable_property_id int8 not null, hibernate_discriminator boolean not null, identifier varchar(255) not null unique, description varchar(255), primary key (observable_property_id));
create table observation (observation_id int8 not null, feature_of_interest_id int8 not null, observable_property_id int8 not null, procedure_id int8 not null, phenomenon_time_start timestamp not null, phenomenon_time_end timestamp not null, result_time timestamp not null, identifier varchar(255) unique, codespace_id int8, deleted boolean default false not null, valid_time_start timestamp, valid_time_end timestamp, unit_id int8, set_id varchar(255), primary key (observation_id), unique (feature_of_interest_id, observable_property_id, procedure_id, phenomenon_time_start, phenomenon_time_end, result_time));
create table observation_constellation (observation_constellation_id int8 not null, observable_property_id int8 not null, procedure_id int8 not null, observation_type_id int8, offering_id int8 not null, deleted boolean default false not null, hidden_child boolean default false not null, primary key (observation_constellation_id), unique (observable_property_id, procedure_id, offering_id));
create table observation_relates_to_offering (observation_id int8 not null, offering_id int8 not null, primary key (observation_id, offering_id));
create table observation_type (observation_type_id int8 not null, observation_type varchar(255) not null unique, primary key (observation_type_id));
create table offering (offering_id int8 not null, hibernate_discriminator boolean not null, identifier varchar(255) not null unique, name varchar(255), primary key (offering_id));
create table offering_has_allowed_feature_of_interest_type (offering_id int8 not null, feature_of_interest_type_id int8 not null, primary key (offering_id, feature_of_interest_type_id));
create table offering_has_allowed_observation_type (offering_id int8 not null, observation_type_id int8 not null, primary key (offering_id, observation_type_id));
create table offering_has_related_feature (related_feature_id int8 not null, offering_id int8 not null, primary key (offering_id, related_feature_id));
create table procedure (procedure_id int8 not null, hibernate_discriminator boolean not null, procedure_description_format_id int8 not null, identifier varchar(255) not null unique, deleted boolean default false not null, description_file varchar(255), primary key (procedure_id));
create table procedure_description_format (procedure_description_format_id int8 not null, procedure_description_format varchar(255) not null, primary key (procedure_description_format_id));
create table related_feature (related_feature_id int8 not null, feature_of_interest_id int8 not null, primary key (related_feature_id));
create table related_feature_has_related_feature_role (related_feature_id int8 not null, related_feature_role_id int8 not null, primary key (related_feature_id, related_feature_role_id));
create table related_feature_role (related_feature_role_id int8 not null, related_feature_role varchar(255) not null unique, primary key (related_feature_role_id));
create table result_template (result_template_id int8 not null, offering_id int8 not null, observable_property_id int8 not null, procedure_id int8 not null, feature_of_interest_id int8 not null, identifier varchar(255) not null, result_structure text not null, result_encoding text not null, primary key (result_template_id));
create table sensor_system (parent_sensor_id int8 not null, child_sensor_id int8 not null, primary key (child_sensor_id, parent_sensor_id));
create table text_value (observation_id int8 not null, value text, primary key (observation_id));
create table unit (unit_id int8 not null, unit varchar(255) not null unique, primary key (unit_id));
create table valid_procedure_time (valid_procedure_time_id int8 not null, procedure_id int8 not null, start_time timestamp not null, end_time timestamp, description_xml text not null, primary key (valid_procedure_time_id));
alter table blob_value add constraint observation_blob_value_fk foreign key (observation_id) references observation;
alter table boolean_value add constraint observation_boolean_value_fk foreign key (observation_id) references observation;
alter table category_value add constraint observation_category_value_fk foreign key (observation_id) references observation;
alter table composite_phenomenon add constraint observable_property_child_fk foreign key (child_observable_property_id) references observable_property;
alter table composite_phenomenon add constraint observable_property_parent_fk foreign key (parent_observable_property_id) references observable_property;
alter table count_value add constraint observation_count_value_fk foreign key (observation_id) references observation;
create index feature_of_interest_identifier_idx on feature_of_interest (identifier);
create index feature_of_interest_geom_idx on feature_of_interest (geom);
alter table feature_of_interest add constraint feature_of_interest_feature_of_interest_type_fk foreign key (feature_of_interest_type_id) references feature_of_interest_type;
alter table feature_of_interest add constraint feature_of_interest_codespace_fk foreign key (codespace_id) references codespace;
alter table feature_relation add constraint FK1372A525899FED20 foreign key (child_feature_id) references feature_of_interest;
alter table feature_relation add constraint feature_of_interest_child_fk foreign key (child_feature_id) references observable_property;
alter table feature_relation add constraint feature_of_interest_parent_fk foreign key (parent_feature_id) references observable_property;
alter table feature_relation add constraint FK1372A5256408B4D2 foreign key (parent_feature_id) references feature_of_interest;
alter table geometry_value add constraint observation_geometry_value_fk foreign key (observation_id) references observation;
alter table numeric_value add constraint observation_numeric_value_fk foreign key (observation_id) references observation;
create index observable_property_identifier_idx on observable_property (identifier);
create index observation_set_idx on observation (set_id);
create index observation_procedure_idx on observation (procedure_id);
create index observation_codespace_idx on observation (codespace_id);
create index observation_phenomenon_time_start_idx on observation (phenomenon_time_start);
create index observation_observable_property_idx on observation (observable_property_id);
create index observation_identifier_idx on observation (identifier);
create index observation_feature_of_interest_idx on observation (feature_of_interest_id);
create index observation_result_time_idx on observation (result_time);
create index observation_phenomenon_time_end_idx on observation (phenomenon_time_end);
alter table observation add constraint observation_procedure_fk foreign key (procedure_id) references procedure;
alter table observation add constraint observation_observable_property_fk foreign key (observable_property_id) references observable_property;
alter table observation add constraint observation_feature_of_interest_fk foreign key (feature_of_interest_id) references feature_of_interest;
alter table observation add constraint observation_codespace_fk foreign key (codespace_id) references codespace;
alter table observation add constraint observation_unit_flk foreign key (unit_id) references unit;
create index observation_constellation_offering_idx on observation_constellation (offering_id);
create index observation_constellation_procedure_idx on observation_constellation (procedure_id);
create index observation_constellation_observable_property_idx on observation_constellation (observable_property_id);
alter table observation_constellation add constraint observation_constellation_procedure_fk foreign key (procedure_id) references procedure;
alter table observation_constellation add constraint observation_constellation_offering_fk foreign key (offering_id) references offering;
alter table observation_constellation add constraint observation_constellation_observable_property_fk foreign key (observable_property_id) references observable_property;
alter table observation_constellation add constraint observation_constellation_observation_type_fk foreign key (observation_type_id) references observation_type;
alter table observation_relates_to_offering add constraint observation_offering_fk foreign key (offering_id) references offering;
alter table observation_relates_to_offering add constraint FK5EE85832DF9AE384 foreign key (observation_id) references observation;
create index offering_identifier_idx on offering (identifier);
alter table offering_has_allowed_feature_of_interest_type add constraint FKB7E41D45163B1858 foreign key (offering_id) references offering;
alter table offering_has_allowed_feature_of_interest_type add constraint offering_feature_of_interest_type_fk foreign key (feature_of_interest_type_id) references feature_of_interest_type;
alter table offering_has_allowed_observation_type add constraint FK410694C2163B1858 foreign key (offering_id) references offering;
alter table offering_has_allowed_observation_type add constraint offering_observation_type_fk foreign key (observation_type_id) references observation_type;
alter table offering_has_related_feature add constraint offering_related_feature_fk foreign key (related_feature_id) references related_feature;
alter table offering_has_related_feature add constraint related_feature_offering_fk foreign key (offering_id) references offering;
create index procedrue_identifier_idx on procedure (identifier);
alter table procedure add constraint procedure_procedure_description_format_fk foreign key (procedure_description_format_id) references procedure_description_format;
alter table related_feature add constraint related_feature_feature_of_interest_fk foreign key (feature_of_interest_id) references feature_of_interest;
alter table related_feature_has_related_feature_role add constraint FK583932F523B29E59 foreign key (related_feature_id) references related_feature;
alter table related_feature_has_related_feature_role add constraint related_feature_related_feature_role_fk foreign key (related_feature_role_id) references related_feature_role;
create index result_template_observable_property_idx on result_template (observable_property_id);
create index result_template_offering_idx on result_template (offering_id);
create index result_template_procedure_idx on result_template (procedure_id);
create index result_template_identifier_idx on result_template (identifier);
alter table result_template add constraint result_template_procedure_fk foreign key (procedure_id) references procedure;
alter table result_template add constraint result_template_observable_property_fk foreign key (observable_property_id) references observable_property;
alter table result_template add constraint result_template_offering_idx foreign key (offering_id) references offering;
alter table result_template add constraint result_template_feature_of_interest_idx foreign key (feature_of_interest_id) references feature_of_interest;
alter table sensor_system add constraint procedure_child_fk foreign key (child_sensor_id) references procedure;
alter table sensor_system add constraint procedure_parenf_fk foreign key (parent_sensor_id) references procedure;
alter table text_value add constraint observation_text_value_fk foreign key (observation_id) references observation;
create index valid_procedure_time_start_time_idx on valid_procedure_time (start_time);
create index valid_procedure_time_end_time_idx on valid_procedure_time (end_time);
create index valid_procedure_time_procedure_idx on valid_procedure_time (procedure_id);
alter table valid_procedure_time add constraint FK228EBB5C2A06BC3C foreign key (procedure_id) references procedure;
alter table valid_procedure_time add constraint valid_procedure_time_procedure_fk foreign key (procedure_id) references procedure;
create sequence codespace_id_seq;
create sequence feature_of_interest_id_seq;
create sequence feature_of_interest_type_id_seq;
create sequence observable_property_id_seq;
create sequence observation_constellation_id_seq;
create sequence observation_id_seq;
create sequence observation_type_id_seq;
create sequence offering_id_seq;
create sequence procedure_description_format_id_seq;
create sequence procedure_id_seq;
create sequence related_feature_id_seq;
create sequence related_feature_role_id_seq;
create sequence result_template_id_seq;
create sequence unit_id_seq;
create sequence valid_procedure_time_id_seq;