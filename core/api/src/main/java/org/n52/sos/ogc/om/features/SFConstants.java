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
package org.n52.sos.ogc.om.features;

import org.n52.sos.util.SchemaLocation;

/**
 * Constants class for SamplingFeature
 * 
 */
public interface SFConstants {

    // namespaces and schema lcations
    String NS_SA = "http://www.opengis.net/sampling/1.0";

    String NS_SA_PREFIX = "sa";

    String NS_SF = "http://www.opengis.net/sampling/2.0";

    String NS_SF_PREFIX = "sf";

    String NS_SAMS = "http://www.opengis.net/samplingSpatial/2.0";

    String NS_SAMS_PREFIX = "sams";

    String SCHEMA_LOCATION_URL_SA = "http://schemas.opengis.net/sampling/1.0.0/sampling.xsd";

    String SCHEMA_LOCATION_URL_SF = "http://schemas.opengis.net/sampling/2.0/samplingFeature.xsd";

    String SCHEMA_LOCATION_URL_SAMS =
            "http://schemas.opengis.net/samplingSpatial/2.0/spatialSamplingFeature.xsd";
    
    SchemaLocation SA_SCHEMA_LOCATION = new SchemaLocation(NS_SA, SCHEMA_LOCATION_URL_SA);
    
    SchemaLocation SF_SCHEMA_LOCATION = new SchemaLocation(NS_SF, SCHEMA_LOCATION_URL_SF);
    
    SchemaLocation SAMS_SCHEMA_LOCATION = new SchemaLocation(NS_SAMS, SCHEMA_LOCATION_URL_SAMS);
    
    // feature types
    String SAMPLING_FEAT_TYPE_SF_SAMPLING_FEATURE =
            "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingFeature";

    String SAMPLING_FEAT_TYPE_SF_SPATIAL_SAMPLING_FEATURE =
            "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SpatialSamplingFeature";

    String SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT =
            "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint";

    String SAMPLING_FEAT_TYPE_SF_SAMPLING_CURVE =
            "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingCurve";

    String SAMPLING_FEAT_TYPE_SF_SAMPLING_SURFACE =
            "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingSurface";

    String SAMPLING_FEAT_TYPE_SF_SAMPLING_SOLID =
            "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingSolid";

    String SAMPLING_FEAT_TYPE_SF_SAMPLING_SPECIMEN =
            "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingSpecimen";

    // element names
    String EN_SAMPLINGPOINT = "SamplingPoint";

    String EN_SAMPLINGSURFACE = "SamplingSurface";

    String EN_SAMPLINGCURVE = "SamplingCurve";

    String FT_SAMPLINGPOINT = NS_SA_PREFIX + ":" + EN_SAMPLINGPOINT;

    String FT_SAMPLINGSURFACE = NS_SA_PREFIX + ":" + EN_SAMPLINGSURFACE;

    String FT_SAMPLINGCURVE = NS_SA_PREFIX + ":" + EN_SAMPLINGCURVE;

}
