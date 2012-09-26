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
package org.n52.sos.ogc.om;

import java.io.Serializable;
import java.util.Collection;

/**
 * Abstract class for phenomena
 * 
 */
public class AbstractSosPhenomenon implements Serializable {

    private static final long serialVersionUID = 1L;

    /** phenomenon identifier */
    private String identifier;

    /** phenomenon description */
    private String description;

    /**
     * constructor
     * 
     * @param identifier
     *            Phenomenon identifier
     */
    public AbstractSosPhenomenon(String identifier) {
        super();
        this.identifier = identifier;
    }

    /**
     * constructor
     * 
     * @param identifier
     *            Phenomenon identifier
     * @param description
     *            Phenomenon description
     */
    public AbstractSosPhenomenon(String identifier, String description) {
        super();
        this.identifier = identifier;
        this.description = description;
    }

    /**
     * Get phenomenon identifier
     * 
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Set phenomenon identifier
     * 
     * @param identifier
     *            the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Get phenomenon description
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set phenomenon description
     * 
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object paramObject) {
        if (paramObject instanceof AbstractSosPhenomenon) {
            AbstractSosPhenomenon phen = (AbstractSosPhenomenon) paramObject;
            return identifier.equals(phen.getIdentifier());
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + identifier.hashCode();
        return hash;
    }
}
