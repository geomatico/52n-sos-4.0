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
    private Map<String, Collection<String>> featuresOfInterests;

    public OfferingCache(List<Offering> offerings) {
        final int size = offerings.size();
        final float loadFactor = 1.0f;
        this.names = CollectionHelper.synchronizedMap(size, loadFactor);
        this.procedures = CollectionHelper.synchronizedMap(size, loadFactor);
        this.observableProperties = CollectionHelper.synchronizedMap(size, loadFactor);
        this.relatedFeatures = CollectionHelper.synchronizedMap(size, loadFactor);
        this.observationTypes = CollectionHelper.synchronizedMap(size, loadFactor);
        this.allowedObservationTypes = CollectionHelper.synchronizedMap(size, loadFactor);
        this.minTimes = CollectionHelper.synchronizedMap(size, loadFactor);
        this.maxTimes = CollectionHelper.synchronizedMap(size, loadFactor);
        this.envelopes = CollectionHelper.synchronizedMap(size, loadFactor);
        this.featuresOfInterests = CollectionHelper.synchronizedMap(size, loadFactor);
    }

    public Map<String, String> getNames() {
        return this.names;
    }

    public Map<String, List<String>> getProcedures() {
        return this.procedures;
    }

    public Map<String, Collection<String>> getObservableProperties() {
        return this.observableProperties;
    }

    public Map<String, Collection<String>> getRelatedFeatures() {
        return this.relatedFeatures;
    }

    public Map<String, Collection<String>> getObservationTypes() {
        return this.observationTypes;
    }

    public Map<String, Collection<String>> getAllowedObservationTypes() {
        return this.allowedObservationTypes;
    }

    public Map<String, DateTime> getMinTimes() {
        return this.minTimes;
    }

    public Map<String, DateTime> getMaxTimes() {
        return this.maxTimes;
    }

    public Map<String, SosEnvelope> getEnvelopes() {
        return this.envelopes;
    }

    public Map<String, Collection<String>> getFeaturesOfInterests() {
        return this.featuresOfInterests;
    }
    
    public void addOfferingName(String offering, String name) {
        getNames().put(offering, name);
    }
    
    public void addProcedures(String offering, List<String> procedures) {
        getProcedures().put(offering, procedures);
    }

    public void addObservableProperties(String offering, Collection<String> observableProperties) {
        getObservableProperties().put(offering, observableProperties);
    }

    public void addRelatedFeatures(String offering, Collection<String> relatedFeatures) {
        getRelatedFeatures().put(offering, relatedFeatures);
    }
    
    public void addObservationTypes(String offering, Collection<String> observationTypes) {
        getObservationTypes().put(offering, observationTypes);
    }
    
    public void addAllowedObservationType(String offering, Collection<String> allowedObservationTypes) {
        getAllowedObservationTypes().put(offering, allowedObservationTypes);
    }
    
    public void addMinTime(String offering, DateTime minTime) {
        getMinTimes().put(offering, minTime);
    }
    
    public void addMaxTime(String offering, DateTime maxTime) {
        getMaxTimes().put(offering, maxTime);
    }
    
    public void addEnvelope(String offering, SosEnvelope envelope) {
        getEnvelopes().put(offering, envelope);
    }

    public void addFeaturesOfInterest(String offering, Collection<String> featuresOfInterest) {
        getFeaturesOfInterests().put(offering, featuresOfInterest);
    }
    
    public void save(CapabilitiesCache cache) {
        cache.setKOfferingVName(getNames());
        cache.setKOfferingVObservableProperties(getObservableProperties());
        cache.setKOfferingVProcedures(getProcedures());
        cache.setKOfferingVRelatedFeatures(getRelatedFeatures());
        cache.setKOfferingVObservationTypes(getObservationTypes());
        cache.setKOfferingVFeatures(getFeaturesOfInterests());
        cache.setAllowedKOfferingVObservationType(getAllowedObservationTypes());
        cache.setKOfferingVEnvelope(getEnvelopes());
        cache.setKOfferingVMinTime(getMinTimes());
        cache.setKOfferingVMaxTime(getMaxTimes());
    }
    
}
