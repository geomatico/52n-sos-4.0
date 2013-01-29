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
package org.n52.sos.ogc.sos;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Class for internal Envelope representation
 * 
 */
public class SosEnvelope {

    /**
     * JTS envelope object
     */
    private Envelope envelope;

    /**
     * SRID
     */
    private int srid;

    /**
     * constructor
     * 
     * @param envelope
     *            JTS envelope
     * @param srid
     *            SRID
     */
    public SosEnvelope(Envelope envelope, int srid) {
        super();
        this.envelope = envelope;
        this.srid = srid;
    }

    /**
     * Get envelope
     * 
     * @return the envelope
     */
    public Envelope getEnvelope() {
        return envelope;
    }

    /**
     * Set envelope
     * 
     * @param envelope
     *            the envelope to set
     */
    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
    }

    /**
     * Get SRID
     * 
     * @return the srid
     */
    public int getSrid() {
        return srid;
    }

    /**
     * Set SRID
     * 
     * @param srid
     *            the srid to set
     */
    public void setSrid(int srid) {
        this.srid = srid;
    }
    
    public boolean isSetEnvelope() {
        return envelope != null && !envelope.isNull();
    }

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((envelope == null) ? 0 : envelope.hashCode());
		result = prime * result + srid;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SosEnvelope))
			return false;
		SosEnvelope other = (SosEnvelope) obj;
		if (envelope == null) {
			if (other.envelope != null)
				return false;
		} else if (!envelope.equals(other.envelope))
			return false;
		if (srid != other.srid)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return String.format("SosEnvelope [envelope=%s, srid=%s]", envelope, srid);
	}
	
}
