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
package org.n52.sos.ogc.sensorML.elements;

import java.util.List;

import org.n52.sos.ogc.swe.SweAbstractDataComponent;
import org.n52.sos.ogc.swe.SweCoordinate;
import org.n52.sos.ogc.swe.SWEConstants.SweDataComponentType;

/**
 * SOS internal representation of SensorML position
 * 
 */
public class SmlPosition extends SweAbstractDataComponent {

    private String name;

    private boolean fixed;

    private String referenceFrame;

    private List<SweCoordinate<?>> position;

    /**
     * default constructor
     */
    public SmlPosition() {
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
    public SmlPosition(final String name, final boolean fixed, final String referenceFrame, final List<SweCoordinate<?>> position) {
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
    public void setName(final String name) {
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
     * @return 
     */
    public SmlPosition setFixed(final boolean fixed) {
        this.fixed = fixed;
        return this;
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
     * @return 
     */
    public SmlPosition setReferenceFrame(final String referenceFrame) {
        this.referenceFrame = referenceFrame;
        return this;
    }

    /**
     * @return the position
     */
    public List<SweCoordinate<?>> getPosition() {
        return position;
    }

    /**
     * @param position
     *            the position to set
     * @return 
     */
    public SmlPosition setPosition(final List<SweCoordinate<?>> position) {
        this.position = position;
        return this;
    }
    
    @Override
    public SweDataComponentType getDataComponentType(){
    	return SweDataComponentType.Position;
    }

}
