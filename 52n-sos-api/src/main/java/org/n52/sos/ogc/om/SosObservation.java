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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.quality.SosQuality;

/**
 * Class represents a SOS observation
 * 
 */
public class SosObservation {

	/**
	 * constellation of procedure, obervedProperty, offering and observationType
	 */
	private SosObservationConstellation observationConstellation;

	/**
	 * Map with observation values for each obsservableProeprty
	 */
	private Map<String, List<SosObservationValue>> values;

	/**
	 * token separator for the value tuples contained in the result element of
	 * the generic observation
	 */
	private String tokenSeparator;

	/** no data value for the values contained in the result element */
	private String noDataValue;

	/** separator of value tuples, which are contained in the resulte element */
	private String tupleSeparator;

	/**
	 * constructor
	 */
	public SosObservation() {
		super();
	}

	/**
	 * Get the observation constellation
	 * 
	 * @return the observationConstellation
	 */
	public SosObservationConstellation getObservationConstellation() {
		return observationConstellation;
	}

	/**
	 * Set the observation constellation
	 * 
	 * @param observationConstellation
	 *            the observationConstellation to set
	 */
	public void setObservationConstellation(
			SosObservationConstellation observationConstellation) {
		this.observationConstellation = observationConstellation;
	}

	/**
	 * Get token separator
	 * 
	 * @return the tokenSeparator
	 */
	public String getTokenSeparator() {
		return tokenSeparator;
	}

	/**
	 * Set token separator
	 * 
	 * @param tokenSeparator
	 *            the tokenSeparator to set
	 */
	public void setTokenSeparator(String tokenSeparator) {
		this.tokenSeparator = tokenSeparator;
	}

	/**
	 * Get noData value
	 * 
	 * @return the noDataValue
	 */
	public String getNoDataValue() {
		return noDataValue;
	}

	/**
	 * Set noData value
	 * 
	 * @param noDataValue
	 *            the noDataValue to set
	 */
	public void setNoDataValue(String noDataValue) {
		this.noDataValue = noDataValue;
	}

	/**
	 * Get tuple separator
	 * 
	 * @return the tupleSeparator
	 */
	public String getTupleSeparator() {
		return tupleSeparator;
	}

	/**
	 * Set tuple separator
	 * 
	 * @param tupleSeparator
	 *            the tupleSeparator to set
	 */
	public void setTupleSeparator(String tupleSeparator) {
		this.tupleSeparator = tupleSeparator;
	}

	/**
	 * Get observation values
	 * 
	 * @return the values
	 */
	public Map<String, List<SosObservationValue>> getValues() {
		return values;
	}

	/**
	 * Set observation values
	 * 
	 * @param values
	 *            the values to set
	 */
	public void setValues(Map<String, List<SosObservationValue>> values) {
		this.values = values;
	}

	/**
	 * Add a new observation value to observableProperty list
	 * 
	 * @param observablePropertyId
	 *            observableProperty
	 * @param value
	 *            observation value
	 */
	public void addValue(String observablePropertyId, SosObservationValue value) {
		if (values == null) {
			values = new HashMap<String, List<SosObservationValue>>();
		}
		if (values.containsKey(observablePropertyId)) {
			values.get(observablePropertyId).add(value);
		} else {
			List<SosObservationValue> sosObsValues = new ArrayList<SosObservationValue>();
			sosObsValues.add(value);
			values.put(observablePropertyId, sosObsValues);
		}

	}

	/**
	 * Check if observation value just exits
	 * 
	 * @param observablePropertyId
	 *            observableProperty
	 * @param sosObsValue
	 *            observation value
	 * @return true if exits
	 */
	public boolean containsObservationValues(String observablePropertyId,
			SosObservationValue sosObsValue) {
		if (values.containsKey(observablePropertyId)) {
			List<SosObservationValue> sosObsValues = values
					.get(observablePropertyId);
			for (SosObservationValue sosObservationValue : sosObsValues) {
				if (sosObservationValue.getObservationID().equals(
						sosObsValue.getObservationID())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Add quality to observation value
	 * 
	 * @param observablePropertyId
	 *            observableProperty
	 * @param sosObsValue
	 *            observation value
	 * @param qualityList
	 *            quality
	 */
	public void addQualityToSosObservationValue(String observablePropertyId,
			SosObservationValue sosObsValue, ArrayList<SosQuality> qualityList) {
		if (values.containsKey(observablePropertyId)) {
			List<SosObservationValue> sosObsValues = values
					.get(observablePropertyId);
			for (SosObservationValue sosObservationValue : sosObsValues) {
				if (sosObservationValue.getObservationID().equals(
						sosObsValue.getObservationID())) {
					if (sosObservationValue.getQuality() == null) {
						sosObservationValue.setQuality(qualityList);
					} else {
						sosObservationValue.addQuality(qualityList);
					}
				}
			}
		}
	}

	/**
	 * Get phenomenonTime from all values
	 * 
	 * @return Time object
	 */
	public ITime getPhenomenonTime() {
		DateTime start = null;
		DateTime end = null;
		for (String obsProp : values.keySet()) {
			for (SosObservationValue sosObsValue : values.get(obsProp)) {
				sosObsValue.getPhenomenonTime();
				if (sosObsValue.getPhenomenonTime() instanceof TimeInstant) {
					TimeInstant ti = (TimeInstant) sosObsValue
							.getPhenomenonTime();
					if (start == null || ti.getValue().isBefore(start)) {
						start = ti.getValue();
					}
					if (end == null || ti.getValue().isAfter(end)) {
						end = ti.getValue();
					}
				} else if (sosObsValue.getPhenomenonTime() instanceof TimePeriod) {
					TimePeriod tp = (TimePeriod) sosObsValue
							.getPhenomenonTime();
					if (start == null || tp.getStart().isBefore(start)) {
						start = tp.getStart();
					}
					if (end == null || tp.getEnd().isAfter(end)) {
						end = tp.getEnd();
					}
				}
			}
		}
		if (start.isEqual(end)) {
			return new TimeInstant(start, null);
		} else {
			return new TimePeriod(start, end);
		}
	}

	/**
	 * Get observation identifiers from all observation values
	 * 
	 * @return List of observation identifiers
	 */
	public List<String> getObservationIDs() {
		Set<String> obsIDs = new HashSet<String>();
		for (String obsProp : values.keySet()) {
			for (SosObservationValue sosObsValue : values.get(obsProp)) {
				if (sosObsValue.getObservationID() != null
						&& !sosObsValue.getObservationID().isEmpty()) {
					obsIDs.add(sosObsValue.getObservationID());
				}
			}
		}
		return new ArrayList<String>(obsIDs);
	}

	/**
	 * Get all observation template identifiers from all observation values
	 * 
	 * @return List of observation template identifiers
	 */
	public List<String> getObservationTemplateIDs() {
		Set<String> obsTemplateIDs = new HashSet<String>();
		for (String obsProp : values.keySet()) {
			for (SosObservationValue sosObsValue : values.get(obsProp)) {
				if (sosObsValue.getObservationTemplateIdentifier() != null
						&& !sosObsValue.getObservationTemplateIdentifier().isEmpty()) {
					obsTemplateIDs.add(sosObsValue.getObservationTemplateIdentifier());
				}
			}
		}
		return new ArrayList<String>(obsTemplateIDs);
	}

	/**
	 * Merge two observations
	 * 
	 * @param sosObservation
	 *            observation to add
	 */
	public void mergeWithObservation(SosObservation sosObservation) {
		// create compPhen or add obsProp to compPhen
		if (this.observationConstellation.getObservableProperty() instanceof SosObservableProperty) {
			List<SosObservableProperty> obsProps = new ArrayList<SosObservableProperty>();
			obsProps.add((SosObservableProperty) this.observationConstellation
					.getObservableProperty());
			obsProps.add((SosObservableProperty) sosObservation
					.getObservationConstellation().getObservableProperty());
			SosCompositePhenomenon sosCompPhen = new SosCompositePhenomenon(
					"CompositePhenomenon_"
							+ this.observationConstellation.getProcedure(),
					null, obsProps);
			this.observationConstellation.setObservableProperty(sosCompPhen);
		} else if (this.observationConstellation.getObservableProperty() instanceof SosCompositePhenomenon) {
			SosCompositePhenomenon sosCompPhen = (SosCompositePhenomenon) this.observationConstellation
					.getObservableProperty();
			sosCompPhen.getPhenomenonComponents().add(
					(SosObservableProperty) sosObservation
							.getObservationConstellation()
							.getObservableProperty());
		}
		// add values
		for (String phenID : sosObservation.getValues().keySet()) {
			this.values.put(phenID, sosObservation.getValues().get(phenID));

		}
	}

}
