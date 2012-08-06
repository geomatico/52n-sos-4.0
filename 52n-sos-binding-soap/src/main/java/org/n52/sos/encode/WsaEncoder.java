package org.n52.sos.encode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;

import org.apache.log4j.Logger;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.soap.SoapHeader;
import org.n52.sos.soap.SoapResponse;
import org.n52.sos.wsa.WsaConstants;
import org.n52.sos.wsa.WsaHeader;
import org.w3c.dom.DOMException;

public class WsaEncoder implements IEncoder<Map<QName, String>, WsaHeader> {

    /** the logger, used to log exceptions and additonaly information */
    private static Logger LOGGER = Logger.getLogger(WsaEncoder.class);

    private List<EncoderKeyType> encoderKeyTypes;

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
        LOGGER.info("Encoder for the following key initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<EncoderKeyType> getEncoderKeyType() {
        return encoderKeyTypes;
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
