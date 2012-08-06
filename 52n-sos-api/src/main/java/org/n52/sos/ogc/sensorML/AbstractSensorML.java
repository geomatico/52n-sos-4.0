package org.n52.sos.ogc.sensorML;

import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.sensorML.elements.SosSMLCapabilities;
import org.n52.sos.ogc.sensorML.elements.SosSMLCharacteristics;
import org.n52.sos.ogc.sensorML.elements.SosSMLClassifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLComponent;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;

public class AbstractSensorML {
    
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

}
