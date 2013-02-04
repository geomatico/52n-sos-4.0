/*
 * Copyright (C) 2013 52north.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
    
    private Map<String, String> kOfferingVName;
    private Map<String, List<String>> kOfferingVProcedures;
    private Map<String, Collection<String>> kOfferingVObservableProperties;
    private Map<String, Collection<String>> kOfferingVRelatedFeatures;
    private Map<String, Collection<String>> kOfferingVObservationTypes;
    private Map<String, Collection<String>> allowedkOfferingVObservationTypes;
    private Map<String, DateTime> kOfferingVMinTime;
    private Map<String, DateTime> kOfferingVMaxTime;
    private Map<String, SosEnvelope> kOfferingVEnvelope;
    private Map<String, Collection<String>> kOfferingVFeaturesOfInterest;

    public OfferingCache(List<Offering> offerings) {
        final int size = offerings.size();
        final float loadFactor = 1.0f;
        kOfferingVName = CollectionHelper.synchronizedMap(size, loadFactor);
        kOfferingVProcedures = CollectionHelper.synchronizedMap(size, loadFactor);
        kOfferingVObservableProperties = CollectionHelper.synchronizedMap(size, loadFactor);
        kOfferingVRelatedFeatures = CollectionHelper.synchronizedMap(size, loadFactor);
        kOfferingVObservationTypes = CollectionHelper.synchronizedMap(size, loadFactor);
        allowedkOfferingVObservationTypes = CollectionHelper.synchronizedMap(size, loadFactor);
        kOfferingVMinTime = CollectionHelper.synchronizedMap(size, loadFactor);
        kOfferingVMaxTime = CollectionHelper.synchronizedMap(size, loadFactor);
        kOfferingVEnvelope = CollectionHelper.synchronizedMap(size, loadFactor);
        kOfferingVFeaturesOfInterest = CollectionHelper.synchronizedMap(size, loadFactor);
    }

    public Map<String, String> getKOfferingVName() {
        return kOfferingVName;
    }

    public Map<String, List<String>> getKOfferingVProcedures() {
        return kOfferingVProcedures;
    }

    public Map<String, Collection<String>> getKOfferingVObservableProperties() {
        return kOfferingVObservableProperties;
    }

    public Map<String, Collection<String>> getKOfferingVRelatedFeatures() {
        return kOfferingVRelatedFeatures;
    }

    public Map<String, Collection<String>> getKOfferingVObservationTypes() {
        return kOfferingVObservationTypes;
    }

    public Map<String, Collection<String>> getAllowedkOfferingVObservationTypes() {
        return allowedkOfferingVObservationTypes;
    }

    public Map<String, DateTime> getKOfferingVMinTime() {
        return kOfferingVMinTime;
    }

    public Map<String, DateTime> getKOfferingVMaxTime() {
        return kOfferingVMaxTime;
    }

    public Map<String, SosEnvelope> getKOfferingVEnvelope() {
        return kOfferingVEnvelope;
    }

    public Map<String, Collection<String>> getKOfferingVFeaturesOfInterest() {
        return kOfferingVFeaturesOfInterest;
    }
    
    public void save(CapabilitiesCache cache) {
        cache.setKOfferingVName(kOfferingVName);
        cache.setKOfferingVObservableProperties(kOfferingVObservableProperties);
        cache.setKOfferingVProcedures(kOfferingVProcedures);
        cache.setKOfferingVRelatedFeatures(kOfferingVRelatedFeatures);
        cache.setKOfferingVObservationTypes(kOfferingVObservationTypes);
        cache.setKOfferingVFeatures(kOfferingVFeaturesOfInterest);
        cache.setAllowedKOfferingVObservationType(allowedkOfferingVObservationTypes);
        cache.setKOfferingVEnvelope(kOfferingVEnvelope);
        cache.setKOfferingVMinTime(kOfferingVMinTime);
        cache.setKOfferingVMaxTime(kOfferingVMaxTime);
    }
    
}
