package org.n52.sos.ogc.swe;

import org.n52.sos.ogc.om.features.SosAbstractFeature;

public class SosFeatureRelationship {

    private String role;
    
    private SosAbstractFeature feature;
    
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public SosAbstractFeature getFeature() {
        return feature;
    }

    public void setFeature(SosAbstractFeature feature) {
        this.feature = feature;
    }

}
