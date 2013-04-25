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

import org.n52.sos.service.SoapHeader;


public class WsaHeader implements SoapHeader {

    private String toValue;

    private String actionValue;

    private String replyToAddress;

    private String messageID;

    /**
     * @return the toValue
     */
    public String getToValue() {
        return toValue;
    }

    /**
     * @param toValue
     *            the toValue to set
     */
    public void setToValue(String toValue) {
        this.toValue = toValue;
    }

    /**
     * @return the actionValue
     */
    public String getActionValue() {
        return actionValue;
    }

    /**
     * @param actionValue
     *            the actionValue to set
     */
    public void setActionValue(String actionValue) {
        this.actionValue = actionValue;
    }

    /**
     * @return the replyToAddress
     */
    public String getReplyToAddress() {
        return replyToAddress;
    }

    /**
     * @param replyToAddress
     *            the replyToAddress to set
     */
    public void setReplyToAddress(String replyToAddress) {
        this.replyToAddress = replyToAddress;
    }

    /**
     * @return the messageID
     */
    public String getMessageID() {
        return messageID;
    }

    /**
     * @param messageID
     *            the messageID to set
     */
    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

}
