/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.soap;

import java.util.Map;

import org.apache.xmlbeans.XmlObject;

public class SoapRequest {

    private String soapNamespace;

    private String soapVersion;

    private SoapFault soapFault;

    private XmlObject soapBodyContent;

    private String soapAction;

    private Map<String, SoapHeader> soapHeader;

    public SoapRequest(String soapNamespace, String soapVersion) {
        this.soapNamespace = soapNamespace;
        this.soapVersion = soapVersion;
    }

    /**
     * @return the soapNamespace
     */
    public String getSoapNamespace() {
        return soapNamespace;
    }

    /**
     * @param soapNamespace
     *            the soapNamespace to set
     */
    public void setSoapNamespace(String soapNamespace) {
        this.soapNamespace = soapNamespace;
    }

    /**
     * @return the soapVersion
     */
    public String getSoapVersion() {
        return soapVersion;
    }

    /**
     * @param soapVersion
     *            the soapVersion to set
     */
    public void setSoapVersion(String soapVersion) {
        this.soapVersion = soapVersion;
    }

    public void setSoapFault(SoapFault fault) {
        this.soapFault = fault;

    }

    public SoapFault getSoapFault() {
        return soapFault;
    }

    public XmlObject getSoapBodyContent() {
        return soapBodyContent;
    }

    public void setSoapBodyContent(XmlObject soapBodyContent) {
        this.soapBodyContent = soapBodyContent;

    }

    public void setAction(String soapAction) {
        this.soapAction = soapAction;

    }

    public void setSoapHeader(Map<String, SoapHeader> soapHeader) {
        this.soapHeader = soapHeader;
    }

    /**
     * @return the soapAction
     */
    public String getSoapAction() {
        return soapAction;
    }

    /**
     * @return the soapHeader
     */
    public Map<String, SoapHeader> getSoapHeader() {
        return soapHeader;
    }

}
