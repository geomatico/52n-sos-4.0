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

import static org.n52.sos.util.HTTPConstants.StatusCode.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ValidProcedureTime;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.XmlDecodingException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.sos.SosProcedureDescriptionUnknowType;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CodingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateProcedureUtilities {
    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateProcedureUtilities.class);

    public static SosProcedureDescription createSosProcedureDescription(final Procedure procedure,
                                                                        final String procedureIdentifier,
                                                                        final String outputFormat)
            throws OwsExceptionReport {
        String filename = null;
        String xmlDoc = null;
        SosProcedureDescription sosProcedureDescription = null;

        // TODO: check and query for validTime parameter
        final Set<ValidProcedureTime> validProcedureTimes = procedure.getValidProcedureTimes();
        for (final ValidProcedureTime validProcedureTime : validProcedureTimes) 
        {
            if (validProcedureTime.getEndTime() == null) 
            {
                filename = validProcedureTime.getDescriptionUrl();
                xmlDoc = validProcedureTime.getDescriptionXml();
            }
        }
        
        final String descriptionFormat = procedure.getProcedureDescriptionFormat().getProcedureDescriptionFormat();
        // check whether SMLFile or Url is set
        if (filename == null && xmlDoc == null) 
        {
        	/*
        	 * TODO Eike: generate SensorML file from available parameters
        	 * 
        	 *  1 get observable properties from cache for procedure identifier
        	 *  
        	 *  2 try to get position from entity (which one?) HasCoordinates, HasGeometry
        	 *  
        	 *  2.1 if no position is available -> processModel -> own class
        	 *  
        	 *  2.2 if position is available -> system -> own class
        	 */
            throw new InvalidParameterValueException().at(SosConstants.DescribeSensorParams.procedure)
                    .withMessage("No sensorML file was found for the requested procedure %s", procedureIdentifier)
                    .setStatus(INTERNAL_SERVER_ERROR);
        }
        else 
        {
            try
            {
                if (filename != null && descriptionFormat != null && xmlDoc == null) 
                {
                    sosProcedureDescription = createProcedureDescriptionFromFile(procedureIdentifier, outputFormat, filename, descriptionFormat);
                }
                else
                {
                    sosProcedureDescription = createProcedureDescriptionFromXml(procedureIdentifier, outputFormat, xmlDoc);
                }
                if (sosProcedureDescription != null) 
                {
                    sosProcedureDescription.setDescriptionFormat(descriptionFormat);
                }
                return sosProcedureDescription;
            }
            catch (final IOException ioe)
            {
                throw new NoApplicableCodeException().causedBy(ioe)
                        .withMessage("An error occured while parsing the sensor description document!")
                        .setStatus(INTERNAL_SERVER_ERROR);
            } 
            catch (final XmlException xmle) 
            {
                throw new XmlDecodingException("sensor description document", xmlDoc, xmle)
                		.setStatus(INTERNAL_SERVER_ERROR);
            }
        }
    }

	private static SosProcedureDescription createProcedureDescriptionFromXml(final String procedureIdentifier,
			final String outputFormat,
			final String xmlDoc) throws XmlException
	{
		final XmlObject procedureDescription = XmlObject.Factory.parse(xmlDoc);
		SosProcedureDescription sosProcedureDescription;
		try {
		    sosProcedureDescription =
		            (SosProcedureDescription) CodingHelper.decodeXmlElement(procedureDescription);
		    sosProcedureDescription.setIdentifier(procedureIdentifier);
		} catch (final OwsExceptionReport owse) {
		    sosProcedureDescription =
		            new SosProcedureDescriptionUnknowType(procedureIdentifier, outputFormat,
		                    procedureDescription.xmlText());
		}
		return sosProcedureDescription;
	}

	private static SosProcedureDescription createProcedureDescriptionFromFile(final String procedureIdentifier,
			final String outputFormat,
			final String filename,
			final String descriptionFormat) throws OwsExceptionReport, XmlException, IOException
	{
		if (!descriptionFormat.equalsIgnoreCase(outputFormat)
		    && !descriptionFormat.equalsIgnoreCase(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE)) 
		{
		    throw new InvalidParameterValueException()
		    		.at(SosConstants.DescribeSensorParams.procedure)
		            .withMessage("The value of the output format is wrong and has to be %s for procedure %s",
		                         descriptionFormat, procedureIdentifier)
		            .setStatus(BAD_REQUEST);
		}

		// check if filename contains placeholder for configured
		// sensor directory
		final XmlObject procedureDescription =
		        XmlObject.Factory.parse(getDescribeSensorDocumentAsStream(filename));
		SosProcedureDescription sosProcedureDescription;
		try {
		    sosProcedureDescription =
		            (SosProcedureDescription) CodingHelper.decodeXmlElement(procedureDescription);
		    sosProcedureDescription.setIdentifier(procedureIdentifier);
		} catch (final OwsExceptionReport owse) {
		    sosProcedureDescription =
		            new SosProcedureDescriptionUnknowType(procedureIdentifier, outputFormat,
		                    procedureDescription.xmlText());
		}
		return sosProcedureDescription;
	}

    public static InputStream getDescribeSensorDocumentAsStream(String filename) {
        final StringBuilder builder = new StringBuilder();
        if (filename.startsWith("standard")) {
            filename = filename.replace("standard", "");
            builder.append(Configurator.getInstance().getSensorDir());
            builder.append("/");
        }
        builder.append(filename);
        LOGGER.debug("Procedure description file name '{}'!", filename);
        return Configurator.getInstance().getClass().getResourceAsStream(builder.toString());
    }

    private HibernateProcedureUtilities() {
    }
}
