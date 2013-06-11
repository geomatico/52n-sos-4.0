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

alter table blobValue drop constraint observationBlobValueFk;
alter table booleanValue drop constraint observationBooleanValueFk;
alter table categoryValue drop constraint observationCategoryValueFk;
alter table compositePhenomenon drop constraint observablePropertyChildFk;
alter table compositePhenomenon drop constraint observablePropertyParentFk;
alter table countValue drop constraint observationCountValueFk;
alter table featureOfInterest drop constraint featureFeatureTypeFk;
alter table featureOfInterest drop constraint featureCodespaceFk;
alter table featureRelation drop constraint featureOfInterestParentFk;
alter table featureRelation drop constraint featureOfInterestChildFk;
alter table geometryValue drop constraint observationGeometryValueFk;
alter table numericValue drop constraint observationNumericValueFk;
alter table observation drop constraint observationUnitFk;
alter table observation drop constraint observationProcedureFk;
alter table observation drop constraint observationObPropFk;
alter table observation drop constraint observationFeatureFk;
alter table observation drop constraint observationCodespaceFk;
alter table observationConstellation drop constraint obsConstObservationIypeFk;
alter table observationConstellation drop constraint obsnConstProcedureFk;
alter table observationConstellation drop constraint obsConstObsPropFk;
alter table observationConstellation drop constraint obsConstOfferingFk;
alter table observationHasOffering drop constraint observationOfferingFk;
alter table observationHasOffering drop constraint FK7D7608F4A0D4D3BD;
alter table offeringAllowedFeatureType drop constraint offeringFeatureTypeFk;
alter table offeringAllowedFeatureType drop constraint FKF68CB72EE4EF3005;
alter table offeringAllowedObservationType drop constraint offeringObservationTypeFk;
alter table offeringAllowedObservationType drop constraint FK28E66A64E4EF3005;
alter table offeringHasRelatedFeature drop constraint offeringRelatedFeatureFk;
alter table offeringHasRelatedFeature drop constraint relatedFeatureOfferingFk;
alter table procedure drop constraint procProcDescFormatFk;
alter table relatedFeature drop constraint relatedFeatureFeatureFk;
alter table relatedFeatureHasRole drop constraint FK5643E7654A79987;
alter table relatedFeatureHasRole drop constraint relatedFeatRelatedFeatRoleFk;
alter table resultTemplate drop constraint resultTemplateProcedureFk;
alter table resultTemplate drop constraint resultTemplateObsPropFk;
alter table resultTemplate drop constraint resultTemplateOfferingIdx;
alter table resultTemplate drop constraint resultTemplateFeatureIdx;
alter table sensorSystem drop constraint procedureParenfFk;
alter table sensorSystem drop constraint procedureChildFk;
alter table sweDataArrayValue drop constraint observationSweDataArrayValueFk;
alter table textValue drop constraint observationTextValueFk;
alter table validProcedureTime drop constraint FK70D221A4BAE84963;
alter table validProcedureTime drop constraint validProcedureTimeProcedureFk;
drop table if exists blobValue cascade;
drop table if exists booleanValue cascade;
drop table if exists categoryValue cascade;
drop table if exists codespace cascade;
drop table if exists compositePhenomenon cascade;
drop table if exists countValue cascade;
drop table if exists featureOfInterest cascade;
drop table if exists featureOfInterestType cascade;
drop table if exists featureRelation cascade;
drop table if exists geometryValue cascade;
drop table if exists numericValue cascade;
drop table if exists observableProperty cascade;
drop table if exists observation cascade;
drop table if exists observationConstellation cascade;
drop table if exists observationHasOffering cascade;
drop table if exists observationType cascade;
drop table if exists offering cascade;
drop table if exists offeringAllowedFeatureType cascade;
drop table if exists offeringAllowedObservationType cascade;
drop table if exists offeringHasRelatedFeature cascade;
drop table if exists procedure cascade;
drop table if exists procedureDescriptionFormat cascade;
drop table if exists relatedFeature cascade;
drop table if exists relatedFeatureHasRole cascade;
drop table if exists relatedFeatureRole cascade;
drop table if exists resultTemplate cascade;
drop table if exists sensorSystem cascade;
drop table if exists sweDataArrayValue cascade;
drop table if exists textValue cascade;
drop table if exists unit cascade;
drop table if exists validProcedureTime cascade;
drop sequence codespaceId_seq;
drop sequence featureOfInterestId_seq;
drop sequence featureOfInterestTypeId_seq;
drop sequence observablePropertyId_seq;
drop sequence observationConstellationId_seq;
drop sequence observationId_seq;
drop sequence observationTypeId_seq;
drop sequence offeringId_seq;
drop sequence procDescFormatId_seq;
drop sequence procedureId_seq;
drop sequence relatedFeatureId_seq;
drop sequence relatedFeatureRoleId_seq;
drop sequence resultTemplateId_seq;
drop sequence unitId_seq;
drop sequence validProcedureTimeId_seq;