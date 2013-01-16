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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ds.IGetObservationByIdDAO;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.GetObservationByIdRequest;
import org.n52.sos.response.GetObservationByIdResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.OwsHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosGetObservationByIdOperatorV20 extends AbstractV2RequestOperator<IGetObservationByIdDAO, GetObservationByIdRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SosGetObservationByIdOperatorV20.class);
    private static final Set<String> CONFORMANCE_CLASSES = Collections.singleton(ConformanceClasses.SOS_V2_OBSERVATION_BY_ID_RETRIEVAL);
    private static final String OPERATION_NAME = SosConstants.Operations.GetObservationById.name();

    public SosGetObservationByIdOperatorV20() {
        super(OPERATION_NAME, GetObservationByIdRequest.class);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }

    @Override
    public ServiceResponse receive(GetObservationByIdRequest sosRequest) throws OwsExceptionReport {
        checkRequestedParameter(sosRequest);
        boolean zipCompression;
        if (sosRequest.getResponseFormat() == null || sosRequest.getResponseFormat().isEmpty()) {
            sosRequest.setResponseFormat(OMConstants.RESPONSE_FORMAT_OM_2);
        } else {
            zipCompression = SosHelper.checkResponseFormatForZipCompression(sosRequest.getResponseFormat());
            if (zipCompression) {
                sosRequest.setResponseFormat(OMConstants.RESPONSE_FORMAT_OM_2);
            }
        }

        GetObservationByIdResponse response = getDao().getObservationById(sosRequest);
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
            final String namespace;
            // O&M 1.0.0
            if (responseFormat.equals(OMConstants.CONTENT_TYPE_OM)
                    || responseFormat.equals(OMConstants.RESPONSE_FORMAT_OM)) {
                namespace = responseFormat;
                // xmlOptions =
                // SosXmlOptionsUtility.getInstance().getXmlOptions();
                contentType = OMConstants.CONTENT_TYPE_OM;
            }
            // O&M 2.0 non SOS 1.0
            else if (!sosRequest.getVersion().equals(Sos2Constants.SERVICEVERSION)
                    && (responseFormat.equals(OMConstants.CONTENT_TYPE_OM_2)
                        || responseFormat .equals(OMConstants.RESPONSE_FORMAT_OM_2))) {
                namespace = responseFormat;
                // xmlOptions =
                // SosXmlOptionsUtility.getInstance().getXmlOptions4Sos2Swe200();
                contentType = OMConstants.CONTENT_TYPE_OM_2;
            }
            // O&M 2.0 for SOS 2.0
            else if (sosRequest.getVersion().equals(Sos2Constants.SERVICEVERSION)
                    && responseFormat.equals(OMConstants.RESPONSE_FORMAT_OM_2)) {
                namespace = Sos2Constants.NS_SOS_20;
                // xmlOptions =
                // SosXmlOptionsUtility.getInstance().getXmlOptions4Sos2Swe200();
            } else {
                String exceptionText = "Received version in request is not supported!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(
                        OWSConstants.RequestParams.version.name(), exceptionText);
            }
            XmlObject encodedObject = CodingHelper.encodeObjectToXml(namespace, response);
            encodedObject.save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
            return new ServiceResponse(baos, contentType, false, true);
        } catch (IOException ioe) {
            String exceptionText = "Error occurs while saving response to output stream!";
            LOGGER.error(exceptionText, ioe);
            throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
        }
    }

    private void checkRequestedParameter(GetObservationByIdRequest sosRequest) throws OwsExceptionReport {
        List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
        // check parameters with variable content
        try {
            SosHelper.checkServiceParameter(sosRequest.getService());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            OwsHelper.checkSingleVersionParameter(sosRequest.getVersion());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            SosHelper.checkObservationIDs(sosRequest.getObservationIdentifier(),
                    Configurator.getInstance().getCapabilitiesCacheController().getObservationIdentifiers(),
                    Sos2Constants.GetObservationByIdParams.observation.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        Util4Exceptions.mergeAndThrowExceptions(exceptions);
    }
}
