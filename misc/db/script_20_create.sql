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

create table blobValue (observationId int8 not null, value oid, primary key (observationId));
create table booleanValue (observationId int8 not null, value char(1), primary key (observationId), check (value in ('T','F')));
create table categoryValue (observationId int8 not null, value varchar(255), primary key (observationId));
create table codespace (codespaceId int8 not null, codespace varchar(255) not null unique, primary key (codespaceId));
create table compositePhenomenon (parentObservablePropertyId int8 not null, childObservablePropertyId int8 not null, primary key (childObservablePropertyId, parentObservablePropertyId));
create table countValue (observationId int8 not null, value int4, primary key (observationId));
create table featureOfInterest (featureOfInterestId int8 not null, hibernateDiscriminator char(1) not null, featureOfInterestTypeId int8 not null, identifier varchar(255), codespaceId int8, names text, geom GEOMETRY, descriptionXml text, url varchar(255) unique, primary key (featureOfInterestId));
create table featureOfInterestType (featureOfInterestTypeId int8 not null, featureOfInterestType varchar(255) not null unique, primary key (featureOfInterestTypeId));
create table featureRelation (parentFeatureId int8 not null, childFeatureId int8 not null, primary key (childFeatureId, parentFeatureId));
create table geometryValue (observationId int8 not null, value GEOMETRY, primary key (observationId));
create table numericValue (observationId int8 not null, value numeric(19, 2), primary key (observationId));
create table observableProperty (observablePropertyId int8 not null, hibernateDiscriminator char(1) not null, identifier varchar(255) not null unique, description varchar(255), primary key (observablePropertyId));
create table observation (observationId int8 not null, featureOfInterestId int8 not null, observablePropertyId int8 not null, procedureId int8 not null, phenomenonTimeStart timestamp not null, phenomenonTimeEnd timestamp not null, resultTime timestamp not null, identifier varchar(255) unique, codespaceId int8, deleted char(1) default 'F' not null check (deleted in ('T','F')), validTimeStart timestamp, validTimeEnd timestamp, unitId int8, setId varchar(255), primary key (observationId), unique (featureOfInterestId, observablePropertyId, procedureId, phenomenonTimeStart, phenomenonTimeEnd, resultTime));
create table observationConstellation (observationConstellationId int8 not null, observablePropertyId int8 not null, procedureId int8 not null, observationTypeId int8, offeringId int8 not null, deleted char(1) default 'F' not null check (deleted in ('T','F')), hiddenChild char(1) default 'F' not null check (hiddenChild in ('T','F')), primary key (observationConstellationId), unique (observablePropertyId, procedureId, offeringId));
create table observationHasOffering (observationId int8 not null, offeringId int8 not null, primary key (observationId, offeringId));
create table observationType (observationTypeId int8 not null, observationType varchar(255) not null unique, primary key (observationTypeId));
create table offering (offeringId int8 not null, hibernateDiscriminator char(1) not null, identifier varchar(255) not null unique, name varchar(255), primary key (offeringId));
create table offeringAllowedFeatureType (offeringId int8 not null, featureOfInterestTypeId int8 not null, primary key (offeringId, featureOfInterestTypeId));
create table offeringAllowedObservationType (offeringId int8 not null, observationTypeId int8 not null, primary key (offeringId, observationTypeId));
create table offeringHasRelatedFeature (relatedFeatureId int8 not null, offeringId int8 not null, primary key (offeringId, relatedFeatureId));
create table procedure (procedureId int8 not null, hibernateDiscriminator char(1) not null, procedureDescriptionFormatId int8 not null, identifier varchar(255) not null unique, deleted char(1) default 'F' not null check (deleted in ('T','F')), descriptionFile text, primary key (procedureId));
create table procedureDescriptionFormat (procedureDescriptionFormatId int8 not null, procedureDescriptionFormat varchar(255) not null, primary key (procedureDescriptionFormatId));
create table relatedFeature (relatedFeatureId int8 not null, featureOfInterestId int8 not null, primary key (relatedFeatureId));
create table relatedFeatureHasRole (relatedFeatureId int8 not null, relatedFeatureRoleId int8 not null, primary key (relatedFeatureId, relatedFeatureRoleId));
create table relatedFeatureRole (relatedFeatureRoleId int8 not null, relatedFeatureRole varchar(255) not null unique, primary key (relatedFeatureRoleId));
create table resultTemplate (resultTemplateId int8 not null, offeringId int8 not null, observablePropertyId int8 not null, procedureId int8 not null, featureOfInterestId int8 not null, identifier varchar(255) not null, resultStructure text not null, resultEncoding text not null, primary key (resultTemplateId));
create table sensorSystem (parentSensorId int8 not null, childSensorId int8 not null, primary key (childSensorId, parentSensorId));
create table textValue (observationId int8 not null, value text, primary key (observationId));
create table unit (unitId int8 not null, unit varchar(255) not null unique, primary key (unitId));
create table validProcedureTime (validProcedureTimeId int8 not null, procedureId int8 not null, startTime timestamp not null, endTime timestamp, descriptionXml text not null, primary key (validProcedureTimeId));
alter table blobValue add constraint observationBlobValueFk foreign key (observationId) references observation;
alter table booleanValue add constraint observationBooleanValueFk foreign key (observationId) references observation;
alter table categoryValue add constraint observationCategoryValueFk foreign key (observationId) references observation;
alter table compositePhenomenon add constraint observablePropertyChildFk foreign key (childObservablePropertyId) references observableProperty;
alter table compositePhenomenon add constraint observablePropertyParentFk foreign key (parentObservablePropertyId) references observableProperty;
alter table countValue add constraint observationCountValueFk foreign key (observationId) references observation;
create index featureOfInterestIdentifierIdx on featureOfInterest (identifier);
alter table featureOfInterest add constraint featureFeatureTypeFk foreign key (featureOfInterestTypeId) references featureOfInterestType;
alter table featureOfInterest add constraint featureCodespaceFk foreign key (codespaceId) references codespace;
alter table featureRelation add constraint featureOfInterest_parentFk foreign key (parentFeatureId) references observableProperty;
alter table featureRelation add constraint FKFD198312780C440 foreign key (parentFeatureId) references featureOfInterest;
alter table featureRelation add constraint FKFD198312DA85B80E foreign key (childFeatureId) references featureOfInterest;
alter table featureRelation add constraint featureOfInterestChildFk foreign key (childFeatureId) references observableProperty;
alter table geometryValue add constraint observationGeometryValueFk foreign key (observationId) references observation;
alter table numericValue add constraint observationNumericValueFk foreign key (observationId) references observation;
create index obsResultTimeIdx on observation (resultTime);
create index obsPhenTimeEndIdx on observation (phenomenonTimeEnd);
create index obsPhenTimeStartIdx on observation (phenomenonTimeStart);
create index observationSetIdx on observation (setId);
create index obsCodespaceIdx on observation (codespaceId);
create index obsObsPropIdx on observation (observablePropertyId);
create index observationIdentifierIdx on observation (identifier);
create index obsFeatureIdx on observation (featureOfInterestId);
create index obsProcedureIdx on observation (procedureId);
alter table observation add constraint observationUnitFk foreign key (unitId) references unit;
alter table observation add constraint observationProcedureFk foreign key (procedureId) references procedure;
alter table observation add constraint observationObPropFk foreign key (observablePropertyId) references observableProperty;
alter table observation add constraint observationFeatureFk foreign key (featureOfInterestId) references featureOfInterest;
alter table observation add constraint observationCodespaceFk foreign key (codespaceId) references codespace;
create index obsConstOfferingIdx on observationConstellation (offeringId);
create index obsConstProcedureIdx on observationConstellation (procedureId);
create index obsConstObsPropIdx on observationConstellation (observablePropertyId);
alter table observationConstellation add constraint obsConstObservationIypeFk foreign key (observationTypeId) references observationType;
alter table observationConstellation add constraint obsnConstProcedureFk foreign key (procedureId) references procedure;
alter table observationConstellation add constraint obsConstObsPropFk foreign key (observablePropertyId) references observableProperty;
alter table observationConstellation add constraint obsConstOfferingFk foreign key (offeringId) references offering;
alter table observationHasOffering add constraint observationOfferingFk foreign key (offeringId) references offering;
alter table observationHasOffering add constraint FK7D7608F4A0D4D3BD foreign key (observationId) references observation;
alter table offeringAllowedFeatureType add constraint offeringFeatureTypeFk foreign key (featureOfInterestTypeId) references featureOfInterestType;
alter table offeringAllowedFeatureType add constraint FKF68CB72EE4EF3005 foreign key (offeringId) references offering;
alter table offeringAllowedObservationType add constraint offeringObservationTypeFk foreign key (observationTypeId) references observationType;
alter table offeringAllowedObservationType add constraint FK28E66A64E4EF3005 foreign key (offeringId) references offering;
alter table offeringHasRelatedFeature add constraint offeringRelatedFeatureFk foreign key (relatedFeatureId) references relatedFeature;
alter table offeringHasRelatedFeature add constraint relatedFeatureOfferingFk foreign key (offeringId) references offering;
alter table procedure add constraint procProcDescFormatFk foreign key (procedureDescriptionFormatId) references procedureDescriptionFormat;
alter table relatedFeature add constraint relatedFeatureFeatureFk foreign key (featureOfInterestId) references featureOfInterest;
alter table relatedFeatureHasRole add constraint FK5643E7654A79987 foreign key (relatedFeatureId) references relatedFeature;
alter table relatedFeatureHasRole add constraint relatedFeatRelatedFeatRoleFk foreign key (relatedFeatureRoleId) references relatedFeatureRole;
create index resultTempOfferingIdx on resultTemplate (offeringId);
create index resultTempeObsPropIdx on resultTemplate (observablePropertyId);
create index resultTempIdentifierIdx on resultTemplate (identifier);
create index resultTempProcedureIdx on resultTemplate (procedureId);
alter table resultTemplate add constraint resultTemplateProcedureFk foreign key (procedureId) references procedure;
alter table resultTemplate add constraint resultTemplateObsPropFk foreign key (observablePropertyId) references observableProperty;
alter table resultTemplate add constraint resultTemplateOfferingIdx foreign key (offeringId) references offering;
alter table resultTemplate add constraint resultTemplateFeatureIdx foreign key (featureOfInterestId) references featureOfInterest;
alter table sensorSystem add constraint procedureParenfFk foreign key (parentSensorId) references procedure;
alter table sensorSystem add constraint procedureChildFk foreign key (childSensorId) references procedure;
alter table textValue add constraint observationTextValueFk foreign key (observationId) references observation;
create index validProcedureTimeEndTimeIdx on validProcedureTime (endTime);
create index validProcedureTimeStartTimeIdx on validProcedureTime (startTime);
alter table validProcedureTime add constraint FK70D221A4BAE84963 foreign key (procedureId) references procedure;
alter table validProcedureTime add constraint validProcedureTimeProcedureFk foreign key (procedureId) references procedure;
create sequence codespaceId_seq;
create sequence featureOfInterestId_seq;
create sequence featureOfInterestTypeId_seq;
create sequence observablePropertyId_seq;
create sequence observationConstellationId_seq;
create sequence observationId_seq;
create sequence observationTypeId_seq;
create sequence offeringId_seq;
create sequence procDescFormatId_seq;
create sequence procedureId_seq;
create sequence relatedFeatureId_seq;
create sequence relatedFeatureRoleId_seq;
create sequence resultTemplateId_seq;
create sequence unitId_seq;
create sequence validProcedureTimeId_seq;