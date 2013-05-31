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
package org.n52.sos.ds.hibernate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.AbstractDescribeSensorDAO;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.HibernateProcedureConverter;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.om.SosOffering;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.AbstractProcess;
import org.n52.sos.ogc.sensorML.AbstractSensorML;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.elements.SosSMLCapabilities;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.swe.DataRecord;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.ogc.swe.SosSweEnvelope;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.request.DescribeSensorRequest;
import org.n52.sos.response.DescribeSensorResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ProcedureDescriptionSettings;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.SosHelper;

/**
 * Implementation of the interface IDescribeSensorDAO
 * 
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a>
 * @author ShaneStClair
 * @author <a href="mailto:c.autermann@52north.org">Christian Autermann</a>
 * 
 * @since 4.0.0
 */
public class DescribeSensorDAO extends AbstractDescribeSensorDAO {
    private final HibernateSessionHolder sessionHolder = new HibernateSessionHolder();
    private final HibernateProcedureConverter procedureConverter = new HibernateProcedureConverter();
    
    public DescribeSensorDAO() {
        super(SosConstants.SOS);
    }

    @Override
    public DescribeSensorResponse getSensorDescription(final DescribeSensorRequest request) throws OwsExceptionReport {
        // sensorDocument which should be returned
        Session session = null;
        try {
            session = sessionHolder.getSession();
            final SosProcedureDescription result = queryProcedure(request, session);
            
            if (result instanceof SensorML && ((SensorML)result).isWrapper()) {
                for (final AbstractProcess abstractProcess :  ((SensorML)result).getMembers()) {
                    addValuesToSensorDescription(abstractProcess, request.getVersion(), request.getProcedureDescriptionFormat(), session);
                }
            } else {
                addValuesToSensorDescription(result, request.getVersion(), request.getProcedureDescriptionFormat(), session);
            }
            
            final DescribeSensorResponse response = new DescribeSensorResponse();
            response.setService(request.getService());
            response.setVersion(request.getVersion());
            response.setOutputFormat(request.getProcedureDescriptionFormat());
            response.setSensorDescription(result);
            return response;
        } catch (final HibernateException he) {
            throw new NoApplicableCodeException().causedBy(he)
                    .withMessage("Error while querying data for DescribeSensor document!");
        } finally {
            sessionHolder.returnSession(session);
        }
    }

    private SosProcedureDescription queryProcedure(final DescribeSensorRequest request, final Session session)
            throws OwsExceptionReport {
        final Procedure procedure = HibernateCriteriaQueryUtilities.getProcedureForIdentifier(request.getProcedure(), session);
        return procedureConverter.createSosProcedureDescription(procedure, request.getProcedure(), request.getProcedureDescriptionFormat());
    }

    private void addValuesToSensorDescription(final SosProcedureDescription procedureDescription, final String version, final String procedureDescriptionFormat,
            final Session session) throws OwsExceptionReport {
    	// enrich with features
    	if (procedureSettings().isEnrichWithFeatures())
    	{
    		final Collection<String> features = getFeatureOfInterestIDsForProcedure(procedureDescription.getIdentifier(), version, session);
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
    	   final Collection<SosOffering> offerings = getOfferingsForProcedure(procedureDescription.getIdentifier());
    	   if (offerings != null && !offerings.isEmpty())
    	   {
    		   procedureDescription.addOfferings(offerings);
    	   }
       }
       
       // enrich according to OGC#09-033 Profile for sensor discovery
       if (procedureSettings().isEnrichWithDiscoveryInformation() && procedureDescription instanceof AbstractSensorML)
       {
    	   // TODO Eike: implement enrichment according OGC#09-033 and move already implemented stuff from ProcedureConverter to this class
    	   final SosSMLCapabilities observedBBox = createObservedBBOXCapability((AbstractSensorML)procedureDescription);
    	   if (observedBBox != null)
    	   {
    		   ((AbstractSensorML)procedureDescription).addCapabilities(observedBBox);
    	   }
       }
    }
    
	private SosSMLCapabilities createObservedBBOXCapability(final AbstractSensorML procedureDescription)
	{
		// get all offerings for this procedure
		final Collection<SosOffering> offeringsForProcedure = getOfferingsForProcedure(procedureDescription.getIdentifier());
		
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
		final SosSweEnvelope envelope = new SosSweEnvelope(mergedBBox,"deg"); // FIXME "deg"<-- configure or compute somehow
		
		final SosSweField field = new SosSweField(SensorMLConstants.ELEMENT_NAME_OBSERVED_BBOX, envelope);

		final DataRecord datarecord = new SosSweDataRecord();
		datarecord.addField(field);
		
		final SosSMLCapabilities capability = new SosSMLCapabilities();
		capability.setName(SensorMLConstants.ELEMENT_NAME_OBSERVED_BBOX);
		capability.setDataRecord(datarecord);
		
		return capability;
	}

	protected SosEnvelope getMergedBBox(final Collection<SosOffering> offeringsForProcedure)
	{
		SosEnvelope mergedEnvelope = null;
		for (final SosOffering sosOffering : offeringsForProcedure)
		{
			final SosEnvelope offeringEnvelope = Configurator.getInstance().getCache().getEnvelopeForOffering(sosOffering.getOfferingIdentifier());
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

	protected Collection<SosOffering> getOfferingsForProcedure(final String procedureIdentifier) {
    	final Collection<String> offeringIds = Configurator.getInstance().getCache().getOfferingsForProcedure(procedureIdentifier);
    	final Collection<SosOffering> offerings = CollectionHelper.list();
    	for (final String offeringIdentifier : offeringIds) {
			final String offeringName = Configurator.getInstance().getCache().getNameForOffering(offeringIdentifier);
			offerings.add(new SosOffering(offeringIdentifier, offeringName));
		}
    	return offerings;
    }

    @SuppressWarnings("unchecked")
    private Collection<String> getFeatureOfInterestIDsForProcedure(final String procedure, final String version,
                                                                   final Session session) throws OwsExceptionReport {
        final Criteria c = session.createCriteria(Observation.class);
        c.createCriteria(Observation.PROCEDURE).add(Restrictions.eq(Procedure.IDENTIFIER, procedure));
        c.createCriteria(Observation.FEATURE_OF_INTEREST)
                .setProjection(Projections.distinct(Projections.property(FeatureOfInterest.IDENTIFIER)));
        // FIXME: checks for generated IDs and remove them for SOS 2.0
        return SosHelper.getFeatureIDs(c.list(), version);
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
    private Set<SosProcedureDescription> getChildProcedures(final String procID, final String outputFormat, final String version,
                                                            final Session session) throws OwsExceptionReport {
        final Set<SosProcedureDescription> childProcedures = new HashSet<SosProcedureDescription>(0);
        final Collection<String> childProcedureIds = getCache().getChildProcedures(procID, false, false);
        if (childProcedureIds != null && !childProcedureIds.isEmpty()) {
            for (final String childProcID : childProcedureIds) {
                final Procedure procedure = HibernateCriteriaQueryUtilities.getProcedureForIdentifier(childProcID, session);
                final SosProcedureDescription childProcedure = procedureConverter.createSosProcedureDescription(procedure, childProcID, outputFormat);
                addValuesToSensorDescription(childProcedure, version, outputFormat, session);
				childProcedures.add(childProcedure);
            }
        }
        return childProcedures;
    }
    
    private ProcedureDescriptionSettings procedureSettings()
    {
    	return ProcedureDescriptionSettings.getInstance();
    }
}
