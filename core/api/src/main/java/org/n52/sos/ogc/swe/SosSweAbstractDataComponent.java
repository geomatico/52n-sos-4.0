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
package org.n52.sos.ogc.swe;

public abstract class SosSweAbstractDataComponent {
    
    private String definition;
    
    /**
     * optional: swe:description[0..1]
     */
    private String description;
    
    /**
     * optional: swe:label [0..1]
     */
    private String label;
    
    /**
     * optional: swe:identifier [0..1]
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
    
    public String getLabel() {
        return label;
    }

    public String getIdentifier() {
        return identifier;
    }

    public SosSweAbstractDataComponent setDefinition(final String definition) {
        this.definition = definition;
        return this;
    }

    public SosSweAbstractDataComponent setDescription(final String description) {
        this.description = description;
        return this;
    }
    
    public SosSweAbstractDataComponent setLabel(final String label) {
        this.label = label;
        return this;
    }

    public SosSweAbstractDataComponent setIdentifier(final String identifier) {
        this.identifier = identifier;
        return this;
    }

    public String getXml() {
        return xml;
    }

    public SosSweAbstractDataComponent setXml(final String xml) {
        this.xml = xml;
        return this;
    }
    
    public boolean isSetDefinition() {
        return definition != null && !definition.isEmpty();
    }
    
    public boolean isSetDescription() {
        return description != null && !description.isEmpty();
    }
    
    public boolean isSetLabel() {
        return label != null && !label.isEmpty();
    }
    
    public boolean isSetIdentifier() {
        return identifier != null && !identifier.isEmpty();
    }
    
    public boolean isSetXml() {
        return xml != null && !xml.isEmpty();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hash = 7;
        hash = prime * hash + (getDefinition() != null ? getDefinition().hashCode() : 0);
        hash = prime * hash + (getDescription() != null ? getDescription().hashCode() : 0);
        hash = prime * hash + (getIdentifier() != null ? getIdentifier().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SosSweAbstractDataComponent other = (SosSweAbstractDataComponent) obj;
        if ((getDefinition() == null) ? (other.getDefinition() != null) : !getDefinition().equals(other.getDefinition())) {
            return false;
        }
        if ((getDescription() == null) ? (other.getDescription() != null) : !getDescription().equals(other.getDescription())) {
            return false;
        }
        if ((getIdentifier() == null) ? (other.getIdentifier() != null) : !getIdentifier().equals(other.getIdentifier())) {
            return false;
        }
        return true;
    }
    
}
