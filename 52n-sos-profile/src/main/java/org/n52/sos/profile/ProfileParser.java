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

import org.n52.sos.service.profile.IProfile;
import org.x52North.sensorweb.sos.profile.DefaultObservationTypesForEncodingDocument.DefaultObservationTypesForEncoding;
import org.x52North.sensorweb.sos.profile.EncodeProcedureDocument.EncodeProcedure;
import org.x52North.sensorweb.sos.profile.SosProfileDocument;
import org.x52North.sensorweb.sos.profile.SosProfileType;

public class ProfileParser {
    
    public static IProfile parseSosProfile(SosProfileDocument sosProfileDoc) {
        Profile profile = new Profile();
        SosProfileType sosProfile = sosProfileDoc.getSosProfile();
        profile.setIdentifier(sosProfile.getIdentifier());
        profile.setActiveProfile(sosProfile.getActiveProfile());
        profile.setAllowSubsettingForSOS20OM20(sosProfile.getAllowSubsettingForSOS20OM20());
        profile.setEncodeFeatureOfInterestInObservations(sosProfile.getEncodeFeatureOfInterestInObservations());
        profile.setEncodingNamespaceForFeatureOfInterest(sosProfile.getEncodingNamespaceForFeatureOfInterestEncoding());
        profile.setMergeValues(sosProfile.getMergeValues());
        profile.setObservationResponseFormat(sosProfile.getObservationResponseFormat());
        profile.setReturnLatestValueIfTemporalFilterIsMissingInGetObservation(sosProfile.getReturnLatestValueIfTemporalFilterIsMissingInGetObservation());
        profile.setShowMetadataOfEmptyObservations(sosProfile.getShowMetadataOfEmptyObservations());
        if (sosProfile.getDefaultObservationTypesForEncodingArray() != null) {
            parseDefaultObservationTypesForEncoding(profile, sosProfile.getDefaultObservationTypesForEncodingArray());
        }
        if (sosProfile.getEncodeProcedureArray() != null) {
            parseEncodeProcedure(profile, sosProfile.getEncodeProcedureArray());
        }
        if (sosProfile.isSetEncodingNamespaceForFeatureOfInterestEncoding()) {
            profile.setEncodingNamespaceForFeatureOfInterest(sosProfile.getEncodingNamespaceForFeatureOfInterestEncoding());
        }
        
        return profile;
    }

    private static void parseEncodeProcedure(Profile profile, EncodeProcedure[] encodeProcedureArray) {
        for (EncodeProcedure encodeProcedure : encodeProcedureArray) {
            profile.addEncodeProcedureInObservation(encodeProcedure.getNamespace(), encodeProcedure.getEncode());
        }
        
    }

    private static void parseDefaultObservationTypesForEncoding(Profile profile,
            DefaultObservationTypesForEncoding[] defaultObservationTypesForEncodingArray) {
        for (DefaultObservationTypesForEncoding defaultObservationTypesForEncoding : defaultObservationTypesForEncodingArray) {
            profile.addDefaultObservationTypesForEncoding(defaultObservationTypesForEncoding.getNamespace(), defaultObservationTypesForEncoding.getObservationType());
        }
    }

}
