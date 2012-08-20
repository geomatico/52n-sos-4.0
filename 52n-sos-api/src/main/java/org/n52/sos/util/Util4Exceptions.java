/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.util;

import java.util.List;

import org.n52.sos.ogc.ows.OWSConstants.ExceptionLevel;
import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants.SosExceptionCode;

/**
 * class offers util methods for Exceptions used in this SOS
 * 
 * 
 */
public class Util4Exceptions {

    /**
     * Hide utility constructor
     */
    private Util4Exceptions() {
        super();
    }

    /**
     * creates a ServiceException, if a request parameter is missing
     * 
     * @param parameterName
     *            name of the parameter, which is missing in the request
     * @return Returns ServiceException with ExceptionCode =
     *         MissingParameterValue and corresponding message
     * 
     */
    public static OwsExceptionReport createMissingParameterValueException(String parameterName)
            throws OwsExceptionReport {
        OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
        owse.addCodedException(OwsExceptionCode.MissingParameterValue, parameterName,
                "The mandatory parameter value '" + parameterName + "' is missing in the request!");
        return owse;
    }

    /**
     * creates a ServiceException for GetObservation requests that are too large
     * 
     * @param responseSize
     *            Number of observations matching request
     * @return Returns ServiceException with ExceptionCode =
     *         ResponseExceedsSizeLimit and corresponding message
     * 
     */
    public static OwsExceptionReport createResponseExceedsSizeLimitException(int responseSize, int responseLimit)
            throws OwsExceptionReport {
        OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
        owse.addCodedException(SosExceptionCode.ResponseExceedsSizeLimit, null, "The request matched " + responseSize
                + " observations, which exceeds this " + " server's limit of " + responseLimit);
        return owse;
    }

    public static OwsExceptionReport createOperationNotSupportedException(String operationName) {
        OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
        owse.addCodedException(OwsExceptionCode.OperationNotSupported, operationName, "The requested operation '"
                + operationName + "' is not supported by this service!");
        return owse;
    }

    public static OwsExceptionReport createNoApplicableCodeException(Exception exception, String message) {
        OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions); 
        owse.addCodedException(OwsExceptionCode.NoApplicableCode, null, message, exception);
        return owse;
    }

    public static OwsExceptionReport createInvalidParameterValueException(String parameterName, String message) {
        OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
        owse.addCodedException(OwsExceptionCode.InvalidParameterValue, parameterName, message);
        return owse;
    }

    public static OwsExceptionReport createVersionNegotiationFailedException(String message) {
        OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
        owse.addCodedException(OwsExceptionCode.VersionNegotiationFailed, null, message);
        return owse;
    }

    public static void mergeExceptions(List<OwsExceptionReport> exceptions) throws OwsExceptionReport {
        if (!exceptions.isEmpty()) {
            OwsExceptionReport owse = null;
            for (OwsExceptionReport owsExceptionReport : exceptions) {
                if (owse == null) {
                    owse = owsExceptionReport;
                } else {
                    owse.addOwsExceptionReport(owsExceptionReport);
                }
            }
            throw owse;
        }
    }

}
