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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.n52.sos.decode.DecoderKey;
import org.n52.sos.decode.KvpOperationDecoderKey;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.RequestParams;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.Sos2Constants.GetResultTemplateParams;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.GetResultTemplateRequest;
import org.n52.sos.util.KvpHelper;
import org.n52.sos.util.Util4Exceptions;

public class GetResultTemplateKvpDecoder extends AbstractKvpDecoder {

     private static final DecoderKey KVP_DECODER_KEY_TYPE 
            = new KvpOperationDecoderKey(SosConstants.SOS, Sos2Constants.SERVICEVERSION, Sos2Constants.Operations.GetResultTemplate.name());
    
    @Override
    public Set<DecoderKey> getDecoderKeyTypes() {
        return Collections.singleton(KVP_DECODER_KEY_TYPE);
    }

    @Override
    public GetResultTemplateRequest decode(Map<String, String> element) throws OwsExceptionReport {
        GetResultTemplateRequest request = new GetResultTemplateRequest();
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
                } else {
                    String exceptionText = String.format("The parameter '%s' is invalid for the GetResultTemplate request!", parameterName);
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
            exceptions.add(Util4Exceptions.createMissingMandatoryParameterException(GetResultTemplateParams.offering.name()));
        }

        if (!foundObservedProperty) {
            exceptions.add(Util4Exceptions.createMissingMandatoryParameterException(GetResultTemplateParams.observedProperty.name()));
        }
        Util4Exceptions.mergeAndThrowExceptions(exceptions);

        return request;
    }
}
