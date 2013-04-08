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
package org.n52.sos.ds.hibernate.cache;

import static org.hibernate.criterion.Restrictions.eq;
import static org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities.*;
import static org.n52.sos.util.CacheHelper.*;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.joda.time.DateTime;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.SosFeatureCollection;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Updates the cache after a Observation was deleted. Uses the deleted observation to determine which cache relations
 * have to be updated.
 * <p/>
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
public class ObservationDeletionCacheUpdate extends CacheUpdate {
    private static final Logger log = LoggerFactory.getLogger(ObservationDeletionCacheUpdate.class);
    /**
     * Maximal difference between double values to consider them "equal".
     */
    private static final double EPSILON = .000001;
    private static final String OFFERING = HibernateConstants.PARAMETER_OFFERING;
    private static final String IDENTIFIER = HibernateConstants.PARAMETER_IDENTIFIER;
    private static final String PROCEDURE = HibernateConstants.PARAMETER_PROCEDURE;
    private static final String OBSERVATION_CONSTELLATION = HibernateConstants.PARAMETER_OBSERVATION_CONSTELLATION;
    private static final String FEATURE_OF_INTEREST = HibernateConstants.PARAMETER_FEATURE_OF_INTEREST;
    private static final String OBSERVATION_CONSTELLATION_OFFERING_OBSERVATION_TYPE =
                                HibernateConstants.PARAMETER_OBSERVATION_CONSTELLATION_OFFERING_OBSERVATION_TYPE;
    private static final String DELETED = HibernateConstants.PARAMETER_DELETED;
    /**
     * Boolean to keep track if we already updated the global bounding box.
     */
    private boolean globalSpatialBoundingBoxUpdated = false;
    /**
     * Set of offering identifiers to keep track for which offerings we already updated the spatial bounding box.
     */
    private Set<String> updatedOfferingBoundingBoxes = new HashSet<String>(0);
    /**
     * The deleted observation.
     */
    private SosObservation o;

    public ObservationDeletionCacheUpdate(SosObservation deletedObservation) {
        this.o = deletedObservation;
    }

    @Override
    public void execute() {
        try {
            updateFeatureOfInterest();
            updateIdentifiers();
            updateTemporalBoundingBoxes();
            updateSpatialBoundingBoxes();
        } catch (OwsExceptionReport ex) {
            getErrors().add(ex);
        }
    }

    /**
     * Removes the observation identifier from the cache (if it exists).
     */
    protected void updateIdentifiers() {
        final String procedure = o.getObservationConstellation().getProcedure().getIdentifier();
        final String identifier = o.getIdentifier() == null ? null : o.getIdentifier().getValue();
        if (identifier != null) {
            getCache().removeObservationIdentifier(identifier);
            getCache().removeObservationIdentifierForProcedure(procedure, identifier);
        }
    }

    /**
     * Update the global and offering specific temporal bounding boxes. The updates are conditional: the database is
     * only queried if the observation bounding boxes touch the cached bounding boxes.
     */
    protected void updateTemporalBoundingBoxes() {
        DateTime minPhenomenonTime = null;
        DateTime maxPhenomenonTime = null;
        DateTime resultTime = null;
        if (o.getPhenomenonTime() != null) {
            if (o.getPhenomenonTime() instanceof TimeInstant) {
                minPhenomenonTime = maxPhenomenonTime = ((TimeInstant) o.getPhenomenonTime()).getValue();
            } else {
                minPhenomenonTime = ((TimePeriod) o.getPhenomenonTime()).getStart();
                maxPhenomenonTime = ((TimePeriod) o.getPhenomenonTime()).getEnd();
            }
            DateTime cachedMin = getCache().getMinPhenomenonTime();
            if (cachedMin != null && cachedMin.equals(minPhenomenonTime)) {
                log.debug("Updating global minimal phenomenon time");
                getCache().setMinPhenomenonTime(getMinPhenomenonTime(getSession()));
            }
            DateTime cachedMax = getCache().getMaxPhenomenonTime();
            if (cachedMax != null && cachedMax.equals(maxPhenomenonTime)) {
                log.debug("Updating global maximal phenomenon time");
                getCache().setMaxPhenomenonTime(getMaxPhenomenonTime(getSession()));
            }
        }

        if (o.getResultTime() != null) {
            resultTime = o.getResultTime().getValue();
            DateTime cachedMin = getCache().getMinResultTime();
            if (cachedMin != null && cachedMin.equals(resultTime)) {
                log.debug("Updating global minimal result time");
                getCache().setMinResultTime(getMinResultTime(getSession()));
            }
            DateTime cachedMax = getCache().getMaxResultTime();
            if (cachedMax != null && cachedMax.equals(resultTime)) {
                log.debug("Updating global maximal result time");
                getCache().setMaxResultTime(getMaxResultTime(getSession()));
            }
        }

        for (String offering : o.getObservationConstellation().getOfferings()) {
            DateTime minPhenomenonTimeForOffering = getCache().getMinPhenomenonTimeForOffering(offering);
            final String dsOffering = removePrefixAndGetOfferingIdentifier(offering);
            if (minPhenomenonTimeForOffering != null && minPhenomenonTimeForOffering.equals(minPhenomenonTime)) {
                log.debug("Updating minimal phenomenon time for offering {}", offering);
                getCache().setMinPhenomenonTimeForOffering(offering, getMinDate4Offering(dsOffering, getSession()));
            }
            DateTime maxPhenomenonTimeForOffering = getCache().getMaxPhenomenonTimeForOffering(offering);
            if (maxPhenomenonTimeForOffering != null && maxPhenomenonTimeForOffering.equals(maxPhenomenonTime)) {
                log.debug("Updating maximal phenomenon time for offering {}", offering);
                getCache().setMaxPhenomenonTimeForOffering(offering, getMaxDate4Offering(dsOffering, getSession()));
            }
            DateTime minResultTimeForOffering = getCache().getMinResultTimeForOffering(offering);
            if (minResultTimeForOffering != null && minResultTimeForOffering.equals(resultTime)) {
                log.debug("Updating minimal result time for offering {}", offering);
                getCache().setMinResultTimeForOffering(offering, getMinResultTime4Offering(dsOffering, getSession()));
            }
            DateTime maxResultTimeForOffering = getCache().getMaxResultTimeForOffering(offering);
            if (maxResultTimeForOffering != null && maxResultTimeForOffering.equals(resultTime)) {
                log.debug("Updating maximal result time for offering {}", offering);
                getCache().setMaxResultTimeForOffering(offering, getMaxResultTime4Offering(dsOffering, getSession()));
            }
        }
    }

    /**
     * Update the spatial bounding boxes for the deleted observation.
     * <p/>
     * This method will use the cache for dbFeature identifiers. These have to be updated beforehand.
     *
     * @throws OwsExceptionReport if the dbFeature of interest is not supported or the FeatureQueryHandler fails.
     */
    private void updateSpatialBoundingBoxes() throws OwsExceptionReport {
        updateSpatialBoundingBoxes(o.getObservationConstellation().getFeatureOfInterest());
    }

    /**
     * Update the global and offering specific spatial bounding box for the specified dbFeature of interest. The update
     * is conditionally executed if the envelope of the dbFeature and the cached envelope share a edge. The method will
     * recursively check containing features if {@code featureOfInterest} is a {@link SosFeatureCollection} while
     * keeping track which offerings are already updated.
     * <p/>
     * This method will use the cache for dbFeature identifiers. These have to be updated beforehand.
     *
     * @param featureOfInterest the dbFeature to check
     *
     * @throws OwsExceptionReport if the FeatureQueryHandler fails
     */
    private void updateSpatialBoundingBoxes(SosAbstractFeature featureOfInterest) throws OwsExceptionReport {
        if (featureOfInterest instanceof SosSamplingFeature) {
            final SosSamplingFeature ssf = (SosSamplingFeature) featureOfInterest;
            if (ssf.getGeometry() != null) {
                if (!globalSpatialBoundingBoxUpdated &&
                    getCache().getGlobalEnvelope() != null &&
                    isCritical(ssf.getGeometry().getEnvelopeInternal(),
                               getCache().getGlobalEnvelope().getEnvelope())) {
                    log.debug("Updating global spatial bounding box");
                    globalSpatialBoundingBoxUpdated = true;
                    getCache().setGlobalEnvelope(getEnvelope(getCache().getFeaturesOfInterest()));
                }
                for (String offering : o.getObservationConstellation().getOfferings()) {
                    if (!updatedOfferingBoundingBoxes.contains(offering) &&
                        getCache().getEnvelopeForOffering(offering) != null &&
                        getCache().getEnvelopeForOffering(offering).getEnvelope() != null &&
                        isCritical(ssf.getGeometry().getEnvelopeInternal(),
                                   getCache().getEnvelopeForOffering(offering).getEnvelope())) {
                        log.debug("Updating spatial bounding box for offering {}", offering);
                        updatedOfferingBoundingBoxes.add(offering);
                        getCache().setEnvelopeForOffering(offering, getEnvelope(getCache()
                                .getFeaturesOfInterestForOffering(offering)));
                    }
                }
            }
        } else if (featureOfInterest instanceof SosFeatureCollection) {
            final SosFeatureCollection sfc = (SosFeatureCollection) featureOfInterest;
            for (SosAbstractFeature saf : sfc.getMembers().values()) {
                updateSpatialBoundingBoxes(saf);
            }
        } else {
            throw new NoApplicableCodeException().withMessage("Unsupported feature type: %s", featureOfInterest);
        }
    }

    /**
     * Check if the two envelopes have common edges. If the geometry represented by {@code e1} (or {@code e2}) is
     * removed from a collection of geometries represented by {@code e2} (or {@code e1}), {@code e2} (or {@code e2}) has
     * to be updated.
      *
     * @param e1 the first envelope
     * @param e2 the second envelope
     *
     * @return {@code true} if the envelopes have to be updated
     */
    protected boolean isCritical(Envelope e1, Envelope e2) {
        return e1 != null &&
               e2 != null && (e1.getMaxX() - e2.getMaxX() < EPSILON ||
                              e1.getMinX() - e2.getMinX() < EPSILON ||
                              e1.getMaxY() - e2.getMaxY() < EPSILON ||
                              e1.getMinY() - e2.getMinY() < EPSILON);
    }

    /**
     * Translates the dbFeature identifiers to database dbFeature identifiers and queries the FeatureQueryHandler for
     * the envelope.
     *
     * @param features the dbFeature identifiers
     *
     * @return the envelope for the identifiers
     *
     * @throws OwsExceptionReport if the FeatureQueryHandler fails
     */
    protected SosEnvelope getEnvelope(Set<String> features) throws OwsExceptionReport {
        final Set<String> dbFeatures = new HashSet<String>(features.size());
        for (String feature : features) {
            dbFeatures.add(removePrefixAndGetFeatureIdentifier(feature));
        }
        return getFeatureQueryHandler().getEnvelopeForFeatureIDs(dbFeatures, getSession());
    }

    /**
     * Disassociates the feature of interest from the procedure and offerings if there are no observations left.
     */
    private void updateFeatureOfInterest() {
        final String feature = o.getObservationConstellation().getFeatureOfInterest().getIdentifier().getValue();
        final String procedure = o.getObservationConstellation().getProcedure().getIdentifier();
        final String dbFeature = removePrefixAndGetFeatureIdentifier(feature);
        final String dbProcedure = removePrefixAndGetProcedureIdentifier(procedure);

        if (isLastForProcedure(dbFeature, dbProcedure)) {
            getCache().removeProcedureForFeatureOfInterest(feature, procedure);
        }

        for (String offering : o.getObservationConstellation().getOfferings()) {
            final String dbOffering = removePrefixAndGetOfferingIdentifier(offering);
            if (isLastForOffering(dbFeature, dbOffering)) {
                getCache().removeFeatureOfInterestForOffering(offering, feature);
            }
        }
    }

    /**
     * Check if there is no observation with the specified dbProcedure/feature combination.
     *
     * @param feature   the feature identifier
     * @param procedure the procedure identifier
     *
     * @return if there is no observation with the specified dbFeature and dbProcedure.
     */
    protected boolean isLastForProcedure(String feature, String procedure) {
        Criteria oc = getSession().createCriteria(Observation.class)
                .add(eq(DELETED, false));
        oc.createCriteria(FEATURE_OF_INTEREST)
                .add(eq(IDENTIFIER, feature));
        oc.createCriteria(OBSERVATION_CONSTELLATION)
                .createCriteria(PROCEDURE)
                .add(eq(IDENTIFIER, procedure));
        return isEmpty(oc);
    }

    /**
     * Checks if there is no observation with the specified offering/feature combination.
     *
     * @param feature  the feature identifier
     * @param offering the offering identifier
     *
     * @return if there is no observation with the specified dbFeature and offering
     */
    protected boolean isLastForOffering(String feature, String offering) {
        Criteria oc = getSession().createCriteria(Observation.class)
                .add(eq(DELETED, false));
        oc.createCriteria(FEATURE_OF_INTEREST)
                .add(eq(IDENTIFIER, feature));
        oc.createCriteria(OBSERVATION_CONSTELLATION)
                .createCriteria(OBSERVATION_CONSTELLATION_OFFERING_OBSERVATION_TYPE)
                .createCriteria(OFFERING)
                .add(eq(IDENTIFIER, offering));
        return isEmpty(oc);
    }

    /**
     * Checks if the specified query has no results.
     *
     * @param q the query
     *
     * @return if it has no results
     */
    protected boolean isEmpty(Criteria q) {
        return ((Number) q.setProjection(Projections.rowCount()).uniqueResult()).longValue() == 0L;
    }
}
