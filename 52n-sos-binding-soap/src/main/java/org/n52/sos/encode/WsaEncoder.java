package org.n52.sos.encode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.wsa.WsaConstants;
import org.n52.sos.wsa.WsaHeader;

public class WsaEncoder implements IEncoder<Map<QName, String>, WsaHeader> {

    /** the logger, used to log exceptions and additonaly information */
    private static Logger LOGGER = Logger.getLogger(WsaEncoder.class);

    private List<EncoderKeyType> encoderKeyTypes;
    
    private Map<SupportedTypeKey, Set<String>> supportedTypes;
    
    private Set<String> conformanceClasses;
    
    /**
     * constructor
     */
    public WsaEncoder() {
        super();
        encoderKeyTypes = new ArrayList<EncoderKeyType>();
        encoderKeyTypes.add(new EncoderKeyType(WsaConstants.NS_WSA));
        StringBuilder builder = new StringBuilder();
        for (EncoderKeyType encoderKeyType : encoderKeyTypes) {
            builder.append(encoderKeyType);
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        supportedTypes = new HashMap<SupportedTypeKey, Set<String>>(0);
        conformanceClasses = new HashSet<String>(0);
        LOGGER.info("Encoder for the following key initialized successfully: " + builder.toString() + "!");
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
    }
    
    
    @Override
    public String getContentType() {
        return "text/xml";
    }

    @Override
    public Map<QName, String> encode(WsaHeader response) throws OwsExceptionReport {
        return encode(response, null);
    }

    @Override
    public Map<QName, String> encode(WsaHeader response, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        Map<QName, String> wsaHeaderValues = new HashMap<QName, String>();
        if (response.getReplyToAddress() != null && !response.getReplyToAddress().isEmpty()) {
            wsaHeaderValues.put(WsaConstants.getQNameTo(), response.getReplyToAddress());
        }
        if (response.getMessageID() != null) {
            wsaHeaderValues.put(WsaConstants.getQNameRelatesTo(), response.getMessageID());
        }
        if (response.getActionValue() != null) {
            wsaHeaderValues.put(WsaConstants.getQNameAction(), response.getActionValue());
        }
        return wsaHeaderValues;
    }

}
