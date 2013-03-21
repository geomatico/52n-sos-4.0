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
package org.n52.sos.decode;

import java.util.Map;
import java.util.Set;

import org.n52.sos.exception.ows.concrete.UnsupportedDecoderInputException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.ConformanceClass;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;

/**
 * Generic interface for decoders.
 *
 * @param <T> the result of the decoding process, the "Target"
 * @param <S> the input which is decoded, the "Source"
 */
public interface Decoder<T, S> extends ConformanceClass {
    /**
     * @return List encodings this implementation (identified by {@link DecoderKeyType}) is able to decode
     */
    public Set<DecoderKey> getDecoderKeyTypes();

    /**
     * Decode a object to another representation.
     *
     * @param objectToDecode the object to encode
     *
     * @return the encoded object
     *
     * @throws OwsExceptionReport               if an error occurs
     * @throws UnsupportedDecoderInputException if the supplied type (or any of it's contents) is not supported by this
     *                                          decoder
     */
    public T decode(S objectToDecode) throws OwsExceptionReport, UnsupportedDecoderInputException;

    /**
     * Get the {@linkplain SupportedTypeKey}
     *
     * @return the supported key types
     */
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes();
}