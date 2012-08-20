package org.n52.sos.ogc.ows;

import java.util.Collection;
import java.util.List;

import org.apache.xmlbeans.XmlObject;

public class SosServiceIdentification {

    private XmlObject serviceIdentification;

    private Collection<String> versions;

    private List<String> profiles;

    private List<String> keywords;

    public XmlObject getServiceIdentification() {
        return serviceIdentification;
    }

    public void setServiceIdentification(XmlObject serviceIdentification) {
        this.serviceIdentification = serviceIdentification;
    }

    public Collection<String> getVersions() {
        return versions;
    }

    public void setVersions(Collection<String> versions) {
        this.versions = versions;
    }

    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

}
