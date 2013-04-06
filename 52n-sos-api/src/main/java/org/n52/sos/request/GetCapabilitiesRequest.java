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
package org.n52.sos.request;

import java.util.Collections;
import java.util.List;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;

/**
 * SOS GetCapabilities request
 *
 */
public class GetCapabilitiesRequest extends AbstractServiceRequest {

    /**
     * GetCapabilities operation name
     */
    private final String operationName = SosConstants.Operations.GetCapabilities.name();

    /**
     * Update sequence
     */
    private String updateSequence;

    /**
     * Accept versions list
     */
    private String[] acceptVersions;

    /**
     * Sections list
     */
    private List<String> sections;

    /**
     * Accept formats list
     */
    private List<String> acceptFormats;

    /**
     * Extensions list
     */
    private List<XmlObject> extensionArray;

    /**
     * constructor
     */
    public GetCapabilitiesRequest() {
        String notSet = SosConstants.PARAMETER_NOT_SET;
        setService(SosConstants.SOS);
        updateSequence = notSet;
        acceptVersions = null;
        sections = null;
        acceptFormats = null;
    }

    /**
     * Get accept Formats
     *
     * @return accept Formats
     */
    public List<String> getAcceptFormats() {
        return acceptFormats;
    }

    /**
     * Set accept Formats
     *
     * @param acceptFormats
     *            accept Formats
     */
    public void setAcceptFormats(List<String> acceptFormats) {
        this.acceptFormats = acceptFormats;
    }

    /**
     * Get accept versions
     *
     * @return accept versions
     */
    public String[] getAcceptVersions() {
        return acceptVersions;
    }

    /**
     * Set accept versions
     *
     * @param acceptVersions
     *            accept versions
     */
    public void setAcceptVersions(String[] acceptVersions) {
        this.acceptVersions = acceptVersions;
    }

    /**
     * Get sections
     *
     * @return sections
     */
    public List<String> getSections() {
        return sections;
    }

    /**
     * Set sections
     *
     * @param sections
     *            sections
     */
    public void setSections(List<String> sections) {
        this.sections = sections;
    }

    /**
     * Get update sequence
     *
     * @return update sequence
     */
    public String getUpdateSequence() {
        return updateSequence;
    }

    /**
     * Set update sequence
     *
     * @param updateSequence
     *            update sequence
     */
    public void setUpdateSequence(String updateSequence) {
        this.updateSequence = updateSequence;
    }

    /**
     * Set extensions
     *
     * @param extensionArray
     *            extensions
     */
    public void setExtensionArray(List<XmlObject> extensionArray) {
        this.extensionArray = extensionArray;
    }

    /**
     * Get extensions
     *
     * @return extensions
     */
    public List<XmlObject> getExtensionArray() {
        return extensionArray;
    }

    @Override
    public String getOperationName() {
        return operationName;
    }

	@Override
    public ServiceOperatorKeyType[] getServiceOperatorKeyType() {
        if (serviceOperatorKeyTypes == null) {
            if (acceptVersions != null && acceptVersions.length > 0) {
                serviceOperatorKeyTypes = new ServiceOperatorKeyType[acceptVersions.length];
                for (int i = 0; i < acceptVersions.length; i++) {
                    serviceOperatorKeyTypes[i] = new ServiceOperatorKeyType(getService(), acceptVersions[i]);
                }
            } else {
                serviceOperatorKeyTypes = new ServiceOperatorKeyType[1];
                setVersion(Collections.max(Configurator.getInstance().getServiceOperatorRepository().getSupportedVersions()));
                serviceOperatorKeyTypes[0] = new ServiceOperatorKeyType(getService(), getVersion());
            }
        }
        return serviceOperatorKeyTypes;
    }

    public boolean isSetAcceptFormats() {
        return acceptFormats != null && !acceptFormats.isEmpty();
    }

    public boolean isSetAcceptVersions() {
        return acceptVersions != null && acceptVersions.length > 0;
    }

}
