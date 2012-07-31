/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.ogc.sensorML;

import java.util.Collection;

import org.n52.sos.ogc.om.AbstractSosPhenomenon;

/**
 * class represents a SensorML System
 * 
 */
public class SensorSystem {

	/** identifier of sensor system (procedure)*/
	private String identifier;
	
    /** parent procedure ids */
    private Collection<String> parentProcedureIds;

	/** URL to sensor description */
	private String descriptionURL;

	/**
	 * description type (either 'text/xml;subtype="SensorML"' or
	 * 'text/xml;subtype="TML"'
	 */
	private String descriptionType;

	/** string representation of complete sensorML file */
	private String sml_file;

	/** output observableProperties of the sensors, which belong to this system */
	private Collection<AbstractSosPhenomenon> outputs;

	/**
	 * constructor
	 * 
	 * @param identifier
	 *            Sensor identifier
	 */
	public SensorSystem(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * constructor
	 * 
	 * @param identifier
	 *            Sensor identifier
	 * @param descriptionURL
	 *            Sensor description URL
	 * @param descriptionType
	 *            Sensor description type
	 * @param smlFile
	 *            Sensor file
	 * @param observableProperties
	 *            Sensor outputs
	 */
	public SensorSystem(String identifier, Collection<String> parentProcedureIds, String descriptionURL,
			String descriptionType, String smlFile,
			Collection<AbstractSosPhenomenon> observableProperties) {
		this.identifier = identifier;
		this.parentProcedureIds = parentProcedureIds;
		this.descriptionURL = descriptionURL;
		this.descriptionType = descriptionType;
		this.sml_file = smlFile;
		this.outputs = observableProperties;
	}

	/**
	 * Get sensor identifier
	 * 
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Set sensor identifier
	 * 
	 * @param identifier
	 *            the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
    /**
     * @return the parent procedure ids
     */
    public Collection<String> getParentProcedureIds() {
        return parentProcedureIds;
    }

    /**
     * @param parentProcedureIds
     *            the parentProcedureIds to set
     */
    public void setParentProcedureIds(Collection<String> parentProcedureIds) {
        this.parentProcedureIds = parentProcedureIds;
    }

	/**
	 * Get sensor outputs
	 * 
	 * @return the outputs
	 */
	public Collection<AbstractSosPhenomenon> getOutputs() {
		return outputs;
	}

	/**
	 * Set sensor outputs
	 * 
	 * @param outputs
	 *            the outputs to set
	 */
	public void setOutputs(Collection<AbstractSosPhenomenon> outputs) {
		this.outputs = outputs;
	}

	/**
	 * Get description URL
	 * 
	 * @return the descriptionURL
	 */
	public String getDescriptionURL() {
		return descriptionURL;
	}

	/**
	 * Set description URL
	 * 
	 * @param descriptionURL
	 *            the descriptionURL to set
	 */
	public void setDescriptionURL(String descriptionURL) {
		this.descriptionURL = descriptionURL;
	}

	/**
	 * Get description type
	 * 
	 * @return the descriptionType
	 */
	public String getDescriptionType() {
		return descriptionType;
	}

	/**
	 * Set description type
	 * 
	 * @param descriptionType
	 *            the descriptionType to set
	 */
	public void setDescriptionType(String descriptionType) {
		this.descriptionType = descriptionType;
	}

	/**
	 * Get sensor file
	 * 
	 * @return the sml_file
	 */
	public String getSmlFile() {
		return sml_file;
	}

	/**
	 * Set sensor file
	 * 
	 * @param sml_file
	 *            the sml_file to set
	 */
	public void setSmlFile(String sml_file) {
		this.sml_file = sml_file;
	}
}
