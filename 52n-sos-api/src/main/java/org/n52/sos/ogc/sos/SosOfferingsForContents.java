/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.ogc.sos;

import java.util.Collection;
import java.util.Map;

import javax.xml.namespace.QName;

import org.n52.sos.ogc.gml.time.ITime;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Class which represents a ObservationOffering. Used in the SosCapabilities.
 * 
 */
public class SosOfferingsForContents {

    /**
     * offering identifier for this contents sub section
     */
    private String offering;

    /**
     * name of the offering
     */
    private String offeringName;

    private SosEnvelope observedArea;
    
//    /**
//     * Envelope of the offering
//     */
//    private Envelope boundeBy;
//
//    /**
//     * SRS id of the offerng
//     */
//    private int srid;

    /**
     * All observableProperties contained in the offering
     */
    private Collection<String> observableProperties;

    /**
     * All compositePhenomenon contained in the offering
     */
    private Collection<String> compositePhenomena;

    /**
     * All phenomenon for compositePhenomenon contained in the offering
     */
    private Map<String, Collection<String>> phens4CompPhens;

    /**
     * TimePeriod of data in the offering
     */
    private ITime time;

    /**
     * All featuresOfinterest contained in the offering
     */
    private Collection<String> featureOfInterest;

    /**
     * All related features contained in the offering
     */
    private Map<String, Collection<String>> relatedFeatures;

    /**
     * All procedures contained in the offering
     */
    private Collection<String> procedures;

    /**
     * All resultModels contained in the offering
     */
    private Collection<QName> resultModels;

    /**
     * All observation types contained in the offering
     */
    private Collection<String> observationTypes;
    
    private Collection<String> featureOfInterestTypes;

    /**
     * All observation result types contained in the offering
     */
    private Map<String, Collection<String>> observationResultTypes;

    /**
     * All response formats contained in the offering
     */
    private Collection<String> responseFormats;

    /**
     * All response modes contained in the offering
     */
    private Collection<String> responseModes;

    /**
     * @return
     */
    public String getOffering() {
        return offering;
    }

    /**
     * @param offeringId
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

//    /**
//     * @return
//     */
//    public Envelope getBoundeBy() {
//        return boundeBy;
//    }
//
//    /**
//     * @param boundeBy
//     */
//    public void setBoundeBy(Envelope boundeBy) {
//        this.boundeBy = boundeBy;
//    }
//
//    /**
//     * @return
//     */
//    public int getSrid() {
//        return srid;
//    }
//
//    /**
//     * @param srid
//     */
//    public void setSrid(int srid) {
//        this.srid = srid;
//    }

    /**
     * @return
     */
    public Collection<String> getObservableProperties() {
        return observableProperties;
    }

    /**
     * @param phenomenons
     */
    public void setObservableProperties(Collection<String> ObservableProperties) {
        this.observableProperties = ObservableProperties;
    }

    /**
     * @return
     */
    public Collection<String> getCompositePhenomena() {
        return compositePhenomena;
    }

    /**
     * @param compositePhenomena
     */
    public void setCompositePhenomena(Collection<String> compositePhenomena) {
        this.compositePhenomena = compositePhenomena;
    }

    /**
     * @return
     */
    public Map<String, Collection<String>> getPhens4CompPhens() {
        return phens4CompPhens;
    }

    /**
     * @param phens4CompPhens
     */
    public void setPhens4CompPhens(Map<String, Collection<String>> phens4CompPhens) {
        this.phens4CompPhens = phens4CompPhens;
    }

    /**
     * @param time
     */
    public void setTime(ITime time) {
        this.time = time;
    }

    /**
     * @return
     */
    public ITime getTime() {
        return time;
    }

    /**
     * @param featureOfInterest
     */
    public void setFeatureOfInterest(Collection<String> featureOfInterest) {
        this.featureOfInterest = featureOfInterest;
    }

    /**
     * @return
     */
    public Collection<String> getFeatureOfInterest() {
        return featureOfInterest;
    }

    /**
     * @param relatedFeatures
     */
    public void setRelatedFeatures(Map<String, Collection<String>> relatedFeatures) {
        this.relatedFeatures = relatedFeatures;
    }

    /**
     * @return
     */
    public Map<String, Collection<String>> getRelatedFeatures() {
        return relatedFeatures;
    }

    /**
     * @return
     */
    public Collection<String> getProcedures() {
        return procedures;
    }

    /**
     * @param procedures
     */
    public void setProcedures(Collection<String> procedures) {
        this.procedures = procedures;
    }

    /**
     * @return
     */
    public Collection<QName> getResultModels() {
        return resultModels;
    }

    /**
     * @param resultModels
     */
    public void setResultModels(Collection<QName> resultModels) {
        this.resultModels = resultModels;
    }

    /**
     * @return
     */
    public Collection<String> getObservationTypes() {
        return observationTypes;
    }

    /**
     * @param observationTypes
     *            the observationTypes to set
     */
    public void setObservationTypes(Collection<String> observationTypes) {
        this.observationTypes = observationTypes;
    }

    /**
     * @return the observationResultTypes
     */
    public Map<String, Collection<String>> getObservationResultTypes() {
        return observationResultTypes;
    }

    /**
     * @param observationResultTypes
     *            the observationResultTypes to set
     */
    public void setObservationResultTypes(Map<String, Collection<String>> observationResultTypes) {
        this.observationResultTypes = observationResultTypes;
    }

    /**
     * @return
     */
    public Collection<String> getResponseFormats() {
        return responseFormats;
    }

    /**
     * @param responseFormats
     */
    public void setResponseFormats(Collection<String> responseFormats) {
        this.responseFormats = responseFormats;
    }

    /**
     * @return
     */
    public Collection<String> getResponseModes() {
        return responseModes;
    }

    /**
     * @param responseModes
     */
    public void setResponseModes(Collection<String> responseModes) {
        this.responseModes = responseModes;
    }

    public SosEnvelope getObservedArea() {
        return observedArea;
    }

    public void setObservedArea(SosEnvelope observedArea) {
        this.observedArea = observedArea;
    }

    public Collection<String> getFeatureOfInterestTypes() {
        return featureOfInterestTypes;
    }

    public void setFeatureOfInterestTypes(Collection<String> featureOfInterestTypes) {
        this.featureOfInterestTypes = featureOfInterestTypes;
    }
}
