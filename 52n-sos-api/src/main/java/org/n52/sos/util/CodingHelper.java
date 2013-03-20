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
package org.n52.sos.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.decode.Decoder;
import org.n52.sos.decode.DecoderKey;
import org.n52.sos.decode.OperationDecoderKey;
import org.n52.sos.decode.XmlNamespaceDecoderKey;
import org.n52.sos.decode.XmlOperationDecoderKey;
import org.n52.sos.encode.Encoder;
import org.n52.sos.encode.EncoderKey;
import org.n52.sos.encode.XmlEncoderKey;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.NoApplicableCodeException.DecoderResponseUnsupportedException;
import org.n52.sos.exception.ows.NoApplicableCodeException.EncoderResponseUnsupportedException;
import org.n52.sos.exception.ows.NoApplicableCodeException.NoDecoderForKeyException;
import org.n52.sos.exception.ows.NoApplicableCodeException.XmlDecodingException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class CodingHelper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CodingHelper.class);
    
    public static Object decodeXmlElement(XmlObject x) throws OwsExceptionReport {
        return decodeXmlObject(x);
    }

    public static <T> XmlObject encodeObjectToXml(String namespace, T o,
                                                  Map<SosConstants.HelperValues, String> helperValues) throws
            OwsExceptionReport {
        Encoder<XmlObject, T> encoder = getEncoder(namespace, o);
        XmlObject encodedObject = encoder.encode(o, helperValues);
        if (encodedObject == null) {
            throw new NoApplicableCodeException()
                    .withMessage("Encoding of type \"%s\" using namespace key \"%s\" failed", o.getClass(), namespace);
        }
        return encodedObject;
    }
    
    public static <T> Encoder<XmlObject, T> getEncoder(String namespace, T o) throws OwsExceptionReport {
        EncoderKey key = getEncoderKey(namespace, o);
        Encoder<XmlObject, T> encoder = Configurator.getInstance().getCodingRepository().getEncoder(key);
        if (encoder == null) {
            throw new NoApplicableCodeException()
                    .withMessage("No encoder found for key \"%s\".", key);
        }
        return encoder;
    }

    public static XmlObject encodeObjectToXml(String namespace, Object o) throws OwsExceptionReport {
        return encodeObjectToXml(namespace, o, CollectionHelper.<SosConstants.HelperValues, String>map());
    }

    public static Set<DecoderKey> decoderKeysForElements(String namespace, Class<?>... elements) {
        HashSet<DecoderKey> keys = new HashSet<DecoderKey>(elements.length);
        for (Class<?> x : elements) {
            keys.add(new XmlNamespaceDecoderKey(namespace, x));
        }
        return keys;
    }
    
     public static Set<DecoderKey> xmlDecoderKeysForOperation(String service, String version, Enum<?>... operations) {
        HashSet<DecoderKey> set = new HashSet<DecoderKey>(operations.length);
        for (Enum<?> o : operations) {
            set.add(new XmlOperationDecoderKey(service, version, o.name()));
        }
        return set;
    }
     
     public static Set<DecoderKey> xmlDecoderKeysForOperation(String service, String version, String... operations) {
        HashSet<DecoderKey> set = new HashSet<DecoderKey>(operations.length);
        for (String o : operations) {
            set.add(new XmlOperationDecoderKey(service, version, o));
        }
        return set;
    }

    @Deprecated
    public static boolean hasXmlEncoderForOperation(OperationDecoderKey k) {
        return Configurator.getInstance().getCodingRepository().getDecoder(new XmlOperationDecoderKey(k)) != null;
    }

    public static Set<EncoderKey> encoderKeysForElements(String namespace, Class<?>... elements) {
        HashSet<EncoderKey> keys = new HashSet<EncoderKey>(elements.length);
        for (Class<?> x : elements) {
            keys.add(new XmlEncoderKey(namespace, x));
        }
        return keys;
    }

    public static EncoderKey getEncoderKey(String namespace, Object o) {
        return new XmlEncoderKey(namespace, o.getClass());
    }

    public static DecoderKey getDecoderKey(XmlObject doc) {
        return new XmlNamespaceDecoderKey(XmlHelper.getNamespace(doc), doc.getClass());
    }

    public static <T extends XmlObject> DecoderKey getDecoderKey(T[] doc) {
        return new XmlNamespaceDecoderKey(XmlHelper.getNamespace(doc[0]), doc.getClass());
    }

    public static Object decodeXmlObject(XmlObject xbObject) throws OwsExceptionReport {
        DecoderKey key = getDecoderKey(xbObject);
        Decoder<?, XmlObject> decoder = Configurator.getInstance().getCodingRepository().getDecoder(key);
        if (decoder == null) {
            throw new NoDecoderForKeyException(key);
        }
        Object decodedObject = decoder.decode(xbObject);
        if (decodedObject == null) {
            throw new DecoderResponseUnsupportedException(xbObject.xmlText(), decodedObject);
        }
        return decodedObject;
    }

    public static Object decodeXmlObject(String xmlString) throws OwsExceptionReport {
        try {
            return decodeXmlObject(XmlObject.Factory.parse(xmlString));
        } catch (XmlException e) {
            throw new XmlDecodingException("XML string", xmlString, e);
        }
    }

    private CodingHelper() {
    }
}
