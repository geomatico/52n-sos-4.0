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
package org.n52.sos.service.profile;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.ConfigurationException;
import org.n52.sos.service.profile.ProfileConstants.XmlElements;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProfileHandler {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileHandler.class);

    private Profile activeProfile;

    private Map<String, Profile> availableProfiles = new HashMap<String, Profile>(1);

    public ProfileHandler() throws ConfigurationException {
        setActiveProfile(new Profile());
        try {
            loadProfiles();
        } catch (OwsExceptionReport e) {
            String exceptionText = "Error while loading profiles";
            LOGGER.error(exceptionText, e);
            throw new ConfigurationException(exceptionText, e);
        }
        // addAvailableProvile(getActiveProfile());
    }

    public Profile getActiveProfile() {
        return activeProfile;
    }

    private void setActiveProfile(Profile profile) {
        this.activeProfile = profile;
        addAvailableProvile(profile);
    }

    private void addAvailableProvile(Profile profile) {
        if (availableProfiles.containsKey(profile.getIdentifier())) {
            LOGGER.warn("Profile with the identifier {} still exist! Exiting profile is overwritten!",
                    profile.getIdentifier());
        }
        availableProfiles.put(profile.getIdentifier(), profile);
    }

    private void loadProfiles() throws OwsExceptionReport {
        IOFileFilter fileFilter = new WildcardFileFilter("*.profile");
        File folder = FileUtils.toFile(ProfileHandler.class.getResource("/"));
        Collection<File> listFiles = FileUtils.listFiles(folder, fileFilter, DirectoryFileFilter.DIRECTORY);
        for (File file : listFiles) {
            XmlObject xmlDocument = XmlHelper.loadXmlDocumentFromFile(file);
            Node firstChild = xmlDocument.getDomNode().getFirstChild();
            if (checkNode(firstChild, XmlElements.profile.name())) {
                if (firstChild.hasChildNodes()) {
                    NodeList childNodes = firstChild.getChildNodes();
                    Profile profile = new Profile();
                    boolean isDefaultProfile = false;
                    for (int i = 0; i < childNodes.getLength(); i++) {
                        addValueToProfile(profile, childNodes.item(i));
                        if (checkNode(childNodes.item(i), XmlElements.defaultProfile.name())) {
                            isDefaultProfile = Boolean.valueOf(checkAndGetBoolean(getTextValue(childNodes.item(i))));
                        }
                    }
                    if (isDefaultProfile) {
                        setActiveProfile(profile);
                    } else {
                        addAvailableProvile(profile);
                    }
                }
            }
        }
    }

    private boolean checkNode(Node node, String name) {
        return node != null && node.getNodeName().equals(name);
    }

    private void addValueToProfile(Profile profile, Node node) {
        // TODO check for valid namespaces and for support
       if (checkNode(node, XmlElements.identifier.name())) {
           profile.setIdentifier(getTextValue(node));
       } else if (checkNode(node, XmlElements.observationResponseFormat.name())) {
           profile.setObservationResponseFormat(getTextValue(node));
       } else if (checkNode(node, XmlElements.encodeFeatureOfInterestInObservations.name())) {
           profile.setEncodeFeatureOfInterestInObservations(checkAndGetBoolean(getTextValue(node)));
       } else if (checkNode(node, XmlElements.encodingNamespaceForFeatureOfInterestEncoding.name())) {
           profile.setEncodingNamespaceForFeatureOfInterest(getTextValue(node));
       } else if (checkNode(node, XmlElements.showMetadataOfEmptyObservations.name())) {
           profile.setShowMetadataOfEmptyObservations(checkAndGetBoolean(getTextValue(node)));
       } else if (checkNode(node, XmlElements.allowSubsettingForSOS20OM20.name())) {
           profile.setAllowSubsettingForSOS20OM20(checkAndGetBoolean(getTextValue(node)));
       } else if (checkNode(node, XmlElements.mergeValues.name())) {
           profile.setMergeValues(checkAndGetBoolean(getTextValue(node)));
       } else if (checkNode(node, XmlElements.encodeProcedureInObservation.name())) {
           addValuesForEncodeProcedureInObservation(profile, node);
       } 
    }

    private String getTextValue(Node node) {
        if (node.hasChildNodes()) {
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                if (checkForTextNode(item)) {
                    return item.getNodeValue();
                }
            }
        }
        return "";
    }

    private boolean checkForTextNode(Node item) {
        if (item.getNodeType() == Node.TEXT_NODE) {
            return true;
        }
        return false;
    }

    private void addValuesForEncodeProcedureInObservation(Profile profile, Node node) {
        String namespace = null;
        Boolean encode = null;
        if (node.hasChildNodes()) {
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                if (checkNode(item, XmlElements.namespace.name())) {
                    namespace = getTextValue(item);
                } else if (checkNode(item, XmlElements.encode.name())) {
                    encode = Boolean.valueOf(checkAndGetBoolean(getTextValue(item)));
                } 
            }
        }
        if (namespace != null && !namespace.isEmpty() && encode != null) {
            Map<String, Boolean> encodeProcedureInObservation = new HashMap<String, Boolean>();
            encodeProcedureInObservation.put(namespace, encode);
            profile.setEncodeProcedureInObservation(encodeProcedureInObservation);
        }
    }

    private boolean checkAndGetBoolean(String textContent) {
        if (textContent != null && !textContent.isEmpty()) { 
            return Boolean.valueOf(textContent).booleanValue();
        }
        return false;
    }

   
    
}
