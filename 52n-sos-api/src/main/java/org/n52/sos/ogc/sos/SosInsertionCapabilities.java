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
import java.util.HashSet;
import java.util.Set;

import org.n52.sos.ogc.ows.CapabilitiesExtension;
import org.n52.sos.ogc.ows.SwesExtension;
import org.n52.sos.ogc.ows.MergableExtension;

public class SosInsertionCapabilities implements SwesExtension, CapabilitiesExtension, MergableExtension<SosInsertionCapabilities> {
    
    private static final String SECTION_NAME = Sos2Constants.CapabilitiesSections.InsertionCapabilities.name();

    private Set<String> featureOfInterestTypes;

    private Set<String> observationTypes;

    private Set<String> procedureDescriptionFormats;

    private Set<String> supportedEncodings;

    public Set<String> getFeatureOfInterestTypes() {
        return featureOfInterestTypes;
    }

    public Set<String> getObservationTypes() {
        return observationTypes;
    }

    public Set<String> getProcedureDescriptionFormats() {
        return procedureDescriptionFormats;
    }

    public Set<String> getSupportedEncodings() {
        return supportedEncodings;
    }

    public void addFeatureOfInterestTypes(Collection<String> featureOfInterestTypes) {
        if (this.featureOfInterestTypes == null) {
            this.featureOfInterestTypes = new HashSet<String>(0);
        }
        this.featureOfInterestTypes.addAll(featureOfInterestTypes);
    }

    public void addObservationTypes(Collection<String> observationTypes) {
        if (this.observationTypes == null) {
            this.observationTypes = new HashSet<String>(0);
        }
        this.observationTypes.addAll(observationTypes);
    }

    public void addProcedureDescriptionFormats(Collection<String> procedureDescriptionFormats) {
        if (this.procedureDescriptionFormats == null) {
            this.procedureDescriptionFormats = new HashSet<String>(0);
        }
        this.procedureDescriptionFormats.addAll(procedureDescriptionFormats);
    }

    public void addSupportedEncodings(Collection<String> supportedEncodings) {
        if (this.supportedEncodings == null) {
            this.supportedEncodings = new HashSet<String>(0);
        }
        this.supportedEncodings.addAll(supportedEncodings);
    }

    @Override
    public String getSectionName() {
        return SECTION_NAME;
    }

    @Override
    public void merge(SosInsertionCapabilities insertionCapabilities) {
        if (insertionCapabilities.getFeatureOfInterestTypes() != null) {
            addFeatureOfInterestTypes(insertionCapabilities.getFeatureOfInterestTypes());
        }
        if (insertionCapabilities.getObservationTypes() != null) {
            addObservationTypes(insertionCapabilities.getObservationTypes());
        }
        if (insertionCapabilities.getProcedureDescriptionFormats() != null) {
            addProcedureDescriptionFormats(insertionCapabilities.getProcedureDescriptionFormats());
        }
        if (insertionCapabilities.getSupportedEncodings() != null) {
            addSupportedEncodings(insertionCapabilities.getSupportedEncodings());
        }
    }
}
