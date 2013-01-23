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
package org.n52.sos.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void mergeObservationsWithSameAntiSubsettingIdentifier() {
        List<SosObservation> mergedObservations = new ArrayList<SosObservation>(0);
        Map<String, SosObservation> antiSubsettingObservations = new HashMap<String, SosObservation>(0);
        for (SosObservation sosObservation : observationCollection) {
            // TODO merge observations with the same antiSubsetting identifier.
            if (sosObservation.isSetAntiSubsetting()) {
                if (antiSubsettingObservations.containsKey(sosObservation.getAntiSubsetting())) {
                    SosObservation antiSubsettingObservation =
                            antiSubsettingObservations.get(sosObservation.getAntiSubsetting());
                    antiSubsettingObservation.mergeWithObservation(sosObservation);
                } else {
                    antiSubsettingObservations.put(sosObservation.getAntiSubsetting(), sosObservation);
                }
            } else {
                mergedObservations.add(sosObservation);
            }
        }
        mergedObservations.addAll(antiSubsettingObservations.values());
        this.observationCollection = mergedObservations;
    }

    public void mergeObservationsWithSameX() {
        // TODO merge all observations with the same observationContellation
        // (proc, obsProp, foi)
        if (observationCollection != null) {
            List<SosObservation> mergedObservations = new ArrayList<SosObservation>(0);
            int obsIdCounter = 1;
                for (SosObservation sosObservation : observationCollection) {
                    if (mergedObservations.isEmpty()) {
                        sosObservation.setObservationID(Integer.toString(obsIdCounter++));
                        mergedObservations.add(sosObservation);
                    } else {
                        boolean combined = false;
                        for (SosObservation combinedSosObs : mergedObservations) {
                            if (combinedSosObs.getObservationConstellation().equals(
                                    sosObservation.getObservationConstellation())) {
                                combinedSosObs.setResultTime(null);
                                combinedSosObs.mergeWithObservation(sosObservation);
                                combined = true;
                                break;
                            }
                        }
                        if (!combined) {
                            mergedObservations.add(sosObservation);
                        }
                    }
                }
            
            this.observationCollection = mergedObservations;
        }
    }

    public boolean hasObservationsWithResultTemplate() {
        for (SosObservation sosObservation : observationCollection) {
            if (sosObservation.getObservationConstellation().isSetResultTemplate()) {
                return true;
            }
        }
        return false;
    }

    /*
     * TODO uncomment when WaterML support is activated public
     * Collection<SosObservation> mergeObservations(boolean
     * mergeObservationValuesWithSameParameters) { Collection<SosObservation>
     * combinedObsCol = new ArrayList<SosObservation>(); int obsIdCounter = 1;
     * for (SosObservation sosObservation : observationCollection) { if
     * (combinedObsCol.isEmpty()) {
     * sosObservation.setObservationID(Integer.toString(obsIdCounter++));
     * combinedObsCol.add(sosObservation); } else { boolean combined = false;
     * for (SosObservation combinedSosObs : combinedObsCol) { if
     * (mergeObservationValuesWithSameParameters) { if
     * (combinedSosObs.getObservationConstellation().equals(
     * sosObservation.getObservationConstellation())) {
     * combinedSosObs.mergeWithObservation(sosObservation, false); combined =
     * true; break; } } else { if
     * (combinedSosObs.getObservationConstellation().equalsExcludingObsProp(
     * sosObservation.getObservationConstellation())) {
     * combinedSosObs.mergeWithObservation(sosObservation, true); combined =
     * true; break; } } } if (!combined) { combinedObsCol.add(sosObservation); }
     * } } return combinedObsCol; }
     */

}
