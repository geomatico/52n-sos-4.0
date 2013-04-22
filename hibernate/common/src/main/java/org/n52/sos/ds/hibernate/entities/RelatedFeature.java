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

import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasFeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasOfferings;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasRelatedFeatureRoles;

public class RelatedFeature implements Serializable, HasFeatureOfInterest, HasRelatedFeatureRoles, HasOfferings {
    public static final String ID = "relatedFeatureId";
    private static final long serialVersionUID = 1685993150557096335L;
    private long relatedFeatureId;
    private FeatureOfInterest featureOfInterest;
    private Set<RelatedFeatureRole> relatedFeatureRoles = new HashSet<RelatedFeatureRole>(0);
    private Set<Offering> offerings = new HashSet<Offering>(0);

    public RelatedFeature() {
    }

    public long getRelatedFeatureId() {
        return this.relatedFeatureId;
    }

    public void setRelatedFeatureId(long relatedFeatureId) {
        this.relatedFeatureId = relatedFeatureId;
    }

    @Override
    public FeatureOfInterest getFeatureOfInterest() {
        return this.featureOfInterest;
    }

    @Override
    public void setFeatureOfInterest(FeatureOfInterest featureOfInterest) {
        this.featureOfInterest = featureOfInterest;
    }

    @Override
    public Set<RelatedFeatureRole> getRelatedFeatureRoles() {
        return this.relatedFeatureRoles;
    }

    @Override
    public void setRelatedFeatureRoles(Set<RelatedFeatureRole> relatedFeatureRoles) {
        this.relatedFeatureRoles = relatedFeatureRoles;
    }

    @Override
    public Set<Offering> getOfferings() {
        return this.offerings;
    }

    @Override
    public void setOfferings(Set<Offering> offerings) {
        this.offerings = offerings;
    }
}
