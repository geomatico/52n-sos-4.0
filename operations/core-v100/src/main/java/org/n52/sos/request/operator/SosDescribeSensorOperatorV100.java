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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ds.AbstractDescribeSensorDAO;
import org.n52.sos.encode.Encoder;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.concrete.ErrorWhileSavingResponseToOutputStreamException;
import org.n52.sos.exception.ows.concrete.InvalidOutputFormatException;
import org.n52.sos.exception.ows.concrete.MissingProcedureDescriptionFormatException;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.request.DescribeSensorRequest;
import org.n52.sos.response.DescribeSensorResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.N52XmlHelper;
import org.n52.sos.util.XmlOptionsHelper;

/**
 * class handles the DescribeSensor request
 * 
 */
public class SosDescribeSensorOperatorV100 extends
        AbstractV1RequestOperator<AbstractDescribeSensorDAO, DescribeSensorRequest> {

    private static final String OPERATION_NAME = SosConstants.Operations.DescribeSensor.name();

    // TODO necessary in SOS 1.0.0, different value?
    private static final Set<String> CONFORMANCE_CLASSES = Collections
            .singleton("http://www.opengis.net/spec/SOS/1.0/conf/core");

    public SosDescribeSensorOperatorV100() {
        super(OPERATION_NAME, DescribeSensorRequest.class);
    }

    /**
     * from SosHelper, I think there was a bug, checks whether the value of
     * outputFormat parameter is valid
     * 
     * @param procedureDecriptionFormat
     *            the outputFormat parameter which should be checked
     * 
     * @throws OwsExceptionReport
     *             if the value of the outputFormat parameter is incorrect
     */
    private void checkProcedureDescriptionFormat(String procedureDecriptionFormat, String parameterName)
            throws OwsExceptionReport {
        if (procedureDecriptionFormat == null || procedureDecriptionFormat.isEmpty()
                || procedureDecriptionFormat.equals(SosConstants.PARAMETER_NOT_SET)) {
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
        DescribeSensorResponse response = getDao().getSensorDescription(sosRequest);
        String contentType = SosConstants.CONTENT_TYPE_XML;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            response.getOutputFormat();
            String namespace = sosRequest.getProcedureDescriptionFormat();
            Encoder<XmlObject, SosProcedureDescription> encoder = CodingHelper.getEncoder(namespace, response.getSensorDescription());
            if (encoder != null) {
                contentType = encoder.getContentType();
                XmlObject encodedObject = encoder.encode(response.getSensorDescription());
                List<String> schemaLocations = new ArrayList<String>(3);
                schemaLocations.add(N52XmlHelper.getSchemaLocationForSML101());
                schemaLocations.add(N52XmlHelper.getSchemaLocationForSWE101());
                N52XmlHelper.setSchemaLocationsToDocument(encodedObject, schemaLocations);
                encodedObject.save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
                return new ServiceResponse(baos, contentType, applyZIPcomp, true);
            } else {
               throw new InvalidOutputFormatException(sosRequest.getProcedureDescriptionFormat());
            }
        } catch (IOException ioe) {
            throw new ErrorWhileSavingResponseToOutputStreamException(ioe);
        }
    }

    private void checkRequestedParameters(DescribeSensorRequest sosRequest) throws OwsExceptionReport {
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
            checkProcedureID(sosRequest.getProcedure(), SosConstants.DescribeSensorParams.procedure.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        // TODO necessary in SOS 1.0.0, different value? take care, here the
        // local method is used, as it sowrks somehow differently in the sos
        // helper(only sos 200?)
        try {
            checkProcedureDescriptionFormat(sosRequest.getProcedureDescriptionFormat(),
                    Sos1Constants.DescribeSensorParams.outputFormat.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        // TODO necessary in SOS 1.0.0, different value?
        // if (sosRequest.getTime() != null && !sosRequest.getTime().isEmpty())
        // {
        // String exceptionText =
        // "The requested parameter is not supported by this server!";
        // exceptions.add(Util4Exceptions.createOptionNotSupportedException(Sos2Constants.DescribeSensorParams.validTime.name(),
        // exceptionText));
        // }
        exceptions.throwIfNotEmpty();
    }

}