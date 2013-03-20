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

import java.util.List;

import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.MissingParameterValueException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.OperationNotSupportedException;
import org.n52.sos.exception.ows.OptionNotSupportedException;
import org.n52.sos.exception.ows.VersionNegotiationFailedException;
import org.n52.sos.exception.sos.InvalidPropertyOfferingCombinationException;
import org.n52.sos.exception.sos.ResponseExceedsSizeLimitException;
import org.n52.sos.exception.swes.InvalidRequestException;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OwsExceptionReport;

/**
 * class offers util methods for Exceptions used in this SOS
 */
public class Util4Exceptions {
    @Deprecated
    public static OwsExceptionReport createMissingMandatoryParameterException(String parameterName) {
        return createMissingParameterValueException(parameterName);
    }

    @Deprecated
    public static OwsExceptionReport createMissingParameterValueException(String parameterName) {
        return new MissingParameterValueException(parameterName);
    }

    @Deprecated
    public static OwsExceptionReport createResponseExceedsSizeLimitException(int responseSize, int responseLimit) {
        return new ResponseExceedsSizeLimitException().forLimit(responseSize, responseLimit);
    }

    @Deprecated
    public static OwsExceptionReport createResponseExceedsSizeLimitException(String message) {
        return new ResponseExceedsSizeLimitException().withMessage(message);
    }

    @Deprecated
    public static OwsExceptionReport createOperationNotSupportedException(String operationName) {
        return new OperationNotSupportedException(operationName);
    }

    @Deprecated
    public static OwsExceptionReport createNoApplicableCodeException(Exception exception, String message) {
        return new NoApplicableCodeException().withMessage(message).causedBy(exception);
    }

    @Deprecated
    public static OwsExceptionReport createInvalidParameterValueException(String parameterName, String message) {
        return new InvalidParameterValueException().at(parameterName).withMessage(message);
    }

    @Deprecated
    public static OwsExceptionReport createVersionNegotiationFailedException(String message) {
        return new VersionNegotiationFailedException().withMessage(message);
    }

    @Deprecated
    public static void mergeAndThrowExceptions(List<OwsExceptionReport> exceptions) throws OwsExceptionReport {
        new CompositeOwsException().add(exceptions).throwIfNotEmpty();
    }

    @Deprecated
    public static OwsExceptionReport merge(OwsExceptionReport... exceptions) {
        return new CompositeOwsException().add(exceptions);
    }

    @Deprecated
    public static OwsExceptionReport createOptionNotSupportedException(String parameterName, String message) {
        return new OptionNotSupportedException().at(parameterName).withMessage(message);
    }

    @Deprecated
    public static OwsExceptionReport createInvalidRequestException(String message, Exception exception) {
        return new InvalidRequestException().withMessage(message).causedBy(exception);
    }

    @Deprecated
    public static OwsExceptionReport createInvalidPropertyOfferingCombination(String message) {
        return new InvalidPropertyOfferingCombinationException().withMessage(message);
    }

    private Util4Exceptions() {
    }
}
