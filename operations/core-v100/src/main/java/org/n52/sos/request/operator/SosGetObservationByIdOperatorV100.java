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
import org.n52.sos.ds.AbstractGetObservationByIdDAO;
import org.n52.sos.encode.Encoder;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.ErrorWhileSavingResponseToOutputStreamException;
import org.n52.sos.exception.ows.concrete.MissingResponseFormatParameterException;
import org.n52.sos.exception.ows.concrete.NoEncoderForResponseException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.GetObservationByIdRequest;
import org.n52.sos.response.GetObservationByIdResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosGetObservationByIdOperatorV100 extends
 AbstractV1RequestOperator<AbstractGetObservationByIdDAO, GetObservationByIdRequest> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SosGetObservationByIdOperatorV100.class.getName());
    private static final String OPERATION_NAME = SosConstants.Operations.GetObservationById.name();
    private static final Set<String> CONFORMANCE_CLASSES = Collections.singleton("http://www.opengis.net/spec/SOS/1.0/conf/enhanced");

    /**
     * Constructor
     *
     */
    public SosGetObservationByIdOperatorV100() {
        super(OPERATION_NAME, GetObservationByIdRequest.class);
    }
    
    private void checkRequestedParameters(GetObservationByIdRequest sosRequest) throws OwsExceptionReport {
        
        checkServiceParameter(sosRequest.getService());
        // check valid obs ids
        checkObservationIDs(sosRequest.getObservationIdentifier(),
                            Sos1Constants.GetObservationByIdParams.ObservationId.name());
        // check responseFormat!
	String responseFormat = sosRequest.getResponseFormat();
        if ((responseFormat == null) || !(responseFormat.length() > 0)) {
            throw new MissingResponseFormatParameterException();
		}
        // srsName and resultModel (omObs, om:Meas, Swe:?, responseMode (inline only)
		String responseMode = sosRequest.getResponseMode();
        if (responseMode != null && !responseMode.equalsIgnoreCase("inline")) {
            throw new NoApplicableCodeException()
                    .withMessage("Only responseMode inline is currently supported by this SOS 1.0.0 implementation")
                    .at(SosConstants.GetObservationParams.responseMode);
		}
        
	String resultModel = sosRequest.getResultModel();
        if (resultModel != null) {
            throw new NoApplicableCodeException().at(SosConstants.GetObservationParams.resultModel)
                    .withMessage("resultModel is currently not supported by this SOS 1.0.0 implementation");
		}
	}

	@Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }

	@Override
	protected ServiceResponse receive(GetObservationByIdRequest sosRequest)
            throws OwsExceptionReport {
		boolean applyZIPcomp = false;

        checkRequestedParameters(sosRequest);

        try {
            GetObservationByIdResponse response = getDao().getObservationById(sosRequest);
            
            String contentType = SosConstants.CONTENT_TYPE_XML;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Encoder<XmlObject, GetObservationByIdResponse> encoder = CodingHelper.getEncoder(sosRequest.getResponseFormat(), response);
            if (encoder != null) {
                contentType = encoder.getContentType();
                encoder.encode(response).save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
                return new ServiceResponse(baos, contentType, applyZIPcomp, true);
                
            } else {
                // complain check missing params throw exception
                throw new NoEncoderForResponseException();
            }
        } catch (IOException ioe) {
            throw new ErrorWhileSavingResponseToOutputStreamException(ioe);
        }
    }
}
