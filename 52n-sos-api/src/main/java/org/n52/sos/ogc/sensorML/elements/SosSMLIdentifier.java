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

/**
 * SOS internal representation of SensorML identifier
 * 
 */
public class SosSMLIdentifier {

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
    public SosSMLIdentifier(String name, String definition, String value) {
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
    public void setName(String name) {
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
    public void setDefinition(String definition) {
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
    public void setValue(String value) {
        this.value = value;
    }

}
