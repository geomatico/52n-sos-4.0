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
import org.n52.sos.exception.ows.OptionNotSupportedException;
import org.n52.sos.exception.ows.concrete.EncoderResponseUnsupportedException;
import org.n52.sos.exception.ows.concrete.ErrorWhileSavingResponseToOutputStreamException;
import org.n52.sos.exception.ows.concrete.InvalidOutputFormatException;
import org.n52.sos.exception.ows.concrete.InvalidProcedureDescriptionFormatException;
import org.n52.sos.exception.ows.concrete.VersionNotSupportedException;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.request.DescribeSensorRequest;
import org.n52.sos.response.DescribeSensorResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.CodingRepository;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.n52.sos.wsdl.WSDLConstants;
import org.n52.sos.wsdl.WSDLOperation;

/**
 * class handles the DescribeSensor request
 *
 */
public class SosDescribeSensorOperatorV20 extends AbstractV2RequestOperator<AbstractDescribeSensorDAO, DescribeSensorRequest> {
    private static final String OPERATION_NAME = SosConstants.Operations.DescribeSensor.name();
    private static final Set<String> CONFORMANCE_CLASSES = Collections.singleton(ConformanceClasses.SOS_V2_CORE_PROFILE);

    public SosDescribeSensorOperatorV20() {
        super(OPERATION_NAME, DescribeSensorRequest.class);
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
                namespace = SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL;
                if (sosRequest.getProcedureDescriptionFormat().equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE)
                        || sosRequest.getProcedureDescriptionFormat().equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL)) {
                    contentType = SensorMLConstants.SENSORML_CONTENT_TYPE;
                }
            } else {
                throw new VersionNotSupportedException();
            }
            Encoder<?, DescribeSensorResponse> encoder = CodingRepository.getInstance().getEncoder(
            		CodingHelper.getEncoderKey(namespace, response));
            if (encoder != null) {
                Object encodedObject = encoder.encode(response);
                if (encodedObject instanceof XmlObject) {
                    ((XmlObject) encodedObject).save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
                    return new ServiceResponse(baos, contentType, false, true);
                } else if (encodedObject instanceof ServiceResponse) {
                    return (ServiceResponse) encodedObject;
                } else {
                    throw new EncoderResponseUnsupportedException();
                }
            } else {
                if (sosRequest.getVersion().equals(Sos1Constants.SERVICEVERSION)) {
                    throw new InvalidOutputFormatException(sosRequest.getProcedureDescriptionFormat());
                } else {
                    throw new InvalidProcedureDescriptionFormatException(sosRequest.getProcedureDescriptionFormat());
                }
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
            checkProcedureID(sosRequest.getProcedure(),
                             SosConstants.DescribeSensorParams.procedure.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            SosHelper.checkProcedureDescriptionFormat(sosRequest.getProcedureDescriptionFormat(),
                    Sos2Constants.DescribeSensorParams.procedureDescriptionFormat.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        if (sosRequest.getTime() != null && !sosRequest.getTime().isEmpty()) {
            exceptions.add(new OptionNotSupportedException().at(Sos2Constants.DescribeSensorParams.validTime)
                    .withMessage("The requested parameter is not supported by this server!"));
        }
        exceptions.throwIfNotEmpty();
    }

    @Override
    public WSDLOperation getSosOperationDefinition() {
        return WSDLConstants.Operations.DESCRIBE_SENSOR;
    }
}