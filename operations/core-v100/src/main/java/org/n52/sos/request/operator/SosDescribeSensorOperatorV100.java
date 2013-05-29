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
import org.n52.sos.exception.ows.concrete.ErrorWhileSavingResponseToOutputStreamException;
import org.n52.sos.exception.ows.concrete.InvalidOutputFormatException;
import org.n52.sos.exception.ows.concrete.NoEncoderForKeyException;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.request.DescribeSensorRequest;
import org.n52.sos.response.DescribeSensorResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.N52XmlHelper;
import org.n52.sos.util.SosHelper;
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

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }

    @Override
    public ServiceResponse receive(DescribeSensorRequest sosRequest) throws OwsExceptionReport {
        boolean applyZIPcomp = false;

        checkRequestedParameters(sosRequest);
        DescribeSensorResponse response = getDao().getSensorDescription(sosRequest);
        String namespace = sosRequest.getProcedureDescriptionFormat();

        Encoder<XmlObject, SosProcedureDescription> encoder;
        try{
            encoder = CodingHelper.getEncoder(namespace, response.getSensorDescription());
        } catch (NoEncoderForKeyException e) {
            throw new InvalidOutputFormatException(sosRequest.getProcedureDescriptionFormat());
        }
        
        XmlObject encodedObject = encoder.encode(response.getSensorDescription());
        N52XmlHelper.setSchemaLocationsToDocument(encodedObject, CollectionHelper.set(N52XmlHelper.getSchemaLocationForSML101(), N52XmlHelper.getSchemaLocationForSWE101()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            encodedObject.save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
        } catch (IOException ioe) {
            throw new ErrorWhileSavingResponseToOutputStreamException(ioe);
        }
        return new ServiceResponse(baos, SosConstants.CONTENT_TYPE_XML, applyZIPcomp, true);
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
        try {
            SosHelper.checkProcedureDescriptionFormat(sosRequest.getProcedureDescriptionFormat(),
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