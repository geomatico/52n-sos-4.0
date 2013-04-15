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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.sos.ogc.om.SosOffering;
import org.n52.sos.util.MultiMaps;
import org.n52.sos.util.SetMultiMap;

public abstract class SosProcedureDescription {
    private String identifier;
    private String sensorDescriptionXmlString;
    private String descriptionFormat;
    private SetMultiMap<String, String> featureOfInterestForProcedure = MultiMaps.newSetMultiMap();
    private SetMultiMap<String, String> parentProcedureForProcedure = MultiMaps.newSetMultiMap();
    private SetMultiMap<String, SosProcedureDescription> childProcedureForProcedure = MultiMaps.newSetMultiMap();

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

    public void setFeatureOfInterest(Map<String, Set<String>> features) {
        featureOfInterestForProcedure.putAll(features);
    }
    
    public void addFeatureOfInterest(Set<String> feature, String procedureIdentifier) {
        featureOfInterestForProcedure.addAll(procedureIdentifier, feature);
    }

    public void addFeatureOfInterest(String featureIdentifier, String procedureIdentifier) {
        featureOfInterestForProcedure.add(procedureIdentifier, featureIdentifier);
    }

    public Map<String, Set<String>> getFeatureOfInterest() {
        return featureOfInterestForProcedure;
    }

    public Set<String> getFeatureOfInterest(String procedureIdentifier) {
        return featureOfInterestForProcedure.get(procedureIdentifier);
    }

    public boolean isSetFeatureOfInterest() {
        return featureOfInterestForProcedure != null && !featureOfInterestForProcedure.isEmpty();
    } 

    public boolean isSetFeatureOfInterest(String procedureIdentifier) {
        if (isSetFeatureOfInterest()) {
            Set<String> features = featureOfInterestForProcedure.get(procedureIdentifier);
            return features != null && !features.isEmpty();
        }
        return false;
    }

    public void setParentProcedures(Map<String, Set<String>> parentProcedures) {
        parentProcedureForProcedure.putAll(parentProcedures);
    }
    
    public void addParentProcedures(Set<String> parentProcedures, String procedureIdentifier) {
        parentProcedureForProcedure.addAll(procedureIdentifier, parentProcedures);
    }
    
    public void addParentProcedures(String parentProcedureIdentifier, String procedureIdentifier) {
        parentProcedureForProcedure.add(procedureIdentifier, parentProcedureIdentifier);
    }

    public Map<String, Set<String>> getParentProcedures() {
        return parentProcedureForProcedure;
    }

    public Set<String> getParentProcedures(String procedureIdentifier) {
        return parentProcedureForProcedure.get(procedureIdentifier);
    }

    public boolean isSetParentProcedures() {
        return parentProcedureForProcedure != null && !parentProcedureForProcedure.isEmpty();
    } 

    public boolean isSetParentProcedures(String procedureIdentifier) {
        if (isSetFeatureOfInterest()) {
            Set<String> parentProcedures = parentProcedureForProcedure.get(procedureIdentifier);
            return parentProcedures != null && !parentProcedures.isEmpty();
        }
        return false;
    }
    
    public void setChildProcedures(Map<String, Set<SosProcedureDescription>> childProcedures) {
        childProcedureForProcedure.putAll(childProcedures);
    }
    
    public void addChildProcedures(Set<SosProcedureDescription> childProcedures, String procedureIdentifier) {
        childProcedureForProcedure.addAll(procedureIdentifier, childProcedures);
    }
    
    public void addChildProcedures(SosProcedureDescription childProcedure, String procedureIdentifier) {
        childProcedureForProcedure.add(procedureIdentifier, childProcedure);
    }

    public Map<String, Set<SosProcedureDescription>> getChildProcedures() {
        return childProcedureForProcedure;
    }

    public Set<SosProcedureDescription> getChildProcedures(String procedureIdentifier) {
        return childProcedureForProcedure.get(procedureIdentifier);
    }

    public boolean isSetChildProcedures() {
        return childProcedureForProcedure != null && !childProcedureForProcedure.isEmpty();
    } 

    public boolean isSetChildProcedures(String procedureIdentifier) {
        if (isSetFeatureOfInterest()) {
            Set<SosProcedureDescription> features = childProcedureForProcedure.get(procedureIdentifier);
            return features != null && !features.isEmpty();
        }
        return false;
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
