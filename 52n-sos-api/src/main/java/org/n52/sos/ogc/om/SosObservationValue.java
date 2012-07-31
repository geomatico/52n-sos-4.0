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

import java.util.List;

import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.om.quality.SosQuality;

/**
 * represents a value of an observation
 */
public class SosObservationValue {

	/**
	 * ID of this observation; in the standard 52n SOS PostgreSQL database, this
	 * is implemented through a sequence type.
	 */
	private String observationID;

	/**
	 * identifier of this observation gml:identifier
	 */
	private String identifier;

	/** phenomenon or sampling time of the observation */
	private ITime phenomenonTime;

	/** result time of the observation */
	private TimeInstant resultTime;

	/** valid time of the observation */
	private ITime validTime;

	/** quality of the observation */
	private List<SosQuality> quality;

	/** observation template identifier */
	private String observationTemplateIdentifier;

	/** type of the value or the result the value points to */
	private String resultType;

	/** value of the observation */
	private Object value;

	/**
	 * default constructor
	 */
	public SosObservationValue() {
		super();
	}

	/**
	 * constructor
	 * 
	 * @param observationID
	 *            observation id
	 * @param phenomenonTime
	 *            phenomenon time
	 * @param resultType
	 *            result type
	 * @param value
	 *            observation value
	 */
	public SosObservationValue(String observationID, ITime phenomenonTime,
			String resultType, Object value) {
		super();
		this.observationID = observationID;
		this.phenomenonTime = phenomenonTime;
		this.resultType = resultType;
		this.value = value;
	}

	/**
	 * constructor
	 * 
	 * @param observationID
	 *            observation id
	 * @param identifier
	 *            observation identifier
	 * @param phenomenonTime
	 *            phenomenon time
	 * @param resultTime
	 *            result time
	 * @param validTime
	 *            valid time
	 * @param quality
	 *            quality information
	 * @param observationTemplateID
	 *            observation template identifier
	 * @param resultType
	 *            result type
	 * @param value
	 *            observation value
	 */
	public SosObservationValue(String observationID, String identifier,
			ITime phenomenonTime, TimeInstant resultTime,
			ITime validTime, List<SosQuality> quality,
			String observationTemplateID, String resultType, Object value) {
		super();
		this.observationID = observationID;
		this.identifier = identifier;
		this.phenomenonTime = phenomenonTime;
		this.resultTime = resultTime;
		this.validTime = validTime;
		this.quality = quality;
		this.observationTemplateIdentifier = observationTemplateID;
		this.resultType = resultType;
		this.value = value;
	}

	/**
	 * Get observation ID
	 * 
	 * @return the observationID
	 */
	public String getObservationID() {
		return observationID;
	}

	/**
	 * Set observation ID
	 * 
	 * @param observationID
	 *            the observationID to set
	 */
	public void setObservationID(String observationID) {
		this.observationID = observationID;
	}

	/**
	 * Get observation identifier
	 * 
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Set observation identifier
	 * 
	 * @param identifier
	 *            the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Get phenomenon time
	 * 
	 * @return the phenomenonTime
	 */
	public ITime getPhenomenonTime() {
		return phenomenonTime;
	}

	/**
	 * Set phenomenon time
	 * 
	 * @param phenomenonTime
	 *            the phenomenonTime to set
	 */
	public void setPhenomenonTime(ITime phenomenonTime) {
		this.phenomenonTime = phenomenonTime;
	}

	/**
	 * Get result time
	 * 
	 * @return the resultTime
	 */
	public TimeInstant getResultTime() {
		return resultTime;
	}

	/**
	 * Set result time
	 * 
	 * @param resultTime
	 *            the resultTime to set
	 */
	public void setResultTime(TimeInstant resultTime) {
		this.resultTime = resultTime;
	}

	/**
	 * Get valid time
	 * 
	 * @return the validTime
	 */
	public ITime getValidTime() {
		return validTime;
	}

	/**
	 * Set valid time
	 * 
	 * @param validTime
	 *            the validTime to set
	 */
	public void setValidTime(ITime validTime) {
		this.validTime = validTime;
	}

	/**
	 * Get observation quality information
	 * 
	 * @return the quality
	 */
	public List<SosQuality> getQuality() {
		return quality;
	}

	/**
	 * Set observation quality information
	 * 
	 * @param quality
	 *            the quality to set
	 */
	public void setQuality(List<SosQuality> quality) {
		this.quality = quality;
	}

	/**
	 * Add observation quality to list
	 * 
	 * @param quality
	 *            Quality to add
	 */
	public void addQuality(List<SosQuality> quality) {
		if (quality == null) {
			this.quality = quality;
		} else {
			this.quality.addAll(quality);
		}

	}

	/**
	 * Get observation template identifier
	 * 
	 * @return the observationTemplate
	 */
	public String getObservationTemplateIdentifier() {
		return observationTemplateIdentifier;
	}

	/**
	 * Set observation template identifier
	 * 
	 * @param observationTemplateIdentifier
	 *            the observationTemplateIdentifier to set
	 */
	public void setObservationTemplateIdentifier(
			String observationTemplateIdentifier) {
		this.observationTemplateIdentifier = observationTemplateIdentifier;
	}

	/**
	 * Get result type
	 * 
	 * @return the resultType
	 */
	public String getResultType() {
		return resultType;
	}

	/**
	 * Set result type
	 * 
	 * @param resultType
	 *            the resultType to set
	 */
	public void setResultType(String resultType) {
		this.resultType = resultType;
	}

	/**
	 * Get observation value
	 * 
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Set observation value
	 * 
	 * @param value
	 *            the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

}
