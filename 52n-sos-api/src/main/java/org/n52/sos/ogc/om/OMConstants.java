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
package org.n52.sos.ogc.om;

import javax.xml.namespace.QName;

/**
 * Class contains element names and namespaces used to encode the O&M responses.
 * 
 */
public class OMConstants {

    public static final String PARAMETER_NOT_SET = "PARAMETER_NOT_SET";

    // //////////////////////////////
    // namespaces and schema locations

    public static final String SCHEMA_LOCATION_OM = "http://schemas.opengis.net/om/1.0.0/om.xsd";

    public static final String SCHEMA_LOCATION_OM_CONSTRAINT =
            "http://schemas.opengis.net/om/1.0.0/extensions/observationSpecialization_constraint.xsd";

    public static final String SCHEMA_LOCATION_OM_2 = "http://schemas.opengis.net/om/2.0/observation.xsd";

    public static final String NS_OM = "http://www.opengis.net/om/1.0";

    public static final String NS_OM_2 = "http://www.opengis.net/om/2.0";

    public static final String NS_OM_PREFIX = "om";

    public static final String NS_GMD = "http://www.isotc211.org/2005/gmd";

    public static final String NS_GMD_PREFIX = "gmd";

    public static final String NS_WV = "http://www.n52.org/wv";

    // //////////////////////////////////////////////////////////////////////
    // other

    public static final String AN_ID = "id";

    public static final String CONTENT_TYPE_OM = "text/xml;subtype=\"om/1.0.0\"";

    public static final String CONTENT_TYPE_OM_2 = "text/xml;subtype=\"om/2.0.0\"";

    public static final String RESPONSE_FORMAT_OM = "http://www.opengis.net/om/1.0.0";

    public static final String RESPONSE_FORMAT_OM_2 = "http://www.opengis.net/om/2.0";

    // ///////////////////////////////////////////////////////////////////
    // names of elements in O&M documents
    public static final String EN_ASCII_BLOCK = "AsciiBlock";

    public static final String EN_ABSTRACT_DATA_GROUP = "_DataGroup";

    public static final String EN_ABSTRACT_DATA_QUALITY = "AbstractDQ_Element";

    public static final String EN_BOUNDED_BY = "boundedBy";

    public static final String EN_CATEGORY_OBSERVATION = "CategoryObservation";

    public static final String EN_COUNT_OBSERVATION = "CountObservation";

    public static final String EN_TEXT_OBSERVATION = "TextObservation";

    public static final String EN_TRUTH_OBSERVATION = "TruthObservation";

    public static final String EN_GEOMETRY_OBSERVATION = "GeometryObservation";

    public static final String EN_COMMON_OBSERVATION = "CommonObservation";

    public static final String EN_COMPOSITE_PHENOMENON = "CompositePhenomenon";

    public static final String EN_DATA_GROUP = "DataGroup";

    public static final String EN_DQ_QUAN_ATTR_ACC = "DQ_QuantitativeAttributeAccuracy";

    public static final String EN_DQ_NON_QUAN_ATTR_ACC = "DQ_NonQuantitativeAttributeAccuracy";

    public static final String EN_DQ_COMPL_COMM = "DQ_CompletenessCommission";

    public static final String EN_DQ_COMPL_OM = "DQ_CompletenessOmission";

    public static final String EN_FEATURE = "Feature";

    public static final String EN_FEATURE_COLLECTION = "FeatureCollection";

    public static final String EN_GEOREF_FEATURE = "GeoReferenceableFeature";

    public static final String EN_MEMBER = "member";

    public static final String EN_MEASUREMENT = "Measurement";

    public static final String EN_OBSERVED_PROPERTY = "observedProperty";

    public static final String EN_OBSERVATION_COLLECTION = "ObservationCollection";

    public static final String EN_OBSERVATION = "Observation";

    public static final String EN_PHENOMENON = "Phenomenon";

    public static final String EN_COMPOSITE_SURFACE = "CompositeSurface";

    public static final String EN_RESULT = "result";

    public static final String EN_WV_STATION = "WVStation";

    public static final String EN_TEMPORAL_OPS = "temporalOps";

    // /////////////////////////////////////////////////////////////////////////////////
    // other constants
    public static final String PHEN_SAMPLING_TIME = "http://www.opengis.net/def/property/OGC/0/SamplingTime";

    public static final String PHENOMENON_TIME = "http://www.opengis.net/def/property/OGC/0/PhenomenonTime";

    public static final String PHENOMENON_TIME_NAME = "phenomenonTime";

    public static final String PHEN_UOM_ISO8601 = "http://www.opengis.net/def/uom/ISO-8601/0/Gregorian";

    public static final String PHEN_FEATURE_OF_INTEREST =
            "http://www.opengis.net/def/property/OGC/0/FeatureOfInterest";

    public static final String EN_ABSTRACT_DATA_RECORD = "AbstractDataRecord";

    public static final String EN_SIMPLE_DATA_RECORD = "SimpleDataRecord";

    public static final String ATTR_SRS_NAME = "srsName";

    // observation types
    public static final String OBS_TYPE_MEASUREMENT =
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement";

    public static final String OBS_TYPE_CATEGORY_OBSERVATION =
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CategoryObservation";

    public static final String OBS_TYPE_COMPLEX_OBSERVATION =
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation";

    public static final String OBS_TYPE_COUNT_OBSERVATION =
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CountObservation";

    public static final String OBS_TYPE_GEOMETRY_OBSERVATION =
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_GeometryObservation";

    // no Definition in O&M and not in Lightweight Profile
    public static final String OBS_TYPE_TEXT_OBSERVATION =
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TextObservation";

    public static final String OBS_TYPE_TRUTH_OBSERVATION =
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TruthObservation";

    public static final String OBS_TYPE_OBSERVATION =
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation";

    public static final String OBS_TYPE_SWE_ARRAY_OBSERVATION =
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_SWEArrayObservation";

    public static final String OBS_RESULT_TYPE_OBSERVATION = "http://www.opengis.net/sensorML/2.0/DataArray";

    public static final String SAMPLING_FEAT_TYPE_UNKNOWN = "http://www.opengis.net/def/samplingFeatureType/unknown";

    // ////////////////////////////////////////////////////////
    // resultModel constants; not possible to use enum because of

    public static final QName RESULT_MODEL_MEASUREMENT = new QName(NS_OM, EN_MEASUREMENT, NS_OM_PREFIX);

    public static final QName RESULT_MODEL_GEOMETRY_OBSERVATION = new QName(NS_OM, EN_GEOMETRY_OBSERVATION,
            NS_OM_PREFIX);

    public static final QName RESULT_MODEL_CATEGORY_OBSERVATION = new QName(NS_OM, EN_CATEGORY_OBSERVATION,
            NS_OM_PREFIX);

    public static final QName RESULT_MODEL_OBSERVATION = new QName(NS_OM, EN_OBSERVATION, NS_OM_PREFIX);

    public static final QName RESULT_MODEL_COUNT_OBSERVATION = new QName(NS_OM, EN_COUNT_OBSERVATION, NS_OM_PREFIX);

    public static final QName RESULT_MODEL_TRUTH_OBSERVATION = new QName(NS_OM, EN_TRUTH_OBSERVATION, NS_OM_PREFIX);

    public static final QName RESULT_MODEL_TEXT_OBSERVATION = new QName(NS_OM, EN_TEXT_OBSERVATION, NS_OM_PREFIX);

    /**
     * Array of constants for result models.
     */
    private static final QName[] RESULT_MODELS = { RESULT_MODEL_OBSERVATION, RESULT_MODEL_MEASUREMENT,
            RESULT_MODEL_CATEGORY_OBSERVATION, RESULT_MODEL_GEOMETRY_OBSERVATION };

    /**
     * Hide utility constructor
     */
    private OMConstants() {
        super();
    }

    /**
     * Returns the supported result models.
     * 
     * @return result models
     */
    public static QName[] getResultModels() {
        return RESULT_MODELS;
    }
}
