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
package org.n52.sos.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.joda.time.DateTime;
import org.n52.sos.config.annotation.Configurable;
import org.n52.sos.config.annotation.Setting;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.request.DeleteSensorRequest;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.request.InsertResultTemplateRequest;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.InsertResultTemplateResponse;
import org.n52.sos.response.InsertSensorResponse;
import org.n52.sos.service.ConfigurationException;
import org.n52.sos.util.Cleanupable;
import org.n52.sos.util.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for capabilities cache controller implementations.
 *
 *
 */
@Configurable
public abstract class ACapabilitiesCacheController implements Cleanupable {

	private class UpdateTimerTask extends TimerTask {

		@Override
		public void run() {
			try {
				if (updateCacheFromDB()) {
					LOGGER.info("Timertask: capabilities cache update successful!");
				} else {
					LOGGER.warn("Timertask: capabilities cache update not successful!");
				}
				schedule();
			} catch (OwsExceptionReport e) {
				LOGGER.error("Fatal error: Timertask couldn't update capabilities cache! Switch log level to DEBUG to get more details.");
				LOGGER.debug("Exception thrown",e);
			}
		}
	}

    @Deprecated
    protected enum Case {
        OBSERVATION_INSERTION,
        SENSOR_INSERTION,
        RESULT_TEMPLATE_INSERTION,
        RESULT_INSERTION,
        SENSOR_DELETION,
        OBSERVATION_DELETION
    }

	private static final Logger LOGGER = LoggerFactory.getLogger(ACapabilitiesCacheController.class);

    /**
     * Lock management
     */
    private final ReentrantLock updateLock = new ReentrantLock(true);
    private final Condition updateFree = updateLock.newCondition();
    private boolean initialized = false;
    private boolean updateIsFree = true;
    private long updateInterval;
	private final Timer timer = new Timer("52n-sos-capabilities-cache-controller", true);
	private TimerTask current = null;

	/**
	 * Starts a new timer task
	 */
	protected final void schedule() {
		/*
		 * Timers can not be rescheduled.
		 * To make the interval changeable reschedule a new timer.
		 */
		current = new UpdateTimerTask();
		long delay = getUpdateInterval();
		if (!initialized) {
			delay = 1;
			initialized = true;
		}
		if (delay > 0) {
			LOGGER.info("Next CapabilitiesCacheUpdate in {}m: {}", delay/60000, new DateTime(System.currentTimeMillis()+delay));
			timer.schedule(current, delay);
		}
	}

    @Setting(CacheControllerSettings.CAPABILITIES_CACHE_UPDATE_INTERVAL)
    public void setUpdateInterval(int interval) throws ConfigurationException {
        Validation.greaterZero("Cache update interval", interval);
        if (this.updateInterval != interval) {
            this.updateInterval = interval;
            reschedule();
        }
    }

    private long getUpdateInterval() {
        return this.updateInterval * 60000;
    }

	/**
	 * Stops the current task, if available and starts a new {@link TimerTask}.
	 * @see #schedule()
	 */
	protected void reschedule() {
        cancelCurrent();
		schedule();
	}
    
    protected void cancelCurrent() {
        if (this.current != null) {
            this.current.cancel();
            LOGGER.debug("Current {} canceled", UpdateTimerTask.class.getSimpleName());
        }
    }
    
     protected void cancelTimer() {
        if (this.timer != null) {
            this.timer.cancel();
            LOGGER.debug("Cache Update timer canceled.");
        }
    }

    @Override
	public void cleanup() {
        cancelCurrent(); 
        cancelTimer();
	}

    @Override
    protected void finalize() {
        try {
			cleanup();
            super.finalize();
        } catch (Throwable e) {
            LOGGER.error("Could not finalize CapabilitiesCacheController! " + e.getMessage());
        }
    }

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
     * @return true, if updated successfully
     * @throws OwsExceptionReport
     *             if the query of one of the values described upside failed
     */
    public abstract boolean updateCacheFromDB() throws OwsExceptionReport;

    public abstract void updateAfterSensorInsertion(InsertSensorRequest sosRequest, InsertSensorResponse sosResponse);

	public abstract void updateAfterObservationInsertion(InsertObservationRequest sosRequest);

	public abstract void updateAfterSensorDeletion(DeleteSensorRequest sosRequest);

	public abstract void updateAfterObservationDeletion() throws OwsExceptionReport;

    public abstract void updateAfterResultTemplateInsertion(InsertResultTemplateRequest sosRequest, InsertResultTemplateResponse sosResponse);

	public abstract void updateAfterResultInsertion(String templateIdentifier, SosObservation sosObservation);

	/**
     * Returns the observedProperties (phenomenons) for the requested offering
     *
     * @param offering
     *            the offering for which observedProperties should be returned
     * @return List<String> containing the phenomenons of the requested offering
     */
    public abstract Collection<String> getObservablePropertiesForOffering(String offering);

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
    public abstract Collection<String> getObservationIdentifiers();

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
    public abstract Collection<String> getParentProcedures(String procId, boolean fullHierarchy, boolean includeSelf);

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
    public abstract Collection<String> getParentProcedures(Collection<String> procIds, boolean fullHierarchy,
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
    public abstract Collection<String> getChildProcedures(String procId, boolean fullHierarchy, boolean includeSelf);

    /**
     * Returns collection containing child procedures for the passed procedures,
     * optionally navigating the full hierarchy and including themselves
     *
     * @param procIds
     *            collection of procedure ids to find children for
     * @param fullHierarchy
     *            whether or not to navigate the full procedure hierarchy
     * @param includeSelves
     *            whether or not to include the passed procedure id in the
     *            result
     *
     * @return Collection<String> containing the passed procedure id's children
     *         (and optionally itself)
     */
    public abstract Collection<String> getChildProcedures(Collection<String> procIds, boolean fullHierarchy,
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
    public abstract Collection<String> getProcedures4FeatureOfInterest(String foiID);

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
    public abstract Collection<String> getOfferings4ObservableProperty(String phenID);

    /**
     * Returns srids, which are supported by this SOS
     *
     * @return Returns srids, which are supported by this SOS
     */
    public abstract Collection<Integer> getSrids();

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

    public abstract Collection<String> getObservationTypes();

    public abstract Collection<String> getFeatureOfInterestTypes();

    public abstract Map<String, Collection<String>> getAllowedKOfferingVObservationTypes();

    public abstract Collection<String> getAllowedObservationTypes4Offering(String offering);

    public abstract Collection<String> getResultTemplates();

	public abstract SosEnvelope getEnvelopeForOffering(String offering);

	public abstract DateTime getMinTimeForOffering(String offering);

	public abstract DateTime getMaxTimeForOffering(String offering);

	public abstract SosEnvelope getGlobalEnvelope();

	public abstract DateTime getMinEventTime();

	public abstract DateTime getMaxEventTime();

	protected abstract CapabilitiesCache getCache();

	/**
	 * @return a Collection&ltString&gt; of currently supported procedure description formats
	 */
	public abstract Collection<String> getProcedureDescriptionFormats();

	/**
	 * @return a Map&lt;String, Collection&ltString&gt;&gt; of currently available result templates for offerings
	 */
	public abstract Map<String, Collection<String>> getKOfferingVResultTemplates();

	/**
	 * @return a Map&lt;String, Collection&ltString&gt;&gt; of currently available observed properties per result template
	 */
	public abstract Map<String, Collection<String>> getKResultTemplateVObservedProperties();

	/**
	 * @return a Map&lt;String, Collection&ltString&gt;&gt; of currently available features per result template
	 */
	public abstract Map<String, Collection<String>> getKResultTemplateVFeaturesOfInterest();
}
