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
package org.n52.sos.decode;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import net.opengis.gml.CodeType;
import net.opengis.gml.DirectPositionType;
import net.opengis.gml.EnvelopeDocument;
import net.opengis.gml.EnvelopeType;
import net.opengis.gml.TimeInstantDocument;
import net.opengis.gml.TimeInstantType;
import net.opengis.gml.TimePeriodDocument;
import net.opengis.gml.TimePeriodType;
import net.opengis.gml.TimePositionType;

import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.sos.decode.IDecoder;

import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants.FirstLatest;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.DateTimeException;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.JTSHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;


public class GmlDecoderv311 implements IDecoder<Object, XmlObject> {

	private static final Logger LOGGER = LoggerFactory.getLogger(GmlDecoderv311.class);

    private Set<DecoderKey> DECODER_KEYS = CodingHelper.decoderKeysForElements(
    		GMLConstants.NS_GML,
    		EnvelopeDocument.class,
            TimeInstantType.class,
            TimePeriodType.class,
            TimeInstantDocument.class,
            TimePeriodDocument.class,
            CodeType.class
            );

    private static final String CS = ",";
    private static final String DECIMAL = ".";
    private static final String TS = " ";

    public GmlDecoderv311() {
        LOGGER.debug("Decoder for the following keys initialized successfully: {}!", StringHelper.join(", ", DECODER_KEYS));
    }

    @Override
    public Set<DecoderKey> getDecoderKeyTypes() {
        return Collections.unmodifiableSet(DECODER_KEYS);
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
        if (xmlObject instanceof CodeType) {
            return parseCodeType((CodeType)xmlObject);
        }

        return null;
    }

	private Object getGeometry4BBOX(EnvelopeDocument xb_bbox) throws OwsExceptionReport {
		Geometry result = null;

        EnvelopeType xb_envelope = xb_bbox.getEnvelope();

        // parse srid; if not set, throw exception!
        int srid = SosHelper.parseSrsName(xb_envelope.getSrsName());

        DirectPositionType xb_lowerCorner = xb_envelope.getLowerCorner();
        DirectPositionType xb_upperCorner = xb_envelope.getUpperCorner();

        String lower = xb_lowerCorner.getStringValue();
        String upper = xb_upperCorner.getStringValue();

        String geomWKT = null;

        // FIXME dont know how to handle
        if (Configurator.getInstance().reversedAxisOrderRequired(srid)) {
            lower = JTSHelper.switchCoordinatesInString(lower);
            upper = JTSHelper.switchCoordinatesInString(upper);
        }
        
        geomWKT = "MULTIPOINT(" + lower + ", " + upper + ")";

        result = JTSHelper.createGeometryFromWKT(geomWKT).getEnvelope();
        result.setSRID(srid);
        return result;
	}

	private Object parseTimePeriod(TimePeriodType xbTimePeriod) throws OwsExceptionReport {
		try {
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
        } catch (DateTimeException dte) {
            String exceptionText = "Error while parsing TimePeriod!";
            LOGGER.error(exceptionText, dte);
            throw Util4Exceptions.createNoApplicableCodeException(dte, exceptionText);
        }
	}

	private Object parseTimeInstant(TimeInstantType xbTimeIntant) throws OwsExceptionReport {
		TimeInstant ti = new TimeInstant();
        TimePositionType xbTimePositionType = xbTimeIntant.getTimePosition();
        String timeString = xbTimePositionType.getStringValue();
        if (timeString != null && !timeString.equals("")) {
            if ((FirstLatest.contains(timeString))) {
                ti.setIndeterminateValue(timeString);
            } else {
                try {
                    ti.setValue(DateTimeHelper.parseIsoString2DateTime(timeString));
                    
                } catch (DateTimeException dte) {
                    String exceptionText = "Error while parsing TimeInstant!";
                    LOGGER.error(exceptionText, dte);
                    throw Util4Exceptions.createNoApplicableCodeException(dte, exceptionText);
                }
                ti.setRequestedTimeLength(timeString.length());
            }
        }
        if (xbTimePositionType.getIndeterminatePosition() != null) {
            ti.setIndeterminateValue(xbTimePositionType.getIndeterminatePosition().toString());
        }
        return ti;
	}
	
	private org.n52.sos.ogc.gml.CodeType parseCodeType(CodeType element) {
        org.n52.sos.ogc.gml.CodeType codeType = new org.n52.sos.ogc.gml.CodeType(element.getStringValue());
        if (element.isSetCodeSpace()) {
            codeType.setCodeSpace(element.getCodeSpace());
        }
        return codeType;
    }

}
