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

import java.util.LinkedList;
import java.util.List;

import org.n52.sos.ogc.om.SosOffering;

/**
 * SOS internal representation of a sensor description
 */
public class SensorML extends AbstractSensorML {

    private String version;

    private final List<AbstractProcess> members = new LinkedList<AbstractProcess>();

    /**
     * default constructor
     */
    public SensorML() {
    }

    public String getVersion() {
        return version;
    }

    public SensorML setVersion(final String version) {
        this.version = version;
        return this;
    }

    public List<AbstractProcess> getMembers() {
        return members;
    }

    public SensorML setMembers(final List<AbstractProcess> members) {
        for (final AbstractProcess member : members) {
            addMember(member);
        }
        return this;
    }

    public SensorML addMember(final AbstractProcess member) {
        if (isEmpty() && !isSetIdentifier() && member.isSetIdentifier()) {
           setIdentifier(member.getIdentifier()); 
        }
        members.add(member);
        return this;
    }
    
    /**
     * @return <tt>true</tt>, if everything from the super class is not set
     */
    private boolean isEmpty() {
        return !isSetKeywords() && !isSetIdentifications() && !isSetClassifications() && !isSetCapabilities()
                && !isSetCharacteristics() && !isSetValidTime() && !isSetContact() && !isSetDocumentation()
                && !isSetHistory();
    }

    /**
     * @return <tt>true</tt>, if this instance contains only members and
     *         everything else is not set
     */
    public boolean isWrapper() {
        return isEmpty() && isSetMembers();
    }

    public boolean isSetMembers() {
        return members != null && !members.isEmpty();
    }

    @Override
    public List<SosOffering> getOfferingIdentifiers() {
        final List<SosOffering> sosOfferings = super.getOfferingIdentifiers();
        if (isWrapper() && getMembers() != null && !getMembers().isEmpty()) {
            for (final AbstractProcess member : getMembers()) {
                final List<SosOffering> offeringIdentifiers = member.getOfferingIdentifiers();
                if (offeringIdentifiers != null && !offeringIdentifiers.isEmpty()) {
                    for (final SosOffering sosOffering : offeringIdentifiers) {
                        if (!sosOfferings.contains(sosOffering)) {
                            sosOfferings.add(sosOffering);
                        }
                    }
                }
            }
        }
        return sosOfferings;
    }

}
