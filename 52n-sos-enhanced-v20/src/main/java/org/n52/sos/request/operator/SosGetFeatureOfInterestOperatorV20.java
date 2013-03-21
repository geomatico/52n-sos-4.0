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
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ds.AbstractGetFeatureOfInterestDAO;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.concrete.EncoderResponseUnsupportedException;
import org.n52.sos.exception.ows.concrete.ErrorWhileSavingResponseToOutputStreamException;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.GetFeatureOfInterestRequest;
import org.n52.sos.response.GetFeatureOfInterestResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.n52.sos.wsdl.WSDLConstants;
import org.n52.sos.wsdl.WSDLOperation;

public class SosGetFeatureOfInterestOperatorV20 extends AbstractV2RequestOperator<AbstractGetFeatureOfInterestDAO, GetFeatureOfInterestRequest> {

    private static final Set<String> CONFORMANCE_CLASSES = Collections.singleton(ConformanceClasses.SOS_V2_FEATURE_OF_INTEREST_RETRIEVAL);
    private static final String OPERATION_NAME = SosConstants.Operations.GetFeatureOfInterest.name();

    public SosGetFeatureOfInterestOperatorV20() {
        super(OPERATION_NAME, GetFeatureOfInterestRequest.class);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }

    @Override
    public ServiceResponse receive(GetFeatureOfInterestRequest sosRequest) throws OwsExceptionReport {
        checkRequestedParameter(sosRequest);

        GetFeatureOfInterestResponse response = getDao().getFeatureOfInterest(sosRequest);
        String contentType = SosConstants.CONTENT_TYPE_XML;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // check SOS version for response encoding
            String namespace;
            if (sosRequest.getVersion().equalsIgnoreCase(Sos1Constants.SERVICEVERSION)) {
                namespace = Sos1Constants.NS_SOS;
            } else if (sosRequest.getVersion().equalsIgnoreCase(Sos2Constants.SERVICEVERSION)) {
                namespace = Sos2Constants.NS_SOS_20;
            } else {
                throw new InvalidParameterValueException(OWSConstants.RequestParams.version, sosRequest.getVersion());
            }

            XmlObject encodedObject = CodingHelper.encodeObjectToXml(namespace, response);
            if (encodedObject instanceof XmlObject) {
                encodedObject.save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
                return new ServiceResponse(baos, contentType, false, true);
            } else if (encodedObject instanceof ServiceResponse) {
                return (ServiceResponse) encodedObject;
            } else {
                throw new EncoderResponseUnsupportedException();
            }
        } catch (IOException ioe) {
            throw new ErrorWhileSavingResponseToOutputStreamException(ioe);
        }
    }

    private void checkRequestedParameter(GetFeatureOfInterestRequest sosRequest) throws OwsExceptionReport {
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
            checkObservedProperties(sosRequest.getObservedProperties(), Configurator.getInstance()
                    .getCache().getObservableProperties(),
                                    Sos2Constants.GetFeatureOfInterestParams.observedProperty.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            checkProcedureIDs(sosRequest.getProcedures(),
                    Sos2Constants.GetFeatureOfInterestParams.procedure.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            checkFeatureOfInterestAndRelatedFeatureIdentifier(sosRequest.getFeatureIdentifiers(), 
                    getCache().getFeaturesOfInterest(), getCache().getRelatedFeatures(),
                                              Sos2Constants.GetFeatureOfInterestParams.featureOfInterest.name());
//            checkFeatureOfInterestIdentifiers(sosRequest.getFeatureIdentifiers(), Configurator.getInstance()
//                    .getCache().getFeaturesOfInterest(),
//                                              Sos2Constants.GetFeatureOfInterestParams.featureOfInterest.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            checkSpatialFilters(sosRequest.getSpatialFilters(),
                    Sos2Constants.GetFeatureOfInterestParams.spatialFilter.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }

        exceptions.throwIfNotEmpty();
    }

    private void checkFeatureOfInterestAndRelatedFeatureIdentifier(List<String> featureIdentifiers,
                                                                   Set<String> validFeaturesOfInterest,
                                                                   Set<String> validRelatedFeatures,
                                                                   String parameterName) throws OwsExceptionReport {
        if (featureIdentifiers != null) {
            CompositeOwsException exceptions = new CompositeOwsException();
            for (String featureOfInterest : featureIdentifiers) {
                try {
                    if (!isRelatedFetureIdentifier(featureOfInterest, validRelatedFeatures)) {
                        checkFeatureOfInterestIdentifier(featureOfInterest, validFeaturesOfInterest, parameterName);
                    }
                } catch (OwsExceptionReport e) {
                        exceptions.add(e);
                }
            }
            exceptions.throwIfNotEmpty();
        }
    }

    @Override
    public WSDLOperation getSosOperationDefinition() {
        return WSDLConstants.Operations.GET_FEATURE_OF_INTEREST;
    }
}
