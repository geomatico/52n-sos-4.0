package org.n52.sos.wsa;

import javax.xml.namespace.QName;

/**
 * Constants for WS-Addressing
 * 
 */
public class WsaConstants {

    /**
     * WSA fault action URI
     */
    public static final String WSA_FAULT_ACTION = "http://www.w3.org/2005/08/addressing/fault";

    /**
     * WSA namespace
     */
    public static final String NS_WSA = "http://www.w3.org/2005/08/addressing";

    /**
     * WSA prefix
     */
    public static final String NS_WSA_PREFIX = "wsa";

    /**
     * WSA to element
     */
    public static final String EN_TO = "To";

    /**
     * WSA action element
     */
    public static final String EN_ACTION = "Action";

    /**
     * WSA replyTo element
     */
    public static final String EN_REPLYTO = "ReplyTo";

    /**
     * WSA address element
     */
    public static final String EN_ADDRESS = "Address";

    /**
     * WSA messageID element
     */
    public static final String EN_MESSAGEID = "MessageID";

    /**
     * WSA relatesTo element
     */
    public static final String EN_RELATESTO = "RelatesTo";

    /**
     * Get QName for To element
     * 
     * @return
     */
    public static QName getQNameTo() {
        return new QName(NS_WSA, EN_TO, NS_WSA_PREFIX);
    }

    /**
     * Get QName for Action element
     * 
     * @return
     */
    public static QName getQNameAction() {
        return new QName(NS_WSA, EN_ACTION, NS_WSA_PREFIX);
    }

    /**
     * Get QName for ReplyTo element
     * 
     * @return
     */
    public static QName getQNameReplyTo() {
        return new QName(NS_WSA, EN_REPLYTO, NS_WSA_PREFIX);
    }

    /**
     * Get QName for Address element
     * 
     * @return
     */
    public static QName getQNameAddress() {
        return new QName(NS_WSA, EN_ADDRESS, NS_WSA_PREFIX);
    }

    /**
     * Get QName for MessageID element
     * 
     * @return
     */
    public static QName getQNameMessageID() {
        return new QName(NS_WSA, EN_MESSAGEID, NS_WSA_PREFIX);
    }

    /**
     * Get QName for RelatesTo element
     * 
     * @return
     */
    public static QName getQNameRelatesTo() {
        return new QName(NS_WSA, EN_RELATESTO, NS_WSA_PREFIX);
    }
}