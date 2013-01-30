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

-- This is script updates the SOS 4.0.0 db model. The old model was use until SVN resision 15... .
-- If the SOS and the db model was installed/created after this SVN revision, it is not necessary to run this script.

-- CREATE NEW TABLES
CREATE TABLE offering_has_allowed_feature_of_interest_type (
  offering_id INTEGER NOT NULL,
  feature_of_interest_type_id INTEGER NOT NULL,
  PRIMARY KEY(offering_id, feature_of_interest_type_id)
);

ALTER TABLE offering_has_allowed_feature_of_interest_type ADD FOREIGN KEY (offering_id) REFERENCES offering(offering_id) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE offering_has_allowed_feature_of_interest_type ADD FOREIGN KEY (feature_of_interest_type_id) REFERENCES feature_of_interest_type(feature_of_interest_type_id) ON DELETE NO ACTION ON UPDATE NO ACTION;

CREATE SEQUENCE observation_constellation_offering_observation_type_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE TABLE observation_constellation_offering_observation_type (
  observation_constellation_offering_observation_type_id bigint NOT NULL DEFAULT nextval('observation_constellation_offering_observation_type_id_seq'),
  observation_constellation_id INTEGER NULL,
  offering_id INTEGER NOT NULL,
  observation_type_id INTEGER NULL,
  UNIQUE (observation_constellation_id,offering_id,observation_type_id),
  PRIMARY KEY(observation_constellation_offering_observation_type_id)
);

ALTER TABLE observation_constellation_offering_observation_type ADD FOREIGN KEY (observation_constellation_id) REFERENCES observation_constellation(observation_constellation_id) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE observation_constellation_offering_observation_type ADD FOREIGN KEY (offering_id) REFERENCES offering(offering_id) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE observation_constellation_offering_observation_type ADD FOREIGN KEY (observation_type_id) REFERENCES observation_type(observation_type_id) ON DELETE NO ACTION ON UPDATE NO ACTION;

CREATE TABLE observation_relates_to_obs_const_off_obs_type (
  observation_id bigint NOT NULL,
  observation_constellation_offering_observation_type_id bigint NOT NULL,
  PRIMARY KEY (observation_id,observation_constellation_offering_observation_type_id)
);

ALTER TABLE observation_relates_to_obs_const_off_obs_type ADD FOREIGN KEY (observation_id) REFERENCES observation(observation_id) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE observation_relates_to_obs_const_off_obs_type ADD FOREIGN KEY (observation_constellation_offering_observation_type_id) REFERENCES observation_constellation_offering_observation_type(observation_constellation_offering_observation_type_id) ON DELETE NO ACTION ON UPDATE NO ACTION;

-- RENAME COLUMN
ALTER TABLE observation RENAME COLUMN anti_subsetting TO set_id;

-- ADD NEW COLUMNS
ALTER TABLE result_template ADD COLUMN observation_constellation_offering_observation_type_id INTEGER NOT NULL DEFAULT -1;

-- CREATE NEW RELATIONS
INSERT INTO offering_has_allowed_feature_of_interest_type (offering_id, feature_of_interest_type_id) 
SELECT oc.offering_id, phfoit.feature_of_interest_type_id FROM observation_constellation AS oc, procedure_has_feature_of_interest_type AS phfoit
WHERE oc.procedure_id = phfoit.procedure_id;

INSERT INTO observation_constellation_offering_observation_type (observation_constellation_id, offering_id, observation_type_id) 
SELECT observation_constellation_id, offering_id, observation_type_id FROM observation_constellation;

INSERT INTO observation_relates_to_obs_const_off_obs_type (observation_constellation_offering_observation_type_id, observation_id) 
SELECT ocoot.observation_constellation_offering_observation_type_id, o.observation_id FROM observation_constellation_offering_observation_type AS ocoot, observation AS o, observation_constellation AS oc
WHERE ocoot.observation_constellation_id = o.observation_constellation_id AND ocoot.offering_id = oc.offering_id AND ocoot.observation_type_id = oc.observation_type_id;

UPDATE result_template SET observation_constellation_offering_observation_type_id = (SELECT ocoot.observation_constellation_offering_observation_type_id 
FROM observation_constellation_offering_observation_type AS ocoot, result_template AS rt, observation_constellation AS oc
WHERE rt.observation_constellation_id = oc.observation_constellation_id AND rt.observation_constellation_id = ocoot.observation_constellation_id AND oc.offering_id = ocoot.offering_id AND oc.observation_type_id = ocoot.observation_type_id);


-- DROP OLD COLUMNS
ALTER TABLE result_template DROP COLUMN IF EXISTS observation_constellation_id CASCADE;
ALTER TABLE observation_constellation DROP COLUMN IF EXISTS offering_id CASCADE;
ALTER TABLE observation_constellation DROP COLUMN IF EXISTS observation_type_id CASCADE;

-- DROP NOT USED TABLES AND SEQUENCES
DROP TABLE IF EXISTS procedure_has_observation_type CASCADE;
DROP TABLE IF EXISTS procedure_has_feature_of_interest_type CASCADE;

-- CREATE INDICES
CREATE INDEX offering_has_allowed_feature_of_interest_type_FKIndex1 ON offering_has_allowed_feature_of_interest_type(offering_id);
CREATE INDEX offering_has_allowed_feature_of_interest_type_FKIndex2 ON offering_has_allowed_feature_of_interest_type(feature_of_interest_type_id);
CREATE INDEX observation_constellation_offering_observation_type_FKIndex ON observation_constellation_offering_observation_type(offering_id);
CREATE INDEX observation_constellation_offering_observation_type_FKIndex1 ON observation_constellation_offering_observation_type(observation_type_id);
CREATE INDEX observation_constellation_offering_observation_type_FKIndex2 ON observation_constellation_offering_observation_type(observation_constellation_id);
CREATE INDEX observation_relates_to_obs_const_off_obs_type_FKindex1 ON observation_relates_to_obs_const_off_obs_type (observation_id );
CREATE INDEX observation_relates_to_obs_const_off_obs_type_FKindex2 ON observation_relates_to_obs_const_off_obs_type (observation_constellation_offering_observation_type_id );