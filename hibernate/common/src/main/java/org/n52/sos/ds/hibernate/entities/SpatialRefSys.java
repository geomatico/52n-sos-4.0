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

import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasSrid;

public class SpatialRefSys implements Serializable, HasSrid {
    public static final String ID = SRID;
    public static final String AUTH_NAME = "authName";
    public static final String AUTH_SRID = "authSrid";
    public static final String SR_TEXT = "srtext";
    public static final String PROJ4_TEXT = "proj4text";
    private static final long serialVersionUID = -7998487345621799526L;
    private int srid;
    private String authName;
    private Integer authSrid;
    private String srtext;
    private String proj4text;

    public SpatialRefSys() {
    }

    public SpatialRefSys(final int srid) {
        this.srid = srid;
    }

    public SpatialRefSys(final int srid, final String authName, final Integer authSrid, final String srtext, final String proj4text) {
        this.srid = srid;
        this.authName = authName;
        this.authSrid = authSrid;
        this.srtext = srtext;
        this.proj4text = proj4text;
    }

    @Override
    public int getSrid() {
        return srid;
    }

    @Override
    public SpatialRefSys setSrid(final int srid) {
        this.srid = srid;
        return this;
    }

    public String getAuthName() {
        return authName;
    }

    public void setAuthName(final String authName) {
        this.authName = authName;
    }

    public Integer getAuthSrid() {
        return authSrid;
    }

    public void setAuthSrid(final Integer authSrid) {
        this.authSrid = authSrid;
    }

    public String getSrtext() {
        return srtext;
    }

    public void setSrtext(final String srtext) {
        this.srtext = srtext;
    }

    public String getProj4text() {
        return proj4text;
    }

    public void setProj4text(final String proj4text) {
        this.proj4text = proj4text;
    }
}
