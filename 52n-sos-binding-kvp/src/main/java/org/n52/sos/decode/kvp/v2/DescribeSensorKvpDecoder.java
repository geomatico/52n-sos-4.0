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
import org.n52.sos.exception.ows.concrete.MissingProcedureDescriptionFormatException;
import org.n52.sos.exception.ows.concrete.MissingProcedureParameterException;
import org.n52.sos.exception.ows.concrete.MissingServiceParameterException;
import org.n52.sos.exception.ows.concrete.MissingVersionParameterException;
import org.n52.sos.exception.ows.concrete.ParameterNotSupportedException;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.DescribeSensorRequest;
import org.n52.sos.exception.ows.concrete.DateTimeParseException;
import org.n52.sos.util.KvpHelper;

public class DescribeSensorKvpDecoder extends AbstractKvpDecoder {
    private static final DecoderKey KVP_DECODER_KEY_TYPE =
                                    new KvpOperationDecoderKey(SosConstants.SOS, Sos2Constants.SERVICEVERSION, SosConstants.Operations.DescribeSensor
            .name());

    @Override
    public Set<DecoderKey> getDecoderKeyTypes() {
        return Collections.singleton(KVP_DECODER_KEY_TYPE);
    }

    @Override
    public DescribeSensorRequest decode(Map<String, String> element) throws OwsExceptionReport {

        DescribeSensorRequest request = new DescribeSensorRequest();
        CompositeOwsException exceptions = new CompositeOwsException();

        boolean foundProcedure = false;
        boolean foundProcedureDescriptionFormat = false;
        boolean foundService = false;
        boolean foundVersion = false;

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
                } // procedure
                else if (parameterName.equalsIgnoreCase(SosConstants.DescribeSensorParams.procedure.name())) {
                    request.setProcedures(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                    foundProcedure = true;
                } // procedureDescriptionFormat
                else if (parameterName.equalsIgnoreCase(Sos2Constants.DescribeSensorParams.procedureDescriptionFormat
                        .name())) {
                    request.setProcedureDescriptionFormat(KvpHelper
                            .checkParameterSingleValue(parameterValues, parameterName));
                    foundProcedureDescriptionFormat = true;
                } // valid time (optional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.DescribeSensorParams.validTime.name())) {
                    try {
                        request.setTime(parseValidTime(parameterValues, parameterName));
                    } catch (DecoderException e) {
                        exceptions.add(new InvalidParameterValueException(parameterName, parameterValues).causedBy(e));
                    } catch (DateTimeParseException e) {
                        exceptions.add(new InvalidParameterValueException(parameterName, parameterValues).causedBy(e));
                    }
                } else {
                    exceptions.add(new ParameterNotSupportedException(parameterName));
                }
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
        }

        if (!foundProcedure) {
            exceptions.add(new MissingProcedureParameterException());
        }

        if (!foundProcedureDescriptionFormat) {
            exceptions.add(new MissingProcedureDescriptionFormatException());
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
