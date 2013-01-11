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
package org.n52.sos.util;

import java.util.List;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.decode.IDecoder;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecoderHelper {
    
    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(DecoderHelper.class);
    
    public static Object decodeXmlElement(XmlObject element) throws OwsExceptionReport {
        
        String namespace = XmlHelper.getNamespace(element);
        List<IDecoder> decoderList = Configurator.getInstance().getDecoder(namespace);
        if (decoderList != null) {
            for (IDecoder decoder : decoderList) {
                Object decodedObject = decoder.decode(element);
                if (decodedObject != null) {
                    return decodedObject;
                }
            }
        }
        String errorMsg = String.format("No decoder found for namespace \"%s\".", namespace);
        LOGGER.error(errorMsg);
        throw Util4Exceptions.createNoApplicableCodeException(null, errorMsg);
    }

}
