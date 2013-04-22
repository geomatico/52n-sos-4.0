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
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.n52.sos.ogc.ows.CapabilitiesExtension;
import org.n52.sos.ogc.ows.MergableExtension;
import org.n52.sos.ogc.ows.SwesExtension;

public class SosInsertionCapabilities implements SwesExtension, CapabilitiesExtension,
                                                 MergableExtension<SosInsertionCapabilities> {
    private static final String SECTION_NAME = Sos2Constants.CapabilitiesSections.InsertionCapabilities.name();
    private SortedSet<String> featureOfInterestTypes = new TreeSet<String>();
    private SortedSet<String> observationTypes = new TreeSet<String>();
    private SortedSet<String> procedureDescriptionFormats = new TreeSet<String>();
    private SortedSet<String> supportedEncodings = new TreeSet<String>();

    public SortedSet<String> getFeatureOfInterestTypes() {
        return Collections.unmodifiableSortedSet(featureOfInterestTypes);
    }

    public SortedSet<String> getObservationTypes() {
        return Collections.unmodifiableSortedSet(observationTypes);
    }

    public SortedSet<String> getProcedureDescriptionFormats() {
        return Collections.unmodifiableSortedSet(procedureDescriptionFormats);
    }

    public SortedSet<String> getSupportedEncodings() {
        return Collections.unmodifiableSortedSet(supportedEncodings);
    }

    public void addFeatureOfInterestTypes(Collection<String> featureOfInterestTypes) {
        this.featureOfInterestTypes.addAll(featureOfInterestTypes);
    }

    public void addObservationTypes(Collection<String> observationTypes) {
        this.observationTypes.addAll(observationTypes);
    }

    public void addProcedureDescriptionFormats(Collection<String> procedureDescriptionFormats) {
        this.procedureDescriptionFormats.addAll(procedureDescriptionFormats);
    }

    public void addSupportedEncodings(Collection<String> supportedEncodings) {
        this.supportedEncodings.addAll(supportedEncodings);
    }

    @Override
    public String getSectionName() {
        return SECTION_NAME;
    }

    @Override
    public void merge(SosInsertionCapabilities insertionCapabilities) {
        addFeatureOfInterestTypes(insertionCapabilities.getFeatureOfInterestTypes());
        addObservationTypes(insertionCapabilities.getObservationTypes());
        addProcedureDescriptionFormats(insertionCapabilities.getProcedureDescriptionFormats());
        addSupportedEncodings(insertionCapabilities.getSupportedEncodings());
    }
}
