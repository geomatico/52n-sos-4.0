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

package org.n52.sos.util;

import java.util.Hashtable;
import java.util.Map;

import org.apache.xmlbeans.XmlOptions;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.filter.FilterConstants;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;

/**
 * SOS XML utility class
 * 
 */
public class XmlOptionsHelper {

    /**
     * instance
     */
    private static XmlOptionsHelper instance = null;

    /**
     * XML options
     */
    private XmlOptions xmlOptions;

    /**
     * Initialize the XML options
     * 
     * @param characterEncoding
     *            Character encoding for XML documents
     */
    private void initialize(String characterEncoding) {
        if (xmlOptions == null) {
            xmlOptions = new XmlOptions();
            Map<String, String> lPrefixMap = new Hashtable<String, String>();
            lPrefixMap.put(SensorMLConstants.NS_SML, SensorMLConstants.NS_SML_PREFIX);
            lPrefixMap.put(SWEConstants.NS_SWE, SWEConstants.NS_SWE_PREFIX);
            lPrefixMap.put(GMLConstants.NS_GML, GMLConstants.NS_GML_PREFIX);
            lPrefixMap.put(OGCConstants.NS_OGC, OGCConstants.NS_OGC_PREFIX);
            lPrefixMap.put(OMConstants.NS_OM, OMConstants.NS_OM_PREFIX);
            lPrefixMap.put(OWSConstants.NS_OWS, OWSConstants.NS_OWS_PREFIX);
            lPrefixMap.put(SFConstants.NS_SA, SFConstants.NS_SA_PREFIX);
            lPrefixMap.put(Sos1Constants.NS_SOS, SosConstants.NS_SOS_PREFIX);
            lPrefixMap.put(W3CConstants.NS_XLINK, W3CConstants.NS_XLINK_PREFIX);
            lPrefixMap.put(W3CConstants.NS_XSI, W3CConstants.NS_XSI_PREFIX);
            lPrefixMap.put(SWEConstants.NS_SWE_20, SWEConstants.NS_SWE_PREFIX);
            lPrefixMap.put(SWEConstants.NS_SWES_20, SWEConstants.NS_SWES_PREFIX);
            lPrefixMap.put(GMLConstants.NS_GML_32, GMLConstants.NS_GML_PREFIX);
            lPrefixMap.put(OMConstants.NS_OM_2, OMConstants.NS_OM_PREFIX);
            lPrefixMap.put(SFConstants.NS_SF, SFConstants.NS_SF_PREFIX);
            lPrefixMap.put(SFConstants.NS_SAMS, SFConstants.NS_SAMS_PREFIX);
            lPrefixMap.put(FilterConstants.NS_FES_2, FilterConstants.NS_FES_2_PREFIX);
            lPrefixMap.put(Sos2Constants.NS_SOS_20, SosConstants.NS_SOS_PREFIX);
            xmlOptions.setSaveSuggestedPrefixes(lPrefixMap);
            xmlOptions.setSaveImplicitNamespaces(lPrefixMap);
            xmlOptions.setSaveAggressiveNamespaces();
            xmlOptions.setSavePrettyPrint();
            xmlOptions.setSaveNamespacesFirst();
            xmlOptions.setCharacterEncoding(characterEncoding);
        }
    }

    /**
     * Get the XML options for SOS 1.0.0
     * 
     * @return SOS 1.0.0 XML options
     */
    public XmlOptions getXmlOptions() {
        return xmlOptions;
    }


    /**
     * Cleanup, set XML options to null
     */
    public void cleanup() {
        xmlOptions = null;
    }

    /**
     * Get instance from class with defined character encoding
     * 
     * @param characterEncoding
     *            Defined character encoding
     * @return instance
     */
    public static synchronized XmlOptionsHelper getInstance(String characterEncoding) {
        if (instance == null) {
            instance = new XmlOptionsHelper();
            instance.initialize(characterEncoding);
        }
        return instance;
    }

    /**
     * Get instance from class with default character encoding UTF-8
     * 
     * @return instance
     */
    public static synchronized XmlOptionsHelper getInstance() {
        if (instance == null) {
            instance = new XmlOptionsHelper();
            instance.initialize("UTF-8");
        }
        return instance;
    }

}
