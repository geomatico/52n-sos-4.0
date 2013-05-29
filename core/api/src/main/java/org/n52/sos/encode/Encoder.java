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
package org.n52.sos.encode;

import java.util.Map;
import java.util.Set;

import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.service.ConformanceClass;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.SchemaLocation;

/**
 * Generic interface for Encoders.
 *
 * @param <T> the resulting type, the "Target"
 * @param <S> the input type, the "Source"
 */
public interface Encoder<T, S> extends ConformanceClass {
    /**
     * @return List of supported encodings of this implementation (identified by {@link EncoderKey})
     */
    Set<EncoderKey> getEncoderKeyType();

    /**
     * Encodes the specified object.
     *
     * @param objectToEncode the object to encode
     *
     * @return the encoded object
     *
     * @throws OwsExceptionReport               if an error occurs
     * @throws UnsupportedEncoderInputException if the supplied object (or any of it's contents) is not supported by
     *                                          this encoder
     */
    T encode(S objectToEncode) throws OwsExceptionReport, UnsupportedEncoderInputException;

    /**
     * Encodes the specified object with the specified {@linkplain HelperValues}.
     *
     * @param objectToEncode the object to encode
     * @param additionalValues the helper values
     *
     * @return the encoded object
     *
     * @throws OwsExceptionReport               if an error occurs
     * @throws UnsupportedEncoderInputException if the supplied object (or any of it's contents) is not supported by
     *                                          this encoder
     */
    T encode(S objectToEncode, Map<HelperValues, String> additionalValues) throws OwsExceptionReport,
                                                                                         UnsupportedEncoderInputException;

    /**
     * Get the {@linkplain SupportedTypeKey}
     *
     * @return the supported key types
     */
    Map<SupportedTypeKey, Set<String>> getSupportedTypes();

    /**
     * Add the namespace prefix of this {@linkplain Encoder} instance to the given {@linkplain Map}.
     *
     * @param nameSpacePrefixMap
     */
    void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap);

    /**
     * @return the content type of the encoded response.
     */
    String getContentType();
    
    Set<SchemaLocation> getSchemaLocations();
}
