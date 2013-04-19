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
package org.n52.sos.ds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.n52.sos.binding.Binding;
import org.n52.sos.decode.Decoder;
import org.n52.sos.encode.Encoder;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.VersionNegotiationFailedException;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.filter.FilterCapabilities;
import org.n52.sos.ogc.filter.FilterConstants.ComparisonOperator;
import org.n52.sos.ogc.filter.FilterConstants.SpatialOperator;
import org.n52.sos.ogc.filter.FilterConstants.TimeOperator;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.ows.CapabilitiesExtension;
import org.n52.sos.ogc.ows.MergableExtension;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OWSOperationsMetadata;
import org.n52.sos.ogc.ows.OWSParameterValuePossibleValues;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosCapabilities;
import org.n52.sos.ogc.ows.SosServiceIdentification;
import org.n52.sos.ogc.ows.SwesExtension;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.ogc.sos.SosObservationOffering;
import org.n52.sos.request.GetCapabilitiesRequest;
import org.n52.sos.request.operator.RequestOperatorKeyType;
import org.n52.sos.response.GetCapabilitiesResponse;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.MultiMaps;
import org.n52.sos.util.SetMultiMap;

/**
 * Implementation of the interface IGetCapabilitiesDAO
 * 
 */
public class GetCapabilitiesDAO extends AbstractGetCapabilitiesDAO {

    /* section flags (values are powers of 2) */
    private static final int SERVICE_IDENTIFICATION = 0x01;

    private static final int SERVICE_PROVIDER = 0x02;

    private static final int OPERATIONS_METADATA = 0x04;

    private static final int FILTER_CAPABILITIES = 0x08;

    private static final int CONTENTS = 0x10;

    private static final int ALL = 0x20 | SERVICE_IDENTIFICATION | SERVICE_PROVIDER | OPERATIONS_METADATA
            | FILTER_CAPABILITIES | CONTENTS;

    @Override
    public GetCapabilitiesResponse getCapabilities(GetCapabilitiesRequest request) throws OwsExceptionReport {
        GetCapabilitiesResponse response = new GetCapabilitiesResponse();
        response.setService(SosConstants.SOS);
        response.setVersion(getVersionParameter(request));

        Set<String> availableExtensionSections = getExtensionSections();
        Set<String> requestedExtensionSections = new HashSet<String>(availableExtensionSections.size());
        int requestedSections =
                identifyRequestedSections(request, response, availableExtensionSections, requestedExtensionSections);

        SosCapabilities sosCapabilities = new SosCapabilities();
        addSectionSpecificContent(response, requestedExtensionSections, requestedSections, sosCapabilities);
        response.setCapabilities(sosCapabilities);

        return response;
    }

    private void addSectionSpecificContent(GetCapabilitiesResponse response, Set<String> requestedExtensionSections,
            int sections, SosCapabilities sosCapabilities) throws OwsExceptionReport {
        if (isServiceIdentificationSectionRequested(sections)) {
            sosCapabilities.setServiceIdentification(getServiceIdentification(response.getVersion()));
        }
        if (isServiceProviderSectionRequested(sections)) {
            sosCapabilities.setServiceProvider(getConfigurator().getServiceProvider());
        }
        if (isOperationsMetadataSectionRequested(sections)) {
            sosCapabilities.setOperationsMetadata(getOperationsMetadataForOperations(response.getService(),
                    response.getVersion()));
        }
        if (isFilterCapabilitiesSectionRequested(sections)) {
            sosCapabilities.setFilterCapabilities(getFilterCapabilities(response.getVersion()));
        }
        if (isContentsSectionRequested(sections)) {
            if (isVersionSos2(response)) {
                sosCapabilities.setContents(getContentsForSosV2(response.getVersion()));
            } else {
                sosCapabilities.setContents(getContents(response.getVersion()));
            }
        }

        if (isVersionSos2(response)) {
            if (sections == ALL) {
                sosCapabilities.setExensions(getAndMergeExtensions());
            } else if (!requestedExtensionSections.isEmpty()) {
                sosCapabilities.setExensions(getExtensions(requestedExtensionSections));
            }
        }
    }

    private int identifyRequestedSections(GetCapabilitiesRequest request, GetCapabilitiesResponse response,
            Set<String> availableExtensionSections, Set<String> requestedExtensionSections) throws OwsExceptionReport {
        int sections = 0;
        // handle sections array and set requested sections flag
        if (!request.isSetSections()) {
            sections = ALL;
        } else {
            for (String section : request.getSections()) {
                if (section.isEmpty()) {
                    // TODO empty section does not result in an exception -
                    // report? <-- should be done by RequestOperator
                    continue;
                }
                if (section.equals(SosConstants.CapabilitiesSections.All.name())) {
                    sections = ALL;
                    break;
                } else if (section.equals(SosConstants.CapabilitiesSections.ServiceIdentification.name())) {
                    sections |= SERVICE_IDENTIFICATION;
                } else if (section.equals(SosConstants.CapabilitiesSections.ServiceProvider.name())) {
                    sections |= SERVICE_PROVIDER;
                } else if (section.equals(SosConstants.CapabilitiesSections.OperationsMetadata.name())) {
                    sections |= OPERATIONS_METADATA;
                } else if ((section.equals(Sos1Constants.CapabilitiesSections.Filter_Capabilities.name()) && response
                        .getVersion().equals(Sos1Constants.SERVICEVERSION))
                        || (section.equals(Sos2Constants.CapabilitiesSections.FilterCapabilities.name()) && isVersionSos2(response))) {
                    sections |= FILTER_CAPABILITIES;
                } else if (section.equals(SosConstants.CapabilitiesSections.Contents.name())) {
                    sections |= CONTENTS;
                } else if (availableExtensionSections.contains(section) && isVersionSos2(response)) {
                    requestedExtensionSections.add(section);
                } else {
                    throw new InvalidParameterValueException().at(SosConstants.GetCapabilitiesParams.Section)
                            .withMessage("The requested section '%s' does not exist or is not supported!", section);
                }
            }
        }
        return sections;
    }

    private String getVersionParameter(GetCapabilitiesRequest request) throws OwsExceptionReport {
        if (!request.isSetVersion()) {
            if (request.isSetAcceptVersions()) {
                for (String acceptedVersion : request.getAcceptVersions()) {
                    if (getConfigurator().getServiceOperatorRepository().isVersionSupported(request.getService(), acceptedVersion)) {
                        return acceptedVersion;
                    }
                }
            } else {
                for (String supportedVersion : getConfigurator().getServiceOperatorRepository().getSupportedVersions(request.getService())) {
                    return supportedVersion;
                }
            }
        } else {
            return request.getVersion();
        }

        throw new VersionNegotiationFailedException().withMessage(
                "The requested '%s' values are not supported by this service!",
                SosConstants.GetCapabilitiesParams.AcceptVersions);
    }

    private SosServiceIdentification getServiceIdentification(String version) throws OwsExceptionReport {
        SosServiceIdentification serviceIdentification = getConfigurator().getServiceIdentification();
        if (version.equals(Sos2Constants.SERVICEVERSION)) {
            serviceIdentification.setProfiles(getProfiles());
        }
        return serviceIdentification;
    }

    private Set<String> getProfiles() {
        List<String> profiles = new LinkedList<String>();
        for (Binding bindig : getConfigurator().getBindingRepository().getBindings().values()) {
            profiles.addAll(bindig.getConformanceClasses());
        }
        for (RequestOperatorKeyType k : getConfigurator().getRequestOperatorRepository()
                .getActiveRequestOperatorKeyTypes()) {
            profiles.addAll(getConfigurator().getRequestOperatorRepository().getRequestOperator(k)
                    .getConformanceClasses());
        }
        for (Decoder<?, ?> decoder : getConfigurator().getCodingRepository().getDecoders()) {
            profiles.addAll(decoder.getConformanceClasses());
        }
        for (Encoder<?, ?> encoder : getConfigurator().getCodingRepository().getEncoders()) {
            profiles.addAll(encoder.getConformanceClasses());
        }
        return CollectionHelper.asSet(profiles);
    }

    /**
     * Get the OperationsMetadat for all supported operations
     * 
     * @param sosCapabilities
     *            SOS internal capabilities SOS internal capabilities
     * @param session
     *            Hibernate session Hibernate session
     * @return List of OperationsMetadata
     * 
     * 
     * @throws OwsExceptionReport
     *             * If an error occurs
     */
    private OWSOperationsMetadata getOperationsMetadataForOperations(String service, String version)
            throws OwsExceptionReport {

        OWSOperationsMetadata operationsMetadata = new OWSOperationsMetadata();
        operationsMetadata.addCommonValue(OWSConstants.RequestParams.service.name(),
                new OWSParameterValuePossibleValues(SosConstants.SOS));
        operationsMetadata.addCommonValue(OWSConstants.RequestParams.version.name(),
                new OWSParameterValuePossibleValues(getConfigurator().getServiceOperatorRepository()
                        .getSupportedVersions(service)));

        // FIXME: OpsMetadata for InsertSensor, InsertObservation SOS 2.0
        Set<RequestOperatorKeyType> requestOperatorKeyTypes =
                getConfigurator().getRequestOperatorRepository().getActiveRequestOperatorKeyTypes();
        List<OWSOperation> opsMetadata = new ArrayList<OWSOperation>(requestOperatorKeyTypes.size());
        for (RequestOperatorKeyType requestOperatorKeyType : requestOperatorKeyTypes) {
            if (requestOperatorKeyType.getServiceOperatorKeyType().getVersion().equals(version)) {
                OWSOperation operationMetadata =
                        getConfigurator().getRequestOperatorRepository().getRequestOperator(requestOperatorKeyType)
                                .getOperationMetadata(service, version);
                if (operationMetadata != null) {
                    opsMetadata.add(operationMetadata);
                }
            }
        }
        operationsMetadata.setOperations(opsMetadata);

        return operationsMetadata;
    }

    /**
     * Get the FilterCapabilities
     * 
     * @param sosCapabilities
     *            SOS internal capabilities
     * @return FilterCapabilities
     */
    private FilterCapabilities getFilterCapabilities(String version) {
        FilterCapabilities filterCapabilities = new FilterCapabilities();

        // !!! Modify methods addicted to your implementation !!!
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            getScalarFilterCapabilities(filterCapabilities);
        }
        getSpatialFilterCapabilities(filterCapabilities, version);
        getTemporalFilterCapabilities(filterCapabilities, version);

        return filterCapabilities;
    }

    /**
     * Get the contents for SOS 1.0.0 capabilities
     * 
     * @param sosCapabilities
     *            SOS internal capabilities
     * @return Offerings for contents
     * 
     * 
     * @throws OwsExceptionReport
     *             * If an error occurs
     */
    private List<SosObservationOffering> getContents(String version) throws OwsExceptionReport {
        Collection<String> offerings = getCache().getOfferings();
        List<SosObservationOffering> sosOfferings = new ArrayList<SosObservationOffering>(offerings.size());
        for (String offering : offerings) {
            Collection<String> procedures = getProceduresForOffering(offering, version);
            SosEnvelope envelopeForOffering = getCache().getEnvelopeForOffering(offering);
            Set<String> featuresForoffering = getFOI4offering(offering);
            Collection<String> responseFormats =
                    getConfigurator().getCodingRepository().getSupportedResponseFormats(SosConstants.SOS,
                            Sos1Constants.SERVICEVERSION);
            if (checkOfferingValues(envelopeForOffering, featuresForoffering, responseFormats)) {
                SosObservationOffering sosOffering = new SosObservationOffering();
                sosOffering.setOffering(offering);

                // insert observationTypes
                sosOffering.setObservationTypes(getObservationTypes(offering));

                // only if fois are contained for the offering set the values of
                // the
                // envelope
                sosOffering.setObservedArea(getCache().getEnvelopeForOffering(offering));
                // SosEnvelope sosEnvelope = getBBOX4Offering(offering,
                // session);
                // sosOffering.setBoundeBy(sosEnvelope.getEnvelope());
                // sosOffering.setSrid(sosEnvelope.getSrid());

                // TODO: add intended application
                // xb_oo.addIntendedApplication("");

                // add offering name
                sosOffering.setOfferingName(getCache().getNameForOffering(offering));

                // set up phenomena
                sosOffering.setObservableProperties(getCache().getObservablePropertiesForOffering(offering));
                sosOffering.setCompositePhenomena(getCache().getCompositePhenomenonsForOffering(offering));
                Map<String, Collection<String>> phens4CompPhens = new HashMap<String, Collection<String>>();
                if (getCache().getCompositePhenomenonsForOffering(offering) != null) {
                    for (String compositePhenomenon : getCache().getCompositePhenomenonsForOffering(offering)) {
                        phens4CompPhens.put(compositePhenomenon, getCache()
                                .getObservablePropertiesForCompositePhenomenon(compositePhenomenon));
                    }
                }
                sosOffering.setPhens4CompPhens(phens4CompPhens);

                // set up time
                setUpTimeForOffering(offering, sosOffering);

                // add feature of interests
                if (getConfigurator().getProfileHandler().getActiveProfile().isListFeatureOfInterestsInOfferings()) {
                    sosOffering.setFeatureOfInterest(getFOI4offering(offering));
                }

                // set procedures
                sosOffering.setProcedures(procedures);

                // insert result models
                Collection<QName> resultModels =
                        getQNamesForResultModel(getCache().getObservationTypesForOffering(offering));
                sosOffering.setResultModels(resultModels);

                // set response format
                responseFormats.add(SosConstants.CONTENT_TYPE_ZIP);
                sosOffering.setResponseFormats(responseFormats);

                // set response Mode
                sosOffering.setResponseModes(SosConstants.RESPONSE_MODES);

                sosOfferings.add(sosOffering);
            }
        }

        return sosOfferings;
    }

    private boolean checkOfferingValues(SosEnvelope envelopeForOffering, Set<String> featuresForOffering,
            Collection<String> responseFormats) {
        return envelopeForOffering != null && envelopeForOffering.isSetEnvelope() && featuresForOffering != null
                && !featuresForOffering.isEmpty() && responseFormats != null && !responseFormats.isEmpty();
    }

    /**
     * Get the contents for SOS 2.0 capabilities
     * 
     * @param sosCapabilities
     *            SOS internal capabilities
     * @param session
     *            Hibernate session
     * @return Offerings for contents
     * 
     * 
     * @throws OwsExceptionReport
     *             * If an error occurs
     */
    private List<SosObservationOffering> getContentsForSosV2(String version) throws OwsExceptionReport {
        Collection<String> offerings = getCache().getOfferings();
        List<SosObservationOffering> sosOfferings = new ArrayList<SosObservationOffering>(offerings.size());

        for (String offering : offerings) {
            Collection<String> procedures = getProceduresForOffering(offering, version);
            Collection<String> observationTypes = getObservationTypes(offering);
            if (observationTypes != null && !observationTypes.isEmpty()) {
                for (String procedure : procedures) {

                    SosObservationOffering sosOffering = new SosObservationOffering();
                    sosOffering.setOffering(offering);

                    // insert observationTypes
                    sosOffering.setObservationTypes(observationTypes);

                    sosOffering.setObservedArea(getCache().getEnvelopeForOffering(offering));

                    sosOffering.setProcedures(Collections.singletonList(procedure));

                    // TODO: add intended application

                    // add offering name
                    sosOffering.setOfferingName(getCache().getNameForOffering(offering));

                    setUpPhenomenaForOffering(offering, procedure, sosOffering);
                    setUpTimeForOffering(offering, sosOffering);
                    setUpRelatedFeaturesForOffering(offering, version, procedure, sosOffering);
                    setUpFeatureOfInterestTypesForOffering(offering, sosOffering);
                    setUpProcedureDescriptionFormatForOffering(sosOffering);
                    setUpResponseFormatForOffering(version, sosOffering);

                    sosOfferings.add(sosOffering);
                }
            }
        }

        return sosOfferings;
    }

    /**
     * Set SpatialFilterCapabilities to FilterCapabilities
     * 
     * @param filterCapabilities
     *            FilterCapabilities
     * @param version
     *            SOS version
     */
    private void getSpatialFilterCapabilities(FilterCapabilities filterCapabilities, String version) {

        // set GeometryOperands
        List<QName> operands = new LinkedList<QName>();
        if (version.equals(Sos2Constants.SERVICEVERSION)) {
            operands.add(GMLConstants.QN_ENVELOPE_32);
        } else if (version.equals(Sos1Constants.SERVICEVERSION)) {
            operands.add(GMLConstants.QN_ENVELOPE);
            operands.add(GMLConstants.QN_POINT);
            operands.add(GMLConstants.QN_LINESTRING);
            operands.add(GMLConstants.QN_POLYGON);
        }

        filterCapabilities.setSpatialOperands(operands);

        // set SpatialOperators
        SetMultiMap<SpatialOperator, QName> ops = MultiMaps.newSetMultiMap(SpatialOperator.class);
        if (version.equals(Sos2Constants.SERVICEVERSION)) {
            ops.add(SpatialOperator.BBOX, GMLConstants.QN_ENVELOPE_32);
        } else if (version.equals(Sos1Constants.SERVICEVERSION)) {
            ops.add(SpatialOperator.BBOX, GMLConstants.QN_ENVELOPE);
            // set Contains
            ops.add(SpatialOperator.Contains, GMLConstants.QN_POINT);
            ops.add(SpatialOperator.Contains, GMLConstants.QN_LINESTRING);
            ops.add(SpatialOperator.Contains, GMLConstants.QN_POLYGON);
            // set Intersects
            ops.add(SpatialOperator.Intersects, GMLConstants.QN_POINT);
            ops.add(SpatialOperator.Intersects, GMLConstants.QN_LINESTRING);
            ops.add(SpatialOperator.Intersects, GMLConstants.QN_POLYGON);
            // set Overlaps
            ops.add(SpatialOperator.Overlaps, GMLConstants.QN_POINT);
            ops.add(SpatialOperator.Overlaps, GMLConstants.QN_LINESTRING);
            ops.add(SpatialOperator.Overlaps, GMLConstants.QN_POLYGON);
        }

        filterCapabilities.setSpatialOperators(ops);
    }

    /**
     * Set TemporalFilterCapabilities to FilterCapabilities
     * 
     * @param filterCapabilities
     *            FilterCapabilities
     * @param version
     *            SOS version
     */
    private void getTemporalFilterCapabilities(FilterCapabilities filterCapabilities, String version) {

        // set TemporalOperands
        List<QName> operands = new ArrayList<QName>(2);
        if (version.equals(Sos2Constants.SERVICEVERSION)) {
            operands.add(GMLConstants.QN_TIME_PERIOD_32);
            operands.add(GMLConstants.QN_TIME_INSTANT_32);
        } else if (version.equals(Sos1Constants.SERVICEVERSION)) {
            operands.add(GMLConstants.QN_TIME_PERIOD);
            operands.add(GMLConstants.QN_TIME_INSTANT);
        }

        filterCapabilities.setTemporalOperands(operands);

        // set TemporalOperators
        SetMultiMap<TimeOperator, QName> ops = MultiMaps.newSetMultiMap(TimeOperator.class);
        if (version.equals(Sos2Constants.SERVICEVERSION)) {
            for (TimeOperator op : TimeOperator.values()) {
                ops.add(op, GMLConstants.QN_TIME_INSTANT_32);
                ops.add(op, GMLConstants.QN_TIME_PERIOD_32);
            }
        } else if (version.equals(Sos1Constants.SERVICEVERSION)) {
            for (TimeOperator op : TimeOperator.values()) {
                ops.add(op, GMLConstants.QN_TIME_INSTANT);
                ops.add(op, GMLConstants.QN_TIME_PERIOD);
            }
        }
        filterCapabilities.setTempporalOperators(ops);
    }

    /**
     * Set ScalarFilterCapabilities to FilterCapabilities
     * 
     * @param filterCapabilities
     *            FilterCapabilities
     */
    private void getScalarFilterCapabilities(FilterCapabilities filterCapabilities) {
        // TODO PropertyIsNil, PropertyIsNull? better:
        // filterCapabilities.setComparisonOperators(Arrays.asList(ComparisonOperator.values()));
        List<ComparisonOperator> comparisonOperators = new ArrayList<ComparisonOperator>(8);
        comparisonOperators.add(ComparisonOperator.PropertyIsBetween);
        comparisonOperators.add(ComparisonOperator.PropertyIsEqualTo);
        comparisonOperators.add(ComparisonOperator.PropertyIsNotEqualTo);
        comparisonOperators.add(ComparisonOperator.PropertyIsLessThan);
        comparisonOperators.add(ComparisonOperator.PropertyIsLessThanOrEqualTo);
        comparisonOperators.add(ComparisonOperator.PropertyIsGreaterThan);
        comparisonOperators.add(ComparisonOperator.PropertyIsGreaterThanOrEqualTo);
        comparisonOperators.add(ComparisonOperator.PropertyIsLike);
        filterCapabilities.setComparisonOperators(comparisonOperators);
    }

    /**
     * Get FOIs contained in an offering
     * 
     * @param offering
     *            Offering identifier
     * @return FOI identifiers
     * 
     * 
     * @throws OwsExceptionReport
     *             * If an error occurs
     */
    private Set<String> getFOI4offering(String offering) throws OwsExceptionReport {
        Set<String> featureIDs = new HashSet<String>(0);
        Set<String> features = getConfigurator().getCache().getFeaturesOfInterestForOffering(offering);
        if (!getConfigurator().getProfileHandler().getActiveProfile().isListFeatureOfInterestsInOfferings()
                || features == null) {
            featureIDs.add(OGCConstants.UNKNOWN);
        } else {
            featureIDs.addAll(features);
        }
        return featureIDs;
    }

    /**
     * Get the QName for resultModels from observationType constant
     * 
     * @param resultModels4Offering
     *            Observation types
     * @return QNames for resultModel parameter
     */
    private Collection<QName> getQNamesForResultModel(Collection<String> resultModels4Offering) {
        List<QName> resultModels = new ArrayList<QName>(9);
        for (String string : resultModels4Offering) {
            if (string.equals(OMConstants.OBS_TYPE_MEASUREMENT)) {
                resultModels.add(OMConstants.RESULT_MODEL_MEASUREMENT);
            } else if (string.equals(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION)) {
                resultModels.add(OMConstants.RESULT_MODEL_CATEGORY_OBSERVATION);
            } else if (string.equals(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION)) {
                resultModels.add(OMConstants.RESULT_MODEL_GEOMETRY_OBSERVATION);
            } else if (string.equals(OMConstants.OBS_TYPE_OBSERVATION)) {
                resultModels.add(OMConstants.RESULT_MODEL_OBSERVATION);
            } else if (string.equals(OMConstants.OBS_TYPE_COUNT_OBSERVATION)) {
                resultModels.add(OMConstants.RESULT_MODEL_COUNT_OBSERVATION);
            } else if (string.equals(OMConstants.OBS_TYPE_TRUTH_OBSERVATION)) {
                resultModels.add(OMConstants.RESULT_MODEL_TRUTH_OBSERVATION);
            } else if (string.equals(OMConstants.OBS_TYPE_TEXT_OBSERVATION)) {
                resultModels.add(OMConstants.RESULT_MODEL_TEXT_OBSERVATION);
            } else if (string.equals(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION)) {
                resultModels.add(OMConstants.RESULT_MODEL_OBSERVATION);
            } else {
                resultModels.add(OMConstants.RESULT_MODEL_OBSERVATION);
            }
        }
        return resultModels;
    }

    private Collection<String> getObservationTypes(String offering) {
        Collection<String> allObservationTypes = getCache().getObservationTypesForOffering(offering);
        List<String> observationTypes = new ArrayList<String>(allObservationTypes.size());

        for (String observationType : allObservationTypes) {
            if (!observationType.equals(SosConstants.NOT_DEFINED)) {
                observationTypes.add(observationType);
            }
        }
        if (observationTypes.isEmpty()) {
            for (String observationType : getCache().getAllowedObservationTypesForOffering(offering)) {
                if (!observationType.equals(SosConstants.NOT_DEFINED)) {
                    observationTypes.add(observationType);
                }
            }
        }
        return observationTypes;
    }

    @Override
    protected Set<String> getExtensionSections() throws OwsExceptionReport {
        Collection<SwesExtension> extensions = getAndMergeExtensions();
        HashSet<String> sections = new HashSet<String>(extensions.size());
        for (SwesExtension e : extensions) {
            if (e instanceof CapabilitiesExtension) {
                sections.add(((CapabilitiesExtension) e).getSectionName());
            }
        }
        return sections;
    }

    /**
     * Get extensions and merge MergableExtension of the same class.
     * 
     * @return Extensions
     * 
     * 
     * @throws OwsExceptionReport
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private List<SwesExtension> getAndMergeExtensions() throws OwsExceptionReport {
        Set<RequestOperatorKeyType> requestOperators =
                getConfigurator().getRequestOperatorRepository().getActiveRequestOperatorKeyTypes();
        List<SwesExtension> extensions = new ArrayList<SwesExtension>(requestOperators.size());
        HashMap<String, MergableExtension> map = new HashMap<String, MergableExtension>(requestOperators.size());
        for (RequestOperatorKeyType k : requestOperators) {
            SwesExtension extension =
                    getConfigurator().getRequestOperatorRepository().getRequestOperator(k).getExtension();
            if (extension != null) {
                if (extension instanceof MergableExtension) {
                    MergableExtension me = (MergableExtension) extension;
                    MergableExtension previous = map.get(me.getSectionName());
                    if (previous == null) {
                        map.put(me.getSectionName(), me);
                    } else {
                        previous.merge(me);
                    }
                } else {
                    extensions.add(extension);
                }
            }
        }
        extensions.addAll(map.values());
        return extensions;
    }

    private Collection<SwesExtension> getExtensions(Set<String> requestedExtensionSections) throws OwsExceptionReport {
        List<SwesExtension> extensions = getAndMergeExtensions();
        List<SwesExtension> filtered = new ArrayList<SwesExtension>(requestedExtensionSections.size());
        for (SwesExtension e : extensions) {
            if (e instanceof CapabilitiesExtension
                    && requestedExtensionSections.contains(((CapabilitiesExtension) e).getSectionName())) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    protected void setUpPhenomenaForOffering(String offering, String procedure, SosObservationOffering sosOffering) {
        Collection<String> phenomenons = new LinkedList<String>();
        Collection<String> observablePropertiesForOffering = getCache().getObservablePropertiesForOffering(offering);
        for (String observableProperty : observablePropertiesForOffering) {
            if (getCache().getProceduresForObservableProperty(observableProperty).contains(procedure)) {
                phenomenons.add(observableProperty);
            }
        }
        sosOffering.setObservableProperties(phenomenons);
        sosOffering.setCompositePhenomena(getCache().getCompositePhenomenonsForOffering(offering));

        Collection<String> compositePhenomenonsForOffering = getCache().getCompositePhenomenonsForOffering(offering);

        if (compositePhenomenonsForOffering != null) {
            Map<String, Collection<String>> phens4CompPhens =
                    new HashMap<String, Collection<String>>(compositePhenomenonsForOffering.size());
            for (String compositePhenomenon : compositePhenomenonsForOffering) {
                Collection<String> phenomenonsForComposite =
                        getCache().getObservablePropertiesForCompositePhenomenon(compositePhenomenon);
                phens4CompPhens.put(compositePhenomenon, phenomenonsForComposite);
            }
            sosOffering.setPhens4CompPhens(phens4CompPhens);
        } else {
            sosOffering.setPhens4CompPhens(Collections.<String, Collection<String>> emptyMap());
        }

    }

    protected void setUpRelatedFeaturesForOffering(String offering, String version, String procedure,
            SosObservationOffering sosOffering) throws OwsExceptionReport {
        Map<String, Set<String>> relatedFeatures = new HashMap<String, Set<String>>();
        Set<String> relatedFeaturesForThisOffering = getCache().getRelatedFeaturesForOffering(offering);
        if (relatedFeaturesForThisOffering != null && !relatedFeaturesForThisOffering.isEmpty()) {
            for (String relatedFeature : relatedFeaturesForThisOffering) {
                relatedFeatures.put(relatedFeature, getCache().getRolesForRelatedFeature(relatedFeature));
            }
        } else {
            Set<String> role = Collections.singleton("featureOfInterestID");
            Set<String> featuresForOffering = getCache().getFeaturesOfInterestForOffering(offering);
            if (featuresForOffering != null) {
                for (String foiID : featuresForOffering) {
                    if (getCache().getProceduresForFeatureOfInterest(foiID).contains(procedure)) {
                        relatedFeatures.put(foiID, role);
                    }
                }
            }
        }
        sosOffering.setRelatedFeatures(relatedFeatures);
    }

    protected void setUpTimeForOffering(String offering, SosObservationOffering sosOffering) {
        sosOffering.setPhenomenonTime(new TimePeriod(getCache().getMinPhenomenonTimeForOffering(offering), getCache()
                .getMaxPhenomenonTimeForOffering(offering)));
        sosOffering.setResultTime(new TimePeriod(getCache().getMinResultTimeForOffering(offering), getCache()
                .getMaxResultTimeForOffering(offering)));
    }

    // if no foi contained, set allowed foitypes
    protected void setUpFeatureOfInterestTypesForOffering(String offering, SosObservationOffering sosOffering) {
        Set<String> features = getCache().getFeaturesOfInterestForOffering(offering);
        if (features == null || features.isEmpty()) {
            sosOffering.setFeatureOfInterestTypes(getCache().getFeatureOfInterestTypes());
        } else {
            // TODO reduce list of feature types to the really available in this
            // offering -> requires additional map in cache
            sosOffering.setFeatureOfInterestTypes(getCache().getFeatureOfInterestTypes());
            sosOffering.setFeatureOfInterest(features); // TODO seems to be
                                                        // useless somehow
        }
    }

    protected void setUpResponseFormatForOffering(String version, SosObservationOffering sosOffering) {
        Collection<String> responseFormats =
                getConfigurator().getCodingRepository().getSupportedResponseFormats(SosConstants.SOS, version);
        sosOffering.setResponseFormats(responseFormats);
        // TODO set as property
        responseFormats.add(SosConstants.CONTENT_TYPE_ZIP);
    }

    protected void setUpProcedureDescriptionFormatForOffering(SosObservationOffering sosOffering) {
        // TODO: set procDescFormat <-- what is required here?
        sosOffering.setProcedureDescriptionFormat(getCache().getProcedureDescriptionFormats());
    }

    private Collection<String> getProceduresForOffering(String offering, String version) throws OwsExceptionReport {
        Collection<String> procedures = CollectionHelper.asSet(getCache().getProceduresForOffering(offering));
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            procedures.addAll(getCache().getHiddenChildProceduresForOffering(offering));
        }
        if (procedures.isEmpty()) {
            throw new NoApplicableCodeException()
                    .withMessage(
                            "No procedures are contained in the database for the offering '%s'! Please contact the admin of this SOS.",
                            offering);
        }
        return procedures;
    }

    private boolean isVersionSos2(GetCapabilitiesResponse response) {
        return response.getVersion().equals(Sos2Constants.SERVICEVERSION);
    }

    private boolean isContentsSectionRequested(int sections) {
        return (sections & CONTENTS) != 0;
    }

    private boolean isFilterCapabilitiesSectionRequested(int sections) {
        return (sections & FILTER_CAPABILITIES) != 0;
    }

    private boolean isOperationsMetadataSectionRequested(int sections) {
        return (sections & OPERATIONS_METADATA) != 0;
    }

    private boolean isServiceProviderSectionRequested(int sections) {
        return (sections & SERVICE_PROVIDER) != 0;
    }

    private boolean isServiceIdentificationSectionRequested(int sections) {
        return (sections & SERVICE_IDENTIFICATION) != 0;
    }
}
