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
package org.n52.sos.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlOptions;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.service.Configurator;

/**
 * SOS XML utility class
 * 
 */
public class XmlOptionsHelper {
    private static XmlOptionsHelper instance;
    private XmlOptions xmlOptions;
    private String characterEncoding;

    /**
     * Initialize the XML options
     * 
     * @param characterEncoding
     *            Character encoding for XML documents
     */
    private XmlOptionsHelper(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    // TODO: To be used by other encoders to have common prefixes
    private Map<String, String> getPrefixMap() {
        Map<String, String> prefixMap = new HashMap<String, String>();
        prefixMap.put(OGCConstants.NS_OGC, OGCConstants.NS_OGC_PREFIX);
        prefixMap.put(OMConstants.NS_OM, OMConstants.NS_OM_PREFIX);
        prefixMap.put(SFConstants.NS_SA, SFConstants.NS_SA_PREFIX);
        prefixMap.put(Sos1Constants.NS_SOS, SosConstants.NS_SOS_PREFIX);
        prefixMap.put(W3CConstants.NS_XLINK, W3CConstants.NS_XLINK_PREFIX);
        prefixMap.put(W3CConstants.NS_XSI, W3CConstants.NS_XSI_PREFIX);
        prefixMap.put(W3CConstants.NS_XS, W3CConstants.NS_XS_PREFIX);
        for (IEncoder<?, ?> encoder : Configurator.getInstance().getCodingRepository().getEncoders()) {
            encoder.addNamespacePrefixToMap(prefixMap);
        }
        return prefixMap;
    }

    /**
     * Get the XML options for SOS 1.0.0
     * 
     * @return SOS 1.0.0 XML options
     */
    public XmlOptions getXmlOptions() {
        if (xmlOptions == null) {
            xmlOptions = new XmlOptions();
            Map<String, String> prefixes = getPrefixMap();
            xmlOptions.setSaveSuggestedPrefixes(prefixes);
            xmlOptions.setSaveImplicitNamespaces(prefixes);
            xmlOptions.setSaveAggressiveNamespaces();
            xmlOptions.setSavePrettyPrint();
            xmlOptions.setSaveNamespacesFirst();
            xmlOptions.setCharacterEncoding(characterEncoding);
        }
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
     * @param reload 
     * @return instance
     */
    public static synchronized XmlOptionsHelper getInstance(String characterEncoding, boolean reload) {
        return (instance == null || reload) ? instance = new XmlOptionsHelper(characterEncoding): instance;
    }

    /**
     * Get instance from class with default character encoding UTF-8
     * 
     * @return instance
     */
    public static synchronized XmlOptionsHelper getInstance() {
        return getInstance("UTF-8", false);
    }
}
