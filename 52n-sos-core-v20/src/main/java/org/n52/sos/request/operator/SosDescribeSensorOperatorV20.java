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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ds.IDescribeSensorDAO;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.SosSensorML;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.sos.SosConstants.Operations;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.SosDescribeSensorRequest;
import org.n52.sos.response.IServiceResponse;
import org.n52.sos.response.SosResponse;
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
    
    private static final String VERSION = Sos2Constants.SERVICEVERSION;

    private static final String SERVICE = SosConstants.SOS;

    private RequestOperatorKeyType requestOperatorKeyType;

    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosDescribeSensorOperatorV20.class);

    /**
     * Constructor
     * 
     */
    public SosDescribeSensorOperatorV20() {
        requestOperatorKeyType =
                new RequestOperatorKeyType(new ServiceOperatorKeyType(SERVICE, VERSION), OPERATION_NAME);
        this.dao = (IDescribeSensorDAO) Configurator.getInstance().getOperationDAOs().get(OPERATION_NAME);
        LOGGER.info("DescribeSensorListenerSOSv20 initialized successfully!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.listener.ISosRequestListener#receiveRequest(org.n52.sos.request
     * .AbstractSosRequest)
     */
    @Override
    public synchronized IServiceResponse receiveRequest(AbstractServiceRequest request) throws OwsExceptionReport {

        boolean applyZIPcomp = false;
        String version = "";

        if (request instanceof SosDescribeSensorRequest) {
            List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();
            SosDescribeSensorRequest descSensRequest = (SosDescribeSensorRequest) request;
            version = descSensRequest.getVersion();
            String procedureID = descSensRequest.getProcedure();
            String outputFormat = descSensRequest.getOutputFormat();
            try {
                SosHelper.checkProcedureID(procedureID, Configurator.getInstance().getCapsCacheController()
                        .getProcedures(), SosConstants.DescribeSensorParams.procedure.name());
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
            try {
                SosHelper.checkProcedureOutputFormat(outputFormat, Sos2Constants.DescribeSensorParams.procedureDescriptionFormat.name());
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
            Util4Exceptions.mergeExceptions(exceptions);

            if (outputFormat.equals(SosConstants.CONTENT_TYPE_ZIP)) {
                applyZIPcomp = true;
            }

            SosSensorML smlDesc = this.dao.getSensorMLDescription(descSensRequest);
            Collection<String> parentProcedureIds =
                    Configurator.getInstance().getCapsCacheController()
                            .getParentProcedures(descSensRequest.getProcedure(), false, false);
            Collection<String> childProcedureIds =
                    Configurator.getInstance().getCapsCacheController()
                            .getChildProcedures(descSensRequest.getProcedure(), false, false);
            Map<String, SosSensorML> childProcedures = new HashMap<String, SosSensorML>(childProcedureIds.size());
            if (childProcedureIds != null && childProcedureIds.size() > 0) {
                for (String childProcedureId : childProcedureIds) {
                    SosDescribeSensorRequest childRequest = new SosDescribeSensorRequest();
                    childRequest.setProcedures(childProcedureId);
                    childRequest.setOutputFormat(descSensRequest.getOutputFormat());
                    childRequest.setService(descSensRequest.getService());
                    childRequest.setVersion(descSensRequest.getVersion());
                    childRequest.setTime(descSensRequest.getTime());
                    childProcedures.put(childProcedureId, this.dao.getSensorMLDescription(childRequest));
                }
            }
            smlDesc.setParentProcedureIDs(parentProcedureIds);
            smlDesc.setChildProcedures(childProcedures);
            String contentType = SosConstants.CONTENT_TYPE_XML;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            contentType = SensorMLConstants.SENSORML_CONTENT_TYPE;
//            XmlOptions xmlOptions;

            try {
                String namespace;
                // check SOS version for response encoding
                if (outputFormat.equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL)) {
                    namespace = outputFormat;
//                    xmlOptions = SosXmlOptionsUtility.getInstance().getXmlOptions4Sos2Swe200();

                } else if (outputFormat.equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE)) {
                    namespace = SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL;
//                    xmlOptions = SosXmlOptionsUtility.getInstance().getXmlOptions();
                } else {
                    String exceptionText = "Received version in request is not supported!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createInvalidParameterValueException(
                            OWSConstants.RequestParams.version.name(), exceptionText);
                }
                IEncoder encoder = Configurator.getInstance().getEncoder(namespace);
                if (encoder != null) {
                    Map<HelperValues, String> additionalValues = new HashMap<HelperValues, String>();
                    additionalValues.put(HelperValues.VERSION, version);
                    additionalValues.put(HelperValues.OPERATION, Operations.DescribeSensor.name());
                    Object encodedObject = encoder.encode(smlDesc, additionalValues);
                    if (encodedObject instanceof XmlObject) {
                        ((XmlObject)encodedObject).save(baos,
                                XmlOptionsHelper.getInstance().getXmlOptions());
                        return new SosResponse(baos, contentType, false, version, true);
                    } else if (encodedObject instanceof IServiceResponse) {
                        return (IServiceResponse)encodedObject;
                    } else {
                        String exceptionText = "The encoder response is not supported!";
                        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                    }
                } else {
                    String parameterName = null;
                    if (version.equals(Sos1Constants.SERVICEVERSION)) {
                        parameterName = Sos1Constants.DescribeSensorParams.outputFormat.name();
                    } else if (version.equals(Sos2Constants.SERVICEVERSION)) {
                        parameterName = Sos2Constants.DescribeSensorParams.procedureDescriptionFormat.name();
                    }
                    String exceptionText =
                            "The value '" + outputFormat
                                    + "' of the outputFormat parameter is incorrect and has to be '"
                                    + SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL + "' for the requested sensor!";
                    throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
                }
            } catch (IOException ioe) {
                String exceptionText = "Error occurs while saving response to output stream!";
                LOGGER.error(exceptionText, ioe);
                throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
            }
        } else {
            String exceptionText = "Received request in DescribeSensorListener is not a SosDescribeSensorRequest!";
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
    public OWSOperation getOperationMetadata(String service, String version, Object connection) throws OwsExceptionReport {
        return dao.getOperationsMetadata(service, version, connection);
    }

    @Override
    public RequestOperatorKeyType getRequestOperatorKeyType() {
        return requestOperatorKeyType;
    }

}