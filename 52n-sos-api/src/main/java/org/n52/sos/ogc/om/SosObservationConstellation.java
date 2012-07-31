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

/**
 * @author c_hollmann
 * 
 */
public class SosObservationConstellation {

	/** Identifier of the procedure by which the observation is made */
	private String procedure;

	/** Identifier of the observableProperty to which the observation accords to */
	private AbstractSosPhenomenon observableProperty;

	/** Identifier of the offering to which this observation belongs */
	private String offering;

	/** Identifier of the featureOfInterest to which this observation belongs */
	private String featureOfInterest;

	/** type of the observation */
	private String observationType;

	/**
	 * default constructor
	 */
	public SosObservationConstellation() {
		super();
	}

	/**
	 * constructor
	 * 
	 * @param procedure
	 *            Procedure by which the observation is made
	 * @param observableProperty
	 *            observableProperty to which the observation accords to
	 * @param offering
	 *            offering to which this observation belongs
	 * @param featureOfInterest
	 *            featureOfInterest to which this observation belongs
	 * @param observationType
	 *            Observation type
	 */
	public SosObservationConstellation(String procedure,
			AbstractSosPhenomenon observableProperty, String offering,
			String featureOfInterest, String observationType) {
		super();
		this.procedure = procedure;
		this.observableProperty = observableProperty;
		this.offering = offering;
		this.featureOfInterest = featureOfInterest;
		this.observationType = observationType;
	}

	/**
	 * Get the procedure
	 * 
	 * @return the procedure
	 */
	public String getProcedure() {
		return procedure;
	}

	/**
	 * Set the procedure
	 * 
	 * @param procedure
	 *            the procedure to set
	 */
	public void setProcedure(String procedure) {
		this.procedure = procedure;
	}

	/**
	 * Get observableProperty
	 * 
	 * @return the observableProperty
	 */
	public AbstractSosPhenomenon getObservableProperty() {
		return observableProperty;
	}

	/**
	 * Set observableProperty
	 * 
	 * @param observableProperty
	 *            the observableProperty to set
	 */
	public void setObservableProperty(AbstractSosPhenomenon observableProperty) {
		this.observableProperty = observableProperty;
	}

	/**
	 * Get offering
	 * 
	 * @return the offering
	 */
	public String getOffering() {
		return offering;
	}

	/**
	 * Set offering
	 * 
	 * @param offering
	 *            the offering to set
	 */
	public void setOffering(String offering) {
		this.offering = offering;
	}

	/**
	 * Get featureOfInterest
	 * 
	 * @return the featureOfInterest
	 */
	public String getFeatureOfInterest() {
		return featureOfInterest;
	}

	/**
	 * Set featureOfInterest
	 * 
	 * @param featureOfInterest
	 *            the featureOfInterest to set
	 */
	public void setFeatureOfInterest(String featureOfInterest) {
		this.featureOfInterest = featureOfInterest;
	}

	/**
	 * Get observation type
	 * 
	 * @return the observationType
	 */
	public String getObservationType() {
		return observationType;
	}

	/**
	 * Set observation type
	 * 
	 * @param observationType
	 *            the observationType to set
	 */
	public void setObservationType(String observationType) {
		this.observationType = observationType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object paramObject) {
		if (paramObject instanceof SosObservationConstellation) {
			SosObservationConstellation obsConst = (SosObservationConstellation) paramObject;
			if (offering != null) {
				return (procedure.equals(obsConst.getProcedure())
						&& observableProperty.getIdentifier().equals(
								obsConst.getObservableProperty()
										.getIdentifier())
						&& offering.equals(obsConst.getOffering())
						&& featureOfInterest.equals(obsConst
								.getFeatureOfInterest()) && observationType
						.equals(obsConst.getObservationType()));
			}
			return (procedure.equals(obsConst.getProcedure())
					&& observableProperty.getIdentifier().equals(
							obsConst.getObservableProperty().getIdentifier())
					&& featureOfInterest
							.equals(obsConst.getFeatureOfInterest()) && observationType
					.equals(obsConst.getObservationType()));
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + procedure.hashCode();
		hash = 31 * hash + observableProperty.hashCode();
		if (offering != null) {
			hash = 31 * hash + offering.hashCode();
		}
		hash = 31 * hash + featureOfInterest.hashCode();
		hash = 31 * hash + observationType.hashCode();
		return hash;
	}

	/**
	 * Check if constellations are equal excluding observableProperty
	 * 
	 * @param toCheckObsConst
	 *            Observation constellation to chek
	 * @return true if equals
	 */
	public boolean equalsExcludingObsProp(
			SosObservationConstellation toCheckObsConst) {
		if (offering != null) {
			return (procedure.equals(toCheckObsConst.getProcedure())
					&& offering.equals(toCheckObsConst.getOffering())
					&& featureOfInterest.equals(toCheckObsConst
							.getFeatureOfInterest())
					&& observationType.equals(toCheckObsConst
							.getObservationType()) && checkObservationTypeForMerging());
		}
		return (procedure.equals(toCheckObsConst.getProcedure())
				&& featureOfInterest.equals(toCheckObsConst
						.getFeatureOfInterest())
				&& observationType.equals(toCheckObsConst.getObservationType()) && checkObservationTypeForMerging());

	}

	private boolean checkObservationTypeForMerging() {
		return (!observationType.equals(OMConstants.OBS_TYPE_MEASUREMENT)
				&& !observationType
						.equals(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION) && !observationType
				.equals(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION));
	}

	/**
	 * Creates a copy of this element
	 * 
	 * @return Copy of observation constellation
	 */
	public SosObservationConstellation copy() {
		return new SosObservationConstellation(procedure, observableProperty,
				offering, featureOfInterest, observationType);
	}
}
