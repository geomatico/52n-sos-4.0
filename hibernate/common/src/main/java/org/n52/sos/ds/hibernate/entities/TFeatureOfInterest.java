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

import java.util.Set;

import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasParentChilds;
import org.n52.sos.util.CollectionHelper;

/**
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a>
 * 
 * @since 4.0.0
 */
public class TFeatureOfInterest extends FeatureOfInterest implements HasParentChilds<FeatureOfInterest, TFeatureOfInterest>{

    private static final long serialVersionUID = -880472749711995015L;

    private Set<FeatureOfInterest> childs = CollectionHelper.set();

    private Set<FeatureOfInterest> parents = CollectionHelper.set();

    public TFeatureOfInterest() {
        super();
    }

    @Override
    public Set<FeatureOfInterest> getParents() {
        return parents;
    }

    @Override
    public TFeatureOfInterest setParents(final Set<FeatureOfInterest> parents) {
        this.parents = parents;
        return this;
    }

    @Override
    public Set<FeatureOfInterest> getChilds() {
        return childs;
    }

    @Override
    public TFeatureOfInterest setChilds(final Set<FeatureOfInterest> childs) {
        this.childs = childs;
        return this;
    }
}