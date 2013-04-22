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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasFeatureOfInterestTypes;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasIdentifier;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasName;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasObservationTypes;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasRelatedFeatures;

public class Offering implements Serializable, HasIdentifier, HasName, HasObservationTypes, HasFeatureOfInterestTypes,
                                 HasRelatedFeatures {
    public static final String ID = "offeringId";
    private static final long serialVersionUID = -852264383811271733L;
    private long offeringId;
    private String identifier;
    private String name;
    private Set<ObservationType> observationTypes = new HashSet<ObservationType>(0);
    private Set<FeatureOfInterestType> featureOfInterestTypes = new HashSet<FeatureOfInterestType>(0);
    private Set<RelatedFeature> relatedFeatures = new HashSet<RelatedFeature>(0);

    public Offering() {
    }

    public long getOfferingId() {
        return offeringId;
    }

    public void setOfferingId(final long offeringId) {
        this.offeringId = offeringId;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Offering setIdentifier(final String identifier) {
        this.identifier = identifier;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public Set<ObservationType> getObservationTypes() {
        return observationTypes;
    }

    @Override
    public void setObservationTypes(final Set<ObservationType> observationTypes) {
        this.observationTypes = observationTypes;
    }

    @Override
    public Set<FeatureOfInterestType> getFeatureOfInterestTypes() {
        return featureOfInterestTypes;
    }

    @Override
    public void setFeatureOfInterestTypes(final Set<FeatureOfInterestType> featureOfInterestTypes) {
        this.featureOfInterestTypes = featureOfInterestTypes;
    }

    @Override
    public Set<RelatedFeature> getRelatedFeatures() {
        return relatedFeatures;
    }

    @Override
    public void setRelatedFeatures(final Set<RelatedFeature> relatedFeatures) {
        this.relatedFeatures = relatedFeatures;
    }
}
