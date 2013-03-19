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
package org.n52.sos.exception.ows;

import java.io.IOException;
import org.n52.sos.decode.DecoderKey;
import org.n52.sos.encode.EncoderKey;
import org.n52.sos.ogc.ows.OwsExceptionReport;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class NoApplicableCodeException extends CodedOwsException {
    private static final long serialVersionUID = 1L;

    public NoApplicableCodeException() {
        super(OwsExceptionCode.NoApplicableCode);
    }

    public static class NotYetSupportedException extends NoApplicableCodeException {
        private static final long serialVersionUID = 1L;

        public OwsExceptionReport forFeature(String feature) {
            return withMessage("%s is not yet supported", feature);
        }
    }

    public static class NoEncoderForResponseException extends NoApplicableCodeException {
        private static final long serialVersionUID = 1L;

        public NoEncoderForResponseException() {
            withMessage("Error while getting encoder for response!");
        }
    }

    public static class NoEncoderForKeyException extends NoApplicableCodeException {
        private static final long serialVersionUID = 1L;

        public NoEncoderForKeyException(EncoderKey key) {
            withMessage("Could not find encoder for key '%s'.", key.toString());
        }
    }

    public static class NoDecoderForKeyException extends NoApplicableCodeException {
        private static final long serialVersionUID = 1L;

        public NoDecoderForKeyException(DecoderKey k) {
            withMessage("No decoder implementation is available for KvpBinding (%s)!", k);
        }
    }

    public static class MethodNotSupportedException extends NoApplicableCodeException {
        private static final long serialVersionUID = 1L;

        public MethodNotSupportedException(String binding, String method) {
            withMessage("HTTP %s is no supported for %s binding!", method, binding);
        }
    }

    public static class EncoderResponseUnsupportedException extends NoApplicableCodeException {
        private static final long serialVersionUID = 1L;

        public EncoderResponseUnsupportedException() {
            withMessage("The encoder response is not supported!");
        }
    }

    public static class ErrorWhileSavingResponseToOutputStreamException extends NoApplicableCodeException {
        private static final long serialVersionUID = 1L;

        public ErrorWhileSavingResponseToOutputStreamException(IOException ioe) {
            withMessage("Error occurs while saving response to output stream!").causedBy(ioe);
        }
    }
}
