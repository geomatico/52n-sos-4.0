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
package org.n52.sos.profile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.n52.sos.service.profile.IProfile;
import org.n52.sos.util.JavaHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Profile implements IProfile {

    private static final Logger LOGGER = LoggerFactory.getLogger(Profile.class.getName());

    private String identifier;

    private boolean activeProfile;

    private String observationResponseFormat;

    /**
     * boolean indicates, whether SOS encodes the complete FOI-instance within
     * the Observation instance or just the FOI id
     */
    private boolean encodeFeatureOfInterestInObservations;

    private String encodingNamespaceForFeatureOfInterestEncoding;

    private boolean showMetadataOfEmptyObservations;

    private boolean listFeatureOfInterestsInOfferings;

    private boolean encodeChildProcedureDescriptions;

    private boolean showFullOperationsMetadata;

    private boolean showFullOperationsMetadataForObservations;

    private boolean allowSubsettingForSOS20OM20;

    private boolean mergeValues;

    private String responseNoDataPlaceholder;
    
    private Set<String> noDataPlaceholder = new HashSet<String>(0);

    private boolean returnLatestValueIfTemporalFilterIsMissingInGetObservation;

    private Map<String, Boolean> encodeProcedureInObservation = new HashMap<String, Boolean>(0);

    private Map<String, String> defaultObservationTypesForEncoding = new HashMap<String, String>(0);

    public Profile() {

    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean isActiveProfile() {
        return activeProfile;
    }

    @Override
    public String getObservationResponseFormat() {
        return observationResponseFormat;
    }

    @Override
    public boolean isEncodeFeatureOfInterestInObservations() {
        return encodeFeatureOfInterestInObservations;
    }

    @Override
    public String getEncodingNamespaceForFeatureOfInterest() {
        return encodingNamespaceForFeatureOfInterestEncoding;
    }

    @Override
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

    public void setActiveProfile(boolean activeProfile) {
        this.activeProfile = activeProfile;
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

    @Override
    public boolean isAllowSubsettingForSOS20OM20() {
        return allowSubsettingForSOS20OM20;
    }

    @Override
    public boolean isMergeValues() {
        return mergeValues;
    }

    public void setAllowSubsettingForSOS20OM20(boolean allowSubsetting) {
        this.allowSubsettingForSOS20OM20 = allowSubsetting;
    }

    public void setMergeValues(boolean mergeValues) {
        this.mergeValues = mergeValues;
    }

    @Override
    public boolean isSetEncodeFeatureOfInterestNamespace() {
        return encodingNamespaceForFeatureOfInterestEncoding != null
                && !encodingNamespaceForFeatureOfInterestEncoding.isEmpty();
    }

    @Override
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

    public void addEncodeProcedureInObservation(String namespace, boolean encode) {
        if (namespace != null && !namespace.isEmpty()) {
            encodeProcedureInObservation.put(namespace, encode);
        }
    }

    @Override
    public boolean isReturnLatestValueIfTemporalFilterIsMissingInGetObservation() {
        return returnLatestValueIfTemporalFilterIsMissingInGetObservation;
    }

    public void setReturnLatestValueIfTemporalFilterIsMissingInGetObservation(
            boolean returnLatestValueIfTemporalFilterIsMissingInGetObservation) {
        this.returnLatestValueIfTemporalFilterIsMissingInGetObservation =
                returnLatestValueIfTemporalFilterIsMissingInGetObservation;
    }

    @Override
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

    public boolean isListFeatureOfInterestsInOfferings() {
        return listFeatureOfInterestsInOfferings;
    }

    public void setListFeatureOfInterestsInOfferings(boolean listFeatureOfInterestsInOfferings) {
        this.listFeatureOfInterestsInOfferings = listFeatureOfInterestsInOfferings;
    }

    public boolean isEncodeChildProcedureDescriptions() {
        return encodeChildProcedureDescriptions;
    }

    public void setEncodeChildProcedureDescriptions(boolean encodeChildProcedureDescriptions) {
        this.encodeChildProcedureDescriptions = encodeChildProcedureDescriptions;
    }

    public boolean isShowFullOperationsMetadata() {
        return showFullOperationsMetadata;
    }

    public void setShowFullOperationsMetadata(boolean showFullOperationsMetadata) {
        this.showFullOperationsMetadata = showFullOperationsMetadata;
    }

    public boolean isShowFullOperationsMetadataForObservations() {
        return showFullOperationsMetadataForObservations;
    }

    public void setShowFullOperationsMetadataForObservations(boolean showFullOperationsMetadataForObservations) {
        this.showFullOperationsMetadataForObservations = showFullOperationsMetadataForObservations;
    }

    public String getResponseNoDataPlaceholder() {
        return responseNoDataPlaceholder;
    }

    public void setResponseNoDataPlaceholder(String responseNoDataPlaceholder) {
        this.responseNoDataPlaceholder = responseNoDataPlaceholder;
    }

    public Set<String> getNoDataPlaceholder() {
        return noDataPlaceholder;
    }

    public void setNoDataPlaceholder(Set<String> noDataPlaceholder) {
        this.noDataPlaceholder = noDataPlaceholder;
    }

    @Override
    public boolean isSetNoDataPlaceholder() {
        return noDataPlaceholder != null && !noDataPlaceholder.isEmpty();
    }

}
