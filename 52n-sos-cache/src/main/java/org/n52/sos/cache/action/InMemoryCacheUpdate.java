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
import java.util.Collections;
import java.util.List;

import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.SosFeatureCollection;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.util.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

/**
 * TODO add log statements to all protected methods! TODO extract sub classes for insertion updates
 *
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @since 4.0
 *
 */
public abstract class InMemoryCacheUpdate implements Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryCacheUpdate.class);
    private WritableContentCache cache;

    /**
     * @return the writable cache of this action
     */
    public WritableContentCache getCache() {
        return cache;
    }

    /**
     * @param cache the writable cache for this action
     */
    public void setCache(WritableContentCache cache) {
        this.cache = cache;
    }

    /**
     * Get a list of all SosSamplingFeatures contained in the abstract feature.
     *
     * @param abstractFeature the abstract feature
     *
     * @return a list of all sampling features
     */
    protected List<SosSamplingFeature> sosFeaturesToList(SosAbstractFeature abstractFeature) {
        if (abstractFeature instanceof SosFeatureCollection) {
            return getAllFeaturesFrom((SosFeatureCollection) abstractFeature);
        } else if (abstractFeature instanceof SosSamplingFeature) {
            return Collections.singletonList((SosSamplingFeature) abstractFeature);
        } else {
            String errorMessage = String.format("Feature Type \"%s\" not supported.", abstractFeature != null
                                                                                      ? abstractFeature.getClass()
                    .getName() : abstractFeature);
            LOGGER.error(errorMessage);
            throw new IllegalArgumentException(errorMessage); // TODO change type of exception to OER?
        }
    }

    private List<SosSamplingFeature> getAllFeaturesFrom(SosFeatureCollection featureCollection) {
        List<SosSamplingFeature> features = new ArrayList<SosSamplingFeature>(featureCollection.getMembers()
                .size());
        for (SosAbstractFeature abstractFeature : featureCollection.getMembers().values()) {
            if (abstractFeature instanceof SosSamplingFeature) {
                features.add((SosSamplingFeature) abstractFeature);
            } else if (abstractFeature instanceof SosFeatureCollection) {
                features.addAll(getAllFeaturesFrom((SosFeatureCollection) abstractFeature));
            }
        }
        return features;
    }

    /**
     * Creates the Envelope for all passed sampling features.
     *
     * @param samplingFeatures the sampling features
     *
     * @return the envelope for all features
     */
    protected Envelope createEnvelopeFrom(List<SosSamplingFeature> samplingFeatures) {
        Envelope featureEnvelope = new Envelope();
        for (SosSamplingFeature samplingFeature : samplingFeatures) {
            featureEnvelope.expandToInclude(samplingFeature.getGeometry().getEnvelopeInternal());
        }
        return featureEnvelope;
    }

    @Override
    public String toString() {
        return String.format("%s [cache=%s]", getClass().getName(), cache);
    }
}
