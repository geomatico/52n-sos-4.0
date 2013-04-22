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
package org.n52.sos.wsa;

import javax.xml.namespace.QName;

/**
 * Constants for WS-Addressing
 *
 */
public interface WsaConstants {
    /**
     * WSA fault action URI
     */
    String WSA_FAULT_ACTION = "http://www.w3.org/2005/08/addressing/fault";
    /**
     * WSA namespace
     */
    String NS_WSA = "http://www.w3.org/2005/08/addressing";
    /**
     * WSA prefix
     */
    String NS_WSA_PREFIX = "wsa";
    /**
     * WSA to element
     */
    String EN_TO = "To";
    /**
     * WSA action element
     */
    String EN_ACTION = "Action";
    /**
     * WSA replyTo element
     */
    String EN_REPLY_TO = "ReplyTo";
    /**
     * WSA address element
     */
    String EN_ADDRESS = "Address";
    /**
     * WSA messageID element
     */
    String EN_MESSAGE_ID = "MessageID";
    /**
     * WSA relatesTo element
     */
    String EN_RELATES_TO = "RelatesTo";
    QName QN_TO = new QName(NS_WSA, EN_TO, NS_WSA_PREFIX);
    QName QN_ACTION = new QName(NS_WSA, EN_ACTION, NS_WSA_PREFIX);
    QName QN_REPLY_TO = new QName(NS_WSA, EN_REPLY_TO, NS_WSA_PREFIX);
    QName QN_ADDRESS = new QName(NS_WSA, EN_ADDRESS, NS_WSA_PREFIX);
    QName QN_MESSAGE_ID = new QName(NS_WSA, EN_MESSAGE_ID, NS_WSA_PREFIX);
    QName QN_RELATES_TO = new QName(NS_WSA, EN_RELATES_TO, NS_WSA_PREFIX);
}