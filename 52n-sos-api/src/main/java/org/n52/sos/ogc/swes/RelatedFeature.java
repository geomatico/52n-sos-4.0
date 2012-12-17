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
