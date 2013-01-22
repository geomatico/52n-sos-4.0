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

import org.n52.sos.ogc.om.OMConstants;

public class ProfileConstants {

    public enum XmlElements {
        profile, identifier, defaultProfile, observationResponseFormat, encodeFeatureOfInterestInObservations, encodingNamespaceForFeatureOfInterestEncoding, showMetadataOfEmptyObservations, allowSubsettingForSOS20OM20, mergeValues, encodeProcedureInObservation, namespace, encode
    }

    public static final String DEFAULT_IDENTIFIER = "SOS_20_PROFILE";

    public static final String DEFAULT_OBSERVATION_RESPONSE_FORMAT = OMConstants.NS_OM_2;

    public static final String DEFAULT_ENCODING_NAMESPACE_FOR_FEATUTREOFINTEREST_SOS_20 = "";

    public static final boolean DEFAULT_ENCODE_FEATUREOFINTEREST_IN_OBSERVATION = true;

    public static final boolean DEFAULT_SHOW_METADATA_OF_EMPTY_OBSERVATIONS = false;

    public static final boolean DEFAULT_ALLOW_SUBSETTING_FOR_OM_20 = false;

    public static final boolean DEAFULT_MERGE_VALUES = false;

}
