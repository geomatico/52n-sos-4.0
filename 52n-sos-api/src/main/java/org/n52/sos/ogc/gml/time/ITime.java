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
package org.n52.sos.ogc.gml.time;

import java.io.Serializable;

/**
 * Interface for time objects
 * 
 */
public abstract class ITime implements Comparable<ITime>, Serializable {
    
    private static final long serialVersionUID = 1366100818431254519L;

    private String id;
    
    private String indeterminateValue;
    
    public ITime() {
    }

    public ITime(String id) {
        this.id = id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    /**
     * Get indeterminate value
     * 
     * @return Returns the indeterminateValue.
     */
    public String getIndeterminateValue() {
        return indeterminateValue;
    }

    /**
     * Set indeterminate value
     * 
     * @param indeterminateValue
     *            The indeterminateValue to set.
     */
    public void setIndeterminateValue(String indeterminateValue) {
        this.indeterminateValue = indeterminateValue;
    }
    
    public boolean isSetId() {
        return id != null && !id.isEmpty();
    }
    
    public boolean isSetIndeterminateValue(){
        return indeterminateValue != null && !indeterminateValue.isEmpty();
    }
}
