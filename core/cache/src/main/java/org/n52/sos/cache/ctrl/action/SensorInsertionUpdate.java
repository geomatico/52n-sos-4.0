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

import java.util.Collection;

import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.ogc.om.SosOffering;
import org.n52.sos.ogc.swe.SosFeatureRelationship;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.InsertSensorResponse;
import org.n52.sos.util.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When executing this &auml;ction (see {@link Action}), the following relations are added and some settings are updated
 * in cache:<ul>
 * <li>Procedure</li>
 * <li>Offering &harr; procedure</li>
 * <li>Offering &rarr; name</li></ul>
 * <li>Offering &rarr; allowed observation type</li>
 * <li>Offering &rarr; related feature</li>
 * <li>Related features &rarr; role</li>
 * <li>Observable Property &harr; Procedure</li>
 * <li>Offering &harr; observable property</li>
 *
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @since 4.0
 *
 */
public class SensorInsertionUpdate extends InMemoryCacheUpdate {
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorInsertionUpdate.class);
    private final InsertSensorResponse response;
    private final InsertSensorRequest request;

    public SensorInsertionUpdate(InsertSensorRequest request, InsertSensorResponse response) {
        if (request == null || response == null) {
            String msg = String.format("Missing argument: '%s': %s; '%s': %s",
                                       InsertSensorRequest.class.getName(), request,
                                       InsertSensorResponse.class.getName(), response);
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
        this.response = response;
        this.request = request;
    }

    @Override
    public void execute() {
        final WritableContentCache cache = getCache();
        final String procedure = response.getAssignedProcedure();
        final String offering = response.getAssignedOffering();

        // procedure relations
        cache.addProcedure(procedure);
        cache.addParentProcedures(procedure, request.getProcedureDescription().getParentProcedures());
        //TODO child procedures
        
        // offerings
        for (SosOffering sosOffering : request.getProcedureDescription().getOfferings()) {
            cache.addOffering(offering);
            cache.addProcedureForOffering(offering, procedure);
            cache.addOfferingForProcedure(procedure, offering);
            if (sosOffering.isSetOfferingName()) {
                cache.setNameForOffering(sosOffering.getOfferingIdentifier(), sosOffering.getOfferingName());
            }
        }

        // allowed observation types
        cache.addAllowedObservationTypesForOffering(offering, request.getMetadata().getObservationTypes());

        // related features
        final Collection<SosFeatureRelationship> relatedFeatures = request.getRelatedFeatures();
        if (relatedFeatures != null && !relatedFeatures.isEmpty()) {
            for (SosFeatureRelationship relatedFeature : relatedFeatures) {
                final String identifier = relatedFeature.getFeature().getIdentifier().getValue();
                cache.addRelatedFeatureForOffering(offering, identifier);
                cache.addRoleForRelatedFeature(identifier, relatedFeature.getRole());
            }
        }

        // observable property relations
        for (String observableProperty : request.getObservableProperty()) {
            cache.addProcedureForObservableProperty(observableProperty, procedure);
            cache.addObservablePropertyForProcedure(procedure, observableProperty);
            cache.addOfferingForObservableProperty(observableProperty, offering);
            cache.addObservablePropertyForOffering(offering, observableProperty);
        }
    }
}
