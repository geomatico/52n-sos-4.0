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
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasValidProcedureTimes;
import org.n52.sos.util.CollectionHelper;

/**
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a>
 * 
 * @since 4.0.0
 */
public class TProcedure extends Procedure implements HasValidProcedureTimes, HasParentChilds<Procedure, TProcedure> {

    private static final long serialVersionUID = 3307492687846686350L;

    private Set<ValidProcedureTime> validProcedureTimes = CollectionHelper.set();

    private Set<Procedure> childs = CollectionHelper.set();

    private Set<Procedure> parents = CollectionHelper.set();

    public TProcedure() {
        super();
    }

    @Override
    public Set<ValidProcedureTime> getValidProcedureTimes() {
        return validProcedureTimes;
    }

    @Override
    public TProcedure setValidProcedureTimes(final Set<ValidProcedureTime> validProcedureTimes) {
        this.validProcedureTimes = validProcedureTimes;
        return this;
    }

    @Override
    public Set<Procedure> getParents() {
        return parents;
    }

    @Override
    public TProcedure setParents(final Set<Procedure> parents) {
        this.parents = parents;
        return this;
    }

    @Override
    public Set<Procedure> getChilds() {
        return childs;
    }

    @Override
    public TProcedure setChilds(final Set<Procedure> childs) {
        this.childs = childs;
        return this;
    }

}
