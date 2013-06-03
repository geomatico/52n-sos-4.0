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

/**
 * SOS internal representation of SensorML identifier
 * 
 */
public class SmlIdentifier {

    private String name;

    private String definition;

    private String value;

    /**
     * constructor
     * 
     * @param name
     *            Identifier name
     * @param definition
     *            Identifier definition
     * @param value
     *            Identifier value
     */
    public SmlIdentifier(final String name, final String definition, final String value) {
        super();
        this.name = name;
        this.definition = definition;
        this.value = value;
    }

    /**
     * @return the Identifier name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            Identifier name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the Identifier definition
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * @param definition
     *            Identifier definition
     */
    public void setDefinition(final String definition) {
        this.definition = definition;
    }

    /**
     * @return the Identifier value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            Identifier value
     */
    public void setValue(final String value) {
        this.value = value;
    }

	@Override
	public String toString()
	{
		return String.format("SosSMLIdentifier [name=%s, definition=%s, value=%s]", name, definition, value);
	}
}
