package org.n52.sos.request.operator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ds.IGetCapabilitiesDAO;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.GetCapabilitiesRequest;
import org.n52.sos.response.GetCapabilitiesResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosGetCapabilitiesOperatorV20 implements IRequestOperator {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosGetCapabilitiesOperatorV20.class);

    /**
     * the data access object for the GetObservation operation
     */
    private IGetCapabilitiesDAO dao;

    /**
     * Name of the operation the listener implements
     */
    private static final String OPERATION_NAME = SosConstants.Operations.GetCapabilities.name();

    private RequestOperatorKeyType requestOperatorKeyType;

    /**
     * constructor
     * 
     */
    public SosGetCapabilitiesOperatorV20() {
        requestOperatorKeyType =
                new RequestOperatorKeyType(new ServiceOperatorKeyType(SosConstants.SOS, Sos2Constants.SERVICEVERSION),
                        OPERATION_NAME);
        this.dao = (IGetCapabilitiesDAO) Configurator.getInstance().getOperationDAOs().get(OPERATION_NAME);
        LOGGER.info("SosGetCapabilitiesOperatorV20 initialized successfully!");
    }

    @Override
    public RequestOperatorKeyType getRequestOperatorKeyType() {
        return requestOperatorKeyType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.listener.ISosRequestListener#receiveRequest(org.n52.sos.request
     * .AbstractSosRequest)
     */
    @Override
    public synchronized ServiceResponse receiveRequest(AbstractServiceRequest request) throws OwsExceptionReport {
        GetCapabilitiesRequest sosRequest;
        if (request instanceof GetCapabilitiesRequest) {
            sosRequest = (GetCapabilitiesRequest) request;
            /*
             * getting parameter acceptFormats (optional) boolean zipCompr shows
             * whether the response format should be zip (true) or xml (false)
             */
            boolean zipCompr = false;
            List<String> acceptFormats = sosRequest.getAcceptFormats();
            if (acceptFormats != null) {
                zipCompr = checkAcceptFormats(acceptFormats);
            }

            GetCapabilitiesResponse response = this.dao.getCapabilities(sosRequest);
            String contentType = SosConstants.CONTENT_TYPE_XML;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // XmlOptions xmlOptions;
            try {
                IEncoder encoder = Configurator.getInstance().getEncoder(Sos2Constants.NS_SOS_20);
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
                    String exceptionText = "Received version in request is not supported!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createInvalidParameterValueException(
                            OWSConstants.RequestParams.version.name(), exceptionText);
                }

            } catch (IOException ioe) {
                String exceptionText = "Error occurs while saving response to output stream!";
                LOGGER.error(exceptionText, ioe);
                throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
            }
        } else {
            String exceptionText = "Received request is not a SosGetCapabilitiesRequest!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createOperationNotSupportedException(request.getOperationName());
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.listener.ISosRequestListener#hasImplementedDAO()
     */
    @Override
    public boolean hasImplementedDAO() {
        if (this.dao != null) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.listener.ISosRequestListener#getOperationMetadata(java.lang
     * .String, java.lang.Object)
     */
    @Override
    public OWSOperation getOperationMetadata(String service, String version, Object connection) throws OwsExceptionReport {
        return dao.getOperationsMetadata(service, version, connection);
    }
    
    @Override
    public IExtension getExtension(Object connection) throws OwsExceptionReport {
        return dao.getExtension(connection);
    }
    
    @Override
    public Set<String> getConformanceClasses() {
        Set<String> conformanceClasses = new HashSet<String>(0);
        if (hasImplementedDAO()) {
            conformanceClasses.add("http://www.opengis.net/spec/SOS/2.0/conf/core");
        }
        return conformanceClasses;
    }

    private boolean checkAcceptFormats(List<String> formats) throws OwsExceptionReport {
        boolean zipCompr = false;

        // ints are necessary for getting the priority of the ouptuformats
        int xml = -1;
        int zip = -1;
        for (String format : formats) {
            if (format.equals(SosConstants.CONTENT_TYPE_XML)) {
                xml = formats.indexOf(format);
            } else if (format.equals(SosConstants.CONTENT_TYPE_ZIP)) {
                zip = formats.indexOf(format);
            }
        }
        if (zip == -1 && xml == -1) {
            String exceptionText =
                    "The parameter '" + SosConstants.GetCapabilitiesParams.AcceptFormats.name() + "'"
                            + " is invalid. The following values are supported: " + SosConstants.CONTENT_TYPE_XML
                            + ", " + SosConstants.CONTENT_TYPE_ZIP;
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(
                    SosConstants.GetCapabilitiesParams.AcceptFormats.name(), exceptionText);
        }

        // if zip is requested testing, whether the priority is bigger than xml
        if (zip != -1 && (zip <= xml || xml == -1)) {
            zipCompr = true;
        }

        return zipCompr;
    }

}
