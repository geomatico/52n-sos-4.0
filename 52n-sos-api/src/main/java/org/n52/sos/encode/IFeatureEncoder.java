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

package org.n52.sos.encode;

import java.util.Map;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;

/**
 * Interface, which offers method to encode features
 * 
 */
public interface IFeatureEncoder {

    /**
     * Creates a GetFeatureOfInterestResponse document from SOS feature.
     * 
     * @param sosAbstractFeatures
     *            SOS feature
     * @return GetFeatureOfInterestResponse
     * @throws OwsExceptionReport
     *             if an error occurs.
     */
    public XmlObject createGetFeatureOfInterestResponse(SosAbstractFeature sosAbstractFeatures)
            throws OwsExceptionReport;

    /**
     * Creates a XML representation of a SpatialSamplingFeature from a SOS
     * feature.
     * 
     * @param absFeature
     *            SOS feature
     * @return XML representation of a SpatialSamplingFeature
     * @throws OwsExceptionReport 
     */
    public XmlObject createFeature(SosAbstractFeature absFeature, String gmlID) throws OwsExceptionReport;

    /**
     * Create a XML representation of a SamplingFeaturecollection from SOS
     * features.
     * 
     * @param foiGmlIds
     *            SOS features
     * @return XML representation of a SamplingFeaturecollection
     * @throws OwsExceptionReport 
     */
    public XmlObject createFeatureCollection(Map<SosAbstractFeature, String> foiGmlIds, boolean forObservation) throws OwsExceptionReport;
    
}
