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

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

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
import net.opengis.sos.x10.CapabilitiesDocument;
import net.opengis.sos.x10.CapabilitiesDocument.Capabilities;
import net.opengis.sos.x10.ContentsDocument.Contents;
import net.opengis.sos.x10.ContentsDocument.Contents.ObservationOfferingList;
import net.opengis.sos.x10.FilterCapabilitiesDocument.FilterCapabilities;
import net.opengis.sos.x10.ObservationOfferingType;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.sos.ogc.filter.FilterConstants.ComparisonOperator;
import org.n52.sos.ogc.filter.FilterConstants.SpatialOperator;
import org.n52.sos.ogc.filter.FilterConstants.TimeOperator;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosCapabilities;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.ogc.sos.SosObservationOffering;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.response.AbstractServiceResponse;
import org.n52.sos.response.GetCapabilitiesResponse;
import org.n52.sos.response.GetObservationResponse;
import org.n52.sos.service.AbstractServiceCommunicationObject;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.N52XmlHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosEncoderv100 implements Encoder<XmlObject, AbstractServiceCommunicationObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SosEncoderv100.class);

    @SuppressWarnings("unchecked")
    private static final Set<EncoderKey> ENCODER_KEYS = CollectionHelper.union(CodingHelper.encoderKeysForElements(
            Sos1Constants.NS_SOS, AbstractServiceRequest.class, AbstractServiceResponse.class,
            GetCapabilitiesResponse.class));

    public SosEncoderv100() {
        LOGGER.debug("Encoder for the following keys initialized successfully: {}!",
                StringHelper.join(", ", ENCODER_KEYS));
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
        throw new UnsupportedEncoderInputException(this, communicationObject);
    }

    private XmlObject encodeRequests(AbstractServiceRequest request) throws OwsExceptionReport {
        // if (request instanceof GetResultTemplateRequest) {
        // return createGetResultTemplateRequest((GetResultTemplateRequest)
        // request);
        // } else if (request instanceof GetResultRequest) {
        // return createGetResultRequest((GetResultRequest) request);
        // } else if (request instanceof GetCapabilitiesRequest) {
        // return createGetCapabilitiesRequest((GetCapabilitiesRequest)
        // request);
        // }
        throw new UnsupportedEncoderInputException(this, request);
    }

    private XmlObject encodeResponse(AbstractServiceResponse response) throws OwsExceptionReport {
        if (response instanceof GetCapabilitiesResponse) {
            return createCapabilitiesDocument((GetCapabilitiesResponse) response);
        }
//        } else if (response instanceof DescribeSensorResponse) {
//            return createDescribeSensorResponse((DescribeSensorResponse) response);
//        } else if (response instanceof GetObservationResponse) {
//            return createGetObservationResponseDocument((GetObservationResponse) response);
//        } else if (response instanceof GetFeatureOfInterestResponse) {
//            return createGetFeatureOfInterestResponse((GetFeatureOfInterestResponse) response);
//        } else if (response instanceof GetObservationByIdResponse) {
//            return createGetObservationByIdResponseDocument((GetObservationByIdResponse) response);
//        }
        // else if (response instanceof InsertObservationResponse) {
        // return createInsertObservationResponse((InsertObservationResponse)
        // response);
        // } else if (response instanceof InsertResultTemplateResponse) {
        // return
        // createInsertResultTemplateResponseDocument((InsertResultTemplateResponse)
        // response);
        // } else if (response instanceof InsertResultResponse) {
        // return createInsertResultResponseDocument((InsertResultResponse)
        // response);
        // } else if (response instanceof GetResultResponse) {
        // return createGetResultResponseDocument((GetResultResponse) response);
        // } else if (response instanceof GetResultTemplateResponse) {
        // return
        // createGetResultTemplateResponseDocument((GetResultTemplateResponse)
        // response);
        // }
        throw new UnsupportedEncoderInputException(this, response);
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
            xbCaps.addNewServiceIdentification().set(CodingHelper.encodeObjectToXml(
                    OWSConstants.NS_OWS, sosCapabilities.getServiceIdentification()));
        }
        if (sosCapabilities.getServiceProvider() != null) {
            xbCaps.addNewServiceProvider().set(CodingHelper.encodeObjectToXml(OWSConstants.NS_OWS,
                    sosCapabilities.getServiceProvider()));

        }
        if (sosCapabilities.getOperationsMetadata() != null
                && sosCapabilities.getOperationsMetadata().getOperations() != null
                && !sosCapabilities.getOperationsMetadata().getOperations().isEmpty()) {
            xbCaps.addNewOperationsMetadata().set(CodingHelper.encodeObjectToXml(OWSConstants.NS_OWS,
                    sosCapabilities.getOperationsMetadata()));
        }
        if (sosCapabilities.getFilterCapabilities() != null) {
            setFilterCapabilities(xbCaps.addNewFilterCapabilities(), sosCapabilities.getFilterCapabilities());
        }
        if (sosCapabilities.getContents() != null && !sosCapabilities.getContents().isEmpty()) {
            setContents(xbCaps.addNewContents(), sosCapabilities.getContents(), response.getVersion());
        }

        N52XmlHelper.setSchemaLocationToDocument(xbCapsDoc, N52XmlHelper.getSchemaLocationForSOS100());

        return xbCapsDoc;
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
     * 
     * 
     * @throws OwsExceptionReport
     *             * if an error occurs.
     */
    private void setContents(Contents xbContents, Collection<SosObservationOffering> offerings, String version)
            throws OwsExceptionReport {
        // Contents xbContType = xbContents.addNewContents();
        ObservationOfferingList xbObservationOfferings = xbContents.addNewObservationOfferingList();

        for (SosObservationOffering offering : offerings) {

            ObservationOfferingType xbObservationOffering = xbObservationOfferings.addNewObservationOffering();
            // TDO check NAme or ID
            xbObservationOffering.setId(offering.getOffering());

            // only if fois are contained for the offering set the values of the
            // envelope
            Encoder<XmlObject, SosEnvelope> encoder = CodingHelper.getEncoder(GMLConstants.NS_GML, offering.getObservedArea());
            xbObservationOffering.addNewBoundedBy().addNewEnvelope().set(encoder.encode(offering.getObservedArea()));

            // TODO: add intended application
            // xbObservationOffering.addIntendedApplication("");

            // add offering name
            xbObservationOffering.addNewName().setStringValue(offering.getOfferingName());
            //
            // // set up phenomena
            // Collection<String> phenomenons =
            // offering.getObservableProperties();
            // Collection<String> compositePhenomena =
            // offering.getCompositePhenomena();
            // Collection<String> componentsOfCompPhens = new
            // ArrayList<String>();

            // set up composite phenomena
            // if (compositePhenomena != null) {
            // // first add a new compositePhenomenon for every
            // // compositePhenomenon
            // for (String compositePhenomenon : compositePhenomena) {
            // Collection<String> components =
            // offering.getPhens4CompPhens().get(compositePhenomenon);
            // componentsOfCompPhens.addAll(components);
            // if (components != null) {
            // PhenomenonPropertyType xb_opType =
            // xb_oo.addNewObservedProperty();
            // xb_opType.set(SosConfigurator.getInstance().getOmEncoder()
            // .createCompositePhenomenon(compositePhenomenon, components));
            // }
            // }
            // }

            // set observableProperties [0..*]
            for (String phenomenon : offering.getObservableProperties()) {
                xbObservationOffering.addNewObservedProperty().setHref(phenomenon);
            }

            // set up time
            if (offering.getPhenomenonTime() instanceof TimePeriod) {
                XmlObject encodeObject = CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE_101, offering.getPhenomenonTime());
                xbObservationOffering.addNewTime().set(encodeObject);
            }

            // add feature of interests
            if (offering.isSetFeatureOfInterestTypes()) {
                for (String featureOfInterestType : offering.getFeatureOfInterestTypes()) {
                    xbObservationOffering.addNewFeatureOfInterest().setHref(featureOfInterestType);
                }
            }

            // set procedures
            if (offering.isSetProcedureDescriptionFormats()) {
                for (String procedureDescriptionFormat : offering.getProcedureDescriptionFormat()) {
                    xbObservationOffering.addNewProcedure().setHref(procedureDescriptionFormat);
                }
            }
            for (String procedure : offering.getProcedures()) {
                xbObservationOffering.addNewProcedure().setHref(procedure);
            }
            
            for (String featureOfInterest : offering.getFeatureOfInterest()) {
                xbObservationOffering.addNewFeatureOfInterest().setHref(featureOfInterest);
            }

            // insert result models
            Collection<QName> resultModels = offering.getResultModels();

            if (CollectionHelper.isEmpty(resultModels)) {
                throw new NoApplicableCodeException()
                        .withMessage(
                                "No result models are contained in the database for the offering: %s! Please contact the admin of this SOS.",
                                offering);
            }

            // for (QName resultModelQName : resultModels) {
            // XmlQName xbResultModel = xb_oo.addNewResultModel();
            // // xbResultModel.setStringValue(rmString.getPrefix() + ":" +
            // // rmString.getLocalPart());
            // // xbResultModel.set(Sos1Constants.RESULT_MODEL_MEASUREMENT);
            // // xbResultModel.setStringValue("om:Measurement");
            // // QName qName = new QName(rmString.getPrefix(),
            // // rmString.getLocalPart());
            // xbResultModel.setQNameValue(resultModelQName);
            // // TODO: Change if XmlBeans-Bug is fixed
            // // String value = cursor.getTextValue();
            // // cursor.setTextValue(value.replaceFirst("ns",
            // // OMConstants.NS_OM_PREFIX));
            // }

            // set responseFormat [0..*]
            if (offering.isSetResponseFormats()) {
                for (String responseFormat : offering.getResponseFormats()) {
                    xbObservationOffering.addNewResponseFormat().setStringValue(responseFormat);
                }
            }

            // set response Mode
            for (String responseMode : offering.getResponseModes()) {
                xbObservationOffering.addNewResponseMode().setStringValue(responseMode);
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
}
