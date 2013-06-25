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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.spatial.criterion.SpatialProjections;
import org.n52.sos.config.annotation.Configurable;
import org.n52.sos.config.annotation.Setting;
import org.n52.sos.ds.FeatureQueryHandler;
import org.n52.sos.ds.hibernate.dao.CodespaceDAO;
import org.n52.sos.ds.hibernate.dao.FeatureOfInterestDAO;
import org.n52.sos.ds.hibernate.dao.FeatureOfInterestTypeDAO;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.TFeatureOfInterest;
import org.n52.sos.ds.hibernate.util.SpatialRestrictions;
import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.NotYetSupportedException;
import org.n52.sos.ogc.filter.SpatialFilter;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.om.features.AbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Range;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.util.JTSHelper;
import org.n52.sos.util.JavaHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Feature handler class for features stored in the database
 */
@Configurable
public class HibernateFeatureQueryHandler implements FeatureQueryHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateFeatureQueryHandler.class);
    private static final int EPSG_WGS84_3D = 4979;
    private static final int EPSG_WGS84 = 4326;
    private List<Range> epsgsWithReversedAxisOrder;
    private int defaultEPSG;
    private boolean spatialDatasource;

    @Override
    public AbstractFeature getFeatureByID(final String featureID, final Object connection, final String version, final int responeSrid)
            throws OwsExceptionReport {
        final Session session = HibernateSessionHolder.getSession(connection);
        try {
            final Criteria q = session.createCriteria(FeatureOfInterest.class).add(
                    Restrictions.eq(FeatureOfInterest.IDENTIFIER, featureID));
            return createSosAbstractFeature((FeatureOfInterest) q.uniqueResult(), version);
        } catch (final HibernateException he) {
            throw new NoApplicableCodeException().causedBy(he)
                    .withMessage("An error occurs while querying feature data for a featureOfInterest identifier!");
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<String> getFeatureIDs(final SpatialFilter filter, final Object connection) throws OwsExceptionReport {
        final Session session = HibernateSessionHolder.getSession(connection);
        try {
            if (isSpatialDatasource()) {
                final Criteria c = session.createCriteria(FeatureOfInterest.class)
                        .setProjection(Projections.distinct(Projections.property(FeatureOfInterest.IDENTIFIER)));
                if (filter != null) {
                    c.add(SpatialRestrictions.filter(FeatureOfInterest.GEOMETRY, filter.getOperator(),
                                                     switchCoordinateAxisOrderIfNeeded(filter.getGeometry())));
                }
                return c.list();
            } else {

                final List<String> identifiers = new LinkedList<String>();
                final List<FeatureOfInterest> features = session.createCriteria(FeatureOfInterest.class).list();
                if (filter != null) {
                    final Geometry envelope = getFilterForNonSpatialDatasource(filter);
                    for (final FeatureOfInterest feature : features) {
                        final Geometry geom = getGeomtery(feature);
                        if (geom != null && envelope.contains(geom)) {
                            identifiers.add(feature.getIdentifier());
                        }
                    }
                }
                return identifiers;
            }
        } catch (final HibernateException he) {
            throw new NoApplicableCodeException().causedBy(he)
                    .withMessage("An error occurs while querying feature identifiers for a featureOfInterest identifier!");
        }
    }

    @Override
    public Map<String, AbstractFeature> getFeatures(final Collection<String> featureIDs,
            final List<SpatialFilter> spatialFilters, final Object connection, final String version, final int responeSrid)
            throws OwsExceptionReport {
        final Session session = HibernateSessionHolder.getSession(connection);
        try {
            if (isSpatialDatasource()) {
                return getFeaturesForSpatialDatasource(featureIDs, spatialFilters, session, version);
            } else {
                return getFeaturesForNonSpatialDatasource(featureIDs, spatialFilters, session, version);
            }
        } catch (final HibernateException he) {
            throw new NoApplicableCodeException().causedBy(he)
                    .withMessage("Error while querying features from data source!");
        }
    }

    @Override
    public SosEnvelope getEnvelopeForFeatureIDs(final Collection<String> featureIDs, final Object connection)
            throws OwsExceptionReport {
        final Session session = HibernateSessionHolder.getSession(connection);
        if (featureIDs != null && !featureIDs.isEmpty()) {
            try {
                if (isSpatialDatasource()) {
                    Geometry geom = (Geometry) session.createCriteria(FeatureOfInterest.class)
                            .add(Restrictions.in(FeatureOfInterest.IDENTIFIER, featureIDs))
                            .setProjection(SpatialProjections.extent(FeatureOfInterest.GEOMETRY)).uniqueResult();
                    geom = switchCoordinateAxisOrderIfNeeded(geom);
                    if (geom != null) {
                        return new SosEnvelope(geom.getEnvelopeInternal(), getDefaultEPSG());
                    }
                } else {
                    final Envelope envelope = new Envelope();
                    final List<FeatureOfInterest> featuresOfInterest =
                            new FeatureOfInterestDAO().getFeatureOfInterestObject(featureIDs, session);
                    for (final FeatureOfInterest feature : featuresOfInterest) {
                        try {
                            final Geometry geom = getGeomtery(feature);
                            if (geom != null) {
                                envelope.expandToInclude(geom.getEnvelopeInternal());
                            }
                        } catch (final OwsExceptionReport owse) {
                            LOGGER.warn(
                                    String.format("Error while adding '%s' to envelope!",
                                            feature.getFeatureOfInterestId()), owse);
                        }

                    }
                    if (!envelope.isNull()) {
                        return new SosEnvelope(envelope, getDefaultEPSG());
                    }
                }
            } catch (final HibernateException he) {
                throw new NoApplicableCodeException().causedBy(he)
                        .withMessage("Exception thrown while requesting global feature envelope");
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.n52.sos.ds.FeatureQueryHandler#insertFeature(org.n52.sos.ogc.om.features.samplingFeatures.SamplingFeature, java.lang.Object)
     * 
     * FIXME check semantics of this method in respect to its name and the documentation in the super class
     */
    @Override
    public String insertFeature(final SamplingFeature samplingFeature, final Object connection) throws OwsExceptionReport {
        if (isFeatureReferenced(samplingFeature)) {
        	if (samplingFeature.isSetIdentifier()) {
        		return samplingFeature.getIdentifier().getValue();
        	} else {
        		return samplingFeature.getUrl();
        	}
        } else {
            final Session session = HibernateSessionHolder.getSession(connection);
            String featureIdentifier;
            if (samplingFeature.isSetIdentifier()) {
                featureIdentifier = samplingFeature.getIdentifier().getValue();
            } else {
                featureIdentifier =
                        SosConstants.GENERATED_IDENTIFIER_PREFIX
                                + JavaHelper.generateID(samplingFeature.getXmlDescription());
                samplingFeature.setIdentifier(new CodeWithAuthority(featureIdentifier));
            }
            return insertFeatureOfInterest(samplingFeature, session);
        }
    }

	private boolean isFeatureReferenced(final SamplingFeature samplingFeature)
	{
		return samplingFeature.getUrl() != null && !samplingFeature.getUrl().isEmpty();
	}

    /**
     * Creates a map with FOI identifier and SOS feature
     * <p/>
     * @param features FeatureOfInterest objects
     * @param version  SOS version
     * <p/>
     * @return Map with FOI identifier and SOS feature
     * <p/>
     * @throws OwsExceptionReport * If feature type is not supported
     */
    protected Map<String, AbstractFeature> createSosFeatures(final List<FeatureOfInterest> features, final String version)
            throws OwsExceptionReport {
        final Map<String, AbstractFeature> sosAbstractFois = new HashMap<String, AbstractFeature>();
        for (final FeatureOfInterest feature : features) {
            final AbstractFeature sosFeature = createSosAbstractFeature(feature, version);
            sosAbstractFois.put(feature.getIdentifier(), sosFeature);
        }
        // TODO if sampledFeatures are also in sosAbstractFois, reference them.
        return sosAbstractFois;
    }

    protected FeatureOfInterest getFeatureOfInterest(final String identifier, final Geometry geometry, final Session session) throws
            OwsExceptionReport {
        if (!identifier.startsWith(SosConstants.GENERATED_IDENTIFIER_PREFIX)) {
            return (FeatureOfInterest) session.createCriteria(FeatureOfInterest.class)
                    .add(Restrictions.eq(FeatureOfInterest.IDENTIFIER, identifier))
                    .uniqueResult();
        } else {
            return (FeatureOfInterest) session.createCriteria(FeatureOfInterest.class)
                    .add(SpatialRestrictions.eq(FeatureOfInterest.GEOMETRY, switchCoordinateAxisOrderIfNeeded(geometry)))
                    .uniqueResult();
        }
    }

    /**
     * Creates a SOS feature from the FeatureOfInterest object
     *
     * @param feature FeatureOfInterest object
     * @param version SOS version
     * <p/>
     * @return SOS feature
     * <p/>
     * @throws OwsExceptionReport
     */
    protected AbstractFeature createSosAbstractFeature(final FeatureOfInterest feature, final String version)
            throws OwsExceptionReport {
        if (feature == null) {
            return null;
        }
        String checkedFoiID = null;
        if (SosHelper.checkFeatureOfInterestIdentifierForSosV2(feature.getIdentifier(), version)) {
            checkedFoiID = feature.getIdentifier();
        }
        final CodeWithAuthority identifier = new CodeWithAuthority(checkedFoiID);
        if (feature.isSetCodespace()) {
            identifier.setCodeSpace(feature.getCodespace().getCodespace());
        }
        final SamplingFeature sampFeat = new SamplingFeature(identifier);
        if (feature.isSetNames()) {
            sampFeat.setName(SosHelper.createCodeTypeListFromCSV(feature.getNames()));
        }
        sampFeat.setDescription(null);
        sampFeat.setGeometry(getGeomtery(feature));
        sampFeat.setFeatureType(feature.getFeatureOfInterestType().getFeatureOfInterestType());
        sampFeat.setUrl(feature.getUrl());
        if (feature.isSetDescriptionXml()) {
            sampFeat.setXmlDescription(feature.getDescriptionXml());
        }
        if (feature instanceof TFeatureOfInterest) {
            final Set<FeatureOfInterest> parentFeatures = ((TFeatureOfInterest)feature).getParents();
            if (parentFeatures != null && !parentFeatures.isEmpty()) {
                final List<AbstractFeature> sampledFeatures = new ArrayList<AbstractFeature>(parentFeatures.size());
                for (final FeatureOfInterest parentFeature : parentFeatures) {
                    sampledFeatures.add(createSosAbstractFeature(parentFeature, version));
                }
                sampFeat.setSampledFeatures(sampledFeatures);
            }
        }
        return sampFeat;
    }

    protected String insertFeatureOfInterest(final SamplingFeature samplingFeature, final Session session) throws OwsExceptionReport {
        if (!isSpatialDatasource()) {
            throw new NotYetSupportedException("Insertion of full encoded features for non spatial datasources");
        }
        final String newId = samplingFeature.getIdentifier().getValue();
        FeatureOfInterest feature = getFeatureOfInterest(newId, samplingFeature.getGeometry(), session);
        if (feature == null) {
            feature = new FeatureOfInterest();
            if (samplingFeature.isSetIdentifier()) {
                feature.setIdentifier(newId);
                if (samplingFeature.getIdentifier().isSetCodeSpace()) {
                    feature.setCodespace(new CodespaceDAO().getOrInsertCodespace(samplingFeature
                            .getIdentifier().getCodeSpace(), session));
                }
            }
            if (samplingFeature.isSetNames()) {
                feature.setNames(SosHelper.createCSVFromCodeTypeList(samplingFeature.getName()));
            }

            processGeometryPreSave(samplingFeature, feature);

            if (samplingFeature.isSetXmlDescription()) {
                feature.setDescriptionXml(samplingFeature.getXmlDescription());
            }
            if (samplingFeature.isSetFeatureType()) {
                feature.setFeatureOfInterestType(new FeatureOfInterestTypeDAO()
                        .getOrInsertFeatureOfInterestType(samplingFeature.getFeatureType(), session));
            }
            
            // TODO: create relationship
//            if (samplingFeature.isSetSampledFeatures()) {
//            }
            session.save(feature);
            session.flush();
            return newId;
        } else {
            return feature.getIdentifier();
        }
    }

    protected void processGeometryPreSave(final SamplingFeature ssf, final FeatureOfInterest f) throws OwsExceptionReport {
        f.setGeom(switchCoordinateAxisOrderIfNeeded(ssf.getGeometry()));
    }

    protected Geometry switchCoordinateAxisOrderIfNeeded(final Geometry geom) throws OwsExceptionReport {
        if (geom != null && isAxisOrderSwitchRequired(geom.getSRID() == 0 ? getDefaultEPSG() : geom.getSRID())) {
            return JTSHelper.switchCoordinateAxisOrder(geom);
        } else {
            return geom;
        }
    }

    /**
     * @param epsgCode <p/>
     * @return boolean indicating if coordinates have to be switched
     */
    protected boolean isAxisOrderSwitchRequired(final int epsgCode) {
        for (final Range r : epsgsWithReversedAxisOrder) {
            if (r.contains(epsgCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the geometry from featureOfInterest object. 
     * 
     * @param feature
     * @return geometry
     * @throws OwsExceptionReport
     */
    protected Geometry getGeomtery(final FeatureOfInterest feature) throws OwsExceptionReport {
        if (feature.isSetGeometry()) {
            return switchCoordinateAxisOrderIfNeeded(feature.getGeom());
        } else if (feature.isSetLongLat()) {
            int epsg = getDefaultEPSG();
            if (feature.isSetSrid()) {
                epsg = feature.getSrid();
            }
            final String wktString = getWktString(feature);
            final Geometry geom = JTSHelper.createGeometryFromWKT(wktString, epsg);
            if (feature.isSetAltitude()) {
                geom.getCoordinate().z = getValueAsDouble(feature.getAltitude());
                if (geom.getSRID() == EPSG_WGS84) {
                    geom.setSRID(EPSG_WGS84_3D);
                }
            }
            return switchCoordinateAxisOrderIfNeeded(geom);
        }
        return null;
    }

    protected double getValueAsDouble(final Object value) {
        if (value instanceof String) {
            return Double.valueOf((String) value).doubleValue();
        } else if (value instanceof BigDecimal) {
            final BigDecimal bdValue = (BigDecimal) value;
            return bdValue.doubleValue();
        } else if (value instanceof Double) {
            return ((Double) value).doubleValue();
        }
        return 0;
    }

    protected String getWktString(final FeatureOfInterest feature) {
        final StringBuilder builder = new StringBuilder();
        builder.append("POINT(");
        builder.append(JavaHelper.asString(feature.getLongitude())).append(" ");
        builder.append(JavaHelper.asString(feature.getLatitude())).append(" ");
        builder.append(")");
        return builder.toString();
    }

    protected Geometry getFilterForNonSpatialDatasource(final SpatialFilter filter) throws OwsExceptionReport {
        switch (filter.getOperator()) {
        case BBOX:
            return filter.getGeometry();
        default:
            throw new InvalidParameterValueException(Sos2Constants.GetObservationParams.spatialFilter, filter.getOperator().name());
        }
    }
    
    protected boolean featureIsInFilter(final Geometry geometry, final List<Geometry> envelopes) throws OwsExceptionReport {
        for (final Geometry envelope : envelopes) {
            if (envelope.contains(geometry)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getDefaultEPSG() {
        return defaultEPSG;
    }

    @Setting(FeatureQuerySettingsProvider.DEFAULT_EPSG)
    public void setDefaultEpsg(final int epsgCode) throws ConfigurationException {
        Validation.greaterZero("Default EPSG Code", epsgCode);
        defaultEPSG = epsgCode;
    }

    @Setting(FeatureQuerySettingsProvider.EPSG_CODES_WITH_REVERSED_AXIS_ORDER)
    public void setEpsgCodesWithReversedAxisOrder(final String codes) throws ConfigurationException {
        Validation.notNullOrEmpty("EPSG Codes to switch coordinates for", codes);
        final String[] splitted = codes.split(";");
        epsgsWithReversedAxisOrder = new ArrayList<Range>(splitted.length);
        for (final String entry : splitted) {
            final String[] splittedEntry = entry.split("-");
            Range r = null;
            if (splittedEntry.length == 1) {
                r = new Range(Integer.parseInt(splittedEntry[0]), Integer.parseInt(splittedEntry[0]));
            } else if (splittedEntry.length == 2) {
                r = new Range(Integer.parseInt(splittedEntry[0]), Integer.parseInt(splittedEntry[1]));
            } else {
                throw new ConfigurationException(String.format("Invalid format of entry in '%s': %s",
                        FeatureQuerySettingsProvider.EPSG_CODES_WITH_REVERSED_AXIS_ORDER, entry));
            }
            epsgsWithReversedAxisOrder.add(r);
        }
    }

    private boolean isSpatialDatasource() {
        return spatialDatasource;
    }

    @Setting(FeatureQuerySettingsProvider.SPATIAL_DATASOURCE)
    public void setSpatialDatasource(final boolean spatialDatasource) {
        this.spatialDatasource = spatialDatasource;
    }

    protected Map<String, AbstractFeature> getFeaturesForNonSpatialDatasource(final Collection<String> featureIDs,
                                                                                 final List<SpatialFilter> spatialFilters,
                                                                                 final Session session, final String version)
            throws OwsExceptionReport {
        final Map<String, AbstractFeature> featureMap = new HashMap<String, AbstractFeature>(0);
        List<Geometry> envelopes = null;
        boolean hasSpatialFilter = false;
        if (spatialFilters != null && !spatialFilters.isEmpty()) {
            hasSpatialFilter = true;
            envelopes = new ArrayList<Geometry>(spatialFilters.size());
            for (final SpatialFilter filter : spatialFilters) {
                envelopes.add(getFilterForNonSpatialDatasource(filter));
            }
        }
        final List<FeatureOfInterest> featuresOfInterest = new FeatureOfInterestDAO()
                .getFeatureOfInterestObject(featureIDs, session);
        for (final FeatureOfInterest feature : featuresOfInterest) {
            final SamplingFeature sosAbstractFeature = (SamplingFeature) createSosAbstractFeature(feature, version);
            if (!hasSpatialFilter) {
                featureMap.put(sosAbstractFeature.getIdentifier().getValue(), sosAbstractFeature);
            } else {
                if (featureIsInFilter(sosAbstractFeature.getGeometry(), envelopes)) {
                    featureMap.put(sosAbstractFeature.getIdentifier().getValue(), sosAbstractFeature);
                }
            }
        }
        return featureMap;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, AbstractFeature> getFeaturesForSpatialDatasource(final Collection<String> featureIDs,
                                                                              final List<SpatialFilter> spatialFilters,
                                                                              final Session session,
                                                                              final String version) throws OwsExceptionReport {
        final Criteria c = session.createCriteria(FeatureOfInterest.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        boolean filtered = false;
        if (featureIDs != null && !featureIDs.isEmpty()) {
            c.add(Restrictions.in(FeatureOfInterest.IDENTIFIER, featureIDs));
            filtered = true;
        }
        if (spatialFilters != null && !spatialFilters.isEmpty()) {
            final Disjunction disjunction = Restrictions.disjunction();
            for (final SpatialFilter filter : spatialFilters) {
                disjunction.add(SpatialRestrictions.filter(FeatureOfInterest.GEOMETRY,
                                                           filter.getOperator(),
                                                           switchCoordinateAxisOrderIfNeeded(filter.getGeometry())));
            }
            c.add(disjunction);
            filtered = true;
        }
        if (filtered) {
            return createSosFeatures(c.list(), version);
        } else {
            return Collections.emptyMap();
        }
    }
}
