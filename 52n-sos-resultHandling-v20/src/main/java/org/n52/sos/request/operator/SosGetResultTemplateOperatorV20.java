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
package org.n52.sos.request.operator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ds.IGetResultTemplateDAO;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.GetResultTemplateRequest;
import org.n52.sos.response.GetResultTemplateResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.n52.sos.wsdl.WSDLOperation;
import org.n52.sos.wsdl.WSDLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosGetResultTemplateOperatorV20 extends AbstractV2RequestOperator<IGetResultTemplateDAO, GetResultTemplateRequest> {

    private static final Set<String> CONFORMANCE_CLASSES = Collections.singleton(ConformanceClasses.SOS_V2_RESULT_RETRIEVAL);
    private static final String OPERATION_NAME = Sos2Constants.Operations.GetResultTemplate.name();
    private static final Logger LOGGER = LoggerFactory.getLogger(SosInsertResultOperatorV20.class);

    public SosGetResultTemplateOperatorV20() {
        super(OPERATION_NAME, GetResultTemplateRequest.class);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }

    @Override
    public ServiceResponse receive(GetResultTemplateRequest sosRequest) throws OwsExceptionReport {
        checkRequestedParameter(sosRequest);
        GetResultTemplateResponse response = getDao().getResultTemplate(sosRequest);
        String contentType = SosConstants.CONTENT_TYPE_XML;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            IEncoder<?, GetResultTemplateResponse> encoder = Configurator.getInstance().getCodingRepository()
                    .getEncoder(CodingHelper.getEncoderKey(Sos2Constants.NS_SOS_20, response));
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
                String exceptionText = "Error while getting encoder for response!";
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }
        } catch (IOException ioe) {
            String exceptionText = "Error occurs while saving response to output stream!";
            LOGGER.error(exceptionText, ioe);
            throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
        }
    }

    private void checkRequestedParameter(GetResultTemplateRequest request) throws OwsExceptionReport {
        List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
        try {
            checkServiceParameter(request.getService());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            checkSingleVersionParameter(request);
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            checkOffering(request.getOffering());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            checkObservedProperty(request.getObservedProperty());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        Util4Exceptions.mergeAndThrowExceptions(exceptions);
    }


    private void checkOffering(String offering) throws OwsExceptionReport {
        if (offering == null || offering.isEmpty()) {
            throw Util4Exceptions.createMissingParameterValueException(Sos2Constants.GetResultTemplateParams.offering.name());
        } else if (!Configurator.getInstance().getCache().getOfferings().contains(offering)) {
            String exceptionText = String.format("The requested offering (%s) is not supported by this server!", offering);
            throw Util4Exceptions.createInvalidParameterValueException(Sos2Constants.GetResultTemplateParams.offering.name(), exceptionText);
        }
    }


    private void checkObservedProperty(String observedProperty) throws OwsExceptionReport {
        if (observedProperty == null || observedProperty.isEmpty()) {
            throw Util4Exceptions.createMissingParameterValueException(Sos2Constants.GetResultTemplateParams.observedProperty.name());
        } else if (!Configurator.getInstance().getCache().getObservableProperties().contains(observedProperty)) {
            String exceptionText = String.format("The requested observedProperty (%s) is not supported by this server!", observedProperty);
            throw Util4Exceptions.createInvalidParameterValueException(Sos2Constants.GetResultTemplateParams.observedProperty.name(), exceptionText);
        }
    }
    
    @Override
    public WSDLOperation getSosOperationDefinition() {
        return WSDLConstants.Operations.GET_RESULT_TEMPLATE;
    }

}
