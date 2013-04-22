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

import java.util.Arrays;
import java.util.HashSet;

import org.n52.sos.service.profile.Profile;
import org.x52North.sensorweb.sos.profile.DefaultObservationTypesForEncodingDocument.DefaultObservationTypesForEncoding;
import org.x52North.sensorweb.sos.profile.EncodeProcedureDocument.EncodeProcedure;
import org.x52North.sensorweb.sos.profile.NoDataPlaceholderDocument.NoDataPlaceholder;
import org.x52North.sensorweb.sos.profile.SosProfileDocument;
import org.x52North.sensorweb.sos.profile.SosProfileType;

/*
 * FIXME why is this class a helper class?
 */
public class ProfileParser {

    public static Profile parseSosProfile(SosProfileDocument sosProfileDoc) {
        ProfileImpl profile = new ProfileImpl();
        SosProfileType sosProfile = sosProfileDoc.getSosProfile();
        profile.setIdentifier(sosProfile.getIdentifier());
        profile.setActiveProfile(sosProfile.getActiveProfile());
        profile.setListFeatureOfInterestsInOfferings(sosProfile.getListFeatureOfInterestsInOfferings());
        profile.setEncodeChildProcedureDescriptions(sosProfile.getEncodeChildProcedureDescriptions());
        profile.setShowFullOperationsMetadata(sosProfile.getShowFullOperationsMetadata());
        profile.setShowFullOperationsMetadataForObservations(sosProfile.getShowFullOperationsMetadataForObservations());
        profile.setAllowSubsettingForSOS20OM20(sosProfile.getAllowSubsettingForSOS20OM20());
        profile.setEncodeFeatureOfInterestInObservations(sosProfile.getEncodeFeatureOfInterestInObservations());
        profile.setEncodingNamespaceForFeatureOfInterest(sosProfile.getEncodingNamespaceForFeatureOfInterestEncoding());
        profile.setMergeValues(sosProfile.getMergeValues());
        profile.setObservationResponseFormat(sosProfile.getObservationResponseFormat());
        parseNoDataPlaceholder(profile, sosProfile.getNoDataPlaceholder());
        profile.setReturnLatestValueIfTemporalFilterIsMissingInGetObservation(sosProfile
                .getReturnLatestValueIfTemporalFilterIsMissingInGetObservation());
        profile.setShowMetadataOfEmptyObservations(sosProfile.getShowMetadataOfEmptyObservations());
        if (sosProfile.getDefaultObservationTypesForEncodingArray() != null) {
            parseDefaultObservationTypesForEncoding(profile, sosProfile.getDefaultObservationTypesForEncodingArray());
        }
        if (sosProfile.getEncodeProcedureArray() != null) {
            parseEncodeProcedure(profile, sosProfile.getEncodeProcedureArray());
        }
        if (sosProfile.isSetEncodingNamespaceForFeatureOfInterestEncoding()) {
            profile.setEncodingNamespaceForFeatureOfInterest(sosProfile
                    .getEncodingNamespaceForFeatureOfInterestEncoding());
        }

        return profile;
    }

    private static void parseNoDataPlaceholder(ProfileImpl profile, NoDataPlaceholder noDataPlaceholder) {
        if (noDataPlaceholder.getResponsePlaceholder() != null
                && !noDataPlaceholder.getResponsePlaceholder().isEmpty()) {
            profile.setResponseNoDataPlaceholder(noDataPlaceholder.getResponsePlaceholder());
        }
        if (noDataPlaceholder.getPlaceholderArray() != null && noDataPlaceholder.getPlaceholderArray().length > 0) {
            profile.setNoDataPlaceholder(new HashSet<String>(Arrays.asList(noDataPlaceholder.getPlaceholderArray())));
        }

    }

    private static void parseEncodeProcedure(ProfileImpl profile, EncodeProcedure[] encodeProcedureArray) {
        for (EncodeProcedure encodeProcedure : encodeProcedureArray) {
            profile.addEncodeProcedureInObservation(encodeProcedure.getNamespace(), encodeProcedure.getEncode());
        }

    }

    private static void parseDefaultObservationTypesForEncoding(ProfileImpl profile,
                                                                DefaultObservationTypesForEncoding[] defaultObservationTypesForEncodingArray) {
        for (DefaultObservationTypesForEncoding defaultObservationTypesForEncoding : defaultObservationTypesForEncodingArray) {
            profile.addDefaultObservationTypesForEncoding(defaultObservationTypesForEncoding.getNamespace(),
                    defaultObservationTypesForEncoding.getObservationType());
        }
    }

    private ProfileParser() {
    }
}
