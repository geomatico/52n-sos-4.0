package org.n52.sos.ogc.swes;

import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.om.features.SosAbstractFeature;

public class RelatedFeature {
    
    private SosAbstractFeature target;
    
    private List<String> roles = new ArrayList<String>(0);
    
    public RelatedFeature(SosAbstractFeature target, List<String> roles) {
        this.target = target;
        this.roles = roles;
    }

    public SosAbstractFeature getTarget() {
        return target;
    }

    public void setTarget(SosAbstractFeature target) {
        this.target = target;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        if (!isSetRoles()) {
            roles.add(OGCConstants.UNKNOWN);
        }
        this.roles = roles;
    }
    
    public boolean isSetTarget() {
        return target != null;
    }
    
    private boolean isSetRoles() {
        return roles != null && !roles.isEmpty();
    }

}
