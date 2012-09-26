/**
 * Copyright (C) 2012
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
package org.n52.sos.ogc.sensorML.elements;

import java.util.List;

import org.n52.sos.ogc.swe.SosSweCoordinate;

/**
 * SOS internal representation of SensorML position
 * 
 */
public class SosSMLPosition {

    private String name;

    private boolean fixed;

    private String referenceFrame;

    private List<SosSweCoordinate> position;

    /**
     * default constructor
     */
    public SosSMLPosition() {
        super();
    }

    /**
     * constructor
     * 
     * @param name
     *            Position name
     * @param fixed
     *            is fixed
     * @param referenceFrame
     *            Position reference frame
     * @param position
     *            Position coordinates
     */
    public SosSMLPosition(String name, boolean fixed, String referenceFrame, List<SosSweCoordinate> position) {
        super();
        this.name = name;
        this.fixed = fixed;
        this.referenceFrame = referenceFrame;
        this.position = position;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the fixed
     */
    public boolean isFixed() {
        return fixed;
    }

    /**
     * @param fixed
     *            the fixed to set
     */
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    /**
     * @return the referenceFrame
     */
    public String getReferenceFrame() {
        return referenceFrame;
    }

    /**
     * @param referenceFrame
     *            the referenceFrame to set
     */
    public void setReferenceFrame(String referenceFrame) {
        this.referenceFrame = referenceFrame;
    }

    /**
     * @return the position
     */
    public List<SosSweCoordinate> getPosition() {
        return position;
    }

    /**
     * @param position
     *            the position to set
     */
    public void setPosition(List<SosSweCoordinate> position) {
        this.position = position;
    }

}
