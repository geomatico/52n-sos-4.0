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
import java.util.List;

import org.n52.sos.ogc.om.SosOffering;

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
    
    /**
     * @return <tt>true</tt>, if this instance contains only members and everything else is not set
     */
    // TODO please review this javadoc
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
    
    @Override
    public String getProcedureIdentifier()
    {
    	if (isWrapper() && getMembers() != null && !getMembers().isEmpty())
        {
        	return this.getMembers().get(0).getProcedureIdentifier();
        }
    	return super.getProcedureIdentifier();
    }
    
    @Override
    public List<SosOffering> getOfferingIdentifiers() {
        List<SosOffering> sosOfferings = super.getOfferingIdentifiers();
        if (isWrapper() && getMembers() != null && !getMembers().isEmpty())
        {
        	for (AbstractProcess member : getMembers())
        	{
        		final List<SosOffering> offeringIdentifiers = member.getOfferingIdentifiers();
				if (offeringIdentifiers != null && !offeringIdentifiers.isEmpty())
        		{
        			for (SosOffering sosOffering : offeringIdentifiers) 
        			{
						if (!sosOfferings.contains(sosOffering))
						{
							sosOfferings.add(sosOffering);
						}
					}
        		}
        	}
        }
        return sosOfferings;
    }

}
