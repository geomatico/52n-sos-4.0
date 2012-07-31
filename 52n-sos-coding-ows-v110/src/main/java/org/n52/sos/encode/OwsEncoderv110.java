package org.n52.sos.encode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.opengis.ows.x11.AllowedValuesDocument.AllowedValues;
import net.opengis.ows.x11.DCPDocument.DCP;
import net.opengis.ows.x11.DomainType;
import net.opengis.ows.x11.HTTPDocument.HTTP;
import net.opengis.ows.x11.KeywordsType;
import net.opengis.ows.x11.OperationDocument.Operation;
import net.opengis.ows.x11.OperationsMetadataDocument.OperationsMetadata;
import net.opengis.ows.x11.RangeType;
import net.opengis.ows.x11.ServiceIdentificationDocument;
import net.opengis.ows.x11.ServiceIdentificationDocument.ServiceIdentification;
import net.opengis.ows.x11.ServiceProviderDocument;
import net.opengis.ows.x11.ServiceProviderDocument.ServiceProvider;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.MinMax;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OWSOperationsMetadata;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosServiceIdentification;
import org.n52.sos.ogc.ows.SosServiceProvider;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OwsEncoderv110 implements IEncoder<XmlObject, Object> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OwsEncoderv110.class);

    private List<EncoderKeyType> encoderKeyTypes;

    public OwsEncoderv110() {
        encoderKeyTypes = new ArrayList<EncoderKeyType>();
        encoderKeyTypes.add(new EncoderKeyType(OWSConstants.NS_OWS));
        StringBuilder builder = new StringBuilder();
        for (EncoderKeyType encoderKeyType : encoderKeyTypes) {
            builder.append(encoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        LOGGER.info("Encoder for the following keys initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<EncoderKeyType> getEncoderKeyType() {
        return encoderKeyTypes;
    }

    @Override
    public XmlObject encode(Object element) throws OwsExceptionReport {
        if (element instanceof SosServiceIdentification) {
            return encodeServiceIdentification((SosServiceIdentification) element);
        } else if (element instanceof SosServiceProvider) {
            return encodeServiceProvider((SosServiceProvider) element);
        } else if (element instanceof OWSOperationsMetadata) {
            return encodeOperationsMetadata((OWSOperationsMetadata) element);
        }
        return null;
    }

    @Override
    public XmlObject encode(Object response, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Set the service identification information
     * 
     * @param serviceIdentification
     *            XML object loaded from file.
     * @param xbCaps
     *            XML capabilities document.
     * @throws OwsExceptionReport
     *             if the file is invalid.
     */
    private XmlObject encodeServiceIdentification(SosServiceIdentification sosServiceIdentification) throws OwsExceptionReport {
        if (sosServiceIdentification.getServiceIdentification() != null) {
            ServiceIdentification serviceIdent;
            if (sosServiceIdentification.getServiceIdentification() instanceof ServiceIdentificationDocument) {
                serviceIdent =
                        ((ServiceIdentificationDocument) sosServiceIdentification.getServiceIdentification())
                                .getServiceIdentification();
            } else if (sosServiceIdentification.getServiceIdentification() instanceof ServiceIdentification) {
                serviceIdent = (ServiceIdentification) sosServiceIdentification.getServiceIdentification();
            } else {
                String exceptionText =
                        "The service identification file is not a ServiceIdentificationDocument, ServiceIdentification or invalid! Check the file in the Tomcat webapps: /SOS_webapp/WEB-INF/conf/capabilities/.";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }
            // set service type versions
            if (sosServiceIdentification.getVersions() != null && !sosServiceIdentification.getVersions().isEmpty()) {
                serviceIdent.setServiceTypeVersionArray(sosServiceIdentification.getVersions().toArray(new String[0]));
            }

            // set Profiles
            if (sosServiceIdentification.getProfiles() != null && !sosServiceIdentification.getProfiles().isEmpty()) {
                serviceIdent.setProfileArray(sosServiceIdentification.getProfiles().toArray(new String[0]));
            }
            // set keywords if they're not already in the service identification
            // doc
            if (sosServiceIdentification.getKeywords() != null && !sosServiceIdentification.getKeywords().isEmpty()) {
                if (serviceIdent.getKeywordsArray().length == 0) {
                    KeywordsType keywordsType = serviceIdent.addNewKeywords();
                    for (String keyword : sosServiceIdentification.getKeywords()) {
                        keywordsType.addNewKeyword().setStringValue(keyword.trim());
                    }
                }
            }

           return serviceIdent;
        } else {
            String exceptionText =
                    "The service identification file is not a ServiceIdentificationDocument, ServiceIdentification or invalid! Check the file in the Tomcat webapps: /SOS_webapp/WEB-INF/conf/capabilities/.";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
    }

    /**
     * Set the service provider information
     * 
     * @param serviceProvider
     *            XML object loaded from file.
     * @param xbCaps
     *            XML capabilities document.
     * @throws OwsExceptionReport
     *             if the file is invalid.
     */
    private XmlObject encodeServiceProvider(SosServiceProvider sosServiceProvider)
            throws OwsExceptionReport {
        if (sosServiceProvider.getServiceProvider() != null) {
            if (sosServiceProvider.getServiceProvider() instanceof ServiceProviderDocument) {
                return ((ServiceProviderDocument) sosServiceProvider.getServiceProvider()).getServiceProvider();
            } else if (sosServiceProvider.getServiceProvider() instanceof ServiceProvider) {
                return sosServiceProvider.getServiceProvider();
            } else {
                String exceptionText =
                        "The service identification file is not a ServiceProviderDocument, ServiceProvider or invalid! Check the file in the Tomcat webapps: /SOS_webapp/WEB-INF/conf/capabilities/.";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }
        } else {
            String exceptionText =
                    "The service identification file is not a ServiceProviderDocument, ServiceProvider or invalid! Check the file in the Tomcat webapps: /SOS_webapp/WEB-INF/conf/capabilities/.";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }

    }

    /**
     * Sets the OperationsMetadata section to the capabilities document.
     * 
     * @param operationsMetadata
     *            SOS metadatas for the operations
     * @throws OwsExceptionReport
     *             if an error occurs
     */
    private OperationsMetadata encodeOperationsMetadata(OWSOperationsMetadata operationsMetadata)
            throws OwsExceptionReport {
        OperationsMetadata xbMeta = OperationsMetadata.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        for (OWSOperation operationMetadata : operationsMetadata.getOperations()) {
            Operation operation = xbMeta.addNewOperation();
            // name
            operation.setName(operationMetadata.getOperationName());
            // dcp
            encodeDCP(operation.addNewDCP(), operationMetadata.getDcp());
            // parameter
            for (String parameterName : operationMetadata.getParameterValues().keySet()) {
                setParamList(operation.addNewParameter(), parameterName,
                        operationMetadata.getParameterValues().get(parameterName));
            }
            if (operationMetadata.getParameterMinMaxMap() != null
                    && !operationMetadata.getParameterMinMaxMap().isEmpty()) {
                for (String parameterName : operationMetadata.getParameterMinMaxMap().keySet()) {
                    setParamMinMax(operation.addNewParameter(), parameterName, operationMetadata
                            .getParameterMinMaxMap().get(parameterName));
                }
            }
        }
        // set SERVICE and VERSION for all operations.
        for (String name : operationsMetadata.getCommonValues().keySet()) {
            setParamList(xbMeta.addNewParameter(), name, operationsMetadata.getCommonValues().get(name));
        }
        return xbMeta;
    }

    /**
     * Sets the DCP operation.
     * 
     * @param dcp
     *            The operation.
     * @param get
     *            Add GET.
     */
    private void encodeDCP(DCP dcp, Map<String, List<String>> supportedDcp) {
        HTTP http = dcp.addNewHTTP();
        for (String dcpGet : supportedDcp.get(SosConstants.HTTP_GET)) {
            http.addNewGet().setHref(dcpGet);
        }
        for (String dcpPost : supportedDcp.get(SosConstants.HTTP_POST)) {
            http.addNewPost().setHref(dcpPost);
        }
    }

    /**
     * Sets operation parameters to AnyValue, NoValues or AllowedValues.
     * 
     * @param domainType
     *            Paramter.
     * @param name
     *            Parameter name.
     * @param values
     *            List of values.
     */
    private void setParamList(DomainType domainType, String name, Collection<String> values) {
        domainType.setName(name);
        if (values != null) {
            if (!values.isEmpty()) {
                AllowedValues allowedValues = domainType.addNewAllowedValues();
                for (String value : values) {
                    allowedValues.addNewValue().setStringValue(value);
                }
            } else {
                domainType.addNewAnyValue();
            }
        } else {
            domainType.addNewNoValues();
        }
    }

    /**
     * Sets the EventTime parameter.
     * 
     * @param domainType
     *            Parameter.
     * @param map
     * @throws OwsExceptionReport
     */
    private void setParamMinMax(DomainType domainType, String parameterName, Map<MinMax, String> map)
            throws OwsExceptionReport {
        domainType.setName(parameterName);

        // set the min value
        String minValue = map.get(OWSConstants.MinMax.MIN);
        String maxValue = map.get(OWSConstants.MinMax.MAX);
        if (minValue != null && !minValue.isEmpty() && maxValue != null && !maxValue.isEmpty()) {
            RangeType range = domainType.addNewAllowedValues().addNewRange();
            range.addNewMinimumValue().setStringValue(minValue);
            range.addNewMaximumValue().setStringValue(maxValue);
        } else {
            domainType.addNewNoValues();
        }
    }

}
