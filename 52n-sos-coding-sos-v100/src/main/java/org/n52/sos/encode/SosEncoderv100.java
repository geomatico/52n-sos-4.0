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
package org.n52.sos.encode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import net.opengis.gml.AbstractTimeGeometricPrimitiveType;
import net.opengis.gml.BoundingShapeType;
import net.opengis.gml.CodeType;
import net.opengis.gml.DirectPositionType;
import net.opengis.gml.EnvelopeType;
import net.opengis.gml.ReferenceType;
import net.opengis.gml.TimePeriodType;
import net.opengis.ogc.ComparisonOperatorType;
import net.opengis.ogc.ComparisonOperatorsType;
import net.opengis.ogc.GeometryOperandsType;
import net.opengis.ogc.IdCapabilitiesType;
import net.opengis.ogc.ScalarCapabilitiesType;
import net.opengis.ogc.SpatialCapabilitiesType;
import net.opengis.ogc.SpatialOperatorNameType;
import net.opengis.ogc.SpatialOperatorType;
import net.opengis.ogc.SpatialOperatorsType;
import net.opengis.ogc.TemporalCapabilitiesType;
import net.opengis.ogc.TemporalOperandsType;
import net.opengis.ogc.TemporalOperatorNameType;
import net.opengis.ogc.TemporalOperatorType;
import net.opengis.ogc.TemporalOperatorsType;
import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.om.x10.ObservationCollectionType;
import net.opengis.om.x10.ObservationPropertyType;
import net.opengis.ows.x11.MimeType;
import net.opengis.ows.x11.OperationsMetadataDocument.OperationsMetadata;
import net.opengis.ows.x11.ServiceIdentificationDocument.ServiceIdentification;
import net.opengis.ows.x11.ServiceProviderDocument.ServiceProvider;
import net.opengis.sos.x10.CapabilitiesDocument;
import net.opengis.sos.x10.CapabilitiesDocument.Capabilities;
import net.opengis.sos.x10.ContentsDocument.Contents;
import net.opengis.sos.x10.ContentsDocument.Contents.ObservationOfferingList;
import net.opengis.sos.x10.FilterCapabilitiesDocument.FilterCapabilities;
import net.opengis.sos.x10.ObservationOfferingType;
import net.opengis.sos.x10.ResponseModeType;
import net.opengis.swe.x101.PhenomenonPropertyType;
import net.opengis.swe.x101.TimeGeometricPrimitivePropertyType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.sos.ogc.filter.FilterConstants.ComparisonOperator;
import org.n52.sos.ogc.filter.FilterConstants.SpatialOperator;
import org.n52.sos.ogc.filter.FilterConstants.TimeOperator;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosCapabilities;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.sos.SosOfferingsForContents;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.response.AbstractServiceResponse;
import org.n52.sos.response.DescribeSensorResponse;
import org.n52.sos.response.GetCapabilitiesResponse;
import org.n52.sos.response.GetObservationResponse;
import org.n52.sos.service.AbstractServiceCommunicationObject;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.N52XmlHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

public class SosEncoderv100 implements IEncoder<XmlObject, AbstractServiceCommunicationObject> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosEncoderv100.class);

    private static final Set<EncoderKey> ENCODER_KEYS = CollectionHelper.union(
        CodingHelper.encoderKeysForElements(Sos1Constants.NS_SOS,
            AbstractServiceRequest.class,
            AbstractServiceResponse.class,
            GetCapabilitiesResponse.class,
            GetObservationResponse.class,
            DescribeSensorResponse.class
        )
    );


    public SosEncoderv100() {
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
        nameSpacePrefixMap.put(Sos1Constants.NS_SOS, SosConstants.NS_SOS_PREFIX);
    }

    @Override
    public String getContentType() {
        return SosConstants.CONTENT_TYPE_XML;
    }

    @Override
    public XmlObject encode(AbstractServiceCommunicationObject communicationObject) throws OwsExceptionReport {
        Map<HelperValues, String> additionalValues = new EnumMap<HelperValues, String>(HelperValues.class);
        additionalValues.put(HelperValues.VERSION, Sos1Constants.SERVICEVERSION);
        return encode(communicationObject, additionalValues);
    }

    @Override
    public XmlObject encode(AbstractServiceCommunicationObject communicationObject,
            Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        if (communicationObject instanceof AbstractServiceRequest) {
            return encodeRequests((AbstractServiceRequest) communicationObject);
        } else if (communicationObject instanceof AbstractServiceResponse) {
            return encodeResponse((AbstractServiceResponse) communicationObject);
        }
        return null;
    }

    private XmlObject encodeRequests(AbstractServiceRequest request) throws OwsExceptionReport {
//        if (request instanceof GetResultTemplateRequest) {
//            return createGetResultTemplateRequest((GetResultTemplateRequest) request);
//        } else if (request instanceof GetResultRequest) {
//            return createGetResultRequest((GetResultRequest) request);
//        } else if (request instanceof GetCapabilitiesRequest) {
//            return createGetCapabilitiesRequest((GetCapabilitiesRequest) request);
//        }
        return null;
    }

    private XmlObject encodeResponse(AbstractServiceResponse response) throws OwsExceptionReport {
        if (response instanceof GetCapabilitiesResponse) {
            return createCapabilitiesDocument((GetCapabilitiesResponse) response);
        } else if (response instanceof DescribeSensorResponse) {
        	return createDescribeSensorResponse((DescribeSensorResponse) response);
        } else if (response instanceof GetObservationResponse) {
        	return createGetObservationResponseDocument((GetObservationResponse) response);
        }
//        } else if (response instanceof GetFeatureOfInterestResponse) {
//            return createGetFeatureOfInterestResponse((GetFeatureOfInterestResponse) response);
//        } else if (response instanceof GetObservationByIdResponse) {
//            return createGetObservationByIdResponse((GetObservationByIdResponse) response);
//        }
//        else if (response instanceof InsertObservationResponse) {
//            return createInsertObservationResponse((InsertObservationResponse) response);
//        } else if (response instanceof InsertResultTemplateResponse) {
//            return createInsertResultTemplateResponseDocument((InsertResultTemplateResponse) response);
//        } else if (response instanceof InsertResultResponse) {
//            return createInsertResultResponseDocument((InsertResultResponse) response);
//        } else if (response instanceof GetResultResponse) {
//            return createGetResultResponseDocument((GetResultResponse) response);
//        } else if (response instanceof GetResultTemplateResponse) {
//            return createGetResultTemplateResponseDocument((GetResultTemplateResponse) response);
//        }
        return null;
    }

    private XmlObject createCapabilitiesDocument(GetCapabilitiesResponse response) throws OwsExceptionReport {
        CapabilitiesDocument xbCapsDoc =
                CapabilitiesDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        // cursor for getting prefixes
        Capabilities xbCaps = xbCapsDoc.addNewCapabilities();

        // set version.
        xbCaps.setVersion(response.getVersion());

        SosCapabilities sosCapabilities = response.getCapabilities();

        if (sosCapabilities.getServiceIdentification() != null) {
            xbCaps.setServiceIdentification((ServiceIdentification) CodingHelper.encodeObjectToXml(OWSConstants.NS_OWS, sosCapabilities.getServiceIdentification()));
        }
        if (sosCapabilities.getServiceProvider() != null) {
            xbCaps.setServiceProvider((ServiceProvider) CodingHelper.encodeObjectToXml(OWSConstants.NS_OWS, sosCapabilities.getServiceProvider()));

        }
        if (sosCapabilities.getOperationsMetadata() != null
                && sosCapabilities.getOperationsMetadata().getOperations() != null
                && !sosCapabilities.getOperationsMetadata().getOperations().isEmpty()) {
            xbCaps.setOperationsMetadata((OperationsMetadata) CodingHelper.encodeObjectToXml(OWSConstants.NS_OWS, sosCapabilities.getOperationsMetadata()));
        }
        if (sosCapabilities.getFilterCapabilities() != null) {
            setFilterCapabilities(xbCaps.addNewFilterCapabilities(), sosCapabilities.getFilterCapabilities());
        }
        if (sosCapabilities.getContents() != null && !sosCapabilities.getContents().isEmpty()) {
            setContents(xbCaps.addNewContents(), sosCapabilities.getContents(), response.getVersion());

        }

//        if (sosCapabilities.getExtensions() != null && !sosCapabilities.getExtensions().isEmpty()) {
//            for (IExtension extension : sosCapabilities.getExtensions()) {
//                setExensions(xbCaps.addNewExtension(), extension);
//            }
//
//        }

        N52XmlHelper.setSchemaLocationToDocument(xbCapsDoc, N52XmlHelper.getSchemaLocationForSOS100());

        return xbCapsDoc;
    }

    private XmlObject createDescribeSensorResponse(DescribeSensorResponse response) throws OwsExceptionReport {

    	String outputFormat;
        if (response.getOutputFormat().equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE)) {
            outputFormat = SensorMLConstants.NS_SML;
        } else {
            outputFormat = response.getOutputFormat();
        }

        XmlObject xmlObject = CodingHelper.encodeObjectToXml(outputFormat, response.getSensorDescription());
        // describeSensorResponse.addNewDescription().addNewSensorDescription().addNewData().set(xmlObject);

        // set schema location
        N52XmlHelper.setSchemaLocationsToDocument(xmlObject, Collections
                .singletonList(N52XmlHelper.getSchemaLocationForSWE101()));
        return xmlObject;
    }

    private XmlObject createGetObservationResponseDocument(GetObservationResponse response) throws OwsExceptionReport {

    	// create ObservationCollectionDocument and add Collection
        ObservationCollectionDocument xb_obsColDoc = ObservationCollectionDocument.Factory.newInstance();
        ObservationCollectionType xb_obsCol = xb_obsColDoc.addNewObservationCollection();
        xb_obsCol.setId(SosConstants.OBS_COL_ID_PREFIX + new DateTime().getMillis());

        Collection<SosObservation> observationCollection = null;

        IEncoder<XmlObject, SosObservation> encoder = CodingHelper.getEncoder(response.getResponseFormat(), new SosObservation());
        if (!(encoder instanceof IObservationEncoder)) {
            String exceptionText = "Error while encoding GetObservation response, encoder is not of type IObservationEncoder!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
        IObservationEncoder<XmlObject, SosObservation> iObservationEncoder
                = (IObservationEncoder<XmlObject, SosObservation>) encoder;
        if (iObservationEncoder.shouldObservationsWithSameXBeMerged()) {
            response.mergeObservationsWithSameX();
        }

        observationCollection = response.getObservationCollection();

        if (observationCollection != null) {
        	if ( observationCollection.size() > 0) {
	            // TODO setBoundedBy (not necessary apparently?)
	
		        for (SosObservation sosObservation : observationCollection) {
		        	XmlObject xmlObject = CodingHelper.encodeObjectToXml(response.getResponseFormat(), sosObservation);
		        	xb_obsCol.addNewMember().addNewObservation().set(xmlObject);
		        }
        	} else {
                ObservationPropertyType xb_obs = xb_obsCol.addNewMember();
                xb_obs.setHref( GMLConstants.NIL_INAPPLICABLE );
            }
        } else {
            ObservationPropertyType xb_obs = xb_obsCol.addNewMember();
            xb_obs.setHref( GMLConstants.NIL_INAPPLICABLE );
        }

        // set schema location
        XmlHelper.makeGmlIdsUnique(xb_obsColDoc.getDomNode());
        List<String> schemaLocations = new ArrayList<String>();
        schemaLocations.add(N52XmlHelper.getSchemaLocationForSOS100());
        schemaLocations.add(N52XmlHelper.getSchemaLocationForOM100());
        schemaLocations.add(N52XmlHelper.getSchemaLocationForSA100());
        // schemaLocations.add(N52XmlHelper.getSchemaLocationForSWE101());
        N52XmlHelper.setSchemaLocationsToDocument(xb_obsColDoc, schemaLocations);
        return xb_obsColDoc;
    }

    /**
     * Sets the FilterCapabilities section to the capabilities document.
     *
     * @param filterCapabilities
     *
     * @param sosFilterCaps
     *            FilterCapabilities.
     */
    protected void setFilterCapabilities(FilterCapabilities filterCapabilities,
            org.n52.sos.ogc.filter.FilterCapabilities sosFilterCaps) {
        setScalarFilterCapabilities(filterCapabilities.addNewScalarCapabilities(), sosFilterCaps);
        setSpatialFilterCapabilities(filterCapabilities.addNewSpatialCapabilities(), sosFilterCaps);
        setTemporalFilterCapabilities(filterCapabilities.addNewTemporalCapabilities(), sosFilterCaps);
        setIdFilterCapabilities(filterCapabilities.addNewIdCapabilities());

    }

    /**
     * Sets the content section to the Capabilities document.
     *
     * @param xbContents
     *            SOS 2.0 contents section
     * @param offerings
     *            SOS offerings for contents
     * @param version
     *            SOS response version
     * @throws OwsExceptionReport
     *             if an error occurs.
     */
    private void setContents(Contents xbContents, Collection<SosOfferingsForContents> offerings, String version)
            throws OwsExceptionReport {
        // Contents xbContType = xbContents.addNewContents();
    	ObservationOfferingList xb_ooList = xbContents.addNewObservationOfferingList();

        for (SosOfferingsForContents offering : offerings) {

            ObservationOfferingType xb_oo = xb_ooList.addNewObservationOffering();
            // TDO check NAme or ID
            xb_oo.setId(offering.getOffering());

            // set bounded by element
            BoundingShapeType xb_boundedBy = xb_oo.addNewBoundedBy();
            xb_boundedBy.addNewEnvelope();

            // only if fois are contained for the offering set the values of the
            // envelope
            xb_boundedBy.setEnvelope(getBBOX4Offering(offering.getObservedArea().getEnvelope(), offering.getObservedArea()
                    .getSrid()));

            // TODO: add intended application
            // xb_oo.addIntendedApplication("");

            // add offering name
            CodeType xb_name = xb_oo.addNewName();
            xb_name.setStringValue(offering.getOfferingName());
//
//            // set up phenomena
//            Collection<String> phenomenons = offering.getObservableProperties();
//            Collection<String> compositePhenomena = offering.getCompositePhenomena();
//            Collection<String> componentsOfCompPhens = new ArrayList<String>();

            // set up composite phenomena
//            if (compositePhenomena != null) {
//                // first add a new compositePhenomenon for every
//                // compositePhenomenon
//                for (String compositePhenomenon : compositePhenomena) {
//                    Collection<String> components = offering.getPhens4CompPhens().get(compositePhenomenon);
//                    componentsOfCompPhens.addAll(components);
//                    if (components != null) {
//                        PhenomenonPropertyType xb_opType = xb_oo.addNewObservedProperty();
//                        xb_opType.set(SosConfigurator.getInstance().getOmEncoder()
//                                .createCompositePhenomenon(compositePhenomenon, components));
//                    }
//                }
//            }

            // set observableProperties [0..*]
            for (String phenomenon : offering.getObservableProperties()) {
            	PhenomenonPropertyType xb_ootype = xb_oo.addNewObservedProperty();
                xb_ootype.setHref(phenomenon);
            }

            // set up time
            if (offering.getTime() instanceof TimePeriod) {
                TimeGeometricPrimitivePropertyType xb_time = xb_oo.addNewTime();
                TimePeriod tp = (TimePeriod) offering.getTime();
                if (tp.getStart() != null && tp.getEnd() != null) {
                        AbstractTimeGeometricPrimitiveType xb_gp = xb_time.addNewTimeGeometricPrimitive();
                        xb_gp.set((TimePeriodType) CodingHelper.encodeObjectToXml(GMLConstants.NS_GML, offering.getTime()));
                }
                // TODO check GML 311 rename nodename of geometric primitive to gml:timePeriod
                XmlCursor timeCursor = xb_time.newCursor();
                boolean hasTimePrimitive =
                        timeCursor.toChild(new QName(GMLConstants.NS_GML, GMLConstants.EN_ABSTRACT_TIME_GEOM_PRIM));
                if (hasTimePrimitive) {
                    timeCursor.setName(new QName(GMLConstants.NS_GML, GMLConstants.EN_TIME_PERIOD));
                }
                timeCursor.dispose();
            }

            // add feature of interests
            if (offering.getFeatureOfInterestTypes() != null) {
                for (String featureOfInterestType : offering.getFeatureOfInterestTypes()) {
                    ReferenceType xb_foiRefType = xb_oo.addNewFeatureOfInterest();
                    xb_foiRefType.setHref(featureOfInterestType);
                }
            }

            // set procedures
            if (offering.getProcedureDescriptionFormat() != null
                    && !offering.getProcedureDescriptionFormat().isEmpty()) {
                for (String procedureDescriptionFormat : offering.getProcedureDescriptionFormat()) {
                    ReferenceType xb_procedureProperty = xb_oo.addNewProcedure();
                    xb_procedureProperty.setHref(procedureDescriptionFormat);
                }
            }
            for (String procedure : offering.getProcedures()) {
            	ReferenceType xb_procedureProperty = xb_oo.addNewProcedure();
                xb_procedureProperty.setHref(procedure);
            }

            // insert result models
            Collection<QName> resultModels = offering.getResultModels();

            if (resultModels == null || resultModels.isEmpty()) {

                String exceptionText = "No result models are contained in the database for the offering: " + offering
                        + "! Please contact the admin of this SOS.";
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }

//            for (QName resultModelQName : resultModels) {
//                XmlQName xb_resultModel = xb_oo.addNewResultModel();
////                 xb_resultModel.setStringValue(rmString.getPrefix() + ":" +
////                 rmString.getLocalPart());
//                // xb_resultModel.set(Sos1Constants.RESULT_MODEL_MEASUREMENT);
//                // xb_resultModel.setStringValue("om:Measurement");
//                // QName qName = new QName(rmString.getPrefix(),
//                // rmString.getLocalPart());
//                xb_resultModel.setQNameValue(resultModelQName);
//                // TODO: Change if XmlBeans-Bug is fixed
//                // String value = cursor.getTextValue();
//                // cursor.setTextValue(value.replaceFirst("ns",
//                // OMConstants.NS_OM_PREFIX));
//            }

            // set response format

            // set responseFormat [0..*]
            if (offering.getResponseFormats() != null) {
                for (String responseFormat : offering.getResponseFormats()) {
                	MimeType xb_respFormat = xb_oo.addNewResponseFormat();
                    xb_respFormat.setStringValue(responseFormat);
                }
            }

            // set response Mode
            for (String responseMode : offering.getResponseModes()) {
                ResponseModeType xb_respMode = xb_oo.addNewResponseMode();
                xb_respMode.setStringValue(responseMode);
            }
        }
    }

    /**
     * Set the IdFilterCapabilities.
     *
     * !!! Modify method addicted to your implementation !!!
     *
     * @param idCapabilities
     *            IdCapabilities.
     */
    protected void setIdFilterCapabilities(IdCapabilitiesType idCapabilities) {

        idCapabilities.addNewFID();
        idCapabilities.addNewEID();
    }

    /**
     * Sets the SpatialFilterCapabilities.
     *
     * !!! Modify method addicted to your implementation !!!
     *
     * @param spatialCapabilities
     *            SpatialCapabilities.
     * @param sosFilterCaps
     */
    protected void setSpatialFilterCapabilities(SpatialCapabilitiesType spatialCapabilities,
            org.n52.sos.ogc.filter.FilterCapabilities sosFilterCaps) {

        // set GeometryOperands
        if (!sosFilterCaps.getSpatialOperands().isEmpty()) {
            GeometryOperandsType spatialOperands = spatialCapabilities.addNewGeometryOperands();
            for (QName operand : sosFilterCaps.getSpatialOperands()) {
                spatialOperands.addGeometryOperand(operand);
            }
        }

        // set SpatialOperators
        if (!sosFilterCaps.getSpatialOperators().isEmpty()) {
            SpatialOperatorsType spatialOps = spatialCapabilities.addNewSpatialOperators();
            Set<SpatialOperator> keys = sosFilterCaps.getSpatialOperators().keySet();
            for (SpatialOperator spatialOperator : keys) {
                SpatialOperatorType operator = spatialOps.addNewSpatialOperator();
                operator.setName(getEnum4SpatialOperator(spatialOperator));
                GeometryOperandsType bboxGeomOps = operator.addNewGeometryOperands();
                for (QName operand : sosFilterCaps.getSpatialOperators().get(spatialOperator)) {
                    bboxGeomOps.addGeometryOperand(operand);
                }
            }
        }
    }

    /**
     * Sets the TemporalFilterCapabilities.
     *
     * !!! Modify method addicted to your implementation !!!
     *
     * @param temporalCapabilities
     *            TemporalCapabilities.
     * @param sosFilterCaps
     */
    protected void setTemporalFilterCapabilities(TemporalCapabilitiesType temporalCapabilities,
            org.n52.sos.ogc.filter.FilterCapabilities sosFilterCaps) {

        // set TemporalOperands
        if (!sosFilterCaps.getTemporalOperands().isEmpty()) {
            TemporalOperandsType tempOperands = temporalCapabilities.addNewTemporalOperands();
            for (QName operand : sosFilterCaps.getTemporalOperands()) {
                tempOperands.addTemporalOperand(operand);
            }
        }

        // set TemporalOperators
        if (!sosFilterCaps.getTempporalOperators().isEmpty()) {
            TemporalOperatorsType temporalOps = temporalCapabilities.addNewTemporalOperators();
            Set<TimeOperator> keys = sosFilterCaps.getTempporalOperators().keySet();
            for (TimeOperator temporalOperator : keys) {
                TemporalOperatorType operator = temporalOps.addNewTemporalOperator();
                operator.setName(getEnum4TemporalOperator(temporalOperator));
                TemporalOperandsType bboxGeomOps = operator.addNewTemporalOperands();
                for (QName operand : sosFilterCaps.getTempporalOperators().get(temporalOperator)) {
                    bboxGeomOps.addTemporalOperand(operand);
                }
            }
        }
    }

    /**
     * Sets the ScalarFilterCapabilities.
     *
     * !!! Modify method addicted to your implementation !!!
     *
     * @param scalarCapabilities
     *            ScalarCapabilities.
     * @param sosFilterCaps
     */
    protected void setScalarFilterCapabilities(ScalarCapabilitiesType scalarCapabilities,
            org.n52.sos.ogc.filter.FilterCapabilities sosFilterCaps) {

        if (!sosFilterCaps.getComparisonOperators().isEmpty()) {
            ComparisonOperatorsType scalarOps = scalarCapabilities.addNewComparisonOperators();
            for (ComparisonOperator operator : sosFilterCaps.getComparisonOperators()) {
                scalarOps.addComparisonOperator(getEnum4ComparisonOperator(operator));
            }
        }
    }

    /**
     * Get the Enum for the spatial operator.
     *
     * @param spatialOperator
     *            Supported spatial operator
     * @return Enum
     */
    protected net.opengis.ogc.SpatialOperatorNameType.Enum getEnum4SpatialOperator(SpatialOperator spatialOperator) {
        switch (spatialOperator) {
        case BBOX:
            return SpatialOperatorNameType.BBOX;
        case Beyond:
            return SpatialOperatorNameType.BEYOND;
        case Contains:
            return SpatialOperatorNameType.CONTAINS;
        case Crosses:
            return SpatialOperatorNameType.CROSSES;
        case Disjoint:
            return SpatialOperatorNameType.DISJOINT;
        case DWithin:
            return SpatialOperatorNameType.D_WITHIN;
        case Equals:
            return SpatialOperatorNameType.EQUALS;
        case Intersects:
            return SpatialOperatorNameType.INTERSECTS;
        case Overlaps:
            return SpatialOperatorNameType.OVERLAPS;
        case Touches:
            return SpatialOperatorNameType.TOUCHES;
        case Within:
            return SpatialOperatorNameType.WITHIN;
        default:
            break;
        }
        return null;
    }

    /**
     * Get the Enum for the temporal operator.
     *
     * @param temporalOperator
     *            Supported temporal operator
     * @return Enum
     */
    protected net.opengis.ogc.TemporalOperatorNameType.Enum getEnum4TemporalOperator(TimeOperator temporalOperator) {
        switch (temporalOperator) {
        case TM_After:
            return TemporalOperatorNameType.TM_AFTER;
        case TM_Before:
            return TemporalOperatorNameType.TM_BEFORE;
        case TM_Begins:
            return TemporalOperatorNameType.TM_BEGINS;
        case TM_BegunBy:
            return TemporalOperatorNameType.TM_BEGUN_BY;
        case TM_Contains:
            return TemporalOperatorNameType.TM_CONTAINS;
        case TM_During:
            return TemporalOperatorNameType.TM_DURING;
        case TM_EndedBy:
            return TemporalOperatorNameType.TM_ENDED_BY;
        case TM_Ends:
            return TemporalOperatorNameType.TM_ENDS;
        case TM_Equals:
            return TemporalOperatorNameType.TM_EQUALS;
        case TM_Meets:
            return TemporalOperatorNameType.TM_MEETS;
        case TM_MetBy:
            return TemporalOperatorNameType.TM_MET_BY;
        case TM_OverlappedBy:
            return TemporalOperatorNameType.TM_OVERLAPPED_BY;
        case TM_Overlaps:
            return TemporalOperatorNameType.TM_OVERLAPS;
        default:
            break;
        }
        return null;
    }

    /**
     * Get the Enum for the comparison operator.
     *
     * @param comparisonOperator
     *            Supported comparison operator
     * @return Enum
     */
    protected net.opengis.ogc.ComparisonOperatorType.Enum getEnum4ComparisonOperator(
            ComparisonOperator comparisonOperator) {
        switch (comparisonOperator) {
        case PropertyIsBetween:
            return ComparisonOperatorType.BETWEEN;
        case PropertyIsEqualTo:
            return ComparisonOperatorType.EQUAL_TO;
        case PropertyIsGreaterThan:
            return ComparisonOperatorType.GREATER_THAN;
        case PropertyIsGreaterThanOrEqualTo:
            return ComparisonOperatorType.GREATER_THAN_EQUAL_TO;
        case PropertyIsLessThan:
            return ComparisonOperatorType.LESS_THAN;
        case PropertyIsLessThanOrEqualTo:
            return ComparisonOperatorType.LESS_THAN_EQUAL_TO;
        case PropertyIsLike:
            return ComparisonOperatorType.LIKE;
        case PropertyIsNotEqualTo:
            return ComparisonOperatorType.NOT_EQUAL_TO;
        case PropertyIsNull:
            return ComparisonOperatorType.NULL_CHECK;
        default:
            break;
        }
        return null;
    }

    /**
     * queries the bounding box of all requested feature of interest IDs
     *
     * @param envelope
     *
     * @param foiIDs
     *            ArrayList with String[]s containing the ids of the feature of
     *            interests for which the BBOX should be returned
     * @return Returns EnvelopeType XmlBean which represents the BBOX of the
     *         requested feature of interests
     * @throws OwsExceptionReport
     *             if query of the BBOX failed
     */
    private EnvelopeType getBBOX4Offering(Envelope envelope, int srsID) throws OwsExceptionReport {

        double minx = envelope.getMinX();
        double maxx = envelope.getMaxX();
        double miny = envelope.getMinY();
        double maxy = envelope.getMaxY();
        @SuppressWarnings("unused")
        String minz = null;
        @SuppressWarnings("unused")
        String maxz = null;

        EnvelopeType envelopeType = EnvelopeType.Factory.newInstance();

        // set lower corner
        // TODO for full 3D support add minz to parameter in setStringValue
        DirectPositionType lowerCorner = envelopeType.addNewLowerCorner();
        DirectPositionType upperCorner = envelopeType.addNewUpperCorner();
        if (srsID > 0) {
            if (!Configurator.getInstance().reversedAxisOrderRequired(srsID)) {
                lowerCorner.setStringValue(minx + " " + miny);
            } else {
                lowerCorner.setStringValue(miny + " " + minx);
            }

            // set upper corner
            // TODO for full 3D support add maxz to parameter in setStringValue
            if (!Configurator.getInstance().reversedAxisOrderRequired(srsID)) {
                upperCorner.setStringValue(maxx + " " + maxy);
            } else {
                upperCorner.setStringValue(maxy + " " + maxx);
            }
            // set SRS
            envelopeType.setSrsName(Configurator.getInstance().getSrsNamePrefixSosV2() + srsID);
        }

        return envelopeType;
    }

}
