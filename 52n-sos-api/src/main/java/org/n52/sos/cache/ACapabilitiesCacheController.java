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

package org.n52.sos.cache;

import java.util.Collection;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.sos.SosConstants.ValueTypes;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for capabilities cache controller implementations.
 * 
 * 
 */
public abstract class ACapabilitiesCacheController extends TimerTask {

	/** logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ACapabilitiesCacheController.class);

	/**
	 * Lock management
	 */
	private final ReentrantLock updateLock = new ReentrantLock(true);

	private final Condition updateFree = updateLock.newCondition();

	private boolean updateIsFree = true;

	/**
	 * default constructor
	 */
	public ACapabilitiesCacheController() {
	}

	/**
	 * Implements TimerTask's abstract run method.
	 */
	public void run() {
		try {
			if (update(true)) {
				LOGGER.info("Timertask: capabilities cache update successful!");
			} else {
				LOGGER.warn("Timertask: capabilities cache update not successful!");
			}
		} catch (OwsExceptionReport e) {
			LOGGER.error("Fatal error: Timertask couldn't update capabilities cache!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		try {
			cancel();
			super.finalize();
		} catch (Throwable e) {
			LOGGER.error("Could not finalize CapabilitiesCacheController! "
					+ e.getMessage());
		}
	}

	/**
	 * queries the service offerings, the observedProperties for each offering,
	 * and the offering names from the DB and sets these values in this
	 * configurator
	 * 
	 * @param checkLastUpdateTime
	 *            Indicator, if some other methods should be started
	 * @return true, if updated successfully
	 * @throws OwsExceptionReport
	 *             if the query of one of the values described upside failed
	 */
	public abstract boolean update(boolean checkLastUpdateTime)
			throws OwsExceptionReport;

	/**
	 * method for refreshing the metadata of fois in the capabilities cache; is
	 * invoked when a new feature of interest is inserted into the SOS database
	 * 
	 * @throws OwsExceptionReport
	 *             if refreshing failed
	 */
	public abstract void updateFoisCache() throws OwsExceptionReport;

	/**
	 * refreshes sensor metadata; used after registration of new sensor at SOS
	 * 
	 * @throws OwsExceptionReport
	 *             if refreshing failed
	 * 
	 */
	public abstract void updateSensorMetadata() throws OwsExceptionReport;

	/**
	 * methods for adding relationships in Cache for recently received new
	 * observation
	 * 
	 * @param observation
	 *            recently received observation which has been inserted into SOS
	 *            db and whose relationships have to be maintained in cache
	 * @throws OwsExceptionReport
	 *             if refreshing failed
	 */
	public abstract void updateMetadata4newObservation(
			SosObservation observation) throws OwsExceptionReport;

	/**
	 * @return the updateIsFree
	 */
	protected boolean isUpdateIsFree() {
		return updateIsFree;
	}

	/**
	 * @param updateIsFree
	 *            the updateIsFree to set
	 */
	protected void setUpdateIsFree(boolean updateIsFree) {
		this.updateIsFree = updateIsFree;
	}

	/**
	 * @return the updateLock
	 */
	protected ReentrantLock getUpdateLock() {
		return updateLock;
	}

	/**
	 * @return the updateFree
	 */
	protected Condition getUpdateFree() {
		return updateFree;
	}

	/**
	 * Returns the observedProperties (phenomenons) for the requested offering
	 * 
	 * @param offering
	 *            the offering for which observedProperties should be returned
	 * @return List<String> containing the phenomenons of the requested offering
	 */
	public abstract Collection<String> getObservablePropertiesForOffering(
			String offering);

	/**
	 * Returns copy of the phenomenons of all offerings
	 * 
	 * @return List<String> containing the phenomenons of all offerings
	 */
	public abstract Collection<String> getObservableProperties();

	/**
	 * returns the offerings of this SOS
	 * 
	 * @return List<String> containing the offerings of this SOS
	 */
	public abstract Collection<String> getOfferings();

	public abstract String getOfferingName(String offering);

	/**
	 * returns the observation ids of this SOS
	 * 
	 * @return List<String> containing the observation ids of this SOS
	 */
	public abstract Collection<String> getObservationIds();

	/**
	 * returns the observedProperties for each offering
	 * 
	 * @return Map<String, String[]> containing the offerings with its
	 *         observedProperties
	 */
	public abstract Map<String, Collection<String>> getKOfferingsVObservableProperties();

	/**
	 * Returns TreeMap containing all procedures which are used by the Offerings
	 * offered in this SOS
	 * 
	 * @return List<String> containing all procedures which are used by the
	 *         Offerings offered in this SOS
	 */
	public abstract Collection<String> getProcedures();

	/**
	 * Returns collection containing parent procedures for the passed procedure,
	 * optionally navigating the full hierarchy and including itself
	 * 
	 * @param procId
	 *            the procedure id to find parents for
	 * @param fullHierarchy
	 *            whether or not to navigate the full procedure hierarchy
	 * @param includeSelf
	 *            whether or not to include the passed procedure id in the
	 *            result
	 * 
	 * @return Collection<String> containing the passed procedure id's parents
	 *         (and optionally itself)
	 */
	public abstract Collection<String> getParentProcedures(String procId,
			boolean fullHierarchy, boolean includeSelf);

	/**
	 * Returns collection containing parent procedures for the passed
	 * procedures, optionally navigating the full hierarchy and including
	 * themselves
	 * 
	 * @param procIds
	 *            collection of the procedure ids to find parents for
	 * @param fullHierarchy
	 *            whether or not to navigate the full procedure hierarchy
	 * @param includeSelves
	 *            whether or not to include the passed procedure ids in the
	 *            result
	 * 
	 * @return Collection<String> containing the passed procedure id's parents
	 *         (and optionally themselves)
	 */
	public abstract Collection<String> getParentProcedures(
			Collection<String> procIds, boolean fullHierarchy,
			boolean includeSelves);

	/**
	 * Returns collection containing child procedures for the passed procedure,
	 * optionally navigating the full hierarchy and including itself
	 * 
	 * @param procId
	 *            procedure id to find children for
	 * @param fullHierarchy
	 *            whether or not to navigate the full procedure hierarchy
	 * @param includeSelf
	 *            whether or not to include the passed procedure id in the
	 *            result
	 * 
	 * @return Collection<String> containing the passed procedure id's children
	 *         (and optionally itself)
	 */
	public abstract Collection<String> getChildProcedures(String procId,
			boolean fullHierarchy, boolean includeSelf);

	/**
	 * Returns collection containing child procedures for the passed procedures,
	 * optionally navigating the full hierarchy and including themselves
	 * 
	 * @param procIds
	 *            collection of procedure ids to find children for
	 * @param fullHierarchy
	 *            whether or not to navigate the full procedure hierarchy
	 * @param includeSelf
	 *            whether or not to include the passed procedure id in the
	 *            result
	 * 
	 * @return Collection<String> containing the passed procedure id's children
	 *         (and optionally itself)
	 */
	public abstract Collection<String> getChildProcedures(
			Collection<String> procIds, boolean fullHierarchy,
			boolean includeSelves);

	/**
	 * return the result models for the requested offering
	 * 
	 * @param offering
	 *            the offering for which the result models should be returned
	 * @return String[] containing the result models for the requested offering
	 */
	public abstract Collection<String> getResultModels4Offering(String offering);

	/**
	 * returns the procedures for the requested offering
	 * 
	 * @param offering
	 *            the offering for which the procedures should be returned
	 * @return String[] containing the procedures for the requested offering
	 */
	public abstract Collection<String> getProcedures4Offering(String offering);

	/**
	 * returns the procedureID for the feature of interest (station)
	 * 
	 * @param foiID
	 *            the foiID for which the procedureID should returned
	 * @return String representing the procedureID
	 */
	public abstract Collection<String> getProcedures4FeatureOfInterest(
			String foiID);

	/**
	 * return the unit of the values for the observedProperty
	 * 
	 * @param observedProperty
	 *            String observedProperty for which the type of the values
	 *            should be returned
	 * @return String representing the valueType of the values for the
	 *         observedProperty
	 */
	public abstract String getUnit4ObservableProperty(String observedProperty);

	/**
	 * returns all features
	 * 
	 * @return ArrayList<String> the FOIs
	 */
	public abstract Collection<String> getAllFeature();

	/**
	 * returns only FOIs which are sampling features
	 * 
	 * @return ArrayList<String> the FOIs
	 */
	public abstract Collection<String> getFeatureOfInterest();

	/**
	 * returns the phens4CompPhens
	 * 
	 * @return HashMap<String, List<String>> the phens4CompPhens
	 */
	public abstract Map<String, Collection<String>> getObservableProperties4CompositePhenomenons();

	/**
	 * Returns the offCompPhens
	 * 
	 * @return HashMap<String, List<String>> the offCompPhens
	 */
	public abstract Map<String, Collection<String>> getKOfferingVCompositePhenomenons();

	/**
	 * returns the procedures for phenomena
	 * 
	 * @return HashMap<String, List<String>> the procedures for phenomena
	 */
	public abstract Map<String, Collection<String>> getKObservablePropertyVProcedures();

	/**
	 * returns the phenomena for procedures
	 * 
	 * @return HashMap<String, List<String>> the phenomena for procedures
	 */
	public abstract Map<String, Collection<String>> getKProcedureVObservableProperties();

	/**
	 * returns the value type for the passed phenomenon
	 * 
	 * @param phenomenonID
	 *            id of the phenomenon for which the value type should be
	 *            returned
	 * @return Returns the value type for the passed phenomenon
	 */
	public abstract ValueTypes getValueType4ObservableProperty(
			String phenomenonID);

	/**
	 * 
	 * @return Returns Map containing offeringIDs as keys and list of
	 *         corresponding features as values
	 */
	public abstract Map<String, Collection<String>> getKOfferingVFeatures();

	/**
	 * returns the offerings for the passed procedure id
	 * 
	 * @param procID
	 *            id of procedure, for which related offerings should be
	 *            returned
	 * @return Returns offerings, for which passed procedure produces data
	 */
	public abstract Collection<String> getOfferings4Procedure(String procID);

	/**
	 * returns the offerings for the passed phenomenon
	 * 
	 * @param phenID
	 *            id of procedure, for which related offerings should be
	 *            returned
	 * @return Returns offerings, to which passed phenomenon belongs to
	 */
	public abstract Collection<String> getOfferings4ObservableProperty(
			String phenID);

	/**
	 * Returns srids, which are supported by this SOS
	 * 
	 * @return Returns srids, which are supported by this SOS
	 */
	public abstract Collection<Integer> getSrids();

	/**
	 * returns valuetypes for obsProps
	 * 
	 * @return HashMap<String, ValueTypes> valuetypes for obsProps
	 */
	public abstract Map<String, ValueTypes> getKObservablePropertyVValueType();

	/**
	 * returns procedures for offerings
	 * 
	 * @return Map<String, List<String>> procedures for offerings
	 */
	public abstract Map<String, Collection<String>> getKOfferingVProcedures();

	/**
	 * returns related features
	 * 
	 * @return List<String> related features
	 */
	public abstract Map<String, Collection<String>> getKOfferingVRelatedFeatures();

	/**
	 * returns map with compositePhenomenon and their observableProperties
	 * 
	 * @return Map<String, Collection<String>> observableProperties for
	 *         compositePhenomenon
	 */
	public abstract Map<String, Collection<String>> getKCompositePhenomenonVObservableProperty();

	/**
	 * returns map with relatedFeatures and their roles.
	 * 
	 * @return Map<String, Collection<String>> roles for relatedFeatures
	 */
	public abstract Map<String, Collection<String>> getKRelatedFeaturesVRole();
	
	public abstract Map<String, Collection<String>> getKOfferingVObservationTypes();
	
	public abstract Collection<String> getObservationTypes4Offering(String offering);
}
