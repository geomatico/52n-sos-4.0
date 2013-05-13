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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.n52.sos.ogc.gml.time.ITime;
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

    private List<SmlContact> contacts = new ArrayList<SmlContact>(0);

    private final List<AbstractSosSMLDocumentation> documentations = new ArrayList<AbstractSosSMLDocumentation>(0);

    private String history;

    @Override
    public SosProcedureDescription setIdentifier(final String identifier) {
        super.setIdentifier(identifier);
        return this;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public AbstractSensorML setKeywords(final List<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    public List<SosSMLIdentifier> getIdentifications() {
        return identifications;
    }

    public AbstractSensorML setIdentifications(final List<SosSMLIdentifier> identifications) {
        if (this.identifications.isEmpty()) {
            this.identifications = identifications;
        } else {
            this.identifications.addAll(identifications);
        }
        return this;
    }

    public List<SosSMLClassifier> getClassifications() {
        return classifications;
    }

    public AbstractSensorML setClassifications(final List<SosSMLClassifier> classifications) {
        if (isSetClassifications()) {
            this.classifications.addAll(classifications);
        }
        this.classifications = classifications;
        return this;
    }

    public AbstractSensorML addClassification(final SosSMLClassifier classifier) {
        classifications.add(classifier);
        return this;
    }

    public ITime getValidTime() {
        return validTime;
    }

    public AbstractSensorML setValidTime(final ITime validTime) {
        this.validTime = validTime;
        return this;
    }

    public List<SosSMLCharacteristics> getCharacteristics() {
        return characteristics;
    }

    public AbstractSensorML setCharacteristics(final List<SosSMLCharacteristics> characteristics) {
        if (isSetCharacteristics()) {
            this.characteristics.addAll(characteristics);
        } else {
            this.characteristics = characteristics;
        }
        return this;
    }

    public AbstractSensorML addCharacteristic(final SosSMLCharacteristics characteristic) {
        characteristics.add(characteristic);
        return this;
    }

    public List<SosSMLCapabilities> getCapabilities() {
        return capabilities;
    }

    public AbstractSensorML addCapabilities(final List<SosSMLCapabilities> capabilities) {
        if (capabilities != null) {
            this.capabilities.addAll(capabilities);
        }
        return this;
    }

    public AbstractSensorML addCapabilities(final SosSMLCapabilities capabilities) {
    	return addCapabilities(Collections.singletonList(capabilities));
    }

    public List<SmlContact> getContact() {
        return contacts;
    }

    public AbstractSensorML setContact(final List<SmlContact> contacts) {
        if (isSetContacts()) {
            this.contacts.addAll(contacts);
        } else {
            this.contacts = contacts;
        }
        return this;
    }

    private boolean isSetContacts() {
        return contacts != null && !contacts.isEmpty();
    }

    public AbstractSensorML addContact(final SmlContact contact) {
        contacts.add(contact);
        return this;
    }

    public List<AbstractSosSMLDocumentation> getDocumentation() {
        return documentations;
    }

    public AbstractSensorML setDocumentation(final List<AbstractSosSMLDocumentation> documentations) {
        this.documentations.addAll(documentations);
        return this;
    }

    public AbstractSensorML addDocumentation(final AbstractSosSMLDocumentation documentation) {
        documentations.add(documentation);
        return this;
    }

    public String getHistory() {
        return history;
    }

    public AbstractSensorML setHistory(final String history) {
        this.history = history;
        return this;
    }

    public AbstractSensorML addIdentifier(final SosSMLIdentifier identifier) {
        identifications.add(identifier);
        return this;
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
        return contacts != null && !contacts.isEmpty();
    }

    public boolean isSetHistory() {
        return history != null && !history.isEmpty();
    }
}
