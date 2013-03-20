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
package org.n52.sos.ds.hibernate.entities;

public class ResultTemplate implements java.io.Serializable {
    private static final long serialVersionUID = -5283637712798249905L;
    private long resultTemplateId;
    private FeatureOfInterest featureOfInterest;
    private ObservationConstellationOfferingObservationType observationConstellationOfferingObservationType;
    private String identifier;
    private String resultStructure;
    private String resultEncoding;

    public ResultTemplate() {
    }

    public long getResultTemplateId() {
        return this.resultTemplateId;
    }

    public void setResultTemplateId(long resultTemplateId) {
        this.resultTemplateId = resultTemplateId;
    }

    public FeatureOfInterest getFeatureOfInterest() {
        return this.featureOfInterest;
    }

    public void setFeatureOfInterest(FeatureOfInterest featureOfInterest) {
        this.featureOfInterest = featureOfInterest;
    }

    public ObservationConstellationOfferingObservationType getObservationConstellationOfferingObservationType() {
        return this.observationConstellationOfferingObservationType;
    }

    public void setObservationConstellationOfferingObservationType(
            ObservationConstellationOfferingObservationType observationConstellationOfferingObservationType) {
        this.observationConstellationOfferingObservationType = observationConstellationOfferingObservationType;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getResultStructure() {
        return this.resultStructure;
    }

    public void setResultStructure(String resultStructure) {
        this.resultStructure = resultStructure;
    }

    public String getResultEncoding() {
        return this.resultEncoding;
    }

    public void setResultEncoding(String resultEncoding) {
        this.resultEncoding = resultEncoding;
    }
}
