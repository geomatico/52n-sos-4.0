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
package org.n52.sos.ogc.sensorML;

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

    private List<AbstractSosSMLDocumentation> documentations = new ArrayList<AbstractSosSMLDocumentation>(0);

    private String history;
    
    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<SosSMLIdentifier> getIdentifications() {
        return identifications;
    }

    public void setIdentifications(List<SosSMLIdentifier> identifications) {
        this.identifications = identifications;
    }

    public List<SosSMLClassifier> getClassifications() {
        return classifications;
    }

    public void setClassifications(List<SosSMLClassifier> classifications) {
        this.classifications = classifications;
    }

    public ITime getValidTime() {
        return validTime;
    }

    public void setValidTime(ITime validTime) {
        this.validTime = validTime;
    }

    public List<SosSMLCharacteristics> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(List<SosSMLCharacteristics> characteristics) {
        if (isSetCharacteristics()) {
            this.characteristics.addAll(characteristics);
        } else {
            this.characteristics = characteristics;
        }
    }
    
    public void addCharacteristic(SosSMLCharacteristics characteristic) {
        this.characteristics.add(characteristic);
    }

    public List<SosSMLCapabilities> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<SosSMLCapabilities> capabilities) {
        if (isSetCapabilities()) {
            this.capabilities.addAll(capabilities);
        } else {
            this.capabilities = capabilities;
        }
    }

    public void addCapabilities(SosSMLCapabilities capabilities) {
        this.capabilities.add(capabilities);
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public List<AbstractSosSMLDocumentation> getDocumentation() {
        return documentations;
    }

    public void setDocumentation(List<AbstractSosSMLDocumentation> documentations) {
        this.documentations.addAll(documentations);
    }
    
    public void addDocumentation(AbstractSosSMLDocumentation documentation) {
        this.documentations.add(documentation);
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    @Override
    public String getProcedureIdentifier() {
        if (identifications != null) {
            for (SosSMLIdentifier identification : identifications) {
                if ((identification.getName() != null && identification.getName().equals("uniqueID"))
                        || (identification.getDefinition() != null
                                && (identification.getDefinition().equals("urn:ogc:def:identifier:OGC:uniqueID")
                                || identification.getDefinition().equals(
                                        "urn:ogc:def:identifier:OGC::identification.getDefinition()") || (identification
                                .getDefinition().startsWith("urn:ogc:def:identifier:OGC:") && identification
                                .getDefinition().contains("uniqueID"))))) {
                    return identification.getValue();
                }
            }
        }
        return null;
    }
    
    public void addIdentifier(SosSMLIdentifier identifier) {
        this.identifications.add(identifier);
    }

    @Override
    public SosOffering getOfferingIdentifier() {
        if (identifications != null) {
            for (SosSMLIdentifier identification : identifications) {
                if (identification.getDefinition() != null
                        && (identification.getDefinition().equals("urn:ogc:def:identifier:OGC:offeringID")
                                || identification.getDefinition().contains("offering"))) {
                            return new SosOffering(identification.getValue(), identification.getName());
                }
            }
        }
        return null;
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
