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

import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.ogc.swe.encoding.SosSweAbstractEncoding;
import org.n52.sos.ogc.swe.encoding.SosSweTextEncoding;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlHelper;

public class ResultTemplate {

    private CodeWithAuthority identifier;

    private String xmlResultStructure;

    private String xmResultEncoding;

    private SosSweAbstractDataComponent resultStructure;

    private SosSweAbstractEncoding resultEncoding;

    public CodeWithAuthority getIdentifier() {
        return identifier;
    }

    public String getXmlResultStructure() {
        return xmlResultStructure;
    }

    public String getXmResultEncoding() {
        return xmResultEncoding;
    }

    public SosSweAbstractDataComponent getResultStructure() throws OwsExceptionReport {
        if (resultStructure == null) {
            this.resultStructure = parseResultStructure();
        }
        return resultStructure;
    }

    public SosSweAbstractEncoding getResultEncoding() throws OwsExceptionReport {
        if (resultEncoding == null) {
            this.resultEncoding = parseResultEncoding();
        }
        return resultEncoding;
    }

    public void setIdentifier(CodeWithAuthority identifier) {
        this.identifier = identifier;
    }

    public void setXmlResultStructure(String xmlResultStructure) {
        this.xmlResultStructure = xmlResultStructure;
    }

    public void setXmResultEncoding(String xmResultEncoding) {
        this.xmResultEncoding = xmResultEncoding;
    }

    public void setResultStructure(SosSweAbstractDataComponent resultStructure) {
        this.resultStructure = resultStructure;
    }

    public void setResultEncoding(SosSweAbstractEncoding resultEncoding) {
        this.resultEncoding = resultEncoding;
    }

    private SosSweAbstractDataComponent parseResultStructure() throws OwsExceptionReport {
        Object decodedObject = CodingHelper.decodeXmlObject(xmlResultStructure);
        if (decodedObject instanceof SosSweDataRecord) {
            return (SosSweDataRecord) decodedObject;
        }
        String errorMsg =
                String.format("Decoding of string \"%s\" failed. Returned type is \"%s\".", resultStructure,
                        decodedObject.getClass().getName());
        throw Util4Exceptions.createNoApplicableCodeException(null, errorMsg);
    }

    private SosSweAbstractEncoding parseResultEncoding() throws OwsExceptionReport {
        Object decodedObject = CodingHelper.decodeXmlObject(xmResultEncoding);
        if (decodedObject instanceof SosSweTextEncoding) {
            return (SosSweTextEncoding) decodedObject;
        }
        String errorMsg =
                String.format("Decoding of string \"%s\" failed. Returned type is \"%s\".", resultEncoding,
                        decodedObject.getClass().getName());
        throw Util4Exceptions.createNoApplicableCodeException(null, errorMsg);
    }
}
