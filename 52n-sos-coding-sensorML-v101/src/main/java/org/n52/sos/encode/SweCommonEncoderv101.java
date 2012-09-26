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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.swe.x101.QuantityDocument.Quantity;
import net.opengis.swe.x101.TextDocument.Text;
import net.opengis.swe.x101.VectorType.Coordinate;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encoder class for SWE Common 1.0.1
 */
public class SweCommonEncoderv101 implements IEncoder<XmlObject, Object> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SweCommonEncoderv101.class);

    private List<EncoderKeyType> encoderKeyTypes;
    
    private Map<SupportedTypeKey, Set<String>> supportedTypes;
    
    private Set<String> conformanceClasses;
    
    public SweCommonEncoderv101() {
        encoderKeyTypes = new ArrayList<EncoderKeyType>();
        encoderKeyTypes.add(new EncoderKeyType(SWEConstants.NS_SWE));
        StringBuilder builder = new StringBuilder();
        for (EncoderKeyType encoderKeyType : encoderKeyTypes) {
            builder.append(encoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        supportedTypes = new HashMap<SupportedTypeKey, Set<String>>(0);
        conformanceClasses = new HashSet<String>(0);
        LOGGER.info("Encoder for the following keys initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<EncoderKeyType> getEncoderKeyType() {
        return encoderKeyTypes;
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return supportedTypes;
    }

    @Override
    public Set<String> getConformanceClasses() {
        return conformanceClasses;
    }
    
    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(SWEConstants.NS_SWE, SWEConstants.NS_SWE_PREFIX);
    }
    
    @Override
    public String getContentType() {
        return "text/xml";
    }
    
    @Override
    public XmlObject encode(Object element) throws OwsExceptionReport {
        return encode(element, null);
    }

    @Override
    public XmlObject encode(Object element, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        if (element instanceof SosSweQuantity) {
            return addValuesToSimpleTypeQuantity((SosSweQuantity) element);
        } else if (element instanceof SosSweText) {
            return addValuesToSimpleTypeText((SosSweText) element);
        } else if (element instanceof SosSweCoordinate) {
            return addValuesToCoordinate((SosSweCoordinate) element);
        }
        return null;
    }
    
    /**
     * Adds values to SWE text
     * 
     * @param text
     *            SOS internal representation
     */
    private Text addValuesToSimpleTypeText(SosSweText text) {
        Text xbText = Text.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (text.getDefinition() != null && !text.getDefinition().isEmpty()) {
            xbText.setDefinition(text.getDefinition());
        }
        if (text.getDescription() != null && !text.getDescription().isEmpty()) {
            xbText.addNewDescription().setStringValue(text.getDescription());
        }
        if (text.getValue() != null && !text.getValue().isEmpty()) {
            xbText.setValue(text.getValue());
        }
        return xbText;
    }

    /**
     * Adds values to SWE quantity
     * 
     * @param quantity
     *            SOS internal representation
     */
    private Quantity addValuesToSimpleTypeQuantity(SosSweQuantity quantity) {
        Quantity xbQuantity = Quantity.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (quantity.getDefinition() != null && !quantity.getDefinition().isEmpty()) {
            xbQuantity.setDefinition(quantity.getDefinition());
        }
        if (quantity.getDescription() != null && !quantity.getDescription().isEmpty()) {
            xbQuantity.addNewDescription().setStringValue(quantity.getDescription());
        }
        if (quantity.getAxisID() != null && !quantity.getAxisID().isEmpty()) {
            xbQuantity.setAxisID(quantity.getDescription());
        }
        if (quantity.getValue() != null && !quantity.getValue().isEmpty()) {
            xbQuantity.setValue(Double.valueOf(quantity.getValue()));
        }
        if (quantity.getUom() != null && !quantity.getUom().isEmpty()) {
            xbQuantity.addNewUom().setCode(quantity.getUom());
        }
        if (quantity.getQuality() != null) {
            // TODO
        }
        return xbQuantity;
    }

    /**
     * Adds values to SWE coordinates
     * 
     * @param coordinate
     *            SOS internal representation
     */
    private Coordinate addValuesToCoordinate(SosSweCoordinate coordinate) {
        Coordinate xbCoordinate = Coordinate.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        xbCoordinate.setName(coordinate.getName().name());
        xbCoordinate.setQuantity(addValuesToSimpleTypeQuantity((SosSweQuantity) coordinate.getValue()));
        return xbCoordinate;
    }

}
