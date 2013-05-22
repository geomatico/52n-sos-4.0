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

import java.io.Serializable;

import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasStringOrClobNames;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasCodespace;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasCoordinate;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasDescriptionXml;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasFeatureOfInterestType;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasGeometry;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasUrl;
import org.n52.sos.util.StringHelper;

public class FeatureOfInterest extends SpatialEntity  implements Serializable, HasFeatureOfInterestType, HasGeometry,
HasDescriptionXml, HasStringOrClobNames, HasUrl, HasCodespace, HasCoordinate {
    
    private static final long serialVersionUID = 4142090100433622512L;
    
    public static final String ID = "featureOfInterestId";

    private long featureOfInterestId;

    private FeatureOfInterestType featureOfInterestType;

    private Codespace codespace;

    private String names;

    private String url;

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
    public Codespace getCodespace() {
        return this.codespace;
    }

    @Override
    public void setCodespace(Codespace codespace) {
        this.codespace = codespace;
    }

    @Override
    public boolean isSetCodespace() {
        return getCodespace() != null && getCodespace().isSetCodespace();
    }

    @Override
    public String getNames() {
        return this.names;
    }
    

    @Override
    public void setNames(String names) {
        this.names = names;
    }

    @Override
    public boolean isSetNames() {
        return StringHelper.isNotEmpty(names);
    }

    @Override
    public String getUrl() {
        return this.url;
    }
    
    @Override
    public void setUrl(String url) {
        this.url = url;
    }

}
