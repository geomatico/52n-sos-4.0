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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
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
import org.n52.sos.ds.hibernate.entities.TProcedure;
import org.n52.sos.ds.hibernate.entities.TextObservation;
import org.n52.sos.ds.hibernate.entities.ValidProcedureTime;
import org.n52.sos.exception.CodedException;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.CodeType;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosServiceProvider;
import org.n52.sos.ogc.sensorML.AbstractProcess;
import org.n52.sos.ogc.sensorML.AbstractSensorML;
import org.n52.sos.ogc.sensorML.ProcessMethod;
import org.n52.sos.ogc.sensorML.ProcessModel;
import org.n52.sos.ogc.sensorML.RulesDefinition;
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
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweBoolean;
import org.n52.sos.ogc.swe.simpleType.SosSweCategory;
import org.n52.sos.ogc.swe.simpleType.SosSweCount;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ProcedureDescriptionSettings;
import org.n52.sos.service.ServiceConfiguration;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.JavaHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;

public class HibernateProcedureConverter {
    private final Logger LOGGER = LoggerFactory.getLogger(HibernateProcedureConverter.class);

    public SosProcedureDescription createSosProcedureDescription(final Procedure procedure,
            final String procedureIdentifier, final String outputFormat) throws OwsExceptionReport {
        if (procedure == null) {
            throw new NoApplicableCodeException().causedBy(
                    new IllegalArgumentException("Parameter 'procedure' should not be null!")).setStatus(
                    INTERNAL_SERVER_ERROR);
        }
        String filename = null;
        String xmlDoc = null;
        SosProcedureDescription sosProcedureDescription = null;

        // TODO: check and query for validTime parameter
        if (procedure instanceof TProcedure) {
            final Set<ValidProcedureTime> validProcedureTimes = ((TProcedure)procedure).getValidProcedureTimes();
            for (final ValidProcedureTime validProcedureTime : validProcedureTimes) {
                if (validProcedureTime.getEndTime() == null) {
                    xmlDoc = validProcedureTime.getDescriptionXml();
                }
            }
        } else {
            filename = procedure.getDescriptionFile();
        }

        final String descriptionFormat = procedure.getProcedureDescriptionFormat().getProcedureDescriptionFormat();
        checkOutputFormatWithDescriptionFormat(outputFormat, procedure.getProcedureDescriptionFormat().getProcedureDescriptionFormat(), procedureIdentifier);
        // check whether SMLFile or Url is set
        if (filename == null && xmlDoc == null) {
            final SensorML sml = new SensorML();

            // 2 try to get position from entity
            if (procedure.isSpatial()) {
                // 2.1 if position is available -> system -> own class <- should
                // be compliant with SWE lightweight profile
                sml.addMember(createSmlSystem(procedure));
            } else {
                // 2.2 if no position is available -> processModel -> own class
                sml.addMember(createSmlProcessModel(procedure));
            }
            sosProcedureDescription = sml;
        } else {
//            try {
                if (filename != null && descriptionFormat != null && xmlDoc == null) {
                    if (filename.startsWith("<")) {
                        sosProcedureDescription =
                                createProcedureDescriptionFromXml(procedureIdentifier, filename);
                    } else {
                        sosProcedureDescription =
                                createProcedureDescriptionFromFile(procedureIdentifier, filename);
                    }
                } else {
                    sosProcedureDescription =
                            createProcedureDescriptionFromXml(procedureIdentifier, xmlDoc);
                }
//            } catch (final IOException ioe) {
//                throw new NoApplicableCodeException().causedBy(ioe)
//                        .withMessage("An error occured while parsing the sensor description document!")
//                        .setStatus(INTERNAL_SERVER_ERROR);
//            } catch (final XmlException xmle) {
//                throw new XmlDecodingException("sensor description document", xmle)
//                        .setStatus(INTERNAL_SERVER_ERROR);
//            }
        }
        if (sosProcedureDescription != null) {
            sosProcedureDescription.setDescriptionFormat(descriptionFormat);
        }
        return sosProcedureDescription;
    }

    private void checkOutputFormatWithDescriptionFormat(final String outputFormat, final String procedureDescriptionFormat,
            final String procedureIdentifier) throws OwsExceptionReport {

        if (StringHelper.isNullOrEmpty(procedureDescriptionFormat) || (!procedureDescriptionFormat.equalsIgnoreCase(outputFormat)
                && !procedureDescriptionFormat.equalsIgnoreCase(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE))) {
            throw new InvalidParameterValueException()
                    .at(SosConstants.DescribeSensorParams.procedure)
                    .withMessage("The value of the output format is wrong and has to be %s for procedure %s",
                            procedureDescriptionFormat, procedureIdentifier).setStatus(BAD_REQUEST);
        } else {
            SosHelper.checkProcedureDescriptionFormat(procedureDescriptionFormat, "ProcedureDescriptionFormatFromDataSource");
        }
    }

    private ProcessModel createSmlProcessModel(final Procedure procedure) throws OwsExceptionReport {
        final ProcessModel smlProcessModel = new ProcessModel();

        setCommonValues(procedure, smlProcessModel);
        
        smlProcessModel.setMethod(createMethod(procedure, getObservablePropertiesForProcedure(procedure.getIdentifier())));
        smlProcessModel.setNames(createNames(procedure));
        
        return smlProcessModel;
    }

    private ProcessMethod createMethod(final Procedure procedure, final String[] observableProperties) {
        final ProcessMethod pM = new ProcessMethod(createRulesDefinition(procedure, observableProperties));
        return pM;
    }

    private RulesDefinition createRulesDefinition(final Procedure procedure, final String[] observableProperties) {
        final RulesDefinition rD = new RulesDefinition();
        final String description =
                String.format(generationSettings().getProcessMethodRulesDefinitionDescriptionTemplate(),
                        procedure.getIdentifier(), StringHelper.join(",", CollectionHelper.list(observableProperties)));
        rD.setDescription(description);
        return rD;
    }

    private List<CodeType> createNames(final Procedure procedure) {
        return CollectionHelper.asList(new CodeType(procedure.getIdentifier()));
    }

    private System createSmlSystem(final Procedure procedure) throws OwsExceptionReport {
        final System smlSystem = new System();

        setCommonValues(procedure, smlSystem);

        // 8 set position --> from procedure
        smlSystem.setPosition(createPosition(procedure));

        // 9 set observed area --> from features
        // TODO implement generation of observed area from available features
        return smlSystem;
    }

	private void setCommonValues(final Procedure procedure,
			final AbstractProcess abstractSensorML) throws OwsExceptionReport
	{
		final String[] observableProperties = getObservablePropertiesForProcedure(procedure.getIdentifier());

        // 1 set description
        abstractSensorML.setDescriptions(createDescriptions(procedure, observableProperties));

        // 2 identifier
        abstractSensorML.setIdentifier(procedure.getIdentifier());

        // 3 set identification
        abstractSensorML.setIdentifications(createIdentifications(procedure.getIdentifier()));

        // 4 set keywords
        abstractSensorML.setKeywords(createKeywordsList(procedure, observableProperties));

        // 5 set classification
        if (generationSettings().isGenerateClassification()) {
            createClassifier(abstractSensorML);
        }

        // 6 set contacts --> take from service information?
        if (generationSettings().isUseServiceContactAsSensorContact()) {
            final List<SmlContact> contacts = createContactFromServiceContact();
            if (contacts != null && !contacts.isEmpty()) {
                abstractSensorML.setContact(contacts);
            }
        }

        // 7 set outputs --> observableProperties
        abstractSensorML.setOutputs(createOutputs(procedure, observableProperties));
	}

    private List<SosSMLIo<?>> createOutputs(final Procedure procedure, final String[] observableProperties)
            throws OwsExceptionReport {
        final ArrayList<SosSMLIo<?>> outputs = new ArrayList<SosSMLIo<?>>(observableProperties.length);
        int i = 1;
        for (final String observableProperty : observableProperties) {
            Observation exampleObservation;
            exampleObservation = getExampleObservation(procedure.getIdentifier(), observableProperty);
            if (exampleObservation == null) {
                LOGGER.debug(
                        "Could not receive example observation from database for procedure '{}' observing property '{}'.",
                        procedure.getIdentifier(), observableProperty);
                continue;
            }
            SosSMLIo<?> output = null;
            if (exampleObservation instanceof BlobObservation) {
                // TODO implement BlobObservations
                logTypeNotSupported(BlobObservation.class);
                continue;
            } else if (exampleObservation instanceof BooleanObservation) {
                final SosSweBoolean bool = new SosSweBoolean();
                bool.setDefinition(observableProperty);
                output = new SosSMLIo<Boolean>(bool);
            } else if (exampleObservation instanceof CategoryObservation) {
                final SosSweCategory category = new SosSweCategory();
                category.setDefinition(observableProperty);
                output = new SosSMLIo<String>(category);
            } else if (exampleObservation instanceof CountObservation) {
                final SosSweCount count = new SosSweCount();
                count.setDefinition(observableProperty);
                output = new SosSMLIo<Integer>(count);
            } else if (exampleObservation instanceof GeometryObservation) {
                // TODO implement GeometryObservations
                logTypeNotSupported(GeometryObservation.class);
                continue;
            } else if (exampleObservation instanceof NumericObservation) {
                final SosSweQuantity quantity = new SosSweQuantity();
                quantity.setDefinition(observableProperty);
                output = new SosSMLIo<Double>(quantity);
            } else if (exampleObservation instanceof TextObservation) {
                final SosSweText text = new SosSweText();
                text.setDefinition(observableProperty);
                output = new SosSMLIo<String>(text);
            }
            if (output != null) {
                output.setIoName("output#" + i++);
                outputs.add(output);
            }
        }
        return outputs;
    }

    private void logTypeNotSupported(final Class<?> clazz) {
        LOGGER.debug("Type '{}' is not supported by the current implementation", clazz.getName());
    }

    protected Observation getExampleObservation(final String identifier, final String observableProperty)
            throws OwsExceptionReport {
        final HibernateSessionHolder sessionHolder = new HibernateSessionHolder();
        Session session = null;
        try {
            session = sessionHolder.getSession();
            final Criteria c =
                    session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false))
                            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            c.createCriteria(Observation.OBSERVABLE_PROPERTY).add(
                    Restrictions.eq(ObservableProperty.IDENTIFIER, observableProperty));
            c.createCriteria(Observation.PROCEDURE).add(Restrictions.eq(Procedure.IDENTIFIER, identifier));
            c.setMaxResults(1);
            return (Observation) c.uniqueResult();
        } catch (final HibernateException he) {
            throw new NoApplicableCodeException().causedBy(he).withMessage("Error while querying observation data!")
                    .setStatus(INTERNAL_SERVER_ERROR);
        } finally {
            sessionHolder.returnSession(session);
        }
    }

    private SosSMLPosition createPosition(final Procedure procedure) {
        SosSMLPosition smlPosition = null;
        smlPosition = new SosSMLPosition();
        smlPosition.setName("sensorPosition");
        smlPosition.setFixed(true);
        int srid = 4326;
        // 8.1 set latlong position
        if (procedure.isSetLongLat()) {
            smlPosition.setPosition(createCoordinatesForPosition(procedure.getLongitude(), procedure.getLatitude(),
                    procedure.getAltitude()));

        }
        // 8.2 set position from geometry
        else if (procedure.isSetGeometry()) {
            if (procedure.getGeom().getSRID() > 0) {
                srid = procedure.getGeom().getSRID();
            }
            final Coordinate coordinate = procedure.getGeom().getCoordinate();
            smlPosition.setPosition(createCoordinatesForPosition(coordinate.y, coordinate.x, coordinate.z));
        }
        if (procedure.isSetSrid()) {
            srid = procedure.getSrid();
        }
        smlPosition.setReferenceFrame(getServiceConfig().getSrsNamePrefixSosV2() + srid);
        return smlPosition;
    }

    private List<SosSweCoordinate<?>> createCoordinatesForPosition(final Object longitude, final Object latitude,
            final Object oAltitude) {
        final List<SosSweCoordinate<?>> sweCoordinates = new ArrayList<SosSweCoordinate<?>>(3);
        sweCoordinates.add(new SosSweCoordinate<Double>(northing, createSweQuantity(JavaHelper.asDouble(latitude),
                "y", generationSettings().getLatLongUom())));
        sweCoordinates.add(new SosSweCoordinate<Double>(easting, createSweQuantity(JavaHelper.asDouble(longitude), "x",
                generationSettings().getLatLongUom())));
        sweCoordinates.add(new SosSweCoordinate<Double>(altitude, createSweQuantity(JavaHelper.asDouble(oAltitude),
                "z", generationSettings().getAltitudeUom())));
        // TODO add Integer: Which SweSimpleType to use?
        return sweCoordinates;
    }

    private SosSweAbstractSimpleType<Double> createSweQuantity(final Double value, final String asixID, final String uom) {
        final SosSweQuantity quantity = new SosSweQuantity();
        quantity.setValue(JavaHelper.asDouble(value));
        quantity.setAxisID(asixID);
        quantity.setUom(uom);
        return quantity;
    }

    private List<SmlContact> createContactFromServiceContact() {
        final SmlResponsibleParty smlRespParty = new SmlResponsibleParty();
        final SosServiceProvider serviceProvider = getServiceProvider();
        if (serviceProvider == null) {
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
        return CollectionHelper.list((SmlContact) smlRespParty);
    }

    protected SosServiceProvider getServiceProvider() {
        SosServiceProvider serviceProvider = null;
        try {
            serviceProvider = Configurator.getInstance().getServiceProvider();
        } catch (final OwsExceptionReport e) {
            LOGGER.error(String.format("Exception thrown: %s", e.getMessage()), e);
        }
        return serviceProvider;
    }

    private void createClassifier(final AbstractSensorML abstractSensorML) {
        if (!generationSettings().getClassifierIntendedApplicationValue().isEmpty()) {
            abstractSensorML.addClassification(new SosSMLClassifier("intendedApplication", generationSettings()
                    .getClassifierIntendedApplicationDefinition(), generationSettings()
                    .getClassifierIntendedApplicationValue()));
        }
        if (!generationSettings().getClassifierSensorTypeValue().isEmpty()) {
            abstractSensorML.addClassification(new SosSMLClassifier("sensorType", generationSettings()
                    .getClassifierSensorTypeDefinition(), generationSettings().getClassifierSensorTypeValue()));
        }
    }

    private List<String> createDescriptions(final Procedure procedure, final String[] observableProperties) {
        return CollectionHelper.list(String.format(generationSettings().getDescriptionTemplate(),
                procedure.isSpatial() ? "sensor system" : "procedure", procedure.getIdentifier(),
                StringHelper.join(",", CollectionHelper.list(observableProperties))));
    }

    private List<SosSMLIdentifier> createIdentifications(final String identifier) {
        // get long and short name definition from misc settings
        final SosSMLIdentifier idUniqueId =
                new SosSMLIdentifier(OGCConstants.URN_UNIQUE_IDENTIFIER_END, OGCConstants.URN_UNIQUE_IDENTIFIER,
                        identifier);
        final SosSMLIdentifier idShortName =
                new SosSMLIdentifier("shortname", generationSettings().getIdentifierShortNameDefinition(), identifier);
        final SosSMLIdentifier idLongName =
                new SosSMLIdentifier("longname", generationSettings().getIdentifierLongNameDefinition(), identifier);
        return CollectionHelper.list(idUniqueId, idLongName, idShortName);
    }

    protected ServiceConfiguration getServiceConfig() {
        return ServiceConfiguration.getInstance();
    }

    private ProcedureDescriptionSettings generationSettings() {
        return ProcedureDescriptionSettings.getInstance();
    }

    private List<String> createKeywordsList(final Procedure procedure, final String[] observableProperties) {
        final List<String> keywords = CollectionHelper.list();
        keywords.addAll(CollectionHelper.list(observableProperties));
        keywords.add(procedure.getIdentifier());
        if (generationSettings().isGenerateClassification()
                && !generationSettings().getClassifierIntendedApplicationValue().isEmpty()) {
            keywords.add(generationSettings().getClassifierIntendedApplicationValue());
        }
        if (generationSettings().isGenerateClassification()
                && !generationSettings().getClassifierSensorTypeValue().isEmpty()) {
            keywords.add(generationSettings().getClassifierSensorTypeValue());
        }
        return keywords;
    }

    protected String[] getObservablePropertiesForProcedure(final String procedureIdentifier) {
        return Configurator.getInstance().getCache().getObservablePropertiesForProcedure(procedureIdentifier)
                .toArray(new String[0]);
    }

    private SosProcedureDescription createProcedureDescriptionFromXml(final String procedureIdentifier,
            final String xmlDoc) throws CodedException, OwsExceptionReport {
            final SosProcedureDescription sosProcedureDescription = (SosProcedureDescription) CodingHelper.decodeXmlElement(XmlHelper.parseXmlString(xmlDoc));
            sosProcedureDescription.setIdentifier(procedureIdentifier);
            return sosProcedureDescription;
    }

    private SosProcedureDescription createProcedureDescriptionFromFile(final String procedureIdentifier,
            final String filename) throws CodedException, OwsExceptionReport {
        // check if filename contains placeholder for configured
        // sensor directory
            final SosProcedureDescription sosProcedureDescription = (SosProcedureDescription) CodingHelper.decodeXmlElement(XmlHelper.parseXmlString(StringHelper.convertStreamToString(getDescribeSensorDocumentAsStream(filename))));
            sosProcedureDescription.setIdentifier(procedureIdentifier);
            return sosProcedureDescription;
    }

    private InputStream getDescribeSensorDocumentAsStream(String filename) {
        final StringBuilder builder = new StringBuilder();
        if (filename.startsWith("standard")) {
            filename = filename.replace("standard", "");
            builder.append(ServiceConfiguration.getInstance().getSensorDir());
            builder.append("/");
        }
        builder.append(filename);
        LOGGER.debug("Procedure description file name '{}'!", filename);
        return Configurator.getInstance().getClass().getResourceAsStream(builder.toString());
    }

}
