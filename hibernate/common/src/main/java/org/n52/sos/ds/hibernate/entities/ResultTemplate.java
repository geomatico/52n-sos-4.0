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

import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasFeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasIdentifier;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasObservableProperty;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasOffering;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasProcedure;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasResultEncoding;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasResultStructure;
import org.n52.sos.util.StringHelper;

public class ResultTemplate implements Serializable, HasIdentifier, HasProcedure, HasObservableProperty, HasOffering,
        HasFeatureOfInterest, HasResultStructure, HasResultEncoding {

    private static final long serialVersionUID = -8847952458819368733L;

    public static final String ID = "resultTemplateId";

    private long resultTemplateId;

    private Offering offering;

    private ObservableProperty observableProperty;

    private Procedure procedure;

    private FeatureOfInterest featureOfInterest;

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

    @Override
    public Offering getOffering() {
        return this.offering;
    }

    @Override
    public void setOffering(Offering offering) {
        this.offering = offering;
    }

    @Override
    public ObservableProperty getObservableProperty() {
        return this.observableProperty;
    }

    @Override
    public void setObservableProperty(ObservableProperty observableProperty) {
        this.observableProperty = observableProperty;
    }

    @Override
    public Procedure getProcedure() {
        return this.procedure;
    }

    public void setProcedure(Procedure procedure) {
        this.procedure = procedure;
    }

    @Override
    public FeatureOfInterest getFeatureOfInterest() {
        return this.featureOfInterest;
    }

    @Override
    public void setFeatureOfInterest(FeatureOfInterest featureOfInterest) {
        this.featureOfInterest = featureOfInterest;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    public ResultTemplate setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }


    @Override
    public String getResultStructure() {
        return resultStructure;
    }
    
    public boolean isSetResultStructure() {
        return StringHelper.isNotEmpty(resultStructure);
    }

    @Override
    public void setResultStructure(String resultStructure) {
        this.resultStructure = resultStructure;
    }

    @Override
    public String getResultEncoding() {
        return this.resultEncoding;
    }

    @Override
    public boolean isSetResultEncoding() {
        return StringHelper.isNotEmpty(resultEncoding);
    }

    @Override
    public void setResultEncoding(String resultEncoding) {
        this.resultEncoding = resultEncoding;
    }

}
