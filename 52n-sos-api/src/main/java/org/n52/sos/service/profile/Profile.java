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
package org.n52.sos.service.profile;

import java.util.HashMap;
import java.util.Map;

import org.n52.sos.util.JavaHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Profile {

    private static final Logger LOGGER = LoggerFactory.getLogger(Profile.class.getName());
    
    private String identifier = ProfileConstants.DEFAULT_IDENTIFIER;

    private String observationResponseFormat = ProfileConstants.DEFAULT_OBSERVATION_RESPONSE_FORMAT;

    /**
     * boolean indicates, whether SOS encodes the complete FOI-instance within
     * the Observation instance or just the FOI id
     */
    private boolean encodeFeatureOfInterestInObservations =
            ProfileConstants.DEFAULT_ENCODE_FEATUREOFINTEREST_IN_OBSERVATION;

    private String encodingNamespaceForFeatureOfInterestEncoding =
            ProfileConstants.DEFAULT_ENCODING_NAMESPACE_FOR_FEATUTREOFINTEREST_SOS_20;

    private boolean showMetadataOfEmptyObservations = ProfileConstants.DEFAULT_SHOW_METADATA_OF_EMPTY_OBSERVATIONS;

    private boolean allowSubsettingForSOS20OM20 = ProfileConstants.DEFAULT_ALLOW_SUBSETTING_FOR_OM_20;

    private boolean mergeValues = ProfileConstants.DEAFULT_MERGE_VALUES;
    
    private boolean returnLatestValueIfTemporalFilterIsMissingInGetObservation = ProfileConstants.DEAFULT_RETURN_LATEST_VALUE_IF_TEMPORAL_FILTER_IS_MISSING_IN_GETOBSERVATION;
    
    private Map<String, Boolean> encodeProcedureInObservation = new HashMap<String, Boolean>(0);
    
    private Map<String, String> defaultObservationTypesForEncoding = new HashMap<String, String>(0);

    public Profile() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getObservationResponseFormat() {
        return observationResponseFormat;
    }

    public boolean isEncodeFeatureOfInterestInObservations() {
        return encodeFeatureOfInterestInObservations;
    }

    public String getEncodingNamespaceForFeatureOfInterest() {
        return encodingNamespaceForFeatureOfInterestEncoding;
    }

    public boolean isShowMetadataOfEmptyObservations() {
        return showMetadataOfEmptyObservations;
    }

    public void setIdentifier(String identifier) {
        if (identifier != null && !identifier.isEmpty()) {
            this.identifier = identifier;
        } else {
            this.identifier = JavaHelper.generateID(Long.toString(System.currentTimeMillis()));
            LOGGER.warn("The identifier is null or empty! This generated identifier {} is set!", this.identifier);
        }
    }

    public void setObservationResponseFormat(String observationResponseFormat) {
        if (observationResponseFormat != null && !observationResponseFormat.isEmpty()) {
            this.observationResponseFormat = observationResponseFormat;
        } else {
            LOGGER.warn("The observationResponseFormat is null or empty! Default observationResponseFormat is set!");
        }
    }

    public void setEncodeFeatureOfInterestInObservations(boolean encodeFeatureOfInterestInObservations) {
        this.encodeFeatureOfInterestInObservations = encodeFeatureOfInterestInObservations;
    }

    public void setEncodingNamespaceForFeatureOfInterest(String encodingNamespateForFeatureOfInterestEncoding) {
        if (encodingNamespateForFeatureOfInterestEncoding != null) {
            this.encodingNamespaceForFeatureOfInterestEncoding = encodingNamespateForFeatureOfInterestEncoding;
        } else {
            LOGGER.warn("The encodingNamespaceForFeatureOfInterestEncoding is null! Default encodingNamespaceForFeatureOfInterestEncoding is set!");
        }

    }

    public void setShowMetadataOfEmptyObservations(boolean showMetadataOfEmptyObservations) {
        this.showMetadataOfEmptyObservations = showMetadataOfEmptyObservations;
    }

    public boolean isAllowSubsettingForSOS20OM20() {
        return allowSubsettingForSOS20OM20;
    }

    public boolean isMergeValues() {
        return mergeValues;
    }

    public void setAllowSubsettingForSOS20OM20(boolean allowSubsetting) {
        this.allowSubsettingForSOS20OM20 = allowSubsetting;
    }

    public void setMergeValues(boolean mergeValues) {
        this.mergeValues = mergeValues;
    }

    public boolean isSetEncodeFeatureOfInterestNamespace() {
        return encodingNamespaceForFeatureOfInterestEncoding != null
                && !encodingNamespaceForFeatureOfInterestEncoding.isEmpty();
    }

    public boolean isEncodeProcedureInObservation(String namespace) {
        Boolean encode = encodeProcedureInObservation.get(namespace);
        if (encode != null) {
            return encode.booleanValue();
        }
        return false;
    }

    public void setEncodeProcedureInObservation(Map<String, Boolean> encodeProcedureInObservation) {
        if (encodeProcedureInObservation != null) {
            this.encodeProcedureInObservation.putAll(encodeProcedureInObservation);
        }
    }
    
    public void addEncodeProcedureInObservation(String namespace, Boolean encode) {
        if (namespace != null && !namespace.isEmpty() && encode != null) {
            encodeProcedureInObservation.put(namespace, Boolean.valueOf(encode));
        }
    }

    public boolean isReturnLatestValueIfTemporalFilterIsMissingInGetObservation() {
        return returnLatestValueIfTemporalFilterIsMissingInGetObservation;
    }

    public void setReturnLatestValueIfTemporalFilterIsMissingInGetObservation(
            boolean returnLatestValueIfTemporalFilterIsMissingInGetObservation) {
        this.returnLatestValueIfTemporalFilterIsMissingInGetObservation =
                returnLatestValueIfTemporalFilterIsMissingInGetObservation;
    }

    public Map<String, String> getDefaultObservationTypesForEncoding() {
        return defaultObservationTypesForEncoding;
    }

    public void setDefaultObservationTypesForEncoding(Map<String, String> defaultObservationTypesForEncoding) {
        if (defaultObservationTypesForEncoding != null) {
            this.defaultObservationTypesForEncoding = defaultObservationTypesForEncoding;
        }
    }

    public void addDefaultObservationTypesForEncoding(String namespace, String observationType) {
        if (namespace != null && !namespace.isEmpty() && observationType != null && !observationType.isEmpty()) {
            defaultObservationTypesForEncoding.put(namespace, observationType);
        }
    }

}
