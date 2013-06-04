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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
import org.n52.sos.ds.hibernate.entities.RelatedFeatureRole;
import org.n52.sos.ogc.om.features.AbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;

/**
 * Hibernate data access class for related features
 * 
 * @author CarstenHollmann
 * @since 4.0.0
 */
public class RelatedFeatureDAO {

    /**
     * Get related feature objects for offering identifier
     * 
     * @param offering
     *            Offering identifier
     * @param session
     *            Hibernate session
     * @return Related feature objects
     */
    @SuppressWarnings("unchecked")
    public List<RelatedFeature> getRelatedFeatureForOffering(String offering, Session session) {
        return session.createCriteria(RelatedFeature.class).createCriteria(RelatedFeature.OFFERINGS)
                .add(Restrictions.eq(Offering.IDENTIFIER, offering)).list();
    }

    /**
     * Get all related feature objects
     * 
     * @param session
     *            Hibernate session
     * @return Related feature objects
     */
    @SuppressWarnings("unchecked")
    public List<RelatedFeature> getRelatedFeatureObjects(Session session) {
        return session.createCriteria(RelatedFeature.class).list();
    }

    /**
     * Get related feature objects for target identifier
     * 
     * @param targetIdentifier
     *            Target identifier
     * @param session
     *            Hibernate session
     * @return Related feature objects
     */
    @SuppressWarnings("unchecked")
    public List<RelatedFeature> getRelatedFeatures(String targetIdentifier, Session session) {
        return session.createCriteria(RelatedFeature.class).createCriteria(RelatedFeature.FEATURE_OF_INTEREST)
                .add(Restrictions.eq(FeatureOfInterest.IDENTIFIER, targetIdentifier)).list();
    }

    /**
     * Insert and get related feature objects.
     * 
     * @param feature
     *            Related feature
     * @param roles
     *            Related feature role objects
     * @param session
     *            Hibernate session
     * @return Related feature objects
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    public List<RelatedFeature> getOrInsertRelatedFeature(AbstractFeature feature, List<RelatedFeatureRole> roles,
            Session session) throws OwsExceptionReport {
        // TODO: create featureOfInterest and link to relatedFeature
        List<RelatedFeature> relFeats = getRelatedFeatures(feature.getIdentifier().getValue(), session);
        if (relFeats == null) {
            relFeats = new LinkedList<RelatedFeature>();
        }
        if (relFeats.isEmpty()) {
            RelatedFeature relFeat = new RelatedFeature();
            String identifier = feature.getIdentifier().getValue();
            String url = null;
            if (feature instanceof SamplingFeature) {
                identifier =
                        Configurator.getInstance().getFeatureQueryHandler()
                                .insertFeature((SamplingFeature) feature, session);
                url = ((SamplingFeature) feature).getUrl();
            }
            relFeat.setFeatureOfInterest(new FeatureOfInterestDAO().getOrInsertFeatureOfInterest(identifier, url,
                    session));
            relFeat.setRelatedFeatureRoles(new HashSet<RelatedFeatureRole>(roles));
            session.save(relFeat);
            session.flush();
            relFeats.add(relFeat);
        }
        return relFeats;
    }
}
