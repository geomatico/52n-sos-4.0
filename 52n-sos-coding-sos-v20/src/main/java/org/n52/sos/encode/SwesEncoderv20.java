/**
 * Copyright (C) 2012
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.response.AbstractServiceResponse;
import org.n52.sos.response.DeleteSensorResponse;
import org.n52.sos.response.DescribeSensorResponse;
import org.n52.sos.response.InsertSensorResponse;
import org.n52.sos.response.UpdateSensorResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.N52XmlHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwesEncoderv20 implements IEncoder<XmlObject, AbstractServiceResponse> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SwesEncoderv20.class);

    private List<EncoderKeyType> encoderKeyTypes;

    public SwesEncoderv20() {
        encoderKeyTypes = new ArrayList<EncoderKeyType>();
        encoderKeyTypes.add(new EncoderKeyType(SWEConstants.NS_SWES_20));
        StringBuilder builder = new StringBuilder();
        for (EncoderKeyType encoderKeyType : encoderKeyTypes) {
            builder.append(encoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        LOGGER.info("Encoder for the following keys initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<EncoderKeyType> getEncoderKeyType() {
        return encoderKeyTypes;
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return new HashMap<SupportedTypeKey, Set<String>>(0);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return new HashSet<String>(0);
    }
    
    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(SWEConstants.NS_SWES_20, SWEConstants.NS_SWES_PREFIX);
    }
    
    @Override
    public String getContentType() {
        return "text/xml";
    }

    @Override
    public XmlObject encode(AbstractServiceResponse response) throws OwsExceptionReport {
        return encode(response, null);
    }

    @Override
    public XmlObject encode(AbstractServiceResponse response, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        if (response instanceof DescribeSensorResponse) {
            return createDescribeSensorResponse((DescribeSensorResponse) response);
        } else if (response instanceof InsertSensorResponse) {
            return createInsertSensorResponse((InsertSensorResponse) response);
        } else if (response instanceof UpdateSensorResponse) {
            return createUpdateSensorResponse((UpdateSensorResponse) response);
        } else if (response instanceof DeleteSensorResponse) {
            return createDeleteSensorResponse((DeleteSensorResponse) response);
        }
        return null;
    }
    
    private XmlObject createDescribeSensorResponse(DescribeSensorResponse response) throws OwsExceptionReport {
        DescribeSensorResponseDocument xbDescSensorRespDoc =
                DescribeSensorResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        DescribeSensorResponseType describeSensorResponse = xbDescSensorRespDoc.addNewDescribeSensorResponse();
        describeSensorResponse.setProcedureDescriptionFormat(response.getOutputFormat());
        String outputFormat = null;
        if (response.getOutputFormat().equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE)) {
            outputFormat = SensorMLConstants.NS_SML;
        } else {
            outputFormat = response.getOutputFormat();
        }
        IEncoder encoder = Configurator.getInstance().getEncoder(outputFormat);
        if (encoder != null) {
            XmlObject xmlObject = (XmlObject) encoder.encode(response.getSensorDescription());
            describeSensorResponse.addNewDescription().addNewSensorDescription().addNewData().set(xmlObject);
            // set schema location
            List<String> schemaLocations = new ArrayList<String>();
            schemaLocations.add(N52XmlHelper.getSchemaLocationForSWES200());
            N52XmlHelper.setSchemaLocationsToDocument(xbDescSensorRespDoc, schemaLocations);
            return xbDescSensorRespDoc;
        }
        String exceptionText = "Error while encoding DescribeSensor response, missing encoder!";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

    private XmlObject createInsertSensorResponse(InsertSensorResponse response) {
        InsertSensorResponseDocument xbInsSenRespDoc =
                InsertSensorResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        InsertSensorResponseType xbInsSenResp = xbInsSenRespDoc.addNewInsertSensorResponse();
        xbInsSenResp.setAssignedProcedure(response.getAssignedProcedure());
        xbInsSenResp.setAssignedOffering(response.getAssignedOffering());
        // set schema location
        List<String> schemaLocations = new ArrayList<String>();
        schemaLocations.add(N52XmlHelper.getSchemaLocationForSWES200());
        N52XmlHelper.setSchemaLocationsToDocument(xbInsSenRespDoc, schemaLocations);
        return xbInsSenRespDoc;
    }

    private XmlObject createUpdateSensorResponse(UpdateSensorResponse response) {
        UpdateSensorDescriptionResponseDocument xbUpSenRespDoc =
                UpdateSensorDescriptionResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance()
                        .getXmlOptions());
        UpdateSensorDescriptionResponseType xbUpSenResp = xbUpSenRespDoc.addNewUpdateSensorDescriptionResponse();
        xbUpSenResp.setUpdatedProcedure(response.getUpdatedProcedure());
        // set schema location
        List<String> schemaLocations = new ArrayList<String>();
        schemaLocations.add(N52XmlHelper.getSchemaLocationForSWES200());
        N52XmlHelper.setSchemaLocationsToDocument(xbUpSenRespDoc, schemaLocations);
        return xbUpSenRespDoc;
    }

    private XmlObject createDeleteSensorResponse(DeleteSensorResponse response) {
        DeleteSensorResponseDocument xbDelSenRespDoc =
                DeleteSensorResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        DeleteSensorResponseType xbDelSenResp = xbDelSenRespDoc.addNewDeleteSensorResponse();
        xbDelSenResp.setDeletedProcedure(response.getDeletedProcedure());
        // set schema location
        List<String> schemaLocations = new ArrayList<String>();
        schemaLocations.add(N52XmlHelper.getSchemaLocationForSWES200());
        N52XmlHelper.setSchemaLocationsToDocument(xbDelSenRespDoc, schemaLocations);
        return xbDelSenRespDoc;
    }

}
