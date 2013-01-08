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
package org.n52.sos.decode.kvp.v2;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.n52.sos.decode.kvp.AbstractKvpDecoderOperationDelegate;
import org.n52.sos.decode.DecoderException;
import org.n52.sos.decode.RequestDecoderKey;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.RequestParams;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.Sos2Constants.GetResultParams;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.GetResultRequest;
import org.n52.sos.util.DateTimeException;
import org.n52.sos.util.KvpHelper;
import org.n52.sos.util.Util4Exceptions;

public class GetResultKvpDecoderOperationDelegate extends AbstractKvpDecoderOperationDelegate {

    private static final RequestDecoderKey KVP_DECODER_KEY_TYPE 
            = new RequestDecoderKey(Sos2Constants.SERVICEVERSION, SosConstants.Operations.GetResult.name());
    
    @Override
    public Set<RequestDecoderKey> getRequestDecoderKeys() {
        return Collections.singleton(KVP_DECODER_KEY_TYPE);
    }

    @Override
    public GetResultRequest decode(RequestDecoderKey decoderKeyType, Map<String, String> element) throws OwsExceptionReport {
        GetResultRequest request = new GetResultRequest();
        List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();

        boolean foundService = false;
        boolean foundVersion = false;
        boolean foundOffering = false;
        boolean foundObservedProperty = false;

        for (String parameterName : element.keySet()) {
            String parameterValues = element.get(parameterName);
            try {
                // service (mandatory)
                if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.service.name())) {
                    request.setService(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                    foundService = true;
                } // version (mandatory)
                else if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.version.name())) {
                    request.setVersion(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                    foundVersion = true;
                } // request (mandatory)
                else if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.request.name())) {
                    KvpHelper.checkParameterSingleValue(parameterValues, parameterName);
                } // offering (mandatory)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetResultTemplateParams.offering.name())) {
                    request.setOffering(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                    foundOffering = true;
                } // observedProperty (mandatory)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetResultTemplateParams.observedProperty.name())) {
                    request.setObservedProperty(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                    foundObservedProperty = true;
                } // featureOfInterest (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.featureOfInterest.name())) {
                    request.setFeatureIdentifiers(KvpHelper.checkParameterMultipleValues(parameterValues, parameterName));
                } // eventTime (optional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetObservationParams.temporalFilter.name())) {
                    try {
                        request.setTemporalFilter(parseTemporalFilter(KvpHelper.checkParameterMultipleValues(
                                parameterValues, parameterName), parameterName));
                    } catch (DecoderException e) {
                        exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                                parameterName, "The parameter value is not valid!"));
                    } catch (DateTimeException e) {
                        exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                                parameterName, "The parameter value is not valid!"));
                    }

                } // spatialFilter (optional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetObservationParams.spatialFilter.name())) {
                    request.setSpatialFilter(parseSpatialFilter(KvpHelper.checkParameterMultipleValues(
                            parameterValues, parameterName), parameterName));
                } // xmlWrapper (default = false) (optional)
                // namespaces (conditional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetObservationParams.namespaces.name())) {
                    request.setNamespaces(parseNamespaces(parameterValues));
                } else {
                    String exceptionText = String.format(
                            "The parameter '%s' is invalid for the GetResult request!", parameterName);
                    LOGGER.debug(exceptionText);
                    exceptions.add(Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText));
                }
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
        }

        if (!foundService) {
            exceptions.add(Util4Exceptions.createMissingMandatoryParameterException(RequestParams.service.name()));
        }

        if (!foundVersion) {
            exceptions.add(Util4Exceptions.createMissingMandatoryParameterException(RequestParams.version.name()));
        }

        if (!foundOffering) {
            exceptions.add(Util4Exceptions.createMissingMandatoryParameterException(GetResultParams.offering.name()));
        }

        if (!foundObservedProperty) {
            exceptions.add(Util4Exceptions.createMissingMandatoryParameterException(GetResultParams.observedProperty.name()));
        }
        Util4Exceptions.mergeAndThrowExceptions(exceptions);
        return request;
    }
}
