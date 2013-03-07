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

import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.util.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

/**
 * When executing this &auml;ction (see {@link Action}), the following relations are added, settings are updated in
 * cache:<ul>
 * <li>'Result template identifier' &rarr; 'observable property' relation</li>
 * <li>'Result template identifier' &rarr; 'feature of interest' relation</li></ul>
 * TODO update list above
 *
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @since 4.0
 */
public class ResultInsertionInMemoryCacheUpdate extends InMemoryCacheUpdate {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultInsertionInMemoryCacheUpdate.class);
    private final SosObservation observation;
    private final String templateIdentifier;

    public ResultInsertionInMemoryCacheUpdate(String templateIdentifier, SosObservation observation) {
        if (observation == null || templateIdentifier == null || templateIdentifier.isEmpty()) {
            String msg = String.format("Missing argument: '{}': {}; template identifier: '{}'",
                                       SosObservation.class.getName(),
                                       observation,
                                       templateIdentifier);
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
        this.observation = observation;
        this.templateIdentifier = templateIdentifier;
    }

    @Override
    public void execute() {
        // TODO remove not required updates and adjust test accordingly
        final WritableContentCache cache = getCache();
        final String observationType = observation.getObservationConstellation().getObservationType();
        final String procedure = observation.getObservationConstellation().getProcedure().getProcedureIdentifier();
        final String observableProperty = observation.getObservationConstellation().getObservableProperty().getIdentifier();
        final ITime phenomenonTime = observation.getPhenomenonTime();
        final ITime resultTime = observation.getResultTime();

        cache.updatePhenomenonTime(phenomenonTime);
        cache.updateResultTime(resultTime);

        cache.addProcedure(procedure);

        cache.addProcedureForObservableProperty(observableProperty, procedure);
        cache.addObservablePropertyForResultTemplate(templateIdentifier, observableProperty);
        cache.addObservablePropertyForProcedure(procedure, observableProperty);

        if (observation.getIdentifier() != null) {
            final String identifier = observation.getIdentifier().getValue();
            cache.addObservationIdentifier(identifier);
            cache.addObservationIdentifierForProcedure(procedure, identifier);
        }

        List<SosSamplingFeature> observedFeatures = sosFeaturesToList(observation.getObservationConstellation()
                .getFeatureOfInterest());

        final Envelope envelope = createEnvelopeFrom(observedFeatures);

        cache.updateGlobalEnvelope(envelope);

        for (SosSamplingFeature sosSamplingFeature : observedFeatures) {
            final String featureOfInterest = sosSamplingFeature.getIdentifier().getValue();
            cache.addFeatureOfInterest(featureOfInterest);
            cache.addFeatureOfInterestForResultTemplate(templateIdentifier, featureOfInterest);
            cache.addProcedureForFeatureOfInterest(featureOfInterest, procedure);
            for (String offering : observation.getObservationConstellation().getOfferings()) {
                cache.addFeatureOfInterestForOffering(offering, featureOfInterest);
            }
        }
        for (String offering : observation.getObservationConstellation().getOfferings()) {
            cache.addProcedureForOffering(offering, procedure);
            cache.addOfferingForProcedure(procedure, offering);
            cache.updateEnvelopeForOffering(offering, envelope);
            cache.updatePhenomenonTimeForOffering(offering, phenomenonTime);
            cache.updateResultTimeForOffering(offering, resultTime);
            // observable property
            cache.addOfferingForObservableProperty(observableProperty, offering);
            cache.addObservablePropertyForOffering(offering, observableProperty);
            // observation type
            cache.addObservationTypesForOffering(offering, observationType);
        }
    }
}
