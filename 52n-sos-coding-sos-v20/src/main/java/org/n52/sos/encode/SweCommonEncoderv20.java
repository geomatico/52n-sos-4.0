package org.n52.sos.encode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.swe.x20.AbstractDataComponentType;
import net.opengis.swe.x20.QuantityType;
import net.opengis.swe.x20.TextType;
import net.opengis.swe.x20.TimeType;
import net.opengis.swe.x20.VectorType.Coordinate;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.simpleType.ISosSweSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.ogc.swe.simpleType.SosSweTime;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SweCommonEncoderv20 implements IEncoder<XmlObject, Object> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SweCommonEncoderv20.class);

    private List<EncoderKeyType> encoderKeyTypes;

    public SweCommonEncoderv20() {
        encoderKeyTypes = new ArrayList<EncoderKeyType>();
        encoderKeyTypes.add(new EncoderKeyType(SWEConstants.NS_SWE_20));
        StringBuilder builder = new StringBuilder();
        for (EncoderKeyType encoderKeyType : encoderKeyTypes) {
            builder.append(encoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        LOGGER.info("Encoder for the following keys initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<EncoderKeyType> getEncoderKeyType() {
        return encoderKeyTypes;
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return new HashMap<SupportedTypeKey, Set<String>>(0);
    }

    @Override
    public Set<String> getConformanceClasses() {
        Set<String> conformanceClasses = new HashSet<String>(0);
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/core");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/uml-simple-components");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/uml-record-components");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/uml-block-components");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/uml-simple-encodings");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/xsd-simple-components");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/xsd-record-components");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/xsd-block-components");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/xsd-simple-encodings");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/general-encoding-rules");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/text-encoding-rules");
        return conformanceClasses;
    }
    
    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(SWEConstants.NS_SWE_20, SWEConstants.NS_SWE_PREFIX);
    }

    @Override
    public XmlObject encode(Object element) throws OwsExceptionReport {
        if (element instanceof SosSweQuantity) {
            return addValuesToSimpleTypeQuantity((SosSweQuantity) element);
        } else if (element instanceof SosSweText) {
            return addValuesToSimpleTypeText((SosSweText) element);
        } else if (element instanceof SosSweTime) {
            return addValuesToSimpleTypeTime((SosSweTime) element);
        } else if (element instanceof SosSweCoordinate) {
            return addValuesToCoordinate((SosSweCoordinate) element);
        } else if (element instanceof ISosSweSimpleType) {
            return addSweSimpleTypToField((ISosSweSimpleType) element);
        }
        return null;
    }

    @Override
    public XmlObject encode(Object response, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        return null;
    }

    /**
     * @param element
     * @return
     * @throws OwsExceptionReport
     */
    private AbstractDataComponentType addSweSimpleTypToField(ISosSweSimpleType element) throws OwsExceptionReport {
        if (element instanceof SosSweQuantity) {
            return addValuesToSimpleTypeQuantity((SosSweQuantity) element);
        } else if (element instanceof SosSweText) {
            return addValuesToSimpleTypeText((SosSweText) element);
        } else if (element instanceof SosSweTime) {
            return addValuesToSimpleTypeTime((SosSweTime) element);
        } else {
            // TODO: NOT SUPPORTED EXCEPTION
            throw new OwsExceptionReport();
        }
    }

    /**
     * Adds values to SWE quantity
     * 
     * @param quantity
     *            SOS internal representation
     */
    private QuantityType addValuesToSimpleTypeQuantity(SosSweQuantity quantity) {
        QuantityType xbQuantity = QuantityType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (quantity.getDefinition() != null && !quantity.getDefinition().isEmpty()) {
            xbQuantity.setDefinition(quantity.getDefinition());
        }
        if (quantity.getDescription() != null && !quantity.getDescription().isEmpty()) {
            xbQuantity.setDescription(quantity.getDescription());
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
     * Adds values to SWE text
     * 
     * @param text
     *            SOS internal representation
     */
    private TextType addValuesToSimpleTypeText(SosSweText text) {
        TextType xbText = TextType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (text.getDefinition() != null && !text.getDefinition().isEmpty()) {
            xbText.setDefinition(text.getDefinition());
        }
        if (text.getDescription() != null && !text.getDescription().isEmpty()) {
            xbText.setDescription(text.getDescription());
        }
        if (text.getValue() != null && !text.getValue().isEmpty()) {
            xbText.setValue(text.getValue());
        }
        return xbText;
    }

    private TimeType addValuesToSimpleTypeTime(SosSweTime time) {
        TimeType xbTime = TimeType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (time.getDefinition() != null && !time.getDefinition().isEmpty()) {
            xbTime.setDefinition(time.getDefinition());
        }
        if (time.getDescription() != null && !time.getDescription().isEmpty()) {
            xbTime.setDescription(time.getDescription());
        }
        if (time.getValue() != null && !time.getValue().isEmpty()) {
            xbTime.setValue(time.getValue());
        }
        if (time.getUom() != null && !time.getUom().isEmpty()) {
            xbTime.addNewUom().setHref(time.getUom());
        }
        if (time.getQuality() != null) {
            // TODO
        }
        return xbTime;
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
