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
import java.util.Set;

import org.n52.sos.util.CollectionHelper;

public abstract class SosProcedureDescription {
    private String identifier;
    private String sensorDescriptionXmlString;
    private String descriptionFormat;
    private final Set<SosOffering> offerings = CollectionHelper.set();
    private final Set<String> featuresOfInterest = CollectionHelper.set();
    private final Set<String> parentProcedures = CollectionHelper.set();
    private final Set<SosProcedureDescription> childProcedures = CollectionHelper.set();

    public SosProcedureDescription setIdentifier(final String identifier) {
        this.identifier = identifier;
        return this;
    }

    public String getIdentifier() {
        return identifier;
    }
    
    public boolean isSetIdentifier() {
        return identifier != null && !identifier.isEmpty();
    }

    public Set<SosOffering> getOfferings(){
    	return offerings;
    }

    public SosProcedureDescription addOfferings(final Collection<SosOffering> offerings) {
        this.offerings.addAll(offerings);
        return this;
    }

    public SosProcedureDescription addOffering(final SosOffering offering) {
        this.offerings.add(offering);
        return this;
    }    
    
    public boolean isSetOfferings(){
    	return offerings != null && !offerings.isEmpty();
    }

    public String getSensorDescriptionXmlString() {
        return sensorDescriptionXmlString;
    }

    public SosProcedureDescription setSensorDescriptionXmlString(final String sensorDescriptionXmlString) {
        this.sensorDescriptionXmlString = sensorDescriptionXmlString;
        return this;
    }

    public boolean isSetSensorDescriptionXmlString() {
        return sensorDescriptionXmlString != null && !sensorDescriptionXmlString.isEmpty();
    }

    public String getDescriptionFormat() {
        return descriptionFormat;
    }

    public SosProcedureDescription setDescriptionFormat(final String descriptionFormat) {
        this.descriptionFormat = descriptionFormat;
        return this;
    }

   
    public SosProcedureDescription addFeaturesOfInterest(final Collection<String> features) {
        featuresOfInterest.addAll(features);
        return this;
    }

    public SosProcedureDescription addFeatureOfInterest(final String featureIdentifier) {
        featuresOfInterest.add(featureIdentifier);
        return this;
    }

    public Set<String> getFeaturesOfInterest() {
        return featuresOfInterest;
    }

    public boolean isSetFeaturesOfInterest() {
        return featuresOfInterest != null && !featuresOfInterest.isEmpty();
    } 

    public SosProcedureDescription addParentProcedures(final Collection<String> parentProcedures) {
        this.parentProcedures.addAll(parentProcedures);
        return this;
    }
    
    public SosProcedureDescription addParentProcedure(final String parentProcedureIdentifier) {
        this.parentProcedures.add(parentProcedureIdentifier);
        return this;
    }

    public Set<String> getParentProcedures() {
        return parentProcedures;
    }

    public boolean isSetParentProcedures() {
        return parentProcedures != null && !parentProcedures.isEmpty();
    } 

    public SosProcedureDescription addChildProcedures(final Collection<SosProcedureDescription> childProcedures) {
        if (childProcedures != null) {
            this.childProcedures.addAll(childProcedures);
        }
        return this;
    }
    
    public SosProcedureDescription addChildProcedure(final SosProcedureDescription childProcedure) {
        this.childProcedures.add(childProcedure);
        return this;
    }

    public Set<SosProcedureDescription> getChildProcedures() {
        return childProcedures;
    }

    public boolean isSetChildProcedures() {
        return childProcedures != null && !childProcedures.isEmpty();
    } 

    @Override
    public int hashCode() {
        int hash = 7;
        hash =  31 * hash + ((getIdentifier() != null) ? getIdentifier().hashCode() : 0);
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
        final SosProcedureDescription other = (SosProcedureDescription) obj;
        if ((getIdentifier() == null)
            ? (other.getIdentifier() != null)
            : !getIdentifier().equals(other.getIdentifier())) {
            return false;
        }
        return true;
    }
}
