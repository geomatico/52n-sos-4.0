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
import java.util.HashSet;
import java.util.Set;

import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasCoordinate;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasDeletedFlag;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasGeometry;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasProcedureDescriptionFormat;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasValidProcedureTimes;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 * @since 4.0.0
 */
public class Procedure extends SpatialEntity implements Serializable, HasDeletedFlag,
                                  HasProcedureDescriptionFormat, HasValidProcedureTimes, HasGeometry, HasCoordinate {
    public static final String ID = "procedureId";
    public static final String PROCEDURES_FOR_CHILD_SENSOR_ID = "proceduresForChildSensorId";
    public static final String PROCEDURES_FOR_PARENT_SENSOR_ID = "proceduresForParentSensorId";
    private static final long serialVersionUID = -4982481368011439515L;
    private long procedureId;
    private ProcedureDescriptionFormat procedureDescriptionFormat;
    private boolean deleted;
    private Set<ValidProcedureTime> validProcedureTimes = new HashSet<ValidProcedureTime>(0);
    private Set<Procedure> proceduresForChildSensorId = new HashSet<Procedure>(0);
    private Set<Procedure> proceduresForParentSensorId = new HashSet<Procedure>(0);

    public Procedure() {
    }

    public long getProcedureId() {
        return procedureId;
    }

    public Procedure setProcedureId(final long procedureId) {
        this.procedureId = procedureId;
        return this;
    }

    @Override
    public ProcedureDescriptionFormat getProcedureDescriptionFormat() {
        return procedureDescriptionFormat;
    }

    @Override
    public Procedure setProcedureDescriptionFormat(final ProcedureDescriptionFormat procedureDescriptionFormat) {
        this.procedureDescriptionFormat = procedureDescriptionFormat;
        return this;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
	public Procedure setDeleted(final boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    @Override
    public Set<ValidProcedureTime> getValidProcedureTimes() {
        return validProcedureTimes;
    }

    @Override
    public Procedure setValidProcedureTimes(final Set<ValidProcedureTime> validProcedureTimes) {
        this.validProcedureTimes = validProcedureTimes;
        return this;
    }

    public Set<Procedure> getProceduresForChildSensorId() {
        return proceduresForChildSensorId;
    }

    public Procedure setProceduresForChildSensorId(final Set<Procedure> proceduresForChildSensorId) {
        this.proceduresForChildSensorId = proceduresForChildSensorId;
        return this;
    }

    public Set<Procedure> getProceduresForParentSensorId() {
        return proceduresForParentSensorId;
    }

    public Procedure setProceduresForParentSensorId(final Set<Procedure> proceduresForParentSensorId) {
        this.proceduresForParentSensorId = proceduresForParentSensorId;
        return this;
    }
}
