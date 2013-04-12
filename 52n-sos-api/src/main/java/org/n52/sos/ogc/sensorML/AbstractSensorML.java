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
package org.n52.sos.ogc.sensorML;

import static org.n52.sos.ogc.OGCConstants.*;
import static org.n52.sos.ogc.sensorML.SensorMLConstants.ELEMENT_NAME_OFFERING;

import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.om.SosOffering;
import org.n52.sos.ogc.sensorML.elements.AbstractSosSMLDocumentation;
import org.n52.sos.ogc.sensorML.elements.SosSMLCapabilities;
import org.n52.sos.ogc.sensorML.elements.SosSMLCharacteristics;
import org.n52.sos.ogc.sensorML.elements.SosSMLClassifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sos.SosProcedureDescription;

public class AbstractSensorML extends SosProcedureDescription {

    private List<String> keywords = new ArrayList<String>(0);

    private List<SosSMLIdentifier> identifications = new ArrayList<SosSMLIdentifier>(0);

    private List<SosSMLClassifier> classifications = new ArrayList<SosSMLClassifier>(0);

    private ITime validTime;

    private List<SosSMLCharacteristics> characteristics = new ArrayList<SosSMLCharacteristics>(0);

    private List<SosSMLCapabilities> capabilities = new ArrayList<SosSMLCapabilities>(0);

    private String contact;

    private final List<AbstractSosSMLDocumentation> documentations = new ArrayList<AbstractSosSMLDocumentation>(0);

    private String history;

    public String getProcedureIdentifierFromIdentifications() {
        if (isSetIdentifications()) {
            for (final SosSMLIdentifier identification : identifications) {
                if (isIdentificationHoldingAnProcedureIdentifier(identification)) {
                    return identification.getValue();
                }
            }
        }
        return null;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(final List<String> keywords) {
        this.keywords = keywords;
    }

    public List<SosSMLIdentifier> getIdentifications() {
        return identifications;
    }

    public void setIdentifications(final List<SosSMLIdentifier> identifications) {
        this.identifications = identifications;
        final String identifier = getProcedureIdentifierFromIdentifications();
        if (!isSetIdentifier() && identifier != null && !identifier.isEmpty()) {
            setIdentifier(identifier);
        }
    }

    public List<SosSMLClassifier> getClassifications() {
        return classifications;
    }

    public void setClassifications(final List<SosSMLClassifier> classifications) {
        this.classifications = classifications;
    }
    
    public void addClassification(final SosSMLClassifier classifier)
    {
    	classifications.add(classifier);
    }

    public ITime getValidTime() {
        return validTime;
    }

    public void setValidTime(final ITime validTime) {
        this.validTime = validTime;
    }

    public List<SosSMLCharacteristics> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(final List<SosSMLCharacteristics> characteristics) {
        if (isSetCharacteristics()) {
            this.characteristics.addAll(characteristics);
        } else {
            this.characteristics = characteristics;
        }
    }

    public void addCharacteristic(final SosSMLCharacteristics characteristic) {
        characteristics.add(characteristic);
    }

    public List<SosSMLCapabilities> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(final List<SosSMLCapabilities> capabilities) {
        if (isSetCapabilities()) {
            this.capabilities.addAll(capabilities);
        } else {
            this.capabilities = capabilities;
        }
    }

    public void addCapabilities(final SosSMLCapabilities capabilities) {
        this.capabilities.add(capabilities);
    }

    public String getContact() {
        return contact;
    }

    public void setContact(final String contact) {
        this.contact = contact;
    }

    public List<AbstractSosSMLDocumentation> getDocumentation() {
        return documentations;
    }

    public void setDocumentation(final List<AbstractSosSMLDocumentation> documentations) {
        this.documentations.addAll(documentations);
    }

    public void addDocumentation(final AbstractSosSMLDocumentation documentation) {
        documentations.add(documentation);
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(final String history) {
        this.history = history;
    }

    public void addIdentifier(final SosSMLIdentifier identifier) {
        identifications.add(identifier);
    }

    @Override
    public List<SosOffering> getOfferingIdentifiers() {
        final List<SosOffering> sosOfferings = new ArrayList<SosOffering>(0);
        if (isSetIdentifications()) {
            for (final SosSMLIdentifier identification : identifications) {
                if (isIdentificationHoldingAnOfferingId(identification)) {
                    sosOfferings.add(new SosOffering(identification.getValue(), identification.getName()));
                }
            }
        }
        return sosOfferings;
    }

    private boolean isIdentificationHoldingAnProcedureIdentifier(final SosSMLIdentifier identification) {
        return (identification.getName() != null && identification.getName().equals(URN_UNIQUE_IDENTIFIER_END))
                || (identification.getDefinition() != null && (identification.getDefinition().equals(
                        URN_UNIQUE_IDENTIFIER)
                        || identification.getDefinition().equals(URN_IDENTIFIER_IDENTIFICATION) || (identification
                        .getDefinition().startsWith(URN_UNIQUE_IDENTIFIER_START) && identification.getDefinition()
                        .contains(URN_UNIQUE_IDENTIFIER_END))));
    }

    private boolean isIdentificationHoldingAnOfferingId(final SosSMLIdentifier identification) {
        return identification.getDefinition() != null
                && (identification.getDefinition().equals(URN_OFFERING_ID) || identification.getDefinition().contains(
                        ELEMENT_NAME_OFFERING));
    }

    public boolean isSetKeywords() {
        return keywords != null && !keywords.isEmpty();
    }

    public boolean isSetIdentifications() {
        return identifications != null && !identifications.isEmpty();
    }

    public boolean isSetClassifications() {
        return classifications != null && !classifications.isEmpty();
    }

    public boolean isSetCharacteristics() {
        return characteristics != null && !characteristics.isEmpty();
    }

    public boolean isSetCapabilities() {
        return capabilities != null && !capabilities.isEmpty();
    }

    public boolean isSetDocumentation() {
        return documentations != null && !documentations.isEmpty();
    }

    public boolean isSetValidTime() {
        return validTime != null;
    }

    public boolean isSetContact() {
        return contact != null && !contact.isEmpty();
    }

    public boolean isSetHistory() {
        return history != null && !history.isEmpty();
    }

}
