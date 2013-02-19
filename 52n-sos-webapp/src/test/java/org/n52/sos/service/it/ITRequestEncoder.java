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
package org.n52.sos.service.it;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import net.opengis.gml.EnvelopeType;
import net.opengis.gml.TimeInstantType;
import net.opengis.gml.TimePeriodType;
import net.opengis.ogc.BBOXType;
import net.opengis.ogc.BinaryComparisonOpType;
import net.opengis.ogc.BinaryTemporalOpType;
import net.opengis.ogc.ComparisonOpsType;
import net.opengis.ogc.ExpressionType;
import net.opengis.ogc.LiteralType;
import net.opengis.ogc.PropertyIsBetweenType;
import net.opengis.ogc.PropertyIsLikeType;
import net.opengis.ogc.PropertyIsNullType;
import net.opengis.ogc.PropertyNameType;
import net.opengis.sos.x10.GetObservationDocument;
import net.opengis.sos.x10.GetObservationDocument.GetObservation;
import net.opengis.sos.x10.GetObservationDocument.GetObservation.EventTime;
import net.opengis.sos.x10.GetObservationDocument.GetObservation.FeatureOfInterest;
import net.opengis.sos.x10.ResponseModeType;

import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.filter.ComparisonFilter;
import org.n52.sos.ogc.filter.FilterConstants;
import org.n52.sos.ogc.filter.FilterConstants.ComparisonOperator;
import org.n52.sos.ogc.filter.SpatialFilter;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.GetObservationRequest;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.DateTimeException;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.JTSHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

/**
 * class encapsulates operations for creating request documents for SOS operations
 * 
 */
public class ITRequestEncoder {
    // QNames for use in xmlbeans substitutions
    public static final QName QN_TIME_INSTANT = new QName(GMLConstants.NS_GML,
                                                          GMLConstants.EN_TIME_INSTANT,
                                                          GMLConstants.NS_GML_PREFIX);

    public static final QName QN_TIME_PERIOD = new QName(GMLConstants.NS_GML,
                                                         GMLConstants.EN_TIME_PERIOD,
                                                         GMLConstants.NS_GML_PREFIX);

    public static final QName QN_TM_EQUALS = new QName(OGCConstants.NS_OGC,
                                                       FilterConstants.TimeOperator.TM_Equals.name(),
                                                       OGCConstants.NS_OGC_PREFIX);

    public static final QName QN_TM_BEFORE = new QName(OGCConstants.NS_OGC,
                                                       FilterConstants.TimeOperator.TM_Before.name(),
                                                       OGCConstants.NS_OGC_PREFIX);

    public static final QName QN_TM_AFTER = new QName(OGCConstants.NS_OGC,
                                                      FilterConstants.TimeOperator.TM_Before.name(),
                                                      OGCConstants.NS_OGC_PREFIX);

    public static final QName QN_TM_DURING = new QName(OGCConstants.NS_OGC,
                                                       FilterConstants.TimeOperator.TM_During.name(),
                                                       OGCConstants.NS_OGC_PREFIX);

    public static final Map<FilterConstants.TimeOperator, QName> timeOpQNameMap;

    public static final String OM_SAMPLING_TIME = "om:samplingTime";

    // logger
    public static final Logger LOGGER = LoggerFactory.getLogger(ITRequestEncoder.class);

    static {
        // initialize map to look up QName by time operator
        timeOpQNameMap = new HashMap<FilterConstants.TimeOperator, QName>();
        timeOpQNameMap.put(FilterConstants.TimeOperator.TM_Equals, QN_TM_EQUALS);
        timeOpQNameMap.put(FilterConstants.TimeOperator.TM_Before, QN_TM_BEFORE);
        timeOpQNameMap.put(FilterConstants.TimeOperator.TM_After, QN_TM_AFTER);
        timeOpQNameMap.put(FilterConstants.TimeOperator.TM_During, QN_TM_DURING);
    }

    /**
     * creates GetObservationDocument from SosGetObservationRequest
     * 
     * @param assignedSensorID
     *        id of new registered sensor
     * @return Returns XMLBeans representation of RegisterSensorResponse
     * @throws OwsExceptionReport
     */
    public GetObservationDocument createGetObservationRequest(AbstractServiceRequest request) throws OwsExceptionReport {
        if (request instanceof GetObservationRequest) {
            GetObservationRequest req = (GetObservationRequest) request;

            GetObservationDocument xb_getObsDoc = GetObservationDocument.Factory.newInstance();
            GetObservation xb_getObs = xb_getObsDoc.addNewGetObservation();

            if (req.getService() != null) {
                xb_getObs.setService(req.getService());
            }

            if (req.getVersion() != null) {
                xb_getObs.setVersion(req.getVersion());
            }

            if (req.getSrid() == -1) {
                xb_getObs.setSrsName(SosConstants.PARAMETER_NOT_SET);
            }
            else {
                xb_getObs.setSrsName(Configurator.getInstance().getSrsNamePrefix() + req.getSrid());
            }

            if (req.getOfferings() != null) {
                xb_getObs.setOffering(req.getOfferings().get(0));
            }

            if (req.getTemporalFilters() != null && !req.getTemporalFilters().isEmpty()) {
                for (TemporalFilter tf : req.getTemporalFilters()) {
                    EventTime xb_eventTime = xb_getObs.addNewEventTime();
                    encodeTemporalFilter(tf, xb_eventTime);
                }
            }

            if (req.getProcedures() != null && !req.getProcedures().isEmpty()) {
                for (String procedure : req.getProcedures()) {
                    xb_getObs.addNewProcedure().setStringValue(procedure);
                }
            }

            if (req.getObservedProperties() != null && !req.getObservedProperties().isEmpty()) {
                for (String observableProperty : req.getObservedProperties()) {
                    xb_getObs.addNewObservedProperty().setStringValue(observableProperty);
                }
            }

            if (req.getFeatureIdentifiers() != null && !req.getFeatureIdentifiers().isEmpty()) {
                for (String featureOfInterest : req.getFeatureIdentifiers()) {
                    FeatureOfInterest xb_foi = xb_getObs.addNewFeatureOfInterest();
                    xb_foi.addNewObjectID().setStringValue(featureOfInterest);
                }
            }
            else if (req.getSpatialFilter() != null) {
                SpatialFilter sf = req.getSpatialFilter();
                FeatureOfInterest xb_foi = xb_getObs.addNewFeatureOfInterest();

                if (sf.getOperator().equals(FilterConstants.SpatialOperator.BBOX)) {
                    BBOXType bboxType = (BBOXType) xb_foi.addNewSpatialOps().changeType(BBOXType.type);

                    createBBOXTypeFromGeometry(sf.getGeometry(), bboxType);
                }
            }

            if (req.getResult() != null) {
                encodeComparisonFilter(req.getResult(), xb_getObs.addNewResult().addNewComparisonOps());
            }

            if (req.getResponseFormat() != null) {
                xb_getObs.setResponseFormat(req.getResponseFormat());
            }

            if (req.getResultModel() != null) {
                xb_getObs.setResultModel(req.getResultModel());
            }

            if (req.getResponseMode() != null && !req.getResponseMode().equals(SosConstants.PARAMETER_NOT_SET)) {
                xb_getObs.setResponseMode(getResponseModeEnum(req.getResponseMode()));
            }

            return xb_getObsDoc;
        }
        return null;
    }

    /**
     * Encodes a ComparisonFilter to a ComparisonOpsType XML bean
     * 
     * @param filter
     *        The filter to be encoded
     * @param xb_compOpsType
     *        The pre-created ComparisonOpsType XML bean to populate
     * @return Populated ComparisonOpsType XML bean (for convenience)
     * @throws OwsExceptionReport
     */
    public static ComparisonOpsType encodeComparisonFilter(ComparisonFilter filter, ComparisonOpsType xb_compOpsType) throws OwsExceptionReport {
        switch (filter.getOperator()) {
        case PropertyIsBetween:
            PropertyIsBetweenType xb_propBetween = (PropertyIsBetweenType) xb_compOpsType.changeType(PropertyIsBetweenType.type);
            xb_propBetween.addNewExpression().newCursor().setTextValue(filter.getPropertyName());
            xb_propBetween.addNewLowerBoundary().addNewExpression().newCursor().setTextValue(filter.getValue());
            xb_propBetween.addNewUpperBoundary().addNewExpression().newCursor().setTextValue(filter.getValueUpper());
            break;
        case PropertyIsLike:
            PropertyIsLikeType xb_propLike = (PropertyIsLikeType) xb_compOpsType.changeType(PropertyIsLikeType.type);
            xb_propLike.addNewPropertyName().newCursor().setTextValue(filter.getPropertyName());
            xb_propLike.addNewLiteral().newCursor().setTextValue(filter.getValue());
            xb_propLike.setWildCard(filter.getWildCard());
            xb_propLike.setSingleChar(filter.getSingleChar());
            xb_propLike.setEscapeChar(filter.getEscapeString());
            break;
        case PropertyIsNull:
            PropertyIsNullType xb_propNull = (PropertyIsNullType) xb_compOpsType.changeType(PropertyIsNullType.type);
            xb_propNull.addNewPropertyName().newCursor().setTextValue(filter.getPropertyName());
            break;
        case PropertyIsEqualTo:
            BinaryComparisonOpType xb_propEqual = (BinaryComparisonOpType) xb_compOpsType.changeType(BinaryComparisonOpType.type);
            xb_propEqual.getDomNode().setPrefix("ogc");
            xb_propEqual.getDomNode().setNodeValue(ComparisonOperator.PropertyIsEqualTo.toString());
            xb_propEqual.setExpressionArray(createExpressionArray(filter.getPropertyName(), filter.getValue()));
            break;
        case PropertyIsGreaterThan:
            BinaryComparisonOpType xb_propGreater = (BinaryComparisonOpType) xb_compOpsType.changeType(BinaryComparisonOpType.type);
            xb_propGreater.getDomNode().setPrefix("ogc");
            xb_propGreater.getDomNode().setNodeValue(ComparisonOperator.PropertyIsGreaterThan.toString());
            xb_propGreater.setExpressionArray(createExpressionArray(filter.getPropertyName(), filter.getValue()));
            break;
        case PropertyIsGreaterThanOrEqualTo:
            BinaryComparisonOpType xb_propGTE = (BinaryComparisonOpType) xb_compOpsType.changeType(BinaryComparisonOpType.type);
            xb_propGTE.getDomNode().setPrefix("ogc");
            xb_propGTE.getDomNode().setNodeValue(ComparisonOperator.PropertyIsGreaterThanOrEqualTo.toString());
            xb_propGTE.setExpressionArray(createExpressionArray(filter.getPropertyName(), filter.getValue()));
            break;
        case PropertyIsLessThan:
            BinaryComparisonOpType xb_propLess = (BinaryComparisonOpType) xb_compOpsType.changeType(BinaryComparisonOpType.type);
            xb_propLess.getDomNode().setPrefix("ogc");
            xb_propLess.getDomNode().setNodeValue(ComparisonOperator.PropertyIsLessThan.toString());
            xb_propLess.setExpressionArray(createExpressionArray(filter.getPropertyName(), filter.getValue()));
            break;
        case PropertyIsLessThanOrEqualTo:
            BinaryComparisonOpType xb_propLTE = (BinaryComparisonOpType) xb_compOpsType.changeType(BinaryComparisonOpType.type);
            xb_propLTE.getDomNode().setPrefix("ogc");
            xb_propLTE.getDomNode().setNodeValue(ComparisonOperator.PropertyIsLessThanOrEqualTo.toString());
            xb_propLTE.setExpressionArray(createExpressionArray(filter.getPropertyName(), filter.getValue()));
            break;
        case PropertyIsNotEqualTo:
            BinaryComparisonOpType xb_propNE = (BinaryComparisonOpType) xb_compOpsType.changeType(BinaryComparisonOpType.type);
            xb_propNE.getDomNode().setPrefix("ogc");
            xb_propNE.getDomNode().setNodeValue(ComparisonOperator.PropertyIsNotEqualTo.toString());
            xb_propNE.setExpressionArray(createExpressionArray(filter.getPropertyName(), filter.getValue()));
            break;
        default:
            String error = "Could not encode unknown comparison operator " + filter.getOperator().toString();
            throw Util4Exceptions.createInvalidParameterValueException(SosConstants.GetObservationParams.result.name(),
                                                                       error);
        }

        return xb_compOpsType;
    }

    /**
     * Creates an ExpressionArray from a property name and value for use in a BinaryComparisonOpType
     * 
     * @param propertyName
     *        The name of the filtered property
     * @param value
     *        The filter value
     * @return The ExpressionArray
     */
    public static ExpressionType[] createExpressionArray(String propertyName, String value) {
        ExpressionType[] expressionArray = new ExpressionType[2];

        PropertyNameType xb_propNameType = PropertyNameType.Factory.newInstance();
        xb_propNameType.newCursor().setTextValue(propertyName);
        expressionArray[0] = xb_propNameType;

        LiteralType xb_literalType = LiteralType.Factory.newInstance();
        xb_literalType.newCursor().setTextValue(propertyName);
        expressionArray[1] = xb_literalType;

        return expressionArray;
    }

    /**
     * Gets a ResponseModeType.Enum given a responseMode string, or throws exception if not found
     * 
     * @param responseMode
     *        The responseMode string to look up
     * @return The matching ResponseModeType.Enum
     * @throws OwsExceptionReport
     */
    public static ResponseModeType.Enum getResponseModeEnum(String responseMode) throws OwsExceptionReport {
        ResponseModeType.Enum responseModeEnum = (ResponseModeType.Enum) ResponseModeType.Enum.table.forString(responseMode);
        if (responseModeEnum == null) {
            String error = "Invalid ResponseMode.Enum " + responseMode;
            throw Util4Exceptions.createInvalidParameterValueException(SosConstants.GetObservationParams.responseMode.name(),
                                                                       error);
        }

        return responseModeEnum;
    }

    /**
     * Encodes a TemporalFilter and adds it to an EventTime xmlbean, using correct substitution groups
     * 
     * @param tf
     *        TemporalFilter to encode
     * @param xb_eventTime
     *        EventTime xmlbean to add encoded temporal filter to
     * @return EventTime arguments (for convenience)
     * @throws OwsExceptionReport
     */
    public static EventTime encodeTemporalFilter(TemporalFilter tf, EventTime xb_eventTime) throws OwsExceptionReport {
        switch (tf.getOperator()) {
        case TM_Equals:
        case TM_Before:
        case TM_After:
        case TM_During:
            QName timeOpQName = timeOpQNameMap.get(tf.getOperator());
            if (timeOpQName == null) {
                // OwsExceptionReport se = new OwsExceptionReport();
                String error = "No QName found for temporal operator " + tf.getOperator().name();
                throw Util4Exceptions.createInvalidParameterValueException(Sos1Constants.GetObservationParams.eventTime.name(),
                                                                           error);
            }
            BinaryTemporalOpType xb_binaryTempOp = (BinaryTemporalOpType) xb_eventTime.addNewTemporalOps().substitute(timeOpQName,
                                                                                                                      BinaryTemporalOpType.type);
            xb_binaryTempOp.addNewPropertyName().newCursor().setTextValue(OM_SAMPLING_TIME);
            addTimeObject(tf.getTime(), xb_binaryTempOp);
            break;
        default:
            // operator not implemented, throw error
            String error = "Temporal filter " + tf.getOperator().name() + " not implemented in " + "SosRequestEncoder";
            throw Util4Exceptions.createInvalidParameterValueException(Sos1Constants.GetObservationParams.eventTime.name(),
                                                                       error);
        }
        return xb_eventTime;
    }

    /**
     * Converts an ISosTime to a TimeInstant or TimePeriod on an BinaryTemporalOpType
     * 
     * @param sosTime
     *        ISosTime to convert (TimeInstant or TimePeriod)
     * @param xb_binaryTempOps
     *        BinaryTemporalOpType to add TimeInstant or TimePeriod to
     * @throws OwsExceptionReport
     */
    public static void addTimeObject(ITime sosTime, BinaryTemporalOpType xb_binaryTempOps) throws OwsExceptionReport {
        try {
            if (sosTime == null) {
                return;
            }
            else if (sosTime instanceof TimeInstant) {
                TimeInstant ti = (TimeInstant) sosTime;
                TimeInstantType xb_timeInstant = (TimeInstantType) xb_binaryTempOps.addNewTimeObject().substitute(QN_TIME_INSTANT,
                                                                                                                  TimeInstantType.type);
                String timeStr;
                if (ti.getIndeterminateValue() != null) {
                    timeStr = ti.getIndeterminateValue();
                }
                else {
                    timeStr = DateTimeHelper.formatDateTime2ResponseString(ti.getValue());
                }
                xb_timeInstant.addNewTimePosition().setStringValue(timeStr);
            }
            else if (sosTime instanceof TimePeriod) {
                TimePeriod tp = (TimePeriod) sosTime;
                TimePeriodType xb_timePeriod = (TimePeriodType) xb_binaryTempOps.addNewTimeObject().substitute(QN_TIME_PERIOD,
                                                                                                               TimePeriodType.type);
                String beginTimeStr;
                if (tp.getStartIndet() != null) {
                    beginTimeStr = tp.getStartIndet();
                }
                else {
                    beginTimeStr = DateTimeHelper.formatDateTime2ResponseString(tp.getStart());
                }

                String endTimeStr;
                if (tp.getEndIndet() != null) {
                    endTimeStr = tp.getEndIndet();
                }
                else {
                    endTimeStr = DateTimeHelper.formatDateTime2ResponseString(tp.getEnd());
                }

                xb_timePeriod.addNewBeginPosition().setStringValue(beginTimeStr);
                xb_timePeriod.addNewEndPosition().setStringValue(endTimeStr);
            }
        }
        catch (DateTimeException dte) {
            String exceptionText = "Error while creating time objects!";
            LOGGER.error(exceptionText, dte);
            throw Util4Exceptions.createNoApplicableCodeException(dte, exceptionText);
        }
    }

    public static BBOXType createBBOXTypeFromGeometry(Geometry geom, BBOXType xb_bbox) throws OwsExceptionReport {
        if (xb_bbox == null) {
            xb_bbox = BBOXType.Factory.newInstance();
        }

        MultiPoint mp = null;
        try {
            mp = (MultiPoint) geom;
        }
        catch (ClassCastException e) {
            String error = "BBOX geometry argument not a MultiPoint";
            throw Util4Exceptions.createInvalidParameterValueException(SosConstants.GetObservationParams.featureOfInterest.name(),
                                                                       error);
        }

        if (mp.getNumPoints() != 2) {
            String error = "BBOX geometry argument does not have two points";
            throw Util4Exceptions.createInvalidParameterValueException(SosConstants.GetObservationParams.featureOfInterest.name(),
                                                                       error);
        }

        int srid = mp.getSRID();
        EnvelopeType xb_env = xb_bbox.addNewEnvelope();
        xb_env.setSrsName(Configurator.getInstance().getSrsNamePrefix() + srid);
        xb_env.addNewLowerCorner().setStringValue(JTSHelper.getCoordinatesString((Point) mp.getGeometryN(0), srid));
        xb_env.addNewUpperCorner().setStringValue(JTSHelper.getCoordinatesString((Point) mp.getGeometryN(1), srid));

        return xb_bbox;
    }
}