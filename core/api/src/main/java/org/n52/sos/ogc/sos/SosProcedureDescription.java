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
package org.n52.sos.ogc.sos;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.n52.sos.ogc.om.SosOffering;
import org.n52.sos.util.CollectionHelper;

public abstract class SosProcedureDescription {
    private String identifier;
    private String sensorDescriptionXmlString;
    private String descriptionFormat;
    private Set<String> featureOfInterest = CollectionHelper.set();
    private Set<String> parentProcedure = CollectionHelper.set();
    private Set<SosProcedureDescription> childProcedure = CollectionHelper.set();

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
    
    public boolean isSetIdentifier() {
        return identifier != null && !identifier.isEmpty();
    }

    public abstract List<SosOffering> getOfferingIdentifiers();

    public abstract boolean isSetOffering();

    public String getSensorDescriptionXmlString() {
        return sensorDescriptionXmlString;
    }

    public void setSensorDescriptionXmlString(String sensorDescriptionXmlString) {
        this.sensorDescriptionXmlString = sensorDescriptionXmlString;
    }

    public boolean isSetSensorDescriptionXmlString() {
        return sensorDescriptionXmlString != null && !sensorDescriptionXmlString.isEmpty();
    }

    public String getDescriptionFormat() {
        return descriptionFormat;
    }

    public void setDescriptionFormat(String descriptionFormat) {
        this.descriptionFormat = descriptionFormat;
    }

   
    public void addFeatureOfInterest(Collection<String> feature) {
        this.featureOfInterest.addAll(feature);
    }

    public void addFeatureOfInterest(String featureIdentifier) {
        this.featureOfInterest.add(featureIdentifier);
    }

    public Set<String> getFeatureOfInterest() {
        return featureOfInterest;
    }

    public boolean isSetFeatureOfInterest() {
        return featureOfInterest != null && !featureOfInterest.isEmpty();
    } 

    public void addParentProcedures(Collection<String> parentProcedures) {
        parentProcedure.addAll( parentProcedures);
    }
    
    public void addParentProcedures(String parentProcedureIdentifier) {
        parentProcedure.add(parentProcedureIdentifier);
    }

    public Set<String> getParentProcedures() {
        return parentProcedure;
    }

    public boolean isSetParentProcedures() {
        return parentProcedure != null && !parentProcedure.isEmpty();
    } 

    public void addChildProcedures(Collection<SosProcedureDescription> childProcedures) {
        if (childProcedures != null) {
            this.childProcedure.addAll(childProcedures);
        }
    }
    
    public void addChildProcedures(SosProcedureDescription childProcedure) {
        this.childProcedure.add(childProcedure);
    }

    public Set<SosProcedureDescription> getChildProcedures() {
        return childProcedure;
    }

    public boolean isSetChildProcedures() {
        return childProcedure != null && !childProcedure.isEmpty();
    } 

    @Override
    public int hashCode() {
        int hash = 7;
        hash =  31 * hash + ((getIdentifier() != null) ? getIdentifier().hashCode() : 0);
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
        final SosProcedureDescription other = (SosProcedureDescription) obj;
        if ((this.getIdentifier() == null)
            ? (other.getIdentifier() != null)
            : !this.getIdentifier().equals(other.getIdentifier())) {
            return false;
        }
        return true;
    }
}
