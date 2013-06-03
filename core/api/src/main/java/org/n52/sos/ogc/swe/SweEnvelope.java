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

package org.n52.sos.ogc.swe;

import java.util.List;

import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.ogc.swe.SWEConstants.SweCoordinateName;
import org.n52.sos.ogc.swe.simpleType.SweQuantity;
import org.n52.sos.ogc.swe.simpleType.SweTimeRange;
import org.n52.sos.util.CollectionHelper;

public class SweEnvelope extends SweAbstractDataComponent {
    private String referenceFrame;
    private SweVector upperCorner;
    private SweVector lowerCorner;
    private SweTimeRange time;

    public SweEnvelope(final String referenceFrame, final SweVector upperCorner, final SweVector lowerCorner) {
        this(referenceFrame, upperCorner, lowerCorner, null);
    }
    
    public SweEnvelope(final SosEnvelope sosEnvelope, final String uom)
    {
    	referenceFrame = Integer.toString(sosEnvelope.getSrid());

    	List<SweCoordinate<?>> coordinates = CollectionHelper.list();
		SweQuantity xCoord = new SweQuantity();
		xCoord.setValue(sosEnvelope.getEnvelope().getMinX());
		xCoord.setUom(uom);
		xCoord.setAxisID("x");
		coordinates.add(new SweCoordinate<Double>(SweCoordinateName.easting, xCoord));
		
		SweQuantity yCoord = new SweQuantity();
		yCoord.setValue(sosEnvelope.getEnvelope().getMinY());
		yCoord.setUom(uom);
		yCoord.setAxisID("y");
		coordinates.add(new SweCoordinate<Double>(SweCoordinateName.northing, yCoord));
		
		lowerCorner = new SweVector();
		lowerCorner.setCoordinates(coordinates);
		
		coordinates = CollectionHelper.list();
		xCoord = new SweQuantity();
		xCoord.setValue(sosEnvelope.getEnvelope().getMaxX());
		xCoord.setUom(uom);
		coordinates.add(new SweCoordinate<Double>(SweCoordinateName.easting, xCoord));
		
		yCoord = new SweQuantity();
		yCoord.setValue(sosEnvelope.getEnvelope().getMaxY());
		yCoord.setUom(uom);
		yCoord.setAxisID("y");
		coordinates.add(new SweCoordinate<Double>(SweCoordinateName.northing, yCoord));
		
		upperCorner = new SweVector();
		upperCorner.setCoordinates(coordinates);
		
    }


    public SweEnvelope() {
        this(null, null, null, null);
    }

    public SweEnvelope(final String referenceFrame, final SweVector upperCorner, final SweVector lowerCorner,
                          final SweTimeRange time) {
        this.referenceFrame = referenceFrame;
        this.upperCorner = upperCorner;
        this.lowerCorner = lowerCorner;
        this.time = time;
    }

    public String getReferenceFrame() {
        return referenceFrame;
    }

    public boolean isReferenceFrameSet() {
        return getReferenceFrame() != null;
    }

    public SweEnvelope setReferenceFrame(final String referenceFrame) {
        this.referenceFrame = referenceFrame;
        return this;
    }

    public SweVector getUpperCorner() {
        return upperCorner;
    }

    public boolean isUpperCornerSet() {
        return getUpperCorner() != null;
    }

    public SweEnvelope setUpperCorner(final SweVector upperCorner) {
        this.upperCorner = upperCorner;
        return this;
    }

    public SweVector getLowerCorner() {
        return lowerCorner;
    }

    public boolean isLowerCornerSet() {
        return getLowerCorner() != null;
    }

    public SweEnvelope setLowerCorner(final SweVector lowerCorner) {
        this.lowerCorner = lowerCorner;
        return this;
    }

    public SweTimeRange getTime() {
        return time;
    }

    public boolean isTimeSet() {
        return getTime() != null;
    }

    public SweEnvelope setTime(final SweTimeRange time) {
        this.time = time;
        return this;
    }

    @Override
    public String toString() {
        return String.format("SosSweEnvelope[referenceFrame=%s, upperCorner=%s, lowerCorner=%s, time=%s]",
                             getReferenceFrame(), getUpperCorner(), getLowerCorner(), getTime());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (referenceFrame != null ? referenceFrame.hashCode() : 0);
        hash = 31 * hash + (upperCorner != null ? upperCorner.hashCode() : 0);
        hash = 31 * hash + (lowerCorner != null ? lowerCorner.hashCode() : 0);
        hash = 31 * hash + (time != null ? time.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SweEnvelope other = (SweEnvelope) obj;
        if ((referenceFrame == null) ? (other.referenceFrame != null)
            : !referenceFrame.equals(other.referenceFrame)) {
            return false;
        }
        if (upperCorner != other.upperCorner && (upperCorner == null || !upperCorner
                .equals(other.upperCorner))) {
            return false;
        }
        if (lowerCorner != other.lowerCorner && (lowerCorner == null || !lowerCorner
                .equals(other.lowerCorner))) {
            return false;
        }
        if (time != other.time && (time == null || !time.equals(other.time))) {
            return false;
        }
        return true;
    }
}
