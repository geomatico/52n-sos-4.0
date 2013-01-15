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
package org.n52.sos.ogc.sos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.n52.sos.ogc.om.SosOffering;

public abstract class SosProcedureDescription {

    private String sensorDescriptionXmlString;

    private String descriptionFormat;

    private Map<String, Set<String>> featureOfInterestForProcedure = new HashMap<String, Set<String>>(0);

    private Map<String, Set<String>> parentProcedureForProcedure = new HashMap<String, Set<String>>(0);

    private Map<String, Set<SosProcedureDescription>> childProcedureForProcedure =
            new HashMap<String, Set<SosProcedureDescription>>(0);

    public abstract String getProcedureIdentifier();

    public abstract SosOffering getOfferingIdentifier();

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
    
    public void addFeatureOfInterst(Set<String> feature, String procedureIdentifier) {
        if (isSetFeatureOfInterest(procedureIdentifier)) {
            Set<String> set = featureOfInterestForProcedure.get(procedureIdentifier);
            set.addAll(feature);
            featureOfInterestForProcedure.put(procedureIdentifier, set);
        } else {
            featureOfInterestForProcedure.put(procedureIdentifier, feature);
        }
    }
    
    public void addFeatureOfInterst(String featureIdentifier, String procedureIdentifier) {
        if (isSetFeatureOfInterest(procedureIdentifier)) {
            Set<String> set = featureOfInterestForProcedure.get(procedureIdentifier);
            set.add(featureIdentifier);
            featureOfInterestForProcedure.put(procedureIdentifier, set);
        } else {
            Set<String> set = new HashSet<String>();
            set.add(featureIdentifier);
            featureOfInterestForProcedure.put(procedureIdentifier, set);
        }
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
        if (isSetFeatureOfInterest(procedureIdentifier)) {
            Set<String> set = parentProcedureForProcedure.get(procedureIdentifier);
            set.addAll(parentProcedures);
            parentProcedureForProcedure.put(procedureIdentifier, set);
        } else {
            parentProcedureForProcedure.put(procedureIdentifier, parentProcedures);
        }
    }
    
    public void addParentProcedures(String parentProcedureIdentifier, String procedureIdentifier) {
        if (isSetFeatureOfInterest(procedureIdentifier)) {
            Set<String> set = parentProcedureForProcedure.get(procedureIdentifier);
            set.add(parentProcedureIdentifier);
            parentProcedureForProcedure.put(procedureIdentifier, set);
        } else {
            Set<String> set = new HashSet<String>();
            set.add(parentProcedureIdentifier);
            parentProcedureForProcedure.put(procedureIdentifier, set);
        }
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
        if (isSetFeatureOfInterest(procedureIdentifier)) {
            Set<SosProcedureDescription> set = childProcedureForProcedure.get(procedureIdentifier);
            set.addAll(childProcedures);
            childProcedureForProcedure.put(procedureIdentifier, set);
        } else {
            childProcedureForProcedure.put(procedureIdentifier, childProcedures);
        }
    }
    
    public void addChildProcedures(SosProcedureDescription childProcedure, String procedureIdentifier) {
        if (isSetFeatureOfInterest(procedureIdentifier)) {
            Set<SosProcedureDescription> set = childProcedureForProcedure.get(procedureIdentifier);
            set.add(childProcedure);
            childProcedureForProcedure.put(procedureIdentifier, set);
        } else {
            Set<SosProcedureDescription> set = new HashSet<SosProcedureDescription>();
            set.add(childProcedure);
            childProcedureForProcedure.put(procedureIdentifier, set);
        }
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = (getProcedureIdentifier() == null) ? 0 : 31 * hash + getProcedureIdentifier().hashCode();
        return hash;
    }
}