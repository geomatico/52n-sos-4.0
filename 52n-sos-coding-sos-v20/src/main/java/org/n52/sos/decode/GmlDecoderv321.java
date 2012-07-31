package org.n52.sos.decode;

import java.util.ArrayList;
import java.util.List;

import net.opengis.gml.x32.EnvelopeDocument;
import net.opengis.gml.x32.EnvelopeType;
import net.opengis.gml.x32.TimeInstantDocument;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.gml.x32.TimePeriodDocument;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.gml.x32.TimePositionType;

import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.JTSHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

public class GmlDecoderv321 implements IDecoder<Object, XmlObject> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GmlDecoderv321.class);

    private List<DecoderKeyType> decoderKeyTypes;

    public GmlDecoderv321() {
        decoderKeyTypes = new ArrayList<DecoderKeyType>();
        decoderKeyTypes.add(new DecoderKeyType(GMLConstants.NS_GML_32));
        StringBuilder builder = new StringBuilder();
        for (DecoderKeyType decoderKeyType : decoderKeyTypes) {
            builder.append(decoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        LOGGER.info("Decoder for the following namespaces initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<DecoderKeyType> getDecoderKeyTypes() {
        return decoderKeyTypes;
    }

    @Override
    public Object decode(XmlObject xmlObject) throws OwsExceptionReport {
        if (xmlObject instanceof EnvelopeDocument) {
            return getGeometry4BBOX((EnvelopeDocument) xmlObject);
        } else if (xmlObject instanceof TimeInstantType) {
            return parseTimeInstant((TimeInstantType) xmlObject);
        } else if (xmlObject instanceof TimePeriodType) {
            return parseTimePeriod((TimePeriodType) xmlObject);
        } else if (xmlObject instanceof TimeInstantDocument) {
            return parseTimeInstant(((TimeInstantDocument) xmlObject).getTimeInstant());
        } else if (xmlObject instanceof TimePeriodDocument) {
            return parseTimePeriod(((TimePeriodDocument) xmlObject).getTimePeriod());
        }
        return null;
    }

    /**
     * parses the BBOX element of the featureOfInterest element contained in the
     * GetObservation request and returns a String representing the BOX in
     * Well-Known-Text format
     * 
     * @param xb_bbox
     *            XmlBean representing the BBOX-element in the request
     * @return Returns WKT-String representing the BBOX as Multipoint with two
     *         elements
     * @throws OwsExceptionReport
     *             if parsing the BBOX element failed
     */
    private Geometry getGeometry4BBOX(EnvelopeDocument envelopeDocument) throws OwsExceptionReport {
        EnvelopeType envelopeType = envelopeDocument.getEnvelope();
        int srid =
                SosHelper.parseSrsName(envelopeType.getSrsName(), Configurator.getInstance()
                        .getSrsNamePrefixSosV2());
        String lowerCorner = envelopeType.getLowerCorner().getStringValue();
        String upperCorner = envelopeType.getUpperCorner().getStringValue();
        if (Configurator.getInstance().switchCoordinatesForEPSG(srid)) {
            lowerCorner = switchCoordinatesInString(lowerCorner);
            upperCorner = switchCoordinatesInString(upperCorner);
        }
        Geometry geom =
                JTSHelper
                        .createGeometryFromWKT(JTSHelper.createWKTPolygonFromEnvelope(lowerCorner, upperCorner));
        geom.setSRID(srid);
        return geom;
    }

    // /**
    // * Parses a XML time object to a SOS representation.
    // *
    // * @param xbTemporalOpsType
    // * XML time object
    // * @return SOS time object representation.
    // * @throws OwsExceptionReport
    // */
    // private ISosTime parseTime(TemporalOpsType xbTemporalOpsType) throws
    // OwsExceptionReport {
    // ISosTime sosTime = null;
    // XmlCursor timeCursor = xbTemporalOpsType.newCursor();
    // try {
    // if (timeCursor.toChild(GMLConstants.QN_TIME_INSTANT_32)) {
    // sosTime = parseTimeInstantNode(timeCursor.getDomNode());
    // } else if (timeCursor.toChild(GMLConstants.QN_TIME_PERIOD_32)) {
    // sosTime = parseTimePeriodNode(timeCursor.getDomNode());
    // } else {
    // OwsExceptionReport se = new OwsExceptionReport();
    // se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue,
    // "Time",
    // "The requested time type is not supported by this SOS!");
    // LOGGER.error("The requested time type is not supported by this SOS!");
    // throw se;
    // }
    // } catch (XmlException xmle) {
    // OwsExceptionReport se = new OwsExceptionReport();
    // se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue,
    // "Time",
    // "Error while parsing time: " + xmle.getMessage());
    // LOGGER.error(se.getMessage() + xmle.getMessage());
    // throw se;
    // }
    // timeCursor.dispose();
    // return sosTime;
    // }

    /**
     * switches the order of coordinates contained in a string, e.g. from String
     * '3.5 4.4' to '4.4 3.5'
     * 
     * NOTE: ACTUALLY checks, whether dimension is 2D, othewise throws
     * Exception!!
     * 
     * @param coordsString
     *            contains coordinates, which should be switched
     * @return Returns String contained coordinates in switched order
     * @throws OwsExceptionReport
     */
    private String switchCoordinatesInString(String coordsString) throws OwsExceptionReport {
        String switchedCoordString = null;
        String[] coordsArray = coordsString.split(" ");
        if (coordsArray.length != 2) {
            String exceptionText =
                    "An error occurred, while switching coordinates. Only a pair with two coordinates are supported!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        } else {
            switchedCoordString = coordsArray[1] + " " + coordsArray[0];
        }
        return switchedCoordString;
    }

    /**
     * parses TimeInstant
     * 
     * @param tp
     *            XmlBean representation of TimeInstant
     * @return Returns a TimeInstant created from the TimeInstantType
     * @throws java.text.ParseException
     * @throws java.text.ParseException
     *             if parsing the datestring into java.util.Date failed
     * @throws OwsExceptionReport
     */
    private TimeInstant parseTimeInstant(TimeInstantType xbTimeIntant) throws OwsExceptionReport {
        TimeInstant ti = new TimeInstant();
        TimePositionType xbTimePositionType = xbTimeIntant.getTimePosition();
        String timeString = xbTimePositionType.getStringValue();
        if (timeString != null && !timeString.equals("")) {
            ti.setValue(DateTimeHelper.parseIsoString2DateTime(timeString));
            ti.setRequestedTimeLength(timeString.length());
        }
        if (xbTimePositionType.getIndeterminatePosition() != null) {
            ti.setIndeterminateValue(xbTimePositionType.getIndeterminatePosition().toString());
            ti.setRequestedTimeLength(xbTimePositionType.getIndeterminatePosition().toString().length());
        }
        return ti;
    }

    /**
     * creates SOS representation of time period from XMLBeans representation of
     * time period
     * 
     * @param xb_timePeriod
     *            XMLBeans representation of time period
     * @return Returns SOS representation of time period
     * @throws OwsExceptionReport
     */
    private TimePeriod parseTimePeriod(TimePeriodType xbTimePeriod) throws OwsExceptionReport {

        // begin position
        TimePositionType xbBeginTPT = xbTimePeriod.getBeginPosition();
        DateTime begin = null;
        if (xbBeginTPT != null) {
            String beginString = xbBeginTPT.getStringValue();
            begin = DateTimeHelper.parseIsoString2DateTime(beginString);
        } else {
            String exceptionText = "gml:TimePeriod! must contain beginPos Element with valid ISO:8601 String!!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }

        // end position
        DateTime end = null;
        TimePositionType xbEndTPT = xbTimePeriod.getEndPosition();
        if (xbEndTPT != null) {
            String endString = xbEndTPT.getStringValue();
            end =
                    DateTimeHelper.setDateTime2EndOfDay4RequestedEndPosition(
                            DateTimeHelper.parseIsoString2DateTime(endString), endString.length());
        } else {
            String exceptionText = "gml:TimePeriod! must contain endPos Element with valid ISO:8601 String!!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }

        return new TimePeriod(begin, end);
    }

    // /**
    // * Parses a XML time object to a SOS representation.
    // *
    // * @param xbTemporalOpsType
    // * XML time object
    // * @return SOS time object representation.
    // * @throws OwsExceptionReport
    // */
    // private ISosTime parseTime(TemporalOpsType xbTemporalOpsType) throws
    // OwsExceptionReport {
    // ISosTime sosTime = null;
    // XmlCursor timeCursor = xbTemporalOpsType.newCursor();
    // try {
    // if (timeCursor.toChild(GMLConstants.QN_TIME_INSTANT_32)) {
    // sosTime = parseTimeInstantNode(timeCursor.getDomNode());
    // } else if (timeCursor.toChild(GMLConstants.QN_TIME_PERIOD_32)) {
    // sosTime = parseTimePeriodNode(timeCursor.getDomNode());
    // } else {
    // OwsExceptionReport se = new OwsExceptionReport();
    // se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue,
    // "Time",
    // "The requested time type is not supported by this SOS!");
    // LOGGER.error("The requested time type is not supported by this SOS!");
    // throw se;
    // }
    // } catch (XmlException xmle) {
    // OwsExceptionReport se = new OwsExceptionReport();
    // se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue,
    // "Time",
    // "Error while parsing time: " + xmle.getMessage());
    // LOGGER.error(se.getMessage() + xmle.getMessage());
    // throw se;
    // }
    // timeCursor.dispose();
    // return sosTime;
    // }
    //
    // /**
    // * help method, which creates an TimeInstant object from the DOM-node of
    // the
    // * TimeInstantType. This constructor is necessary cause XMLBeans does not
    // * full support substitution groups. So one has to do a workaround with
    // * XmlCursor and the DomNodes of the elements.
    // *
    // * @param timeInstant
    // * DOM Node of timeInstant element
    // * @return Returns a TimeInstant created from the DOM-Node
    // * @throws OwsExceptionReport
    // * if no timePosition element is cotained in the timeInstant
    // * element
    // * @throws XmlException
    // * if parsing the DomNode to an XMLBeans XmlObject failed
    // */
    // private TimeInstant parseTimeInstantNode(Node timeInstant) throws
    // OwsExceptionReport, XmlException {
    //
    // TimeInstant ti = new TimeInstant();
    //
    // TimeInstantDocument xbTimeInstantDocument =
    // TimeInstantDocument.Factory.parse(timeInstant);
    // TimeInstantType xbTimeInstant = xbTimeInstantDocument.getTimeInstant();
    //
    // if (xbTimeInstant.getTimePosition() != null) {
    // try {
    // String positionString = xbTimeInstant.getTimePosition().getStringValue();
    // if (positionString != null && !positionString.equals("")) {
    // if (positionString.equals(SosConstants.FirstLatest.latest.name())
    // || positionString.equals(SosConstants.FirstLatest.getFirst.name())) {
    // ti.setIndeterminateValue(positionString);
    // ti.setRequestedTimeLength(positionString.length());
    // } else {
    // ti.setValue(SosDateTimeUtilities.parseIsoString2DateTime(positionString));
    // ti.setRequestedTimeLength(positionString.length());
    // }
    // }
    //
    // // if intdeterminateTime attribute is set, set string value
    // if (xbTimeInstant.getTimePosition().getIndeterminatePosition() != null) {
    // ti.setIndeterminateValue(xbTimeInstant.getTimePosition().getIndeterminatePosition().toString());
    // }
    // if (!(ti.getIndeterminateValue() != null &&
    // !ti.getIndeterminateValue().isEmpty())
    // && ti.getValue() == null) {
    // OwsExceptionReport se = new
    // OwsExceptionReport(ExceptionLevel.DetailedExceptions);
    // se.addCodedException(ExceptionCode.MissingParameterValue,
    // "gml:timePosition",
    // "No IndeterminateValue attribute and gml:timePosition value ist null or empty!");
    // throw se;
    // }
    // } catch (Exception e) {
    // OwsExceptionReport se = new
    // OwsExceptionReport(ExceptionLevel.DetailedExceptions);
    // LOGGER.error("Error while parse time String to DateTime!", e);
    // se.addCodedException(null, null,
    // "Error while parse time String to DateTime!");
    // throw se;
    // }
    // } else {
    // OwsExceptionReport se = new
    // OwsExceptionReport(ExceptionLevel.DetailedExceptions);
    // se.addCodedException(ExceptionCode.MissingParameterValue,
    // "gml:timePosition",
    // "No timePosition element is contained in the gml:timeInstant element");
    // throw se;
    // }
    // return ti;
    // }
    //
    // /**
    // * parse methode, which creates an TimePeriod object from the DOM-node of
    // * the TimePeriodType. This constructor is necessary cause XMLBeans does
    // not
    // * fully support substitution groups. So one has to do a workaround with
    // * XmlCursor and the DomNodes of the elements.
    // *
    // *
    // * @param timePeriod
    // * the DomNode of the timePeriod element
    // * @return Returns a TimePeriod created from the DOM-Node
    // * @throws XmlException
    // * if the Node could not be parsed into a XmlBean
    // * @throws OwsExceptionReport
    // * if required elements of the timePeriod are missed
    // */
    // private TimePeriod parseTimePeriodNode(Node timePeriod) throws
    // XmlException, OwsExceptionReport {
    //
    // TimePeriod tp = new TimePeriod();
    //
    // TimePeriodDocument xbTimePeriodDocument =
    // TimePeriodDocument.Factory.parse(timePeriod);
    // TimePeriodType xbTimePeriod = xbTimePeriodDocument.getTimePeriod();
    //
    // if (xbTimePeriod.getBegin() != null) {
    // // TODO:
    //
    // } else if (xbTimePeriod.getBeginPosition() != null) {
    // String startString = xbTimePeriod.getBeginPosition().getStringValue();
    // if (startString.equals("")) {
    // tp.setStart(null);
    // } else {
    // try {
    // tp.setStart(SosDateTimeUtilities.parseIsoString2DateTime(startString));
    // } catch (Exception e) {
    // OwsExceptionReport se = new
    // OwsExceptionReport(ExceptionLevel.DetailedExceptions);
    // LOGGER.error("Error while parse time String to DateTime!", e);
    // se.addCodedException(null, null,
    // "Error while parse time String to DateTime!");
    // throw se;
    // }
    // }
    // } else {
    // OwsExceptionReport se = new OwsExceptionReport();
    // se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue,
    // GetObservationParams.eventTime.toString(),
    // "The start time is missed in the timePeriod element of time parameter!");
    // throw se;
    // }
    //
    // if (xbTimePeriod.getEnd() != null) {
    // // TODO:
    // } else if (xbTimePeriod.getEndPosition() != null) {
    // String endString = xbTimePeriod.getEndPosition().getStringValue();
    // if (endString.equals("")) {
    // tp.setEnd(null);
    // } else {
    // try {
    // tp.setEnd(SosDateTimeUtilities.setDateTime2EndOfDay4RequestedEndPosition(
    // SosDateTimeUtilities.parseIsoString2DateTime(endString),
    // endString.length()));
    // } catch (Exception e) {
    // OwsExceptionReport se = new
    // OwsExceptionReport(ExceptionLevel.DetailedExceptions);
    // LOGGER.error("Error while parse time String to DateTime!", e);
    // se.addCodedException(null, null,
    // "Error while parse time String to DateTime!");
    // throw se;
    // }
    // }
    //
    // if (xbTimePeriod.getEndPosition().getIndeterminatePosition() != null) {
    // tp.setEndIndet(xbTimePeriod.getEndPosition().getIndeterminatePosition().toString());
    // }
    //
    // }
    //
    // // else no endPosition -> throw exception!!
    // else {
    // OwsExceptionReport se = new OwsExceptionReport();
    // se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue,
    // GetObservationParams.eventTime.toString(),
    // "The end time is missed in the timePeriod element of time parameter!");
    // throw se;
    // }
    //
    // if (xbTimePeriod.getDuration() != null) {
    // // TODO: JODA TIME
    // tp.setDuration(SosDateTimeUtilities.parseDuration(xbTimePeriod.getDuration().toString()));
    // }
    //
    // return tp;
    // }

}
