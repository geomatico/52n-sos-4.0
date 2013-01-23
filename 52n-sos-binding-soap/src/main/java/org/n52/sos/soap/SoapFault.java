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
package org.n52.sos.soap;

import java.util.Locale;

import javax.xml.namespace.QName;

public class SoapFault {

    private QName faultCode;

    private QName faultSubcode;

    private String faultReason;

    private Locale locale;

    private String detailText;

    public void setFaultCode(QName faultCode) {
        this.faultCode = faultCode;
    }

    public void setFaultReason(String faultReason) {
        this.faultReason = faultReason;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setDetailText(String detailText) {
        this.detailText = detailText;
    }

    public QName getFaultCode() {
        return faultCode;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getFaultReason() {
        return faultReason;
    }

    public String getDetailText() {
        return detailText;
    }

    public void setFaultSubcode(QName faultSubcode) {
        this.faultSubcode = faultSubcode;
    }

    /**
     * @return the faultSubcode
     */
    public QName getFaultSubcode() {
        return faultSubcode;
    }

}
