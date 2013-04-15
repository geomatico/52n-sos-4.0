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

import static org.n52.sos.ogc.swe.SWEConstants.SweCoordinateName.*;
import static org.n52.sos.util.HTTPConstants.StatusCode.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.config.annotation.Configurable;
import org.n52.sos.config.annotation.Setting;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ValidProcedureTime;
import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.XmlDecodingException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosServiceProvider;
import org.n52.sos.ogc.sensorML.ProcessModel;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.SmlContact;
import org.n52.sos.ogc.sensorML.SmlResponsibleParty;
import org.n52.sos.ogc.sensorML.System;
import org.n52.sos.ogc.sensorML.elements.SosSMLClassifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLIo;
import org.n52.sos.ogc.sensorML.elements.SosSMLPosition;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.sos.SosProcedureDescriptionUnknowType;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConfiguration;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;

// TODO Eike: move all strings to constants classes or create settings for them
@Configurable
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
        	final SensorML sml = new SensorML();
        	sml.setVersion(SensorMLConstants.VERSION_V101); // TODO should this be configurable?
        	
        	// 2 try to get position from entity
        	if (procedure.isSpatial())
        	{
        		// 2.1 if position is available -> system -> own class <- should be compliant with SWE lightweight profile
        		sml.addMember(createSmlSystem(procedure));
        	}
        	else
        	{
        		// 2.2 if no position is available -> processModel -> own class
        		sml.addMember(createSmlProcessModel(procedure));
        	}
        	sosProcedureDescription = sml;
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
        if (sosProcedureDescription != null) 
        {
        	sosProcedureDescription.setDescriptionFormat(descriptionFormat);
        }
        return sosProcedureDescription;
    }

	/**
	 * 
	 * @param procedure
	 * @param observableProperties
	 * @return
	 */
	private static ProcessModel createSmlProcessModel(final Procedure procedure)
	{
		// TODO Auto-generated method "createSmlProcessModel" stub generated on 10.04.2013 around 12:36:39 by eike
		return new ProcessModel();
	}

	private static org.n52.sos.ogc.sensorML.System createSmlSystem(final Procedure procedure)
	{
		final System smlSystem = new System();
		
    	final String[] observableProperties = getObservablePropertiesForProcedure(procedure.getIdentifier());
		
		// 1 set description
		smlSystem.setDescriptions(createDescriptions(procedure,observableProperties));
		
		// 2 identifier 
		smlSystem.setIdentifier(procedure.getIdentifier());
		
		// 3 set identification
		smlSystem.setIdentifications(createIdentifications(procedure.getIdentifier()));
		
		// 4 set keywords
		smlSystem.setKeywords(createKeywordsList(procedure,observableProperties));
		
		// 5 set classification
		if (getServiceConfig().isSmlGenerationGenerateClassification())
		{
			addClassifier(smlSystem);
		}
		
		// 6 set contacts --> take from service information?
		if (getServiceConfig().isSmlGenerationUseServiceContactAsSensorContact())
		{
			final List<SmlContact> contacts = createContactFromServiceContact();
			if (contacts != null && !contacts.isEmpty()) 
			{
				smlSystem.setContact(contacts);
			}
		}
		
		// 7 set outputs --> observableProperties
		// TODO where to get the type from
		smlSystem.setOutputs(createOutputs(observableProperties));
		
		
		// 8 set position --> from procedure
		smlSystem.setPosition(createPosition(procedure));
		
		// 9 set observed area --> from features
		
		// TODO Eike: continue implementation here
		return smlSystem;
	}

	private static List<SosSMLIo<?>> createOutputs(final String[] observableProperties)
	{
		final ArrayList<SosSMLIo<?>> outputs = new ArrayList<SosSMLIo<?>>(observableProperties.length);
		// FIXME how to determine the type of the output from sensor id and observable property?
		int i = 1;
		for (final String observableProperty : observableProperties)
		{
			final SosSweQuantity quantity = new SosSweQuantity();
			quantity.setDefinition(observableProperty);
            final SosSMLIo<Double> output = new SosSMLIo<Double>(quantity);
			output.setIoName("output#"+i++);
			outputs.add(output);
		}
		return outputs;
	}

	private static SosSMLPosition createPosition(final Procedure procedure)
	{
		// TODO Auto-generated method "createPosition" stub generated on 12.04.2013 around 15:08:04 by eike
		SosSMLPosition smlPosition = null;
		smlPosition = new SosSMLPosition();
                smlPosition.setName("sensorPosition");
                smlPosition.setFixed(true);
                int srid = 4326; // TODO use default EPSG code from settings
		// 8.1 set latlong position
		if (procedure.isSetLongLat())
		{
			smlPosition.setPosition(createCoordinatesForPosition(procedure.getLongitude(), procedure.getLatitude(), procedure.getAltitude()));
			
		}
		// 8.2 set position from geometry
		else if (procedure.isSetGeometry())
		{
		    if (procedure.getGeom().getSRID() > 0) {
		        srid = procedure.getGeom().getSRID();
		    }
			// TODO implement
		    Coordinate coordinate = procedure.getGeom().getCoordinate();
		    smlPosition.setPosition(createCoordinatesForPosition(coordinate.y, coordinate.x, coordinate.z));
		}
		if (procedure.isSetSrid()) {
		    srid = procedure.getSrid();
		}
 		smlPosition.setReferenceFrame(getServiceConfig().getSrsNamePrefixSosV2() + srid);
		return smlPosition;
	}
	
	private static List<SosSweCoordinate<?>> createCoordinatesForPosition(Object longitude, Object latitude,
            Object oAltitude) {
                final List<SosSweCoordinate<?>> sweCoordinates = new ArrayList<SosSweCoordinate<?>>(3);
                if (latitude instanceof Double)
                {
                        final SosSweQuantity quantity = new SosSweQuantity();
                        quantity.setValue((Double)latitude);
                        quantity.setAxisID("y");
                        quantity.setUom("degree"); // TODO add to mapping or setting
                        sweCoordinates.add(new SosSweCoordinate<Double>(northing,quantity));
                }
                if (longitude instanceof Double)
                {
                        final SosSweQuantity quantity = new SosSweQuantity();
                        quantity.setValue((Double)longitude);
                        quantity.setAxisID("x");
                        quantity.setUom("degree"); // TODO add to mapping or setting
                        sweCoordinates.add(new SosSweCoordinate<Double>(easting,quantity));
                }
                if (oAltitude != null && oAltitude instanceof Double)
                {
                        final SosSweQuantity quantity = new SosSweQuantity();
                        quantity.setValue((Double)oAltitude);
                        quantity.setAxisID("z");
                        quantity.setUom("m"); // TODO add to mapping or setting
                        sweCoordinates.add(new SosSweCoordinate<Double>(altitude,quantity));
                }
                return sweCoordinates;
    }

	private static List<SmlContact> createContactFromServiceContact()
	{
		final SmlResponsibleParty smlRespParty = new SmlResponsibleParty();
		SosServiceProvider serviceProvider = null;
		try {
			serviceProvider = Configurator.getInstance().getServiceProvider();
		} catch (final OwsExceptionReport e) {
			LOGGER.error(String.format("Exception thrown: %s",
						e.getMessage()),
					e);
		}
		if (serviceProvider == null)
		{
			return null;
		}
		smlRespParty.setIndividualName(serviceProvider.getIndividualName());
		smlRespParty.setOrganizationName(serviceProvider.getName());
		smlRespParty.addOnlineResource(serviceProvider.getSite());
		smlRespParty.setPositionName(serviceProvider.getPositionName());
		smlRespParty.addDeliveryPoint(serviceProvider.getDeliveryPoint());
		smlRespParty.addPhoneVoice(serviceProvider.getPhone());
		smlRespParty.setCity(serviceProvider.getCity());
		smlRespParty.setCountry(serviceProvider.getCountry());
		smlRespParty.setPostalCode(serviceProvider.getPostalCode());
		smlRespParty.setEmail(serviceProvider.getMailAddress());
		return CollectionHelper.list((SmlContact)smlRespParty);
	}

	private static void addClassifier(final System smlSystem)
	{
		if (!getServiceConfig().getSmlGenerationClassifierIntendedApplicationValue().isEmpty())
		{
			smlSystem.addClassification(new SosSMLClassifier(
					"intendedApplication",
					getServiceConfig().getSmlGenerationClassifierIntendedApplicationDefinition(),
					getServiceConfig().getSmlGenerationClassifierIntendedApplicationValue()));
		}
		if (!getServiceConfig().getSmlGenerationClassifierSensorTypeValue().isEmpty())
		{
			smlSystem.addClassification(new SosSMLClassifier(
					"sensorType",
					getServiceConfig().getSmlGenerationClassifierSensorTypeDefinition(),
					getServiceConfig().getSmlGenerationClassifierSensorTypeValue()));
		}
	}

	private static List<String> createDescriptions(final Procedure procedure,
			final String[] observableProperties)
	{
		return CollectionHelper.list(String.format(getServiceConfig().getSmlGenerationDescriptionTemplate(), 
				procedure.getIdentifier(), 
				StringHelper.join(",", CollectionHelper.list(observableProperties))));
	}

	private static List<SosSMLIdentifier> createIdentifications(final String identifier)
	{
		// get long and short name definition from misc settings
		final SosSMLIdentifier idShortName = new SosSMLIdentifier("shortname", getServiceConfig().getSmlGenerationIdentifierShortNameDefinition(), identifier);
		final SosSMLIdentifier idLongName = new SosSMLIdentifier("longname", getServiceConfig().getSmlGenerationIdentifierLongNameDefinition(), identifier);
		return CollectionHelper.list(idLongName,idShortName);
	}

	private static ServiceConfiguration getServiceConfig()
	{
		return Configurator.getInstance().getServiceConfiguration();
	}

	private static List<String> createKeywordsList(final Procedure procedure,
			final String[] observableProperties)
	{
		final List<String> keywords = CollectionHelper.list();
		keywords.addAll(CollectionHelper.list(observableProperties));
		keywords.add(procedure.getIdentifier());
		if (getServiceConfig().isSmlGenerationGenerateClassification() && 
				!getServiceConfig().getSmlGenerationClassifierIntendedApplicationValue().isEmpty())
		{
			keywords.add(getServiceConfig().getSmlGenerationClassifierIntendedApplicationValue());
		}
		if (getServiceConfig().isSmlGenerationGenerateClassification() && 
				!getServiceConfig().getSmlGenerationClassifierSensorTypeValue().isEmpty())
		{
			keywords.add(getServiceConfig().getSmlGenerationClassifierSensorTypeValue());
		}
		return keywords;
	}

	private static String[] getObservablePropertiesForProcedure(final String procedureIdentifier)
	{
		return Configurator.getInstance().getCache().getObservablePropertiesForProcedure(procedureIdentifier).toArray(new String[0]);
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
