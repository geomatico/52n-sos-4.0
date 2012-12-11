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
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosResultStructure {
    
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosResultStructure.class);
    
    private SosSweAbstractDataComponent resultStructure;
    
    private String xml;

    public SosResultStructure() {
    }
    
    public SosResultStructure(String resultStructure) throws OwsExceptionReport {
        this.xml = resultStructure;
        this.resultStructure = parseResultStructure();
    }

    public String getXml() {
        if (resultStructure != null) {
            return resultStructure.getXml();
        }
        return xml;
    }

    public void setResultStructure(SosSweAbstractDataComponent resultStructure) {
        this.resultStructure = resultStructure;
    }

    public SosSweAbstractDataComponent getResultStructure() throws OwsExceptionReport {
        if (resultStructure == null && xml != null && !xml.isEmpty()) {
           resultStructure = parseResultStructure();
        }
       return resultStructure;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }
    
    private SosSweAbstractDataComponent parseResultStructure() throws OwsExceptionReport {
        try {
            Object decodedObject = decodeXmlToObject(XmlObject.Factory.parse(xml));
            if (decodedObject != null && decodedObject instanceof SosSweAbstractDataComponent) {
                SosSweAbstractDataComponent sosSweData = (SosSweAbstractDataComponent) decodedObject;
                return sosSweData;
            } else {
                StringBuilder exceptionText = new StringBuilder();
                exceptionText.append("Error while parsing result structure!");
                LOGGER.debug(exceptionText.toString());
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText.toString());
            }
        } catch (XmlException xmle) {
            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("Error while parsing result structure!");
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
    public boolean equals(Object o) {
        if (o != null && o instanceof SosResultStructure) {
            SosResultStructure other = (SosResultStructure) o;
            try {
                if (getResultStructure() == other.getResultStructure()) {
                    return true;
                } else if (getResultStructure() != null) {
                    return getResultStructure().equals(other.getResultStructure());
                }
            } catch (OwsExceptionReport ex) {
                return false;
            }
        }
        return false;
    }
}
