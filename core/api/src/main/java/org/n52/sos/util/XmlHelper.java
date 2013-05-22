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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.swes.InvalidRequestException;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML utility class TODO add javadoc to public methods.
 */
public final class XmlHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlHelper.class);

    /**
     * Parse XML document from HTTP-Post request.
     * 
     * @param request
     *            HTTP-Post request
     * @return XML document
     * 
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    public static XmlObject parseXmlSosRequest(HttpServletRequest request) throws OwsExceptionReport {
        XmlObject doc;
        try {
            if (request.getParameterMap().isEmpty()) {
                String requestContent = StringHelper.convertStreamToString(request.getInputStream(), request.getCharacterEncoding());
                doc = XmlObject.Factory.parse(requestContent);
            } else {
                doc =
                        XmlObject.Factory.parse(SosHelper.parseHttpPostBodyWithParameter(request.getParameterNames(),
                                request.getParameterMap()));
            }
        } catch (XmlException xmle) {
            throw new NoApplicableCodeException().causedBy(xmle).withMessage(
                    "An xml error occured when parsing the request! Message: %s", xmle.getMessage());
        } catch (IOException ioe) {
            throw new NoApplicableCodeException().causedBy(ioe).withMessage(
                    "Error while reading request! Message: %s", ioe.getMessage());
        }
        // validateDocument(doc);
        return doc;
    }

    /**
     * Get element Node from NodeList.
     * 
     * @param nodeList
     *            NodeList.
     * @return Element Node
     */
    public static Node getNodeFromNodeList(NodeList nodeList) {
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    return nodeList.item(i);
                }
            }
        }
        return null;
    }

    /**
     * checks whether the XMLDocument is valid
     * 
     * @param doc
     *            the document which should be checked
     * 
     * @throws OwsExceptionReport
     *             * if the Document is not valid
     */
    public static boolean validateDocument(XmlObject doc) throws OwsExceptionReport {
        // Create an XmlOptions instance and set the error listener.
        LinkedList<XmlError> validationErrors = new LinkedList<XmlError>();
        XmlOptions validationOptions = new XmlOptions();
        validationOptions.setErrorListener(validationErrors);

        // Validate the GetCapabilitiesRequest XML document
        boolean isValid = doc.validate(validationOptions);

        // Create Exception with error message if the xml document is invalid
        if (!isValid) {

            String message = null;

            // getValidation error and throw service exception for the first
            // error
            Iterator<XmlError> iter = validationErrors.iterator();
            List<XmlError> shouldPassErrors = new LinkedList<XmlError>();
            List<XmlError> errors = new LinkedList<XmlError>();
            while (iter.hasNext()) {
                XmlError error = iter.next();
                boolean shouldPass = false;
                if (error instanceof XmlValidationError) {
                    for (LaxValidationCase lvc : LaxValidationCase.values()) {
                        if (lvc.shouldPass((XmlValidationError) error)) {
                            shouldPass = true;
                            break;
                        }
                    }
                }
                if (shouldPass) {
                    shouldPassErrors.add(error);
                } else {
                    errors.add(error);
                }
            }
            CompositeOwsException exceptions = new CompositeOwsException();
            for (XmlError error : errors) {

                // get name of the missing or invalid parameter
                message = error.getMessage();
                if (message != null) {

                    exceptions.add(new InvalidRequestException().at(message).withMessage(
                            "[XmlBeans validation error:] %s", message));

                    // TODO check if code can be used for validation of SOS
                    // 1.0.0 requests
                    // // check, if parameter is missing or value of parameter
                    // // is
                    // // invalid to ensure, that correct
                    // // exceptioncode in exception response is used
                    //
                    // // invalid parameter value
                    // if (message.startsWith("The value")) {
                    // exCode = OwsExceptionCode.InvalidParameterValue;
                    //
                    // // split message string to get attribute name
                    // String[] messAndAttribute = message.split("attribute '");
                    // if (messAndAttribute.length == 2) {
                    // parameterName = messAndAttribute[1].replace("'", "");
                    // }
                    // }
                    //
                    // // invalid enumeration value --> InvalidParameterValue
                    // else if
                    // (message.contains("not a valid enumeration value")) {
                    // exCode = OwsExceptionCode.InvalidParameterValue;
                    //
                    // // get attribute name
                    // String[] messAndAttribute = message.split(" ");
                    // parameterName = messAndAttribute[10];
                    // }
                    //
                    // // mandatory attribute is missing -->
                    // // missingParameterValue
                    // else if (message.startsWith("Expected attribute")) {
                    // exCode = OwsExceptionCode.MissingParameterValue;
                    //
                    // // get attribute name
                    // String[] messAndAttribute = message.split("attribute: ");
                    // if (messAndAttribute.length == 2) {
                    // String[] attrAndRest = messAndAttribute[1].split(" in");
                    // if (attrAndRest.length == 2) {
                    // parameterName = attrAndRest[0];
                    // }
                    // }
                    // }
                    //
                    // // mandatory element is missing -->
                    // // missingParameterValue
                    // else if (message.startsWith("Expected element")) {
                    // exCode = SwesExceptionCode.InvalidRequest;
                    //
                    // // get element name
                    // String[] messAndElements = message.split(" '");
                    // if (messAndElements.length >= 2) {
                    // String elements = messAndElements[1];
                    // if (elements.contains("offering")) {
                    // parameterName = "offering";
                    // } else if (elements.contains("observedProperty")) {
                    // parameterName = "observedProperty";
                    // } else if (elements.contains("responseFormat")) {
                    // parameterName = "responseFormat";
                    // } else if (elements.contains("procedure")) {
                    // parameterName = "procedure";
                    // } else if (elements.contains("featureOfInterest")) {
                    // parameterName = "featureOfInterest";
                    // } else {
                    // // TODO check if other elements are invalid
                    // }
                    // }
                    // }
                    // // invalidParameterValue
                    // else if (message.startsWith("Element")) {
                    // exCode = OwsExceptionCode.InvalidParameterValue;
                    //
                    // // get element name
                    // String[] messAndElements = message.split(" '");
                    // if (messAndElements.length >= 2) {
                    // String elements = messAndElements[1];
                    // if (elements.contains("offering")) {
                    // parameterName = "offering";
                    // } else if (elements.contains("observedProperty")) {
                    // parameterName = "observedProperty";
                    // } else if (elements.contains("responseFormat")) {
                    // parameterName = "responseFormat";
                    // } else if (elements.contains("procedure")) {
                    // parameterName = "procedure";
                    // } else if (elements.contains("featureOfInterest")) {
                    // parameterName = "featureOfInterest";
                    // } else {
                    // // TODO check if other elements are invalid
                    // }
                    // }
                    // } else {
                    // // create service exception
                    // OwsExceptionReport se = new OwsExceptionReport();
                    // se.addCodedException(SwesExceptionCode.InvalidRequest,
                    // message,
                    // "[XmlBeans validation error:] " + message);
                    // LOGGER.error("The request is invalid!", se);
                    // throw se;
                    // }
                    //
                    // // create service exception
                    // OwsExceptionReport se = new OwsExceptionReport();
                    // se.addCodedException(exCode, message,
                    // "[XmlBeans validation error:] " + message);
                    // LOGGER.error("The request is invalid!", se);
                    // throw se;
                }
            }
            exceptions.throwIfNotEmpty();
        }
        return isValid;
    }

    /**
     * Loads a XML document from File.
     * 
     * @param file
     *            File
     * @return XML document
     * 
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    public static XmlObject loadXmlDocumentFromFile(File file) throws OwsExceptionReport {
        InputStream is = null;
        try {
            is = FileIOHelper.loadInputStreamFromFile(file);
            return XmlObject.Factory.parse(is);
        } catch (XmlException xmle) {
            throw new NoApplicableCodeException().causedBy(xmle).withMessage("Error while parsing file %s!",
                    file.getName());
        } catch (IOException ioe) {
            throw new NoApplicableCodeException().causedBy(ioe).withMessage("Error while parsing file %s!",
                    file.getName());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {
                    LOGGER.warn("Error while closing the file " + file.getName() + " input stream!", ioe);
                }
            }
        }
    }

    /**
     * Recurse through a node and its children and make all gml:ids unique
     * 
     * @param node
     *            The root node
     */
    public static void makeGmlIdsUnique(Node node) {
        makeGmlIdsUnique(node, new HashMap<String, Integer>());
    }

    /**
     * Recurse through a node and its children and make all gml:ids unique
     * 
     * @param node
     *            The node to examine
     */
    public static void makeGmlIdsUnique(Node node, Map<String, Integer> foundIds) {
        // check this node's attributes
        NamedNodeMap attributes = node.getAttributes();
        String nodeNamespace = node.getNamespaceURI();
        if (attributes != null) {
            for (int i = 0, len = attributes.getLength(); i < len; i++) {
                Attr attr = (Attr) attributes.item(i);
                if (attr.getLocalName().equals(GMLConstants.AN_ID)) {
                    String attrNamespace = attr.getNamespaceURI();
                    if ((attrNamespace != null && GMLConstants.NS_GML.equals(attrNamespace) || GMLConstants.NS_GML_32
                            .equals(attrNamespace))
                            || (attrNamespace == null && nodeNamespace != null
                                    && GMLConstants.NS_GML.equals(nodeNamespace) || GMLConstants.NS_GML_32
                                        .equals(nodeNamespace)) || (attr.getName().equals("gml:id"))) {
                        String gmlId = attr.getValue();
                        if (foundIds.containsKey(gmlId)) {
                            // id has already been found, suffix this one with
                            // the
                            // found count for this id
                            attr.setValue(gmlId + foundIds.get(gmlId));
                            // increment the found count for this id
                            foundIds.put(gmlId, foundIds.get(gmlId) + 1);
                        } else {
                            // id is new, add it to the foundIds map
                            foundIds.put(gmlId, 1);
                        }
                    }
                }
            }
        }

        // recurse this node's children
        NodeList children = node.getChildNodes();
        if (children != null) {
            for (int i = 0, len = children.getLength(); i < len; i++) {
                makeGmlIdsUnique(children.item(i), foundIds);
            }
        }
    }

    public static String getNamespace(XmlObject doc) {
        String namespaceURI = doc.getDomNode().getNamespaceURI();
        if (namespaceURI == null) {
            namespaceURI = doc.getDomNode().getFirstChild().getNamespaceURI();
        }
        // if document starts with a comment, get next sibling (and ignore
        // initial comment)
        if (namespaceURI == null) {
            namespaceURI = doc.getDomNode().getFirstChild().getNextSibling().getNamespaceURI();
        }
        // check with schemaType namespace, necessary for anyType elements
        String schemaTypeNamespace = getSchemaTypeNamespace(doc);
        if (schemaTypeNamespace == null) {
            return namespaceURI;
        } else {
            if (schemaTypeNamespace.equals(namespaceURI)) {
                return namespaceURI;
            } else {
                return schemaTypeNamespace;
            }
        }

    }

    private static String getSchemaTypeNamespace(XmlObject doc) {
        QName name = doc.schemaType().getName();
        if (name != null) {
            return name.getNamespaceURI();
        }
        return null;
    }

    public static void updateGmlIDs(Node node, String gmlID, String oldGmlID) {
        // check this node's attributes
        String nodeNamespace = node.getNamespaceURI();
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            for (int i = 0, len = attributes.getLength(); i < len; i++) {
                Attr attr = (Attr) attributes.item(i);
                if (attr.getLocalName().equals(GMLConstants.AN_ID)) {
                    String attrNamespace = attr.getNamespaceURI();
                    if ((attrNamespace != null && GMLConstants.NS_GML.equals(attrNamespace) || GMLConstants.NS_GML_32
                            .equals(attrNamespace))
                            || (attrNamespace == null && nodeNamespace != null
                                    && GMLConstants.NS_GML.equals(nodeNamespace) || GMLConstants.NS_GML_32
                                        .equals(nodeNamespace)) || (attr.getName().equals("gml:id"))) {
                        if (oldGmlID == null) {
                            oldGmlID = attr.getValue();
                            attr.setValue((gmlID));
                        } else {
                            String helperString = attr.getValue();
                            helperString = helperString.replace(oldGmlID, gmlID);
                            attr.setValue(helperString);
                        }
                    }
                }
            }
            // recurse this node's children
            NodeList children = node.getChildNodes();
            if (children != null) {
                for (int i = 0, len = children.getLength(); i < len; i++) {
                    updateGmlIDs(children.item(i), gmlID, oldGmlID);
                }
            }
        }
    }

    public static XmlObject substituteElement(XmlObject elementToSubstitute, XmlObject substitutionElement) {
        Node domNode = substitutionElement.getDomNode();
        QName name = null;
        if (domNode.getNamespaceURI() != null && domNode.getLocalName() != null) {
            String prefix = getPrefixForNamespace(elementToSubstitute, domNode.getNamespaceURI());
            if (prefix != null && !prefix.isEmpty()) {
                name = new QName(domNode.getNamespaceURI(), domNode.getLocalName(), prefix);
            } else {
                name = new QName(domNode.getNamespaceURI(), domNode.getLocalName());
            }
        } else {
            QName nameOfElement = substitutionElement.schemaType().getName();
            String localPart = nameOfElement.getLocalPart().replace("Type", "");
            name =
                    new QName(nameOfElement.getNamespaceURI(), localPart, getPrefixForNamespace(elementToSubstitute,
                            nameOfElement.getNamespaceURI()));
        }
        return substituteElement(elementToSubstitute, substitutionElement.schemaType(), name);
    }

    public static String getPrefixForNamespace(XmlObject element, String namespace) {
        XmlCursor cursor = element.newCursor();
        String prefix = cursor.prefixForNamespace(namespace);
        cursor.dispose();
        return prefix;
    }

    public static XmlObject substituteElement(XmlObject elementToSubstitute, SchemaType schemaType, QName name) {
        return elementToSubstitute.substitute(name, schemaType);
    }

    public static String getLocalName(XmlObject element) {
        return (element == null) ? null : element.getDomNode().getLocalName();
    }

    private XmlHelper() {
    }

    /**
     * Interface for providing exceptional cases in XML validation (e.g.
     * substituation groups).
     */
    private enum LaxValidationCase {
        ABSTRACT_OFFERING {
            @Override
            public boolean shouldPass(XmlValidationError xve) {
                return xve.getFieldQName() != null
                        && xve.getExpectedQNames() != null
                        && xve.getFieldQName().equals(SWEConstants.QN_OFFERING)
                        && xve.getExpectedQNames().contains(SWEConstants.QN_ABSTRACT_OFFERING)
                        && (xve.getMessage().contains(BEFORE_END_CONTENT_ELEMENT) || (xve.getOffendingQName() != null && xve
                                .getOffendingQName().equals(Sos2Constants.QN_OBSERVATION_OFFERING)));
            }
        },
        /**
         * Allow substitutions of gml:AbstractFeature. This lax validation lets
         * pass every child, hence it checks not _if_ this is a valid
         * substitution.
         */
        ABSTRACT_FEATURE_GML {
            @Override
            public boolean shouldPass(XmlValidationError xve) {
                return xve.getExpectedQNames() != null
                        && (xve.getExpectedQNames().contains(GMLConstants.QN_ABSTRACT_FEATURE_GML) || xve
                                .getExpectedQNames().contains(GMLConstants.QN_ABSTRACT_FEATURE_GML_32));
            }
        },
        ABSTRACT_TIME_GML_3_2_1 {
            @Override
            public boolean shouldPass(XmlValidationError xve) {
                return xve.getExpectedQNames() != null
                        && xve.getExpectedQNames().contains(GMLConstants.QN_ABSTRACT_TIME_32);
            }
        },
        SOS_INSERTION_META_DATA {
            @Override
            public boolean shouldPass(XmlValidationError xve) {
                return xve.getFieldQName() != null
                        && xve.getExpectedQNames() != null
                        && xve.getFieldQName().equals(SWEConstants.QN_METADATA)
                        && xve.getExpectedQNames().contains(SWEConstants.QN_INSERTION_METADATA)
                        && (xve.getMessage().contains(BEFORE_END_CONTENT_ELEMENT) || (xve.getOffendingQName() != null && xve
                                .getOffendingQName().equals(Sos2Constants.QN_SOS_INSERTION_METADATA)));
            }
        };
        private static final String BEFORE_END_CONTENT_ELEMENT = "before the end of the content in element";

        public abstract boolean shouldPass(XmlValidationError xve);
    }

    /**
     * Utility method to append the contents of the child docment to the end of
     * the parent XmlObject. This is useful when dealing with elements without
     * generated methods (like elements with xs:any)
     * 
     * @param parent
     *            Parent to append contents to
     * @param childDoc
     *            Xml document containing contents to be appended
     */
    public static void append(XmlObject parent, XmlObject childDoc) {
        XmlCursor parentCursor = parent.newCursor();
        parentCursor.toEndToken();

        XmlCursor childCursor = childDoc.newCursor();
        childCursor.toFirstChild();

        childCursor.moveXml(parentCursor);
        parentCursor.dispose();
        childCursor.dispose();
    }

    /**
     * Remove namespace declarations from an xml fragment (useful for moving all
     * declarations to a document root
     * 
     * @param x
     *            The fragment to localize
     */
    public static void removeNamespaces(XmlObject x) {
        XmlCursor c = x.newCursor();
        while (c.hasNextToken()) {
            if (c.isNamespace()) {
                c.removeXml();
            } else {
                c.toNextToken();
            }
        }
        c.dispose();
    }
}
