/**
 * Copyright (C) 2012
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

import java.util.List;
import java.util.Map;

import org.n52.sos.ogc.filter.SpatialFilter;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.sos.SosConstants;

public class GetResultRequest extends AbstractServiceRequest {

    private final String operationName = SosConstants.Operations.GetResult.name();
    
    /**
     * Identifier for the observation template
     */
    private String observationTemplateIdentifier;

    private String offering;

    private String observedProperty;

    private List<String> featureIdentifiers;

    private List<TemporalFilter> temporalFilter;

    private SpatialFilter spatialFilter;
    
    private Map<String, String> namespaces;
    
    public GetResultRequest() {
        super();
    }

    @Override
    public String getOperationName() {
        return operationName;
    }

    /**
     * Set observation template identifier
     * 
     * @param observationTemplateIdentifier
     *            observation template identifier
     */
    public void setObservationTemplateIdentifier(String observationTemplateIdentifier) {
        this.observationTemplateIdentifier = observationTemplateIdentifier;
    }
    
    /**
     * Get observation template identifier
     * 
     * @return observation template identifier
     */
    public String getObservationTemplateIdentifier() {
        return observationTemplateIdentifier;
    }

    public String getOffering() {
        return offering;
    }

    public String getObservedProperty() {
        return observedProperty;
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

    public List<TemporalFilter> getTemporalFilter() {
        return temporalFilter;
    }

    public SpatialFilter getSpatialFilter() {
        return spatialFilter;
    }

    public void setOffering(String offering) {
        this.offering = offering;
    }

    public void setObservedProperty(String observedProperty) {
        this.observedProperty = observedProperty;
    }

    public void setTemporalFilter(List<TemporalFilter> temporalFilters) {
        this.temporalFilter = temporalFilters;
    }

    public void setSpatialFilter(SpatialFilter spatialFilter) {
        this.spatialFilter = spatialFilter;
    }

    public boolean hasFeatureOfInterest() {
        return featureIdentifiers != null && !featureIdentifiers.isEmpty();
    }

    public boolean hasTemporalFilter() {
        return temporalFilter != null && !temporalFilter.isEmpty();
    }

    public boolean hasSpatialFilter() {
        return spatialFilter != null;
    }
    
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    public Map<String, String> getNamespaces() {
        return namespaces;
    }

}
