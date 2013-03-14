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
package org.n52.sos.ogc.sos;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.util.QNameComparator;

/**
 * Class which represents a ObservationOffering. Used in the SosCapabilities.
 *
 */
public class SosOfferingsForContents implements Comparable<SosOfferingsForContents> {
    private static <T> void set(SortedSet<T> set, Collection<? extends T> coll) {
        if (set != null) {
            set.clear();
            if (coll != null) {
                set.addAll(coll);
            }
        }
    }

    private static <K, V> void addToMap(SortedMap<K, SortedSet<V>> map, K key, V value) {
        if (map != null && key != null && value != null) {
            SortedSet<V> set = map.get(key);
            if (set == null) {
                map.put(key, set = new TreeSet<V>());
            }
            set.add(value);
        }
    }

    private static <K, V> void addToMap(SortedMap<K, SortedSet<V>> map, K key, Collection<V> value) {
        if (map != null && key != null && value != null) {
            SortedSet<V> set = map.get(key);
            if (set == null) {
                map.put(key, set = new TreeSet<V>());
            }
            set.addAll(value);
        }
    }

    private static <K, V> void set(SortedMap<K, SortedSet<V>> sortedMap, Map<K, ? extends Collection<V>> map) {
        if (sortedMap != null) {
            sortedMap.clear();
            if (map != null) {
                for (Entry<K, ? extends Collection<V>> e : map.entrySet()) {
                    sortedMap.put(e.getKey(), e.getValue() != null ? new TreeSet<V>(e.getValue()) : new TreeSet<V>());
                }
            }
        }
    }
    /**
     * offering identifier for this contents sub section
     */
    private String offering;
    /**
     * name of the offering
     */
    private String offeringName;
    private SosEnvelope observedArea;
    /**
     * All observableProperties contained in the offering
     */
    private SortedSet<String> observableProperties = new TreeSet<String>();
    /**
     * All compositePhenomenon contained in the offering
     */
    private SortedSet<String> compositePhenomena = new TreeSet<String>();
    /**
     * All phenomenon for compositePhenomenon contained in the offering
     */
    private SortedMap<String, SortedSet<String>> phens4CompPhens = new TreeMap<String, SortedSet<String>>();
    /**
     * TimePeriod of data in the offering
     */
    private ITime phenomenonTime;
    private ITime resultTime;
    /**
     * All featuresOfinterest contained in the offering
     */
    private SortedSet<String> featureOfInterest = new TreeSet<String>();
    /**
     * All related features contained in the offering
     */
    private SortedMap<String, SortedSet<String>> relatedFeatures = new TreeMap<String, SortedSet<String>>();
    /**
     * All procedures contained in the offering
     */
    private SortedSet<String> procedures = new TreeSet<String>();
    /**
     * All resultModels contained in the offering
     */
    private SortedSet<QName> resultModels = new TreeSet<QName>(new QNameComparator());
    /**
     * All observation types contained in the offering
     */
    private SortedSet<String> observationTypes = new TreeSet<String>();
    private SortedSet<String> featureOfInterestTypes = new TreeSet<String>();
    /**
     * All observation result types contained in the offering
     */
    private SortedMap<String, SortedSet<String>> observationResultTypes = new TreeMap<String, SortedSet<String>>();
    /**
     * All response formats contained in the offering
     */
    private SortedSet<String> responseFormats = new TreeSet<String>();
    /**
     * All response modes contained in the offering
     */
    private SortedSet<String> responseModes = new TreeSet<String>();
    private SortedSet<String> procedureDescriptionFormats = new TreeSet<String>();

    /**
     * @return
     */
    public String getOffering() {
        return offering;
    }

    /**
     * @param offering
     */
    public void setOffering(String offering) {
        this.offering = offering;
    }

    /**
     * @return
     */
    public String getOfferingName() {
        return offeringName;
    }

    /**
     * @param offeringName
     */
    public void setOfferingName(String offeringName) {
        this.offeringName = offeringName;
    }

    /**
     * @return
     */
    public SortedSet<String> getObservableProperties() {
        return Collections.unmodifiableSortedSet(observableProperties);
    }

    /**
     * @param observableProperties
     */
    public void setObservableProperties(Collection<String> observableProperties) {
        set(this.observableProperties, observableProperties);
    }

    /**
     * @return
     */
    public SortedSet<String> getCompositePhenomena() {
        return Collections.unmodifiableSortedSet(compositePhenomena);
    }

    /**
     * @param compositePhenomena
     */
    public void setCompositePhenomena(Collection<String> compositePhenomena) {
        set(this.compositePhenomena, compositePhenomena);
    }

    /**
     * @return
     */
    public SortedMap<String, SortedSet<String>> getPhens4CompPhens() {
        return Collections.unmodifiableSortedMap(phens4CompPhens);
    }

    /**
     * @param phens4CompPhens
     */
    public void setPhens4CompPhens(Map<String, Collection<String>> phens4CompPhens) {
        set(this.phens4CompPhens, phens4CompPhens);
    }

    /**
     * @param phenomenonTime the phenomenon time
     */
    public void setPhenomenonTime(ITime phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
    }

    /**
     * @return the phenomenon time
     */
    public ITime getPhenomenonTime() {
        return phenomenonTime;
    }

    /**
     * @param resultTime the result time
     */
    public void setResultTime(ITime resultTime) {
        this.resultTime = resultTime;
    }

    /**
     * @return the result time
     */
    public ITime getResultTime() {
        return resultTime;
    }

    /**
     * @param featureOfInterest
     */
    public void setFeatureOfInterest(Collection<String> featureOfInterest) {
        set(this.featureOfInterest, featureOfInterest);
    }

    /**
     * @return
     */
    public SortedSet<String> getFeatureOfInterest() {
        return Collections.unmodifiableSortedSet(featureOfInterest);
    }

    /**
     * @param relatedFeatures
     */
    public void setRelatedFeatures(Map<String, Set<String>> relatedFeatures) {
        set(this.relatedFeatures, relatedFeatures);
    }

    public void addRelatedFeature(String identifier, String role) {
        addToMap(this.relatedFeatures, identifier, role);
    }

    public void addRelatedFeature(String identifier, Set<String> roles) {
        addToMap(this.relatedFeatures, identifier, roles);
    }

    /**
     * @return
     */
    public SortedMap<String, SortedSet<String>> getRelatedFeatures() {
        return Collections.unmodifiableSortedMap(relatedFeatures);
    }

    /**
     * @return
     */
    public SortedSet<String> getProcedures() {
        return Collections.unmodifiableSortedSet(procedures);
    }

    /**
     * @param procedures
     */
    public void setProcedures(Collection<String> procedures) {
        set(this.procedures, procedures);
    }

    /**
     * @return
     */
    public SortedSet<QName> getResultModels() {
        return Collections.unmodifiableSortedSet(resultModels);
    }

    /**
     * @param resultModels
     */
    public void setResultModels(Collection<QName> resultModels) {
        set(this.resultModels, resultModels);
    }

    /**
     * @return
     */
    public SortedSet<String> getObservationTypes() {
        return Collections.unmodifiableSortedSet(observationTypes);
    }

    /**
     * @param observationTypes the observationTypes to set
     */
    public void setObservationTypes(Collection<String> observationTypes) {
        set(this.observationTypes, observationTypes);
    }

    /**
     * @return the observationResultTypes
     */
    public SortedMap<String, SortedSet<String>> getObservationResultTypes() {
        return Collections.unmodifiableSortedMap(observationResultTypes);
    }

    /**
     * @param observationResultTypes the observationResultTypes to set
     */
    public void setObservationResultTypes(Map<String, Collection<String>> observationResultTypes) {
        set(this.observationResultTypes, observationResultTypes);
    }

    /**
     * @return
     */
    public SortedSet<String> getResponseFormats() {
        return Collections.unmodifiableSortedSet(responseFormats);
    }

    /**
     * @param responseFormats
     */
    public void setResponseFormats(Collection<String> responseFormats) {
        set(this.responseFormats, responseFormats);
    }

    /**
     * @return
     */
    public SortedSet<String> getResponseModes() {
        return Collections.unmodifiableSortedSet(responseModes);
    }

    /**
     * @param responseModes
     */
    public void setResponseModes(Collection<String> responseModes) {
        set(this.responseModes, responseModes);
    }

    public SosEnvelope getObservedArea() {
        return observedArea;
    }

    public void setObservedArea(SosEnvelope observedArea) {
        this.observedArea = observedArea;
    }

    public void setFeatureOfInterestTypes(Collection<String> featureOfInterestTypes) {
        set(this.featureOfInterestTypes, featureOfInterestTypes);
    }

    public SortedSet<String> getFeatureOfInterestTypes() {
        return Collections.unmodifiableSortedSet(featureOfInterestTypes);
    }

    public void setProcedureDescriptionFormat(Collection<String> procedureDescriptionFormats) {
        set(this.procedureDescriptionFormats, procedureDescriptionFormats);
    }

    public SortedSet<String> getProcedureDescriptionFormat() {
        return Collections.unmodifiableSortedSet(this.procedureDescriptionFormats);
    }

    @Override
    public int compareTo(SosOfferingsForContents o) {
        return getOffering().compareTo(o.getOffering());
    }
}
