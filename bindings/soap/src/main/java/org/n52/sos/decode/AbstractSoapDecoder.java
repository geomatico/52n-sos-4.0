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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.sos.exception.swes.InvalidRequestException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.CodingRepository;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.service.SoapHeader;
import org.n52.sos.soap.SoapRequest;
import org.n52.sos.util.LinkedListMultiMap;
import org.n52.sos.util.ListMultiMap;
import org.n52.sos.util.W3cHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.n52.sos.wsa.WsaConstants;
import org.n52.sos.wsa.WsaHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class AbstractSoapDecoder implements Decoder<SoapRequest, XmlObject> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSoapDecoder.class);
    private final Set<DecoderKey> decoderKeys;

    public AbstractSoapDecoder(String namespace) {
        this.decoderKeys = Collections.<DecoderKey>singleton(new XmlNamespaceDecoderKey(namespace, XmlObject.class));
    }

    @Override
    public Set<DecoderKey> getDecoderKeyTypes() {
        return Collections.unmodifiableSet(decoderKeys);
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
    public SoapRequest decode(XmlObject xmlObject) throws OwsExceptionReport {
        try {
            return createEnvelope(xmlObject);
        } catch (OwsExceptionReport owse) {
            return createFault(owse);
        }
    }

    protected abstract SoapRequest createEnvelope(XmlObject xml) throws OwsExceptionReport;

    protected abstract SoapRequest createFault(OwsExceptionReport xml);

    /**
     * Parses the SOAPBody content to a text representation
     *
     * @param message SOAP message
     *
     * @return SOAPBody content as text
     *
     *
     * @throws OwsExceptionReport * if an error occurs.
     */
    protected XmlObject getSOAPBodyContent(SOAPMessage message) throws OwsExceptionReport {
        try {
            Document bodyRequestDoc = message.getSOAPBody().extractContentAsDocument();
            XmlOptions options = XmlOptionsHelper.getInstance().getXmlOptions();
            String xmlString = W3cHelper.nodeToXmlString(bodyRequestDoc.getDocumentElement());
            return XmlObject.Factory.parse(xmlString, options);
        } catch (SOAPException soape) {
            throw new InvalidRequestException().causedBy(soape)
                    .withMessage("Error while parsing SOAPMessage body content!");
        } catch (XmlException xmle) {
            throw new InvalidRequestException().causedBy(xmle)
                    .withMessage("Error while parsing SOAPMessage body content!");
        }
    }

    protected Map<String, SoapHeader> getSoapHeader(SOAPHeader soapHeader) {
        ListMultiMap<String, SOAPHeaderElement> headersByNamespace = new LinkedListMultiMap<String, SOAPHeaderElement>();
        Iterator<?> headerElements = soapHeader.extractAllHeaderElements();
        while (headerElements.hasNext()) {
            SOAPHeaderElement element = (SOAPHeaderElement) headerElements.next();
            headersByNamespace.add(element.getNamespaceURI(), element);
        }
        Map<String, SoapHeader> soapHeaders = new HashMap<String, SoapHeader>();
        for (String namespace : headersByNamespace.keySet()) {
            try {
                Decoder<?, List<SOAPHeaderElement>> decoder = CodingRepository.getInstance().getDecoder(
                		new NamespaceDecoderKey(namespace, SOAPHeaderElement.class));
                if (decoder != null) {
                    Object header = decoder.decode(headersByNamespace.get(namespace));
                    if (header instanceof SoapHeader) {
                        soapHeaders.put(namespace, (SoapHeader) header);
                    }
                } else {
                    LOGGER.info("The SOAP-Header elements for namespace '{}' are not supported by this server!",
                                namespace);
                }
            } catch (OwsExceptionReport owse) {
                LOGGER.debug("Requested SOAPHeader element is not supported", owse);
            }
        }
        return soapHeaders;
    }

    protected String checkSoapAction(String soapAction, Map<String, SoapHeader> soapHeader) {
        if (soapAction != null && !soapAction.isEmpty()) {
            return soapAction;
        } else if (soapHeader != null && soapHeader.containsKey(WsaConstants.NS_WSA)) {
            return ((WsaHeader) soapHeader.get(WsaConstants.NS_WSA)).getActionValue();
        }
        return null;
    }
}
