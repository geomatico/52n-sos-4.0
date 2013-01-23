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
package org.n52.sos.ds.hibernate.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ValidProcedureTime;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.sos.SosProcedureDescriptionUnknowType;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateProcedureUtilities {
    
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateProcedureUtilities.class);

    
    public static SosProcedureDescription createSosProcedureDescription(Procedure procedure, String procedureIdentifier, String outputFormat) throws OwsExceptionReport {
        String filename = null;
        String smlFile = null;
        String descriptionFormat;
        
        // TODO: check and query for validTime parameter
        Set<ValidProcedureTime> validProcedureTimes = procedure.getValidProcedureTimes();
        for (ValidProcedureTime validProcedureTime : validProcedureTimes) {
            if (validProcedureTime.getEndTime() == null) {
                filename = validProcedureTime.getDescriptionUrl();
                smlFile = validProcedureTime.getDescriptionXml();
            }
        }
        descriptionFormat = procedure.getProcedureDescriptionFormat().getProcedureDescriptionFormat();
        SosProcedureDescription sosProcedureDescription = null;
        // check whether SMLFile or Url is set
        if (filename == null && smlFile == null) {
            String exceptionText = "No sensorML file was found for the requested procedure " + procedureIdentifier;
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(
                    SosConstants.DescribeSensorParams.procedure.toString(), exceptionText);

        } else {
            try {
                if (filename != null && descriptionFormat != null && smlFile == null) {
                    // return sensorML from folder

                    if (!descriptionFormat.equalsIgnoreCase(outputFormat)
                            && !descriptionFormat.equalsIgnoreCase(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE)) {
                        String exceptionText =
                                "The value of the output format is wrong and has to be " + descriptionFormat
                                        + " for procedure " + procedureIdentifier;
                        LOGGER.error(exceptionText);
                        throw Util4Exceptions.createInvalidParameterValueException(
                                SosConstants.DescribeSensorParams.procedure.toString(), exceptionText);
                    }
                    File sensorFile;
                    LOGGER.info(filename);
                    // read in the description file
                    if (filename.startsWith("standard/")) {
                        filename = filename.replace("standard/", "");
                        sensorFile = new File(Configurator.getInstance().getSensorDir(), filename);
                    } else {
                        sensorFile = new File(filename);
                    }
                    XmlObject procedureDescription = XmlObject.Factory.parse(sensorFile);
                    try {
                        sosProcedureDescription = (SosProcedureDescription) CodingHelper.decodeXmlElement(procedureDescription);
                    } catch (OwsExceptionReport owse) {
                        sosProcedureDescription = new SosProcedureDescriptionUnknowType(procedureIdentifier, outputFormat,
                                procedureDescription.xmlText());
                    }
                } else {
                    XmlObject procedureDescription = XmlObject.Factory.parse(smlFile);
                    try {
                        sosProcedureDescription = (SosProcedureDescription) CodingHelper.decodeXmlElement(procedureDescription);
                    } catch (OwsExceptionReport owse) {
                        sosProcedureDescription = new SosProcedureDescriptionUnknowType(procedureIdentifier, outputFormat,
                                procedureDescription.xmlText());
                    }
                }
                if (sosProcedureDescription != null) {
                    sosProcedureDescription.setDescriptionFormat(descriptionFormat);
                }
                return sosProcedureDescription;
            } catch (FileNotFoundException fnfe) {
                String exceptionText = "No sensorML file was found for the requested procedure " + procedureIdentifier;
                LOGGER.error(exceptionText, fnfe);
                throw Util4Exceptions.createInvalidParameterValueException(
                        SosConstants.DescribeSensorParams.procedure.toString(), exceptionText);
            } catch (IOException ioe) {
                String exceptionText = "An error occured while parsing the sensor description document!";
                LOGGER.error(exceptionText, ioe);
                throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
            } catch (XmlException xmle) {
                String exceptionText = "An error occured while parsing the sensor description document!";
                LOGGER.error(exceptionText, xmle);
                throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
            }
        }
    }

}
