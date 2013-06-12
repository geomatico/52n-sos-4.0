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

import org.n52.sos.util.SchemaLocation;


/**
 * Constants class for SWE
 * 
 */
public interface SWEConstants {

    // namespaces and schema locations
    String NS_SWE_101 = "http://www.opengis.net/swe/1.0.1";

    String NS_SWE_20 = "http://www.opengis.net/swe/2.0";

    String NS_SWE_PREFIX = "swe";

    String NS_SWES_20 = "http://www.opengis.net/swes/2.0";

    String NS_SWES_PREFIX = "swes";

    String SCHEMA_LOCATION_URL_SWE_101 = "http://schemas.opengis.net/sweCommon/1.0.1/swe.xsd";

    String SCHEMA_LOCATION_URL_SWE_20 = "http://schemas.opengis.net/sweCommon/2.0/swe.xsd";
    
    String SCHEMA_LOCATION_URL_SWES_20 = "http://schemas.opengis.net/swes/2.0/swes.xsd";
    
    String SCHEMA_LOCATION_URL_SWES_20_DESCRIBE_SENSOR = "http://schemas.opengis.net/swes/2.0/swesDescribeSensor.xsd";
    
    String SCHEMA_LOCATION_URL_SWES_20_DELETE_SENSOR = "http://schemas.opengis.net/swes/2.0/swesDeleteSensor.xsd";
    
    String SCHEMA_LOCATION_URL_SWES_20_INSERT_SENSOR = "http://schemas.opengis.net/swes/2.0/swesInsertSensor.xsd";
    
    String SCHEMA_LOCATION_URL_SWES_20_UPDATE_SENSOR_DESCRIPTION = "http://schemas.opengis.net/swes/2.0/swesUpdateSensorDescription.xsd";
    
    SchemaLocation SWE_101_SCHEMA_LOCATION = new SchemaLocation(NS_SWE_101, SCHEMA_LOCATION_URL_SWE_101);
    
    SchemaLocation SWE_20_SCHEMA_LOCATION = new SchemaLocation(NS_SWE_20, SCHEMA_LOCATION_URL_SWE_20);
    
    SchemaLocation SWES_20_SCHEMA_LOCATION = new SchemaLocation(NS_SWES_20, SCHEMA_LOCATION_URL_SWES_20);
    
    SchemaLocation SWES_20_DESCRIBE_SENSOR_SCHEMA_LOCATION = new SchemaLocation(NS_SWES_20, SCHEMA_LOCATION_URL_SWES_20_DESCRIBE_SENSOR);

    SchemaLocation SWES_20_INSERT_SENSOR_SCHEMA_LOCATION = new SchemaLocation(NS_SWES_20, SCHEMA_LOCATION_URL_SWES_20_INSERT_SENSOR);

    SchemaLocation SWES_20_UPDATE_SENSOR_DESCRIPTION_SCHEMA_LOCATION = new SchemaLocation(NS_SWES_20, SCHEMA_LOCATION_URL_SWES_20_UPDATE_SENSOR_DESCRIPTION);

    SchemaLocation SWES_20_DELETE_SENSOR_SCHEMA_LOCATION = new SchemaLocation(NS_SWES_20, SCHEMA_LOCATION_URL_SWES_20_DELETE_SENSOR);

    // element names
    String EN_ABSTRACT_OFFERING = "AbstractOffering";

    String EN_BOOLEAN = "Boolean";

    String EN_CATEGORY = "Category";

    String EN_COUNT = "Count";

    String EN_DATA_ARRAY = "DataArray";

    String EN_DATA_ARRAY_PROPERTY_TYPE = "DataArrayPropertyType";

    String EN_DATA_CHOICE = "DataChoice";
    
    String EN_DATA_RECORD = "DataRecord";

    String EN_DELETE_SENSOR = "DeleteSensor";

    String EN_DELETE_SENSOR_RESPONSE = "DeleteSensorResponse";

    String EN_DESCRIBE_SENSOR = "DescribeSensor";

    String EN_DESCRIBE_SENSOR_RESPONSE = "DescribeSensorResponse";
    
    String EN_ENVELOPE = "Envelope";    

    String EN_INSERT_SENSOR = "InsertSensor";

    String EN_INSERT_SENSOR_RESPONSE = "InsertSensorResponse";

    String EN_INSERTION_METADATA = "InsertionMetadata";

    String EN_METADATA = "metadata";

    String EN_OFFERING = "offering";

    String EN_QUANTITY = "Quantity";
    
    String EN_QUANTITY_RANGE = "QuantityRange";

    String EN_SIMPLEDATARECORD = "SimpleDataRecord";

    String EN_TEXT = "Text";

    String EN_TEXT_ENCODING = "TextEncoding";

    String EN_TIME = "Time";

    String EN_TIME_RANGE = "TimeRange";

    String EN_UPDATE_SENSOR_DESCRIPTION = "UpdateSensorDescription";

    String EN_UPDATE_SENSOR_DESCRIPTION_RESPONSE = "UpdateSensorDescriptionResponse";

    String EN_VECTOR = "Vector";    

    // QNames for elements
    QName QN_ABSTRACT_OFFERING = new QName(NS_SWES_20, EN_ABSTRACT_OFFERING, NS_SWES_PREFIX);

    QName QN_BOOLEAN_SWE_101 = new QName(NS_SWE_101, EN_BOOLEAN, NS_SWE_PREFIX);

    QName QN_BOOLEAN_SWE_200 = new QName(NS_SWE_20, EN_BOOLEAN, NS_SWE_PREFIX);

    QName QN_CATEGORY_SWE_101 = new QName(NS_SWE_101, EN_CATEGORY, NS_SWE_PREFIX);

    QName QN_CATEGORY_SWE_200 = new QName(NS_SWE_20, EN_CATEGORY, NS_SWE_PREFIX);

    QName QN_COUNT_SWE_101 = new QName(NS_SWE_101, EN_COUNT, NS_SWE_PREFIX);

    QName QN_COUNT_SWE_200 = new QName(NS_SWE_20, EN_COUNT, NS_SWE_PREFIX);

    QName QN_DATA_ARRAY_SWE_200 = new QName(NS_SWE_20, EN_DATA_ARRAY, NS_SWE_PREFIX);

    QName QN_DATA_RECORD_SWE_200 = new QName(NS_SWE_20, EN_DATA_RECORD, NS_SWE_PREFIX);

    QName QN_DATA_ARRAY_PROPERTY_TYPE_SWE_200 = new QName(NS_SWE_20, EN_DATA_ARRAY_PROPERTY_TYPE, NS_SWE_PREFIX);

    QName QN_DELETE_SENSOR = new QName(NS_SWES_20, EN_DELETE_SENSOR, NS_SWES_PREFIX);

    QName QN_DELETE_SENSOR_RESPONSE = new QName(NS_SWES_20, EN_DELETE_SENSOR_RESPONSE, NS_SWES_PREFIX);

    QName QN_DESCRIBE_SENSOR = new QName(NS_SWES_20, EN_DESCRIBE_SENSOR, NS_SWES_PREFIX);

    QName QN_DESCRIBE_SENSOR_RESPONSE = new QName(NS_SWES_20, EN_DESCRIBE_SENSOR_RESPONSE, NS_SWES_PREFIX);

    QName QN_INSERT_SENSOR = new QName(NS_SWES_20, EN_INSERT_SENSOR, NS_SWES_PREFIX);

    QName QN_INSERT_SENSOR_RESPONSE = new QName(NS_SWES_20, EN_INSERT_SENSOR_RESPONSE, NS_SWES_PREFIX);

    QName QN_INSERTION_METADATA = new QName(NS_SWES_20, EN_INSERTION_METADATA, NS_SWES_PREFIX);

    QName QN_METADATA = new QName(NS_SWES_20, EN_METADATA, NS_SWES_PREFIX);

    QName QN_OFFERING = new QName(NS_SWES_20, EN_OFFERING, NS_SWES_PREFIX);

    QName QN_QUANTITY_SWE_101 = new QName(NS_SWE_101, EN_QUANTITY, NS_SWE_PREFIX);

    QName QN_QUANTITY_SWE_200 = new QName(NS_SWE_20, EN_QUANTITY, NS_SWE_PREFIX);
    
    QName QN_QUANTITY_RANGE_SWE_200 = new QName(NS_SWE_20, EN_QUANTITY_RANGE, NS_SWE_PREFIX);

    QName QN_SIMPLEDATARECORD_SWE_101 = new QName(NS_SWE_101, EN_SIMPLEDATARECORD, NS_SWE_PREFIX);
    QName QN_DATA_RECORD_SWE_101 = new QName(NS_SWE_101, EN_DATA_RECORD, NS_SWE_PREFIX);

    QName QN_TEXT_ENCODING_SWE_101 = new QName(NS_SWE_101, EN_TEXT_ENCODING, NS_SWE_PREFIX);

    QName QN_TEXT_ENCODING_SWE_200 = new QName(NS_SWE_20, EN_TEXT_ENCODING, NS_SWE_PREFIX);

    QName QN_TEXT_SWE_101 = new QName(NS_SWE_101, EN_TEXT, NS_SWE_PREFIX);

    QName QN_TEXT_SWE_200 = new QName(NS_SWE_20, EN_TEXT, NS_SWE_PREFIX);

    QName QN_TIME_RANGE_SWE_101 = new QName(NS_SWE_101, EN_TIME_RANGE, NS_SWE_PREFIX);

    QName QN_TIME_RANGE_SWE_200 = new QName(NS_SWE_20, EN_TIME_RANGE, NS_SWE_PREFIX);

    QName QN_TIME_SWE_101 = new QName(NS_SWE_101, EN_TIME, NS_SWE_PREFIX);
    QName QN_ENVELOPE_SWE_101 = new QName(NS_SWE_101, EN_ENVELOPE, NS_SWE_PREFIX);

    QName QN_TIME_SWE_200 = new QName(NS_SWE_20, EN_TIME, NS_SWE_PREFIX);

    QName QN_UPDATE_SENSOR_DESCRIPTION = new QName(NS_SWES_20, EN_UPDATE_SENSOR_DESCRIPTION, NS_SWES_PREFIX);

    QName QN_UPDATE_SENSOR_DESCRIPTION_RESPONSE = new QName(NS_SWES_20, EN_UPDATE_SENSOR_DESCRIPTION_RESPONSE, NS_SWES_PREFIX);
    
    String SOAP_REASON_INVALID_REQUEST = "The request did not conform to its XML Schema definition.";

    String SOAP_REASON_REQUEST_EXTENSION_NOT_SUPPORTED = ""; //FIXME emtpy constant

    /**
     * Enum for SensorML types
     */
    enum SensorMLType {
        System,
        Component,
        ProcessModel,
        ProcessChain
    }

    /**
     * Enum for SWE aggregate types
     */
    enum SweAggregateType {
        SimpleDataRecord,
        DataRecord
    }

    /**
     * Enum for coordinate names
     */
    enum SweCoordinateName {
        easting,
        northing,
        altitude
    }

    /**
     * Enum for sensor descriptions
     */
    enum SosSensorDescription {
        XmlStringDescription,
        SosDescription
    }
    
    /**
     * Enum for SWE DataComponent types
     */
    enum SweDataComponentType {
        DataArray,
        DataRecord,
        SimpleDataRecord,
        Envelope,
        Field,
        Vector,
        Position,
        Boolean,
        Category,
        Count,
        CountRange,
        Quantity,
        QuantityRange,
        Text,
        Time,
        TimeRange,
        ObservableProperty
    }
}
