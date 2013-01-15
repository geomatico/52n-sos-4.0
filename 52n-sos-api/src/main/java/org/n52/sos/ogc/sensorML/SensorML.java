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

/**
 * SOS internal representation of a sensor description
 */
public class SensorML extends AbstractSensorML {

    private String version;

    private List<AbstractProcess> members;

    /**
     * default constructor
     */
    public SensorML() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<AbstractProcess> getMembers() {
        return members;
    }

    public void setMembers(List<AbstractProcess> members) {
        this.members = members;
    }

    public void addMember(AbstractProcess member) {
        if (members == null) {
            members = new ArrayList<AbstractProcess>();
        }
        members.add(member);
    }
    
    public boolean isWrapper() {
        return !isSetKeywords()
                && !isSetIdentifications()
                && !isSetClassifications()
                && !isSetCapabilities()
                && !isSetCharacteristics()
                && !isSetValidTime() && !isSetContact()
                && !isSetDocumentation()
                && !isSetHistory()
                && isSetMembers();
    }

    public boolean isSetMembers() {
        return members != null && !members.isEmpty();
    }

}
