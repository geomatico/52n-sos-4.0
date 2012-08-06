package org.n52.sos.request.operator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ds.IGetFeatureOfInterestDAO;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.ExceptionLevel;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.sos.SosConstants.Operations;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.GetFeatureOfInterestRequest;
import org.n52.sos.response.GetFeatureOfInterestResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.OwsHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosGetFeatureOfInterestOperatorV20 implements IRequestOperator {

    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosGetFeatureOfInterestOperatorV20.class.getName());

    /** the data access object for the GetObservation operation */
    private IGetFeatureOfInterestDAO dao;

    /** Name of the operation the listener implements */
    private static final String OPERATION_NAME = SosConstants.Operations.GetFeatureOfInterest.name();

    private RequestOperatorKeyType requestOperatorKeyType;

    /**
     * Constructor
     * 
     */
    public SosGetFeatureOfInterestOperatorV20() {
        requestOperatorKeyType =
                new RequestOperatorKeyType(new ServiceOperatorKeyType(SosConstants.SOS, Sos2Constants.SERVICEVERSION),
                        OPERATION_NAME);
        this.dao = (IGetFeatureOfInterestDAO) Configurator.getInstance().getOperationDAOs().get(OPERATION_NAME);
        LOGGER.info("SosGetFeatureOfInterestOperatorV20 initialized successfully!");
    }

    @Override
    public ServiceResponse receiveRequest(AbstractServiceRequest request) throws OwsExceptionReport {
        if (request instanceof GetFeatureOfInterestRequest) {
            List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();
            GetFeatureOfInterestRequest sosRequest = (GetFeatureOfInterestRequest) request;
            // check parameters with variable content
            try {
                SosHelper.checkServiceParameter(sosRequest.getService());
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
            try {
                OwsHelper.checkSingleVersionParameter(sosRequest.getVersion(), Configurator.getInstance()
                        .getSupportedVersions());
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
            try {
                SosHelper.checkObservedProperties(sosRequest.getObservedProperties(), Configurator.getInstance()
                        .getCapsCacheController().getObservableProperties(),
                        Sos2Constants.GetFeatureOfInterestParams.observedProperty.name());
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
            try {
                SosHelper.checkProcedureIDs(sosRequest.getProcedures(), Configurator.getInstance()
                        .getCapsCacheController().getProcedures(),
                        Sos2Constants.GetFeatureOfInterestParams.procedure.name());
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
            try {
                SosHelper.checkFeatureOfInterest(sosRequest.getFeatureIdentifiers(), Configurator.getInstance()
                        .getCapsCacheController().getFeatureOfInterest(),
                        Sos2Constants.GetFeatureOfInterestParams.featureOfInterest.name());
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
            Util4Exceptions.mergeExceptions(exceptions);

            GetFeatureOfInterestResponse response = this.dao.getFeatureOfInterest(sosRequest);
            String contentType = SosConstants.CONTENT_TYPE_XML;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // XmlOptions xmlOptions;

            try {
                // check SOS version for response encoding
                StringBuilder namespace = new StringBuilder();
                if (sosRequest.getVersion().equalsIgnoreCase(Sos1Constants.SERVICEVERSION)) {
                    namespace.append(Sos1Constants.NS_SOS);
                    // xmlOptions =
                    // SosXmlOptionsUtility.getInstance().getXmlOptions();
                } else if (sosRequest.getVersion().equalsIgnoreCase(Sos2Constants.SERVICEVERSION)) {
                    namespace.append(Sos2Constants.NS_SOS_20);
                    // xmlOptions =
                    // SosXmlOptionsUtility.getInstance().getXmlOptions4Sos2Swe200();
                } else {
                    String exceptionText = "Received version in request is not supported!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createInvalidParameterValueException(
                            OWSConstants.RequestParams.version.name(), exceptionText);
                }
                IEncoder encoder = Configurator.getInstance().getEncoder(namespace.toString());
                if (encoder != null) {
                    Object encodedObject = encoder.encode(response);
                    if (encodedObject instanceof XmlObject) {
                        ((XmlObject) encodedObject).save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
                        return new ServiceResponse(baos, contentType, false, true);
                    } else if (encodedObject instanceof ServiceResponse) {
                        return (ServiceResponse) encodedObject;
                    } else {
                        String exceptionText = "The encoder response is not supported!";
                        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                    }
                } else {
                    OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
                    throw owse;
                }
            } catch (IOException ioe) {
                String exceptionText = "Error occurs while saving response to output stream!";
                LOGGER.error(exceptionText, ioe);
                throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
            }

        } else {
            String exceptionText = "Received request is not a SosGetObservationRequest!";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createOperationNotSupportedException(request.getOperationName());
        }
    }

    @Override
    public RequestOperatorKeyType getRequestOperatorKeyType() {
        return requestOperatorKeyType;
    }

    @Override
    public boolean hasImplementedDAO() {
        if (this.dao != null) {
            return true;
        }
        return false;
    }

    @Override
    public OWSOperation getOperationMetadata(String service, String version, Object connection)
            throws OwsExceptionReport {
        return dao.getOperationsMetadata(service, version, connection);
    }

}
