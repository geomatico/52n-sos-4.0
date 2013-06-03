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
package org.n52.sos.ds.hibernate.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
import org.n52.sos.ds.hibernate.entities.TFeatureOfInterest;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CollectionHelper;

/**
 * Hibernate data access class for featureOfInterest
 * 
 * @author CarstenHollmann
 * @since 4.0.0
 */
public class FeatureOfInterestDAO {

    /**
     * Get featureOfInterest object for identifier
     * 
     * @param identifier
     *            FeatureOfInterest identifier
     * @param session
     *            Hibernate session Hibernate session
     * @return
     */
    public FeatureOfInterest getFeatureOfInterest(String identifier, Session session) {
        return (FeatureOfInterest) session.createCriteria(FeatureOfInterest.class)
                .add(Restrictions.eq(FeatureOfInterest.IDENTIFIER, identifier)).uniqueResult();
    }

    /**
     * Get featureOfInterest identifiers for observation constellation
     * 
     * @param observationConstellation
     *            Observation constellation
     * @param session
     *            Hibernate session Hibernate session
     * @return FeatureOfInterest identifiers for observation constellation
     */
    @SuppressWarnings("unchecked")
    public List<String> getFeatureOfInterestIdentifiersForObservationConstellation(
            ObservationConstellation observationConstellation, Session session) {
        Criteria criteria =
                session.createCriteria(Observation.class)
                        .add(Restrictions.eq(Observation.DELETED, false))
                        .add(Restrictions.eq(Observation.PROCEDURE, observationConstellation.getProcedure()))
                        .add(Restrictions.eq(Observation.OBSERVABLE_PROPERTY,
                                observationConstellation.getObservableProperty()));
        criteria.createCriteria(Observation.OFFERINGS).add(
                Restrictions.eq(Offering.ID, observationConstellation.getOffering().getOfferingId()));
        criteria.createCriteria(Observation.FEATURE_OF_INTEREST).setProjection(
                Projections.distinct(Projections.property(FeatureOfInterest.IDENTIFIER)));
        return criteria.list();
    }

    /**
     * Get featureOfInterest identifiers for an offering identifier
     * 
     * @param offeringIdentifiers
     *            Offering identifier
     * @param session
     *            Hibernate session Hibernate session
     * @return FeatureOfInterest identifiers for offering
     */
    @SuppressWarnings("unchecked")
    public List<String> getFeatureOfInterestIdentifiersForOffering(String offeringIdentifiers, Session session) {
        Criteria c = session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false));
        c.createCriteria(Observation.FEATURE_OF_INTEREST).setProjection(
                Projections.distinct(Projections.property(FeatureOfInterest.IDENTIFIER)));
        c.createCriteria(Observation.OFFERINGS).add(Restrictions.eq(Offering.IDENTIFIER, offeringIdentifiers));
        return c.list();
    }

    /**
     * Get featureOfInterest objects for featureOfInterest identifiers
     * 
     * @param identifiers
     *            FeatureOfInterest identifiers
     * @param session
     *            Hibernate session
     * @return FeatureOfInterest objects
     */
    @SuppressWarnings("unchecked")
    public List<FeatureOfInterest> getFeatureOfInterestObject(Collection<String> identifiers, Session session) {
        if (identifiers != null && !identifiers.isEmpty()) {
            return session.createCriteria(FeatureOfInterest.class)
                    .add(Restrictions.in(FeatureOfInterest.IDENTIFIER, identifiers)).list();
        }
        return Collections.emptyList();
    }

    /**
     * Get all featureOfInterest objects
     * 
     * @param session
     *            Hibernate session
     * @return FeatureOfInterest objects
     */
    @SuppressWarnings("unchecked")
    public List<FeatureOfInterest> getFeatureOfInterestObjects(Session session) {
        return session.createCriteria(FeatureOfInterest.class).list();
    }

    /**
     * Insert and/or get featureOfInterest object for identifier
     * 
     * @param identifier
     *            FeatureOfInterest identifier
     * @param url
     *            FeatureOfInterest URL, if defined as link
     * @param session
     *            Hibernate session
     * @return FeatureOfInterest object
     */
    public FeatureOfInterest getOrInsertFeatureOfInterest(String identifier, String url, Session session) {
        FeatureOfInterest feature = new FeatureOfInterestDAO().getFeatureOfInterest(identifier, session);
        if (feature == null) {
            feature = new FeatureOfInterest();
            feature.setIdentifier(identifier);
            if (url != null && !url.isEmpty()) {
                feature.setUrl(url);
            }
            session.save(feature);
            session.flush();
        } else if (feature.getUrl() != null && !feature.getUrl().isEmpty() && url != null && !url.isEmpty()) {
            feature.setUrl(url);
            session.saveOrUpdate(feature);
            session.flush();
        }
        return feature;
    }

    /**
     * Insert featureOfInterest relationship
     * 
     * @param parentFeature
     *            Parent featureOfInterest
     * @param childFeature
     *            Child featureOfInterest
     * @param session
     *            Hibernate session
     */
    public void insertFeatureOfInterestRelationShip(TFeatureOfInterest parentFeature, FeatureOfInterest childFeature,
            Session session) {
        parentFeature.getChilds().add(childFeature);
        session.saveOrUpdate(parentFeature);
        session.flush();
    }

    /**
     * Insert featureOfInterest/related feature relations if relatedFeatures
     * exists for offering.
     * 
     * @param featureOfInterest
     *            FeatureOfInerest
     * @param offering
     *            Offering
     * @param session
     *            Hibernate session
     */
    public void checkOrInsertFeatureOfInterestRelatedFeatureRelation(FeatureOfInterest featureOfInterest,
            Offering offering, Session session) {
        List<RelatedFeature> relatedFeatures =
                new RelatedFeatureDAO().getRelatedFeatureForOffering(offering.getIdentifier(), session);
        if (CollectionHelper.isNotEmpty(relatedFeatures)) {
            for (RelatedFeature relatedFeature : relatedFeatures) {
                insertFeatureOfInterestRelationShip((TFeatureOfInterest) relatedFeature.getFeatureOfInterest(),
                        featureOfInterest, session);
            }
        }
    }

    /**
     * Insert featureOfInterest if it is supported
     * 
     * @param featureOfInterest
     *            SOS featureOfInterest to insert
     * @param session
     *            Hibernate session
     * @return FeatureOfInterest object
     * @throws OwsExceptionReport
     *             If SOS feature type is not supported
     */
    public FeatureOfInterest checkOrInsertFeatureOfInterest(SosAbstractFeature featureOfInterest, Session session)
            throws OwsExceptionReport {
        if (featureOfInterest instanceof SosSamplingFeature) {
            String featureIdentifier =
                    Configurator.getInstance().getFeatureQueryHandler()
                            .insertFeature((SosSamplingFeature) featureOfInterest, session);
            return getOrInsertFeatureOfInterest(featureIdentifier, ((SosSamplingFeature) featureOfInterest).getUrl(),
                    session);
        } else {
            // TODO: throw exception
            throw new NoApplicableCodeException();
        }
    }

}
