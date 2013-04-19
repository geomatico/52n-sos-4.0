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
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.config.annotation.Configurable;
import org.n52.sos.ds.hibernate.HibernateSessionHolder;
import org.n52.sos.ds.hibernate.entities.BlobObservation;
import org.n52.sos.ds.hibernate.entities.BooleanObservation;
import org.n52.sos.ds.hibernate.entities.CategoryObservation;
import org.n52.sos.ds.hibernate.entities.CountObservation;
import org.n52.sos.ds.hibernate.entities.GeometryObservation;
import org.n52.sos.ds.hibernate.entities.NumericObservation;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.TextObservation;
import org.n52.sos.ds.hibernate.entities.ValidProcedureTime;
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
import org.n52.sos.ogc.swe.simpleType.SosSweBoolean;
import org.n52.sos.ogc.swe.simpleType.SosSweCategory;
import org.n52.sos.ogc.swe.simpleType.SosSweCount;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.SensorDescriptionGenerationSettings;
import org.n52.sos.service.ServiceConfiguration;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.StringHelper;
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

	private static org.n52.sos.ogc.sensorML.System createSmlSystem(final Procedure procedure) throws OwsExceptionReport
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
		if (generationSettings().isGenerateClassification())
		{
			addClassifier(smlSystem);
		}
		
		// 6 set contacts --> take from service information?
		if (generationSettings().isUseServiceContactAsSensorContact())
		{
			final List<SmlContact> contacts = createContactFromServiceContact();
			if (contacts != null && !contacts.isEmpty()) 
			{
				smlSystem.setContact(contacts);
			}
		}
		
		// 7 set outputs --> observableProperties
		smlSystem.setOutputs(createOutputs(procedure,observableProperties));
		
		// 8 set position --> from procedure
		smlSystem.setPosition(createPosition(procedure));
		
		// 9 set observed area --> from features
		
		return smlSystem;
	}

	private static List<SosSMLIo<?>> createOutputs(final Procedure procedure, final String[] observableProperties) throws OwsExceptionReport
	{
		final ArrayList<SosSMLIo<?>> outputs = new ArrayList<SosSMLIo<?>>(observableProperties.length);
		int i = 1;
		for (final String observableProperty : observableProperties)
		{
			Observation exampleObservation;
			exampleObservation = getExampleObservation(procedure.getIdentifier(),observableProperty);
			if (exampleObservation == null)
			{
				LOGGER.debug("Could not receive example observation from database for procedure '{}' observing property '{}'.",
						procedure.getIdentifier(),
						observableProperty);
				continue;
			}
			SosSMLIo<?> output = null;
			if (exampleObservation instanceof BlobObservation)
			{
				// TODO implement BlobObservations
				LOGGER.debug("Type '{}' is not supported by the current implementation",BlobObservation.class.getName());
				continue;
			}
			else if (exampleObservation instanceof BooleanObservation)
			{
				final SosSweBoolean bool = new SosSweBoolean();
				bool.setDefinition(observableProperty);
				output = new SosSMLIo<Boolean>(bool);
			}
			else if (exampleObservation instanceof CategoryObservation)
			{
				final SosSweCategory category = new SosSweCategory();
				category.setDefinition(observableProperty);
				output = new SosSMLIo<String>(category);
			}
			else if (exampleObservation instanceof CountObservation)
			{
				final SosSweCount count = new SosSweCount();
				count.setDefinition(observableProperty);
				output = new SosSMLIo<Integer>(count);
			}
			else if (exampleObservation instanceof GeometryObservation)
			{
				// TODO implement GeometryObservations
				LOGGER.debug("Type '{}' is not supported by the current implementation",GeometryObservation.class.getName());
				continue;
			}
			else if (exampleObservation instanceof NumericObservation)
			{
				final SosSweQuantity quantity = new SosSweQuantity();
				quantity.setDefinition(observableProperty);
				output = new SosSMLIo<Double>(quantity);
			}
			else if (exampleObservation instanceof TextObservation)
			{
				final SosSweText text = new SosSweText();
				text.setDefinition(observableProperty);
				output = new SosSMLIo<String>(text);
			}
			if (output != null)
			{
				output.setIoName("output#"+i++);
				outputs.add(output);
			}
		}
		return outputs;
	}

	private static Observation getExampleObservation(final String identifier,
			final String observableProperty) throws OwsExceptionReport
	{
		final HibernateSessionHolder sessionHolder = new HibernateSessionHolder();
		Session session = null;
		try
		{
			session = sessionHolder.getSession();
			final String[] procedures = new String[1];
			procedures[0] = identifier;
			final String[] observableProperties = new String[1];
			observableProperties[0] = observableProperty;
			final Criteria c = session.createCriteria(Observation.class)
					.add(Restrictions.eq(Observation.DELETED, false))
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			c.createCriteria(Observation.OBSERVABLE_PROPERTY)
			.add(Restrictions.in(ObservableProperty.IDENTIFIER, observableProperties));
			c.createCriteria(Observation.PROCEDURE)
			.add(Restrictions.in(Procedure.IDENTIFIER, procedures));
			c.setMaxResults(1);
			return (Observation) c.list().get(0);
		} 
		catch (final HibernateException he) 
		{
			throw new NoApplicableCodeException().causedBy(he)
			.withMessage("Error while querying observation data!")
			.setStatus(INTERNAL_SERVER_ERROR);
		} 
		finally
		{
			sessionHolder.returnSession(session);
		}
	}

	private static SosSMLPosition createPosition(final Procedure procedure)
	{
		SosSMLPosition smlPosition = null;
		smlPosition = new SosSMLPosition();
                smlPosition.setName("sensorPosition");
                smlPosition.setFixed(true);
        int srid = 4326;
		// 8.1 set latlong position
		if (procedure.isSetLongLat())
		{
			smlPosition.setPosition(createCoordinatesForPosition(procedure.getLongitude(), procedure.getLatitude(), procedure.getAltitude()));
			
		}
		// 8.2 set position from geometry
		else if (procedure.isSetGeometry())
		{
		    if (procedure.getGeom().getSRID() > 0)
		    {
		        srid = procedure.getGeom().getSRID();
		    }
		    final Coordinate coordinate = procedure.getGeom().getCoordinate();
		    smlPosition.setPosition(createCoordinatesForPosition(coordinate.y, coordinate.x, coordinate.z));
		}
		if (procedure.isSetSrid())
		{
		    srid = procedure.getSrid();
		}
 		smlPosition.setReferenceFrame(getServiceConfig().getSrsNamePrefixSosV2() + srid);
		return smlPosition;
	}
	
	private static List<SosSweCoordinate<?>> createCoordinatesForPosition(final Object longitude, final Object latitude,
            final Object oAltitude) {
                final List<SosSweCoordinate<?>> sweCoordinates = new ArrayList<SosSweCoordinate<?>>(3);
                if (latitude instanceof Double)
                {
                        final SosSweQuantity quantity = new SosSweQuantity();
                        quantity.setValue((Double)latitude);
                        quantity.setAxisID("y");
                        quantity.setUom(generationSettings().getLatitudeUom());
                        sweCoordinates.add(new SosSweCoordinate<Double>(northing,quantity));
                }
                if (longitude instanceof Double)
                {
                        final SosSweQuantity quantity = new SosSweQuantity();
                        quantity.setValue((Double)longitude);
                        quantity.setAxisID("x");
                        quantity.setUom(generationSettings().getLongitudeUom());
                        sweCoordinates.add(new SosSweCoordinate<Double>(easting,quantity));
                }
                if (oAltitude instanceof Double)
                {
                        final SosSweQuantity quantity = new SosSweQuantity();
                        quantity.setValue((Double)oAltitude);
                        quantity.setAxisID("z");
                        quantity.setUom(generationSettings().getAltitudeUom());
                        sweCoordinates.add(new SosSweCoordinate<Double>(altitude,quantity));
                }
                // TODO add Integer: Which SweSimpleType to use?
                return sweCoordinates;
    }

	private static List<SmlContact> createContactFromServiceContact()
	{
		final SmlResponsibleParty smlRespParty = new SmlResponsibleParty();
		SosServiceProvider serviceProvider = null;
		try
		{
			serviceProvider = Configurator.getInstance().getServiceProvider();
		} 
		catch (final OwsExceptionReport e) {
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
		if (!generationSettings().getClassifierIntendedApplicationValue().isEmpty())
		{
			smlSystem.addClassification(new SosSMLClassifier(
					"intendedApplication",
					generationSettings().getClassifierIntendedApplicationDefinition(),
					generationSettings().getClassifierIntendedApplicationValue()));
		}
		if (!generationSettings().getClassifierSensorTypeValue().isEmpty())
		{
			smlSystem.addClassification(new SosSMLClassifier(
					"sensorType",
					generationSettings().getClassifierSensorTypeDefinition(),
					generationSettings().getClassifierSensorTypeValue()));
		}
	}

	private static List<String> createDescriptions(final Procedure procedure,
			final String[] observableProperties)
	{
		return CollectionHelper.list(String.format(generationSettings().getDescriptionTemplate(), 
				procedure.getIdentifier(), 
				StringHelper.join(",", CollectionHelper.list(observableProperties))));
	}

	private static List<SosSMLIdentifier> createIdentifications(final String identifier)
	{
		// get long and short name definition from misc settings
		final SosSMLIdentifier idShortName = new SosSMLIdentifier("shortname", generationSettings().getIdentifierShortNameDefinition(), identifier);
		final SosSMLIdentifier idLongName = new SosSMLIdentifier("longname", generationSettings().getIdentifierLongNameDefinition(), identifier);
		return CollectionHelper.list(idLongName,idShortName);
	}

	private static ServiceConfiguration getServiceConfig()
	{
		return Configurator.getInstance().getServiceConfiguration();
	}
	
	private static SensorDescriptionGenerationSettings generationSettings()
	{
		return SensorDescriptionGenerationSettings.getInstance();
	}

	private static List<String> createKeywordsList(final Procedure procedure,
			final String[] observableProperties)
	{
		final List<String> keywords = CollectionHelper.list();
		keywords.addAll(CollectionHelper.list(observableProperties));
		keywords.add(procedure.getIdentifier());
		if (generationSettings().isGenerateClassification() && 
				!generationSettings().getClassifierIntendedApplicationValue().isEmpty())
		{
			keywords.add(generationSettings().getClassifierIntendedApplicationValue());
		}
		if (generationSettings().isGenerateClassification() && 
				!generationSettings().getClassifierSensorTypeValue().isEmpty())
		{
			keywords.add(generationSettings().getClassifierSensorTypeValue());
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
	
    private static InputStream getDescribeSensorDocumentAsStream(String filename) {
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
