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

import org.n52.sos.ogc.swe.simpleType.SosSweTimeRange;

public class SosSweEnvelope extends SosSweAbstractDataComponent {
    private String referenceFrame;
    private SosSweVector upperCorner;
    private SosSweVector lowerCorner;
    private SosSweTimeRange time;

    public SosSweEnvelope(String referenceFrame, SosSweVector upperCorner, SosSweVector lowerCorner) {
        this(referenceFrame, upperCorner, lowerCorner, null);
    }


    public SosSweEnvelope() {
        this(null, null, null, null);
    }

    public SosSweEnvelope(String referenceFrame, SosSweVector upperCorner, SosSweVector lowerCorner,
                          SosSweTimeRange time) {
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

    public void setReferenceFrame(String referenceFrame) {
        this.referenceFrame = referenceFrame;
    }

    public SosSweVector getUpperCorner() {
        return upperCorner;
    }

    public boolean isUpperCornerSet() {
        return getUpperCorner() != null;
    }

    public void setUpperCorner(SosSweVector upperCorner) {
        this.upperCorner = upperCorner;
    }

    public SosSweVector getLowerCorner() {
        return lowerCorner;
    }

    public boolean isLowerCornerSet() {
        return getLowerCorner() != null;
    }

    public void setLowerCorner(SosSweVector lowerCorner) {
        this.lowerCorner = lowerCorner;
    }

    public SosSweTimeRange getTime() {
        return time;
    }

    public boolean isTimeSet() {
        return getTime() != null;
    }

    public void setTime(SosSweTimeRange time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return String.format("SosSweEnvelope[referenceFrame=%s, upperCorner=%s, lowerCorner=%s, time=%s]",
                             getReferenceFrame(), getUpperCorner(), getLowerCorner(), getTime());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.referenceFrame != null ? this.referenceFrame.hashCode() : 0);
        hash = 31 * hash + (this.upperCorner != null ? this.upperCorner.hashCode() : 0);
        hash = 31 * hash + (this.lowerCorner != null ? this.lowerCorner.hashCode() : 0);
        hash = 31 * hash + (this.time != null ? this.time.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SosSweEnvelope other = (SosSweEnvelope) obj;
        if ((this.referenceFrame == null) ? (other.referenceFrame != null)
            : !this.referenceFrame.equals(other.referenceFrame)) {
            return false;
        }
        if (this.upperCorner != other.upperCorner && (this.upperCorner == null || !this.upperCorner
                .equals(other.upperCorner))) {
            return false;
        }
        if (this.lowerCorner != other.lowerCorner && (this.lowerCorner == null || !this.lowerCorner
                .equals(other.lowerCorner))) {
            return false;
        }
        if (this.time != other.time && (this.time == null || !this.time.equals(other.time))) {
            return false;
        }
        return true;
    }
}
