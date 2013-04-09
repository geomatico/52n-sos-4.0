/**
 * Copyright (C) 2013 by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk 52 North Initiative for Geospatial Open Source Software GmbH Martin-Luther-King-Weg 24 48155
 * Muenster, Germany info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under the terms of the GNU General Public
 * License version 2 as published by the Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied WARRANTY OF MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program (see gnu-gpl v2.txt). If
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or visit
 * the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.sos.ds.hibernate.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.n52.sos.ds.FeatureQueryHandler;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasCodespace;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasCoordinate;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasDescriptionXml;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasFeatureOfInterestType;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasGeometry;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasIdentifier;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasName;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasUrl;

import com.vividsolutions.jts.geom.Geometry;

public class FeatureOfInterest implements Serializable, HasIdentifier, HasFeatureOfInterestType, HasGeometry,
                                          HasDescriptionXml, HasName, HasUrl, HasCodespace, HasCoordinate {
    public static final String ID = "featureOfInterestId";
    public static final String FEATURE_OF_INTEREST_FOR_CHILD_FEATURE_ID = "featureOfInterestsForChildFeatureId";
    public static final String FEATURE_OF_INTEREST_FOR_PARENT_FEATURE_ID = "featureOfInterestsForParentFeatureId";
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

    @Override
    public FeatureOfInterestType getFeatureOfInterestType() {
        return this.featureOfInterestType;
    }

    @Override
    public void setFeatureOfInterestType(FeatureOfInterestType featureOfInterestType) {
        this.featureOfInterestType = featureOfInterestType;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public Codespace getCodespace() {
        return this.codespace;
    }

    @Override
    public void setCodespace(Codespace codespace) {
        this.codespace = codespace;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * DO NOT ACCESS THE GEOMETRY OF THIS OBJECT DIRECTLY.
     * <p/>
     * Instead use {@link FeatureQueryHandler#getFeatureByID(java.lang.String, java.lang.Object, java.lang.String, int)}
     * to retrieve the Geometry.
     */
    @Override
    public Geometry getGeom() {
        return this.geom;
    }

    @Override
    public void setGeom(Geometry geom) {
        this.geom = geom;
    }

    @Override
    public String getDescriptionXml() {
        return this.descriptionXml;
    }

    @Override
    public void setDescriptionXml(String descriptionXml) {
        this.descriptionXml = descriptionXml;
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public Object getLongitude() {
        return longitude;
    }

    @Override
    public void setLongitude(Object longitude) {
        this.longitude = longitude;
    }

    @Override
    public Object getLatitude() {
        return latitude;
    }

    @Override
    public void setLatitude(Object latitude) {
        this.latitude = latitude;
    }

    @Override
    public Object getAltitude() {
        return altitude;
    }

    @Override
    public void setAltitude(Object altitude) {
        this.altitude = altitude;
    }

    @Override
    public int getSrid() {
        return srid;
    }

    @Override
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
