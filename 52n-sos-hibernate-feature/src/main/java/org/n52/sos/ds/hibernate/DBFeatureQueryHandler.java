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
package org.n52.sos.ds.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.spatial.criterion.SpatialProjections;
import org.hibernate.spatial.criterion.SpatialRestrictions;
import org.n52.sos.config.annotation.Configurable;
import org.n52.sos.config.annotation.Setting;
import org.n52.sos.ds.IFeatureQueryHandler;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaTransactionalUtilities;
import org.n52.sos.ogc.filter.SpatialFilter;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Range;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.config.ConfigurationException;
import org.n52.sos.util.JTSHelper;
import org.n52.sos.util.JavaHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Feature handler class for features stored in the database
 */
@Configurable
public class DBFeatureQueryHandler implements IFeatureQueryHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBFeatureQueryHandler.class);
    private List<Range> epsgsWithReversedAxisOrder;
    private int defaultEPSG;

    @Override
    public SosAbstractFeature getFeatureByID(String featureID, Object connection, String version, int responeSrid)
            throws OwsExceptionReport {
        Session session = getSessionFromConnection(connection);
        try {
            Criteria q = session.createCriteria(FeatureOfInterest.class)
                    .add(Restrictions.eq(HibernateConstants.PARAMETER_IDENTIFIER, featureID));
            return createSosAbstractFeature((FeatureOfInterest) q.uniqueResult(), version);
        } catch (HibernateException he) {
            String exceptionText = "An error occurs while querying feature data for a featureOfInterest identifier!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        }

    }

    @Override
    public Collection<String> getFeatureIDs(SpatialFilter filter, Object connection) throws OwsExceptionReport {
        Session session = getSessionFromConnection(connection);
        HibernateQueryObject queryObject = new HibernateQueryObject();
        try {
            if (filter != null) {
                queryObject.addCriterion(HibernateCriteriaQueryUtilities
                        .getCriterionForSpatialFilter(HibernateConstants.PARAMETER_GEOMETRY, filter));
            }
            return getFeatureOfInterestIdentifier(queryObject, session);
        } catch (HibernateException he) {
            String exceptionText =
                   "An error occurs while querying feature identifiers for a featureOfInterest identifier!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        }
    }

    @Override
    public Map<String, SosAbstractFeature> getFeatures(Collection<String> featureIDs, List<SpatialFilter> spatialFilters,
                                                       Object connection, String version, int responeSrid) throws
            OwsExceptionReport {
        Session session = getSessionFromConnection(connection);
        HibernateQueryObject queryObject = new HibernateQueryObject();
        try {
            if (featureIDs != null && !featureIDs.isEmpty()) {
                queryObject.addCriterion(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                        HibernateConstants.PARAMETER_IDENTIFIER, featureIDs));
            }
            if (spatialFilters != null && !spatialFilters.isEmpty()) {
                // String foiAlias = HibernateCriteriaQueryUtilities
                // .addFeatureOfInterestAliasToMap(aliases, null);
                // String propertyName = foiAlias + "."
                // + HibernateConstants.PARAMETER_GEOMETRY;
                Disjunction disjunction = Restrictions.disjunction();
                for (SpatialFilter filter : spatialFilters) {
                    disjunction.add(HibernateCriteriaQueryUtilities.getCriterionForSpatialFilter(
                            HibernateConstants.PARAMETER_GEOMETRY, filter));
                }
                queryObject.addCriterion(disjunction);
            }
            if (queryObject.isSetCriterions()) {
                return createSosFeatures(getFeatureOfInterests(queryObject, session), version);
            } else {
                return new HashMap<String, SosAbstractFeature>(0);
            }

        } catch (HibernateException he) {
            String exceptionText = "Error while querying features from data source!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        }
    }

    @Override
    public SosEnvelope getEnvelopeForFeatureIDs(Collection<String> featureIDs, Object connection) throws
            OwsExceptionReport {
        Session session = getSessionFromConnection(connection);
        if (featureIDs != null && !featureIDs.isEmpty()) {
            try {
                Geometry geom = (Geometry) session.createCriteria(FeatureOfInterest.class)
                        .add(Restrictions.in(HibernateConstants.PARAMETER_IDENTIFIER, featureIDs))
                        .setProjection(SpatialProjections.extent(HibernateConstants.PARAMETER_GEOMETRY))
                        .uniqueResult();
                geom = switchCoordinateAxisOrderIfNeeded(geom);
                if (geom != null) {
                    return new SosEnvelope(geom.getEnvelopeInternal(), getDefaultEPSG());
                }
            } catch (HibernateException he) {
                String exceptionText = "Exception thrown while requesting global feature envelope";
                LOGGER.error(exceptionText, he);
                throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
            }
        }
        return null;
    }

    @Override
    public String insertFeature(SosSamplingFeature samplingFeature, Object connection) throws OwsExceptionReport {
        if (samplingFeature.getUrl() != null && !samplingFeature.getUrl().isEmpty()) {
            return samplingFeature.getUrl();
        } else {
            Session session = getSessionFromConnection(connection);
            String featureIdentifier;
            if (samplingFeature.isSetIdentifier()) {
                featureIdentifier = samplingFeature.getIdentifier().getValue();
            } else {
                featureIdentifier = SosConstants.GENERATED_IDENTIFIER_PREFIX
                                    + JavaHelper.generateID(samplingFeature.getXmlDescription());
                samplingFeature.setIdentifier(new CodeWithAuthority(featureIdentifier));
            }
            insertFeatureOfInterest(samplingFeature, session);
            return featureIdentifier;
        }
    }

    /**
     * Creates a map with FOI identifier and SOS feature
     * <p/>
     * @param features
     * FeatureOfInterest objects
     * @param version
     * SOS version
     * <p/>
     * @return Map with FOI identifier and SOS feature
     * <p/>
     * @throws OwsExceptionReport
     * If feature type is not supported
     */
    protected Map<String, SosAbstractFeature> createSosFeatures(List<FeatureOfInterest> features,
                                                                String version) throws OwsExceptionReport {
        Map<String, SosAbstractFeature> sosAbstractFois = new HashMap<String, SosAbstractFeature>();
        for (FeatureOfInterest feature : features) {
            SosAbstractFeature sosFeature = createSosAbstractFeature(feature, version);
            sosAbstractFois.put(feature.getIdentifier(), sosFeature);
        }
        // TODO if sampledFeatures are also in sosAbstractFois, reference them.
        return sosAbstractFois;
    }

    /**
     * Checks if connection is a Hibernate Session and casts the connection
     * <p/>
     * @param connection
     * connection
     * <p/>
     * @return Hibernate Session
     * <p/>
     * @throws OwsExceptionReport
     * If connection is not a Hibentat Sessrion
     */
    protected Session getSessionFromConnection(Object connection) throws OwsExceptionReport {
        if (connection instanceof Session) {
            return (Session) connection;
        } else {
            String exceptionText = "The connection is not a Hibernate Session!";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
    }

    /**
     * Get FeatureOfInterest objects for the defined restrictions
     * <p/>
     * @param aliases
     * Aliases for query between tables
     * @param criterions
     * Restriction for the query
     * @param projections
     * Projections for the query
     * @param session
     * Hibernate session
     * <p/>
     * @return FeatureOfInterest objects
     */
    @SuppressWarnings("unchecked")
    protected List<FeatureOfInterest> getFeatureOfInterests(HibernateQueryObject queryObject, Session session) {
        return (List<FeatureOfInterest>) HibernateCriteriaQueryUtilities.getObjectList(queryObject, session,
                                                                                       FeatureOfInterest.class);
    }

    protected FeatureOfInterest getFeatureOfInterest(String identifier, Geometry geometry, Session session) throws OwsExceptionReport {
        Criteria q = session.createCriteria(FeatureOfInterest.class)
                .add(Restrictions.disjunction()
                    .add(Restrictions.eq(HibernateConstants.PARAMETER_IDENTIFIER, identifier))
                    .add(SpatialRestrictions.eq(HibernateConstants.PARAMETER_GEOMETRY, switchCoordinateAxisOrderIfNeeded(geometry))));
        return (FeatureOfInterest) q.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    protected List<String> getFeatureOfInterestIdentifier(HibernateQueryObject queryObject, Session session) {
        queryObject.addProjection(HibernateCriteriaQueryUtilities.getDistinctProjection(HibernateCriteriaQueryUtilities
                .getIdentifierParameter(null)));
        return (List<String>) HibernateCriteriaQueryUtilities.getObjectList(queryObject, session,
                                                                            FeatureOfInterest.class);
    }

     /**
     * Creates a SOS feature from the FeatureOfInterest object
     *
     * @param feature
     * FeatureOfInterest object
     * @param version
     * SOS version
     * <p/>
     * @return SOS feature
     * <p/>
     * @throws OwsExceptionReport  
     */
    protected SosAbstractFeature createSosAbstractFeature(FeatureOfInterest feature, String version) throws OwsExceptionReport {
        if (feature == null) {
            return null;
        }
        String checkedFoiID = null;
        if (SosHelper.checkFeatureOfInterestIdentifierForSosV2(feature.getIdentifier(), version)) {
            checkedFoiID = feature.getIdentifier();
        }
        SosSamplingFeature sampFeat = new SosSamplingFeature(new CodeWithAuthority(checkedFoiID));
        if (feature.getName() != null && !feature.getName().isEmpty()) {
            sampFeat.setName(SosHelper.createCodeTypeListFromCSV(feature.getName()));
        }
        sampFeat.setDescription(null);
        processGeometryPostLoad(feature, sampFeat);
        sampFeat.setFeatureType(feature.getFeatureOfInterestType().getFeatureOfInterestType());
        sampFeat.setUrl(feature.getUrl());
        sampFeat.setXmlDescription(feature.getDescriptionXml());
        Set<FeatureOfInterest> parentFeatures = feature.getFeatureOfInterestsForParentFeatureId();
        if (parentFeatures != null && !parentFeatures.isEmpty()) {
            List<SosAbstractFeature> sampledFeatures = new ArrayList<SosAbstractFeature>(parentFeatures.size());
            for (FeatureOfInterest parentFeature : parentFeatures) {
                sampledFeatures.add(createSosAbstractFeature(parentFeature, version));
            }
            sampFeat.setSampledFeatures(sampledFeatures);
        }
        return sampFeat;
    }
    
    protected void insertFeatureOfInterest(SosSamplingFeature samplingFeature, Session session) throws OwsExceptionReport {
        FeatureOfInterest feature = getFeatureOfInterest(samplingFeature.getIdentifier().getValue(), 
                                                         samplingFeature.getGeometry(), session);
        if (feature == null) {
            feature = new FeatureOfInterest();
            if (samplingFeature.isSetIdentifier()) {
                feature.setIdentifier(samplingFeature.getIdentifier().getValue());
            }
            if (samplingFeature.isSetNames()) {
                feature.setName(SosHelper.createCSVFromCodeTypeList(samplingFeature.getName()));
            }
            
            processGeometryPreSave(samplingFeature, feature);
            
            if (samplingFeature.isSetXmlDescription()) {
                feature.setDescriptionXml(samplingFeature.getXmlDescription());
            }
            if (samplingFeature.isSetFeatureType()) {
                feature.setFeatureOfInterestType(HibernateCriteriaTransactionalUtilities
                        .getOrInsertFeatureOfInterestType(samplingFeature.getFeatureType(), session));
            }
            if (samplingFeature.isSetSampledFeatures()) {
                // TODO: create relationship
            }
            session.save(feature);
            session.flush();
        }
    }

    protected void processGeometryPreSave(SosSamplingFeature ssf, FeatureOfInterest f) throws OwsExceptionReport {
        f.setGeom(switchCoordinateAxisOrderIfNeeded(ssf.getGeometry()));
    }

    protected void processGeometryPostLoad(FeatureOfInterest f, SosSamplingFeature ssf) throws OwsExceptionReport {
        ssf.setGeometry(switchCoordinateAxisOrderIfNeeded(f.getGeom()));
    }

    protected Geometry switchCoordinateAxisOrderIfNeeded(Geometry geom) throws OwsExceptionReport {
        if (geom != null && isAxisOrderSwitchRequired(geom.getSRID() == 0 ? getDefaultEPSG() : geom.getSRID())) {
            return JTSHelper.switchCoordinateAxisOrder(geom);
        } else {
            return geom;
        }
    }
    
     /**
     * @param epsgCode
     * <p/>
     * @return boolean indicating if coordinates have to be switched
     */
    protected boolean isAxisOrderSwitchRequired(int epsgCode) {
        for (Range r : epsgsWithReversedAxisOrder) {
            if (r.contains(epsgCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getDefaultEPSG() {
        return this.defaultEPSG;
    }

    @Setting(FeatureQuerySettingsProvider.DEFAULT_EPSG)
    public void setDefaultEpsg(int epsgCode) throws ConfigurationException {
        Validation.greaterZero("Default EPSG Code", epsgCode);
        this.defaultEPSG = epsgCode;
    }

    @Setting(FeatureQuerySettingsProvider.EPSG_CODES_WITH_REVERSED_AXIS_ORDER)
    public void setEpsgCodesWithReversedAxisOrder(String codes) throws ConfigurationException {
        Validation.notNullOrEmpty("EPSG Codes to switch coordinates for", codes);
        String[] splitted = codes.split(";");
        this.epsgsWithReversedAxisOrder = new ArrayList<Range>(splitted.length);
        for (String entry : splitted) {
            String[] splittedEntry = entry.split("-");
            Range r = null;
            if (splittedEntry.length == 1) {
                r = new Range(Integer.parseInt(splittedEntry[0]), Integer.parseInt(splittedEntry[0]));
            } else if (splittedEntry.length == 2) {
                r = new Range(Integer.parseInt(splittedEntry[0]), Integer.parseInt(splittedEntry[1]));
            } else {
                throw new ConfigurationException(String.format("Invalid format of entry in '%s': %s",
                                                               FeatureQuerySettingsProvider.EPSG_CODES_WITH_REVERSED_AXIS_ORDER,
                                                               entry));
            }
            this.epsgsWithReversedAxisOrder.add(r);
        }
    }
}
