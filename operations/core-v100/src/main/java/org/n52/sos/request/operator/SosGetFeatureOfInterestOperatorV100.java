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
import org.n52.sos.ds.AbstractGetFeatureOfInterestDAO;
import org.n52.sos.encode.Encoder;
import org.n52.sos.exception.ows.concrete.ErrorWhileSavingResponseToOutputStreamException;
import org.n52.sos.exception.ows.concrete.NoEncoderForResponseException;
import org.n52.sos.exception.ows.concrete.VersionNotSupportedException;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.GetFeatureOfInterestRequest;
import org.n52.sos.response.GetFeatureOfInterestResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.XmlOptionsHelper;

public class SosGetFeatureOfInterestOperatorV100 extends
 AbstractV1RequestOperator<AbstractGetFeatureOfInterestDAO, GetFeatureOfInterestRequest> {

    private static final String OPERATION_NAME = SosConstants.Operations.GetFeatureOfInterest.name();
    private static final Set<String> CONFORMANCE_CLASSES = Collections.singleton("http://www.opengis.net/spec/SOS/1.0/conf/enhanced");

    /**
     * Constructor
     *
     */
    public SosGetFeatureOfInterestOperatorV100() {
        super(OPERATION_NAME, GetFeatureOfInterestRequest.class);
    }
    
    private void checkRequestedParameters(GetFeatureOfInterestRequest sosRequest) throws OwsExceptionReport {
        CompositeOwsException exceptions = new CompositeOwsException();
        try {
            checkServiceParameter(sosRequest.getService());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        exceptions.throwIfNotEmpty();
	}

	@Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }

	@Override
	protected ServiceResponse receive(GetFeatureOfInterestRequest sosRequest)
            throws OwsExceptionReport {
		boolean applyZIPcomp = false;

        checkRequestedParameters(sosRequest);
        checkFeatureOfInterestIdentifiers(sosRequest.getFeatureIdentifiers(), Sos1Constants.GetFeatureOfInterestParams.featureOfInterestID.name());
        
        try {
            String namespace;
            GetFeatureOfInterestResponse response = getDao().getFeatureOfInterest(sosRequest);
            // TODO call encoder directly, Collection == GmlEncoder, single FOI == SamplingEncoder 
            String contentType = SosConstants.CONTENT_TYPE_XML;
            if (sosRequest.getVersion().equals(Sos1Constants.SERVICEVERSION) ) {
                namespace = Sos1Constants.NS_SOS;
            } else {
                throw new VersionNotSupportedException();
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Encoder<XmlObject, GetFeatureOfInterestResponse> encoder = CodingHelper.getEncoder(namespace, response);
            
            if (encoder != null) {
                encoder.encode(response).save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
                return new ServiceResponse(baos, contentType, applyZIPcomp, true);
            } else {
                throw new NoEncoderForResponseException();
            }
        } catch (IOException ioe) {
            throw new ErrorWhileSavingResponseToOutputStreamException(ioe);
        }
    }
}
