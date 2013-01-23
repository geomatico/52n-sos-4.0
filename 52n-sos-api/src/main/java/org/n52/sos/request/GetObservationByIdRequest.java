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

import java.util.List;

import javax.xml.namespace.QName;

import org.n52.sos.ogc.sos.SosConstants;

/**
 * SOS GetObservationById request
 * 
 */
public class GetObservationByIdRequest extends AbstractServiceRequest {

    /**
     * GetObservationById operation name
     */
    private final String operationName = SosConstants.Operations.GetObservationById.name();

    /**
     * SRS name
     */
    private String srsName;

    /**
     * Observation identifier
     */
    private List<String> observationIdentifier;

    /**
     * Response format
     */
    private String responseFormat;

    /**
     * Result model
     */
    private QName resultModel;

    /**
     * Response mode
     */
    private String responseMode;

    /**
     * Get observation identifier
     * 
     * @return observation identifier
     */
    public List<String> getObservationIdentifier() {
        return observationIdentifier;
    }

    /**
     * Set observation identifier
     * 
     * @param observationIdentifier
     *            observation identifier
     */
    public void setObservationIdentifier(List<String> observationIdentifier) {
        this.observationIdentifier = observationIdentifier;
    }

    /**
     * Get response format
     * 
     * @return response format
     */
    public String getResponseFormat() {
        return responseFormat;
    }

    /**
     * Set response format
     * 
     * @param responseFormat
     *            response format
     */
    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    /**
     * Get response mode
     * 
     * @return response mode
     */
    public String getResponseMode() {
        return responseMode;
    }

    /**
     * Set response mode
     * 
     * @param responseMode
     *            response mode
     */
    public void setResponseMode(String responseMode) {
        this.responseMode = responseMode;
    }

    /**
     * Get result model
     * 
     * @return result model
     */
    public QName getResultModel() {
        return resultModel;
    }

    /**
     * Set result model
     * 
     * @param resultModel
     *            result model
     */
    public void setResultModel(QName resultModel) {
        this.resultModel = resultModel;
    }

    /**
     * Get SRS name
     * 
     * @return SRS name
     */
    public String getSrsName() {
        return srsName;
    }

    /**
     * Set SRS name
     * 
     * @param srsName
     *            SRS name
     */
    public void setSrsName(String srsName) {
        this.srsName = srsName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.request.AbstractSosRequest#getOperationName()
     */
    @Override
    public String getOperationName() {
        return operationName;
    }

}
