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
import org.n52.sos.decode.DecoderKey;
import org.n52.sos.decode.KvpOperationDecoderKey;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.GetCapabilitiesRequest;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.KvpHelper;
import org.n52.sos.util.Util4Exceptions;

public class GetCapabilitiesKvpDecoder extends AbstractKvpDecoder {

    private static final Set<DecoderKey> KVP_DECODER_KEY_TYPE = CollectionHelper.<DecoderKey>set(
        new KvpOperationDecoderKey(SosConstants.SOS, null,                         SosConstants.Operations.GetCapabilities.name()),
        new KvpOperationDecoderKey(SosConstants.SOS, Sos2Constants.SERVICEVERSION, SosConstants.Operations.GetCapabilities.name()),
        new KvpOperationDecoderKey(null            , Sos2Constants.SERVICEVERSION, SosConstants.Operations.GetCapabilities.name()),
        new KvpOperationDecoderKey(null            , null,                         SosConstants.Operations.GetCapabilities.name())
    );

    @Override
    public Set<DecoderKey> getDecoderKeyTypes() {
        return Collections.unmodifiableSet(KVP_DECODER_KEY_TYPE);
    }

    /**
     * parses the String representing the getCapabilities request and creates a
     * SosGetCapabilities request
     *
     * @param decoderKeyType the matched <code>KvpDecoderKeyType</code>
     * @param element String with getCapabilities parameters
     *
     * @return Returns SosGetCapabilitiesRequest representing the request
     *
     * @throws OwsExceptionReport If parsing the String failed
     */
    @Override
    public GetCapabilitiesRequest decode(Map<String, String> element) throws OwsExceptionReport {

        GetCapabilitiesRequest request = new GetCapabilitiesRequest();
        List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();

        for (String parameterName : element.keySet()) {
            String parameterValues = element.get(parameterName);
            try {
                // service (mandatory SOS 1.0.0, SOS 2.0 default)
                if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.service.name())) {
                    request.setService(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                } // request (mandatory)
                else if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.request.name())) {
                    KvpHelper.checkParameterSingleValue(parameterValues, parameterName);
                } // acceptVersions (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetCapabilitiesParams.AcceptVersions.name())) {
                    if (!parameterValues.isEmpty()) {
                        request.setAcceptVersions(parameterValues.split(","));
                    } else {
                        OwsExceptionReport owse = new OwsExceptionReport();
                        owse.addCodedException(OwsExceptionCode.InvalidParameterValue, parameterName, 
                                String.format("The value of parameter %s (%s) is invalid.", parameterName, parameterValues));
                        throw owse;
                    }
                } // acceptFormats (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetCapabilitiesParams.AcceptFormats.name())) {
                    request.setAcceptFormats(KvpHelper.checkParameterMultipleValues(parameterValues, parameterName));
                } // updateSequence (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetCapabilitiesParams.updateSequence.name())) {
                    request.setUpdateSequence(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));

                } // sections (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetCapabilitiesParams.Sections.name())) {
                    request.setSections(KvpHelper.checkParameterMultipleValues(parameterValues, parameterName));
                } else {
                    String exceptionText = String.format(
                            "The parameter '%s' is invalid for the GetCapabilities request!", parameterName);
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
                }
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
        }
        Util4Exceptions.mergeAndThrowExceptions(exceptions);

        return request;

    }
}
