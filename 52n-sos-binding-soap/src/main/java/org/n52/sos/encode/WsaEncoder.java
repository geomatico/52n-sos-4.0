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
package org.n52.sos.encode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.wsa.WsaConstants;
import org.n52.sos.wsa.WsaHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WsaEncoder implements IEncoder<Map<QName, String>, WsaHeader> {

    /** the logger, used to log exceptions and additonaly information */
    private static Logger LOGGER = LoggerFactory.getLogger(WsaEncoder.class);

    private List<EncoderKeyType> encoderKeyTypes;
    
    private Map<SupportedTypeKey, Set<String>> supportedTypes;
    
    private Set<String> conformanceClasses;
    
    /**
     * constructor
     */
    public WsaEncoder() {
        super();
        supportedTypes = new HashMap<SupportedTypeKey, Set<String>>(0);
        conformanceClasses = new HashSet<String>(0);
        encoderKeyTypes = new ArrayList<EncoderKeyType>();
        encoderKeyTypes.add(new EncoderKeyType(WsaConstants.NS_WSA));
        StringBuilder logMsgBuilder = new StringBuilder();
        logMsgBuilder.append("Encoder for the following keys initialized successfully: ");
        for (EncoderKeyType encoderKeyType : encoderKeyTypes) {
            logMsgBuilder.append(encoderKeyType.toString());
            logMsgBuilder.append(", ");
        }
        logMsgBuilder.delete(logMsgBuilder.lastIndexOf(", "), logMsgBuilder.length());
        logMsgBuilder.append("!");
        LOGGER.info(logMsgBuilder.toString());
    }

    @Override
    public List<EncoderKeyType> getEncoderKeyType() {
        return encoderKeyTypes;
    }
    
    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return supportedTypes;
    }

    @Override
    public Set<String> getConformanceClasses() {
        return conformanceClasses;
    }

    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
    }
    
    
    @Override
    public String getContentType() {
        return "text/xml";
    }

    @Override
    public Map<QName, String> encode(WsaHeader response) throws OwsExceptionReport {
        return encode(response, null);
    }

    @Override
    public Map<QName, String> encode(WsaHeader response, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        Map<QName, String> wsaHeaderValues = new HashMap<QName, String>();
        if (response.getReplyToAddress() != null && !response.getReplyToAddress().isEmpty()) {
            wsaHeaderValues.put(WsaConstants.getQNameTo(), response.getReplyToAddress());
        }
        if (response.getMessageID() != null) {
            wsaHeaderValues.put(WsaConstants.getQNameRelatesTo(), response.getMessageID());
        }
        if (response.getActionValue() != null) {
            wsaHeaderValues.put(WsaConstants.getQNameAction(), response.getActionValue());
        }
        return wsaHeaderValues;
    }

}
