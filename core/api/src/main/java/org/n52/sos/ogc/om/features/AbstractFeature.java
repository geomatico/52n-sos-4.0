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
package org.n52.sos.ogc.om.features;

import java.io.Serializable;

import org.n52.sos.ogc.gml.CodeWithAuthority;

/**
 * Abstract class for encoding the feature of interest. Necessary because
 * different feature types should be supported. The SOS database or another
 * feature source (e.g. WFS) should provide information about the application
 * schema.
 * 
 */
public abstract class AbstractFeature implements Serializable {

    /** identifier */
    private CodeWithAuthority identifier;

    private String gmlId;

    /**
     * constructor
     * 
     * @param featureIdentifier
     *            Feature identifier
     */
    public AbstractFeature(CodeWithAuthority featureIdentifier) {
        this.identifier = featureIdentifier;
    }

    public AbstractFeature(CodeWithAuthority featureIdentifier, String gmlId) {
        this.identifier = featureIdentifier;
        this.gmlId = gmlId;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AbstractFeature) {
            AbstractFeature feature = (AbstractFeature) o;
            if (feature.isSetIdentifier() && this.isSetIdentifier() && feature.isSetGmlID() && this.isSetGmlID()) {
                if (feature.getIdentifier().equals(this.getIdentifier()) && feature.getGmlId().equals(this.getGmlId())) {
                    return true;
                }
            } else if (feature.isSetIdentifier() && this.isSetIdentifier()) {
                if (feature.getIdentifier().equals(this.getIdentifier())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier != null) ? identifier.hashCode() : 0);
        result = prime * result + ((gmlId != null) ? gmlId.hashCode() : 0);
        return result;
    }

    /**
     * Get identifier
     * 
     * @return Returns the identifier.
     */
    public CodeWithAuthority getIdentifier() {
        return identifier;
    }

    /**
     * Set identifier
     * 
     * @param identifier
     *            The identifier to set.
     */
    public void setIdentifier(CodeWithAuthority identifier) {
        this.identifier = identifier;
    }
    
    public boolean isSetIdentifier() {
        return identifier != null
                && identifier.isSetValue();
    }

    public String getGmlId() {
        return gmlId == null ? null : gmlId.replaceFirst("#", "");
    }

    public void setGmlId(String gmlId) {
        this.gmlId = gmlId;
    }

    public boolean isSetGmlID() {
       return gmlId != null && !gmlId.isEmpty();
    }
    
    public boolean isReferenced() {
        return isSetGmlID() && gmlId.startsWith("#");
    }

}
