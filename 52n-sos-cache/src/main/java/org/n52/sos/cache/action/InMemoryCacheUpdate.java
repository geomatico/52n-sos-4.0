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
package org.n52.sos.cache.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.n52.sos.cache.CapabilitiesCache;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.SosFeatureCollection;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.util.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

/**
 * TODO add log statements to all protected methods!
 * TODO extract sub classes for insertion updates
 * 
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @since 4.0
 *
 */
public abstract class InMemoryCacheUpdate implements Action {

	private CapabilitiesCache cache;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryCacheUpdate.class);

	public CapabilitiesCache getCache()
	{
		return cache;
	}

	public void setCache(CapabilitiesCache cache)
	{
		this.cache = cache;
	}
	
	protected String getProcedureIdentifier(SosObservation sosObservation)
	{
		return sosObservation.getObservationConstellation().getProcedure().getProcedureIdentifier();
	}
	
	protected String getObservablePropertyIdentifier(SosObservation sosObservation)
	{
		return sosObservation.getObservationConstellation().getObservableProperty().getIdentifier();
	}
	
	protected void addProcedureToCache(String procedureIdentifier)
	{
		if (!getCache().getProcedures().contains(procedureIdentifier)) {
			getCache().getProcedures().add(procedureIdentifier);
			LOGGER.debug("added procedure '{}' to cache? {}",
					procedureIdentifier,
					getCache().getProcedures().contains(procedureIdentifier));
		}
	}
	
	protected void addObservablePropertyToProcedureRelation(String observablePropertyIdentifier,
			String procedureIdentifier)
	{
		if (getCache().getKObservablePropertyVProcedures().get(observablePropertyIdentifier) == null)
		{
			List<String> relatedProcedures = Collections.synchronizedList(new ArrayList<String>());
			relatedProcedures.add(procedureIdentifier);
			getCache().getKObservablePropertyVProcedures().put(observablePropertyIdentifier, relatedProcedures);
		}
		else if (!getCache().getKObservablePropertyVProcedures().get(observablePropertyIdentifier).contains(procedureIdentifier))
		{
			getCache().getKObservablePropertyVProcedures().get(observablePropertyIdentifier).add(procedureIdentifier);
		}
	}
	
	protected void addProcedureToObservablePropertyRelation(String procedureIdentifier,
			String observablePropertyIdentifier)
	{
		if (getCache().getKProcedureVObservableProperties().get(procedureIdentifier) == null) {
			List<String> relatedProperties = Collections.synchronizedList(new ArrayList<String>());
			relatedProperties.add(observablePropertyIdentifier);
			getCache().getKProcedureVObservableProperties().put(procedureIdentifier, relatedProperties);
		} else if (!getCache().getKProcedureVObservableProperties().get(procedureIdentifier).contains(observablePropertyIdentifier)) {
			getCache().getKProcedureVObservableProperties().get(procedureIdentifier).add(observablePropertyIdentifier);
		}
	}
	
	protected TimePeriod phenomenonTimeFrom(SosObservation sosObservation)
	{
		ITime phenomenonTime = sosObservation.getPhenomenonTime();
		if (phenomenonTime instanceof TimeInstant) {
			return new TimePeriod(((TimeInstant) phenomenonTime).getValue(), ((TimeInstant) phenomenonTime).getValue());
		} else {
			return (TimePeriod) phenomenonTime;
		}
	}
	
	protected void updateGlobalTemporalBBoxUsingNew(TimePeriod observationEventTime)
	{
		if (getCache().getMinEventTime() == null || getCache().getMinEventTime().isAfter(observationEventTime.getStart())) {
			getCache().setMinEventTime(observationEventTime.getStart());
			LOGGER.debug("Updated global temporal bounding box: new min time: {}", observationEventTime.getStart());
		}
		if (getCache().getMaxEventTime() == null || getCache().getMaxEventTime().isBefore(observationEventTime.getEnd())) {
			getCache().setMaxEventTime(observationEventTime.getEnd());
			LOGGER.debug("Updated global temporal bounding box: new max time: {}", observationEventTime.getEnd());
		}
	}
	
	protected void addObservationTypeToCache(SosObservation sosObservation)
	{
		if (!getCache().getObservationTypes().contains(getObservationType(sosObservation))) {
			getCache().getObservationTypes().add(getObservationType(sosObservation));
			LOGGER.debug("observation type '{}' added to cache? {}",
					getObservationType(sosObservation),
					getCache().getObservationTypes().contains(getObservationType(sosObservation)));
		}
	}
	
	protected void addObservationIdToCache(SosObservation sosObservation)
	{
		getCache().getObservationIdentifiers().add(sosObservation.getIdentifier().getValue());
		LOGGER.debug("Added observation id '{}' to cache? {}",
				sosObservation.getIdentifier().getValue(),
				getCache().getObservationIdentifiers().contains(sosObservation.getIdentifier().getValue()));
	}
	
	protected void addProcedureToObservationIdRelationToCache(String procedureIdentifier, String observationIdentifier)
	{
		if (!getCache().getKProcedureVObservationIdentifiers().containsKey(procedureIdentifier))
		{
			Collection<String> value = Collections.synchronizedList(new ArrayList<String>());
			getCache().getKProcedureVObservationIdentifiers().put(procedureIdentifier, value);
		}
		getCache().getKProcedureVObservationIdentifiers().get(procedureIdentifier).add(observationIdentifier);
		LOGGER.debug("procedure '{}' to observation id '{}' relation added to cache? {}",
				procedureIdentifier,
				observationIdentifier,
				getCache().getKProcedureVObservationIdentifiers().get(procedureIdentifier).contains(observationIdentifier));
	}
	
	protected List<SosSamplingFeature> sosFeaturesToList(SosAbstractFeature sosFeatureOfInterest)
	{
		if (sosFeatureOfInterest instanceof SosFeatureCollection) {
			return getAllFeaturesFrom((SosFeatureCollection) sosFeatureOfInterest);
		} else if (sosFeatureOfInterest instanceof SosSamplingFeature) {
			List<SosSamplingFeature> observedFeatures = new ArrayList<SosSamplingFeature>(1);
			observedFeatures.add((SosSamplingFeature) sosFeatureOfInterest);
			return observedFeatures;
		} else {
			String errorMessage = String.format("Feature Type \"%s\" not supported.", sosFeatureOfInterest != null ? sosFeatureOfInterest.getClass().getName() : sosFeatureOfInterest);
			LOGGER.error(errorMessage);
			throw new IllegalArgumentException(errorMessage); // TODO change type of exception to OER?
		}
	}
	
	private List<SosSamplingFeature> getAllFeaturesFrom(SosFeatureCollection sosFeatureOfInterest)
	{
		List<SosSamplingFeature> allFeatures = new ArrayList<SosSamplingFeature>(((SosFeatureCollection) sosFeatureOfInterest).getMembers().size());
		for (Entry<String, SosAbstractFeature> entry : ((SosFeatureCollection) sosFeatureOfInterest).getMembers().entrySet()) {
			if (entry.getValue() instanceof SosSamplingFeature) {
				allFeatures.add((SosSamplingFeature) entry.getValue());
			} else if (entry.getValue() instanceof SosFeatureCollection) {
				allFeatures.addAll(getAllFeaturesFrom((SosFeatureCollection) entry.getValue()));
			}
		}
		return allFeatures;
	}
	
	protected Envelope createEnvelopeFrom(List<SosSamplingFeature> observedFeatures)
	{
		Envelope featureEnvelope = new Envelope();
		for (SosSamplingFeature sosSamplingFeature : observedFeatures) {
			featureEnvelope.expandToInclude(sosSamplingFeature.getGeometry().getEnvelopeInternal());
		}
		return featureEnvelope;
	}
	
	protected void updateGlobalEnvelopeUsing(Envelope observedFeatureEnvelope)
	{
		SosEnvelope globalEnvelope = getCache().getGlobalEnvelope();
		if (!globalEnvelope.isSetEnvelope()) {
			// add new envelope
			SosEnvelope newFeatureEnvelope = new SosEnvelope(observedFeatureEnvelope, getCache().getDefaultEPSGCode());
			getCache().setGlobalEnvelope(newFeatureEnvelope);
		} else if (!globalEnvelope.getEnvelope().contains(observedFeatureEnvelope)) {
			// extend envelope
			globalEnvelope.getEnvelope().expandToInclude(observedFeatureEnvelope);
		}
	}
	
	protected void addFeatureIdentifierToCache(String observedFeatureIdentifier)
	{
		if (!getCache().getFeatureOfInterest().contains(observedFeatureIdentifier)) {
			getCache().getFeatureOfInterest().add(observedFeatureIdentifier);
			LOGGER.debug("feature identifier '{}' added to cache? {}",
					observedFeatureIdentifier,
					getCache().getFeatureOfInterest().contains(observedFeatureIdentifier));
		}
	}
	
	protected void addFeatureToProcedureRelationToCache(String observedFeatureIdentifier,
			String procedureIdentifier)
	{
		if (getCache().getKFeatureOfInterestVProcedures().get(observedFeatureIdentifier) == null) {
			getCache().getKFeatureOfInterestVProcedures().put(observedFeatureIdentifier, Collections.synchronizedList(new ArrayList<String>()));
		}
		if (!getCache().getKFeatureOfInterestVProcedures().get(observedFeatureIdentifier).contains(procedureIdentifier)) {
			getCache().getKFeatureOfInterestVProcedures().get(observedFeatureIdentifier).add(procedureIdentifier);
			LOGGER.debug("feature '{}' to procedure '{}' relation added to cache? {}",
					observedFeatureIdentifier,
					procedureIdentifier,
					getCache().getKFeatureOfInterestVProcedures().get(observedFeatureIdentifier).contains(procedureIdentifier));
		}
	}
	
	protected void addOfferingToFeatureRelationToCache(String observedFeatureIdentifier,
			String offeringIdentifier)
	{
		if (!getCache().getKOfferingVFeaturesOfInterest().containsKey(offeringIdentifier)) {
			getCache().getKOfferingVFeaturesOfInterest().put(offeringIdentifier, Collections.synchronizedList(new ArrayList<String>()));
		} 
		if (!getCache().getKOfferingVFeaturesOfInterest().get(offeringIdentifier).contains(observedFeatureIdentifier)) {
			getCache().getKOfferingVFeaturesOfInterest().get(offeringIdentifier).add(observedFeatureIdentifier);
			LOGGER.debug("offering '{}' to feature '{}' relation added to cache? {}",
					offeringIdentifier,
					observedFeatureIdentifier,
					getCache().getKOfferingVFeaturesOfInterest().get(offeringIdentifier).contains(observedFeatureIdentifier));
		}
	}
	
	protected void addFeatureTypeToCache(String featureType)
	{
		if (!getCache().getFeatureOfInterestTypes().contains(featureType)) {
			getCache().getFeatureOfInterestTypes().add(featureType);
			LOGGER.debug("feature type '{}' added to cache? {}",
					featureType,
					getCache().getFeatureOfInterestTypes().contains(featureType));
		}
	}
	
	protected void addOfferingToProcedureRelation(String offeringIdentifier,
			String procedureIdentifier)
	{
		if (getCache().getKOfferingVProcedures().get(offeringIdentifier) == null) {
			getCache().getKOfferingVProcedures().put(offeringIdentifier, Collections.synchronizedList(new ArrayList<String>()));
		}
		if (!getCache().getKOfferingVProcedures().get(offeringIdentifier).contains(procedureIdentifier)) {
			getCache().getKOfferingVProcedures().get(offeringIdentifier).add(procedureIdentifier);
			LOGGER.debug("offering '{}' -> procedure '{}' relation added to cache? {}",
					offeringIdentifier,
					procedureIdentifier,
					getCache().getKOfferingVProcedures().get(offeringIdentifier).contains(procedureIdentifier));
		}
	}
	
	protected void addProcedureToOfferingRelation(String procedureIdentifier,
			String offeringIdentifier)
	{
		if (getCache().getKProcedureVOffering().get(procedureIdentifier) == null) {
			getCache().getKProcedureVOffering().put(procedureIdentifier, Collections.synchronizedList(new ArrayList<String>()));
		}
		if (!getCache().getKProcedureVOffering().get(procedureIdentifier).contains(offeringIdentifier)) {
			getCache().getKProcedureVOffering().get(procedureIdentifier).add(offeringIdentifier);
			LOGGER.debug("procedure '{}' -> offering '{}' relation added to cache? {}",
					procedureIdentifier,
					offeringIdentifier,
					getCache().getKProcedureVOffering().get(procedureIdentifier).contains(offeringIdentifier));
		}
	}
	
	protected void addObservablePropertiesToOfferingRelation(String observablePropertyIdentifier,
			String offeringIdentifier)
	{
		if (getCache().getKObservablePropertyVOffering().get(observablePropertyIdentifier) == null) {
			getCache().getKObservablePropertyVOffering().put(observablePropertyIdentifier, Collections.synchronizedList(new ArrayList<String>()));
		}
		if (!getCache().getKObservablePropertyVOffering().get(observablePropertyIdentifier).contains(offeringIdentifier)) {
			getCache().getKObservablePropertyVOffering().get(observablePropertyIdentifier).add(offeringIdentifier);
			LOGGER.debug("observable property '{}' -> offering '{}' relation added to cache? {}",
					observablePropertyIdentifier,
					offeringIdentifier,
					getCache().getKObservablePropertyVOffering().get(observablePropertyIdentifier).contains(offeringIdentifier));
		}
	}
	
	protected void addOfferingToObservablePropertyRelation(String offeringIdentifier,
			String observablePropertyIdentifier)
	{
		if (getCache().getKOfferingVObservableProperties().get(offeringIdentifier) == null) {
			getCache().getKOfferingVObservableProperties().put(offeringIdentifier, Collections.synchronizedList(new ArrayList<String>()));
		}
		if (!getCache().getKOfferingVObservableProperties().get(offeringIdentifier).contains(observablePropertyIdentifier)) {
			getCache().getKOfferingVObservableProperties().get(offeringIdentifier).add(observablePropertyIdentifier);
			LOGGER.debug("offering '{}' -> observable property '{}' relation added to cache? {}",
					offeringIdentifier,
					observablePropertyIdentifier,
					getCache().getKOfferingVObservableProperties().get(offeringIdentifier).contains(observablePropertyIdentifier));
		}
	}
	
	protected void addOfferingToObservationTypeRelation(String offeringIdentifier,
			String observationType)
	{
		if (getCache().getKOfferingVObservationTypes().get(offeringIdentifier) == null)
		{
			getCache().getKOfferingVObservationTypes().put(offeringIdentifier, Collections.synchronizedList(new ArrayList<String>()));
		} 
		if (!getCache().getKOfferingVObservationTypes().get(offeringIdentifier).contains(observationType)) {
			getCache().getKOfferingVObservationTypes().get(offeringIdentifier).add(observationType);
			LOGGER.debug("offering '{}' -> observation type '{}' relation added to cache? {}",
					offeringIdentifier,
					observationType,
					getCache().getKOfferingVObservationTypes().get(offeringIdentifier).contains(observationType));
		}
	}
	
	protected void updateTemporalBoundingBoxOf(String offeringIdentifier,
			TimePeriod observationEventTime)
	{
		// offering-maxtime
		// check and update if later
		if (!getCache().getKOfferingVMaxTime().containsKey(offeringIdentifier))
		{
			getCache().getKOfferingVMaxTime().put(offeringIdentifier, observationEventTime.getEnd());
		} 
		else if (getCache().getKOfferingVMaxTime().get(offeringIdentifier).isBefore(observationEventTime.getEnd())) 
		{
			getCache().getKOfferingVMaxTime().put(offeringIdentifier, observationEventTime.getEnd());
		}
		// offering-mintime
		// check and update if before
		if (!getCache().getKOfferingVMinTime().containsKey(offeringIdentifier))
		{
			getCache().getKOfferingVMinTime().put(offeringIdentifier, observationEventTime.getStart());
		}
		if (getCache().getKOfferingVMinTime().get(offeringIdentifier).isAfter(observationEventTime.getStart()))
		{
			getCache().getKOfferingVMinTime().put(offeringIdentifier, observationEventTime.getStart());
		}
		LOGGER.debug("temporal boundingbox of offering '{}' set to: {}",
				offeringIdentifier,
				observationEventTime);
	}
	
	protected void updateOfferingEnvelope(Envelope observedFeatureEnvelope,
			int observedFeatureEnvelopeSRID,
			String offeringIdentifier)
	{
		if (isEnvelopeInMapAndDoesNotContainFeatureEnvelope(observedFeatureEnvelope, offeringIdentifier)) 
		{
			// update envelope
			getCache().getKOfferingVEnvelope().get(offeringIdentifier).getEnvelope().expandToInclude(observedFeatureEnvelope);
		} 
		else
		{
			// add new envelope
			SosEnvelope newOfferingEnvelope = new SosEnvelope(observedFeatureEnvelope, observedFeatureEnvelopeSRID);
			getCache().getKOfferingVEnvelope().put(offeringIdentifier, newOfferingEnvelope);
		}
		LOGGER.debug("Envelope for offering '{}': {}",
				offeringIdentifier,
				getCache().getKOfferingVEnvelope().get(offeringIdentifier));
	}

	private boolean isEnvelopeInMapAndDoesNotContainFeatureEnvelope(Envelope observedFeatureEnvelope,
			String offeringIdentifier)
	{
		return getCache().getKOfferingVEnvelope().containsKey(offeringIdentifier) &&
				getCache().getKOfferingVEnvelope().get(offeringIdentifier).isSetEnvelope() && 
				!getCache().getKOfferingVEnvelope().get(offeringIdentifier).getEnvelope().contains(observedFeatureEnvelope);
	}

	private String getObservationType(SosObservation sosObservation)
	{
		return sosObservation.getObservationConstellation().getObservationType();
	}

	@Override
	public String toString()
	{
		return String.format("%s [cache=%s]", getClass().getName(), cache);
	}
}
