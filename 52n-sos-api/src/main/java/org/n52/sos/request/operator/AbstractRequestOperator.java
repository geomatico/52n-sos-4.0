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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.n52.sos.cache.ContentCache;
import org.n52.sos.ds.OperationDAO;
import org.n52.sos.event.SosEventBus;
import org.n52.sos.event.events.RequestEvent;
import org.n52.sos.ogc.filter.SpatialFilter;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <D> the OperationDAO of this operator
 * @param <R> The AbstractServiceRequest to handle
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class AbstractRequestOperator<D extends OperationDAO, R extends AbstractServiceRequest> implements IRequestOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRequestOperator.class);
    private final D dao;
    private final String operationName;
    private final RequestOperatorKeyType requestOperatorKeyType;
    private final Class<R> requestType;

    @SuppressWarnings("unchecked")
    public AbstractRequestOperator(String service, String version, String operationName, Class<R> requestType) {
        this.operationName = operationName;
        this.requestOperatorKeyType = new RequestOperatorKeyType(new ServiceOperatorKeyType(service, version), operationName);
        this.requestType = requestType;
        this.dao = (D) Configurator.getInstance().getOperationDaoRepository().getOperationDAO(operationName);
        if (this.dao == null) {
            throw new NullPointerException(String
                    .format("IOperationDao for Operation %s has no implementation!", operationName));
        }
        LOGGER.info("{} initialized successfully!", getClass().getSimpleName());
    }

    protected D getDao() {
        return this.dao;
    }

    @Override
    public IExtension getExtension() throws OwsExceptionReport {
    	if (hasImplementedDAO()) {
    		return getDao().getExtension();
    	}
    	return null;
    }

    @Override
    public OWSOperation getOperationMetadata(String service, String version) throws OwsExceptionReport {
    	if (hasImplementedDAO()) {
    		return getDao().getOperationsMetadata(service, version);
    	}
    	return null;
    }

    protected String getOperationName() {
        return this.operationName;
    }

    @Override
    public boolean hasImplementedDAO() {
        return getDao() != null;
    }

    @Override
    public RequestOperatorKeyType getRequestOperatorKeyType() {
        return requestOperatorKeyType;
    }

    protected abstract ServiceResponse receive(R request) throws OwsExceptionReport;

    @Override
    public ServiceResponse receiveRequest(AbstractServiceRequest request) throws OwsExceptionReport {
		SosEventBus.fire(new RequestEvent(request));
        if (requestType.isAssignableFrom(request.getClass())) {
            return receive(requestType.cast(request));
        } else {
            String exceptionText = String.format("Received request is not a %s!", requestType.getSimpleName());
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createOperationNotSupportedException(request.getOperationName());
        }
    }
    
    protected ContentCache getCache() {
        return Configurator.getInstance().getCache();
    }

	 /**
     * method checks whether this SOS supports the requested versions
     *
     * @param versions
     *            the requested versions of the SOS
     *
     * @throws OwsExceptionReport
     *             if this SOS does not support the requested versions
     */
    protected List<String> checkAcceptedVersionsParameter(List<String> versions, Set<String> supportedVersions)
            throws OwsExceptionReport {

        List<String> validVersions = new LinkedList<String>();
        if (versions != null) {

            for (String version : versions) {
                if (supportedVersions.contains(version)) {
                    validVersions.add(version);
                }
            }
            if (validVersions.isEmpty()) {
                String exceptionText =
                        "The parameter '" + SosConstants.GetCapabilitiesParams.AcceptVersions.name() + "'"
                                + " does not contain a supported Service version!";
                LOGGER.error(exceptionText);
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OWSConstants.OwsExceptionCode.VersionNegotiationFailed,
                        SosConstants.GetCapabilitiesParams.AcceptVersions.name(), exceptionText);
                throw se;
            }
            return validVersions;
        } else {
            OwsExceptionReport owse =
                    Util4Exceptions
                            .createMissingParameterValueException(SosConstants.GetCapabilitiesParams.AcceptVersions
                                    .name());
            LOGGER.error("checkAcceptedVersionsParameters", owse);
            throw owse;
        }
    }

	/**
     * method checks whether this SOS supports the requested versions
     *
     * @param versions
     *            the requested versions of the SOS
     *
     * @throws OwsExceptionReport
     *             if this SOS does not support the requested versions
     */
    protected List<String> checkAcceptedVersionsParameter(List<String> versions)
            throws OwsExceptionReport {
		return checkAcceptedVersionsParameter(versions, Configurator.getInstance()
				.getServiceOperatorRepository().getSupportedVersions());
    }

    /**
     * method checks whether this SOS supports the single requested version
     *
     * @param request
     *            the request
     * @throws OwsExceptionReport
     *             if this SOS does not support the requested versions
     */
    protected void checkSingleVersionParameter(AbstractServiceRequest request)
            throws OwsExceptionReport {

        // if version is incorrect, throw exception
        if (request.getVersion() == null || !Configurator.getInstance().getServiceOperatorRepository().isVersionSupported(request.getVersion())) {

            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("The parameter '");
            exceptionText.append(OWSConstants.RequestParams.version.name());
            exceptionText.append("'  does not contain version(s) supported by this Service: '");
            for (String supportedVersion : Configurator.getInstance().getServiceOperatorRepository().getSupportedVersions()) {
                exceptionText.append(supportedVersion);
                exceptionText.append("', '");
            }
            exceptionText.delete(exceptionText.lastIndexOf("', '"), exceptionText.length());
            exceptionText.append("'!");
            LOGGER.error("The accepted versions parameter is incorrect.");
            OwsExceptionReport owse = new OwsExceptionReport();
            owse.addCodedException(OWSConstants.OwsExceptionCode.InvalidParameterValue, OWSConstants.RequestParams.version.name(),
                    exceptionText.toString());

            throw owse;
        }
    }

    /**
     * method checks, whether the passed string containing the requested
     * versions of the SOS contains the versions, the 52n SOS supports
     *
     * @param versionsString
     *            comma seperated list of requested service versions
     * @throws OwsExceptionReport
     *             if the versions list is empty or no matching version is
     *             contained
     */
    protected void checkAcceptedVersionsParameter(String versionsString, Set<String> supportedVersions)
            throws OwsExceptionReport {
        // check acceptVersions
        if (versionsString != null && !versionsString.isEmpty()) {
            String[] versionsArray = versionsString.split(",");
            checkAcceptedVersionsParameter(Arrays.asList(versionsArray), supportedVersions);
        } else {
            OwsExceptionReport se =
                    Util4Exceptions
                            .createMissingParameterValueException(SosConstants.GetCapabilitiesParams.AcceptVersions
                                    .name());
            LOGGER.error("checkAcceptedVersionsParameter", se);
            throw se;
        }
    }

	/**
     * method checks, whether the passed string containing the requested
     * versions of the SOS contains the versions, the 52n SOS supports
     *
     * @param versionsString
     *            comma seperated list of requested service versions
     * @throws OwsExceptionReport
     *             if the versions list is empty or no matching version is
     *             contained
     */
    protected void checkAcceptedVersionsParameter(String versionsString)
            throws OwsExceptionReport {
		checkAcceptedVersionsParameter(versionsString, Configurator.getInstance()
				.getServiceOperatorRepository().getSupportedVersions());
    }


	/**
     * checks whether the required service parameter is correct
     *
     * @param service
     *            service parameter of the request
     * @throws OwsExceptionReport
     *             if service parameter is incorrect
     */
    protected static void checkServiceParameter(String service) throws OwsExceptionReport {

        // if service==null, throw exception with missing parameter value code
        if (service == null || service.equalsIgnoreCase("NOT_SET")) {
            OwsExceptionReport se =
                    Util4Exceptions.createMissingParameterValueException(SosConstants.GetCapabilitiesParams.service
                            .name());
            LOGGER.debug("checkServiceParameter", se);
            throw se;
        }
        // if not null, but incorrect, throw also exception
        else if (!service.equals(SosConstants.SOS)) {
            String exceptionText = String.format(
                    "The value of the mandatory parameter '%s' must be '%s'. Delivered value was: %s",
                    SosConstants.GetCapabilitiesParams.service.toString(),  SosConstants.SOS, service);
            LOGGER.error(exceptionText);
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OWSConstants.OwsExceptionCode.InvalidParameterValue,
                    SosConstants.GetCapabilitiesParams.service.toString(), exceptionText);
            throw se;
        }
    }

	/**
     * checks whether the requested sensor ID is valid
     *
     * @param procedureID
     *            the sensor ID which should be checked
     * @param validProcedures the valid procedure identifiers
     * @param parameterName the parameter name
     * @throws OwsExceptionReport
     *             if the value of the sensor ID parameter is incorrect
     */
    protected void checkProcedureID(String procedureID, Collection<String> validProcedures, String parameterName)
            throws OwsExceptionReport {
        // null or an empty String
        if (procedureID == null || procedureID.isEmpty()) {
            String exceptionText = String.format(
                    "The value of the mandatory parameter '%s' was not found in the request or is incorrect!", parameterName);
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createMissingParameterValueException(parameterName);
        }
        if (!validProcedures.contains(procedureID)) {
            String exceptionText = String.format(
                    "The value of the '%s' parameter is incorrect. Please check the capabilities response document for valid values!", parameterName);
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
        }
    }

	protected void checkProcedureIDs(Collection<String> procedureIDs, String parameterName) throws OwsExceptionReport {
		checkProcedureIDs(procedureIDs, parameterName, Configurator.getInstance()
                .getCache().getProcedures());
	}

    protected void checkProcedureIDs(Collection<String> procedureIDs, String parameterName, Collection<String> validProcedures) throws OwsExceptionReport {
        if (procedureIDs != null) {
            List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
            for (String procedureID : procedureIDs) {
                try {
                    checkProcedureID(procedureID, validProcedures, parameterName);
                } catch (OwsExceptionReport owse) {
                    exceptions.add(owse);
                }
            }
            Util4Exceptions.mergeAndThrowExceptions(exceptions);
        }
    }

	 protected void checkObservationID(String observationID, Collection<String> validObservations, String parameterName) throws OwsExceptionReport {
        if (observationID == null || observationID.isEmpty()) {
            String exceptionText = String.format("The value of the mandatory parameter '%s' was not found in the request or is incorrect!",parameterName);
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createMissingParameterValueException(parameterName);
        }
        if (!validObservations.contains(observationID)) {
            String exceptionText = String.format("The value of the '%s' parameter is incorrect. Please check the capabilities response document for valid values!", parameterName);
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
        }
    }

    protected void checkObservationIDs(Collection<String> observationIDs, Collection<String> validObservations,
            String parameterName) throws OwsExceptionReport {
        if (observationIDs != null) {
            List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
            for (String procedureID : observationIDs) {
                try {
                    checkObservationID(procedureID, validObservations, parameterName);
                } catch (OwsExceptionReport owse) {
                    exceptions.add(owse);
                }
            }
            Util4Exceptions.mergeAndThrowExceptions(exceptions);
        }
    }

    protected void checkFeatureOfInterestIdentifiers(List<String> featuresOfInterest,
            Collection<String> validFeatureOfInterest, String parameterName) throws OwsExceptionReport {
        if (featuresOfInterest != null) {
            List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
            for (String featureOfInterest : featuresOfInterest) {
                try {
                    checkFeatureOfInterestIdentifier(featureOfInterest, validFeatureOfInterest, parameterName);
                } catch (OwsExceptionReport e) {
                    exceptions.add(e);
                }
            }
            Util4Exceptions.mergeAndThrowExceptions(exceptions);
        }
    }

    protected void checkFeatureOfInterestIdentifier(String featureOfInterest,
            Collection<String> validFeatureOfInterest, String parameterName) throws OwsExceptionReport {
        if (featureOfInterest == null || featureOfInterest.isEmpty()) {
            String exceptionText = String.format(
                    "The value of the parameter '%s' was not found in the request or is incorrect!", parameterName);
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createMissingParameterValueException(parameterName);
        }
        if (!validFeatureOfInterest.contains(featureOfInterest)) {
            String exceptionText = String.format(
                    "The value '%s' of the parameter '%s' is invalid", featureOfInterest, parameterName);
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
        }
    }
    
    protected boolean isRelatedFetureIdentifier(String relatedFeature,
            Collection<String> validRelatedFeatures) throws OwsExceptionReport {
        if (relatedFeature == null || relatedFeature.isEmpty()) {
           return false;
        }
        return validRelatedFeatures.contains(relatedFeature);
    }
    
    protected void checkRelatedFeatureIdentifier(String relatedFeature,
            Collection<String> validRelatedFeatures, String parameterName) throws OwsExceptionReport {
        if (relatedFeature == null || relatedFeature.isEmpty()) {
            String exceptionText = String.format(
                    "The value of the parameter '%s' was not found in the request or is incorrect!", parameterName);
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createMissingParameterValueException(parameterName);
        }
        if (!validRelatedFeatures.contains(relatedFeature)) {
            String exceptionText = String.format(
                    "The value '%s' of the parameter '%s' is invalid", relatedFeature, parameterName);
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
        }
    }

    protected void checkObservedProperties(List<String> observedProperties,
            Collection<String> validObservedProperties, String parameterName) throws OwsExceptionReport {
        if (observedProperties != null) {
            List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
            for (String observedProperty : observedProperties) {
                try {
                    checkObservedProperty(observedProperty, validObservedProperties, parameterName);
                } catch (OwsExceptionReport e) {
                    exceptions.add(e);
                }
            }
            Util4Exceptions.mergeAndThrowExceptions(exceptions);
        }
    }

    protected void checkObservedProperty(String observedProperty, Collection<String> validObservedProperties,
            String parameterName) throws OwsExceptionReport {
        if (observedProperty == null || observedProperty.isEmpty()) {
            String exceptionText =
                    "The value of the parameter '" + parameterName + "' was not found in the request or is not set!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createMissingParameterValueException(parameterName);
        }
        if (!validObservedProperties.contains(observedProperty)) {
            String exceptionText =
                    "The value '" + observedProperty + "' of the parameter '" + parameterName + "' is invalid";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
        }
    }

    protected void checkOfferings(Set<String> offerings, Collection<String> validOfferings, String parameterName)
            throws OwsExceptionReport {
        if (offerings != null) {
            List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
            for (String offering : offerings) {
                try {
                    checkObservedProperty(offering, validOfferings, parameterName);
                } catch (OwsExceptionReport e) {
                    exceptions.add(e);
                }
            }
            Util4Exceptions.mergeAndThrowExceptions(exceptions);
        }
    }

    protected void checkOffering(String offering, Collection<String> validOfferings, String parameterName)
            throws OwsExceptionReport {
        if (offering == null || offering.isEmpty()) {
            String exceptionText =
                    "The value of the parameter '" + parameterName + "' was not found in the request or is not set!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createMissingParameterValueException(parameterName);
        }
        if (!validOfferings.contains(offering)) {
            String exceptionText = "The value '" + offering + "' of the parameter '" + parameterName + "' is invalid";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
        }
    }

	 protected void checkSpatialFilters(List<SpatialFilter> spatialFilters, String name) throws OwsExceptionReport {
        // TODO make supported ValueReferences dynamic
        if (spatialFilters != null) {
            for (SpatialFilter spatialFilter : spatialFilters) {
                checkSpatialFilter(spatialFilter, name);
            }
        }

    }

    protected void checkSpatialFilter(SpatialFilter spatialFilter, String name) throws OwsExceptionReport {
        // TODO make supported ValueReferences dynamic
        if (spatialFilter != null) {
            if (spatialFilter.getValueReference() == null
                    || (spatialFilter.getValueReference() != null && spatialFilter.getValueReference().isEmpty())) {
                String exceptionText =
                        "The value of the parameter '" + SosConstants.Filter.ValueReference.name()
                                + "' was not found in the request or is not set!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createMissingParameterValueException(SosConstants.Filter.ValueReference.name());
            } else if (!spatialFilter.getValueReference().equals("sams:shape")
                    && !spatialFilter.getValueReference().equals(
                            "om:featureOfInterest/sams:SF_SpatialSamplingFeature/sams:shape")
                    && !spatialFilter.getValueReference().equals("om:featureOfInterest/*/sams:shape")) {
                String exceptionText =
                        "The value of the parameter '" + SosConstants.Filter.ValueReference.name() + "' is invalid!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(SosConstants.Filter.ValueReference.name(),
                        exceptionText);
            }
        }
    }

    protected void checkTemporalFilter(List<TemporalFilter> temporalFilters, String name)
            throws OwsExceptionReport {
        // TODO make supported ValueReferences dynamic
        if (temporalFilters != null) {
            for (TemporalFilter temporalFilter : temporalFilters) {
                if (temporalFilter.getValueReference() == null
                        || (temporalFilter.getValueReference() != null && temporalFilter.getValueReference().isEmpty())) {
                    String exceptionText =
                            "The value of the parameter '" + SosConstants.Filter.ValueReference.name()
                                    + "' was not found in the request or is not set!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createMissingParameterValueException(SosConstants.Filter.ValueReference
                            .name());
                } else if (!temporalFilter.getValueReference().equals("phenomenonTime")
                        && !temporalFilter.getValueReference().equals("om:phenomenonTime")
                        && !temporalFilter.getValueReference().equals("resultTime")
                        && !temporalFilter.getValueReference().equals("om:resultTime")
                        && !temporalFilter.getValueReference().equals("validTime")
                        && !temporalFilter.getValueReference().equals("om:validTime")) {
                    String exceptionText =
                            "The value of the parameter '" + SosConstants.Filter.ValueReference.name()
                                    + "' was not found in the request or is not set!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createInvalidParameterValueException(
                            SosConstants.Filter.ValueReference.name(), exceptionText);
                }
            }
        }
    }

    protected void checkResultTemplate(String resultTemplate, String parameterName) throws OwsExceptionReport {
        if (resultTemplate == null || (resultTemplate != null && resultTemplate.isEmpty())) {
            throw Util4Exceptions.createMissingParameterValueException(parameterName);
        } else if (resultTemplate != null
            && !Configurator.getInstance().getCache().getResultTemplates()                        .contains(resultTemplate)) {
            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("The requested template identifier (");
            exceptionText.append(resultTemplate);
            exceptionText.append(") is not supported by this server!");
            throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText.toString());
        }
    }

}
