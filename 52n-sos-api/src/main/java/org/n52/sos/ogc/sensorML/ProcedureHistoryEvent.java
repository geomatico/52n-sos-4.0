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

package org.n52.sos.ogc.sensorML;


/**
 * represents a timestamp in a procedure history
 *
 * 
 */
public class ProcedureHistoryEvent {

    /** identifier of sensor */
    private String procedure;

    /**
     * constructor
     * 
     * @param procedure
     *            identifier of sensor
     */
    public ProcedureHistoryEvent(String procedure) {
        this.procedure = procedure;
    }

    /**
     * Returns identifier of sensor of this history event
     * 
     * @return Returns the identifier
     * 
     */
    public String getProcedure() {
        return procedure;
    }

    /**
     * sets identifier of sensor of this history event
     * 
     * @param procedure
     *            the identifier to set
     */
	public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

}
