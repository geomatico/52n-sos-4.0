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

import java.util.List;

import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @since 4.0
 */
public class ResultInsertionInMemoryCacheUpdate extends InMemoryCacheUpdate {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultInsertionInMemoryCacheUpdate.class);

	private final SosObservation sosObservation;

	public ResultInsertionInMemoryCacheUpdate(SosObservation sosObservation) {
		if (sosObservation == null)
		{
			String msg = String.format("Missing argument: '{}': {}", 
					SosObservation.class.getName(),
					sosObservation);
			LOGGER.error(msg);
			throw new IllegalArgumentException(msg);
		}
		this.sosObservation = sosObservation;
	}

	@Override
	public void execute()
	{
		addObservationTypeToCache(sosObservation);
		updateGlobalTemporalBoundingBox(sosObservation.getPhenomenonTime());
		addProcedureToCache(getProcedureIdentifier(sosObservation));
		addObservablePropertyToProcedureRelation(getObservablePropertyIdentifier(sosObservation), getProcedureIdentifier(sosObservation));
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
