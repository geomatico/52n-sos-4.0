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
package org.n52.sos.cache.ctrl.action;

import java.util.List;

import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.ogc.gml.time.Time;
import org.n52.sos.ogc.om.OmObservation;
import org.n52.sos.ogc.om.features.AbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.util.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

/**
 * When executing this &auml;ction (see {@link Action}), the following relations are added, settings are updated in
 * cache:<ul>
 * <li>Observation Type</li>
 * <li>Observation identifier (OPTIONAL)</li>
 * <li>Procedure &rarr; Observation identifier (OPTIONAL)</li>
 * <li>Global spatial bounding box</li>
 * <li>Feature identifier</li>
 * <li>Feature types</li>
 * <li>Feature &harr; procedure</li>
 * <li>Feature &harr; feature</li>
 * <li>Offering &harr; related feature</li>
 * <li>Offering &harr; procedure</li>
 * <li>Offering &harr; observable property</li>
 * <li>Offering &rarr; observation type</li>
 * <li>Offering &rarr; temporal bounding box</li>
 * <li>Offering &rarr; spatial bounding box</li>
 * <li>Procedure &rarr; temporal bounding box</li> 
 * <li>Global temporal bounding box</li></ul>
 *
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @since 4.0
 *
 */
public class ObservationInsertionUpdate extends InMemoryCacheUpdate {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationInsertionUpdate.class);
    private final InsertObservationRequest request;

    public ObservationInsertionUpdate(InsertObservationRequest request) {
        if (request == null) {
            String msg = String.format("Missing argument: '%s': %s",
                                       InsertObservationRequest.class.getName(),
                                       request);
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
        this.request = request;
    }

    @Override
    public void execute() {
        final WritableContentCache cache = getCache();
        // TODO Review required methods and update test accordingly (@see SensorInsertionInMemoryCacheUpdate)
        // Always update the javadoc when changing this method!
        for (OmObservation observation : request.getObservations()) {
            final String observableProperty = observation.getObservationConstellation().getObservableProperty()
                    .getIdentifier();
            final String observationType = observation.getObservationConstellation().getObservationType();
            final String procedure = observation.getObservationConstellation().getProcedure().getIdentifier();
            final Time phenomenonTime = observation.getPhenomenonTime();
            final Time resultTime = observation.getResultTime();

            cache.updatePhenomenonTime(phenomenonTime);
            cache.updateResultTime(resultTime);
            cache.updatePhenomenonTimeForProcedure(procedure, phenomenonTime);
            
            if (observation.getIdentifier() != null) {
                final String identifier = observation.getIdentifier().getValue();
                cache.addObservationIdentifier(identifier);
                cache.addObservationIdentifierForProcedure(procedure, identifier);
            }

            // update features
            List<SamplingFeature> observedFeatures = sosFeaturesToList(observation.getObservationConstellation()
                    .getFeatureOfInterest());

            final Envelope envelope = createEnvelopeFrom(observedFeatures);
            cache.updateGlobalEnvelope(envelope);

            for (SamplingFeature sosSamplingFeature : observedFeatures) {
                String featureOfInterest = sosSamplingFeature.getIdentifier().getValue();

                cache.addFeatureOfInterest(featureOfInterest);
                cache.addProcedureForFeatureOfInterest(featureOfInterest, procedure);
                if (sosSamplingFeature.isSetSampledFeatures()) {
                    for (AbstractFeature parentFeature : sosSamplingFeature.getSampledFeatures()) {
                        getCache().addParentFeature(sosSamplingFeature.getIdentifier().getValue(),
                                                    parentFeature.getIdentifier().getValue());
                    }
                }
                for (String offering : request.getOfferings()) {
                    cache.addRelatedFeatureForOffering(offering, featureOfInterest);
                    cache.addFeatureOfInterestForOffering(offering, featureOfInterest);
                }
            }

            // update offerings
            for (String offering : request.getOfferings()) {
                // procedure
                cache.addOffering(offering);
                cache.addProcedureForOffering(offering, procedure);
                cache.addOfferingForProcedure(procedure, offering);
                // observable property
                cache.addOfferingForObservableProperty(observableProperty, offering);
                cache.addObservablePropertyForOffering(offering, observableProperty);
                // observation type
                cache.addObservationTypesForOffering(offering, observationType);
                // envelopes/bounding boxes (spatial and temporal)
                cache.updatePhenomenonTimeForOffering(offering, phenomenonTime);
                cache.updateResultTimeForOffering(offering, resultTime);
                cache.updateEnvelopeForOffering(offering, envelope);
            }

        }
    }
}
