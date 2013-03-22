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

-- codespcae sequence
CREATE SEQUENCE codespace_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;

-- codespace table
CREATE TABLE codespace (
  codespace_id bigint NOT NULL DEFAULT nextval('codespace_id_seq'),
  codespace TEXT NOT NULL,
  UNIQUE (codespace),
  PRIMARY KEY(codespace_id)
);

-- add codespace_id columns
ALTER TABLE feature_of_interest ADD COLUMN codespace_id bigint NULL;
ALTER TABLE observation ADD COLUMN codespace_id bigint NULL;

-- create index
CREATE INDEX feature_of_interest_FKIndex2 ON feature_of_interest(codespace_id);
CREATE INDEX observation_FKIndex3 ON observation(codespace_id);

-- add foeign keys for codespace
ALTER TABLE feature_of_interest ADD FOREIGN KEY (codespace_id) REFERENCES codespace(codespace_id) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE observation ADD FOREIGN KEY (codespace_id) REFERENCES codespace(codespace_id) ON DELETE NO ACTION ON UPDATE NO ACTION;