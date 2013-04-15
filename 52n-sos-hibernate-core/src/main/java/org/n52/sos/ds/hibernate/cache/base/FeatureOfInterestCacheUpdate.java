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
package org.n52.sos.ds.hibernate.cache.base;

import static org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities.getFeatureOfInterestObjects;
import static org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities.getProceduresForFeatureOfInterest;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.n52.sos.ds.hibernate.cache.AbstractDatasourceCacheUpdate;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ogc.ows.OwsExceptionReport;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class FeatureOfInterestCacheUpdate extends AbstractDatasourceCacheUpdate {
    protected Set<String> getFeatureIdentifiers(Collection<FeatureOfInterest> featuresOfInterest) {
        Set<String> featureList = new HashSet<String>(featuresOfInterest.size());
        for (FeatureOfInterest featureOfInterest : featuresOfInterest) {
            featureList.add(featureOfInterest.getIdentifier());
        }
        return featureList;
    }

    @Override
    public void execute() {
        // FIXME shouldn't the identifiers be translated using CacheHelper.addPrefixAndGetFeatureIdentifier()?
        for (FeatureOfInterest featureOfInterest : getFeatureOfInterestObjects(getSession())) {
            getCache().addFeatureOfInterest(featureOfInterest.getIdentifier());
            getCache().setProceduresForFeatureOfInterest(featureOfInterest.getIdentifier(),
                                                         getProceduresForFeatureOfInterest(getSession(), featureOfInterest));
            getCache().setFeatureHierarchy(featureOfInterest.getIdentifier(),
                                           getFeatureIdentifiers(featureOfInterest.getFeatureOfInterestsForChildFeatureId()));
        }
        try {
            getCache().setGlobalEnvelope(getFeatureQueryHandler().getEnvelopeForFeatureIDs(
                    getCache().getFeaturesOfInterest(), getSession()));
        } catch (OwsExceptionReport ex) {
            getErrors().add(ex);
        }
    }
}
