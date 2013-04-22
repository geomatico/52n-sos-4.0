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
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasObservationConstellation;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasResultEncoding;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasResultStructure;

public class ResultTemplate implements Serializable, HasIdentifier, HasFeatureOfInterest, HasResultStructure,
                                       HasResultEncoding, HasObservationConstellation {
    public static final String ID = "resultTemplateId";
    private static final long serialVersionUID = -5283637712798249905L;
    private long resultTemplateId;
    private FeatureOfInterest featureOfInterest;
    private ObservationConstellation observationConstellation;
    private String identifier;
    private String resultStructure;
    private String resultEncoding;

    public ResultTemplate() {
    }

    public long getResultTemplateId() {
        return resultTemplateId;
    }

    public void setResultTemplateId(final long resultTemplateId) {
        this.resultTemplateId = resultTemplateId;
    }

    @Override
    public FeatureOfInterest getFeatureOfInterest() {
        return featureOfInterest;
    }

    @Override
    public void setFeatureOfInterest(final FeatureOfInterest featureOfInterest) {
        this.featureOfInterest = featureOfInterest;
    }

    @Override
    public ObservationConstellation getObservationConstellation() {
        return observationConstellation;
    }

    @Override
    public void setObservationConstellation(
            final ObservationConstellation observationConstellation) {
        this.observationConstellation = observationConstellation;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public ResultTemplate setIdentifier(final String identifier) {
        this.identifier = identifier;
        return this;
    }

    @Override
    public String getResultStructure() {
        return resultStructure;
    }

    @Override
    public void setResultStructure(final String resultStructure) {
        this.resultStructure = resultStructure;
    }

    @Override
    public String getResultEncoding() {
        return resultEncoding;
    }

    @Override
    public void setResultEncoding(final String resultEncoding) {
        this.resultEncoding = resultEncoding;
    }
}
