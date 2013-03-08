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
package org.n52.sos.service.admin.request.operator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.encode.EncoderKey;
import org.n52.sos.encode.Encoder;
import org.n52.sos.encode.XmlEncoderKey;
import org.n52.sos.exception.AdministratorException;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OWSOperationsMetadata;
import org.n52.sos.ogc.ows.OWSParameterValuePossibleValues;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosCapabilities;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.response.GetCapabilitiesResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.config.ConfigurationException;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.admin.AdministratorConstants.AdministratorParams;
import org.n52.sos.service.admin.request.AdminRequest;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosAdminRequestOperator implements IAdminRequestOperator {

    /*
     * To support full dynamic loading of a new JAR, the Tomcat context.xml file has to be modified.
     * Add the attribute 'reloadable="true"' to <Context>.
     * Or you have to reload the Webapp.
     * Maybe there are other solution: CLassLoader, ...
     */

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosAdminRequestOperator.class);

    private static final String KEY = "SOS";

    private static final String CONTENT_TYPE_PLAIN = "text/plain";

    public static final String REQUEST_GET_CAPABILITIES = "GetCapabilities";

    public static final String REQUEST_UPDATE = "Update";

    public static final String UPDATE_ENCODER = "Encoder";

    public static final String UPDATE_DECODER = "Decoder";

    public static final String UPDATE_OPERATIONS = "Operations";

    public static final String UPDATE_SERVICES = "Services";

    public static final String UPDATE_BINDINGS = "Bindings";

    public static final String UPDATE_CONFIGURATION = "Configuration";

    private static final List<String> PARAMETERS = CollectionHelper.list(UPDATE_BINDINGS,
        UPDATE_CONFIGURATION, UPDATE_DECODER, UPDATE_ENCODER, UPDATE_OPERATIONS, UPDATE_SERVICES);

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public ServiceResponse receiveRequest(AdminRequest request) throws AdministratorException, OwsExceptionReport {
        try {
            if (request.getRequest().equalsIgnoreCase(REQUEST_GET_CAPABILITIES)) {
                return createCapabilities();
            } else if (request.getRequest().equalsIgnoreCase(REQUEST_UPDATE)) {
                return handleUpdateRequest(request);
            } else {
                throw Util4Exceptions.createOperationNotSupportedException(request.getRequest());
            }
        } catch (ConfigurationException e) {
            throw new AdministratorException(e);
        }
    }

    private ServiceResponse handleUpdateRequest(AdminRequest request) throws ConfigurationException,
            OwsExceptionReport {
        String[] parameters = request.getParameters();
        if (parameters != null && parameters.length > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("The following resources are successful updated: ");
            List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>(0);
            for (String parameter : parameters) {
                if (parameter.equalsIgnoreCase(UPDATE_BINDINGS)) {
                    Configurator.getInstance().updateBindings();
                    builder.append("Bindings");
                } else if (parameter.equalsIgnoreCase(UPDATE_CONFIGURATION)) {
                    Configurator.getInstance().updateConfiguration();
                    builder.append("Configuration");
                } else if (parameter.equalsIgnoreCase(UPDATE_DECODER)) {
                    Configurator.getInstance().updateDecoder();
                    builder.append("Decoder");
                } else if (parameter.equalsIgnoreCase(UPDATE_ENCODER)) {
                    Configurator.getInstance().updateEncoder();
                    builder.append("Encoder");
                } else if (parameter.equalsIgnoreCase(UPDATE_OPERATIONS)) {
                    Configurator.getInstance().updateRequestOperator();
                    builder.append("Supported Operations");
                } else if (parameter.equalsIgnoreCase(UPDATE_SERVICES)) {
                    Configurator.getInstance().updateServiceOperators();
                    builder.append("Supported Services");
                } else {
                    String exceptionTex = "";
                    exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                            AdministratorParams.parameter.name(), exceptionTex));
                }
                builder.append(", ");
            }
            if (!exceptions.isEmpty()) {
                Util4Exceptions.mergeAndThrowExceptions(exceptions);
            }
            builder.delete(builder.lastIndexOf(", "), builder.length());
            return createServiceResponse(builder.toString());
        } else {
            throw Util4Exceptions.createMissingParameterValueException(AdministratorParams.parameter.name());
        }
    }

    private ServiceResponse createCapabilities() throws OwsExceptionReport {
        GetCapabilitiesResponse response = new GetCapabilitiesResponse();
        response.setService(SosConstants.SOS);
        SosCapabilities sosCapabilities = new SosCapabilities();
        OWSOperationsMetadata operationsMetadata = new OWSOperationsMetadata();
        List<OWSOperation> opsMetadata = new ArrayList<OWSOperation>(2);
        opsMetadata.add(getOpsMetadataForCapabilities());
        opsMetadata.add(getOpsMetadataForUpdate());
        operationsMetadata.setOperations(opsMetadata);
        operationsMetadata.addCommonValue(AdministratorParams.service.name(), new OWSParameterValuePossibleValues(KEY));
        sosCapabilities.setOperationsMetadata(operationsMetadata);
        response.setCapabilities(sosCapabilities);
        return createServiceResponse(response);
    }

    private OWSOperation getOpsMetadataForCapabilities() {
        OWSOperation opsMeta = new OWSOperation();
        opsMeta.setOperationName(REQUEST_GET_CAPABILITIES);
        opsMeta.setDcp(getDCP());
        opsMeta.addAnyParameterValue(AdministratorParams.parameter);
        return opsMeta;
    }

    private OWSOperation getOpsMetadataForUpdate() {
        OWSOperation opsMeta = new OWSOperation();
        opsMeta.setOperationName(REQUEST_UPDATE);
        opsMeta.setDcp(getDCP());
        opsMeta.addPossibleValuesParameter(AdministratorParams.parameter, PARAMETERS);
        return opsMeta;
    }

    private Map<String, List<String>> getDCP() {
        return Collections.singletonMap(SosConstants.HTTP_GET,
                Collections.singletonList(Configurator.getInstance().getServiceURL() + "/admin?"));
    }

    private ServiceResponse createServiceResponse(String string) throws OwsExceptionReport {
        String contentType = CONTENT_TYPE_PLAIN;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(string.getBytes());
            return new ServiceResponse(baos, contentType, false, true);
        } catch (IOException e) {
            String exceptionText = "Error occurs while saving response to output stream!";
            LOGGER.error(exceptionText, e);
            throw Util4Exceptions.createNoApplicableCodeException(e, exceptionText);
        }
    }

    private ServiceResponse createServiceResponse(GetCapabilitiesResponse response) throws OwsExceptionReport {
        String contentType = SosConstants.CONTENT_TYPE_XML;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            EncoderKey key = new XmlEncoderKey(Sos2Constants.NS_SOS_20, GetCapabilitiesResponse.class);
            Encoder<?, GetCapabilitiesResponse>  encoder = Configurator.getInstance().getCodingRepository().getEncoder(key);
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
                String exceptionText = "Received version in request is not supported!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(OWSConstants.RequestParams.version.name(),
                        exceptionText);
            }

        } catch (IOException ioe) {
            String exceptionText = "Error occurs while saving response to output stream!";
            LOGGER.error(exceptionText, ioe);
            throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
        }
    }

}
