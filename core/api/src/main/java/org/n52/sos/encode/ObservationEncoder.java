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
package org.n52.sos.encode;

import java.util.Set;

public interface ObservationEncoder<S, T> extends Encoder<S, T> {

    /**
     * Indicator whether the ObservationEncoder of type or subtype
     * Observation&Measurement 2.0
     * 
     * @return Of type or not
     */
    boolean isObservationAndMeasurmentV20Type();

    /**
     * Indicator whether the single observations with the same procedure,
     * observableProperty and featureOfInterest should be merged to one
     * observation.
     * 
     * @return Merge or not
     */
    boolean shouldObservationsWithSameXBeMerged();

    /**
     * Get the supported response formats for this
     * {@linkplain ObservationEncoder} and the specified service and version.
     * 
     * @param service
     *            the service
     * @param version
     *            the version
     * 
     * @return the response formats
     */
    Set<String> getSupportedResponseFormats(String service, String version);

}
