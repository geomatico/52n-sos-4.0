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

package org.n52.sos.ogc.om;

import java.util.Collection;

/**
 * class represents a phenomenon of an observation
 * 
 */
public class SosObservableProperty extends AbstractSosPhenomenon {

	/** unit of the values of the phenomenons observations */
	private String unit;

	/** O&M application schema link of the phenomenon */
	private String applicationSchemaLink;

	/** valueType in the database of the phenomenons observation values */
	private String valueType;

	/**
	 * constructor
	 * 
	 * @param identifier
	 *            observableProperty identifier
	 */
	public SosObservableProperty(String identifier) {
		super(identifier);
	}

	/**
	 * constructor
	 * 
	 * @param identifier
	 *            id of the observableProperty
	 * @param description
	 *            description of the observableProperty
	 * @param unit
	 *            unit of the observation values according to this
	 *            observableProperty
	 * @param applicationSchemaLink
	 *            OM application schema link of this observableProperty
	 * @param valueType
	 *            database valType of the observation values according to this
	 *            observableProperty
	 * @param offerings
	 *            Collection of Offerings the observableProperty should be added
	 *            to
	 */
	public SosObservableProperty(String identifier, String description,
			String unit, String applicationSchemaLink, String valueType,
			Collection<SosOffering> offerings) {
		super(identifier, description, offerings);
		this.unit = unit;
		this.applicationSchemaLink = applicationSchemaLink;
		this.valueType = valueType;
	}

	/**
	 * constructor
	 * 
	 * @param identifier
	 *            id of the observableProperty
	 * @param description
	 *            description of the observableProperty
	 * @param unit
	 *            unit of the observation values according to this
	 *            observableProperty
	 * @param applicationSchemaLink
	 *            OM application schema link of this observableProperty
	 * @param valueType
	 *            database valType of the observation values according to this
	 *            observableProperty
	 */
	public SosObservableProperty(String identifier, String description,
			String unit, String applicationSchemaLink, String valueType) {
		super(identifier, description);
		this.unit = unit;
		this.applicationSchemaLink = applicationSchemaLink;
		this.valueType = valueType;
	}

	/**
	 * Get application schema link
	 * 
	 * @return Returns the applicationSchemaLink.
	 */
	public String getApplicationSchemaLink() {
		return applicationSchemaLink;
	}

	/**
	 * Set application schema link
	 * 
	 * @param applicationSchemaLink
	 *            The applicationSchemaLink to set.
	 */
	public void setApplicationSchemaLink(String applicationSchemaLink) {
		this.applicationSchemaLink = applicationSchemaLink;
	}

	/**
	 * Get unit of measurement
	 * 
	 * @return Returns the unit.
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * Set unit of measurement
	 * 
	 * @param unit
	 *            The unit to set.
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

	/**
	 * Get value type
	 * 
	 * @return Returns the valueType.
	 */
	public String getValueType() {
		return valueType;
	}

	/**
	 * Set value type
	 * 
	 * @param valueType
	 *            The valueType to set.
	 */
	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

}
