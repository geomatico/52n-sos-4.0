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
package org.n52.sos.ogc.swe;

import javax.xml.namespace.QName;

import org.n52.sos.exception.IExceptionCode;

/**
 * Constants class for SWE
 * 
 */
public class SWEConstants {

    // namespaces and schema locations
    public static final String NS_SWE = "http://www.opengis.net/swe/1.0.1";

    public static final String NS_SWE_20 = "http://www.opengis.net/swe/2.0";

    public static final String NS_SWE_PREFIX = "swe";

    public static final String NS_SWES_20 = "http://www.opengis.net/swes/2.0";

    public static final String NS_SWES_PREFIX = "swes";

    public static final String SCHEMA_LOCATION_SWE = "http://schemas.opengis.net/sweCommon/1.0.1/swe.xsd";

    public static final String SCHEMA_LOCATION_SWE_200 = "http://schemas.opengis.net/sweCommon/2.0/swe.xsd";
    
    public static final String SCHEMA_LOCATION_SWES_200 = "http://schemas.opengis.net/swes/2.0/swes.xsd";

    // element names

    public static final String EN_SIMPLEDATARECORD = "SimpleDataRecord";

    public static final String EN_BOOLEAN = "Boolean";

    public static final String EN_CATEGORY = "Category";

    public static final String EN_COUNT = "Count";
    
    public static final String EN_DATA_RECORD = "DataRecord";
    
    public static final String EN_DATA_ARRAY = "DataArray";

    public static final String EN_QUANTITY = "Quantity";

    public static final String EN_TEXT = "Text";

    public static final String EN_TIME = "Time";
    
    public static final String EN_TIME_RANGE = "TimeRange";

    public static final String EN_TEXT_ENCODING = "TextEncoding";

    public static final String EN_INSERTION_METADATA = "InsertionMetadata";

    public static final String EN_METADATA = "metadata";

    public static final String EN_ABSTRACT_OFFERING = "AbstractOffering";

    public static final String EN_OFFERING = "offering";
    
    // QNames for elements

    public static final QName QN_DATA_ARRAY_SWE_200 = new QName(NS_SWE, EN_DATA_ARRAY, NS_SWE_PREFIX);

    public static final QName QN_DATA_RECORD_SWE_200 = new QName(NS_SWE, EN_DATA_RECORD, NS_SWE_PREFIX);

    public static final QName QN_SIMPLEDATARECORD_SWE_101 = new QName(NS_SWE, EN_SIMPLEDATARECORD, NS_SWE_PREFIX);

    public static final QName QN_BOOLEAN_SWE_101 = new QName(NS_SWE, EN_BOOLEAN, NS_SWE_PREFIX);

    public static final QName QN_CATEGORY_SWE_101 = new QName(NS_SWE, EN_CATEGORY, NS_SWE_PREFIX);

    public static final QName QN_COUNT_SWE_101 = new QName(NS_SWE, EN_COUNT, NS_SWE_PREFIX);

    public static final QName QN_QUANTITY_SWE_101 = new QName(NS_SWE, EN_QUANTITY, NS_SWE_PREFIX);

    public static final QName QN_TEXT_SWE_101 = new QName(NS_SWE, EN_TEXT, NS_SWE_PREFIX);

    public static final QName QN_TIME_SWE_101 = new QName(NS_SWE, EN_TIME, NS_SWE_PREFIX);

    public static final QName QN_TIME_RANGE_SWE_101 = new QName(NS_SWE, EN_TIME_RANGE, NS_SWE_PREFIX);

    public static final QName QN_TEXT_ENCODING_SWE_101 = new QName(NS_SWE, EN_TEXT_ENCODING, NS_SWE_PREFIX);
    
    public static final QName QN_BOOLEAN_SWE_200 = new QName(NS_SWE_20, EN_BOOLEAN, NS_SWE_PREFIX);

    public static final QName QN_CATEGORY_SWE_200 = new QName(NS_SWE_20, EN_CATEGORY, NS_SWE_PREFIX);

    public static final QName QN_COUNT_SWE_200 = new QName(NS_SWE_20, EN_COUNT, NS_SWE_PREFIX);

    public static final QName QN_QUANTITY_SWE_200 = new QName(NS_SWE_20, EN_QUANTITY, NS_SWE_PREFIX);

    public static final QName QN_TEXT_SWE_200 = new QName(NS_SWE_20, EN_TEXT, NS_SWE_PREFIX);

    public static final QName QN_TIME_SWE_200 = new QName(NS_SWE_20, EN_TIME, NS_SWE_PREFIX);

    public static final QName QN_TIME_RANGE_SWE_200 = new QName(NS_SWE_20, EN_TIME_RANGE, NS_SWE_PREFIX);

    public static final QName QN_TEXT_ENCODING_SWE_200 = new QName(NS_SWE_20, EN_TEXT_ENCODING, NS_SWE_PREFIX);

	public static final QName QN_INSERTION_METADATA = new QName(NS_SWES_20, EN_INSERTION_METADATA, NS_SWES_PREFIX);

    public static final QName QN_METADATA = new QName(NS_SWES_20, EN_METADATA, NS_SWES_PREFIX);

    public static final QName QN_ABSTRACT_OFFERING = new QName(NS_SWES_20, EN_ABSTRACT_OFFERING, NS_SWES_PREFIX);

    public static final QName QN_OFFERING = new QName(NS_SWES_20, EN_OFFERING, NS_SWES_PREFIX);

    public static final String SOAP_REASON_INVALID_REQUEST =
            "The request did not conform to its XML Schema definition.";

    public static final String SOAP_REASON_REQUEST_EXTENSION_NOT_SUPPORTED = "";

    /**
     * Enum for SensorML types
     */
    public static enum SensorMLType {
        System, Component, ProcessModel, ProcessChain
    }

    /**
     * Enum for SWE aggregate types
     */
    public static enum SweAggregateType {
        SimpleDataRecord, DataRecord
    }

    /**
     * Enum for SWE simple types
     */
    public static enum SweSimpleType {
        Boolean, Category, Count, CountRange, Quantity, QuantityRange, Text, Time, TimeRange, ObservableProperty
    }

    /**
     * Enum for coordinate names
     */
    public static enum SweCoordinateName {
        easting, northing, altitude
    }

    /**
     * Enum for sensor descriptions
     */
    public static enum SosSensorDescription {
        XmlStringDescription, SosDescription
    }

    public enum SwesExceptionCode implements IExceptionCode {
        InvalidRequest(SOAP_REASON_INVALID_REQUEST), 
        RequestExtensionNotSupported(SOAP_REASON_REQUEST_EXTENSION_NOT_SUPPORTED);

        private final String soapFaultReason;
        
        private SwesExceptionCode(String soapFaultReason) {
            this.soapFaultReason = soapFaultReason;
        }
        
        @Override
        public String getSoapFaultReason() {
            return this.soapFaultReason;
        }
    }

    private SWEConstants() {
    }

}
