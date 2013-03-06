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

import java.util.Set;

import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.request.DeleteSensorRequest;
import org.n52.sos.util.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When executing this &auml;ction (see {@link Action}), the following relations are deleted, settings are updated in
 * cache:<ul>
 * <li>Result template</li>
 * <li>Offering &rarr; Result template</li></ul>
 *
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @since 4.0
 */
public class SensorDeletionInMemoryCacheUpdate extends InMemoryCacheUpdate {
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorDeletionInMemoryCacheUpdate.class);
    private final DeleteSensorRequest request;

    public SensorDeletionInMemoryCacheUpdate(DeleteSensorRequest response) {
        if (response == null) {
            String msg = String.format("Missing argument: '{}': {}",
                                       DeleteSensorRequest.class.getName(),
                                       response);
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
        this.request = response;
    }

    @Override
    public void execute() {
        final WritableContentCache cache = getCache();
        final String procedure = request.getProcedureIdentifier();

        cache.removeProcedure(procedure);
        for (String feature : cache.getFeaturesOfInterest()) {
            cache.removeProcedureForFeatureOfInterest(feature, procedure);
            if (cache.getProceduresForFeatureOfInterest(feature).isEmpty()) {
                cache.removeProceduresForFeatureOfInterest(feature);
            }
        }

        final Set<String> observationIdentifiers = cache.getObservationIdentifiersForProcedure(procedure);
        cache.removeObservationIdentifiersForProcedure(procedure);
        cache.removeObservationIdentifiers(observationIdentifiers);

        for (String offering : cache.getOfferingsForProcedure(procedure)) {
            cache.removeMaxTimeForOffering(offering);
            cache.removeMinTimeForOffering(offering);
            cache.removeNameForOffering(offering);
            cache.removeFeaturesOfInterestForOffering(offering);
            cache.removeRelatedFeaturesForOffering(offering);
            cache.removeObservationTypesForOffering(offering);
            cache.removeEnvelopeForOffering(offering);
            for (String observableProperty : cache.getObservablePropertiesForOffering(offering)) {
                cache.removeOfferingForObservableProperty(observableProperty, offering);
            }
            cache.removeObservablePropertiesForOffering(offering);
            Set<String> resultTemplatesToRemove = cache.getResultTemplatesForOffering(offering);
            cache.removeResultTemplatesForOffering(offering);
            cache.removeResultTemplates(resultTemplatesToRemove);
            for (String resultTemplate : resultTemplatesToRemove) {
                cache.removeFeaturesOfInterestForResultTemplate(resultTemplate);
                cache.removeObservablePropertiesForResultTemplate(resultTemplate);
            }
            cache.removeOfferingForProcedure(procedure, offering);
            cache.removeProcedureForOffering(offering, procedure);
        }

        cache.removeRolesForRelatedFeatureNotIn(cache.getRelatedFeatures());
        cache.setFeaturesOfInterest(cache.getFeaturesOfInterestWithOffering());

        // observable property relations
        for (String observableProperty : cache.getObservablePropertiesForProcedure(procedure)) {
            cache.removeProcedureForObservableProperty(observableProperty, procedure);
            cache.removeObservablePropertyForProcedure(procedure, observableProperty);
        }
        // At the latest
        cache.removeOfferingsForProcedure(procedure);
        cache.recalculateEventTime();
        cache.recalculateGlobalEnvelope();
    }
}
