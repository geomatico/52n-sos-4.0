/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.ogc.om;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.n52.sos.ogc.AbstractServiceResponseObject;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.features.SosAbstractFeature;

import com.vividsolutions.jts.geom.Envelope;

/**
 * class represents an observation collection
 * 
 */
public class SosObservationCollection extends AbstractServiceResponseObject {

    /**
     * date indicating when the result set will expire; if this date is not
     * specified (null) the result set expires immediately
     */
    private DateTime expiresDate;

    /** list containing the members of this observation collection */
    private Collection<SosObservation> observationMembers;

    /**
     * two element multipoint in WKT format, which represents the BoundingBox
     * for which values are returned
     */
    private Envelope boundedBy;

    /** the srid id of the BoundingBox **/
    private int srid;

    /**
     * identifier for observation collection
     */
    private String observationCollectionIdentifier;

    /**
     * map with feature identifier(k) and SOS abstract feature(v)
     */
    private Map<String, SosAbstractFeature> features;

    private String responseFormat;

    /**
     * @param identifier
     *            identifier of this observation collection
     * @param observationMembers
     *            list containing the members of this observation collection
     * @param time
     *            time period, for which values are returned in this observation
     *            collection
     * @param boundedBy
     *            Envelope which represents the BoundingBox for which values are
     *            returned
     */
    public SosObservationCollection(String identifier, DateTime expiresDate, List<SosObservation> observationMembers,
            TimePeriod time, Envelope boundedBy, String responseFormat) {
        observationCollectionIdentifier = identifier;
        this.expiresDate = expiresDate;
        this.observationMembers = observationMembers;
        // this.time = time;
        this.boundedBy = boundedBy;
        this.setResponseFormat(responseFormat);
    }

    /**
     * default constructor
     * 
     */
    public SosObservationCollection(String responseFormat) {
        this.setResponseFormat(responseFormat);
    }

    /**
     * @return the responseFormat
     */
    public String getResponseFormat() {
        return responseFormat;
    }

    /**
     * @param responseFormat
     *            the responseFormat to set
     */
    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    /**
     * Get the envelope
     * 
     * @return the boundedBy
     */
    public Envelope getBoundedBy() {
        return boundedBy;
    }

    /**
     * Set the envelope
     * 
     * @param boundedBy
     *            the boundedBy to set
     */
    public void setBoundedBy(Envelope boundedBy) {
        this.boundedBy = boundedBy;
    }

    /**
     * Set SRID
     * 
     * @param srid
     *            the srid of boundedByWKT to set
     */
    public void setSrid(int srid) {
        this.srid = srid;
    }

    /**
     * Get SRID
     * 
     * @return the srid of boundedBy
     */
    public int getSrid() {
        return srid;
    }

    /**
     * Get expires date
     * 
     * @return the expiresDate
     */
    public DateTime getExpiresDate() {
        return expiresDate;
    }

    /**
     * Set expires date
     * 
     * @param expiresDate
     *            the expiresDate to set
     */
    public void setExpiresDate(DateTime expiresDate) {
        this.expiresDate = expiresDate;
    }

    /**
     * Get observations
     * 
     * @return the observationMembers
     */
    public Collection<SosObservation> getObservationMembers() {
        return observationMembers;
    }

    /**
     * Set observations
     * 
     * @param observationMembers
     *            the observationMembers to set
     */
    public void setObservationMembers(Collection<SosObservation> observationMembers) {
        this.observationMembers = observationMembers;
    }

    /**
     * adds passed ObservationCollection to observation collection
     * 
     * @param obsColToAdd
     *            observation collection, which should be added
     */
    public void addColllection(SosObservationCollection obsColToAdd) {

        // initialize, if necessary
        if (this.observationMembers == null) {
            this.observationMembers = new ArrayList<SosObservation>();
        }
        if (this.features == null) {
            this.features = new HashMap<String, SosAbstractFeature>();
        }
        // initialize, if necessary
        if (this.boundedBy == null) {
            this.boundedBy = new Envelope();
        }
        if (obsColToAdd.getSrid() != 0 || obsColToAdd.getSrid() != -1) {
            this.srid = obsColToAdd.getSrid();
        }
        if (obsColToAdd.getObservationMembers() != null) {
            this.observationMembers.addAll(obsColToAdd.getObservationMembers());
            if (obsColToAdd.getBoundedBy() != null) {
                this.boundedBy.expandToInclude(obsColToAdd.getBoundedBy());
            }
        }
        if (obsColToAdd.getFeatures() != null) {
            for (String foiID : obsColToAdd.getFeatures().keySet()) {
                if (!this.features.containsKey(foiID)) {
                    this.features.put(foiID, obsColToAdd.getFeatures().get(foiID));
                }
            }
        }
    }

    /**
     * Get the identifier
     * 
     * @return the observationCollectionIdentifier : String
     */
    public String getObservationCollectionIdentifier() {
        return observationCollectionIdentifier;
    }

    /**
     * @param observationCollectionIdentifier
     *            the observationCollectionIdentifier to set
     */
    public void setObservationCollectionIdentifier(String observationCollectionIdentifier) {
        this.observationCollectionIdentifier = observationCollectionIdentifier;
    }

    /**
     * Get features map
     * 
     * @return the features
     */
    public Map<String, SosAbstractFeature> getFeatures() {
        return features;
    }

    /**
     * Set features map
     * 
     * @param features
     *            the features to set
     */
    public void setFeatures(Map<String, SosAbstractFeature> features) {
        this.features = features;
    }

    public void addObservation(SosObservation sosObs) {
        if (this.observationMembers == null) {
            this.observationMembers = new ArrayList<SosObservation>();
        }
        if (this.features == null) {
            this.features = new HashMap<String, SosAbstractFeature>();
        }
        if (sosObs != null) {
            this.observationMembers.add(sosObs);
        }
    }

}
