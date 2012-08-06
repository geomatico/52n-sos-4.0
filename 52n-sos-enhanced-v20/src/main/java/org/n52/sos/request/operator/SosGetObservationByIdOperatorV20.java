/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.request.operator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ds.IGetObservationByIdDAO;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.ExceptionLevel;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.GetObservationByIdRequest;
import org.n52.sos.response.GetObservationByIdResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.OwsHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosGetObservationByIdOperatorV20 implements IRequestOperator {

    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosGetObservationByIdOperatorV20.class.getName());

    /** the data access object for the GetObservation operation */
    private IGetObservationByIdDAO dao;

    /** Name of the operation the listener implements */
    private static final String OPERATION_NAME = SosConstants.Operations.GetObservationById.name();

    private RequestOperatorKeyType requestOperatorKeyType;

    /**
     * Constructor
     * 
     */
    public SosGetObservationByIdOperatorV20() {
        requestOperatorKeyType =
                new RequestOperatorKeyType(new ServiceOperatorKeyType(SosConstants.SOS, Sos2Constants.SERVICEVERSION),
                        OPERATION_NAME);
        this.dao = (IGetObservationByIdDAO) Configurator.getInstance().getOperationDAOs().get(OPERATION_NAME);
        LOGGER.info("SosGetObservationByIdOperatorV20 initialized successfully!");
    }

    @Override
    public RequestOperatorKeyType getRequestOperatorKeyType() {
        return requestOperatorKeyType;
    }

    @Override
    public ServiceResponse receiveRequest(AbstractServiceRequest request) throws OwsExceptionReport {
        if (request instanceof GetObservationByIdRequest) {
            List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();
            GetObservationByIdRequest sosRequest = (GetObservationByIdRequest) request;
            // check parameters with variable content
            try {
                SosHelper.checkServiceParameter(sosRequest.getService());
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
            try {
                OwsHelper.checkSingleVersionParameter(sosRequest.getVersion(), Configurator.getInstance()
                        .getSupportedVersions());
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
            Util4Exceptions.mergeExceptions(exceptions);
            
            boolean zipCompression = false;
            if (sosRequest.getResponseFormat() == null || (sosRequest.getResponseFormat() != null && sosRequest.getResponseFormat().isEmpty())) {
                sosRequest.setResponseFormat(OMConstants.RESPONSE_FORMAT_OM_2);
            } else {
                zipCompression = SosHelper.checkResponseFormatForZipCompression(sosRequest.getResponseFormat());
                if (zipCompression) {
                    sosRequest.setResponseFormat(OMConstants.RESPONSE_FORMAT_OM_2); 
                }
            }

            GetObservationByIdResponse response = this.dao.getObservationById(sosRequest);
            String responseFormat = response.getResponseFormat();
            String contentType = SosConstants.CONTENT_TYPE_XML;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // XmlOptions xmlOptions;

            try {
                if (responseFormat == null) {
                    String exceptionText = "Missing responseFormat definition in GetObservationById response!";
                    LOGGER.error(exceptionText);
                    throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                }
                // check SOS version for response encoding
                StringBuilder namespace = new StringBuilder();
                // O&M 1.0.0
                if (responseFormat.equals(OMConstants.CONTENT_TYPE_OM)
                        || responseFormat.equals(OMConstants.RESPONSE_FORMAT_OM)) {
                    namespace.append(responseFormat);
                    // xmlOptions =
                    // SosXmlOptionsUtility.getInstance().getXmlOptions();
                    contentType = OMConstants.CONTENT_TYPE_OM;
                }
                // O&M 2.0 non SOS 1.0
                else if (!request.getVersion().equals(Sos2Constants.SERVICEVERSION)
                        && (responseFormat.equals(OMConstants.CONTENT_TYPE_OM_2) || responseFormat.equals(OMConstants.RESPONSE_FORMAT_OM_2))) {
                    namespace.append(responseFormat);
                    // xmlOptions =
                    // SosXmlOptionsUtility.getInstance().getXmlOptions4Sos2Swe200();
                    contentType = OMConstants.CONTENT_TYPE_OM_2;
                }
                // O&M 2.0 for SOS 2.0
                else if (request.getVersion().equals(Sos2Constants.SERVICEVERSION)
                        && responseFormat.equals(OMConstants.RESPONSE_FORMAT_OM_2)) {
                    namespace.append(Sos2Constants.NS_SOS_20);
                    // xmlOptions =
                    // SosXmlOptionsUtility.getInstance().getXmlOptions4Sos2Swe200();
                } else {
                    String exceptionText = "Received version in request is not supported!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createInvalidParameterValueException(
                            OWSConstants.RequestParams.version.name(), exceptionText);
                }
                IEncoder encoder = Configurator.getInstance().getEncoder(namespace.toString());
                if (encoder != null) {
                    Object encodedObject = encoder.encode(response);
                    if (encodedObject instanceof XmlObject) {
                        ((XmlObject) encodedObject).save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
                        return new ServiceResponse(baos, contentType, false, true);
                    } else if (encodedObject instanceof ServiceResponse) {
                        return (ServiceResponse) encodedObject;
                    } else {
                        String exceptionText = "The encoder response is not supported!";
                        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                    }
                } else {
                    OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
                    throw owse;
                }
            } catch (IOException ioe) {
                String exceptionText = "Error occurs while saving response to output stream!";
                LOGGER.error(exceptionText, ioe);
                throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
            }

        } else {
            String exceptionText =
                    "Received request is not a SosGetObservationByIdRequest!";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createOperationNotSupportedException(request.getOperationName());
        }
    }

    @Override
    public boolean hasImplementedDAO() {
        if (this.dao != null) {
            return true;
        }
        return false;
    }

    @Override
    public OWSOperation getOperationMetadata(String service, String version, Object connection)
            throws OwsExceptionReport {
        return dao.getOperationsMetadata(service, version, connection);
    }

}
