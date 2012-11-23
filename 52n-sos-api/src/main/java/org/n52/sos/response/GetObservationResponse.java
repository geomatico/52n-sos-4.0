/**
 * Copyright (C) 2012
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
package org.n52.sos.response;

import java.util.List;

import org.n52.sos.ogc.om.SosObservation;

public class GetObservationResponse extends AbstractServiceResponse {

    private String responseFormat;

    private List<SosObservation> observationCollection;

    public String getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    public List<SosObservation> getObservationCollection() {
        return observationCollection;
    }

    public void setObservationCollection(List<SosObservation> observationCollection) {
        this.observationCollection = observationCollection;
    }

    /* TODO uncomment when WaterML support is activated
    public Collection<SosObservation> mergeObservations(boolean mergeObservationValuesWithSameParameters) {
        Collection<SosObservation> combinedObsCol = new ArrayList<SosObservation>();
        int obsIdCounter = 1;
        for (SosObservation sosObservation : observationCollection) {
            if (combinedObsCol.isEmpty()) {
                sosObservation.setObservationID(Integer.toString(obsIdCounter++));
                combinedObsCol.add(sosObservation);
            } else {
                boolean combined = false;
                for (SosObservation combinedSosObs : combinedObsCol) {
                    if (mergeObservationValuesWithSameParameters) {
                        if (combinedSosObs.getObservationConstellation().equals(
                                sosObservation.getObservationConstellation())) {
                            combinedSosObs.mergeWithObservation(sosObservation, false);
                            combined = true;
                            break;
                        }
                    } else {
                        if (combinedSosObs.getObservationConstellation().equalsExcludingObsProp(
                                sosObservation.getObservationConstellation())) {
                            combinedSosObs.mergeWithObservation(sosObservation, true);
                            combined = true;
                            break;
                        }
                    }
                }
                if (!combined) {
                    combinedObsCol.add(sosObservation);
                }
            }
        }
        return combinedObsCol;
    }
    */

}
