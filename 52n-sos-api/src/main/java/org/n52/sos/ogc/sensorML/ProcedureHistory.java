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

import java.util.ArrayList;
import java.util.Collection;

/**
 * class represents a history of a procedure (sensor)
 * 
 */
public class ProcedureHistory {

    /** collection containing the history of procedure */
    private Collection<ProcedureHistoryEvent> history;

    /**
     * constructor
     * 
     * @param eventCollection
     *            conatins events of this procedure history
     */
    public ProcedureHistory(Collection<ProcedureHistoryEvent> eventCollection) {
        this.history = eventCollection;
    }

    /**
     * constructor for single procedure history event
     * 
     * @param event
     */
    public ProcedureHistory(ProcedureHistoryEvent event) {
        this.history = new ArrayList<ProcedureHistoryEvent>(1);
        this.history.add(event);
    }

    /**
     * Returns the the history events
     * 
     * @return the history
     */
    public Collection<ProcedureHistoryEvent> getHistory() {
        return history;
    }

    /**
     * sets the history events
     * 
     * @param history
     *            the history to set
     */
    public void setHistory(Collection<ProcedureHistoryEvent> history) {
        this.history = history;
    }
}
