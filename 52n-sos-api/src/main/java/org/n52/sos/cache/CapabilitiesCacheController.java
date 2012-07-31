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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.n52.sos.ogc.sos.SosConstants.ValueTypes;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ds.ICacheFeederDAO;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CapabilitiesCacheController implements all methods to request all objects and
 * relationships from a standard datasource
 * 
 */
public class CapabilitiesCacheController extends ACapabilitiesCacheController {

    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(CapabilitiesCacheController.class);

    /**
     * CapabilitiesCache instance
     */
    private CapabilitiesCache capabilitiesCache;
    
    /**
     * ICacheFeederDAO instance
     */
    private ICacheFeederDAO cacheFeederDAO;

    /**
     * constructor
     * 
     */
    public CapabilitiesCacheController() {
        super();
        this.capabilitiesCache = new CapabilitiesCache();
        this.cacheFeederDAO = Configurator.getInstance().getCacheFeederDAO();
        // try {
        // update(false);
        // } catch (OwsExceptionReport e) {
        // log.fatal("Fatal error: Couldn't initialize capabilities cache!");
        // }
    }

    /**
     * queries the service offerings, the observedProperties for each offering,
     * and the offering names from the DB and sets these values in this
     * configurator
     * 
     * @throws OwsExceptionReport
     *             if the query of one of the values described upside failed
     * 
     */
    public boolean update(boolean checkLastUpdateTime) throws OwsExceptionReport {
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
            
            this.cacheFeederDAO.initalizeCache(capabilitiesCache);

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

        // queryObservationIds();
    }

    /**
     * method for refreshing the metadata of fois in the capabilities cache; is
     * invoked when a new feature of interest is inserted into the SOS database
     * 
     * @throws OwsExceptionReport
     *             if refreshing failed
     */
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
//            queryFois();
//            queryFoiProcedures();
//            queryOffFois();
//            queryOffRelatedFeatures();

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
     * refreshes sensor metadata; used after registration of new sensor at SOS
     * 
     * @throws OwsExceptionReport
     * 
     */
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
//            queryPhenProcs();
//            queryProcPhens();
//            queryProcedures();
//            queryOffProcedures();

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
     * methods for adding relationships in Cache for recently received new
     * observation
     * 
     * @param observation
     *            recently received observation which has been inserted into SOS
     *            db and whose relationships have to be maintained in cache
     * @throws OwsExceptionReport
     */
    public void updateMetadata4newObservation(SosObservation observation)
            throws OwsExceptionReport {

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

//            queryOffResultModels();
//            queryOffFois();
//            queryFois();
//            queryFoiProcedures();
//            queryPhens4CompPhens();
//            queryOffCompPhens();
//            queryTimes4Offerings();
//            querySRIDs();
//            queryOffRelatedFeatures();

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
        if (this.capabilitiesCache.getPhenomenons4Offering(offering) != null) {
            return new ArrayList<String>(this.capabilitiesCache.getPhenomenons4Offering(offering));
        }
        return new ArrayList<String>();
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
        return new ArrayList<String>(this.capabilitiesCache.getAllPhenomenons4Offering(offering));
    }

    /**
     * Returns copy of the phenomenons of all offerings
     * 
     * @return List<String> containing the phenomenons of all offerings
     */
    public List<String> getAllPhenomenons() {
        return new ArrayList<String>(this.capabilitiesCache.getAllPhenomenons());
    }

    /**
     * returns the offerings of this SOS
     * 
     * @return List<String> containing the offerings of this SOS
     */
    public List<String> getOfferings() {
        if (this.capabilitiesCache.getOfferings() != null) {
            return new ArrayList<String>(this.capabilitiesCache.getOfferings());
        }
        return new ArrayList<String>();
    }

    /**
     * returns the observation ids of this SOS
     * 
     * @return List<String> containing the observation ids of this SOS
     */
    public List<String> getObservationIds() {
        if (this.capabilitiesCache.getObsIds() != null) {
            return new ArrayList<String>(this.capabilitiesCache.getObsIds());
        }
        return new ArrayList<String>();
    }

    /**
     * returns the observedProperties for each offering
     * 
     * @return Map<String, String[]> containing the offerings with its
     *         observedProperties
     */
    public Map<String, Collection<String>> getObsPhenomenons() {
        if (this.capabilitiesCache.getOffPhenomenons() != null) {
            return new TreeMap<String, Collection<String>>(this.capabilitiesCache.getOffPhenomenons());
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
    public String getOfferingName(String offering) {
        return this.capabilitiesCache.getOffName().get(offering);
    }

    /**
     * Returns TreeMap containing all procedures which are used by the Offerings
     * offered in this SOS
     * 
     * @return TreeMap<String, SensorSystem> TreeMap containing all procedures
     *         which are used by the Offerings offered in this SOS
     */
    public List<String> getProcedures() {
        if (this.capabilitiesCache.getProcedures() != null) {
            return new ArrayList<String>(this.capabilitiesCache.getProcedures());
        }
        return new ArrayList<String>();
    }
    
    /**
     * Returns collection containing parent procedures for the passed procedure, optionally navigating
     * the full hierarchy and including itself
     * 
     * @param procId the procedure id to find parents for
     * @param fullHierarchy whether or not to navigate the full procedure hierarchy 
     * @param includeSelf whether or not to include the passed procedure id in the result
     * 
     * @return Collection<String> containing the passed procedure id's parents (and optionally itself)
     */
    public Collection<String> getParentProcedures( String procId, boolean fullHierarchy, boolean includeSelf ){
        return this.capabilitiesCache.getParentProcs( procId, fullHierarchy, includeSelf );        
    }

    /**
     * Returns collection containing parent procedures for the passed procedures, optionally navigating
     * the full hierarchy and including themselves
     * 
     * @param procIds collection of the procedure ids to find parents for
     * @param fullHierarchy whether or not to navigate the full procedure hierarchy 
     * @param includeSelves whether or not to include the passed procedure ids in the result
     * 
     * @return Collection<String> containing the passed procedure id's parents (and optionally themselves)
     */
    public Collection<String> getParentProcedures( Collection<String> procIds, boolean fullHierarchy,
            boolean includeSelves ){
        return this.capabilitiesCache.getParentProcs( procIds, fullHierarchy, includeSelves );
    }

    /**
     * Returns collection containing child procedures for the passed procedure, optionally navigating
     * the full hierarchy and including itself
     * 
     * @param procId procedure id to find children for
     * @param fullHierarchy whether or not to navigate the full procedure hierarchy 
     * @param includeSelf whether or not to include the passed procedure id in the result
     * 
     * @return Collection<String> containing the passed procedure id's children (and optionally itself)
     */
    public Collection<String> getChildProcedures( String procId, boolean fullHierarchy, boolean includeSelf ){
        return this.capabilitiesCache.getChildProcs( procId, fullHierarchy, includeSelf );        
    }

    /**
     * Returns collection containing child procedures for the passed procedures, optionally navigating
     * the full hierarchy and including themselves
     * 
     * @param procIds collection of procedure ids to find children for
     * @param fullHierarchy whether or not to navigate the full procedure hierarchy 
     * @param includeSelves whether or not to include the passed procedure ids in the result
     * 
     * @return Collection<String> containing the passed procedure id's children (and optionally itself)
     */
    public Collection<String> getChildProcedures( Collection<String> procIds, boolean fullHierarchy,
            boolean includeSelves ){
        return this.capabilitiesCache.getChildProcs( procIds, fullHierarchy, includeSelves );
    }
    
    
    public Collection<String> getParentFeatures( String featureID, boolean fullHierarchy, boolean includeSelf ){
        return this.capabilitiesCache.getParentProcs( featureID, fullHierarchy, includeSelf );        
    }

    public Collection<String> getParentFeatures( Collection<String> featureIDs, boolean fullHierarchy,
            boolean includeSelves ){
        return this.capabilitiesCache.getParentProcs( featureIDs, fullHierarchy, includeSelves );
    }

    public Collection<String> getChildFeatures( String featureIDs, boolean fullHierarchy, boolean includeSelf ){
        return this.capabilitiesCache.getChildProcs( featureIDs, fullHierarchy, includeSelf );        
    }

    public Collection<String> getChildFeatures( Collection<String> featureIDs, boolean fullHierarchy,
            boolean includeSelves ){
        return this.capabilitiesCache.getChildProcs( featureIDs, fullHierarchy, includeSelves );
    }

    /**
     * return the result models for the requested offering
     * 
     * @param offering
     *            the offering for which the result models should be returned
     * @return String[] containing the result models for the requested offering
     */
    public Collection<String> getResultModels4Offering(String offering) {
        if (!this.capabilitiesCache.getOffResultModels().containsKey(offering)) {
            return new ArrayList<String>();
        }
        return this.capabilitiesCache.getOffResultModels().get(offering);
    }

    /**
     * returns the procedures for the requested offering
     * 
     * @param offering
     *            the offering for which the procedures should be returned
     * @return String[] containing the procedures for the requested offering
     */
    public Collection<String> getProcedures4Offering(String offering) {
        if (!this.capabilitiesCache.getOffProcedures().containsKey(offering)) {
            return new ArrayList<String>();
        }
        return new ArrayList<String>(this.capabilitiesCache.getOffProcedures().get(offering));
    }

    /**
     * returns the procedureID for the feature of interest (station)
     * 
     * @param foiID
     *            the foiID for which the procedureID should returned
     * @return String representing the procedureID
     */
    public Collection<String> getProc4FOI(String foiID) {
        if (!this.capabilitiesCache.getFoiProcedures().containsKey(foiID)) {
            return new ArrayList<String>();
        }
        return new ArrayList<String>(this.capabilitiesCache.getFoiProcedures().get(foiID));
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
        return this.capabilitiesCache.getUnit4Phen().get(observedProperty);
    }

    /**
     * returns the phens4CompPhens
     * 
     * @return HashMap<String, List<String>> the phens4CompPhens
     */
    public Map<String, Collection<String>> getPhens4CompPhens() {
        if (this.capabilitiesCache.getPhens4CompPhens() != null) {
            return new HashMap<String, Collection<String>>(this.capabilitiesCache.getPhens4CompPhens());
        }
        return new HashMap<String, Collection<String>>();
    }

    /**
     * Returns the offCompPhens
     * 
     * @return HashMap<String, List<String>> the offCompPhens
     */
    public Map<String, Collection<String>> getOffCompPhens() {
        if (this.capabilitiesCache.getOffCompPhens() != null) {
            return new HashMap<String, Collection<String>>(this.capabilitiesCache.getOffCompPhens());
        }
        return new HashMap<String, Collection<String>>();
    }

    /**
     * returns the procedures for phenomena
     * 
     * @return HashMap<String, List<String>> the procedures for phenomena
     */
    public Map<String, Collection<String>> getPhenProcs() {
        if (this.capabilitiesCache.getPhenProcs() != null) {
            return new HashMap<String, Collection<String>>(this.capabilitiesCache.getPhenProcs());
        }
        return new HashMap<String, Collection<String>>();
    }

    /**
     * returns the phenomena for procedures
     * 
     * @return HashMap<String, List<String>> the phenomena for procedures
     */
    public Map<String, Collection<String>> getProcPhens() {
        if (this.capabilitiesCache.getProcPhens() != null) {
            return new HashMap<String, Collection<String>>(this.capabilitiesCache.getProcPhens());
        }
        return new HashMap<String, Collection<String>>();
    }

    /**
     * returns the value type for the passed phenomenon
     * 
     * @param phenomenonID
     *            id of the phenomenon for which the value type should be
     *            returned
     * @return Returns the value type for the passed phenomenon
     */
    public ValueTypes getValueType4Phenomenon(String phenomenonID) {
        return this.capabilitiesCache.getObsPropsValueTypes().get(phenomenonID);
    }

    /**
     * returns the the times for offerings
     * 
     * @return Map<String, String[]> the times for offerings
     */
    public Map<String, String[]> getTimes4Offerings() {
        if (this.capabilitiesCache.getTimes4Offerings() != null) {
            return new HashMap<String, String[]>(this.capabilitiesCache.getTimes4Offerings());
        }
        return new HashMap<String, String[]>();
    }

    /**
     * Returns Srid of coordinates stored in SOS database
     * 
     * @return int Srid of coordinates stored in SOS database
     */
    public int getSrid() {
        return this.capabilitiesCache.getSrid();
    }

    /**
     * 
     * @return Returns Map containing offeringIDs as keys and list of
     *         corresponding features as values
     */
    public Map<String, Collection<String>> getOffFeatures() {
        if (this.capabilitiesCache.getOffFeatures() != null) {
            return new HashMap<String, Collection<String>>(this.capabilitiesCache.getOffFeatures());
        }
        return new HashMap<String, Collection<String>>();
    }

    /**
     * returns the offerings for the passed procedure id
     * 
     * @param procID
     *            id of procedure, for which related offerings should be
     *            returned
     * @return Returns offerings, for which passed procedure produces data
     */
    public List<String> getOfferings4Procedure(String procID) {
        return new ArrayList<String>(this.capabilitiesCache.getOfferings4Procedure(procID));
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
        return new ArrayList<String>(this.capabilitiesCache.getOfferings4Phenomenon(phenID));
    }

    /**
     * Returns srids, which are supported by this SOS
     * 
     * @return Returns srids, which are supported by this SOS
     */
    public Collection<Integer> getSrids() {
        if (this.capabilitiesCache.getSrids() != null) {
            return new ArrayList<Integer>(this.capabilitiesCache.getSrids());
        }
        return new ArrayList<Integer>();
    }

    /**
     * returns valuetypes for obsProps
     * 
     * @return HashMap<String, ValueTypes> valuetypes for obsProps
     */
    public Map<String, ValueTypes> getValueTypes4ObsProps() {
        if (this.capabilitiesCache.getValueTypes4ObsProps() != null) {
            return new HashMap<String, ValueTypes>(this.capabilitiesCache.getValueTypes4ObsProps());
        }
        return new HashMap<String, ValueTypes>();
    }

    /**
     * returns procedures for offerings
     * 
     * @return Map<String, List<String>> procedures for offerings
     */
    public Map<String, Collection<String>> getOffProcedures() {
        if (this.capabilitiesCache.getOffProcedures() != null) {
            return new HashMap<String, Collection<String>>(this.capabilitiesCache.getOffProcedures());
        }
        return new HashMap<String, Collection<String>>();
    }

    /**
     * returns related features for offerings
     * 
     * @return Map<String, List<String>> related features for offerings
     */
    public Map<String, Collection<String>> getOffRelatedFeatures() {
        if (this.capabilitiesCache.getKOfferingVRelatedFeatures() != null) {
            return new HashMap<String, Collection<String>>(this.capabilitiesCache.getKOfferingVRelatedFeatures());
        }
        return new HashMap<String, Collection<String>>();
    }

	/* (non-Javadoc)
	 * @see org.n52.sos.cache.ACapabilitiesCacheController#getObservablePropertiesForOffering(java.lang.String)
	 */
	@Override
	public Collection<String> getObservablePropertiesForOffering(String offering) {
		if (this.capabilitiesCache.getPhenomenons4Offering(offering) != null) {
			return new ArrayList<String>(this.capabilitiesCache.getPhenomenons4Offering(offering));
		}
		return new ArrayList<String>();
	}

	/* (non-Javadoc)
	 * @see org.n52.sos.cache.ACapabilitiesCacheController#getObservableProperties()
	 */
	@Override
	public Collection<String> getObservableProperties() {
		if (this.capabilitiesCache.getAllPhenomenons() != null) {
			return new ArrayList<String>(this.capabilitiesCache.getAllPhenomenons());
		}
		return new ArrayList<String>();
	}

	/* (non-Javadoc)
	 * @see org.n52.sos.cache.ACapabilitiesCacheController#getKOfferingsVObservableProperties()
	 */
	@Override
	public Map<String, Collection<String>> getKOfferingsVObservableProperties() {
        if (this.capabilitiesCache.getOffCompPhens() != null) {
            return new HashMap<String, Collection<String>>(this.capabilitiesCache.getOffCompPhens());
        }
        return new HashMap<String, Collection<String>>();
	}

	/* (non-Javadoc)
	 * @see org.n52.sos.cache.ACapabilitiesCacheController#getProcedures4FeatureOfInterest(java.lang.String)
	 */
	@Override
	public Collection<String> getProcedures4FeatureOfInterest(String foiID) {
		if (this.capabilitiesCache.getProc4FOI(foiID) != null) {
			return new ArrayList<String>(this.capabilitiesCache.getProc4FOI(foiID));
		}
		return new ArrayList<String>();
	}

	/* (non-Javadoc)
	 * @see org.n52.sos.cache.ACapabilitiesCacheController#getUnit4ObservableProperty(java.lang.String)
	 */
	@Override
	public String getUnit4ObservableProperty(String observedProperty) {
		return this.capabilitiesCache.getUnit4ObsProp(observedProperty);
	}

	/* (non-Javadoc)
	 * @see org.n52.sos.cache.ACapabilitiesCacheController#getFeatureOfInterest()
	 */
	@Override
	public Collection<String> getFeatureOfInterest() {
		if (this.capabilitiesCache.getFeatureOfInterest() != null) {
			return new ArrayList<String>(this.capabilitiesCache.getFeatureOfInterest());
		}
		return new ArrayList<String>();
	}
	
	/* (non-Javadoc)
	 * @see org.n52.sos.cache.ACapabilitiesCacheController#getAllFeature()
	 */
	@Override
	public Collection<String> getAllFeature() {
		if (this.capabilitiesCache.getAllFeature() != null) {
			return new ArrayList<String>(this.capabilitiesCache.getAllFeature());
		}
		return new ArrayList<String>();
	}

	/* (non-Javadoc)
	 * @see org.n52.sos.cache.ACapabilitiesCacheController#getObservableProperties4CompositePhenomenons()
	 */
	@Override
	public Map<String, Collection<String>> getObservableProperties4CompositePhenomenons() {
        if (this.capabilitiesCache.getPhens4CompPhens() != null) {
            return new HashMap<String, Collection<String>>(this.capabilitiesCache.getPhens4CompPhens());
        }
        return new HashMap<String, Collection<String>>();
	}

	/* (non-Javadoc)
	 * @see org.n52.sos.cache.ACapabilitiesCacheController#getKOfferingVCompositePhenomenons()
	 */
	@Override
	public Map<String, Collection<String>> getKOfferingVCompositePhenomenons() {
        if (this.capabilitiesCache.getOffCompPhens() != null) {
            return new HashMap<String, Collection<String>>(this.capabilitiesCache.getOffCompPhens());
        }
        return new HashMap<String, Collection<String>>();
	}

	/* (non-Javadoc)
	 * @see org.n52.sos.cache.ACapabilitiesCacheController#getKObservablePropertyVProcedures()
	 */
	@Override
	public Map<String, Collection<String>> getKObservablePropertyVProcedures() {
        if (this.capabilitiesCache.getPhenProcs() != null) {
            return new HashMap<String, Collection<String>>(this.capabilitiesCache.getPhenProcs());
        }
        return new HashMap<String, Collection<String>>();
	}

	/* (non-Javadoc)
	 * @see org.n52.sos.cache.ACapabilitiesCacheController#getKProcedureVObservableProperties()
	 */
	@Override
	public Map<String, Collection<String>> getKProcedureVObservableProperties() {
        if (this.capabilitiesCache.getProcPhens() != null) {
            return new HashMap<String, Collection<String>>(this.capabilitiesCache.getProcPhens());
        }
        return new HashMap<String, Collection<String>>();
	}

	/* (non-Javadoc)
	 * @see org.n52.sos.cache.ACapabilitiesCacheController#getValueType4ObservableProperty(java.lang.String)
	 */
	@Override
	public ValueTypes getValueType4ObservableProperty(String phenomenonID) {
		return this.capabilitiesCache.getValueType4Phenomenon(phenomenonID);
	}

	/* (non-Javadoc)
	 * @see org.n52.sos.cache.ACapabilitiesCacheController#getKOfferingVFeatures()
	 */
	@Override
	public Map<String, Collection<String>> getKOfferingVFeatures() {
        if (this.capabilitiesCache.getOffFeatures() != null) {
            return new HashMap<String, Collection<String>>(this.capabilitiesCache.getOffFeatures());
        }
        return new HashMap<String, Collection<String>>();
	}

	/* (non-Javadoc)
	 * @see org.n52.sos.cache.ACapabilitiesCacheController#getOfferings4ObservableProperty(java.lang.String)
	 */
	@Override
	public Collection<String> getOfferings4ObservableProperty(String phenID) {
		if (this.capabilitiesCache.getOfferings4Phenomenon(phenID) != null) {
			return new ArrayList<String>(this.capabilitiesCache.getOfferings4Phenomenon(phenID));
		}
		return new ArrayList<String>();
	}

	/* (non-Javadoc)
	 * @see org.n52.sos.cache.ACapabilitiesCacheController#getKObservablePropertyVValueType()
	 */
	@Override
	public Map<String, ValueTypes> getKObservablePropertyVValueType() {
        if (this.capabilitiesCache.getValueTypes4ObsProps() != null) {
            return new HashMap<String, ValueTypes>(this.capabilitiesCache.getValueTypes4ObsProps());
        }
        return new HashMap<String, ValueTypes>();
	}

	/* (non-Javadoc)
	 * @see org.n52.sos.cache.ACapabilitiesCacheController#getKOfferingVProcedures()
	 */
	@Override
	public Map<String, Collection<String>> getKOfferingVProcedures() {
        if (this.capabilitiesCache.getOffProcedures() != null) {
            return new HashMap<String, Collection<String>>(this.capabilitiesCache.getOffProcedures());
        }
        return new HashMap<String, Collection<String>>();
	}

	/* (non-Javadoc)
	 * @see org.n52.sos.cache.ACapabilitiesCacheController#getKOfferingVRelatedFeatures()
	 */
	@Override
	public Map<String, Collection<String>> getKOfferingVRelatedFeatures() {
        if (this.capabilitiesCache.getKOfferingVRelatedFeatures() != null) {
            return new HashMap<String, Collection<String>>(this.capabilitiesCache.getKOfferingVRelatedFeatures());
        }
        return new HashMap<String, Collection<String>>();
	}

	/* (non-Javadoc)
	 * @see org.n52.sos.cache.ACapabilitiesCacheController#getKCompositePhenomenonVObservableProperty()
	 */
	@Override
	public Map<String, Collection<String>> getKCompositePhenomenonVObservableProperty() {
        if (this.capabilitiesCache.getPhens4CompPhens() != null) {
            return new HashMap<String, Collection<String>>(this.capabilitiesCache.getPhens4CompPhens());
        }
        return new HashMap<String, Collection<String>>();
	}

	@Override
	public Map<String, Collection<String>> getKRelatedFeaturesVRole() {
        if (this.capabilitiesCache.getKRelatedFeatureVRole() != null) {
            return new HashMap<String, Collection<String>>(this.capabilitiesCache.getKRelatedFeatureVRole());
        }
        return new HashMap<String, Collection<String>>();
	}

}
