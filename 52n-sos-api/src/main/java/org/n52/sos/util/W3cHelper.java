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

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class W3cHelper {

    /** the logger, used to log exceptions and additonaly information */
    private static Logger LOGGER = LoggerFactory.getLogger(W3cHelper.class);

    /**
     * Parses w3c.Node to String
     * 
     * @param node
     *            Node to parse.
     * 
     * @return Node as String.
     * @throws OwsExceptionReport
     *             if an error occurs.
     */
    public static String nodeToXmlString(Node node) throws OwsExceptionReport {
        String xmlString = "";
        StringWriter sw = null;
        try {
            sw = new StringWriter();
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            t.transform(new DOMSource(node), new StreamResult(sw));
            xmlString = sw.toString();
        } catch (TransformerException te) {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.NoApplicableCode, null,
                    "The request was sent in an unknown format or is invalid!!");
            LOGGER.error("nodeToString Transformer Exception", te);
            throw se;
        } finally {
            try {
                if (sw != null) {
                    sw.close();
                }
            } catch (IOException ioe) {
                LOGGER.error("cannot close string writer", ioe);
            }
        }
        return xmlString;
    }

    /**
     * Get text content from element by namespace.
     * 
     * @param soapHeader
     *            SOAPHeader element
     * @param namespaceURI
     *            Namespace URI
     * @param localName
     *            local name
     * @return Text content.
     */
    public static String getContentFromElement(Element element, String namespaceURI, String localName) {
        String elementContent = null;
        NodeList nodes = element.getElementsByTagNameNS(namespaceURI, localName);
        for (int i = 0; i < nodes.getLength(); i++) {
            elementContent = nodes.item(i).getTextContent();
        }
        return elementContent;
    }

    private W3cHelper() {
    }
}
