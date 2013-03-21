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

import java.util.Collections;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.decode.Decoder;
import org.n52.sos.decode.OperationDecoderKey;
import org.n52.sos.decode.XmlOperationDecoderKey;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.GenericThrowableWrapperException;
import org.n52.sos.exception.ows.concrete.InvalidAcceptVersionsParameterException;
import org.n52.sos.exception.ows.concrete.InvalidServiceOrVersionException;
import org.n52.sos.exception.ows.concrete.MethodNotSupportedException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.GetCapabilitiesRequest;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.operator.ServiceOperator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoxBinding extends Binding {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoxBinding.class);
    private static final Set<String> CONFORMANCE_CLASSES = Collections.singleton(ConformanceClasses.SOS_V2_POX_BINDING);
    private static final String URL_PATTERN = "/pox";

    @Override
    public ServiceResponse doGetOperation(HttpServletRequest request) throws OwsExceptionReport {
        throw new MethodNotSupportedException("POX", "GET");
    }

    @Override
    public ServiceResponse doPostOperation(HttpServletRequest request) throws OwsExceptionReport {
        ServiceResponse serviceResponse = null;
        AbstractServiceRequest sosRequest = null;
        try {
            XmlObject doc = XmlHelper.parseXmlSosRequest(request);
            LOGGER.debug("XML-REQUEST: {}", doc.xmlText());
            Decoder<AbstractServiceRequest, XmlObject> decoder = getDecoder(CodingHelper.getDecoderKey(doc));
            // decode XML message
            Object abstractRequest = decoder.decode(doc);
            if (abstractRequest instanceof AbstractServiceRequest) {
                sosRequest = (AbstractServiceRequest) abstractRequest;
                checkServiceOperatorKeyTypes(sosRequest);
                for (ServiceOperatorKeyType serviceVersionIdentifier : sosRequest.getServiceOperatorKeyType()) {
                    ServiceOperator serviceOperator = getServiceOperatorRepository()
							.getServiceOperator(serviceVersionIdentifier);
                    if (serviceOperator != null) {
                        serviceResponse = serviceOperator.receiveRequest(sosRequest);
                        break;
                    }
                }
                if (serviceResponse == null) {
                    if (sosRequest instanceof GetCapabilitiesRequest) {
                        GetCapabilitiesRequest gcr = (GetCapabilitiesRequest) sosRequest;
                        throw new InvalidAcceptVersionsParameterException(gcr.getAcceptVersions());
                    } else {
                        throw new InvalidServiceOrVersionException(sosRequest.getService(), sosRequest.getVersion());
                    }
                }
            } else {
                throw new NoApplicableCodeException()
                        .withMessage("The returned object is not an AbstractServiceRequest implementation");
            }
        } catch (Throwable t) {
            OwsExceptionReport owse;
            if (t instanceof OwsExceptionReport) {
                owse = (OwsExceptionReport) t;
            } else {
                owse = new GenericThrowableWrapperException(t);
            }
            throw owse.setVersion(sosRequest != null ? sosRequest.getVersion() : null);
        }
        return serviceResponse;
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
        return getDecoder(new XmlOperationDecoderKey(k)) != null;
    }
}
