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
package org.n52.sos.decode.kvp.v2;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.n52.sos.decode.DecoderException;
import org.n52.sos.decode.DecoderKey;
import org.n52.sos.decode.KvpOperationDecoderKey;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.MissingParameterValueException.MissingServiceParameterException;
import org.n52.sos.exception.ows.MissingParameterValueException.MissingVersionParameterException;
import org.n52.sos.exception.ows.OptionNotSupportedException.ParameterNotSupportedException;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.GetObservationRequest;
import org.n52.sos.util.DateTimeException;
import org.n52.sos.util.KvpHelper;

public class GetObservationKvpDecoder extends AbstractKvpDecoder {

    private static final DecoderKey KVP_DECODER_KEY_TYPE 
            = new KvpOperationDecoderKey(SosConstants.SOS, Sos2Constants.SERVICEVERSION, SosConstants.Operations.GetObservation.name());

    @Override
    public Set<DecoderKey> getDecoderKeyTypes() {
        return Collections.singleton(KVP_DECODER_KEY_TYPE);
    }

    @Override
    public GetObservationRequest decode(Map<String, String> element) throws OwsExceptionReport {

        final GetObservationRequest request = new GetObservationRequest();
        final CompositeOwsException exceptions = new CompositeOwsException();
        boolean foundService = false;
        boolean foundVersion = false;

        for (String parameterName : element.keySet()) {
            String parameterValues = element.get(parameterName);
            try {
                // service (mandatory)
                if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.service.name())) {
                    request.setService(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                    foundService = true;
                }

                // version (mandatory)
                else if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.version.name())) {
                    request.setVersion(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                    foundVersion = true;
                }
                // request (mandatory)
                else if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.request.name())) {
                    KvpHelper.checkParameterSingleValue(parameterValues, parameterName);
                }

                // offering (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.offering.name())) {
                    request.setOfferings(KvpHelper.checkParameterMultipleValues(parameterValues, parameterName));
                }

                // observedProperty (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.observedProperty.name())) {
                    request.setObservedProperties(KvpHelper.checkParameterMultipleValues(parameterValues, parameterName));
                }

                // procedure (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.procedure.name())) {
                    request.setProcedures(KvpHelper.checkParameterMultipleValues(parameterValues, parameterName));
                }

                // featureOfInterest (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.featureOfInterest.name())) {
                    request.setFeatureIdentifiers(KvpHelper.checkParameterMultipleValues(parameterValues, parameterName));
                }

                // eventTime (optional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetObservationParams.temporalFilter.name())) {
                    try {
                        request.setTemporalFilters(parseTemporalFilter(KvpHelper.checkParameterMultipleValues(
                                parameterValues, parameterName), parameterName));
                    } catch (DecoderException e) {
                        exceptions.add(new InvalidParameterValueException(parameterName, parameterValues).causedBy(e));
                    } catch (DateTimeException e) {
                        exceptions.add(new InvalidParameterValueException(parameterName, parameterValues).causedBy(e));
                    }
                }

                // spatialFilter (optional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetObservationParams.spatialFilter.name())) {
                    request.setSpatialFilter(parseSpatialFilter(
                            KvpHelper.checkParameterMultipleValues(parameterValues, parameterName), parameterName));
                }

                // responseFormat (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.responseFormat.name())) {
                    request.setResponseFormat(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                }
                // namespaces (conditional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetObservationParams.namespaces.name())) {
                    request.setNamespaces(parseNamespaces(parameterValues));
                } else {
                    exceptions.add(new ParameterNotSupportedException(parameterName));
                }

            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
        }

        if (!foundService) {
            exceptions.add(new MissingServiceParameterException());
        }

        if (!foundVersion) {
            exceptions.add(new MissingVersionParameterException());
        }

        exceptions.throwIfNotEmpty();

        return request;
    }
}
