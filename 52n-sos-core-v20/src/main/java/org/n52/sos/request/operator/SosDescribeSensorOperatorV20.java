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
package org.n52.sos.request.operator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ds.IDescribeSensorDAO;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.DescribeSensorRequest;
import org.n52.sos.response.DescribeSensorResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class handles the DescribeSensor request
 * 
 */
public class SosDescribeSensorOperatorV20 implements IRequestOperator {

    /** the data access object for the DescribeSensor operation */
    private IDescribeSensorDAO dao;

    /** Name of the operation the listener implements */
    private static final String OPERATION_NAME = SosConstants.Operations.DescribeSensor.name();

    private RequestOperatorKeyType requestOperatorKeyType;

    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosDescribeSensorOperatorV20.class);

    /**
     * Constructor
     * 
     */
    public SosDescribeSensorOperatorV20() {
        requestOperatorKeyType =
                new RequestOperatorKeyType(new ServiceOperatorKeyType(SosConstants.SOS, Sos2Constants.SERVICEVERSION),
                        OPERATION_NAME);
        this.dao = (IDescribeSensorDAO) Configurator.getInstance().getOperationDAOs().get(OPERATION_NAME);
        LOGGER.info("SosDescribeSensorOperatorV20 initialized successfully!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.listener.ISosRequestListener#receiveRequest(org.n52.sos.request
     * .AbstractSosRequest)
     */
    @Override
    public synchronized ServiceResponse receiveRequest(AbstractServiceRequest request) throws OwsExceptionReport {

        boolean applyZIPcomp = false;

        if (request instanceof DescribeSensorRequest) {
            DescribeSensorRequest sosRequest = (DescribeSensorRequest) request;
            checkRequestedParameters(sosRequest);
            if (sosRequest.getOutputFormat().equals(SosConstants.CONTENT_TYPE_ZIP)) {
                applyZIPcomp = true;
            }

            DescribeSensorResponse response = this.dao.getSensorDescription(sosRequest);
            String contentType = SosConstants.CONTENT_TYPE_XML;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try {
                String namespace;
                // check SOS version for response encoding
                if (response.getVersion().equals(Sos2Constants.SERVICEVERSION)) {
                    namespace = SWEConstants.NS_SWES_20;
                } else if (response.getVersion().equals(Sos1Constants.SERVICEVERSION)) {
                    namespace = SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL;
                    if (sosRequest.getOutputFormat().equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE)
                            || sosRequest.getOutputFormat().equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL)) {
                        contentType = SensorMLConstants.SENSORML_CONTENT_TYPE;
                    }
                } else {
                    String exceptionText = "Received version in request is not supported!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createInvalidParameterValueException(
                            OWSConstants.RequestParams.version.name(), exceptionText);
                }
                IEncoder encoder = Configurator.getInstance().getEncoder(namespace);
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
                    String parameterName = null;
                    if (sosRequest.getVersion().equals(Sos1Constants.SERVICEVERSION)) {
                        parameterName = Sos1Constants.DescribeSensorParams.outputFormat.name();
                    } else if (sosRequest.getVersion().equals(Sos2Constants.SERVICEVERSION)) {
                        parameterName = Sos2Constants.DescribeSensorParams.procedureDescriptionFormat.name();
                    }
                    StringBuilder exceptionText = new StringBuilder();
                    exceptionText.append("The value '");
                    exceptionText.append(sosRequest.getOutputFormat());
                    exceptionText.append("' of the outputFormat parameter is incorrect and has to be '");
                    exceptionText.append(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL);
                    exceptionText.append("' for the requested sensor!");
                    throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText.toString());
                }
            } catch (IOException ioe) {
                String exceptionText = "Error occurs while saving response to output stream!";
                LOGGER.error(exceptionText, ioe);
                throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
            }
        } else {
            String exceptionText = "Received request is not a SosDescribeSensorRequest!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createOperationNotSupportedException(request.getOperationName());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.listener.ISosRequestListener#hasImplementedDAO()
     */
    @Override
    public boolean hasImplementedDAO() {
        if (this.dao != null) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.listener.ISosRequestListener#getOperationMetadata(java.lang
     * .String, java.lang.Object)
     */
    @Override
    public OWSOperation getOperationMetadata(String service, String version, Object connection)
            throws OwsExceptionReport {
        return dao.getOperationsMetadata(service, version, connection);
    }
    
    @Override
    public IExtension getExtension(Object connection) throws OwsExceptionReport {
        return dao.getExtension(connection);
    }

    @Override
    public RequestOperatorKeyType getRequestOperatorKeyType() {
        return requestOperatorKeyType;
    }
    
    @Override
    public Set<String> getConformanceClasses() {
        Set<String> conformanceClasses = new HashSet<String>(0);
        if (hasImplementedDAO()) {
            conformanceClasses.add("http://www.opengis.net/spec/SOS/2.0/conf/core");
        }
        return conformanceClasses;
    }

    private void checkRequestedParameters(DescribeSensorRequest sosRequest) throws OwsExceptionReport {
        List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();
        try {
            SosHelper.checkProcedureID(sosRequest.getProcedure(), Configurator.getInstance().getCapabilitiesCacheController()
                    .getProcedures(), SosConstants.DescribeSensorParams.procedure.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            SosHelper.checkProcedureOutputFormat(sosRequest.getOutputFormat(),
                    Sos2Constants.DescribeSensorParams.procedureDescriptionFormat.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        if (sosRequest.getTime() != null && !sosRequest.getTime().isEmpty()) {
            String exceptionText = "The requested parameter is not supported by this server!";
            exceptions.add(Util4Exceptions.createOptionNotSupportedException(Sos2Constants.DescribeSensorParams.validTime.name(), exceptionText));
        }
        Util4Exceptions.mergeExceptions(exceptions);
    }

}