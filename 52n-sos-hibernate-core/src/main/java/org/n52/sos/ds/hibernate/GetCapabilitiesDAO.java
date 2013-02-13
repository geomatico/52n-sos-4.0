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
package org.n52.sos.ds.hibernate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.n52.sos.binding.Binding;
import org.n52.sos.decode.IDecoder;
import org.n52.sos.ds.IGetCapabilitiesDAO;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.filter.FilterCapabilities;
import org.n52.sos.ogc.filter.FilterConstants.ComparisonOperator;
import org.n52.sos.ogc.filter.FilterConstants.SpatialOperator;
import org.n52.sos.ogc.filter.FilterConstants.TimeOperator;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.ICapabilitiesExtension;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.IMergableExtension;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OWSOperationsMetadata;
import org.n52.sos.ogc.ows.OWSParameterValuePossibleValues;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosCapabilities;
import org.n52.sos.ogc.ows.SosServiceIdentification;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.ogc.sos.SosOfferingsForContents;
import org.n52.sos.request.GetCapabilitiesRequest;
import org.n52.sos.request.operator.IRequestOperator;
import org.n52.sos.request.operator.RequestOperatorKeyType;
import org.n52.sos.response.GetCapabilitiesResponse;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the interface IGetCapabilitiesDAO
 *
 */
public class GetCapabilitiesDAO extends AbstractHibernateOperationDao implements IGetCapabilitiesDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GetCapabilitiesDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = SosConstants.Operations.GetCapabilities.name();

    /* section flags (values are powers of 2) */
    private static final int SERVICE_IDENTIFICATION = 0x01;

    private static final int SERVICE_PROVIDER = 0x02;

    private static final int OPERATIONS_METADATA = 0x04;

    private static final int FILTER_CAPABILITIES = 0x08;

    private static final int CONTENTS = 0x10;

    private static final int ALL = 0x20 | SERVICE_IDENTIFICATION | SERVICE_PROVIDER | OPERATIONS_METADATA
            | FILTER_CAPABILITIES | CONTENTS;

    /*
     * (non-Javadoc)
     *
     * @see org.n52.sos.ds.ISosOperationDAO#getOperationName()
     */
    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.n52.sos.ds.hibernate.AbstractHibernateOperationDao#getOperationsMetadata
     * (java.lang.String, org.hibernate.Session)
     */
    @Override
    protected void setOperationsMetadata(OWSOperation opsMeta, String service, String version, Session session)
            throws OwsExceptionReport {
        // set param Sections
        List<String> sectionsValues = new LinkedList<String>();
        /* common sections */
        sectionsValues.add(SosConstants.CapabilitiesSections.ServiceIdentification.name());
        sectionsValues.add(SosConstants.CapabilitiesSections.ServiceProvider.name());
        sectionsValues.add(SosConstants.CapabilitiesSections.OperationsMetadata.name());
        sectionsValues.add(SosConstants.CapabilitiesSections.Contents.name());
        sectionsValues.add(SosConstants.CapabilitiesSections.All.name());

        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            sectionsValues.add(Sos1Constants.CapabilitiesSections.Filter_Capabilities.name());
        } else if (version.equals(Sos2Constants.SERVICEVERSION)) {
            sectionsValues.add(Sos2Constants.CapabilitiesSections.FilterCapabilities.name());
            /* sections of extension points */
            for (String section : getExtensionSections(session)) {
                sectionsValues.add(section);
            }
        }

        opsMeta.addPossibleValuesParameter(SosConstants.GetCapabilitiesParams.Sections, sectionsValues);
        opsMeta.addPossibleValuesParameter(SosConstants.GetCapabilitiesParams.AcceptFormats,
                Arrays.asList(SosConstants.getAcceptFormats()));
        opsMeta.addPossibleValuesParameter(SosConstants.GetCapabilitiesParams.AcceptVersions,
				getConfigurator().getServiceOperatorRepository().getSupportedVersions());
        opsMeta.addAnyParameterValue(SosConstants.GetCapabilitiesParams.updateSequence);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.n52.sos.ds.IGetCapabilitiesDAO#getCapabilities(org.n52.sos.request
     * .AbstractSosRequest)
     */
    @Override
    public GetCapabilitiesResponse getCapabilities(GetCapabilitiesRequest request) throws OwsExceptionReport {
        Session session = null;
        try {
            session = getSession();
            GetCapabilitiesResponse response = new GetCapabilitiesResponse();
            response.setService(SosConstants.SOS);
            if (request.getVersion() == null) {
                if (request.getAcceptVersions() != null) {
                    String[] acceptedVersion = request.getAcceptVersions();
                    for (int i = 0; i < acceptedVersion.length; i++) {
                        if (getConfigurator().getServiceOperatorRepository()
								.isVersionSupported(acceptedVersion[i])) {
                            response.setVersion(acceptedVersion[i]);
                            break;
                        }
                    }
                } else {
                    for (String supportedVersion : getConfigurator().getServiceOperatorRepository()
							.getSupportedVersions()) {
                        response.setVersion(supportedVersion);
                        break;
                    }
                }
            } else {
                response.setVersion(request.getVersion());
            }
            if (response.getVersion() == null) {
                String exceptionText =
                        String.format("The requested '%s' values are not supported by this service!",
                                SosConstants.GetCapabilitiesParams.AcceptVersions.name());
                LOGGER.error(exceptionText);
                throw Util4Exceptions.createVersionNegotiationFailedException(exceptionText);
            }

            Set<String> availableExtensionSections = getExtensionSections(session);
            Set<String> requestedExtensionSections = new HashSet<String>(availableExtensionSections.size());
            // section flags
            int sections = 0;

            // handle sections array and set requested sections flag
            if (request.getSections() == null) {
                sections = ALL;
            } else {
                for (String section : request.getSections()) {
                    if (section.isEmpty()) {
                        // TODO empty section does not result in an exception
                        // report?
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
                            || (section.equals(Sos2Constants.CapabilitiesSections.FilterCapabilities.name()) && response
                                    .getVersion().equals(Sos2Constants.SERVICEVERSION))) {
                        sections |= FILTER_CAPABILITIES;
                    } else if (section.equals(SosConstants.CapabilitiesSections.Contents.name())) {
                        sections |= CONTENTS;
                    } else if (availableExtensionSections.contains(section)
                            && response.getVersion().equals(Sos2Constants.SERVICEVERSION)) {
                        requestedExtensionSections.add(section);
                    } else {
                        String exceptionText =
                                String.format("The requested section '%s' does not exist or is not supported!",
                                        section);
                        LOGGER.debug(exceptionText);
                        throw Util4Exceptions.createInvalidParameterValueException(
                                SosConstants.GetCapabilitiesParams.Section.name(), exceptionText);
                    }
                }
            }

            SosCapabilities sosCapabilities = new SosCapabilities();

            if ((sections & SERVICE_IDENTIFICATION) != 0) {
                sosCapabilities.setServiceIdentification(getServiceIdentification(response.getVersion()));
            }
            if ((sections & SERVICE_PROVIDER) != 0) {
                sosCapabilities.setServiceProvider(getConfigurator().getServiceProvider());
            }
            if ((sections & OPERATIONS_METADATA) != 0) {
                sosCapabilities.setOperationsMetadata(getOperationsMetadataForOperations(response.getService(),
                        response.getVersion(), session));
            }
            if ((sections & FILTER_CAPABILITIES) != 0) {
                sosCapabilities.setFilterCapabilities(getFilterCapabilities(response.getVersion()));
            }
            if ((sections & CONTENTS) != 0) {
                if (response.getVersion().equals(Sos2Constants.SERVICEVERSION)) {
                    sosCapabilities.setContents(getContentsForSosV2(response.getVersion(), session));
                } else {
                    sosCapabilities.setContents(getContents());
                }
            }

            if (response.getVersion().equals(Sos2Constants.SERVICEVERSION)) {
                if (sections == ALL) {
                    sosCapabilities.setExensions(getAndMergeExtensions(session));
                } else if (!requestedExtensionSections.isEmpty()) {
                    sosCapabilities.setExensions(getExtensions(session, requestedExtensionSections));
                }
            }
            response.setCapabilities(sosCapabilities);
            return response;
        } catch (HibernateException he) {
            String exceptionText = "Error while querying data for Capabilities document!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            returnSession(session);
        }
    }

    private SosServiceIdentification getServiceIdentification(String version) throws OwsExceptionReport {
        SosServiceIdentification serviceIdentification = getConfigurator().getServiceIdentification();
        if (version.equals(Sos2Constants.SERVICEVERSION)) {
            serviceIdentification.setProfiles(getProfiles());
        }
        return serviceIdentification;
    }

    private List<String> getProfiles() {
        Set<String> profiles = new HashSet<String>();
        for (Binding bindig : getConfigurator().getBindingRepository().getBindings().values()) {
            profiles.addAll(bindig.getConformanceClasses());
        }
        for (IRequestOperator requestOperator : getConfigurator().getRequestOperatorRepository().getRequestOperator().values()) {
            profiles.addAll(requestOperator.getConformanceClasses());
        }
        for (IDecoder<?,?> decoder : getConfigurator().getCodingRepository().getDecoders()) {
            profiles.addAll(decoder.getConformanceClasses());
        }
        for (IEncoder<?,?> encoder : getConfigurator().getCodingRepository().getEncoders()) {
            profiles.addAll(encoder.getConformanceClasses());
        }
        return new ArrayList<String>(profiles);
    }

    /**
     * Get the OperationsMetadat for all supported operations
     *
     * @param sosCapabilities
     *            SOS internal capabilities SOS internal capabilities
     * @param session
     *            Hibernate session Hibernate session
     * @return List of OperationsMetadata
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    private OWSOperationsMetadata getOperationsMetadataForOperations(String service, String version, Session session)
            throws OwsExceptionReport {

        OWSOperationsMetadata operationsMetadata = new OWSOperationsMetadata();
        operationsMetadata.addCommonValue(OWSConstants.RequestParams.service.name(),
                new OWSParameterValuePossibleValues(SosConstants.SOS));
        operationsMetadata.addCommonValue(OWSConstants.RequestParams.version.name(),
                new OWSParameterValuePossibleValues(getConfigurator().getServiceOperatorRepository().getSupportedVersions()));

        // FIXME: OpsMetadata for InsertSensor, InsertObservation SOS 2.0
        Map<RequestOperatorKeyType, IRequestOperator> requestOperators = getConfigurator().getRequestOperatorRepository().getRequestOperator();
        List<OWSOperation> opsMetadata = new ArrayList<OWSOperation>(requestOperators.size());
        for (RequestOperatorKeyType requestOperatorKeyType : requestOperators.keySet()) {
            if (requestOperatorKeyType.getServiceOperatorKeyType().getVersion().equals(version)) {
                OWSOperation operationMetadata =
                        requestOperators.get(requestOperatorKeyType).getOperationMetadata(service, version, session);  // FIXME session is not required because it is not used here!
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
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    private List<SosOfferingsForContents> getContents() throws OwsExceptionReport {
        Collection<String> offerings = getCache().getOfferings();
        List<SosOfferingsForContents> sosOfferings = new ArrayList<SosOfferingsForContents>(offerings.size());
        for (String offering : offerings) {

            SosEnvelope envelopeForOffering = getCache().getEnvelopeForOffering(offering);
            List<String> featuresForoffering = getFOI4offering(offering);
            Collection<String> responseFormats = SosHelper.getSupportedResponseFormats(SosConstants.SOS, "1.0.0");
            if (checkOfferingValues(envelopeForOffering, featuresForoffering, responseFormats)) {
                SosOfferingsForContents sosOffering = new SosOfferingsForContents();
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
                sosOffering.setOfferingName(getCache().getOfferingName(offering));

                // set up phenomena
                sosOffering.setObservableProperties(getCache().getObservablePropertiesForOffering(offering));
                sosOffering.setCompositePhenomena(getCache().getKOfferingVCompositePhenomenons().get(offering));
                Map<String, Collection<String>> phens4CompPhens = new HashMap<String, Collection<String>>();
                if (getCache().getKOfferingVCompositePhenomenons().get(offering) != null) {
                    for (String compositePhenomenon : getCache().getKOfferingVCompositePhenomenons().get(offering)) {
                        phens4CompPhens.put(compositePhenomenon, getCache().getKCompositePhenomenonVObservableProperty().get(compositePhenomenon));
                    }
                }
                sosOffering.setPhens4CompPhens(phens4CompPhens);

                // set up time
                DateTime minDate = getCache().getMinTimeForOffering(offering);
                DateTime maxDate = getCache().getMaxTimeForOffering(offering);
                sosOffering.setTime(new TimePeriod(minDate, maxDate));

                // add feature of interests
                if (getConfigurator().getActiveProfile().isListFeatureOfInterestsInOfferings()) {
                    sosOffering.setFeatureOfInterest(getFOI4offering(offering));
                }

                // set procedures
                Collection<String> procedures = getCache().getProcedures4Offering(offering);
                if (procedures == null || procedures.isEmpty()) {
                    String exceptionText = String.format(
                            "No procedures are contained in the database for the offering: %s! Please contact the admin of this SOS.", offering);
                    LOGGER.error(exceptionText);
                    throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                }
                sosOffering.setProcedures(procedures);

                // insert result models
                Collection<QName> resultModels = getQNamesForResultModel(getCache().getResultModels4Offering(offering));
                sosOffering.setResultModels(resultModels);

                // set response format
                responseFormats.add(SosConstants.CONTENT_TYPE_ZIP);
                sosOffering.setResponseFormats(responseFormats);

                // set response Mode
                sosOffering.setResponseModes(Arrays.asList(SosConstants.getResponseModes()));

                sosOfferings.add(sosOffering);
            }
        }

        return sosOfferings;
    }

    private boolean checkOfferingValues(SosEnvelope envelopeForOffering, List<String> featuresForoffering,
            Collection<String> responseFormats) {
        return envelopeForOffering != null && envelopeForOffering.isSetEnvelope() && featuresForoffering != null
                && !featuresForoffering.isEmpty() && responseFormats != null && !responseFormats.isEmpty();
    }

    /**
     * Get the contents for SOS 2.0 capabilities
     *
     * @param sosCapabilities
     *            SOS internal capabilities
     * @param session
     *            Hibernate session
     * @return Offerings for contents
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    private List<SosOfferingsForContents> getContentsForSosV2(String version, Session session)
            throws OwsExceptionReport {
        // TODO shouldn't this be part of the encoder?
        int phenTimeCounter = 0;
        Collection<String> offerings = getCache().getOfferings();
        List<SosOfferingsForContents> sosOfferings = new ArrayList<SosOfferingsForContents>(offerings.size());

        for (String offering : offerings) {
            Collection<String> procedures = getProceduresForOffering(offering);
            Collection<String> observationTypes = getObservationTypes(offering);
            if (observationTypes != null && !observationTypes.isEmpty()) {
                for (String procedure : procedures) {

                    SosOfferingsForContents sosOffering = new SosOfferingsForContents();
                    sosOffering.setOffering(offering);

                    // insert observationTypes
                    sosOffering.setObservationTypes(observationTypes);

                    sosOffering.setObservedArea(getCache().getEnvelopeForOffering(offering));

                    sosOffering.setProcedures(Collections.singletonList(procedure));

                    // TODO: add intended application

                    // add offering name
                    sosOffering.setOfferingName(getCache().getOfferingName(offering));

                    setUpPhenomenaForOffering(offering, procedure, sosOffering);
                    setUpTimeForOffering(offering, session, ++phenTimeCounter, sosOffering);
                    setUpFeaturesForOffering(offering, session, version, procedure, sosOffering);
                    setUpFeatureOfInterestTypesForOffering(offering, session, sosOffering);
                    setUpProcedureDescriptionFormatForOffering(sosOffering, session);
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
        List<QName> operands = new ArrayList<QName>(4);
        operands.add(GMLConstants.QN_ENVELOPE);
        // additional spatial operands for SOS 1.0
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            operands.add(GMLConstants.QN_POINT);
            operands.add(GMLConstants.QN_LINESTRING);
            operands.add(GMLConstants.QN_POLYGON);
        }

        filterCapabilities.setSpatialOperands(operands);

        // set SpatialOperators
        Map<SpatialOperator, List<QName>> spatialOperators =
                new EnumMap<SpatialOperator, List<QName>>(SpatialOperator.class);
        // set BBOX
        spatialOperators.put(SpatialOperator.BBOX, Collections.singletonList(GMLConstants.QN_ENVELOPE));

        // additional spatial operators for SOS 1.0
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            // set Contains
            List<QName> operands4Contains = new ArrayList<QName>(3);
            operands4Contains.add(GMLConstants.QN_POINT);
            operands4Contains.add(GMLConstants.QN_LINESTRING);
            operands4Contains.add(GMLConstants.QN_POLYGON);
            spatialOperators.put(SpatialOperator.Contains, operands4Contains);
            // set Intersects
            List<QName> operands4Intersects = new ArrayList<QName>(3);
            operands4Intersects.add(GMLConstants.QN_POINT);
            operands4Intersects.add(GMLConstants.QN_LINESTRING);
            operands4Intersects.add(GMLConstants.QN_POLYGON);
            spatialOperators.put(SpatialOperator.Intersects, operands4Intersects);
            // set Overlaps
            List<QName> operands4Overlaps = new ArrayList<QName>(3);
            operands4Overlaps.add(GMLConstants.QN_POINT);
            operands4Overlaps.add(GMLConstants.QN_LINESTRING);
            operands4Overlaps.add(GMLConstants.QN_POLYGON);
            spatialOperators.put(SpatialOperator.Overlaps, operands4Overlaps);
        }

        filterCapabilities.setSpatialOperators(spatialOperators);
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
        operands.add(GMLConstants.QN_TIME_PERIOD);
        operands.add(GMLConstants.QN_TIME_INSTANT);

        filterCapabilities.setTemporalOperands(operands);

        // set TemporalOperators
        Map<TimeOperator, List<QName>> temporalOperators = new EnumMap<TimeOperator, List<QName>>(TimeOperator.class);
        // set TM_During
        List<QName> operands4During = new ArrayList<QName>(1);
        operands4During.add(GMLConstants.QN_TIME_PERIOD);
        temporalOperators.put(TimeOperator.TM_During, operands4During);
        // set TM_Equals
        List<QName> operands4Equals = new ArrayList<QName>(1);
        operands4Equals.add(GMLConstants.QN_TIME_INSTANT);
        temporalOperators.put(TimeOperator.TM_Equals, operands4Equals);
        // additional temporal operators for SOS 1.0
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            // set TM_After
            List<QName> operands4After = new ArrayList<QName>(1);
            operands4After.add(GMLConstants.QN_TIME_INSTANT);
            temporalOperators.put(TimeOperator.TM_After, operands4After);
            // set TM_Before
            List<QName> operands4Before = new ArrayList<QName>(1);
            operands4Before.add(GMLConstants.QN_TIME_INSTANT);
            temporalOperators.put(TimeOperator.TM_Before, operands4Before);
        }

        filterCapabilities.setTempporalOperators(temporalOperators);
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
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    private List<String> getFOI4offering(String offering) throws OwsExceptionReport {
        List<String> featureIDs = new ArrayList<String>(0);
        Collection<String> features =
                getConfigurator().getCapabilitiesCacheController().getKOfferingVFeatures().get(offering);
        if (!getConfigurator().getActiveProfile().isListFeatureOfInterestsInOfferings() || features == null) {
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

    private String getRelatedFeatureID(String identifier, Session session, String version) throws OwsExceptionReport {
        SosSamplingFeature feature =
                (SosSamplingFeature) getConfigurator().getFeatureQueryHandler().getFeatureByID(identifier, session,
                        version);
        if (feature.getUrl() != null && !feature.getUrl().isEmpty()) {
            return feature.getUrl();
        } else {
            // String urlPattern =
            // SosHelper.getUrlPatternForHttpGetMethod(Configurator.getInstance().getBindingOperators().values(),
            // SosConstants.Operations.GetFeatureOfInterest.name(), new
            // DecoderKeyType(SosConstants.SOS,
            // version));
            // return SosHelper.createFoiGetUrl(identifier, version,
            // Configurator.getInstance().getServiceURL(),
            // urlPattern);
            return identifier;
        }
    }

    private Collection<String> getObservationTypes(String offering) {
        Collection<String> allObservationTypes = getCache().getObservationTypes4Offering(offering);
        List<String> observationTypes = new ArrayList<String>(allObservationTypes.size());

        for (String observationType : allObservationTypes) {
            if (!observationType.equals(SosConstants.NOT_DEFINED)) {
                observationTypes.add(observationType);
            }
        }
        if (observationTypes.isEmpty()) {
            for (String observationType : getCache().getAllowedObservationTypes4Offering(offering)) {
                if (!observationType.equals(SosConstants.NOT_DEFINED)) {
                    observationTypes.add(observationType);
                }
            }
        }
        return observationTypes;
    }

    private Collection<String> getFeatureOfInterestTypes(String offering, Session session) {
        Collection<String> featureIDs = getCache().getKOfferingVFeatures().get(offering);
        if (featureIDs == null || (featureIDs != null && featureIDs.isEmpty())) {
            return HibernateCriteriaQueryUtilities.getFeatureOfInterestTypesForFeatureOfInterest(featureIDs, session);
        }
        return new ArrayList<String>(0);

    }

    private Set<String> getExtensionSections(Session session) throws OwsExceptionReport {
        Collection<IExtension> extensions = getAndMergeExtensions(session);
        HashSet<String> sections = new HashSet<String>(extensions.size());
        for (IExtension e : extensions) {
            if (e instanceof ICapabilitiesExtension) {
                sections.add(((ICapabilitiesExtension) e).getSectionName());
            }
        }
        return sections;
    }

    /**
     * Get extensions and merge IMergableExtension of the same class.
     *
     * @return Extensions
     * @throws OwsExceptionReport
     */
    @SuppressWarnings("rawtypes")
    private List<IExtension> getAndMergeExtensions(Session session) throws OwsExceptionReport {
        Map<RequestOperatorKeyType, IRequestOperator> requestOperators = getConfigurator()
				.getRequestOperatorRepository().getRequestOperator();
        List<IExtension> extensions = new ArrayList<IExtension>(requestOperators.size());
        HashMap<String, IMergableExtension> map = new HashMap<String, IMergableExtension>(requestOperators.size());
        for (IRequestOperator requestOperator : requestOperators.values()) {
            IExtension extension = requestOperator.getExtension(session);
            if (extension != null) {
                if (extension instanceof IMergableExtension) {
                    IMergableExtension me = (IMergableExtension) extension;
                    IMergableExtension previous = map.get(me.getSectionName());
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

    private Collection<IExtension> getExtensions(Session session, Set<String> requestedExtensionSections)
            throws OwsExceptionReport {
        List<IExtension> extensions = getAndMergeExtensions(session);
        List<IExtension> filtered = new ArrayList<IExtension>(requestedExtensionSections.size());
        for (IExtension e : extensions) {
            if (e instanceof ICapabilitiesExtension) {
                if (requestedExtensionSections.contains(((ICapabilitiesExtension) e).getSectionName())) {
                    filtered.add(e);
                }
            }
        }
        return filtered;
    }

    protected void setUpPhenomenaForOffering(String offering, String procedure, SosOfferingsForContents sosOffering) {
        Collection<String> phenomenons = new LinkedList<String>();
        Map<String, Collection<String>> phenProcs = getCache().getKObservablePropertyVProcedures();
        Collection<String> phens4Off = getCache().getObservablePropertiesForOffering(offering);
        for (String phenID : phens4Off) {
            if (phenProcs.get(phenID).contains(procedure)) {
                phenomenons.add(phenID);
            }
        }
        sosOffering.setObservableProperties(phenomenons);
        sosOffering.setCompositePhenomena(getCache().getKOfferingVCompositePhenomenons().get(offering));

        Collection<String> compositePhenomenonsForOffering =
                getCache().getKOfferingVCompositePhenomenons().get(offering);

        if (compositePhenomenonsForOffering != null) {
            Map<String, Collection<String>> phens4CompPhens =
                    new HashMap<String, Collection<String>>(compositePhenomenonsForOffering.size());
            for (String compositePhenomenon : compositePhenomenonsForOffering) {
                Collection<String> phenomenonsForComposite =
                        getCache().getKCompositePhenomenonVObservableProperty().get(compositePhenomenon);
                phens4CompPhens.put(compositePhenomenon, phenomenonsForComposite);
            }
            sosOffering.setPhens4CompPhens(phens4CompPhens);
        } else {
            sosOffering.setPhens4CompPhens(Collections.<String, Collection<String>> emptyMap());
        }

    }

    protected void setUpFeaturesForOffering(String offering, Session session, String version, String procedure,
            SosOfferingsForContents sosOffering) throws OwsExceptionReport {
        Map<String, Collection<String>> relatedFeatures = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> relatedFeaturesForOffering = getCache().getKOfferingVRelatedFeatures();
        if (relatedFeaturesForOffering != null && !relatedFeaturesForOffering.isEmpty()) {
            Collection<String> relatedFeatureMap = relatedFeaturesForOffering.get(offering);
            for (String relatedFeature : relatedFeatureMap) {
                relatedFeatures.put(relatedFeature, getCache().getKRelatedFeaturesVRole().get(relatedFeature));
                // if (relatedFeature.toLowerCase().contains("http")) {
                // relatedFeatures.put(relatedFeature,
                // getCache().getKRelatedFeaturesVRole().get(relatedFeature));
                // } else {
                //
                // String relatedFeatureID = getRelatedFeatureID(relatedFeature,
                // session, version);
                // if (relatedFeatureID != null) {
                // relatedFeatures.put(relatedFeatureID,
                // getCache().getKRelatedFeaturesVRole().get(relatedFeature));
                // }
                // }
            }
        } else {
            List<String> role = Collections.singletonList("featureOfInterestID");
            if (getCache().getKOfferingVFeatures().containsKey(offering))
            {
            	for (String foiID : getCache().getKOfferingVFeatures().get(offering)) 
            	{
            		if (getCache().getProcedures4FeatureOfInterest(foiID).contains(procedure)) 
            		{
            			relatedFeatures.put(foiID, role);
            		}
            	}
            }
        }
        sosOffering.setRelatedFeatures(relatedFeatures);
    }

    protected void setUpTimeForOffering(String offering, Session session, int id, SosOfferingsForContents sosOffering) {
        DateTime minDate = HibernateCriteriaQueryUtilities.getMinDate4Offering(offering, session);
        DateTime maxDate = HibernateCriteriaQueryUtilities.getMaxDate4Offering(offering, session);
        String phenTimeId = Sos2Constants.EN_PHENOMENON_TIME + "_" + id;
        sosOffering.setTime(new TimePeriod(minDate, maxDate, phenTimeId));
    }

    protected void setUpFeatureOfInterestTypesForOffering(String offering, Session session,
            SosOfferingsForContents sosOffering) {
        // TODO: if no foi contained, set allowed foitypes
        // insert featureOfInterestTypes
        Collection<String> featureTypes = getFeatureOfInterestTypes(offering, session);
        if (featureTypes == null || featureTypes.isEmpty()) {
            featureTypes = HibernateCriteriaQueryUtilities.getFeatureOfInterestTypes(session);
        }
        sosOffering.setFeatureOfInterestTypes(featureTypes);
    }

    protected void setUpResponseFormatForOffering(String version, SosOfferingsForContents sosOffering) {
        Collection<String> responseFormats = SosHelper.getSupportedResponseFormats(SosConstants.SOS, version);
        sosOffering.setResponseFormats(responseFormats);
        // TODO set as property
        if (true) {
            responseFormats.add(SosConstants.CONTENT_TYPE_ZIP);
        }
    }

    protected void setUpProcedureDescriptionFormatForOffering(SosOfferingsForContents sosOffering, Session session) {
        // TODO: set procDescFormat
        sosOffering.setProcedureDescriptionFormat(HibernateCriteriaQueryUtilities
                .getProcedureDescriptionFormatIdentifiers(session));
    }

    private Collection<String> getProceduresForOffering(String offering) throws OwsExceptionReport {
        Collection<String> procedures = getCache().getProcedures4Offering(offering);
        if (procedures == null || procedures.isEmpty()) {
            String exceptionText =
                    String.format(
                            "No procedures are contained in the database for the offering '%s'! Please contact the admin of this SOS.",
                            offering);
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
        return procedures;
    }
}
