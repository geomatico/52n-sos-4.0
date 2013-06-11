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
import java.util.List;

import org.n52.sos.ogc.om.OmObservation;

public class GetObservationResponse extends AbstractServiceResponse {

    private String responseFormat;

    private List<OmObservation> observationCollection;

    private String resultModel;

    public String getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    public List<OmObservation> getObservationCollection() {
        return observationCollection;
    }

    public void setObservationCollection(List<OmObservation> observationCollection) {
        this.observationCollection = observationCollection;
    }

    public void mergeObservationsWithSameX() {
        // TODO merge all observations with the same observationContellation
        // (proc, obsProp, foi)
        if (observationCollection != null) {
            List<OmObservation> mergedObservations = new ArrayList<OmObservation>(0);
            int obsIdCounter = 1;
                for (OmObservation sosObservation : observationCollection) {
                    if (mergedObservations.isEmpty()) {
                        sosObservation.setObservationID(Integer.toString(obsIdCounter++));
                        mergedObservations.add(sosObservation);
                    } else {
                        boolean combined = false;
                        for (OmObservation combinedSosObs : mergedObservations) {
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

    public void setResultModel(String resultModel) {
        this.resultModel = resultModel;
    }
    
    public String getResultModel() {
        return resultModel;
    }
    
    public boolean isSetResultModel() {
        return resultModel != null;
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
