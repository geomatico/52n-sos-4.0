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

import static org.n52.sos.util.CollectionHelper.synchronizedList;

import java.util.Collection;
import java.util.List;

import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.util.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

/**
 * When executing this &auml;ction (see {@link Action}), the following relations are added, settings are updated in cache:<ul>
 * <li>'Result template identifier' &rarr; 'observable property' relation</li>
 * <li>'Result template identifier' &rarr; 'feature of interest' relation</li></ul>
 * TODO update list above
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @since 4.0
 */
public class ResultInsertionInMemoryCacheUpdate extends InMemoryCacheUpdate {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultInsertionInMemoryCacheUpdate.class);

	private final SosObservation sosObservation;

	private final String templateIdentifier;

	public ResultInsertionInMemoryCacheUpdate(String templateIdentifier, SosObservation sosObservation) {
		if (sosObservation == null || templateIdentifier == null || templateIdentifier.isEmpty())
		{
			String msg = String.format("Missing argument: '{}': {}; template identifier: '{}'",  
					SosObservation.class.getName(),
					sosObservation,
					templateIdentifier);
			LOGGER.error(msg);
			throw new IllegalArgumentException(msg);
		}
		this.sosObservation = sosObservation;
		this.templateIdentifier = templateIdentifier;
	}

	@Override
	public void execute()
	{
		// TODO remove not required updates and adjust test accordingly
		addObservationTypeToCache(sosObservation);
		updateGlobalTemporalBoundingBox(sosObservation.getPhenomenonTime());
		addProcedureToCache(getProcedureIdentifier(sosObservation));
		addObservablePropertyToProcedureRelation(getObservablePropertyIdentifier(sosObservation), getProcedureIdentifier(sosObservation));
		addResultTemplateToObservablePropertyRelation(templateIdentifier,getObservablePropertyIdentifier(sosObservation));
		addProcedureToObservablePropertyRelation(getProcedureIdentifier(sosObservation), getObservablePropertyIdentifier(sosObservation));
		
		if (sosObservation.getIdentifier() != null)
		{
			addObservationIdToCache(sosObservation);
			addProcedureToObservationIdRelationToCache(getProcedureIdentifier(sosObservation),sosObservation.getIdentifier().getValue());
		}
		
		List<SosSamplingFeature> observedFeatures = sosFeaturesToList(sosObservation.getObservationConstellation().getFeatureOfInterest());

		Envelope observedFeatureEnvelope = createEnvelopeFrom(observedFeatures);
		updateGlobalEnvelopeUsing(observedFeatureEnvelope);

		for (SosSamplingFeature sosSamplingFeature : observedFeatures)
		{
			String observedFeatureIdentifier = sosSamplingFeature.getIdentifier().getValue();
			addFeatureIdentifierToCache(observedFeatureIdentifier);
			addResultTemplateToFeatureOfInterestRelation(templateIdentifier, observedFeatureIdentifier);
			addFeatureToProcedureRelationToCache(observedFeatureIdentifier, getProcedureIdentifier(sosObservation));
			addFeatureTypeToCache(sosSamplingFeature.getFeatureType());
			for (String offeringIdentifier : sosObservation.getObservationConstellation().getOfferings())
			{
				addOfferingToFeatureRelationToCache(observedFeatureIdentifier, offeringIdentifier);
			}
		}
		for (String offeringIdentifier : sosObservation.getObservationConstellation().getOfferings())
		{
			addOfferingToProcedureRelation(offeringIdentifier, getProcedureIdentifier(sosObservation));
			addProcedureToOfferingRelation(getProcedureIdentifier(sosObservation), offeringIdentifier);
			updateOfferingEnvelope(observedFeatureEnvelope, getCache().getDefaultEPSGCode(), offeringIdentifier);
			updateTemporalBoundingBoxOf(offeringIdentifier, phenomenonTimeFrom(sosObservation));
			// observable property
			addObservablePropertiesToOfferingRelation(getObservablePropertyIdentifier(sosObservation), offeringIdentifier);
			addOfferingToObservablePropertyRelation(offeringIdentifier, getObservablePropertyIdentifier(sosObservation));
			// observation type
			addOfferingToObservationTypeRelation(offeringIdentifier, sosObservation.getObservationConstellation().getObservationType());
		}
	}
	
	/**
	 * @see {@link ResultTemplateCacheUpdate#addResultTemplateToFeatureOfInterestRelation(String,String)} 
	 */
	private void addResultTemplateToFeatureOfInterestRelation(String resultTemplateIdentifier,
			String featureOfInterestIdentifier)
	{
		if(!getCache().getKResultTemplateVFeaturesOfInterest().containsKey(resultTemplateIdentifier))
		{
			Collection<String> featureOfInterestIdentifiers = synchronizedList(1);
			getCache().getKResultTemplateVFeaturesOfInterest().put(resultTemplateIdentifier, featureOfInterestIdentifiers);
		}
		getCache().getKResultTemplateVFeaturesOfInterest().get(resultTemplateIdentifier).add(featureOfInterestIdentifier);
		LOGGER.debug("Result Template '{}' to feature of interest '{}' relation added to cache? {}",
				resultTemplateIdentifier,
				featureOfInterestIdentifier,
				getCache().getKResultTemplateVFeaturesOfInterest().get(resultTemplateIdentifier).contains(featureOfInterestIdentifier));
	}

	/**
	 * @see {@link ResultTemplateCacheUpdate#addResultTemplateToObservablePropertyRelation(String,String)} 
	 */
	private void addResultTemplateToObservablePropertyRelation(String resultTemplateIdentifier,
			String observablePropertyIdentifier)
	{
		if(!getCache().getKResultTemplateVObservedProperties().containsKey(resultTemplateIdentifier))
		{
			Collection<String> observedPropertyIdentifiers = synchronizedList(1);
			getCache().getKResultTemplateVObservedProperties().put(resultTemplateIdentifier, observedPropertyIdentifiers);
		}
		getCache().getKResultTemplateVObservedProperties().get(resultTemplateIdentifier).add(observablePropertyIdentifier);
		LOGGER.debug("Result Template '{}' to observable property '{}' relation added to cache? {}",
				resultTemplateIdentifier,
				observablePropertyIdentifier,
				getCache().getKResultTemplateVObservedProperties().get(resultTemplateIdentifier).contains(observablePropertyIdentifier));
	}

	private void updateGlobalTemporalBoundingBox(ITime phenomenonTime)
	{
		if (phenomenonTime instanceof TimeInstant)
		{
			updateGlobalTemporalBBoxUsingNew(new TimePeriod(((TimeInstant)phenomenonTime).getValue(), ((TimeInstant)phenomenonTime).getValue()));
		}
		else if (phenomenonTime instanceof TimePeriod)
		{
			updateGlobalTemporalBBoxUsingNew((TimePeriod) phenomenonTime);
		}
		else
		{
			// TODO throw exception?
			LOGGER.error("phenomenon time type '{}' is not supported implementation of '{}'",
					phenomenonTime.getClass().getName(),
					ITime.class.getName());
		}
	}

}
