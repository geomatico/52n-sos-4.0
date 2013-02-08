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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.n52.sos.cache.CapabilitiesCache;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.util.CollectionHelper;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class OfferingCache {
    
    private Map<String, String> names;
    private Map<String, List<String>> procedures;
    private Map<String, Collection<String>> observableProperties;
    private Map<String, Collection<String>> relatedFeatures;
    private Map<String, Collection<String>> observationTypes;
    private Map<String, Collection<String>> allowedObservationTypes;
    private Map<String, DateTime> minTimes;
    private Map<String, DateTime> maxTimes;
    private Map<String, SosEnvelope> envelopes;
    private Map<String, Collection<String>> featuresOfInterest;

    public OfferingCache(List<Offering> offerings) {
        final int size = offerings.size();
        names = CollectionHelper.synchronizedMap(size);
        procedures = CollectionHelper.synchronizedMap(size);
        observableProperties = CollectionHelper.synchronizedMap(size);
        relatedFeatures = CollectionHelper.synchronizedMap(size);
        observationTypes = CollectionHelper.synchronizedMap(size);
        allowedObservationTypes = CollectionHelper.synchronizedMap(size);
        minTimes = CollectionHelper.synchronizedMap(size);
        maxTimes = CollectionHelper.synchronizedMap(size);
        envelopes = CollectionHelper.synchronizedMap(size);
        featuresOfInterest = CollectionHelper.synchronizedMap(size);
    }

    public Map<String, String> getNames() {
        return names;
    }

    public Map<String, List<String>> getProcedures() {
        return procedures;
    }

    public Map<String, Collection<String>> getObservableProperties() {
        return observableProperties;
    }

    public Map<String, Collection<String>> getRelatedFeatures() {
        return relatedFeatures;
    }

    public Map<String, Collection<String>> getObservationTypes() {
        return observationTypes;
    }

    public Map<String, Collection<String>> getAllowedObservationTypes() {
        return allowedObservationTypes;
    }

    public Map<String, DateTime> getMinTimes() {
        return minTimes;
    }

    public Map<String, DateTime> getMaxTimes() {
        return maxTimes;
    }

    public Map<String, SosEnvelope> getEnvelopes() {
        return envelopes;
    }

    public Map<String, Collection<String>> getFeaturesOfInterest() {
        return featuresOfInterest;
    }
    
    public void setName(String offering, String name) {
        names.put(offering, name);
    }
    
    public void setProcedures(String offering, List<String> procedures) {
        this.procedures.put(offering, procedures);
    }

    public void setObservableProperties(String offering, Collection<String> observableProperties) {
        this.observableProperties.put(offering, observableProperties);
    }

    public void setRelatedFeatures(String offering, Collection<String> relatedFeatures) {
        this.relatedFeatures.put(offering, relatedFeatures);
    }
    
    public void setObservationTypes(String offering, Collection<String> observationTypes) {
        this.observationTypes.put(offering, observationTypes);
    }
    
    public void setAllowedObservationType(String offering, Collection<String> allowedObservationTypes) {
        this.allowedObservationTypes.put(offering, allowedObservationTypes);
    }
    
    public void setMinTime(String offering, DateTime minTime) {
        minTimes.put(offering, minTime);
    }
    
    public void setMaxTime(String offering, DateTime maxTime) {
        maxTimes.put(offering, maxTime);
    }
    
    public void setEnvelope(String offering, SosEnvelope envelope) {
        envelopes.put(offering, envelope);
    }

    public void setFeaturesOfInterest(String offering, Collection<String> featuresOfInterest) {
        this.featuresOfInterest.put(offering, featuresOfInterest);
    }
    
    public void save(CapabilitiesCache cache) {
        cache.setKOfferingVName(getNames());
        cache.setKOfferingVObservableProperties(getObservableProperties());
        cache.setKOfferingVProcedures(getProcedures());
        cache.setKOfferingVRelatedFeatures(getRelatedFeatures());
        cache.setKOfferingVObservationTypes(getObservationTypes());
        cache.setKOfferingVFeatures(getFeaturesOfInterest());
        cache.setAllowedKOfferingVObservationType(getAllowedObservationTypes());
        cache.setKOfferingVEnvelope(getEnvelopes());
        cache.setKOfferingVMinTime(getMinTimes());
        cache.setKOfferingVMaxTime(getMaxTimes());
    }
    
}
