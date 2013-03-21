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

package org.n52.sos.exception.ows.concrete;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.decode.Decoder;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.w3c.dom.Node;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class UnsupportedDecoderInputException extends NoApplicableCodeException {
    private static final long serialVersionUID = 5561451567407304739L;

    public UnsupportedDecoderInputException(Decoder<?, ?> decoder, Object o) {
        if (o == null) {
            withMessage("Decoder %s can not decode 'null'", decoder.getClass().getSimpleName());
        } else {
            String name;
            if (o instanceof XmlObject) {
                Node n = ((XmlObject) o).getDomNode();
                name = n.getPrefix() != null ? n.getPrefix() + ":" + n.getLocalName() : n.getLocalName();
            } else {
                name = o.getClass().getName();
            }
            withMessage("%s can not be decoded by %s", name, decoder.getClass().getName());
        }
    }
}
