package org.n52.sos.ogc.sensorML;

import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.om.SosOffering;
import org.n52.sos.ogc.sensorML.elements.SosSMLCapabilities;
import org.n52.sos.ogc.sensorML.elements.SosSMLCharacteristics;
import org.n52.sos.ogc.sensorML.elements.SosSMLClassifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.swe.SosSweField;

public class AbstractSensorML extends SosProcedureDescription {

    private String sensorDescriptionXmlString;

    private List<String> keywords;

    private List<SosSMLIdentifier> identifications;

    private List<SosSMLClassifier> classifications;

    private ITime validTime;

    private List<SosSMLCharacteristics> characteristics;

    private List<SosSMLCapabilities> capabilities;

    private String contact;

    private String documentation;

    private String history;

    public String getSensorDescriptionXmlString() {
        return sensorDescriptionXmlString;
    }

    public void setSensorDescriptionXmlString(String sensorDescriptionXmlString) {
        this.sensorDescriptionXmlString = sensorDescriptionXmlString;
    }

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
        this.characteristics = characteristics;
    }

    public List<SosSMLCapabilities> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<SosSMLCapabilities> capabilities) {
        this.capabilities = capabilities;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public void addCapabilities(SosSMLCapabilities capability) {
        if (capabilities == null) {
            capabilities = new ArrayList<SosSMLCapabilities>();
        }
        capabilities.add(capability);
    }

    @Override
    public String getProcedureIdentifier() {
        if (identifications != null) {
            for (SosSMLIdentifier identification : identifications) {
                if (identification.getDefinition() != null
                        && (identification.getDefinition().equals("urn:ogc:def:identifier:OGC:uniqueID")
                                || identification.getDefinition().equals(
                                        "urn:ogc:def:identifier:OGC::identification.getDefinition()") || (identification
                                .getDefinition().startsWith("urn:ogc:def:identifier:OGC:") && identification
                                .getDefinition().contains("uniqueID")))) {
                    return identification.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public SosOffering getOfferingIdentifier() {
        if (capabilities != null) {
            for (SosSMLCapabilities capability : capabilities) {
                if (capability.getName() != null && capability.getName().equals("offering")) {
                    if (capability.getFields() != null) {
                        for (SosSweField field : capability.getFields()) {
                            if (field.getName() != null && field.getName().equals("Offering")) {
                                if (field.getElement() != null && field.getElement().getDefinition() != null && field.getElement().getDefinition().equals("Offering identifier")) {
                                    return new SosOffering(field.getElement().getValue(), field.getElement().getDescription());
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
