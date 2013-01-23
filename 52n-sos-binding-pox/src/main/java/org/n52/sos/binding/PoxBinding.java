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
package org.n52.sos.binding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.decode.IDecoder;
import org.n52.sos.decode.OperationDecoderKey;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.OWSConstants.RequestParams;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.GetCapabilitiesRequest;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.IServiceOperator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoxBinding extends Binding {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoxBinding.class);
    private static final Set<String> CONFORMANCE_CLASSES = Collections.singleton(ConformanceClasses.SOS_V2_POX_BINDING);
    /**
     * URL pattern for POX requests
     */
    private static final String URL_PATTERN = "/pox";

    @Override
    public ServiceResponse doGetOperation(HttpServletRequest request) throws OwsExceptionReport {
        String message = "HTTP GET is no supported for POX binding!";
        OwsExceptionReport owse = Util4Exceptions.createNoApplicableCodeException(null, message);
        if (Configurator.getInstance().getServiceOperatorRepository().isVersionSupported(Sos1Constants.SERVICEVERSION)) {
            owse.setVersion(Sos1Constants.SERVICEVERSION);
        } else {
            owse.setVersion(Sos2Constants.SERVICEVERSION);
        }
        throw owse;
    }

    @Override
    public ServiceResponse doPostOperation(HttpServletRequest request) throws OwsExceptionReport {
        ServiceResponse serviceResponse = null;
        String version = null;
        try {
            XmlObject doc = XmlHelper.parseXmlSosRequest(request);
            LOGGER.debug("XML-REQUEST: {}", doc.xmlText());
            IDecoder<AbstractServiceRequest, XmlObject> decoder = Configurator.getInstance()
                    .getCodingRepository().getDecoder(CodingHelper.getDecoderKey(doc));
            // decode XML message
            Object abstractRequest = decoder.decode(doc);
            if (abstractRequest instanceof AbstractServiceRequest) {
                AbstractServiceRequest sosRequest = (AbstractServiceRequest) abstractRequest;
                checkServiceOperatorKeyTypes(sosRequest);
                for (ServiceOperatorKeyType serviceVersionIdentifier : sosRequest.getServiceOperatorKeyType()) {
                    IServiceOperator serviceOperator = Configurator.getInstance().getServiceOperatorRepository()
							.getServiceOperator(serviceVersionIdentifier);
                    if (serviceOperator != null) {
                        serviceResponse = serviceOperator.receiveRequest(sosRequest);
                        break;
                    }
                }
                if (serviceResponse == null) {
                    if (sosRequest instanceof GetCapabilitiesRequest) {
                        StringBuilder exceptionText = new StringBuilder();
                        exceptionText.append("The requested ");
                        exceptionText.append(SosConstants.GetCapabilitiesParams.AcceptVersions.name());
                        exceptionText.append(" values (");
                        for (String acceptVersion : ((GetCapabilitiesRequest) sosRequest).getAcceptVersions()) {
                            exceptionText.append(acceptVersion);
                            exceptionText.append(", ");
                        }
                        exceptionText.delete(exceptionText.lastIndexOf(", "), exceptionText.length());
                        exceptionText.append(") are not supported by this server!");
                        throw Util4Exceptions.createVersionNegotiationFailedException(exceptionText.toString());
                    } else {
                        StringBuilder exceptionText = new StringBuilder();
                        exceptionText.append("The requested service (");
                        exceptionText.append(sosRequest.getService());
                        exceptionText.append(") and/or version (");
                        exceptionText.append(sosRequest.getVersion());
                        exceptionText.append(") is not supported by this server!");
                        LOGGER.debug(exceptionText.toString());
                        throw Util4Exceptions.createInvalidParameterValueException(
                                OWSConstants.RequestParams.service.name(), exceptionText.toString());
                    }
                }
            } else {
                throw Util4Exceptions.createNoApplicableCodeException(null,
                        "The returned object is not an AbstractServiceRequest implementation");
            }
        } catch (OwsExceptionReport owse) {
            // FIXME: valid debug text
            LOGGER.debug("", owse);
            if (version != null) {
                owse.setVersion(version);
            } else {
                if (Configurator.getInstance().getServiceOperatorRepository().isVersionSupported(Sos1Constants.SERVICEVERSION)) {
                    owse.setVersion(Sos1Constants.SERVICEVERSION);
                } else {
                    owse.setVersion(Sos2Constants.SERVICEVERSION);
                }
            }
            throw owse;
        }
        return serviceResponse;
    }

    private void checkServiceOperatorKeyTypes(AbstractServiceRequest request) throws OwsExceptionReport {
        List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>(0);
        for (ServiceOperatorKeyType serviceVersionIdentifier : request.getServiceOperatorKeyType()) {
            if (serviceVersionIdentifier.getService() != null) {
                if (serviceVersionIdentifier.getService().isEmpty()) {
                    exceptions.add(Util4Exceptions.createMissingParameterValueException(RequestParams.service.name()));
                } else {
                    if (!Configurator.getInstance().getServiceOperatorRepository().isServiceSupported(serviceVersionIdentifier.getService())) {
                        String exceptionText = "The requested service is not supported!";
                        exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                                RequestParams.service.name(), exceptionText));
                    }
                }
            }
            if (request instanceof GetCapabilitiesRequest) {
                GetCapabilitiesRequest getCapsRequest = (GetCapabilitiesRequest) request;
                if (getCapsRequest.isSetAcceptVersions()) {
                    boolean hasSupportedVersion = false;
                    for (String accaptVersion : getCapsRequest.getAcceptVersions()) {
                        if (Configurator.getInstance().getServiceOperatorRepository().isVersionSupported(accaptVersion)) {
                            hasSupportedVersion = true;
                        }
                    }
                    if (!hasSupportedVersion) {
                        String exceptionText = "The requested acceptedVersions are not supported by this service!";
                        exceptions.add(Util4Exceptions.createVersionNegotiationFailedException(exceptionText));
                    }
                }
            } else {
                if (serviceVersionIdentifier.getVersion() != null) {
                    if (serviceVersionIdentifier.getVersion().isEmpty()) {
                        exceptions.add(Util4Exceptions.createMissingParameterValueException(RequestParams.version
                                .name()));
                    } else {
                        if (!Configurator.getInstance().getServiceOperatorRepository().isVersionSupported(serviceVersionIdentifier.getVersion())) {
                            String exceptionText = "The requested version is not supported!";
                            exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                                    RequestParams.version.name(), exceptionText));
                        }
                    }
                }
            }
        }
        Util4Exceptions.mergeAndThrowExceptions(exceptions);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }

    @Override
    public String getUrlPattern() {
        return URL_PATTERN;
    }

    @Override
    public boolean checkOperationHttpPostSupported(OperationDecoderKey k) throws OwsExceptionReport {
        return CodingHelper.hasXmlEncoderForOperation(k);
    }
}
