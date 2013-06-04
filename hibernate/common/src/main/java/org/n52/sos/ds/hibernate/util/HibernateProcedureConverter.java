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

import static org.n52.sos.ogc.sensorML.elements.SmlClassifier.*;
import static org.n52.sos.ogc.swe.SWEConstants.SweCoordinateName.*;
import static org.n52.sos.util.HTTPConstants.StatusCode.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.cache.ContentCache;
import org.n52.sos.ds.hibernate.HibernateSessionHolder;
import org.n52.sos.ds.hibernate.dao.ProcedureDAO;
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
import org.n52.sos.ogc.sensorML.elements.SmlCapabilities;
import org.n52.sos.ogc.sensorML.elements.SmlClassifier;
import org.n52.sos.ogc.sensorML.elements.SmlIdentifier;
import org.n52.sos.ogc.sensorML.elements.SmlIo;
import org.n52.sos.ogc.sensorML.elements.SmlPosition;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.ogc.sos.SosOffering;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.swe.DataRecord;
import org.n52.sos.ogc.swe.SweCoordinate;
import org.n52.sos.ogc.swe.SweDataRecord;
import org.n52.sos.ogc.swe.SweEnvelope;
import org.n52.sos.ogc.swe.SweField;
import org.n52.sos.ogc.swe.simpleType.SweAbstractSimpleType;
import org.n52.sos.ogc.swe.simpleType.SweBoolean;
import org.n52.sos.ogc.swe.simpleType.SweCategory;
import org.n52.sos.ogc.swe.simpleType.SweCount;
import org.n52.sos.ogc.swe.simpleType.SweQuantity;
import org.n52.sos.ogc.swe.simpleType.SweText;
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

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a>
 * @author <a href="mailto:c.autermann@52north.org">Christian Autermann</a>
 * @author ShaneStClair
 * 
 * @since 4.0.0
 * 
 * TODO
 * - apply description enrichment to all types of procedures (creates, file, or database)
 * - use setting switches for code flow
 */
public class HibernateProcedureConverter {
    private final Logger LOGGER = LoggerFactory.getLogger(HibernateProcedureConverter.class);

    public SosProcedureDescription createSosProcedureDescription(
    		final Procedure procedure,
            final String procedureId,
            final String requestedDescriptionFormat,
            final String requestedServiceVersion,
            final Session session) throws OwsExceptionReport {
        if (procedure == null) {
            throw new NoApplicableCodeException().causedBy(
                    new IllegalArgumentException("Parameter 'procedure' should not be null!")).setStatus(
                    INTERNAL_SERVER_ERROR);
        }
        String filename = null;
        String xmlDoc = null;
        SosProcedureDescription sosProcedureDescription = null;

        // TODO: check and query for validTime parameter <-- according to request? This todo is unclear to me! (eike)
        if (procedure instanceof TProcedure) {
            final Set<ValidProcedureTime> validProcedureTimes = ((TProcedure)procedure).getValidProcedureTimes();
            // get the content of the latest validProcedureTime entry 
            for (final ValidProcedureTime validProcedureTime : validProcedureTimes) {
                if (validProcedureTime.getEndTime() == null) {
                    xmlDoc = validProcedureTime.getDescriptionXml();
                }
            }
        } else {
            filename = procedure.getDescriptionFile();
        }

        final String descriptionFormat = procedure.getProcedureDescriptionFormat().getProcedureDescriptionFormat();
        checkOutputFormatWithDescriptionFormat(requestedDescriptionFormat, descriptionFormat, procedureId);
        
        if (!isDescriptionAvailable(filename, xmlDoc)) {
            sosProcedureDescription = generateProcedureDescription(procedure);
        } else {
        	if (filename != null && descriptionFormat != null && xmlDoc == null) {
        		if (filename.startsWith("<")) {
        			sosProcedureDescription =
        					createProcedureDescriptionFromXml(procedureId, filename);
        		} else {
        			sosProcedureDescription =
        					createProcedureDescriptionFromFile(procedureId, filename);
        		}
        	} else {
        		sosProcedureDescription =
        				createProcedureDescriptionFromXml(procedureId, xmlDoc);
        	}
        }
        if (sosProcedureDescription != null) {
            if (sosProcedureDescription instanceof SensorML && ((SensorML)sosProcedureDescription).isWrapper()) {
                for (final AbstractProcess abstractProcess :  ((SensorML)sosProcedureDescription).getMembers()) {
                    addValuesToSensorDescription(abstractProcess, requestedServiceVersion, descriptionFormat, session);
                }
            } else {
                addValuesToSensorDescription(sosProcedureDescription, requestedServiceVersion, descriptionFormat, session);
            }
            sosProcedureDescription.setDescriptionFormat(descriptionFormat);
        }
        return sosProcedureDescription;
    }
    
    private void addValuesToSensorDescription(
    		final SosProcedureDescription procedureDescription,
    		final String version,
    		final String procedureDescriptionFormat,
            final Session session)
            		throws OwsExceptionReport {
    	// enrich with features
    	if (procedureSettings().isEnrichWithFeatures())
    	{
    		final Collection<String> features = getFeatureOfInterestIDsForProcedure(procedureDescription.getIdentifier(), version);
    		if (features != null && !features.isEmpty()) {
    			procedureDescription.addFeaturesOfInterest(new HashSet<String>(features));
    		}
    	}

        // parent procs
        final Collection<String> parentProcedures = getParentProcedures(procedureDescription.getIdentifier(), version);
        if (parentProcedures != null && !parentProcedures.isEmpty()) {
            procedureDescription.addParentProcedures(new HashSet<String>(parentProcedures));
        }

        // child procs
        final Set<SosProcedureDescription> childProcedures =
                getChildProcedures(procedureDescription.getIdentifier(), procedureDescriptionFormat,
                       version, session);
       procedureDescription.addChildProcedures(childProcedures);
       
       // enrich with offerings
       if (procedureSettings().isEnrichWithOfferings())
       {
    	   final Collection<SosOffering> offerings = getSosOfferingsForProcedure(procedureDescription.getIdentifier());
    	   if (offerings != null && !offerings.isEmpty())
    	   {
    		   procedureDescription.addOfferings(offerings);
    	   }
       }
       
       // enrich according to OGC#09-033 Profile for sensor discovery
       if (procedureSettings().isEnrichWithDiscoveryInformation() && procedureDescription instanceof AbstractSensorML)
       {
    	   // TODO Eike: implement enrichment according OGC#09-033 and move already implemented stuff from ProcedureConverter to this class
    	   if (procedureDescription instanceof AbstractSensorML)
    	   {
    		   final AbstractSensorML abstractSensorML = (AbstractSensorML)procedureDescription;
    		   
    		   // add observed BBox
    		   final SmlCapabilities observedBBox = createObservedBBOXCapability(abstractSensorML,
    				   procedureSettings().getLatLongUom());
    		   if (observedBBox != null)
    		   {
    			   abstractSensorML.addCapabilities(observedBBox);
    		   }

    		   final String[] observableProperties = getObservablePropertiesForProcedure(procedureDescription.getIdentifier());


    		   // add classification
    		   if (procedureSettings().isGenerateClassification()) {
    			   final List<SmlClassifier> classifier = createClassifier(abstractSensorML);
    			   if (classifier != null && !classifier.isEmpty()) {
    				   abstractSensorML.setClassifications(classifier);
    			   }
    		   }

    		   // add longName
    		   if (!isIdentifierLongNameSet(abstractSensorML))
    		   {
    			   abstractSensorML.addIdentifier(createLongName(procedureDescription.getIdentifier()));
    		   }

    		   // add shortName
    		   if (!isIdentifierShortNameSet(abstractSensorML))
    		   {
    			   abstractSensorML.addIdentifier(createShortName(procedureDescription.getIdentifier()));
    		   }
    		   
    		   // set contacts --> take from service information
    	        if (procedureSettings().isUseServiceContactAsProcedureContact()) {
    	            final List<SmlContact> contacts = createContactFromServiceContact();
    	            if (contacts != null && !contacts.isEmpty()) {
    	                abstractSensorML.setContact(contacts);
    	            }
    	        }
    	        
    	        // add keywords TODO use values like longName and shortName and others
    	        final List<String> keywordsList = createKeywordsList(procedureDescription.getIdentifier(), observableProperties);
    	        if (keywordsList != null && !keywordsList.isEmpty())
    	        {
    	        	abstractSensorML.setKeywords(keywordsList);
    	        }
    	   }
       }
    }

	private boolean isIdentifierShortNameSet(final AbstractSensorML abstractSensorML)
	{
		if (abstractSensorML.isSetIdentifications())
		{
			for (final SmlIdentifier smlIdentifier : abstractSensorML.getIdentifications()) {
				if ((smlIdentifier.isSetName() && smlIdentifier.getName().equalsIgnoreCase(SensorMLConstants.ELEMENT_NAME_SHORT_NAME))
						|| (smlIdentifier.isSetDefinition() && smlIdentifier.getDefinition().equalsIgnoreCase(procedureSettings().getIdentifierShortNameDefinition())))
				{
					return true;
				}
			}
		}
		return false;
	}

	private boolean isIdentifierLongNameSet(final AbstractSensorML abstractSensorML)
	{
		if (abstractSensorML.isSetIdentifications())
		{
			for (final SmlIdentifier smlIdentifier : abstractSensorML.getIdentifications()) {
				if ((smlIdentifier.isSetName() && smlIdentifier.getName().equalsIgnoreCase(SensorMLConstants.ELEMENT_NAME_LONG_NAME))
						|| (smlIdentifier.isSetDefinition() && smlIdentifier.getDefinition().equalsIgnoreCase(procedureSettings().getIdentifierLongNameDefinition())))
				{
					return true;
				}
			}
		}
		return false;
	}

    private SmlCapabilities createObservedBBOXCapability(
    		final AbstractSensorML procedureDescription,
    		final String uomForCoordinates)
	{
		// get all offerings for this procedure
		final Collection<SosOffering> offeringsForProcedure = getSosOfferingsForProcedure(procedureDescription.getIdentifier());
		
		if (offeringsForProcedure == null)
		{
			return null;
		}
		
		// get bbox for each offering and merge to one bbox
		final SosEnvelope mergedBBox = getMergedBBox(offeringsForProcedure);
		
		if (mergedBBox == null) 
		{
			return null;
		}
		// add merged bbox to capabilities as swe:envelope
		final SweEnvelope envelope = new SweEnvelope(mergedBBox,uomForCoordinates);
		
		final SweField field = new SweField(SensorMLConstants.ELEMENT_NAME_OBSERVED_BBOX, envelope);

		final DataRecord datarecord = new SweDataRecord();
		datarecord.addField(field);
		
		final SmlCapabilities capability = new SmlCapabilities();
		capability.setName(SensorMLConstants.ELEMENT_NAME_OBSERVED_BBOX);
		capability.setDataRecord(datarecord);
		
		return capability;
	}

	protected SosEnvelope getMergedBBox(final Collection<SosOffering> offeringsForProcedure)
	{
		SosEnvelope mergedEnvelope = null;
		for (final SosOffering sosOffering : offeringsForProcedure)
		{
			final SosEnvelope offeringEnvelope = getCache().getEnvelopeForOffering(sosOffering.getOfferingIdentifier());
			if (offeringEnvelope != null && offeringEnvelope.isSetEnvelope())
			{
				if (mergedEnvelope == null) 
				{
					mergedEnvelope = offeringEnvelope;
				}
				else 
				{
					mergedEnvelope.expandToInclude(offeringEnvelope.getEnvelope());
				}
			}
		}
		return mergedEnvelope;
	}

    private Collection<String> getFeatureOfInterestIDsForProcedure(
    		final String procedure,
    		final String version) throws OwsExceptionReport {
    	final Set<String> featureIds = CollectionHelper.set();
    	for (final String offeringId : getCache().getOfferingsForProcedure(procedure)) 
    	{	
			featureIds.addAll(getCache().getFeaturesOfInterestForOffering(offeringId));
		}
    	return SosHelper.getFeatureIDs(featureIds, version);
    }

    /**
     * Add parent procedures to a procedure
     * 
     * @param procID
     *            procedure identifier to add parent procedures to
     * @param parentProcedureIds
     *            The parent procedures to add

     *
     * @throws OwsExceptionReport
     */
    private Set<String> getParentProcedures(final String procID, final String version) throws OwsExceptionReport {
        return getCache().getParentProcedures(procID, false, false);
    }

    /**
     * Add a collection of child procedures to a procedure 
     * 
     * @param procID
     *            procedure identifier  to add child procedures to
     * @param childProcedures
     *            The child procedures to add
     *
     * @throws OwsExceptionReport
     */
	private Set<SosProcedureDescription> getChildProcedures(
			final String procID,
			final String outputFormat,
			final String version,
			final Session session)
					throws OwsExceptionReport {
        final Set<SosProcedureDescription> childProcedures = new HashSet<SosProcedureDescription>(0);
        final Collection<String> childProcedureIds = getCache().getChildProcedures(procID, false, false);
        if (childProcedureIds != null && !childProcedureIds.isEmpty()) {
            for (final String childProcID : childProcedureIds) {
                final Procedure childProcedure = new ProcedureDAO().getProcedureForIdentifier(childProcID, session);
                final SosProcedureDescription childProcedureDescription = createSosProcedureDescription(childProcedure, childProcID, outputFormat,version,session);
                addValuesToSensorDescription(childProcedureDescription, version, outputFormat, session);
				childProcedures.add(childProcedureDescription);
            }
        }
        return childProcedures;
    }
    
    private boolean isDescriptionAvailable(final String filename,
			final String xmlDoc)
	{
		return filename != null || xmlDoc != null;
	}

	private SosProcedureDescription generateProcedureDescription(final Procedure procedure) throws OwsExceptionReport
	{
		SosProcedureDescription sosProcedureDescription;
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
                String.format(procedureSettings().getProcessMethodRulesDefinitionDescriptionTemplate(),
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

        smlSystem.setPosition(createPosition(procedure));

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

        // 7 set outputs --> observableProperties
        abstractSensorML.setOutputs(createOutputs(procedure, observableProperties));
	}

    private List<SmlIo<?>> createOutputs(final Procedure procedure, final String[] observableProperties)
            throws OwsExceptionReport {
        final ArrayList<SmlIo<?>> outputs = new ArrayList<SmlIo<?>>(observableProperties.length);
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
            SmlIo<?> output = null;
            if (exampleObservation instanceof BlobObservation) {
                // TODO implement BlobObservations
                logTypeNotSupported(BlobObservation.class);
                continue;
            } else if (exampleObservation instanceof BooleanObservation) {
                final SweBoolean bool = new SweBoolean();
                bool.setDefinition(observableProperty);
                output = new SmlIo<Boolean>(bool);
            } else if (exampleObservation instanceof CategoryObservation) {
                final SweCategory category = new SweCategory();
                category.setDefinition(observableProperty);
                output = new SmlIo<String>(category);
            } else if (exampleObservation instanceof CountObservation) {
                final SweCount count = new SweCount();
                count.setDefinition(observableProperty);
                output = new SmlIo<Integer>(count);
            } else if (exampleObservation instanceof GeometryObservation) {
                // TODO implement GeometryObservations
                logTypeNotSupported(GeometryObservation.class);
                continue;
            } else if (exampleObservation instanceof NumericObservation) {
                final SweQuantity quantity = new SweQuantity();
                quantity.setDefinition(observableProperty);
                output = new SmlIo<Double>(quantity);
            } else if (exampleObservation instanceof TextObservation) {
                final SweText text = new SweText();
                text.setDefinition(observableProperty);
                output = new SmlIo<String>(text);
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

    private SmlPosition createPosition(final Procedure procedure) {
        SmlPosition smlPosition = null;
        smlPosition = new SmlPosition();
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

    private List<SweCoordinate<?>> createCoordinatesForPosition(final Object longitude, final Object latitude,
            final Object oAltitude) {
        final List<SweCoordinate<?>> sweCoordinates = new ArrayList<SweCoordinate<?>>(3);
        sweCoordinates.add(new SweCoordinate<Double>(northing, createSweQuantity(JavaHelper.asDouble(latitude),
                "y", procedureSettings().getLatLongUom())));
        sweCoordinates.add(new SweCoordinate<Double>(easting, createSweQuantity(JavaHelper.asDouble(longitude), "x",
                procedureSettings().getLatLongUom())));
        sweCoordinates.add(new SweCoordinate<Double>(altitude, createSweQuantity(JavaHelper.asDouble(oAltitude),
                "z", procedureSettings().getAltitudeUom())));
        // TODO add Integer: Which SweSimpleType to use?
        return sweCoordinates;
    }

    private SweAbstractSimpleType<Double> createSweQuantity(final Double value, final String asixID, final String uom) {
        final SweQuantity quantity = new SweQuantity();
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

    private List<SmlClassifier> createClassifier(final AbstractSensorML abstractSensorML) {
    	final List<SmlClassifier> classifications = CollectionHelper.list();
        if (!procedureSettings().getClassifierIntendedApplicationValue().isEmpty()) {
            classifications.add(new SmlClassifier(INTENDED_APPLICATION, 
            		procedureSettings().getClassifierIntendedApplicationDefinition(),
            		procedureSettings().getClassifierIntendedApplicationValue()));
        }
        if (!procedureSettings().getClassifierProcedureTypeValue().isEmpty()) {
            classifications.add(new SmlClassifier(PROCEDURE_TYPE,
            		procedureSettings().getClassifierSensorTypeDefinition(),
            		procedureSettings().getClassifierProcedureTypeValue()));
        }
        return classifications;
    }

    private List<String> createDescriptions(final Procedure procedure, final String[] observableProperties) {
        return CollectionHelper.list(String.format(procedureSettings().getDescriptionTemplate(),
                procedure.isSpatial() ? "sensor system" : "procedure", procedure.getIdentifier(),
                StringHelper.join(",", CollectionHelper.list(observableProperties))));
    }

    private List<SmlIdentifier> createIdentifications(final String identifier) {
        final List<SmlIdentifier> list = CollectionHelper.list();
        list.add(createIdentifier(identifier));
        return list;
    }

	private SmlIdentifier createIdentifier(final String identifier)
	{
		return new SmlIdentifier(OGCConstants.URN_UNIQUE_IDENTIFIER_END, OGCConstants.URN_UNIQUE_IDENTIFIER,
		        identifier);
	}

	private SmlIdentifier createLongName(final String identifier)
	{
		return new SmlIdentifier(SensorMLConstants.ELEMENT_NAME_LONG_NAME, procedureSettings().getIdentifierLongNameDefinition(), identifier);
	}

	private SmlIdentifier createShortName(final String identifier)
	{
		return new SmlIdentifier(SensorMLConstants.ELEMENT_NAME_SHORT_NAME, procedureSettings().getIdentifierShortNameDefinition(), identifier);
	}

    protected ServiceConfiguration getServiceConfig() {
        return ServiceConfiguration.getInstance();
    }

	private ProcedureDescriptionSettings procedureSettings() {
        return ProcedureDescriptionSettings.getInstance();
    }

	// TODO use more values like longName and shortName from procedureDescription
    private List<String> createKeywordsList(
    		final String procedureIdentifier,
    		final String[] observableProperties) {
        final List<String> keywords = CollectionHelper.list();
        keywords.addAll(CollectionHelper.list(observableProperties));
        keywords.add(procedureIdentifier);
        if (procedureSettings().isGenerateClassification()
                && !procedureSettings().getClassifierIntendedApplicationValue().isEmpty()) {
            keywords.add(procedureSettings().getClassifierIntendedApplicationValue());
        }
        if (procedureSettings().isGenerateClassification()
                && !procedureSettings().getClassifierProcedureTypeValue().isEmpty()) {
            keywords.add(procedureSettings().getClassifierProcedureTypeValue());
        } 
        // TODO test this and add offering names, too
        if (procedureSettings().isEnrichWithOfferings()) {
        	keywords.addAll(getOfferingIdentifiersForProcedure(procedureIdentifier));
        }
        return keywords;
    }

    protected String[] getObservablePropertiesForProcedure(final String procedureIdentifier) {
        return getCache().getObservablePropertiesForProcedure(procedureIdentifier)
                .toArray(new String[0]);
    }
    
    protected Collection<SosOffering> getSosOfferingsForProcedure(final String procedureIdentifier) {
		final Collection<String> offeringIds = getCache().getOfferingsForProcedure(procedureIdentifier);
		final Collection<SosOffering> offerings = CollectionHelper.list();
		for (final String offeringIdentifier : offeringIds) {
			final String offeringName = getCache().getNameForOffering(offeringIdentifier);
			offerings.add(new SosOffering(offeringIdentifier, offeringName));
		}
		return offerings;
	}

	protected Collection<String> getOfferingIdentifiersForProcedure(final String procedureIdentifier) {
    	return getCache().getOfferingsForProcedure(procedureIdentifier);
    }

	protected ContentCache getCache()
	{
		return Configurator.getInstance().getCache();
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
