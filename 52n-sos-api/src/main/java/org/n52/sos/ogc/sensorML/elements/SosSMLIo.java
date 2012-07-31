/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.ogc.sensorML.elements;

import org.n52.sos.ogc.swe.simpleType.ISosSweSimpleType;

/**
 * SOS internal representation of SensorML IOs
 */
public class SosSMLIo {

    private String ioName;

    private ISosSweSimpleType ioValue;

    /**
     * default constructor
     */
    public SosSMLIo() {
        super();
    }

    /**
     * constructor
     * 
     * @param ioValue
     *            The IO value
     */
    public SosSMLIo(ISosSweSimpleType ioValue) {
        super();
        this.ioValue = ioValue;
    }

    /**
     * @return the inputName
     */
    public String getIoName() {
        return ioName;
    }

    /**
     * @param inputName
     *            the inputName to set
     */
    public void setIoName(String inputName) {
        this.ioName = inputName;
    }

    /**
     * @return the input
     */
    public ISosSweSimpleType getIoValue() {
        return ioValue;
    }

    /**
     * @param input
     *            the input to set
     */
    public void setIoValue(ISosSweSimpleType ioValue) {
        this.ioValue = ioValue;
    }
}
