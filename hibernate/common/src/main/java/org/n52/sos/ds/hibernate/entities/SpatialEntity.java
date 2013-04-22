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

import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasCoordinate;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasGeometry;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasIdentifier;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * 
 * @since 4.0.0
 */
public abstract class SpatialEntity implements HasGeometry, HasCoordinate, HasIdentifier {

	private Geometry geom;
	private Object longitude;
	private Object latitude;
	private Object altitude;
	private int srid;
	private String identifier;

	public SpatialEntity() {
		super();
	}
	
	@Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public SpatialEntity setIdentifier(final String identifier) {
        this.identifier = identifier;
        return this;
    }

	@Override
	public Geometry getGeom()
	{
	    return geom;
	}

	@Override
	public SpatialEntity setGeom(final Geometry geom)
	{
	    this.geom = geom;
	    return this;
	}

	@Override
	public Object getLongitude()
	{
	    return longitude;
	}

	@Override
	public SpatialEntity setLongitude(final Object longitude)
	{
	    this.longitude = longitude;
	    return this;
	}

	@Override
	public Object getLatitude()
	{
	    return latitude;
	}

	@Override
	public SpatialEntity setLatitude(final Object latitude)
	{
	    this.latitude = latitude;
	    return this;
	}

	@Override
	public Object getAltitude()
	{
	    return altitude;
	}

	@Override
	public SpatialEntity setAltitude(final Object altitude)
	{
	    this.altitude = altitude;
	    return this;
	}

	@Override
	public int getSrid()
	{
	    return srid;
	}

	@Override
	public SpatialEntity setSrid(final int srid)
	{
	    this.srid = srid;
	    return this;
	}

	public boolean isSetGeometry()
	{
	    return getGeom() != null;
	}

	public boolean isSetLongLat()
	{
	    return getLongitude() != null && getLatitude() != null;
	}

	public boolean isSetAltitude()
	{
	    return getAltitude() != null;
	}

	public boolean isSetSrid()
	{
	    return getSrid() > 0;
	}
	
	public boolean isSpatial()
    {
    	return isSetGeometry() || (isSetLongLat() && isSetSrid());
    }

}