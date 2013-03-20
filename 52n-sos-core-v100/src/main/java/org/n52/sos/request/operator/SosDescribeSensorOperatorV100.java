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
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ds.AbstractDescribeSensorDAO;
import org.n52.sos.encode.Encoder;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.concrete.InvalidOutputFormatException;
import org.n52.sos.exception.ows.concrete.InvalidProcedureDescriptionFormatException;
import org.n52.sos.exception.ows.concrete.MissingProcedureDescriptionFormatException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.request.DescribeSensorRequest;
import org.n52.sos.response.DescribeSensorResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.XmlOptionsHelper;

/**
 * class handles the DescribeSensor request
 *
 */
public class SosDescribeSensorOperatorV100 extends AbstractV1RequestOperator<AbstractDescribeSensorDAO, DescribeSensorRequest> {

    private static final String OPERATION_NAME = SosConstants.Operations.DescribeSensor.name();
    // TODO necessary in SOS 1.0.0, different value?
    private static final Set<String> CONFORMANCE_CLASSES = Collections.singleton("http://www.opengis.net/spec/SOS/1.0/conf/core");

    public SosDescribeSensorOperatorV100() {
        super(OPERATION_NAME, DescribeSensorRequest.class);
    }

    /**
     * from SosHelper, I think there was a bug, checks whether the value of outputFormat parameter is valid
     *
     * @param procedureDecriptionFormat the outputFormat parameter which should be checked
     *
     * @throws OwsExceptionReport if the value of the outputFormat parameter is incorrect
     */
    private void checkProcedureDescriptionFormat(String procedureDecriptionFormat, String parameterName) throws
            OwsExceptionReport {
        if (procedureDecriptionFormat == null || procedureDecriptionFormat.isEmpty() || procedureDecriptionFormat
                .equals(SosConstants.PARAMETER_NOT_SET)) {
            throw new MissingProcedureDescriptionFormatException();
        }
        if (!procedureDecriptionFormat.equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE)) {
            throw new InvalidParameterValueException(parameterName, procedureDecriptionFormat);
        }
}

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }

    @Override
    public ServiceResponse receive(DescribeSensorRequest sosRequest) throws OwsExceptionReport {
        boolean applyZIPcomp = false;

        checkRequestedParameters(sosRequest);
        if (sosRequest.getProcedureDescriptionFormat().equals(SosConstants.CONTENT_TYPE_ZIP)) {
            applyZIPcomp = true;
        }

        DescribeSensorResponse response = getDao().getSensorDescription(sosRequest);
        String contentType = SosConstants.CONTENT_TYPE_XML;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            String namespace;
            // check SOS version for response encoding
            if (response.getVersion().equals(Sos2Constants.SERVICEVERSION)) {
                namespace = SWEConstants.NS_SWES_20;
            } else if (response.getVersion().equals(Sos1Constants.SERVICEVERSION)) {
                // FIXME workaround to find the by-way through the sweencoder100 before going to the actual sensorml encoder
                namespace = Sos1Constants.NS_SOS;
                if (sosRequest.getProcedureDescriptionFormat().equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE)
                        || sosRequest.getProcedureDescriptionFormat().equals(SensorMLConstants.NS_SML)) {
                    contentType = SensorMLConstants.SENSORML_CONTENT_TYPE;
                }
            } else {
                throw new InvalidParameterValueException()
                        .at(OWSConstants.RequestParams.version)
                        .withMessage("Received version in request is not supported!");
            }

            Encoder<XmlObject, DescribeSensorResponse> encoder = CodingHelper.getEncoder(namespace, response);
            if (encoder != null) {
                encoder.encode(response).save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
                return new ServiceResponse(baos, contentType, applyZIPcomp, true);
            } else {
                if (sosRequest.getVersion().equals(Sos1Constants.SERVICEVERSION)) {
                    throw new InvalidOutputFormatException(sosRequest.getProcedureDescriptionFormat());
                } else {
                    throw new InvalidProcedureDescriptionFormatException(sosRequest.getProcedureDescriptionFormat());
                }
            }
        } catch (IOException ioe) {
            throw new NoApplicableCodeException().causedBy(ioe)
                    .withMessage("Error occurs while saving response to output stream!");
        }
    }

    private void checkRequestedParameters(DescribeSensorRequest sosRequest) throws
            OwsExceptionReport {
        CompositeOwsException exceptions = new CompositeOwsException();
        try {
            checkServiceParameter(sosRequest.getService());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            checkSingleVersionParameter(sosRequest);
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            checkProcedureID(sosRequest.getProcedure(), Configurator.getInstance().getCache()
                    .getProcedures(), SosConstants.DescribeSensorParams.procedure.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        // TODO necessary in SOS 1.0.0, different value? take care, here the local method is used, as it sowrks somehow differently in the sos helper(only sos 200?)
        try {
            checkProcedureDescriptionFormat(sosRequest.getProcedureDescriptionFormat(),
                    Sos1Constants.DescribeSensorParams.outputFormat.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        // TODO necessary in SOS 1.0.0, different value?
//        if (sosRequest.getTime() != null && !sosRequest.getTime().isEmpty()) {
//            String exceptionText = "The requested parameter is not supported by this server!";
//            exceptions.add(Util4Exceptions.createOptionNotSupportedException(Sos2Constants.DescribeSensorParams.validTime.name(), exceptionText));
//        }
        exceptions.throwIfNotEmpty();
    }

}