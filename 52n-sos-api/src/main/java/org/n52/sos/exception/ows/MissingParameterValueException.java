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
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class MissingParameterValueException extends CodedOwsException {
    private static final long serialVersionUID = 1L;
    public static final OwsExceptionCode EXCEPTION_CODE = OwsExceptionCode.MissingParameterValue;

    public MissingParameterValueException(String parameter) {
        super(EXCEPTION_CODE);
        at(parameter).withMessage("The value for the parameter '%s' is missing in the request!", parameter);
    }

    public MissingParameterValueException(Enum<?> parameter) {
        this(parameter.name());
    }

    public static class MissingObservedPropertyParameterException extends MissingParameterValueException {
        private static final long serialVersionUID = 1L;

        public MissingObservedPropertyParameterException() {
            super(SosConstants.GetObservationParams.observedProperty);
        }
    }

    public static class MissingRequestParameterException extends MissingParameterValueException {
        private static final long serialVersionUID = 1L;

        public MissingRequestParameterException() {
            super(RequestParams.request);
        }
    }

    public static class MissingFeatureOfInterestTypeParameterException extends MissingParameterValueException {
        private static final long serialVersionUID = 1L;

        public MissingFeatureOfInterestTypeParameterException() {
            super(Sos2Constants.InsertSensorParams.featureOfInterestType);
        }
    }

    public static class MissingOfferingParameterException extends MissingParameterValueException {
        private static final long serialVersionUID = 1L;

        public MissingOfferingParameterException() {
            super(SosConstants.GetObservationParams.offering);
        }
    }

    public static class MissingProcedureDescriptionFormatException extends MissingParameterValueException {
        private static final long serialVersionUID = 1L;

        public MissingProcedureDescriptionFormatException() {
            super(Sos2Constants.DescribeSensorParams.procedureDescriptionFormat);
        }
    }

    public static class MissingProcedureParameterException extends MissingParameterValueException {
        private static final long serialVersionUID = 1L;

        public MissingProcedureParameterException() {
            super(SosConstants.DescribeSensorParams.procedure);
        }
    }

    public static class MissingResponseFormatParameterException extends MissingParameterValueException {
        private static final long serialVersionUID = 1L;

        public MissingResponseFormatParameterException() {
            super(SosConstants.GetObservationParams.responseFormat);
        }
    }

    public static class MissingResultValuesException extends MissingParameterValueException {
        private static final long serialVersionUID = 1L;

        public MissingResultValuesException() {
            super(Sos2Constants.InsertResultParams.resultValues);
        }
    }

    public static class MissingServiceParameterException extends MissingParameterValueException {
        private static final long serialVersionUID = 1L;

        public MissingServiceParameterException() {
            super(RequestParams.service);
        }
    }

    public static class MissingVersionParameterException extends MissingParameterValueException {
        private static final long serialVersionUID = 1L;

        public MissingVersionParameterException() {
            super(RequestParams.version);
        }
    }

    public static class MissingValueReferenceException extends MissingParameterValueException {
        private static final long serialVersionUID = 1L;

        public MissingValueReferenceException() {
            super(SosConstants.Filter.ValueReference);
        }
    }

    public static class MissingObservationParameterException extends MissingParameterValueException {
        private static final long serialVersionUID = 1L;

        public MissingObservationParameterException() {
            super(Sos2Constants.InsertObservationParams.observation);
        }
    }
}
