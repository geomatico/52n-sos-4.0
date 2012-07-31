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

package org.n52.sos.ogc.om.features;

import org.n52.sos.ogc.AbstractServiceResponseObject;


/**
 * Abstract class for encoding the feature of interest. Necessary because
 * different feature types should be supported. The SOS database or another
 * feature source (e.g. WFS) should provide information about the application
 * schema.
 * 
 */
public abstract class SosAbstractFeature extends AbstractServiceResponseObject {

    /** identifier */
    private String identifier;

    /**
     * constructor
     * 
     * @param featureIdentifier
     *            Feature identifier
     */
    public SosAbstractFeature(String featureIdentifier) {
        this.identifier = featureIdentifier;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof SosAbstractFeature) {
            SosAbstractFeature feature = (SosAbstractFeature) o;
            if (feature.getIdentifier().equals(this.getIdentifier())) {
                return true;
            }
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        return result;
    }

    /**
     * Get identifier
     * 
     * @return Returns the identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Set identifier
     * 
     * @param identifier
     *            The identifier to set.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

}
