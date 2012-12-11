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
package org.n52.sos.ogc.sos;

import java.util.List;
import java.util.logging.Level;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.decode.IDecoder;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.swe.encoding.SosSweAbstractEncoding;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosResultEncoding {
    
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosResultEncoding.class);

    private String xml;

    private SosSweAbstractEncoding encoding;
    
    public SosResultEncoding() {
    }

    public SosResultEncoding(String resultEncoding) throws OwsExceptionReport {
        this.xml = resultEncoding;
        encoding = parseResultEncoding();
    }

    public String getXml() {
        if (encoding != null) {
            return encoding.getXml();
        }
        return xml;
    }

    public void setEncoding(SosSweAbstractEncoding encoding) {
        this.encoding = encoding;
    }

    public SosSweAbstractEncoding getEncoding() throws OwsExceptionReport {
        if (encoding == null && xml != null && !xml.isEmpty()) {
            encoding = parseResultEncoding();
        }
       return encoding;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }
    
    private SosSweAbstractEncoding parseResultEncoding() throws OwsExceptionReport {
        try {
            Object decodedObject = decodeXmlToObject(XmlObject.Factory.parse(xml));
            if (decodedObject != null && decodedObject instanceof SosSweAbstractEncoding) {
                SosSweAbstractEncoding sosSweEncoding = (SosSweAbstractEncoding) decodedObject;
                return sosSweEncoding;
            } else {
                StringBuilder exceptionText = new StringBuilder();
                exceptionText.append("Error while parsing result encoding!");
                LOGGER.debug(exceptionText.toString());
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText.toString());
            }
        } catch (XmlException xmle) {
            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("Error while parsing result encoding!");
            LOGGER.debug(exceptionText.toString());
            throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText.toString());
        }
    }
    
    private Object decodeXmlToObject(XmlObject xmlObject) throws OwsExceptionReport {
        List<IDecoder> decoderList = Configurator.getInstance().getDecoder(XmlHelper.getNamespace(xmlObject));
        if (decoderList != null) {
            for (IDecoder decoder : decoderList) {
                return decoder.decode(xmlObject);
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SosResultEncoding other = (SosResultEncoding) obj;
        try {
            if (this.getEncoding() != other.getEncoding() && (this.getEncoding() == null 
                    || !this.getEncoding().equals(other.getEncoding()))) {
                return false;
            }
        } catch (OwsExceptionReport ex) {
            return false;
        }
        return true;
    }

}
