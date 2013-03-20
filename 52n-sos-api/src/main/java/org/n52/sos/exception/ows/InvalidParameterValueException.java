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

import org.n52.sos.ogc.ows.OWSConstants.RequestParams;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class InvalidParameterValueException extends CodedOwsException {
    private static final long serialVersionUID = 7664405001972222761L;

    public InvalidParameterValueException() {
        super(OwsExceptionCode.InvalidParameterValue);
    }

    public InvalidParameterValueException(String parameterName, String value) {
        super(OwsExceptionCode.InvalidParameterValue);
        withMessage("The value '%s' of the parameter '%s' is invalid", value, parameterName).at(parameterName);
    }

    public InvalidParameterValueException(Enum<?> parameterName, String value) {
        this(parameterName.name(), value);
    }

    public static class VersionNotSupportedException extends InvalidParameterValueException {
        private static final long serialVersionUID = 7013609926378578859L;

        public VersionNotSupportedException() {
            withMessage("The requested version is not supported!").at(RequestParams.version);
        }
    }

    public static class ServiceNotSupportedException extends InvalidParameterValueException {
        private static final long serialVersionUID = -4185556617598192292L;

        public ServiceNotSupportedException() {
            super(RequestParams.service, "The requested service is not supported!");
        }
    }

    public static class InvalidServiceOrVersionException extends InvalidParameterValueException {
        private static final long serialVersionUID = 6678340028297145825L;

        public InvalidServiceOrVersionException(String service, String version) {
            at(RequestParams.service);
            withMessage("The requested service (%s) and/or version (%s) is not supported by this server!", service, version);
        }
    }

    public static class InvalidOfferingParameterException extends InvalidParameterValueException {
        private static final long serialVersionUID = 3382928908282530867L;

        public InvalidOfferingParameterException(String value) {
            super(SosConstants.GetObservationParams.offering, value);
        }
    }

    public static class InvalidObservedPropertyParameterException extends InvalidParameterValueException {
        private static final long serialVersionUID = -4272023297121839387L;

        public InvalidObservedPropertyParameterException(String value) {
            super(SosConstants.GetObservationParams.observedProperty, value);
        }
    }

    public static class InvalidFeatureOfInterestTypeException extends InvalidParameterValueException {
        private static final long serialVersionUID = 4225597475733453751L;

        public InvalidFeatureOfInterestTypeException(String value) {
            super(Sos2Constants.InsertSensorParams.featureOfInterestType, value);
        }
    }

    public static class InvalidValueReferenceException extends InvalidParameterValueException {
        private static final long serialVersionUID = 4751250325590494752L;

        public InvalidValueReferenceException(String value) {
            super(SosConstants.Filter.ValueReference, value);
        }
    }

    public static class InvalidObservationTypeException extends InvalidParameterValueException {
        private static final long serialVersionUID = 7783723530983520961L;

        public InvalidObservationTypeException(String value) {
            super(Sos2Constants.InsertObservationParams.observationType, value);
        }
    }

    public static class InvalidObservationTypeForOfferingException extends InvalidParameterValueException {
        private static final long serialVersionUID = -1598937401956045205L;

        public InvalidObservationTypeForOfferingException(String observationType, String offering) {
            at(Sos2Constants.InsertObservationParams.observationType);
            withMessage("The requested observationType (%s) is not allowed for the requested offering (%s)!",
                        observationType, offering);

        }
    }

    public static class InvalidResponseFormatParameterException extends InvalidParameterValueException {
        private static final long serialVersionUID = 1740331674439130975L;

        public InvalidResponseFormatParameterException(String value) {
            super(SosConstants.GetObservationParams.responseFormat, value);
        }
    }

    public static class InvalidResponseModeParameterException extends InvalidParameterValueException {
        private static final long serialVersionUID = -4370825833122909637L;

        public InvalidResponseModeParameterException(String value) {
            super(SosConstants.GetObservationParams.responseMode, value);
        }
    }

    public static class InvalidTemporalFilterParameterException extends InvalidParameterValueException {
        private static final long serialVersionUID = 4454244754129206013L;

        public InvalidTemporalFilterParameterException(String value) {
            super(Sos2Constants.GetObservationParams.temporalFilter, value);
        }
    }

    public static class InvalidProcedureParameterException extends InvalidParameterValueException {
        private static final long serialVersionUID = 2578703817081517533L;

        public InvalidProcedureParameterException(String value) {
            super(SosConstants.DescribeSensorParams.procedure, value);
        }
    }

    public static class InvalidProcedureDescriptionFormatException extends InvalidParameterValueException {
        private static final long serialVersionUID = -6138488504467961928L;

        public InvalidProcedureDescriptionFormatException(String value) {
            super(Sos2Constants.DescribeSensorParams.procedureDescriptionFormat, value);
        }
    }

    public static class InvalidOutputFormatException extends InvalidParameterValueException {
        private static final long serialVersionUID = 6425942384678159423L;

        public InvalidOutputFormatException(String value) {
            super(Sos1Constants.DescribeSensorParams.outputFormat, value);
        }
    }
}
