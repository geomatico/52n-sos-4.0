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
import org.n52.sos.ds.IGetFeatureOfInterestDAO;
import org.n52.sos.ds.IGetObservationByIdDAO;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.request.GetObservationByIdRequest;
import org.n52.sos.response.GetObservationByIdResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetObservationByIdOperatorV100 extends
		AbstractV1RequestOperator <IGetObservationByIdDAO, GetObservationByIdRequest> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SosGetFeatureOfInterestOperatorV100.class.getName());
    private static final String OPERATION_NAME = SosConstants.Operations.GetFeatureOfInterest.name();
    private static final Set<String> CONFORMANCE_CLASSES = Collections.singleton("http://www.opengis.net/spec/SOS/1.0/conf/extended");

    /**
     * Constructor
     *
     */
    public GetObservationByIdOperatorV100() {
        super(OPERATION_NAME, GetObservationByIdRequest.class);
    }
    
	private void checkRequestedParameters(GetObservationByIdRequest sosRequest) throws OwsExceptionReport {
        List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>(0);
        try {
            checkServiceParameter(sosRequest.getService());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
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
            String namespace;
            GetObservationByIdResponse response = getDao().getObservationById(sosRequest);
            
            String contentType = SosConstants.CONTENT_TYPE_XML;
            if (sosRequest.getVersion().equals(Sos1Constants.SERVICEVERSION) ) {
                namespace = Sos1Constants.NS_SOS;
            } else {
                String exceptionText = "Received version in request is not supported!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(
                        OWSConstants.RequestParams.version.name(), exceptionText);
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IEncoder<XmlObject, GetObservationByIdResponse> encoder = CodingHelper.getEncoder(namespace, response);
            
            if (encoder != null) {
                encoder.encode(response).save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
                return new ServiceResponse(baos, contentType, applyZIPcomp, true);
                
            } else {
                // complain check missing params throw exception
            	OwsExceptionReport owse = new OwsExceptionReport();
            	String exceptionText = "No encoder for operation found!";
                LOGGER.error(exceptionText, owse);
                throw Util4Exceptions.createNoApplicableCodeException(owse, exceptionText);
            }
        } catch (IOException ioe) {
            String exceptionText = "Error occurs while saving response to output stream!";
            LOGGER.error(exceptionText, ioe);
            throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
        }
    }
}
