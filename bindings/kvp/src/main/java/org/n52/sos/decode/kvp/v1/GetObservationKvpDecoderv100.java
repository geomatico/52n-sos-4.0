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
package org.n52.sos.decode.kvp.v1;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.n52.sos.decode.DecoderKey;
import org.n52.sos.decode.KvpOperationDecoderKey;
import org.n52.sos.decode.kvp.AbstractKvpDecoder;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.concrete.MissingObservedPropertyParameterException;
import org.n52.sos.exception.ows.concrete.MissingOfferingParameterException;
import org.n52.sos.exception.ows.concrete.MissingResponseFormatParameterException;
import org.n52.sos.exception.ows.concrete.MissingServiceParameterException;
import org.n52.sos.exception.ows.concrete.MissingVersionParameterException;
import org.n52.sos.exception.ows.concrete.ParameterNotSupportedException;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.GetObservationRequest;
import org.n52.sos.util.KvpHelper;

public class GetObservationKvpDecoderv100 extends AbstractKvpDecoder {

    private static final DecoderKey KVP_DECODER_KEY_TYPE 
            = new KvpOperationDecoderKey(SosConstants.SOS, Sos1Constants.SERVICEVERSION, SosConstants.Operations.GetObservation.name());

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
        boolean foundOffering = false;
        boolean foundObservedProperty = false;
        boolean foundResponseFormat = false;
        
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

                // offering (mandatory)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.offering.name())) {
                    request.setOfferings(KvpHelper.checkParameterMultipleValues(parameterValues, parameterName));
                    foundOffering = true;
                }

                // eventTime (optional)
                else if (parameterName.equalsIgnoreCase(Sos1Constants.GetObservationParams.eventTime.name())) {
                    if (!parameterValues.contains(",")) {
                        //for v1, prepend om:phenomenonTime if not present
                        parameterValues = "om:phenomenonTime," + parameterValues;
                    }
                    try {
                        request.setTemporalFilters(parseTemporalFilter(KvpHelper.checkParameterMultipleValues(
                                parameterValues, parameterName), parameterName));
                    } catch (OwsExceptionReport e) {
                        exceptions.add(new InvalidParameterValueException(parameterName, parameterValues).causedBy(e));
                    }
                }

                // procedure (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.procedure.name())) {
                    request.setProcedures(KvpHelper.checkParameterMultipleValues(parameterValues, parameterName));
                }
                
                // observedProperty (mandatory)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.observedProperty.name())) {
                    request.setObservedProperties(KvpHelper.checkParameterMultipleValues(parameterValues, parameterName));
                    foundObservedProperty = true;
                }

                // featureOfInterest (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.featureOfInterest.name())) {
                	//try to detect spatial filter bbox. should this be different for v100?
                	if (Pattern.matches("^om:featureOfInterest.*(,\\s*[-+]?\\d*\\.?\\d+){4}(,.*)?$",parameterValues)) {
                        request.setSpatialFilter(parseSpatialFilter(
                                KvpHelper.checkParameterMultipleValues(parameterValues, parameterName), parameterName));                		
                	} else {
                        request.setFeatureIdentifiers(KvpHelper.checkParameterMultipleValues(parameterValues, parameterName));                		
                	}
                }

                // responseFormat (mandatory)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.responseFormat.name())) {
                    request.setResponseFormat(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                    foundResponseFormat = true;
                }

                //resultModel (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.resultModel.name())) {
                    request.setResultModel(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                }

                //responseMode (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.responseMode.name())) {
                    request.setResponseMode(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));                    
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

        if (!foundOffering) {
            exceptions.add(new MissingOfferingParameterException());
        }

        if (!foundObservedProperty) {
            exceptions.add(new MissingObservedPropertyParameterException());
        }

        if (!foundResponseFormat) {
            exceptions.add(new MissingResponseFormatParameterException());
        }

        exceptions.throwIfNotEmpty();

        return request;
    }
}
