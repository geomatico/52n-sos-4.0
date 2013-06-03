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
package org.n52.sos.ogc.sos;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.n52.sos.util.CollectionHelper;

/**
 * SOS internal representation of SOS insertion metadata
 * 
 */
public class SosInsertionMetadata {

    /**
     * list of valid feature types
     */
    private Set<String> featureOfInterestTypes;

    /**
     * list of valid observation types
     */
    private Set<String> observationTypes;

    /**
     * constructor
     */
    public SosInsertionMetadata() {
        super();
    }

    /**
     * @return the featureOfInterestTypes
     */
    public Set<String> getFeatureOfInterestTypes() {
        return featureOfInterestTypes;
    }

    /**
     * @param featureOfInterestTypes
     *            the featureOfInterestTypes to set
     */
    public void setFeatureOfInterestTypes(Collection<String> featureOfInterestTypes) {
        this.featureOfInterestTypes = CollectionHelper.asSet(featureOfInterestTypes);
    }

    /**
     * @return the observationTypes
     */
    public Set<String> getObservationTypes() {
        return observationTypes;
    }

    /**
     * @param observationTypes
     *            the observationTypes to set
     */
    public void setObservationTypes(Collection<String> observationTypes) {
        this.observationTypes = CollectionHelper.asSet(observationTypes);
    }
}
