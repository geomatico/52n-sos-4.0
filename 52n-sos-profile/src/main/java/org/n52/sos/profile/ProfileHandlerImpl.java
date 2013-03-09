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
package org.n52.sos.profile;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.config.ConfigurationException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.profile.DefaultProfile;
import org.n52.sos.service.profile.Profile;
import org.n52.sos.service.profile.ProfileHandler;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.x52North.sensorweb.sos.profile.SosProfileDocument;

public class ProfileHandlerImpl implements ProfileHandler {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileHandlerImpl.class);

    private Profile activeProfile;

    private Map<String, Profile> availableProfiles = new HashMap<String, Profile>(1);

    public ProfileHandlerImpl() throws ConfigurationException {
        setActiveProfile(new DefaultProfile());
        addAvailableProvile(getActiveProfile());
        try {
            loadProfiles();
        } catch (OwsExceptionReport e) {
            String exceptionText = "Error while loading profiles";
            LOGGER.error(exceptionText, e);
            throw new ConfigurationException(exceptionText, e);
        }
    }

    @Override
    public Profile getActiveProfile() {
        return activeProfile;
    }

    private void setActiveProfile(Profile profile) {
        this.activeProfile = profile;
        addAvailableProvile(profile);
    }

    private void addAvailableProvile(Profile profile) {
        if (availableProfiles.containsKey(profile.getIdentifier())) {
            LOGGER.warn("Profile with the identifier {} still exist! Existing profile is overwritten!",
                    profile.getIdentifier());
        }
        availableProfiles.put(profile.getIdentifier(), profile);
    }

    private void loadProfiles() throws OwsExceptionReport {
        IOFileFilter fileFilter = new WildcardFileFilter("*-profile.xml");
        File folder = FileUtils.toFile(ProfileHandlerImpl.class.getResource("/"));
        Collection<File> listFiles = FileUtils.listFiles(folder, fileFilter, DirectoryFileFilter.DIRECTORY);
        for (File file : listFiles) {
            XmlObject xmlDocument = XmlHelper.loadXmlDocumentFromFile(file);
            if (xmlDocument instanceof SosProfileDocument) {
                Profile profile = ProfileParser.parseSosProfile((SosProfileDocument) xmlDocument);
                addProfile(profile);
            }
        }
    }

    private void addProfile(Profile profile) {
        if (profile != null) {
            if (profile.isActiveProfile()) {
                setActiveProfile(profile);
            } else {
                addAvailableProvile(profile);
            }
        }
    }

    @Override
    public Map<String, Profile> getAvailableProfiles() {
        return availableProfiles;
    }

    @Override
    public boolean isSetActiveProfile() {
        return activeProfile != null;
    }

   
    
}
