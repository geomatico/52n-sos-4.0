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
package org.n52.sos.ogc.sos;

import org.n52.sos.exception.ows.concrete.DecoderResponseUnsupportedException;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.swe.SweAbstractDataComponent;
import org.n52.sos.ogc.swe.SweDataRecord;
import org.n52.sos.ogc.swe.encoding.SweAbstractEncoding;
import org.n52.sos.ogc.swe.encoding.SweTextEncoding;
import org.n52.sos.util.CodingHelper;

public class SosResultTemplate {

    private CodeWithAuthority identifier;

    private String xmlResultStructure;

    private String xmResultEncoding;

    private SweAbstractDataComponent resultStructure;

    private SweAbstractEncoding resultEncoding;

    public CodeWithAuthority getIdentifier() {
        return identifier;
    }

    public String getXmlResultStructure() {
        return xmlResultStructure;
    }

    public String getXmResultEncoding() {
        return xmResultEncoding;
    }

    public SweAbstractDataComponent getResultStructure() throws OwsExceptionReport {
        if (resultStructure == null) {
            this.resultStructure = parseResultStructure();
        }
        return resultStructure;
    }

    public SweAbstractEncoding getResultEncoding() throws OwsExceptionReport {
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

    public void setResultStructure(SweAbstractDataComponent resultStructure) {
        this.resultStructure = resultStructure;
    }

    public void setResultEncoding(SweAbstractEncoding resultEncoding) {
        this.resultEncoding = resultEncoding;
    }

    private SweAbstractDataComponent parseResultStructure() throws OwsExceptionReport {
        Object decodedObject = CodingHelper.decodeXmlObject(xmlResultStructure);
        if (decodedObject instanceof SweDataRecord) {
            return (SweDataRecord) decodedObject;
        }
        throw new DecoderResponseUnsupportedException(xmlResultStructure, decodedObject);
    }

    private SweAbstractEncoding parseResultEncoding() throws OwsExceptionReport {
        Object decodedObject = CodingHelper.decodeXmlObject(xmResultEncoding);
        if (decodedObject instanceof SweTextEncoding) {
            return (SweTextEncoding) decodedObject;
        }
        throw new DecoderResponseUnsupportedException(xmResultEncoding, decodedObject);
    }
}
