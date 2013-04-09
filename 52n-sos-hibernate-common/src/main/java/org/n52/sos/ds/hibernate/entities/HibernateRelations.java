/**
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.sos.ds.hibernate.entities;

import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Interfaces that entities can implement to share constants and to make clear which entities have which relations.
 * Allows to throw compile time errors for non existing relations.
 *
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
public interface HibernateRelations {
    interface HasObservationConstellation {
        String OBSERVATION_CONSTELLATION = "observationConstellation";
        ObservationConstellation getObservationConstellation();
        void setObservationConstellation(ObservationConstellation observationConstellation);
    }

    interface HasObservationConstellations {
        String OBSERVATION_CONSTELLATIONS = "observationConstellations";
        Set<ObservationConstellation> getObservationConstellations();
        void setObservationConstellations(Set<ObservationConstellation> observationConstellations);
    }

    interface HasDescription {
        String DESCRIPTION = "description";
        String getDescription();
        void setDescription(String description);
    }

    interface HasCodespace {
        String CODESPACE = "codespace";
        Codespace getCodespace();
        void setCodespace(Codespace codespace);
    }

    interface HasDeletedFlag {
        String DELETED = "deleted";
        void setDeleted(boolean deleted);
        boolean isDeleted();
    }

    interface HasCompositePhenomenons {
        String COMPOSITE_PHENOMENONS = "compositePhenomenons";
        Set<CompositePhenomenon> getCompositePhenomenons();
        void setCompositePhenomenons(Set<CompositePhenomenon> compositePhenomenons);
    }

    interface HasFeatureOfInterestType {
        String FEATURE_OF_INTEREST_TYPE = FeatureOfInterestType.FEATURE_OF_INTEREST_TYPE;
        FeatureOfInterestType getFeatureOfInterestType();
        void setFeatureOfInterestType(FeatureOfInterestType featureOfInterestType);
    }

    interface HasFeatureOfInterestTypes {
        String FEATURE_OF_INTEREST_TYPES = "featureOfInterestTypes";
        Set<FeatureOfInterestType> getFeatureOfInterestTypes();
        void setFeatureOfInterestTypes(Set<FeatureOfInterestType> featureOfInterestTypes);
    }

    interface HasFeatureOfInterest {
        String FEATURE_OF_INTEREST = "featureOfInterest";
        FeatureOfInterest getFeatureOfInterest();
        void setFeatureOfInterest(FeatureOfInterest featureOfInterest);
    }

    interface HasDescriptionXml {
        String DESCRIPTION_XML = "descriptionXml";
        String getDescriptionXml();
        void setDescriptionXml(String descriptionXml);
    }

    interface HasGeometry {
        String GEOMETRY = "geom";
        Geometry getGeom();
        void setGeom(Geometry geom);
    }

    interface HasIdentifier {
        String IDENTIFIER = "identifier";
        String getIdentifier();
        void setIdentifier(String identifier);
    }

    interface HasName {
        String NAME = "name";
        String getName();
        void setName(String name);
    }

    interface HasObservableProperty {
        String OBSERVABLE_PROPERTY = "observableProperty";
        ObservableProperty getObservableProperty();
        void setObservableProperty(ObservableProperty observableProperty);
    }

    interface HasObservationTemplates {
        String OBSERVATION_TEMPLATES = "observationTemplates";
        Set<ObservationTemplate> getObservationTemplates();
        void setObservationTemplates(Set<ObservationTemplate> observationTemplates);
    }

    interface HasObservationType {
        String OBSERVATION_TYPE = ObservationType.OBSERVATION_TYPE;
        ObservationType getObservationType();
        void setObservationType(ObservationType observationType);
    }

    interface HasObservationTypes {
        String OBSERVATION_TYPES = "observationTypes";
        Set<ObservationType> getObservationTypes();
        void setObservationTypes(Set<ObservationType> observationTypes);
    }

    interface HasOffering {
        String OFFERING = "offering";
        void setOffering(Offering offering);
        Offering getOffering();
    }

    interface HasProcedure {
        String PROCEDURE = "procedure";
        Procedure getProcedure();
        void setProcedure(Procedure procedure);
    }

    interface HasProcedureDescriptionFormat {
        String PROCEDURE_DESCRIPTION_FORMAT = ProcedureDescriptionFormat.PROCEDURE_DESCRIPTION_FORMAT;
        ProcedureDescriptionFormat getProcedureDescriptionFormat();
        void setProcedureDescriptionFormat(ProcedureDescriptionFormat procedureDescriptionFormat);
    }

    interface HasRelatedFeatureRoles {
        String RELATED_FEATURE_ROLES = "relatedFeatureRoles";
        Set<RelatedFeatureRole> getRelatedFeatureRoles();
        void setRelatedFeatureRoles(Set<RelatedFeatureRole> relatedFeatureRoles);
    }

    interface HasRelatedFeatures {
        String RELATED_FEATURES = "relatedFeatures";
        Set<RelatedFeature> getRelatedFeatures();
        void setRelatedFeatures(Set<RelatedFeature> relatedFeatures);
    }

    interface HasRequests {
        String REQUESTS = "requests";
        Set<Request> getRequests();
        void setRequests(Set<Request> requests);
    }

    interface HasResultEncoding {
        String RESULT_ENCODING = "resultEncoding";
        String getResultEncoding();
        void setResultEncoding(String resultEncoding);
    }

    interface HasResultStructure {
        String RESULT_STRUCTURE = "resultStructure";
        String getResultStructure();
        void setResultStructure(String resultStructure);
    }

    interface HasSweType {
        String SWE_TYPE = SweType.SWE_TYPE;
        SweType getSweType();
        void setSweType(SweType sweType);
    }

    interface HasUnit {
        String UNIT = Unit.UNIT;
        Unit getUnit();
        void setUnit(Unit unit);
    }

    interface HasUrl {
        String URL = "url";
        String getUrl();
        void setUrl(String url);
    }

    interface HasValue<T> {
        String VALUE = "value";
        T getValue();
        void setValue(T value);
    }

    interface HasObservationConstellationOfferingObservationType {
        String OBSERVATION_CONSTELLATION_OFFERING_OBSERVATION_TYPE = "observationConstellationOfferingObservationType";
        ObservationConstellationOfferingObservationType getObservationConstellationOfferingObservationType();
        void setObservationConstellationOfferingObservationType(
                ObservationConstellationOfferingObservationType observationConstellationOfferingObservationType);
    }

    interface HasObservationConstellationOfferingObservationTypes {
        String OBSERVATION_CONSTELLATION_OFFERING_OBSERVATION_TYPES = "observationConstellationOfferingObservationTypes";
        Set<ObservationConstellationOfferingObservationType> getObservationConstellationOfferingObservationTypes();
        void setObservationConstellationOfferingObservationTypes(
                Set<ObservationConstellationOfferingObservationType> observationConstellationOfferingObservationTypes);
    }

    interface HasOfferings {
        String OFFERINGS = "offerings";
        Set<Offering> getOfferings();
        void setOfferings(Set<Offering> offerings);
    }

    interface HasObservableProperties {
        String OBSERVABLE_PROPERTIES = "observableProperties";
        Set<ObservableProperty> getObservableProperties();
        void setObservableProperties(Set<ObservableProperty> observableProperties);
    }

    public interface GeoColumnsId {
        String COORD_DIMENSION = "coordDimension";
        String SRID = "srid";
        String TABLE_CATALOG = "FTableCatalog";
        String TABLE_NAME = "FTableName";
        String TABLE_SCHEMA = "FTableSchema";
        String TYPE = "type";
        Integer getCoordDimension();
        void setCoordDimension(Integer coordDimension);
        String getFTableCatalog();
        void setFTableCatalog(String FTableCatalog);
        String getFTableName();
        void setFTableName(String FTableName);
        String getFTableSchema();
        void setFTableSchema(String FTableSchema);
        Integer getSrid();
        void setSrid(Integer srid);
        String getType();
        void setType(String type);
    }

    interface HasSrid {
        String SRID = "srid";
        int getSrid();
        void setSrid(int srid);
    }

    interface HasCoordinate extends HasSrid {
        String LONGITUDE = "longitude";
        String LATITUDE = "latitude";
        String ALTITUDE = "altitude";
        Object getLongitude();
        void setLongitude(Object longitude);
        Object getLatitude();
        void setLatitude(Object latitude);
        Object getAltitude();
        void setAltitude(Object altitude);
    }

    interface HasQualities {
        String QUALITIES = "qualities";
        Set<Quality> getQualities();
        void setQualities(Set<Quality> qualities);
    }

    interface HasSpatialFilteringProfiles {
        String SPATIAL_FILTERING_PROFILES = "spatialFilteringProfiles";
        Set<SpatialFilteringProfile> getSpatialFilteringProfiles();
        void setSpatialFilteringProfiles(Set<SpatialFilteringProfile> spatialFilteringProfiles);
    }

    interface HasValidProcedureTimes {
        String VALID_PROCEDURE_TIMES = "validProcedureTimes";
        Set<ValidProcedureTime> getValidProcedureTimes();
        void setValidProcedureTimes(Set<ValidProcedureTime> validProcedureTimes);
    }
}
