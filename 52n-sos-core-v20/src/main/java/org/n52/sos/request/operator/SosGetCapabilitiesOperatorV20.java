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
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ds.AbstractGetCapabilitiesDAO;
import org.n52.sos.encode.Encoder;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.GetCapabilitiesRequest;
import org.n52.sos.response.GetCapabilitiesResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.n52.sos.wsdl.WSDLConstants;
import org.n52.sos.wsdl.WSDLOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosGetCapabilitiesOperatorV20 extends AbstractV2RequestOperator<AbstractGetCapabilitiesDAO, GetCapabilitiesRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SosGetCapabilitiesOperatorV20.class);
    private static final String OPERATION_NAME = SosConstants.Operations.GetCapabilities.name();
    private static final Set<String> CONFORMANCE_CLASSES = Collections.singleton(ConformanceClasses.SOS_V2_CORE_PROFILE);

    public SosGetCapabilitiesOperatorV20() {
        super(OPERATION_NAME, GetCapabilitiesRequest.class);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }

    @Override
    public ServiceResponse receive(GetCapabilitiesRequest sosRequest) throws OwsExceptionReport {
        /*
         * getting parameter acceptFormats (optional) boolean zipCompr shows
         * whether the response format should be zip (true) or xml (false)
         */
        boolean zipCompr = false;
        List<String> acceptFormats = sosRequest.getAcceptFormats();
        if (acceptFormats != null) {
            zipCompr = checkAcceptFormats(acceptFormats);
        }

        GetCapabilitiesResponse response = getDao().getCapabilities(sosRequest);
        String contentType = SosConstants.CONTENT_TYPE_XML;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // XmlOptions xmlOptions;
        try {
            Encoder<?,GetCapabilitiesResponse> encoder = Configurator.getInstance().getCodingRepository()
                    .getEncoder(CodingHelper.getEncoderKey(Sos2Constants.NS_SOS_20, response));
            if (encoder != null) {
                Object encodedObject = encoder.encode(response);
                if (encodedObject instanceof XmlObject) {
                    ((XmlObject) encodedObject).save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
                    return new ServiceResponse(baos, contentType, zipCompr, true);
                } else if (encodedObject instanceof ServiceResponse) {
                    return (ServiceResponse) encodedObject;
                } else {
                    String exceptionText = "The encoder response is not supported!";
                    throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                }
            } else {
                String exceptionText = "Received version in request is not supported!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(
                        OWSConstants.RequestParams.version.name(), exceptionText);
            }

        } catch (IOException ioe) {
            String exceptionText = "Error occurs while saving response to output stream!";
            LOGGER.error(exceptionText, ioe);
            throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
        }
    }

    private boolean checkAcceptFormats(List<String> formats) throws OwsExceptionReport {
        boolean zipCompr = false;

        // ints are necessary for getting the priority of the ouptuformats
        int xml = -1;
        int zip = -1;
        for (String format : formats) {
            if (format.equals(SosConstants.CONTENT_TYPE_XML)) {
                xml = formats.indexOf(format);
            } else if (format.equals(SosConstants.CONTENT_TYPE_ZIP)) {
                zip = formats.indexOf(format);
            }
        }
        if (zip == -1 && xml == -1) {
            String exceptionText =
                    "The parameter '" + SosConstants.GetCapabilitiesParams.AcceptFormats.name() + "'"
                            + " is invalid. The following values are supported: " + SosConstants.CONTENT_TYPE_XML
                            + ", " + SosConstants.CONTENT_TYPE_ZIP;
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(
                    SosConstants.GetCapabilitiesParams.AcceptFormats.name(), exceptionText);
        }

        // if zip is requested testing, whether the priority is bigger than xml
        if (zip != -1 && (zip <= xml || xml == -1)) {
            zipCompr = true;
        }

        return zipCompr;
    }

    @Override
    public WSDLOperation getSosOperationDefinition() {
        return WSDLConstants.Operations.GET_CAPABILITIES;
    }
}
