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
package org.n52.sos.decode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.ogc.BBOXType;
import net.opengis.ogc.BinarySpatialOpType;
import net.opengis.ogc.BinaryTemporalOpType;
import net.opengis.ogc.PropertyNameDocument;
import net.opengis.ogc.PropertyNameType;
import net.opengis.ogc.TemporalOperatorType;
import net.opengis.ogc.impl.BBOXTypeImpl;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.filter.FilterConstants;
import org.n52.sos.ogc.filter.SpatialFilter;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.filter.FilterConstants.TimeOperator;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import com.vividsolutions.jts.geom.Geometry;

public class OgcDecoderv100 implements IDecoder<Object, XmlObject> {

	private static final Logger LOGGER = LoggerFactory.getLogger(OgcDecoderv100.class);

    private Set<DecoderKey> DECODER_KEYS = CodingHelper.decoderKeysForElements(OGCConstants.NS_OGC);

    public OgcDecoderv100() {
        LOGGER.debug("Decoder for the following keys initialized successfully: {}!", StringHelper.join(", ", DECODER_KEYS));
        }

    @Override
    public Set<DecoderKey> getDecoderKeyTypes() {
        return Collections.unmodifiableSet(DECODER_KEYS);
    }
    
	@Override
	public Set<String> getConformanceClasses() {
		return new HashSet<String>(0);
	}

	@Override
	public Object decode(XmlObject xmlObject) throws OwsExceptionReport {

		if (xmlObject instanceof BinaryTemporalOpType) {
            return parseTemporalOperatorType((BinaryTemporalOpType) xmlObject);
        } 
		if (xmlObject instanceof TemporalOperatorType) {
			String exceptionText = "The requested temporal filter operand is not supported by this SOS!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(
                    Sos1Constants.GetObservationParams.eventTime.name(), exceptionText);
		}
		// add propertyNameDoc here 
		if (xmlObject instanceof PropertyNameDocument) {
			PropertyNameDocument xb_propDoc = ((PropertyNameDocument) xmlObject);
			return xb_propDoc.getPropertyName();
        }
		// add BBOXType here		
		if (xmlObject instanceof BinarySpatialOpType) {
            return parseSpatialOperatorType((BinarySpatialOpType) xmlObject);
        }
		if (xmlObject instanceof BBOXType) {
            return parseBBOXFilterType((BBOXTypeImpl) xmlObject);
        }
		if (xmlObject instanceof BBOXTypeImpl) {
            return parseBBOXFilterType((BBOXTypeImpl) xmlObject);
        } 
        return null;
	}

	@Override
	public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
		return new HashMap<SupportedTypeKey, Set<String>>(0);
	}
	
	/**
     * parses a single temporal filter of the requests and returns SOS temporal
     * filter
     * 
     * @param xbTemporalOpsType
     *            XmlObject representing the temporal filter
     * @return Returns SOS representation of temporal filter
     * @throws OwsExceptionReport
     *             if parsing of the element failed
     */
	private Object parseTemporalOperatorType(BinaryTemporalOpType xb_btot) throws OwsExceptionReport {

		TemporalFilter temporalFilter = new TemporalFilter();
		// FIXME local workaround against SOSHelper check value reference
		String valueRef = "phenomenonTime";
		try {
			
			NodeList nodes = xb_btot.getDomNode().getChildNodes();
	        for (int i = 0; i < nodes.getLength(); i++) {

	            if (nodes.item(i).getNamespaceURI() != null
	                    && !nodes.item(i).getLocalName().equals(FilterConstants.EN_VALUE_REFERENCE)) {
	            	// GML decoder will return TimeInstant or TimePriod
	                Object timeObject = CodingHelper.decodeXmlElement(XmlObject.Factory.parse(nodes.item(i)));
	                
	                if (timeObject != null && timeObject instanceof PropertyNameType) {
	                	PropertyNameType propType = (PropertyNameType) timeObject;

	                	// TODO here apply logic for ogc property om:samplingTime etc
	                	// valueRef = propType.getDomNode().getNodeValue();
	                	
	                }
	                
	                if (timeObject != null && timeObject instanceof ITime) {
	                    TimeOperator operator;
	                    ITime time = (ITime) timeObject;
	                    String localName = XmlHelper.getLocalName(xb_btot);
	                    // change to SOS 1.0. TMDuring kind of
	                    if (localName.equals(TimeOperator.TM_During.name()) && time instanceof TimePeriod) {
	                        operator = TimeOperator.TM_During;
	                    } else if (localName.equals(TimeOperator.TM_Equals.name()) && time instanceof TimeInstant) {
	                        operator = TimeOperator.TM_Equals;
	                    } else if (localName.equals(TimeOperator.TM_After.name()) && time instanceof TimeInstant) {
	                        operator = TimeOperator.TM_After;
	                    } else if (localName.equals(TimeOperator.TM_Before.name()) && time instanceof TimeInstant) {
	                        operator = TimeOperator.TM_Before;
	                    } else {
	                        String exceptionText =
	                                "The requested temporal filter operand is not supported by this SOS!";
	                        LOGGER.debug(exceptionText);
	                        throw Util4Exceptions.createInvalidParameterValueException(
	                                Sos1Constants.GetObservationParams.eventTime.name(), exceptionText);
	                    }
	                    temporalFilter.setOperator(operator);
	                    temporalFilter.setTime(time);
	                    // actually it should be eg om:samplingTime 
	                    temporalFilter.setValueReference(valueRef);
	                    break;
	                }
	            }
	        }
            
        } catch (XmlException xmle) {
            String exceptionText = "Error while parsing temporal filter!";
            LOGGER.error(exceptionText, xmle);
            throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
        }
        return temporalFilter;
        
	}
	
    /**
     * Parses the spatial filter of a request.
     * 
     * @param xbSpatialOpsType
     *            XmlBean representing the feature of interest parameter of the
     *            request
     * @return Returns SpatialFilter created from the passed foi request
     *         parameter
     * @throws OwsExceptionReport
     *             if creation of the SpatialFilter failed
     */
    private SpatialFilter parseBBOXFilterType(BBOXTypeImpl xbBBOX) throws OwsExceptionReport {
        
    	SpatialFilter spatialFilter = new SpatialFilter();
    	// FIXME local workaround for SOSHelper check value reference 
    	String valueRef = "om:featureOfInterest/sams:SF_SpatialSamplingFeature/sams:shape";
        try {
            
            spatialFilter.setOperator(FilterConstants.SpatialOperator.BBOX);
            XmlCursor geometryCursor = xbBBOX.newCursor();
            if (geometryCursor.toChild(GMLConstants.QN_ENVELOPE)) {
                Object sosGeometry = CodingHelper.decodeXmlElement(XmlObject.Factory.parse(geometryCursor.getDomNode()));
                
                if (sosGeometry != null && sosGeometry instanceof PropertyNameType) {
                	PropertyNameType propType = (PropertyNameType) sosGeometry;

                	// TODO here apply logic for ogc property urn:ogc:data:location etc
                	// valueRef = propType.getDomNode().getNodeValue();
                	
                }
                
                if (sosGeometry != null && sosGeometry instanceof Geometry) {
                    spatialFilter.setGeometry((Geometry) sosGeometry);
                    spatialFilter.setValueReference(valueRef);
                }

            } else {
                String exceptionText = "The requested spatial filter operand is not supported by this SOS!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(
                        "FeatureOfInterest Filter", exceptionText);
            }
            geometryCursor.dispose();
            
        } catch (XmlException xmle) {
            String exceptionText = "Error while parsing spatial filter!";
            LOGGER.error(exceptionText, xmle);
            throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
        }
        return spatialFilter;
    }
    
    private Object parseSpatialOperatorType(BinarySpatialOpType xbSpatialOpsType) throws OwsExceptionReport {
    	SpatialFilter spatialFilter = new SpatialFilter();
    	try {
			if (xbSpatialOpsType instanceof BBOXTypeImpl) {
		        spatialFilter.setOperator(FilterConstants.SpatialOperator.BBOX);
		        BBOXTypeImpl xbBBOX = (BBOXTypeImpl) xbSpatialOpsType;
	            spatialFilter.setOperator(FilterConstants.SpatialOperator.BBOX);
	            XmlCursor geometryCursor = xbBBOX.newCursor();
	            if (geometryCursor.toChild(GMLConstants.QN_ENVELOPE)) {
	                Object sosGeometry = CodingHelper.decodeXmlElement(XmlObject.Factory.parse(geometryCursor.getDomNode()));
	                if (sosGeometry != null && sosGeometry instanceof Geometry) {
	                    spatialFilter.setGeometry((Geometry) sosGeometry);
	                }
	
	            } else {
	                String exceptionText = "The requested spatial filter operand is not supported by this SOS!";
	                LOGGER.debug(exceptionText);
	                throw Util4Exceptions.createInvalidParameterValueException(
	                        Sos2Constants.GetObservationParams.spatialFilter.name(), exceptionText);
	            }
	            geometryCursor.dispose();
			} else {
                String exceptionText = "The requested spatial filter is not supported by this SOS!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(
                        "GetFeatureOfInterest Filter", exceptionText);           
			}
        } catch (XmlException xmle) {
            String exceptionText = "Error while parsing spatial filter!";
            LOGGER.error(exceptionText, xmle);
            throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
        }
        return spatialFilter;
	}
}
