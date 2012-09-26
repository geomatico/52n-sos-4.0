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
package org.n52.sos.decode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.soap.Node;
import javax.xml.soap.SOAPHeaderElement;

import org.apache.log4j.Logger;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.wsa.WsaConstants;
import org.n52.sos.wsa.WsaHeader;

public class WsaDecoder implements IDecoder<WsaHeader, List<SOAPHeaderElement>> {

    /** the logger, used to log exceptions and additonaly information */
    private static Logger LOGGER = Logger.getLogger(WsaDecoder.class);

    private List<DecoderKeyType> decoderKeyTypes;

    /**
     * constructor
     */
    public WsaDecoder() {
        super();
        decoderKeyTypes = new ArrayList<DecoderKeyType>();
        decoderKeyTypes.add(new DecoderKeyType(WsaConstants.NS_WSA));
        StringBuilder builder = new StringBuilder();
        for (DecoderKeyType decoderKeyType : decoderKeyTypes) {
            builder.append(decoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        LOGGER.debug("Decoder for the following keys initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<DecoderKeyType> getDecoderKeyTypes() {
        return decoderKeyTypes;
    }

    public WsaHeader decode(List<SOAPHeaderElement> list) {
        WsaHeader wsaHeaderRequest = new WsaHeader();
        for (SOAPHeaderElement soapHeaderElement : list) {
            if (soapHeaderElement.getLocalName().equals(WsaConstants.EN_TO)) {
                wsaHeaderRequest.setToValue(soapHeaderElement.getValue());
            } else if (soapHeaderElement.getLocalName().equals(WsaConstants.EN_ACTION)) {
                wsaHeaderRequest.setActionValue(soapHeaderElement.getValue());
            } else if (soapHeaderElement.getLocalName().equals(WsaConstants.EN_REPLYTO)) {
                Iterator<Node> iter = (Iterator<Node>) soapHeaderElement.getChildElements();
                while (iter.hasNext()) {
                    Node node = (Node) iter.next();
                    if (node.getLocalName() != null && node.getLocalName().equals(WsaConstants.EN_ADDRESS)) {
                        wsaHeaderRequest.setReplyToAddress(node.getValue());
                    }
                }
            } else if (soapHeaderElement.getLocalName().equals(WsaConstants.EN_MESSAGEID)) {
                wsaHeaderRequest.setMessageID(soapHeaderElement.getValue());
            }
        }
        if ((wsaHeaderRequest.getToValue() != null || wsaHeaderRequest.getReplyToAddress() != null || wsaHeaderRequest
                .getMessageID() != null) && wsaHeaderRequest.getActionValue() == null) {
            wsaHeaderRequest.setActionValue(WsaConstants.WSA_FAULT_ACTION);
        }
        return wsaHeaderRequest;
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return new HashMap<SupportedTypeKey, Set<String>>(0);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return new HashSet<String>(0);
    }

}
