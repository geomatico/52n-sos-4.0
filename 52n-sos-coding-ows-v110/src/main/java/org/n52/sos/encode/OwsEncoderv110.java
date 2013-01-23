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
package org.n52.sos.encode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.ows.x11.AddressType;
import net.opengis.ows.x11.AllowedValuesDocument.AllowedValues;
import net.opengis.ows.x11.ContactType;
import net.opengis.ows.x11.DCPDocument.DCP;
import net.opengis.ows.x11.DomainType;
import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.ows.x11.ExceptionReportDocument.ExceptionReport;
import net.opengis.ows.x11.ExceptionType;
import net.opengis.ows.x11.HTTPDocument.HTTP;
import net.opengis.ows.x11.KeywordsType;
import net.opengis.ows.x11.OperationDocument.Operation;
import net.opengis.ows.x11.OperationsMetadataDocument.OperationsMetadata;
import net.opengis.ows.x11.RangeType;
import net.opengis.ows.x11.ResponsiblePartySubsetType;
import net.opengis.ows.x11.ServiceIdentificationDocument;
import net.opengis.ows.x11.ServiceIdentificationDocument.ServiceIdentification;
import net.opengis.ows.x11.ServiceProviderDocument;
import net.opengis.ows.x11.ServiceProviderDocument.ServiceProvider;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.ows.IOWSParameterValue;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.ExceptionLevel;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OWSOperationsMetadata;
import org.n52.sos.ogc.ows.OWSParameterDataType;
import org.n52.sos.ogc.ows.OWSParameterValuePossibleValues;
import org.n52.sos.ogc.ows.OWSParameterValueRange;
import org.n52.sos.ogc.ows.OwsException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosServiceIdentification;
import org.n52.sos.ogc.ows.SosServiceProvider;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.N52XmlHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OwsEncoderv110 implements IEncoder<XmlObject, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OwsEncoderv110.class);

    private Set<EncoderKey> ENCODER_KEYS = CodingHelper.encoderKeysForElements(OWSConstants.NS_OWS,
        SosServiceIdentification.class, SosServiceProvider.class, OWSOperationsMetadata.class,
        OwsExceptionReport.class);
    
    public OwsEncoderv110() {
        LOGGER.debug("Encoder for the following keys initialized successfully: {}!", StringHelper.join(", ", ENCODER_KEYS));
    }

    @Override
    public Set<EncoderKey> getEncoderKeyType() {
        return Collections.unmodifiableSet(ENCODER_KEYS);
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.emptySet();
    }

    @Override
    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(OWSConstants.NS_OWS, OWSConstants.NS_OWS_PREFIX);
    }
    
    @Override
    public String getContentType() {
        return SosConstants.CONTENT_TYPE_XML;
    }

    @Override
    public XmlObject encode(Object element) throws OwsExceptionReport {
        return encode(element, null);
    }
    
    @Override
    public XmlObject encode(Object element, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        if (element instanceof SosServiceIdentification) {
            return encodeServiceIdentification((SosServiceIdentification) element);
        } else if (element instanceof SosServiceProvider) {
            return encodeServiceProvider((SosServiceProvider) element);
        } else if (element instanceof OWSOperationsMetadata) {
            return encodeOperationsMetadata((OWSOperationsMetadata) element);
        } else if (element instanceof OwsExceptionReport) {
            return encodeOwsExceptionReport((OwsExceptionReport) element);
        }
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
    private XmlObject encodeServiceIdentification(SosServiceIdentification sosServiceIdentification)
            throws OwsExceptionReport {
		ServiceIdentification serviceIdent;
        if (sosServiceIdentification.getServiceIdentification() != null) {
            
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
		} else {
			/* TODO check for required fields and fail on missing ones */
			serviceIdent = ServiceIdentification.Factory.newInstance();
			serviceIdent.addAccessConstraints(sosServiceIdentification.getAccessConstraints());
			serviceIdent.setFees(sosServiceIdentification.getFees());
			serviceIdent.addNewAbstract().setStringValue(sosServiceIdentification.getAbstract());
			serviceIdent.addNewServiceType().setStringValue(sosServiceIdentification.getServiceType());
			serviceIdent.addNewTitle().setStringValue(sosServiceIdentification.getTitle());
		}
		// set service type versions
		if (sosServiceIdentification.getVersions() != null && !sosServiceIdentification.getVersions().isEmpty()) {
			serviceIdent.setServiceTypeVersionArray(sosServiceIdentification.getVersions().toArray(new String[sosServiceIdentification.getVersions().size()]));
		}

		// set Profiles
		if (sosServiceIdentification.getProfiles() != null && !sosServiceIdentification.getProfiles().isEmpty()) {
			serviceIdent.setProfileArray(sosServiceIdentification.getProfiles().toArray(new String[sosServiceIdentification.getProfiles().size()]));
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
    private XmlObject encodeServiceProvider(SosServiceProvider sosServiceProvider) throws OwsExceptionReport {
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
			/* TODO check for required fields and fail on missing ones */
			ServiceProvider serviceProvider = ServiceProvider.Factory.newInstance();
			serviceProvider.setProviderName(sosServiceProvider.getName());
			serviceProvider.addNewProviderSite().setHref(sosServiceProvider.getSite());
			ResponsiblePartySubsetType responsibleParty = serviceProvider.addNewServiceContact();
			responsibleParty.setIndividualName(sosServiceProvider.getIndividualName());
			responsibleParty.setPositionName(sosServiceProvider.getPositionName());
			ContactType contact = responsibleParty.addNewContactInfo();
			contact.addNewPhone().addVoice(sosServiceProvider.getPhone());
			AddressType address = contact.addNewAddress();
			address.addDeliveryPoint(sosServiceProvider.getDeliveryPoint());
			address.addElectronicMailAddress(sosServiceProvider.getMailAddress());
			address.setAdministrativeArea(sosServiceProvider.getAdministrativeArea());
			address.setCity(sosServiceProvider.getCity());
			address.setCountry(sosServiceProvider.getCountry());
			address.setPostalCode(sosServiceProvider.getPostalCode());
			return serviceProvider;
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
        OperationsMetadata xbMeta =
                OperationsMetadata.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        for (OWSOperation operationMetadata : operationsMetadata.getOperations()) {
            Operation operation = xbMeta.addNewOperation();
            // name
            operation.setName(operationMetadata.getOperationName());
            // dcp
            encodeDCP(operation.addNewDCP(), operationMetadata.getDcp());
            // parameter
            if (operationMetadata.getParameterValues() != null) {
                for (String parameterName : operationMetadata.getParameterValues().keySet()) {
                    setParameterValue(operation.addNewParameter(), parameterName, operationMetadata
                            .getParameterValues().get(parameterName));
                }
            }
            // if (operationMetadata.getParameterMinMaxMap() != null
            // && !operationMetadata.getParameterMinMaxMap().isEmpty()) {
            // for (String parameterName :
            // operationMetadata.getParameterMinMaxMap().keySet()) {
            // setParamMinMax(operation.addNewParameter(), parameterName,
            // operationMetadata
            // .getParameterMinMaxMap().get(parameterName));
            // }
            // }
        }
        // set SERVICE and VERSION for all operations.
        for (String name : operationsMetadata.getCommonValues().keySet()) {
            setParameterValue(xbMeta.addNewParameter(), name, operationsMetadata.getCommonValues().get(name));
        }
        return xbMeta;
    }

    private ExceptionReportDocument encodeOwsExceptionReport(OwsExceptionReport owsExceptionReport) {
        ExceptionReportDocument erd =
                ExceptionReportDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        ExceptionReport er = erd.addNewExceptionReport();
        // er.setLanguage("en");
        er.setVersion(owsExceptionReport.getVersion());
        if (owsExceptionReport.getExceptions() != null) {
            List<ExceptionType> exceptionTypes = new ArrayList<ExceptionType>(owsExceptionReport.getExceptions().size());
            for (OwsException owsException : owsExceptionReport.getExceptions()) {
                ExceptionType exceptionType =
                        ExceptionType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
                String exceptionCode = null;
                if (owsException.getCode() == null)
                {
                	exceptionCode = OWSConstants.OwsExceptionCode.NoApplicableCode.toString();
                }
                else
                {
                	exceptionCode = owsException.getCode().toString();
                }
                exceptionType.setExceptionCode(exceptionCode);
                if (owsException.getLocator() != null) {
                    exceptionType.setLocator(owsException.getLocator());
                }
                StringBuilder exceptionText = new StringBuilder();
                if (owsException.getMessages() != null) {
                    for (String message : owsException.getMessages()) {
                        exceptionText.append(message);
                        exceptionText.append("\n");
                    }
                }
                if (owsException.getException() != null) {
                    Exception exception = owsException.getException();
                    String name = exception.getClass().getName();
                    String message = exception.getMessage();
                    StackTraceElement[] stackTraces = exception.getStackTrace();

                    exceptionText.append("[EXC] internal service exception");
                    if (owsExceptionReport.getExcLevel().compareTo(ExceptionLevel.PlainExceptions) == 0) {
                        exceptionText.append(". Message: ");
                        exceptionText.append(message);
                    } else if (owsExceptionReport.getExcLevel().compareTo(ExceptionLevel.DetailedExceptions) == 0) {
                        exceptionText.append(": ").append(name).append("\n");
                        exceptionText.append("[EXC] message: ").append(message).append("\n");
                        for (StackTraceElement stackTraceElement : stackTraces) {
                            exceptionText.append("[EXC]").append(stackTraceElement.toString()).append("\n");
                        }
                    } else {
                        LOGGER.warn("addCodedException: unknown ExceptionLevel " + "("
                                + owsExceptionReport.getExcLevel().toString() + ")occurred.");
                    }
                }
                exceptionType.addExceptionText(exceptionText.toString());
                exceptionTypes.add(exceptionType);
            }
            er.setExceptionArray(exceptionTypes.toArray(new ExceptionType[exceptionTypes.size()]));
        }
        N52XmlHelper.setSchemaLocationToDocument(erd, N52XmlHelper.getSchemaLocationForOWS110());
        return erd;
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
        if (supportedDcp.containsKey(SosConstants.HTTP_GET)) {
            for (String dcpGet : supportedDcp.get(SosConstants.HTTP_GET)) {
                http.addNewGet().setHref(dcpGet);
            }
        }
        if (supportedDcp.containsKey(SosConstants.HTTP_POST)) {
            for (String dcpPost : supportedDcp.get(SosConstants.HTTP_POST)) {
                http.addNewPost().setHref(dcpPost);
            }
        }
        // TODO add if ows supports more than get and post
        /*
        if (supportedDcp.containsKey(SosConstants.HTTP_PUT)) {
            for (String dcpPut : supportedDcp.get(SosConstants.HTTP_PUT)) {
                http.addNewPut().setHref(dcpPut);
            }
        }
        if (supportedDcp.containsKey(SosConstants.HTTP_DELETE)) {
            for (String dcpDelete : supportedDcp.get(SosConstants.HTTP_DELETE)) {
                http.addNewDelete().setHref(dcpDelete);
            }
        }
        */
    }

    private void setParameterValue(DomainType domainType, String parameterName,
            List<IOWSParameterValue> parameterValues) throws OwsExceptionReport {
        if (parameterValues != null && !parameterValues.isEmpty()) {
            domainType.setName(parameterName);
            for (IOWSParameterValue parameterValue : parameterValues) {
                if (parameterValue instanceof OWSParameterValuePossibleValues) {
                    setParamList(domainType, (OWSParameterValuePossibleValues) parameterValue);
                } else if (parameterValue instanceof OWSParameterValueRange) {
                    setParamRange(domainType, (OWSParameterValueRange) parameterValue);
                } else if (parameterValue instanceof OWSParameterDataType) {
                    setParamDataType(domainType, (OWSParameterDataType) parameterValue);
                }
            }
        } else {
            domainType.setName(parameterName);
            domainType.addNewNoValues();
        }
    }

    /**
     * Sets operation parameters to AnyValue, NoValues or AllowedValues.
     * 
     * @param domainType
     *            Paramter.
     * @param name
     *            Parameter name.
     * @param parameterValue
     *            .getValues() List of values.
     */
    private void setParamList(DomainType domainType, OWSParameterValuePossibleValues parameterValue) {
        if (parameterValue.getValues() != null) {
            if (!parameterValue.getValues().isEmpty()) {
                AllowedValues allowedValues = null;
                for (String value : parameterValue.getValues()) {
                    if (value == null) {
                        domainType.addNewNoValues();
                        break;
                    } else {
                        if (allowedValues == null) {
                            allowedValues = domainType.addNewAllowedValues();
                        }
                        allowedValues.addNewValue().setStringValue(value);
                    }
                }
            } else {
                domainType.addNewAnyValue();
            }
        } else {
            domainType.addNewNoValues();
        }
    }

    private void setParamDataType(DomainType domainType, OWSParameterDataType parameterValue) {
        if (parameterValue.getReference() != null && !parameterValue.getReference().isEmpty()) {
            domainType.addNewDataType().setReference(parameterValue.getReference());
        } else {
            domainType.addNewNoValues();
        }

    }

    /**
     * Sets the EventTime parameter.
     * 
     * @param domainType
     *            Parameter.
     * @param parameterValue
     * @throws OwsExceptionReport
     */
    private void setParamRange(DomainType domainType, OWSParameterValueRange parameterValue) throws OwsExceptionReport {
        if (parameterValue.getMinValue() != null && parameterValue.getMaxValue() != null) {
            if (!parameterValue.getMinValue().isEmpty() && !parameterValue.getMaxValue().isEmpty()) {
                RangeType range = domainType.addNewAllowedValues().addNewRange();
                range.addNewMinimumValue().setStringValue(parameterValue.getMinValue());
                range.addNewMaximumValue().setStringValue(parameterValue.getMaxValue());
            } else {
                domainType.addNewAnyValue();
            }
        } else {
            domainType.addNewNoValues();
        }
    }

}
