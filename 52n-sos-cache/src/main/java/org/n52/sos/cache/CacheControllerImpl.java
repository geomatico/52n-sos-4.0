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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.n52.sos.ds.ICacheFeederDAO;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CacheControllerImpl implements all methods to request all objects and
 * relationships from a standard datasource
 *
 */
public class CacheControllerImpl extends ACapabilitiesCacheController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheControllerImpl.class);
    
    /**
     * CacheImpl instance
     */
    private CacheImpl cacheImpl;

    /**
     * ICacheFeederDAO instance
     */
    private ICacheFeederDAO cacheFeederDAO;

    public CacheControllerImpl() {
        cacheImpl = new CacheImpl();
        cacheFeederDAO = getCacheDAO();
		schedule();
    }

	protected ICacheFeederDAO getCacheDAO()
	{
		return Configurator.getInstance().getCacheFeederDAO();
	}

    @Override
    public boolean updateCacheFromDB() throws OwsExceptionReport {
		LOGGER.info("Capabilities Cache Update started");
        boolean timeNotElapsed = true;
        try {
            // thread safe updating of the cache map
            timeNotElapsed = getUpdateLock().tryLock(SosConstants.UPDATE_TIMEOUT, TimeUnit.MILLISECONDS);

            // has waiting for lock got a time out?
            if (!timeNotElapsed) {
                LOGGER.warn("\n******\nCapabilities caches not updated "
                        + "because of time out while waiting for update lock." + "\nWaited "
                        + SosConstants.UPDATE_TIMEOUT + " milliseconds.\n******\n");
                return false;
            }

            while (!isUpdateIsFree()) {

                getUpdateFree().await();
            }
            setUpdateIsFree(false);

            cacheFeederDAO.updateCache(cacheImpl);

        } catch (InterruptedException ie) {
            LOGGER.error("Problem while threadsafe capabilities cache update", ie);
            return false;
        } finally {
            if (timeNotElapsed) {
                getUpdateLock().unlock();
                setUpdateIsFree(true);
            }
        }
        return true;
    }

    private void update(Case c) throws OwsExceptionReport
	{
		boolean timeNotElapsed = true;
	    try {
	        // thread safe updating of the cache map
	        timeNotElapsed = getUpdateLock().tryLock(SosConstants.UPDATE_TIMEOUT, TimeUnit.MILLISECONDS);
	
	        // has waiting for lock got a time out?
	        if (!timeNotElapsed) {
	            LOGGER.warn("\n******\nupdate after {} not successful because of time out while waiting for update lock." + 
	            					"\nWaited {} milliseconds.\n******\n",c,SosConstants.UPDATE_TIMEOUT);
	            return;
	        }
	        while (!isUpdateIsFree()) {
	            getUpdateFree().await();
	        }
	        setUpdateIsFree(false);
	        switch (c) {
			case OBSERVATION_DELETION:
				cacheFeederDAO.updateAfterObservationDeletion(cacheImpl);
				break;
			case SENSOR_DELETION:
				cacheFeederDAO.updateAfterSensorDeletion(cacheImpl);
				break;
			case OBSERVATION_INSERTION:
				cacheFeederDAO.updateAfterObservationInsertion(cacheImpl);
				break;
			case RESULT_TEMPLATE_INSERTION:
				cacheFeederDAO.updateAfterResultTemplateInsertion(cacheImpl);
				break;
			case SENSOR_INSERTION:
				cacheFeederDAO.updateAfterSensorInsertion(cacheImpl);
				break;
			}
	
	    } catch (InterruptedException e) {
	        LOGGER.error("Problem while threadsafe capabilities cache update", e);
	    } finally {
	        if (timeNotElapsed) {
	            getUpdateLock().unlock();
	            setUpdateIsFree(true);
	        }
	    }
	}

	@Override
	public void updateAfterObservationDeletion() throws OwsExceptionReport
	{
	    update(Case.OBSERVATION_DELETION);
	}

	@Override
	public void updateAfterObservationInsertion() throws OwsExceptionReport {
	    update(Case.OBSERVATION_INSERTION);
	}

	@Override
	public void updateAfterResultTemplateInsertion() throws OwsExceptionReport {
	    update(Case.RESULT_TEMPLATE_INSERTION);
	}

	@Override
	public void updateAfterSensorDeletion() throws OwsExceptionReport {
	    update(Case.SENSOR_DELETION);
	}

	@Override
	public void updateAfterSensorInsertion() throws OwsExceptionReport {
		update(Case.SENSOR_INSERTION);
	}

	/**
     * FIXME Update or remove, the method is doing nothing but wait
     * method for refreshing the metadata of fois in the capabilities cache; is
     * invoked when a new feature of interest is inserted into the SOS database
     *
     * @throws OwsExceptionReport
     *             if refreshing failed
     */
	@Override
    public void updateFoisCache() throws OwsExceptionReport {

        boolean timeNotElapsed = true;
        try {
            // thread safe updating of the cache map
            timeNotElapsed = getUpdateLock().tryLock(SosConstants.UPDATE_TIMEOUT, TimeUnit.MILLISECONDS);

            // has waiting for lock got a time out?
            if (!timeNotElapsed) {
                LOGGER.warn("\n******\nupdateFois() not successful "
                        + "because of time out while waiting for update lock." + "\nWaited "
                        + SosConstants.UPDATE_TIMEOUT + " milliseconds.\n******\n");
                return;
            }
            while (!isUpdateIsFree()) {

                getUpdateFree().await();
            }
            setUpdateIsFree(false);
            // queryFois();
            // queryFoiProcedures();
            // queryOffFois();
            // queryOffRelatedFeatures();

        } catch (InterruptedException e) {
            LOGGER.error("Problem while threadsafe capabilities cache update", e);
        } finally {
            if (timeNotElapsed) {
                getUpdateLock().unlock();
                setUpdateIsFree(true);
            }
        }
    }

    /**
     * FIXME Update or remove, the method is doing nothing but wait
     * refreshes sensor metadata; used after registration of new sensor at SOS
     *
     * @throws OwsExceptionReport
     *
     */
	@Override
    public void updateSensorMetadata() throws OwsExceptionReport {

        boolean timeNotElapsed = true;
        try {
            // thread safe updating of the cache map
            timeNotElapsed = getUpdateLock().tryLock(SosConstants.UPDATE_TIMEOUT, TimeUnit.MILLISECONDS);

            // has waiting for lock got a time out?
            if (!timeNotElapsed) {
                LOGGER.warn("\n******\nupdateSensorMetadata() not successful "
                        + "because of time out while waiting for update lock." + "\nWaited "
                        + SosConstants.UPDATE_TIMEOUT + " milliseconds.\n******\n");
                return;
            }
            while (!isUpdateIsFree()) {

                getUpdateFree().await();
            }
            setUpdateIsFree(false);
            // queryPhenProcs();
            // queryProcPhens();
            // queryProcedures();
            // queryOffProcedures();

        } catch (InterruptedException e) {
            LOGGER.error("Problem while threadsafe capabilities cache update", e);
        } finally {
            if (timeNotElapsed) {
                getUpdateLock().unlock();
                setUpdateIsFree(true);
            }
        }
    }

    /**
     * FIXME Update or remove, the method is doing nothing but wait
     * methods for adding relationships in Cache for recently received new
     * observation
     *
     * @param observation
     *            recently received observation which has been inserted into SOS
     *            db and whose relationships have to be maintained in cache
     * @throws OwsExceptionReport
     */
	@Override
    public void updateMetadata4newObservation(SosObservation observation) throws OwsExceptionReport {

        boolean timeNotElapsed = true;
        try {
            // thread safe updating of the cache map
            timeNotElapsed = getUpdateLock().tryLock(SosConstants.UPDATE_TIMEOUT, TimeUnit.MILLISECONDS);

            // has waiting for lock got a time out?
            if (!timeNotElapsed) {
                LOGGER.warn("\n******\nupdateMetadata4newObservation() not successful "
                        + "because of time out while waiting for update lock." + "\nWaited "
                        + SosConstants.UPDATE_TIMEOUT + " milliseconds.\n******\n");
                return;
            }
            while (!isUpdateIsFree()) {

                getUpdateFree().await();
            }
            setUpdateIsFree(false);

            // queryOffResultModels();
            // queryOffFois();
            // queryFois();
            // queryFoiProcedures();
            // queryPhens4CompPhens();
            // queryOffCompPhens();
            // queryTimes4Offerings();
            // querySRIDs();
            // queryOffRelatedFeatures();

        } catch (InterruptedException e) {
            LOGGER.error("Problem while threadsafe capabilities cache update", e);
        } finally {
            if (timeNotElapsed) {
                getUpdateLock().unlock();
                setUpdateIsFree(true);
            }
        }
    }

    /**
     * Returns the observedProperties (phenomenons) for the requested offering
     *
     * @param offering
     *            the offering for which observedProperties should be returned
     * @return Returns String[] containing the phenomenons of the requested
     *         offering
     */
    public List<String> getPhenomenons4Offering(String offering) {
        if (this.cacheImpl.getPhenomenons4Offering(offering) != null) {
            return new ArrayList<String>(this.cacheImpl.getPhenomenons4Offering(offering));
        }
        return new ArrayList<String>(0);
    }

    /**
     * methods returns all phenomena (single or components of composite
     * phenomena) which belong to the requested offering; necessary for database
     * queries
     *
     * @param offering
     *            the id of the offering for which all phenomena should be
     *            returned
     * @return List<String> containing all phenomena which belong to the
     *         offering
     */
    public List<String> getAllPhenomenons4Offering(String offering) {
        return new ArrayList<String>(this.cacheImpl.getObservableProperties4Offering(offering));
    }

    /**
     * Returns copy of the phenomenons of all offerings
     *
     * @return List<String> containing the phenomenons of all offerings
     */
    public List<String> getAllPhenomenons() {
        return new ArrayList<String>(this.cacheImpl.getObservableProperties());
    }

    /**
     * returns the offerings of this SOS
     *
     * @return List<String> containing the offerings of this SOS
     */
	@Override
    public List<String> getOfferings() {
        if (this.cacheImpl.getOfferings() != null) {
            return new ArrayList<String>(this.cacheImpl.getOfferings());
        }
        return new ArrayList<String>(0);
    }

    /**
     * returns the observation ids of this SOS
     *
     * @return List<String> containing the observation ids of this SOS
     */
	@Override
    public List<String> getObservationIdentifiers() {
        if (this.cacheImpl.getObservationIdentifiers() != null) {
            return new ArrayList<String>(this.cacheImpl.getObservationIdentifiers());
        }
        return new ArrayList<String>(0);
    }

    /**
     * returns the observedProperties for each offering
     *
     * @return Map<String, String[]> containing the offerings with its
     *         observedProperties
     */
    public Map<String, Collection<String>> getObsPhenomenons() {
        if (this.cacheImpl.getOffPhenomenons() != null) {
            return new TreeMap<String, Collection<String>>(this.cacheImpl.getOffPhenomenons());
        }
        return new TreeMap<String, Collection<String>>();
    }

    /**
     * returns the name of the requested offering
     *
     * @param offering
     *            the offering for which the name should be returned
     * @return String containing the name of the offering
     */
	@Override
    public String getOfferingName(String offering) {
        return this.cacheImpl.getOffName().get(offering);
    }

    /**
     * Returns TreeMap containing all procedures which are used by the Offerings
     * offered in this SOS
     *
     * @return TreeMap<String, SensorSystem> TreeMap containing all procedures
     *         which are used by the Offerings offered in this SOS
     */
	@Override
    public List<String> getProcedures() {
        if (this.cacheImpl.getProcedures() != null) {
            return new ArrayList<String>(this.cacheImpl.getProcedures());
        }
        return new ArrayList<String>(0);
    }

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
	@Override
    public Collection<String> getParentProcedures(String procId, boolean fullHierarchy, boolean includeSelf) {
        return this.cacheImpl.getParentProcs(procId, fullHierarchy, includeSelf);
    }

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
	@Override
    public Collection<String> getParentProcedures(Collection<String> procIds, boolean fullHierarchy,
            boolean includeSelves) {
        return this.cacheImpl.getParentProcs(procIds, fullHierarchy, includeSelves);
    }

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
	@Override
    public Collection<String> getChildProcedures(String procId, boolean fullHierarchy, boolean includeSelf) {
        return this.cacheImpl.getChildProcs(procId, fullHierarchy, includeSelf);
    }

    /**
     * Returns collection containing child procedures for the passed procedures,
     * optionally navigating the full hierarchy and including themselves
     *
     * @param procIds
     *            collection of procedure ids to find children for
     * @param fullHierarchy
     *            whether or not to navigate the full procedure hierarchy
     * @param includeSelves
     *            whether or not to include the passed procedure ids in the
     *            result
     *
     * @return Collection<String> containing the passed procedure id's children
     *         (and optionally itself)
     */
	@Override
    public Collection<String> getChildProcedures(Collection<String> procIds, boolean fullHierarchy,
            boolean includeSelves) {
        return this.cacheImpl.getChildProcs(procIds, fullHierarchy, includeSelves);
    }

    public Collection<String> getParentFeatures(String featureID, boolean fullHierarchy, boolean includeSelf) {
        return this.cacheImpl.getParentFeatures(featureID, fullHierarchy, includeSelf);
    }

    public Collection<String> getParentFeatures(Collection<String> featureIDs, boolean fullHierarchy,
            boolean includeSelves) {
        return this.cacheImpl.getParentFeatures(featureIDs, fullHierarchy, includeSelves);
    }

    public Collection<String> getChildFeatures(String featureIDs, boolean fullHierarchy, boolean includeSelf) {
        return this.cacheImpl.getParentFeatures(featureIDs, fullHierarchy, includeSelf);
    }

    public Collection<String> getChildFeatures(Collection<String> featureIDs, boolean fullHierarchy,
            boolean includeSelves) {
        return this.cacheImpl.getParentFeatures(featureIDs, fullHierarchy, includeSelves);
    }

    /**
     * return the result models for the requested offering
     *
     * @param offering
     *            the offering for which the result models should be returned
     * @return String[] containing the result models for the requested offering
     */
	@Override
    public Collection<String> getResultModels4Offering(String offering) {
        if (!this.cacheImpl.getKOfferingVObservationTypes().containsKey(offering)) {
            return new ArrayList<String>(0);
        }
        return this.cacheImpl.getKOfferingVObservationTypes().get(offering);
    }

    /**
     * returns the procedures for the requested offering
     *
     * @param offering
     *            the offering for which the procedures should be returned
     * @return String[] containing the procedures for the requested offering
     */
	@Override
    public Collection<String> getProcedures4Offering(String offering) {
        if (!this.cacheImpl.getOffProcedures().containsKey(offering)) {
            return new ArrayList<String>(0);
        }
        return new ArrayList<String>(this.cacheImpl.getOffProcedures().get(offering));
    }

    /**
     * returns the procedureID for the feature of interest (station)
     *
     * @param foiID
     *            the foiID for which the procedureID should returned
     * @return String representing the procedureID
     */
    public Collection<String> getProc4FOI(String foiID) {
        if (!this.cacheImpl.getFoiProcedures().containsKey(foiID)) {
            return new ArrayList<String>(0);
        }
        return new ArrayList<String>(this.cacheImpl.getFoiProcedures().get(foiID));
    }

    /**
     * return the unit of the values for the observedProperty
     *
     * @param observedProperty
     *            String observedProperty for which the type of the values
     *            should be returned
     * @return String representing the valueType of the values for the
     *         observedProperty
     */
    public String getUnit4ObsProp(String observedProperty) {
        return this.cacheImpl.getUnit4Phen().get(observedProperty);
    }

    /**
     * returns the phens4CompPhens
     *
     * @return HashMap<String, List<String>> the phens4CompPhens
     */
    public Map<String, Collection<String>> getPhens4CompPhens() {
        if (this.cacheImpl.getPhens4CompPhens() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getPhens4CompPhens());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    /**
     * Returns the offCompPhens
     *
     * @return HashMap<String, List<String>> the offCompPhens
     */
    public Map<String, Collection<String>> getOffCompPhens() {
        if (this.cacheImpl.getOffCompPhens() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getOffCompPhens());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    /**
     * returns the procedures for phenomena
     *
     * @return HashMap<String, List<String>> the procedures for phenomena
     */
    public Map<String, Collection<String>> getPhenProcs() {
        if (this.cacheImpl.getPhenProcs() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getPhenProcs());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    /**
     * returns the phenomena for procedures
     *
     * @return HashMap<String, List<String>> the phenomena for procedures
     */
    public Map<String, Collection<String>> getProcPhens() {
        if (this.cacheImpl.getProcPhens() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getProcPhens());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    /**
     * Returns Srid of coordinates stored in SOS database
     *
     * @return int Srid of coordinates stored in SOS database
     */
    public int getSrid() {
        return this.cacheImpl.getSrid();
    }

    /**
     *
     * @return Returns Map containing offeringIDs as keys and list of
     *         corresponding features as values
     */
    public Map<String, Collection<String>> getOffFeatures() {
        if (this.cacheImpl.getOffFeatures() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getOffFeatures());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    /**
     * returns the offerings for the passed procedure id
     *
     * @param procID
     *            id of procedure, for which related offerings should be
     *            returned
     * @return Returns offerings, for which passed procedure produces data
     */
	@Override
    public List<String> getOfferings4Procedure(String procID) {
        return new ArrayList<String>(this.cacheImpl.getOfferings4Procedure(procID));
    }

    /**
     * returns the offerings for the passed phenomenon
     *
     * @param phenID
     *            id of procedure, for which related offerings should be
     *            returned
     * @return Returns offerings, to which passed phenomenon belongs to
     */
    public List<String> getOfferings4Phenomenon(String phenID) {
        return new ArrayList<String>(this.cacheImpl.getOfferings4Phenomenon(phenID));
    }

    /**
     * Returns srids, which are supported by this SOS
     *
     * @return Returns srids, which are supported by this SOS
     */
	@Override
    public Collection<Integer> getSrids() {
        if (this.cacheImpl.getSrids() != null) {
            return new ArrayList<Integer>(this.cacheImpl.getSrids());
        }
        return new ArrayList<Integer>(0);
    }

    /**
     * returns procedures for offerings
     *
     * @return Map<String, List<String>> procedures for offerings
     */
    public Map<String, Collection<String>> getOffProcedures() {
        if (this.cacheImpl.getOffProcedures() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getOffProcedures());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    /**
     * returns related features for offerings
     *
     * @return Map<String, List<String>> related features for offerings
     */
    public Map<String, Collection<String>> getOffRelatedFeatures() {
        if (this.cacheImpl.getKOfferingVRelatedFeatures() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getKOfferingVRelatedFeatures());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    @Override
    public Collection<String> getObservablePropertiesForOffering(String offering) {
        if (this.cacheImpl.getPhenomenons4Offering(offering) != null) {
            return new ArrayList<String>(this.cacheImpl.getPhenomenons4Offering(offering));
        }
        return new ArrayList<String>(0);
    }

    @Override
    public Collection<String> getObservableProperties() {
        if (this.cacheImpl.getObservableProperties() != null) {
            return new ArrayList<String>(this.cacheImpl.getObservableProperties());
        }
        return new ArrayList<String>(0);
    }

    @Override
    public Map<String, Collection<String>> getKOfferingsVObservableProperties() {
        if (this.cacheImpl.getOffCompPhens() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getOffCompPhens());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    @Override
    public Collection<String> getProcedures4FeatureOfInterest(String foiID) {
        if (cacheImpl.getProceduresForFeature(foiID) != null) {
            return new ArrayList<String>(cacheImpl.getProceduresForFeature(foiID));
        }
        return new ArrayList<String>(0);
    }

    @Override
    public String getUnit4ObservableProperty(String observedProperty) {
        return this.cacheImpl.getUnit4ObsProp(observedProperty);
    }

    @Override
    public Collection<String> getFeatureOfInterest() {
        if (this.cacheImpl.getFeatureOfInterest() != null) {
            return new ArrayList<String>(this.cacheImpl.getFeatureOfInterest());
        }
        return new ArrayList<String>(0);
    }

    @Override
    public Map<String, Collection<String>> getObservableProperties4CompositePhenomenons() {
        if (this.cacheImpl.getPhens4CompPhens() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getPhens4CompPhens());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    @Override
    public Map<String, Collection<String>> getKOfferingVCompositePhenomenons() {
        if (this.cacheImpl.getOffCompPhens() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getOffCompPhens());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    @Override
    public Map<String, Collection<String>> getKObservablePropertyVProcedures() {
        if (this.cacheImpl.getPhenProcs() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getPhenProcs());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    @Override
    public Map<String, Collection<String>> getKProcedureVObservableProperties() {
        if (this.cacheImpl.getProcPhens() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getProcPhens());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    @Override
    public Map<String, Collection<String>> getKOfferingVFeatures() {
        if (this.cacheImpl.getOffFeatures() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getOffFeatures());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    @Override
    public Collection<String> getOfferings4ObservableProperty(String phenID) {
        if (this.cacheImpl.getOfferings4Phenomenon(phenID) != null) {
            return new ArrayList<String>(this.cacheImpl.getOfferings4Phenomenon(phenID));
        }
        return new ArrayList<String>(0);
    }

    @Override
    public Map<String, Collection<String>> getKOfferingVProcedures() {
        if (this.cacheImpl.getOffProcedures() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getOffProcedures());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    @Override
    public Map<String, Collection<String>> getKOfferingVRelatedFeatures() {
        if (this.cacheImpl.getKOfferingVRelatedFeatures() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getKOfferingVRelatedFeatures());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    @Override
    public Map<String, Collection<String>> getKCompositePhenomenonVObservableProperty() {
        if (this.cacheImpl.getPhens4CompPhens() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getPhens4CompPhens());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    @Override
    public Map<String, Collection<String>> getKRelatedFeaturesVRole() {
        if (this.cacheImpl.getKRelatedFeatureVRole() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getKRelatedFeatureVRole());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    @Override
    public Map<String, Collection<String>> getKOfferingVObservationTypes() {
        if (this.cacheImpl.getKOfferingVObservationTypes() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getKOfferingVObservationTypes());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    @Override
    public Collection<String> getObservationTypes4Offering(String offering) {
        if (this.cacheImpl.getKOfferingVObservationTypes() != null) {
            return new ArrayList<String>(this.cacheImpl.getKOfferingVObservationTypes().get(offering));
        }
        return new ArrayList<String>(0);
    }

    @Override
    public Collection<String> getObservationTypes() {
        if (this.cacheImpl.getObservationTypes() != null) {
            return new ArrayList<String>(this.cacheImpl.getObservationTypes());
        }
        return new ArrayList<String>(0);
    }

    @Override
    public Collection<String> getFeatureOfInterestTypes() {
        if (this.cacheImpl.getFeatureOfInterestTypes() != null) {
            return new ArrayList<String>(this.cacheImpl.getFeatureOfInterestTypes());
        }
        return new ArrayList<String>(0);
    }

    @Override
    public Map<String, Collection<String>> getAllowedKOfferingVObservationTypes() {
        if (this.cacheImpl.getAllowedKOfferingVObservationType() != null) {
            return new HashMap<String, Collection<String>>(this.cacheImpl.getAllowedKOfferingVObservationType());
        }
        return new HashMap<String, Collection<String>>(0);
    }

    @Override
    public Collection<String> getAllowedObservationTypes4Offering(String offering) {
        if (this.cacheImpl.getAllowedKOfferingVObservationType() != null) {
            return new ArrayList<String>(this.cacheImpl.getAllowedKOfferingVObservationType().get(offering));
        }
        return new ArrayList<String>(0);
    }

    @Override
    public Collection<String> getResultTemplates() {
        if (this.cacheImpl.getResultTemplates() != null) {
            return new ArrayList<String>(this.cacheImpl.getResultTemplates());
        }
        return new ArrayList<String>(0);
    }

	@Override
	public SosEnvelope getEnvelopeForOffering(String offering) {
		if (this.cacheImpl.getKOfferingVEnvelope() != null) {
			return this.cacheImpl.getKOfferingVEnvelope().get(offering);
		}
		return null;
	}

	@Override
	public DateTime getMinTimeForOffering(String offering) {
		if (this.cacheImpl.getKOfferingVMinTime() != null) {
			return this.cacheImpl.getKOfferingVMinTime().get(offering);
		}
		return null;
	}

	@Override
	public DateTime getMaxTimeForOffering(String offering) {
		if (this.cacheImpl.getKOfferingVMaxTime() != null) {
			return this.cacheImpl.getKOfferingVMaxTime().get(offering);
		}
		return null;
	}

	@Override
	public SosEnvelope getGlobalEnvelope() {
		return this.cacheImpl.getGlobalEnvelope();
	}

	@Override
	public DateTime getMinEventTime() {
		return this.cacheImpl.getMinEventTime();
	}

	@Override
	public DateTime getMaxEventTime() {
		return this.cacheImpl.getMaxEventTime();
	}

	@Override
	protected CacheImpl getCapabilitiesCache()
	{
		return cacheImpl;
	}

}
