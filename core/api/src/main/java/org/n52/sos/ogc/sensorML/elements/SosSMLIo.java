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

import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;

/**
 * SOS internal representation of SensorML IOs
 * @param <T> 
 */
public class SosSMLIo<T> {

    private String ioName;

    private SosSweAbstractSimpleType<T> ioValue;

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
    public SosSMLIo(final SosSweAbstractSimpleType<T> ioValue) {
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
     * @return 
     */
    public SosSMLIo<T> setIoName(final String inputName) {
        this.ioName = inputName;
        return this;
    }

    /**
     * @return the input
     */
    public SosSweAbstractSimpleType<T> getIoValue() {
        return ioValue;
    }

    /**
     * @param ioValue
     *            the input to set
     * @return 
     */
    public SosSMLIo<T> setIoValue(final SosSweAbstractSimpleType<T> ioValue) {
        this.ioValue = ioValue;
        return this;
    }

    public boolean isSetName() {
        return ioName != null && !ioName.isEmpty();
    }

	@Override
	public String toString()
	{
		return String.format("SosSMLIo [ioName=%s, ioValue=%s]", ioName, ioValue);
	}
    
}
