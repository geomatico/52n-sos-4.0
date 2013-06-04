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
package org.n52.sos.ogc.sos;

/**
 * class represents an offering in the SOS database
 * 
 */
public class SosOffering implements Comparable<SosOffering> {

    /** identifier of this offering */
    private String offeringIdentifier;

    /** name of this offering */
    private String offeringName;
    
    /** flag to identify offering as offering from a parent procedure, default = false */
    private boolean parentOfferingFlag = false;

    /**
     * constructor
     * 
     * @param offeringIdentifier
     *            offering identifier
     * @param offeringName
     *            offering name
     */
    public SosOffering(String offeringIdentifier, String offeringName) {
        this.offeringIdentifier = offeringIdentifier;
        this.offeringName = offeringName;
    }
    
    public SosOffering(String procedureIdentifier) {
        this.offeringIdentifier = procedureIdentifier + "/observations";
        this.offeringName = "Offering for sensor " + procedureIdentifier;
    }

    /**
     * Get offering identifier
     * 
     * @return Returns the offeringIdentifier.
     */
    public String getOfferingIdentifier() {
        return offeringIdentifier;
    }

    /**
     * Set offering identifier
     * 
     * @param offeringIdentifier
     *            The offeringIdentifier to set.
     */
    public void setOfferingIdentifier(String offeringIdentifier) {
        this.offeringIdentifier = offeringIdentifier;
    }

    /**
     * Get offering name
     * 
     * @return Returns the offeringName.
     */
    public String getOfferingName() {
        return offeringName;
    }

    /**
     * Set offering name
     * 
     * @param offeringName
     *            The offeringName to set.
     */
    public void setOfferingName(String offeringName) {
        this.offeringName = offeringName;
    }

    public boolean isSetOfferingIdentifier() {
        return offeringIdentifier != null && !offeringIdentifier.isEmpty();
    }
    
    public boolean isSetOfferingName() {
        return offeringName != null && !offeringName.isEmpty();
    }
    
    /**
     * Set if offering is from parent procedure or not
     * 
     * @param parentOfferingFlag Offering is from parent procedure or not
     */
    public void setParentOfferingFlag(boolean parentOfferingFlag) {
        this.parentOfferingFlag = parentOfferingFlag;
    }
    
    /**
     * 
     * @return offering is from parent procedure or not
     */
    public boolean isParentOffering() {
        return parentOfferingFlag;
    }

	@Override
	public int compareTo(SosOffering o) {
		if (o == null){
			throw new NullPointerException();
		}
		if (getOfferingIdentifier() == null ^ o.getOfferingIdentifier() == null) {
	        return (getOfferingIdentifier() == null) ? -1 : 1;
	    }
		if (getOfferingIdentifier() == null && o.getOfferingIdentifier() == null) {
			return 0;			
		}
		return getOfferingIdentifier().compareTo(o.getOfferingIdentifier());
	}

}
