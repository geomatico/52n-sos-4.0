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
package org.n52.sos.decode;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.soap.Node;
import javax.xml.soap.SOAPHeaderElement;

import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.StringHelper;
import org.n52.sos.wsa.WsaConstants;
import org.n52.sos.wsa.WsaHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WsaDecoder implements Decoder<WsaHeader, List<SOAPHeaderElement>> {

    private static Logger LOGGER = LoggerFactory.getLogger(WsaDecoder.class);

    private Set<DecoderKey> DECODER_KEYS = Collections.<DecoderKey>singleton(
            new NamespaceDecoderKey(WsaConstants.NS_WSA, SOAPHeaderElement.class));

    /**
     * constructor
     */
    public WsaDecoder() {
        LOGGER.debug("Decoder for the following keys initialized successfully: {}!", StringHelper.join(", ", DECODER_KEYS));
    }

    @Override
    public Set<DecoderKey> getDecoderKeyTypes() {
        return Collections.unmodifiableSet(DECODER_KEYS);
    }
    
     @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.emptySet();
    }

    @Override
    public WsaHeader decode(List<SOAPHeaderElement> list) {
        WsaHeader wsaHeaderRequest = new WsaHeader();
        for (SOAPHeaderElement soapHeaderElement : list) {
            if (soapHeaderElement.getLocalName().equals(WsaConstants.EN_TO)) {
                wsaHeaderRequest.setToValue(soapHeaderElement.getValue());
            } else if (soapHeaderElement.getLocalName().equals(WsaConstants.EN_ACTION)) {
                wsaHeaderRequest.setActionValue(soapHeaderElement.getValue());
            } else if (soapHeaderElement.getLocalName().equals(WsaConstants.EN_REPLYTO)) {
                Iterator<?> iter = soapHeaderElement.getChildElements();
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
}
