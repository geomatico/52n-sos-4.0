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
package org.n52.sos.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.xmlbeans.XmlOptions;
import org.n52.sos.encode.Encoder;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.service.CodingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SOS XML utility class
 * 
 */
public final class XmlOptionsHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlOptionsHelper.class);

    /**
     * Get INSTANCE from class with default character encoding UTF-8
     *
     * @return INSTANCE
     */
    public static XmlOptionsHelper getInstance() {
        return LazyHolder.INSTANCE;
    }
    private final ReentrantLock lock = new ReentrantLock();
    private XmlOptions xmlOptions;
    private String characterEncoding = "UTF-8";
    private boolean prettyPrint = true;

    /**
     * Initialize the XML options
     * 
     * @param characterEncoding
     *            Character encoding for XML documents
     */
    private XmlOptionsHelper() {
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
        for (Encoder<?, ?> encoder : CodingRepository.getInstance().getEncoders()) {
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
        if (this.xmlOptions == null) {
            this.lock.lock();
            try {
                if (xmlOptions == null) {
                    xmlOptions = new XmlOptions();
                    Map<String, String> prefixes = getPrefixMap();
                    xmlOptions.setSaveSuggestedPrefixes(prefixes);
                    xmlOptions.setSaveImplicitNamespaces(prefixes);
                    xmlOptions.setSaveAggressiveNamespaces();
                    if (prettyPrint) {
                        xmlOptions.setSavePrettyPrint();
                    }
                    xmlOptions.setSaveNamespacesFirst();
                    xmlOptions.setCharacterEncoding(characterEncoding);
                }
            } finally {
                this.lock.unlock();
            }
        }
        return xmlOptions;
    }

    /**
     * Cleanup, set XML options to null
     */
    public void cleanup() {
        xmlOptions = null;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.lock.lock();
        try {
            if (this.prettyPrint != prettyPrint) {
                setReload();
            }
            this.prettyPrint = prettyPrint;
        } finally {
            this.lock.unlock();
        }
    }

    public void setCharacterEncoding(String characterEncoding) {
        this.lock.lock();
        try {
            if (!this.characterEncoding.equals(characterEncoding)) {
                setReload();
            }
            this.characterEncoding = characterEncoding;
        } finally {
            this.lock.unlock();
        }
    }

    private void setReload() {
        this.lock.lock();
        try {
            this.xmlOptions = null;
        } finally {
            this.lock.unlock();
        }
    }

    private static class LazyHolder {
        private static final XmlOptionsHelper INSTANCE = new XmlOptionsHelper();
    }
}
