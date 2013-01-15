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

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import net.opengis.swe.x101.AbstractDataComponentType;
import net.opengis.swe.x101.CategoryDocument.Category;
import net.opengis.swe.x101.CountDocument.Count;
import net.opengis.swe.x101.ObservablePropertyDocument.ObservableProperty;
import net.opengis.swe.x101.QuantityDocument.Quantity;
import net.opengis.swe.x101.QuantityRangeDocument.QuantityRange;
import net.opengis.swe.x101.TextDocument.Text;
import net.opengis.swe.x101.TimeDocument.Time;
import net.opengis.swe.x101.TimeRangeDocument.TimeRange;
import net.opengis.swe.x101.VectorType.Coordinate;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.simpleType.SosSweBoolean;
import org.n52.sos.ogc.swe.simpleType.SosSweCategory;
import org.n52.sos.ogc.swe.simpleType.SosSweCount;
import org.n52.sos.ogc.swe.simpleType.SosSweObservableProperty;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantityRange;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.ogc.swe.simpleType.SosSweTime;
import org.n52.sos.ogc.swe.simpleType.SosSweTimeRange;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.StringHelper;
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

    private Set<EncoderKey> ENCODER_KEYS = CodingHelper.encoderKeysForElements(SWEConstants.NS_SWE,
            SosSweQuantity.class, SosSweText.class, SosSweCoordinate.class);
    
    
    public SweCommonEncoderv101() {
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
        nameSpacePrefixMap.put(SWEConstants.NS_SWE, SWEConstants.NS_SWE_PREFIX);
    }
    
    @Override
    public String getContentType() {
        return SosConstants.CONTENT_TYPE_XML;
    }
    
    @Override
    public XmlObject encode(Object element) throws OwsExceptionReport {
        return encode(element, null);
    }

    @Override
    public XmlObject encode(Object element, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        if (element instanceof SosSweBoolean) {
            return createBoolean((SosSweBoolean) element);
        } else if (element instanceof SosSweCategory) {
            return createCategory((SosSweCategory) element);
        } else if (element instanceof SosSweCount) {
            return createCount((SosSweCount) element);
        } else if (element instanceof SosSweObservableProperty) {
            return createObservableProperty((SosSweObservableProperty) element);
        } else if (element instanceof SosSweQuantity) {
            return createQuantity((SosSweQuantity) element);
        } else if (element instanceof SosSweQuantityRange) {
            return createQuantityRange((SosSweQuantityRange) element);
        } else if (element instanceof SosSweText) {
            return createText((SosSweText) element);
        } else if (element instanceof SosSweTime) {
            return createTime((SosSweTime) element);
        } else if (element instanceof SosSweTimeRange) {
            return createTimeRange((SosSweTimeRange) element);
        } else if (element instanceof SosSweCoordinate) {
            return addValuesToCoordinate((SosSweCoordinate) element);
        }
        return null;
    }
    
    private net.opengis.swe.x101.BooleanDocument.Boolean createBoolean(SosSweBoolean bool) {
        net.opengis.swe.x101.BooleanDocument.Boolean xbBoolean = net.opengis.swe.x101.BooleanDocument.Boolean.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbBoolean, bool);
        if (bool.isSetValue()) {
            xbBoolean.setValue(bool.getValue().booleanValue());
        }
        if (xbBoolean.isSetQuality()) {
            // TODO
        }
        return xbBoolean;
    }
    
    private Category createCategory(SosSweCategory category) {
        Category xbCategory = Category.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbCategory, category);
        if (category.isSetValue()) {
            xbCategory.setValue(category.getValue());
        }
        if (category.isSetUom()) {
            xbCategory.addNewCodeSpace().setHref(category.getUom());
        }
        if (category.isSetQuality()) {
            // TODO
        }
        return xbCategory;
    }

    private Count createCount(SosSweCount count) {
        Count xbCount = Count.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbCount, count);
        if (count.isSetValue()) {
            xbCount.setValue(new BigInteger(Integer.toString(count.getValue().intValue())));
        }
        if (count.isSetQuality()) {
            // TODO
        }
        return xbCount;
    }

    private ObservableProperty createObservableProperty(SosSweObservableProperty observableProperty) {
        ObservableProperty xbObservableProperty = ObservableProperty.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbObservableProperty, observableProperty);
        if (observableProperty.isSetQuality()) {
            // TODO
        }
        return xbObservableProperty;
    }

    /**
     * Adds values to SWE quantity
     * 
     * @param quantity
     *            SOS internal representation
     */
    private Quantity createQuantity(SosSweQuantity quantity) {
        Quantity xbQuantity = Quantity.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbQuantity, quantity);
        if (quantity.isSetAxisID()) {
            xbQuantity.setAxisID(quantity.getDescription());
        }
        if (quantity.isSetValue()) {
            xbQuantity.setValue(Double.valueOf(quantity.getValue()));
        }
        if (quantity.isSetUom()) {
            xbQuantity.addNewUom().setCode(quantity.getUom());
        }
        if (quantity.isSetQuality()) {
            // TODO
        }
        return xbQuantity;
    }

    private QuantityRange createQuantityRange(SosSweQuantityRange quantityRange) {
        QuantityRange xbQuantityRange = QuantityRange.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbQuantityRange, quantityRange);
        if (quantityRange.isSetAxisID()) {
            xbQuantityRange.setAxisID(quantityRange.getDescription());
        }
        if (quantityRange.isSetValue()) {
            xbQuantityRange.setValue(quantityRange.getValue().getRangeAsList());
        }
        if (quantityRange.isSetUom()) {
            xbQuantityRange.addNewUom().setCode(quantityRange.getUom());
        }
        if (quantityRange.isSetQuality()) {
            // TODO
        }
        return xbQuantityRange;
    }

    /**
     * Adds values to SWE text
     * 
     * @param text
     *            SOS internal representation
     */
    private Text createText(SosSweText text) {
        Text xbText = Text.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbText, text);
        if (text.isSetValue()) {
            xbText.setValue(text.getValue());
        }
        if (text.isSetQuality()) {
            // TODO
        }
        return xbText;
    }
    
    private Time createTime(SosSweTime time) {
        Time xbTime = Time.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbTime, time);
        if (time.isSetValue()) {
            xbTime.setValue(time.getValue());
        }
        if (time.isSetUom()) {
            xbTime.addNewUom().setCode(time.getUom());
        }
        if (time.isSetQuality()) {
            // TODO
        }
        return xbTime;
    }
    
    private TimeRange createTimeRange(SosSweTimeRange timeRange) {
        TimeRange xbTimeRange = TimeRange.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbTimeRange, timeRange);
        if (timeRange.isSetValue()) {
            xbTimeRange.setValue(timeRange.getValue().getRangeAsStringList());
        }
        if (timeRange.isSetUom()) {
            xbTimeRange.addNewUom().setCode(timeRange.getUom());
        }
        if (timeRange.isSetQuality()) {
            // TODO
        }
        return xbTimeRange;
    }
    
    private void addAbstractDataComponentValues(AbstractDataComponentType xbComponent, SosSweAbstractDataComponent component) {
        if (component.isSetDefinition()) {
            xbComponent.setDefinition(component.getDefinition());
        }
        if (component.isSetDescription()) {
            xbComponent.addNewDescription().setStringValue(component.getDescription());
        }
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
        xbCoordinate.setQuantity(createQuantity((SosSweQuantity) coordinate.getValue()));
        return xbCoordinate;
    }

}
