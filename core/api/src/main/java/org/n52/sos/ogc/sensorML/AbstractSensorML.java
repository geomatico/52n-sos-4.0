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

import org.n52.sos.ogc.gml.time.Time;
import org.n52.sos.ogc.sensorML.elements.AbstractSmlDocumentation;
import org.n52.sos.ogc.sensorML.elements.SmlCapabilities;
import org.n52.sos.ogc.sensorML.elements.SmlCharacteristics;
import org.n52.sos.ogc.sensorML.elements.SmlClassifier;
import org.n52.sos.ogc.sensorML.elements.SmlIdentifier;
import org.n52.sos.ogc.sos.SosProcedureDescription;

public class AbstractSensorML extends SosProcedureDescription {

    private List<String> keywords = new ArrayList<String>(0);

    private List<SmlIdentifier> identifications = new ArrayList<SmlIdentifier>(0);

    private List<SmlClassifier> classifications = new ArrayList<SmlClassifier>(0);

    private Time validTime;

    private List<SmlCharacteristics> characteristics = new ArrayList<SmlCharacteristics>(0);

    private final List<SmlCapabilities> capabilities = new ArrayList<SmlCapabilities>(0);

    private List<SmlContact> contacts = new ArrayList<SmlContact>(0);

    private final List<AbstractSmlDocumentation> documentations = new ArrayList<AbstractSmlDocumentation>(0);

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
    	if (isSetKeywords()) {
    		this.keywords.addAll(keywords);
    	} else {
    		this.keywords = keywords;
    	}
        return this;
    }

    public List<SmlIdentifier> getIdentifications() {
        return identifications;
    }

    public AbstractSensorML setIdentifications(final List<SmlIdentifier> identifications) {
        if (this.identifications.isEmpty()) {
            this.identifications = identifications;
        } else {
            this.identifications.addAll(identifications);
        }
        return this;
    }

    public List<SmlClassifier> getClassifications() {
        return classifications;
    }

    public AbstractSensorML setClassifications(final List<SmlClassifier> classifications) {
        if (isSetClassifications()) {
            this.classifications.addAll(classifications);
        }
        this.classifications = classifications;
        return this;
    }

    public AbstractSensorML addClassification(final SmlClassifier classifier) {
        classifications.add(classifier);
        return this;
    }

    public Time getValidTime() {
        return validTime;
    }

    public AbstractSensorML setValidTime(final Time validTime) {
        this.validTime = validTime;
        return this;
    }

    public List<SmlCharacteristics> getCharacteristics() {
        return characteristics;
    }

    public AbstractSensorML setCharacteristics(final List<SmlCharacteristics> characteristics) {
        if (isSetCharacteristics()) {
            this.characteristics.addAll(characteristics);
        } else {
            this.characteristics = characteristics;
        }
        return this;
    }

    public AbstractSensorML addCharacteristic(final SmlCharacteristics characteristic) {
        characteristics.add(characteristic);
        return this;
    }

    public List<SmlCapabilities> getCapabilities() {
        return capabilities;
    }

    public AbstractSensorML addCapabilities(final List<SmlCapabilities> capabilities) {
        if (capabilities != null) {
            this.capabilities.addAll(capabilities);
        }
        return this;
    }

    public AbstractSensorML addCapabilities(final SmlCapabilities capabilities) {
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

    public List<AbstractSmlDocumentation> getDocumentation() {
        return documentations;
    }

    public AbstractSensorML setDocumentation(final List<AbstractSmlDocumentation> documentations) {
        this.documentations.addAll(documentations);
        return this;
    }

    public AbstractSensorML addDocumentation(final AbstractSmlDocumentation documentation) {
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

    public AbstractSensorML addIdentifier(final SmlIdentifier identifier) {
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
