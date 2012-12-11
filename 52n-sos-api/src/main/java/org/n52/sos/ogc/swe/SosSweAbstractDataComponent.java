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
package org.n52.sos.ogc.swe;

public abstract class SosSweAbstractDataComponent {
    
    private String definition;
    
    private String description;
    
    /**
     * optional: swe:identifier
     */
    private String identifier;

    /**
     * pre-set XML representation
     */
    private String xml;
    
    public String getDefinition() {
        return definition;
    }

    public String getDescription() {
        return description;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }
    
    public boolean isSetDefinition() {
        return definition != null && !definition.isEmpty();
    }
    
    public boolean isSetDescription() {
        return description != null && !description.isEmpty();
    }
    
    public boolean isSetIdentifier() {
        return identifier != null && !identifier.isEmpty();
    }
    
    public boolean isSetXml() {
        return xml != null && !xml.isEmpty();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (getDefinition() != null ? getDefinition().hashCode() : 0);
        hash = 31 * hash + (getDescription() != null ? getDescription().hashCode() : 0);
        hash = 31 * hash + (getIdentifier() != null ? getIdentifier().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SosSweAbstractDataComponent other = (SosSweAbstractDataComponent) obj;
        if ((this.getDefinition() == null) ? (other.getDefinition() != null) : !this.getDefinition().equals(other.getDefinition())) {
            return false;
        }
        if ((this.getDescription() == null) ? (other.getDescription() != null) : !this.getDescription().equals(other.getDescription())) {
            return false;
        }
        if ((this.getIdentifier() == null) ? (other.getIdentifier() != null) : !this.getIdentifier().equals(other.getIdentifier())) {
            return false;
        }
        return true;
    }
    
}
