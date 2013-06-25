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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import net.opengis.swes.x20.DeleteSensorResponseDocument;
import net.opengis.swes.x20.DeleteSensorResponseType;
import net.opengis.swes.x20.DescribeSensorResponseDocument;
import net.opengis.swes.x20.DescribeSensorResponseType;
import net.opengis.swes.x20.InsertSensorResponseDocument;
import net.opengis.swes.x20.InsertSensorResponseType;
import net.opengis.swes.x20.UpdateSensorDescriptionResponseDocument;
import net.opengis.swes.x20.UpdateSensorDescriptionResponseType;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.response.AbstractServiceResponse;
import org.n52.sos.response.DeleteSensorResponse;
import org.n52.sos.response.DescribeSensorResponse;
import org.n52.sos.response.InsertSensorResponse;
import org.n52.sos.response.UpdateSensorResponse;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.N52XmlHelper;
import org.n52.sos.util.SchemaLocation;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.XmlHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwesEncoderv20 implements Encoder<XmlObject, AbstractServiceResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwesEncoderv20.class);

    private static final Set<EncoderKey> ENCODER_KEYS = CodingHelper.encoderKeysForElements(SWEConstants.NS_SWES_20,
            DescribeSensorResponse.class, InsertSensorResponse.class, UpdateSensorResponse.class,
            DeleteSensorResponse.class);

    public SwesEncoderv20() {
        LOGGER.debug("Encoder for the following keys initialized successfully: {}!",
                StringHelper.join(", ", ENCODER_KEYS));
    }

    @Override
    public Set<EncoderKey> getEncoderKeyType() {
        return Collections.unmodifiableSet(ENCODER_KEYS);
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.emptySet();
    }

    @Override
    public void addNamespacePrefixToMap(final Map<String, String> nameSpacePrefixMap) {
        if (nameSpacePrefixMap != null) {
            nameSpacePrefixMap.put(SWEConstants.NS_SWES_20, SWEConstants.NS_SWES_PREFIX);
        }
    }

    @Override
    public String getContentType() {
        return SosConstants.CONTENT_TYPE_XML;
    }

    @Override
    public Set<SchemaLocation> getSchemaLocations() {
        return CollectionHelper.set(SWEConstants.SWES_20_SCHEMA_LOCATION);
    }

    @Override
    public XmlObject encode(final AbstractServiceResponse response) throws OwsExceptionReport {
        return encode(response, null);
    }

    @Override
    public XmlObject encode(final AbstractServiceResponse response, final Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        XmlObject encodedObject = null;
        if (response instanceof DescribeSensorResponse) {
            encodedObject = createDescribeSensorResponse((DescribeSensorResponse) response);
        } else if (response instanceof InsertSensorResponse) {
            encodedObject = createInsertSensorResponse((InsertSensorResponse) response);
        } else if (response instanceof UpdateSensorResponse) {
            encodedObject = createUpdateSensorResponse((UpdateSensorResponse) response);
        } else if (response instanceof DeleteSensorResponse) {
            encodedObject = createDeleteSensorResponse((DeleteSensorResponse) response);
        } else {
            throw new UnsupportedEncoderInputException(this, response);
        }
        LOGGER.debug("Encoded object {} is valid: {}", encodedObject.schemaType().toString(),
                XmlHelper.validateDocument(encodedObject));
        return encodedObject;
    }

    private XmlObject createDescribeSensorResponse(final DescribeSensorResponse response) throws OwsExceptionReport {
        final DescribeSensorResponseDocument xbDescSensorRespDoc =
                DescribeSensorResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final DescribeSensorResponseType describeSensorResponse = xbDescSensorRespDoc.addNewDescribeSensorResponse();
        describeSensorResponse.setProcedureDescriptionFormat(response.getOutputFormat());
        final XmlObject xmlObject =
                CodingHelper.encodeObjectToXml(response.getOutputFormat(), response.getSensorDescription());
        describeSensorResponse.addNewDescription().addNewSensorDescription().addNewData().set(xmlObject);
        // set schema location
        final Set<SchemaLocation> schemaLocations = CollectionHelper.set();
        schemaLocations.add(SWEConstants.SWES_20_DESCRIBE_SENSOR_SCHEMA_LOCATION);
        N52XmlHelper.addSchemaLocationsForTo(xbDescSensorRespDoc, schemaLocations);
        N52XmlHelper.setSchemaLocationsToDocument(xbDescSensorRespDoc, schemaLocations);
        return xbDescSensorRespDoc;
    }

    private XmlObject createInsertSensorResponse(final InsertSensorResponse response) {
        final InsertSensorResponseDocument xbInsSenRespDoc =
                InsertSensorResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final InsertSensorResponseType xbInsSenResp = xbInsSenRespDoc.addNewInsertSensorResponse();
        xbInsSenResp.setAssignedProcedure(response.getAssignedProcedure());
        xbInsSenResp.setAssignedOffering(response.getAssignedOffering());
        // set schema location
        final Set<SchemaLocation> schemaLocations = CollectionHelper.set();
        schemaLocations.add(SWEConstants.SWES_20_INSERT_SENSOR_SCHEMA_LOCATION);
        N52XmlHelper.addSchemaLocationsForTo(xbInsSenRespDoc, schemaLocations);
        N52XmlHelper.setSchemaLocationsToDocument(xbInsSenRespDoc, schemaLocations);
        return xbInsSenRespDoc;
    }

    private XmlObject createUpdateSensorResponse(final UpdateSensorResponse response) {
        final UpdateSensorDescriptionResponseDocument xbUpSenRespDoc =
                UpdateSensorDescriptionResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance()
                        .getXmlOptions());
        final UpdateSensorDescriptionResponseType xbUpSenResp = xbUpSenRespDoc.addNewUpdateSensorDescriptionResponse();
        xbUpSenResp.setUpdatedProcedure(response.getUpdatedProcedure());
        // set schema location
        final Set<SchemaLocation> schemaLocations = CollectionHelper.set();
        schemaLocations.add(SWEConstants.SWES_20_UPDATE_SENSOR_DESCRIPTION_SCHEMA_LOCATION);
        N52XmlHelper.addSchemaLocationsForTo(xbUpSenRespDoc, schemaLocations);
        N52XmlHelper.setSchemaLocationsToDocument(xbUpSenRespDoc, schemaLocations);
        return xbUpSenRespDoc;
    }

    private XmlObject createDeleteSensorResponse(final DeleteSensorResponse response) {
        final DeleteSensorResponseDocument xbDelSenRespDoc =
                DeleteSensorResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final DeleteSensorResponseType xbDelSenResp = xbDelSenRespDoc.addNewDeleteSensorResponse();
        xbDelSenResp.setDeletedProcedure(response.getDeletedProcedure());
        // set schema location
        final Set<SchemaLocation> schemaLocations = CollectionHelper.set();
        schemaLocations.add(SWEConstants.SWES_20_DELETE_SENSOR_SCHEMA_LOCATION);
        N52XmlHelper.addSchemaLocationsForTo(xbDelSenRespDoc, schemaLocations);
        N52XmlHelper.setSchemaLocationsToDocument(xbDelSenRespDoc, schemaLocations);
        return xbDelSenRespDoc;
    }

}
