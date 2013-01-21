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
package org.n52.sos.request.operator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ds.IGetFeatureOfInterestDAO;
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
import org.n52.sos.util.OwsHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosGetFeatureOfInterestOperatorV20 extends AbstractV2RequestOperator<IGetFeatureOfInterestDAO, GetFeatureOfInterestRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SosGetFeatureOfInterestOperatorV20.class.getName());
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
                String exceptionText = "Received version in request is not supported!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(
                        OWSConstants.RequestParams.version.name(), exceptionText);
            }

            XmlObject encodedObject = CodingHelper.encodeObjectToXml(namespace, response);
            if (encodedObject instanceof XmlObject) {
                encodedObject.save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
                return new ServiceResponse(baos, contentType, false, true);
            } else if (encodedObject instanceof ServiceResponse) {
                return (ServiceResponse) encodedObject;
            } else {
                String exceptionText = "The encoder response is not supported!";
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }
        } catch (IOException ioe) {
            String exceptionText = "Error occurs while saving response to output stream!";
            LOGGER.error(exceptionText, ioe);
            throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
        }
    }

    private void checkRequestedParameter(GetFeatureOfInterestRequest sosRequest) throws OwsExceptionReport {
        List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
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
                    .getCapabilitiesCacheController().getObservableProperties(),
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
            checkFeatureOfInterestIdentifiers(sosRequest.getFeatureIdentifiers(), Configurator.getInstance()
                    .getCapabilitiesCacheController().getFeatureOfInterest(),
                    Sos2Constants.GetFeatureOfInterestParams.featureOfInterest.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            checkSpatialFilters(sosRequest.getSpatialFilters(),
                    Sos2Constants.GetFeatureOfInterestParams.spatialFilter.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }

        Util4Exceptions.mergeAndThrowExceptions(exceptions);
    }

}
