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
package org.n52.sos.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.n52.sos.ogc.filter.ComparisonFilter;
import org.n52.sos.ogc.filter.SpatialFilter;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.FirstLatest;

/**
 * SOS GetObservation request
 * 
 */
public class GetObservationRequest extends AbstractServiceRequest {

    /**
     * GetObservation operation name
     */
    private final String operationName = SosConstants.Operations.GetObservation.name();

    /**
     * Request as String
     */
    private String requestString;

    /**
     * SRID
     */
    private int srid;

    /**
     * Offerings list
     */
    private List<String> offerings;

    /**
     * Temporal filters list
     */
    private List<TemporalFilter> temporalFilters;

    /**
     * Procedures list
     */
    private List<String> procedures;

    /**
     * ObservedProperties list
     */
    private List<String> observedProperties;

    /**
     * FOI identifiers list
     */
    private List<String> featureIdentifiers;

    /**
     * Spatial filters list
     */
    private SpatialFilter spatialFilter;

    /**
     * Result filters list
     */
    private ComparisonFilter result;

    /**
     * Response format
     */
    private String responseFormat;

    /**
     * Result model
     */
    private String resultModel;

    private Map<String, String> namespaces;

    /**
     * Response mode
     */
    private String responseMode;

    /**
     * constructor
     */
    public GetObservationRequest() {
        String notSet = SosConstants.PARAMETER_NOT_SET;
        srid = -1;
        /*
         * resultType = null; startPosition = Integer.MIN_VALUE; maxRecords =
         * Integer.MIN_VALUE;
         */
        offerings = new ArrayList<String>(1);
        temporalFilters = new ArrayList<TemporalFilter>(1);
        procedures = new ArrayList<String>(1);
        observedProperties = new ArrayList<String>(1);
        featureIdentifiers = new ArrayList<String>(1);
        spatialFilter = null;
        result = null;
        resultModel = null;
        responseMode = notSet;
        namespaces = new HashMap<String, String>(1);
    }

    /**
     * Get temporal filters
     * 
     * @return temporal filters
     */
    public List<TemporalFilter> getTemporalFilters() {
        return temporalFilters;
    }

    /**
     * Set temporal filters
     * 
     * @param eventTime
     *            temporal filters
     */
    public void setTemporalFilters(List<TemporalFilter> temporalFilters) {
        this.temporalFilters = temporalFilters;
    }

    /**
     * Get FOI identifiers
     * 
     * @return FOI identifiers
     */
    public List<String> getFeatureIdentifiers() {
        return featureIdentifiers;
    }

    /**
     * Set FOI identifiers
     * 
     * @param featureIdentifiers
     *            FOI identifiers
     */
    public void setFeatureIdentifiers(List<String> featureIdentifiers) {
        this.featureIdentifiers = featureIdentifiers;
    }

    /**
     * Get observableProperties
     * 
     * @return observableProperties
     */
    public List<String> getObservedProperties() {
        return observedProperties;
    }

    /**
     * Set observableProperties
     * 
     * @param observableProperties
     *            observableProperties
     */
    public void setObservedProperties(List<String> observedProperties) {
        this.observedProperties = observedProperties;
        // CapabilitiesCacheController cache =
        // (CapabilitiesCacheController)SosConfigurator.getInstance().getCapsCacheController();
        // TODO: FIXME
        //
        // List<String> phens = new ArrayList<String>();
        // Map<String, Collection<String>> compPhens =
        // SosConfigurator.getInstance().getCapsCacheController().getPhens4CompPhens();
        //
        // // find phens for comp phens
        // for (String phen : observedProperty) {
        // if (compPhens.keySet().contains(phen)) {
        // phens.addAll(compPhens.get(phen));
        // } else {
        // phens.add(phen);
        // }
        // }
        //
        // this.observedProperty = (String[]) phens.toArray(new
        // String[phens.size()]);
    }

    /**
     * Get offerings
     * 
     * @return offerings
     */
    public List<String> getOfferings() {
        return offerings;
    }

    /**
     * Set offerings
     * 
     * @param offerings
     *            offerings
     */
    public void setOfferings(List<String> offerings) {
        this.offerings = offerings;
    }

    /**
     * Get procedures
     * 
     * @return procedures
     */
    public List<String> getProcedures() {
        return procedures;
    }

    /**
     * Set procedures
     * 
     * @param procedures
     *            procedures
     */
    public void setProcedures(List<String> procedures) {
        this.procedures = procedures;
    }

    /**
     * Get response format
     * 
     * @return response format
     */
    public String getResponseFormat() {
        return responseFormat;
    }

    /**
     * Set response format
     * 
     * @param responseFormat
     *            response format
     */
    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    /**
     * Get response mode
     * 
     * @return response mode
     */
    public String getResponseMode() {
        return responseMode;
    }

    /**
     * Set response mode
     * 
     * @param responseMode
     *            response mode
     */
    public void setResponseMode(String responseMode) {
        this.responseMode = responseMode;
    }

    /**
     * Get result filters
     * 
     * @return result filters
     */
    public ComparisonFilter getResult() {
        return result;
    }

    /**
     * Set result filters
     * 
     * @param result
     *            result filters
     */
    public void setResult(ComparisonFilter result) {
        this.result = result;
    }

    /**
     * Get result model
     * 
     * @return result model
     */
    public String getResultModel() {
        return resultModel;
    }

    /**
     * Set result model
     * 
     * @param resultModel
     *            result model
     */
    public void setResultModel(String resultModel) {
        this.resultModel = resultModel;
    }

    /**
     * Get SRID
     * 
     * @return SRID
     */
    public int getSrid() {
        return srid;
    }

    /**
     * Set SRID
     * 
     * @param srid
     *            SRID
     */
    public void setSrid(int srid) {
        this.srid = srid;
    }

    /**
     * Get request as String
     * 
     * @return request as String
     */
    public String getRequestString() {
        return requestString;
    }

    /**
     * Set request as String
     * 
     * @param requestString
     *            request as String
     */
    public void setRequestString(String requestString) {
        this.requestString = requestString;
    }

    /**
     * Get spatial filter
     * 
     * @return spatial filter
     */
    public SpatialFilter getSpatialFilter() {
        return spatialFilter;
    }

    /**
     * Set spatial filter
     * 
     * @param resultSpatialFilter
     *            spatial filter
     */
    public void setSpatialFilter(SpatialFilter resultSpatialFilter) {
        this.spatialFilter = resultSpatialFilter;
    }

    /**
     * Create a copy of this request with defined observableProperties
     * 
     * @param obsProps
     *            defined observableProperties
     * @return SOS GetObservation request copy
     */
    public GetObservationRequest copyOf(List<String> obsProps) {
        GetObservationRequest res = new GetObservationRequest();
        res.setTemporalFilters(this.temporalFilters);
        res.setObservedProperties(obsProps);
        res.setOfferings(this.offerings);
        res.setProcedures(this.procedures);
        res.setResponseFormat(this.responseFormat);
        res.setResponseMode(this.responseMode);
        res.setSpatialFilter(this.spatialFilter);
        res.setResult(this.result);
        res.setResultModel(this.resultModel);
        res.setFeatureIdentifiers(this.featureIdentifiers);
        res.setService(this.getService());
        res.setSrid(this.srid);
        res.setRequestString(this.requestString);
        return res;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.request.AbstractSosRequest#getOperationName()
     */
    @Override
    public String getOperationName() {
        return operationName;
    }

    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    public boolean isSetOffering() {
        if (offerings != null && !offerings.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean isSetObservableProperty() {
        if (observedProperties != null && !observedProperties.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean isSetProcedure() {
        if (procedures != null && !procedures.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean isSetFeatureOfInterest() {
        if (featureIdentifiers != null && !featureIdentifiers.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean isSetTemporalFilter() {
        if (temporalFilters != null && !temporalFilters.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean isSetSpatialFilter() {
        if (spatialFilter != null) {
            return true;
        }
        return false;
    }

    public boolean hasFirstLatestTemporalFilter() {
        for (TemporalFilter temporalFilter : temporalFilters) {
            if (temporalFilter.getTime() instanceof TimeInstant) {
                TimeInstant ti = (TimeInstant)temporalFilter.getTime();
                if (ti.isSetIndeterminateValue() && FirstLatest.contains(ti.getIndeterminateValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<FirstLatest> getFirstLatestTemporalFilter() {
        List<FirstLatest> tf = new LinkedList<FirstLatest>();
        for (TemporalFilter temporalFilter : temporalFilters) {
            if (temporalFilter.getTime() instanceof TimeInstant) {
                TimeInstant ti = (TimeInstant) temporalFilter.getTime();
                if (ti.isSetIndeterminateValue() && FirstLatest.contains(ti.getIndeterminateValue())) {
                    tf.add(FirstLatest.valueOf(ti.getIndeterminateValue()));
                }
            }
        }
        return tf;
    }

    public List<TemporalFilter> getNotFirstLatestTemporalFilter() {
        List<TemporalFilter> tf = new LinkedList<TemporalFilter>();
        for (TemporalFilter temporalFilter : temporalFilters) {
            if (temporalFilter.getTime() instanceof TimeInstant) {
                TimeInstant ti = (TimeInstant) temporalFilter.getTime();
                if (!ti.isSetIndeterminateValue() || !FirstLatest.contains(ti.getIndeterminateValue())) {
                    tf.add(temporalFilter);
                }
            } else {
                tf.add(temporalFilter);
            }
        }
        return tf;
    }

    public boolean hasTemporalFilters() {
        return temporalFilters != null && !temporalFilters.isEmpty();
    }
    
    public boolean isSetResultModel() {
        return resultModel != null;
    }
    
    public boolean isEmpty() {
        return !isSetOffering() 
                && !isSetObservableProperty() 
                && !isSetProcedure()
                && !isSetFeatureOfInterest() 
                && !isSetTemporalFilter()
                && !isSetSpatialFilter();
    }
}
