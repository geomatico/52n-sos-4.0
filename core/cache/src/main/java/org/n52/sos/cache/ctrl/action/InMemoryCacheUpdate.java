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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.n52.sos.cache.ContentCacheUpdate;
import org.n52.sos.ogc.om.features.AbstractFeature;
import org.n52.sos.ogc.om.features.FeatureCollection;
import org.n52.sos.ogc.om.features.samplingFeatures.SamplingFeature;
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
public abstract class InMemoryCacheUpdate extends ContentCacheUpdate {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryCacheUpdate.class);

    /**
     * Get a list of all SosSamplingFeatures contained in the abstract feature.
     *
     * @param abstractFeature the abstract feature
     *
     * @return a list of all sampling features
     */
    protected List<SamplingFeature> sosFeaturesToList(AbstractFeature abstractFeature) {
        if (abstractFeature instanceof FeatureCollection) {
            return getAllFeaturesFrom((FeatureCollection) abstractFeature);
        } else if (abstractFeature instanceof SamplingFeature) {
            return Collections.singletonList((SamplingFeature) abstractFeature);
        } else {
            String errorMessage = String.format("Feature Type \"%s\" not supported.", abstractFeature != null
                                                                                      ? abstractFeature.getClass()
                    .getName() : abstractFeature);
            LOGGER.error(errorMessage);
            throw new IllegalArgumentException(errorMessage); // TODO change type of exception to OER?
        }
    }

    private List<SamplingFeature> getAllFeaturesFrom(FeatureCollection featureCollection) {
        List<SamplingFeature> features = new ArrayList<SamplingFeature>(featureCollection.getMembers()
                .size());
        for (AbstractFeature abstractFeature : featureCollection.getMembers().values()) {
            if (abstractFeature instanceof SamplingFeature) {
                features.add((SamplingFeature) abstractFeature);
            } else if (abstractFeature instanceof FeatureCollection) {
                features.addAll(getAllFeaturesFrom((FeatureCollection) abstractFeature));
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
    protected Envelope createEnvelopeFrom(List<SamplingFeature> samplingFeatures) {
        Envelope featureEnvelope = new Envelope();
        for (SamplingFeature samplingFeature : samplingFeatures) {
            featureEnvelope.expandToInclude(samplingFeature.getGeometry().getEnvelopeInternal());
        }
        return featureEnvelope;
    }

    @Override
    public String toString() {
        return String.format("%s [cache=%s]", getClass().getName(), getCache());
    }
}
