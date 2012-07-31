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

import org.n52.sos.response.IServiceResponse;

public class SoapResponse {
    
    private String soapNamespace;
    
    private String soapVersion;
    
    private String soapAction;
   
    private SoapFault soapFault;
    
    private IServiceResponse bodyContent;
    
    private Map<String, SoapHeader> header;

    public SoapResponse() {
        // TODO Auto-generated constructor stub
    }
    /**
     * @return the soapNamespace
     */
    public String getSoapNamespace() {
        return soapNamespace;
    }

    /**
     * @param soapNamespace the soapNamespace to set
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
     * @param soapVersion the soapVersion to set
     */
    public void setSoapVersion(String soapVersion) {
        this.soapVersion = soapVersion;
    }

    public void setSoapFault(SoapFault soapFault) {
        this.soapFault = soapFault;
    }

    public SoapFault getSoapFault() {
        return soapFault;
    }

    public IServiceResponse getSoapBodyContent() {
        return bodyContent;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    public String getSoapAction() {
        return soapAction;
    }
    public void setSoapBodyContent(IServiceResponse bodyContent) {
        this.bodyContent = bodyContent;
    }

    public void setHeader(Map<String, SoapHeader> map) {
        this.header = map;
    }
    public Map<String, SoapHeader> getHeader() {
        return header;
    }

}
