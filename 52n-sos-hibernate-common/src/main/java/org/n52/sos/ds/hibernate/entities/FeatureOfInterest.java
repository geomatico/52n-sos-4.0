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

import java.util.HashSet;
import java.util.Set;

import org.n52.sos.ds.FeatureQueryHandler;

import com.vividsolutions.jts.geom.Geometry;

public class FeatureOfInterest implements java.io.Serializable {
    private static final long serialVersionUID = -4296313199622310037L;
    private long featureOfInterestId;
    private FeatureOfInterestType featureOfInterestType;
    private String identifier;
    private Codespace codespace;
    private String name;
    private Geometry geom;
    private String descriptionXml;
    private String url;
    
    private Object longitude;
    
    private Object latitude;
    
    private Object altitude;
    
    private int srid;
    private Set<FeatureOfInterest> featureOfInterestsForChildFeatureId = new HashSet<FeatureOfInterest>(0);
    private Set<FeatureOfInterest> featureOfInterestsForParentFeatureId = new HashSet<FeatureOfInterest>(0);

    public FeatureOfInterest() {
    }

    public long getFeatureOfInterestId() {
        return this.featureOfInterestId;
    }

    public void setFeatureOfInterestId(long featureOfInterestId) {
        this.featureOfInterestId = featureOfInterestId;
    }

    public FeatureOfInterestType getFeatureOfInterestType() {
        return this.featureOfInterestType;
    }

    public void setFeatureOfInterestType(FeatureOfInterestType featureOfInterestType) {
        this.featureOfInterestType = featureOfInterestType;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Codespace getCodespace() {
        return this.codespace;
    }

    public void setCodespace(Codespace codespace) {
        this.codespace = codespace;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * DO NOT ACCESS THE GEOMETRY OF THIS OBJECT DIRECTLY.
     * <p/>
     * Instead use {@link FeatureQueryHandler#getFeatureByID(java.lang.String, java.lang.Object, java.lang.String, int)}
     * to retrieve the Geometry.
     */
    public Geometry getGeom() {
        return this.geom;
    }

    public void setGeom(Geometry geom) {
        this.geom = geom;
    }

    public String getDescriptionXml() {
        return this.descriptionXml;
    }

    public void setDescriptionXml(String descriptionXml) {
        this.descriptionXml = descriptionXml;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Object getLongitude() {
        return longitude;
    }

    public void setLongitude(Object longitude) {
        this.longitude = longitude;
    }

    public Object getLatitude() {
        return latitude;
    }

    public void setLatitude(Object latitude) {
        this.latitude = latitude;
    }

    public Object getAltitude() {
        return altitude;
    }

    public void setAltitude(Object altitude) {
        this.altitude = altitude;
    }

    public int getSrid() {
        return srid;
    }

    public void setSrid(int srid) {
        this.srid = srid;
    }

    public Set<FeatureOfInterest> getFeatureOfInterestsForChildFeatureId() {
        return this.featureOfInterestsForChildFeatureId;
    }

    public void setFeatureOfInterestsForChildFeatureId(Set<FeatureOfInterest> featureOfInterestsForChildFeatureId) {
        this.featureOfInterestsForChildFeatureId = featureOfInterestsForChildFeatureId;
    }

    public Set<FeatureOfInterest> getFeatureOfInterestsForParentFeatureId() {
        return this.featureOfInterestsForParentFeatureId;
    }

    public void setFeatureOfInterestsForParentFeatureId(Set<FeatureOfInterest> featureOfInterestsForParentFeatureId) {
        this.featureOfInterestsForParentFeatureId = featureOfInterestsForParentFeatureId;
    }
    
    public boolean isSetGeometry() {
        return getGeom() != null;
    }
    
    public boolean isSetLongLat() {
        return getLongitude() != null && getLatitude() != null;
    }
    
    public boolean isSetAltitude() {
        return getAltitude() != null;
    }
    
    public boolean isSetSrid() {
        return getSrid() > 0;
    }

    public boolean isSetCodespace() {
        return getCodespace() != null && getCodespace().isSetCodespace();
    }
}
