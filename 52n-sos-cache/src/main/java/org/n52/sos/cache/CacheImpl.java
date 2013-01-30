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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.util.SosHelper;

/**
 * This singleton class encapsulates HashMaps, which store relationships between
 * the different metadata components of this SOS (e.g. fois 4 offerings). The
 * intention is to achieve better performance in getting this information from
 * this cache than to query always the DB for this information. (Usually the
 * informations stored here do not often change)
 * 
 * TODO use one naming convention regarding getters and setters in this class
 * 
 */
public class CacheImpl implements CapabilitiesCache{

    /**
     * contains the supported ids of SRS, which are supported by this SOS
     * instance
     */
    private Collection<Integer> srids;

    /** contains the procedure IDs offered in the database */
    private Collection<String> procedures;

    /** contains the feature IDs offered in the database */
    private Collection<String> featureOfInterestIdentifiers;

    /** contains the observation IDs offered in the database */
    private Collection<String> observationIdentifiers;

    /** hash map containing the phenomenons for each offering */
    private Map<String, Collection<String>> kOfferingVObservableProperties;

    /** hash map containing the name for each offering */
    private Map<String, String> kOfferingVName;

    /** hash map containing the name for each offering */
    private Map<String, Collection<String>> kOfferingVObservationTypes;

    /** hash map containing the procedures for each offering */
    private Map<String, List<String>> kOfferingVProcedures;

    /** hash map containing the features of interest for each offering */
    private Map<String, Collection<String>> kOfferingVFeaturesOfInterest;

    /** hash map containing the procedures for each feature of interest */
    private Map<String, Collection<String>> kFeatureOfInterestVProcedures;

    /**
     * hash map containing the phenomenon components of each compositePhenomenon
     */
    private Map<String, Collection<String>> phens4CompPhens;

    /**
     * hash map containing the offering IDs as keys and the corresponding
     * composite phenomena ids as values
     */
    private Map<String, Collection<String>> kOfferingVCompositePhenomenon;

    /** hash map containing parent procedures for each procedure */
    private Map<String, Collection<String>> parentProcs;

    /** hash map containing child procedures for each procedure */
    private Map<String, Collection<String>> childProcs;

    /** hash map containing the corresponding phenomena for each procedure */
    private Map<String, Collection<String>> kProcedureVObservableProperties;

    /** hash map containing the offerings(values) for each procedure (key) */
    private Map<String, Collection<String>> kProcedureVOfferings;

    private Map<String, Collection<String>> parentFeatures;

    private Map<String, Collection<String>> childFeatures;

    /**
     * hash map containing the phenomenon IDs as keys and the corresponding
     * procedure ids as values
     */
    private Map<String, List<String>> kObservablePropertyVProcedures;

    /** map contains the offerings for each phenomenon */
    private Map<String, List<String>> kObservablePropertyVOfferings;

    /** contains the unit (value) for each phenomenon (key) */
    private Map<String, String> unit4Phen;

    /** EPSG code of coordinates contained in the database */
    private int srid;

    /** hash map containing the related features for each offering */
    private Map<String, Collection<String>> kOfferingVRelatedFeatures;

    /** hash map containing the roles for each related feature */
    private Map<String, Collection<String>> kRelatedFeatureVRole;

    private Map<String, Collection<String>> allowedKOfferingVObservationType;

    private Collection<String> observationTypes;
    
    private Collection<String> featureOfInterestTypes;

    private Collection<String> resultTemplates;
	
	private Map<String, SosEnvelope> kOfferingVEnvelope;
	
	// TODO merge next two maps
	private Map<String, DateTime> kOfferingVMinTime;
	private Map<String, DateTime> kOfferingVMaxTime;
	
	private SosEnvelope globalEnvelope;
	
	private TimePeriod globalTemporalBoundingBox;

	private Map<String, Collection<String>> kOfferingVObservationIdentifiers;

    public CacheImpl() {
    	allowedKOfferingVObservationType = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
    	childFeatures = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
    	childProcs = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
    	featureOfInterestIdentifiers = Collections.synchronizedList(new ArrayList<String>());
    	featureOfInterestTypes = Collections.synchronizedList(new ArrayList<String>());
    	globalEnvelope = new SosEnvelope(null, getSrid());
    	kFeatureOfInterestVProcedures = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
    	kObservablePropertyVProcedures = Collections.synchronizedMap(new HashMap<String, List<String>>());
    	kObservablePropertyVOfferings = Collections.synchronizedMap(new HashMap<String, List<String>>());
    	kOfferingVCompositePhenomenon = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
    	kOfferingVEnvelope = Collections.synchronizedMap(new HashMap<String, SosEnvelope>());
    	kOfferingVFeaturesOfInterest = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
    	kOfferingVMaxTime = Collections.synchronizedMap(new HashMap<String, DateTime>());
    	kOfferingVMinTime = Collections.synchronizedMap(new HashMap<String, DateTime>());
    	kOfferingVObservableProperties = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
    	kOfferingVObservationIdentifiers = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
    	kOfferingVObservationTypes = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
    	kOfferingVProcedures = Collections.synchronizedMap(new HashMap<String, List<String>>());
    	kOfferingVRelatedFeatures = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
    	kProcedureVObservableProperties = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
    	kProcedureVOfferings = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
    	kRelatedFeatureVRole = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
    	observationIdentifiers = Collections.synchronizedList(new ArrayList<String>());
    	observationTypes = Collections.synchronizedList(new ArrayList<String>());
    	kOfferingVName = Collections.synchronizedMap(new HashMap<String, String>());
    	parentFeatures = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
    	parentProcs = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
    	globalTemporalBoundingBox = new TimePeriod();
    	phens4CompPhens = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
    	procedures = Collections.synchronizedList(new ArrayList<String>());
    	resultTemplates = Collections.synchronizedList(new ArrayList<String>());
    	srid = -1;
    	srids = Collections.synchronizedList(new ArrayList<Integer>());
    	unit4Phen = Collections.synchronizedMap(new HashMap<String, String>());
    }

    protected Collection<String> getPhenomenons4Offering(String offering) {
        return kOfferingVObservableProperties.get(offering);
    }
    
    /**
     * returns relationships between offerings and phenomena
     * 
     * @return
     */
    protected Map<String, Collection<String>> getOffPhenomenons() {
        return kOfferingVObservableProperties;
    }

    /**
     * returns the observedProperties for each offering
     * 
     * @return Map<String, String[]> containing the offerings with its
     *         observedProperties
     * @deprecated use {@link #getOffPhenomenons()}
     */
    protected Map<String, Collection<String>> getObsPhenomenons() {
        return kOfferingVObservableProperties;
    }
    
    @Override
	public void setKOfferingVObservableProperties(Map<String, Collection<String>> offPhenomenons) {
        this.kOfferingVObservableProperties = offPhenomenons;
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
    protected Collection<String> getObservableProperties4Offering(String offering) {
        List<String> result = new ArrayList<String>();

        // single phenomena
        if (kOfferingVObservableProperties.containsKey(offering)) {

            result.addAll(kOfferingVObservableProperties.get(offering));

            // components of composite phenomena
            if (kOfferingVCompositePhenomenon.containsKey(offering)) {
                Collection<String> compPhens = kOfferingVCompositePhenomenon.get(offering);
                for (String cp : compPhens) {
                    if (phens4CompPhens.containsKey(cp)) {
                        result.addAll(phens4CompPhens.get(cp));
                    }
                }

            }
        }

        // only components of composite phenomena
        else {
            if (kOfferingVCompositePhenomenon.containsKey(offering)) {
                Collection<String> compPhens = kOfferingVCompositePhenomenon.get(offering);
                for (String cp : compPhens) {
                    if (phens4CompPhens.containsKey(cp)) {
                        result.addAll(phens4CompPhens.get(cp));
                    }
                }

            }
        }
        return result;
    }

    /**
     * returns relationships between names and offerings
     * 
     * @return
     */
    protected Map<String, String> getOffName() {
        return kOfferingVName;
    }
    
    @Override
	public void setKOfferingVName(Map<String, String> offName) {
        this.kOfferingVName = offName;
    }
    
    /**
     * returns the name of the requested offering
     * 
     * @param offering
     *            the offering for which the name should be returned
     * @return String containing the name of the offering
     */
    protected String getOfferingName(String offering) {
        return kOfferingVName.get(offering);
    }

    /**
     * returns the offerings of this SOS
     * 
     * @return List<String> containing the offerings of this SOS
     */
    protected Collection<String> getOfferings() {
        if (kOfferingVName != null) {
            return kOfferingVName.keySet();
        }
        return new ArrayList<String>();
    }

    /**
     * returns the observation ids of this SOS
     * 
     * @return List<String> containing the observation ids of this SOS
     */
    protected Collection<String> getObservationIdentifiers() {
        return observationIdentifiers;
    }
    
    @Override
	public void setObservationIdentifiers(Collection<String> observationIdentifiers) {
        this.observationIdentifiers = observationIdentifiers;
    }
    
    /**
     * returns relationships between offerings and result models
     * 
     * @return
     */
    protected Map<String, Collection<String>> getKOfferingVObservationTypes() {
        return kOfferingVObservationTypes;
    }
    
    @Override
	public void setKOfferingVObservationTypes(Map<String, Collection<String>> offferingObservationTypes) {
        this.kOfferingVObservationTypes = offferingObservationTypes;
    }

    /**
     * @deprecated use {@link #getKOfferingVProcedures()}
     */
    protected Map<String, List<String>> getOffProcedures() {
        return getKOfferingVProcedures();
    }
    
    @Override
	public void setKOfferingVProcedures(Map<String, List<String>> offProcedures) {
        this.kOfferingVProcedures = offProcedures;
    }
    
	protected Map<String, List<String>> getKOfferingVProcedures()
	{
		return kOfferingVProcedures;
	}
    
    /**
     * returns the procedures for the requested offering
     * 
     * @param offering
     *            the offering for which the procedures should be returned
     * @return String[] containing the procedures for the requested offering
     */
    protected Collection<String> getProceduresForOffering(String offering) {
        return kOfferingVProcedures.get(offering);
    }

    /**
     * returns the units for phenomena
     * 
     * @return the units related to phenomenon
     */
    protected Map<String, String> getUnit4Phen() {
        return unit4Phen;
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
    protected String getUnit4ObsProp(String observedProperty) {
        return unit4Phen.get(observedProperty);
    }

    /**
     * 
     * 
     * @return Returns ListString containing all procedures which are used by
     *         the Offerings offered in this SOS
     */
    protected Collection<String> getProcedures() {
        return procedures;
    }
    
    @Override
	public void setProcedures(Collection<String> procedures) {
        this.procedures = procedures;
    }

    /**
     * returns the procedureIdentifers for the feature of interest (station, for example)
     * 
     * @param foiIdentifier
     *            the foiIdentifier for which the procedureIdentifers should returned
     * @return Collection<String> representing the procedureIdentifers
     */
    protected Collection<String> getProceduresForFeature(String foiIdentifier) {
        return kFeatureOfInterestVProcedures.get(foiIdentifier);
    }

    /**
     * @return Map&lt;String, List&lt;String>> kFeatureOfInterestVProcedures
     * @deprecated use {@link #getKFeatureOfInterestVProcedures()}
     */
    protected Map<String, Collection<String>> getFoiProcedures() {
        return getKFeatureOfInterestVProcedures();
    }
    
    protected Map<String, Collection<String>> getKFeatureOfInterestVProcedures()
    {
    	return kFeatureOfInterestVProcedures;
    }
    
    @Override
	public void setKFeatureOfInterestVProcedures(Map<String, Collection<String>> foiProcedures) {
        this.kFeatureOfInterestVProcedures = foiProcedures;
    }

    /**
     * @return Returns only FOIs which are sampling features
     */
    protected Collection<String> getFeatureOfInterest() {
        return featureOfInterestIdentifiers;
    }

    @Override
	public void setFeatureOfInterest(Collection<String> featuresOfInterest) {
        this.featureOfInterestIdentifiers = featuresOfInterest;
    }
    
    @Override
	public void setAllFeatureOfInterests(Collection<String> featuresOfInterest) {
        this.featureOfInterestIdentifiers = featuresOfInterest;
    }

    /**
     * @return Returns the phens4CompPhens.
     */
    protected Map<String, Collection<String>> getPhens4CompPhens() {
        return phens4CompPhens;
    }
    
    @Override
	public void setKCompositePhenomenonVObservableProperties(Map<String, Collection<String>> phens4CompPhens) {
        this.phens4CompPhens = phens4CompPhens;
    }

    /**
     * @return Returns the offCompPhens.
     */
    protected Map<String, Collection<String>> getOffCompPhens() {
        return kOfferingVCompositePhenomenon;
    }
    
    @Override
	public void setKOfferingVCompositePhenomenon(Map<String, Collection<String>> offCompPhens) {
        this.kOfferingVCompositePhenomenon = offCompPhens;
    }

    /**
     * @deprecated use {@link #getKObservablePropertyVProcedures()}
     */
    protected Map<String, List<String>> getPhenProcs() {
        return kObservablePropertyVProcedures;
    }
    
    protected Map<String, List<String>> getKObservablePropertyVProcedures() {
        return kObservablePropertyVProcedures;
    }
    
    @Override
	public void setKObservablePropertyVProcedures(Map<String, List<String>> phenProcs) {
        this.kObservablePropertyVProcedures = phenProcs;
    }

    /**
     * returns parent procedure map
     * 
     * @return Returns the parentProcs.
     */
    protected Map<String, Collection<String>> getParentProcs() {
        return parentProcs;
    }

    /**
     * sets the parent procedure map
     * 
     * @param parentProcs
     *            The parentProcs map to set.
     */
    protected void setParentProcs(Map<String, Collection<String>> parentProcs) {
        this.parentProcs = parentProcs;
    }

    /**
     * returns child procedure map
     * 
     * @return Returns the childProcs.
     */
    protected Map<String, Collection<String>> getChildProcs() {
        return childProcs;
    }

    /**
     * sets the child procedure map
     * 
     * @param childProcs
     *            The childProcs map to set.
     */
    protected void setChildProcs(Map<String, Collection<String>> childProcs) {
        this.childProcs = childProcs;
    }

    @Override
	public void setProcedureHierarchies(Map<String, Collection<String>> parentProcs) {
        this.parentProcs = parentProcs;
        childProcs = SosHelper.invertHierarchy(parentProcs);
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
    protected Collection<String> getParentProcs(String procId, boolean fullHierarchy, boolean includeSelf) {
        return SosHelper.getHierarchy(parentProcs, procId, fullHierarchy, includeSelf);
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
    protected Collection<String> getParentProcs(Collection<String> procIds, boolean fullHierarchy,
            boolean includeSelves) {
        Collection<String> collectionParentProcs = new HashSet<String>();

        for (String procId : procIds) {
            collectionParentProcs.addAll(SosHelper.getHierarchy(parentProcs, procId, fullHierarchy, includeSelves));
        }

        List<String> cppList = new ArrayList<String>(collectionParentProcs);
        Collections.sort(cppList);
        return cppList;
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
    protected Collection<String> getChildProcs(String procId, boolean fullHierarchy, boolean includeSelf) {
        return SosHelper.getHierarchy(childProcs, procId, fullHierarchy, includeSelf);
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
    protected Collection<String> getChildProcs(Collection<String> procIds, boolean fullHierarchy, boolean includeSelves) {
        Collection<String> collectionChildProcs = new HashSet<String>();

        for (String procId : procIds) {
            collectionChildProcs.addAll(SosHelper.getHierarchy(childProcs, procId, fullHierarchy, includeSelves));
        }

        List<String> ccpList = new ArrayList<String>(collectionChildProcs);
        Collections.sort(ccpList);
        return ccpList;
    }

    @Override
	public void setSrids(Collection<Integer> srids) {
        this.srids = srids;
    }
    
    /**
     * Returns srids, which are supported by this SOS
     * 
     * @return Returns srids, which are supported by this SOS
     */
    protected Collection<Integer> getSrids() {
        return this.srids;
    }

    protected Map<String, Collection<String>> getParentFeatures() {
        return parentFeatures;
    }

    protected void setParentFeatures(Map<String, Collection<String>> parentFeatures) {
        this.parentFeatures = parentFeatures;
    }

    protected Map<String, Collection<String>> getChildFeatures() {
        return childFeatures;
    }

    protected void setChildFeatures(Map<String, Collection<String>> childFeatures) {
        this.childFeatures = childFeatures;
    }

    @Override
	public void setFeatureHierarchies(Map<String, Collection<String>> parentFeatures) {
        this.parentFeatures = parentFeatures;
        this.childFeatures = SosHelper.invertHierarchy(parentFeatures);
    }

    @Override
	public void setKOfferingVFeatures(Map<String, Collection<String>> offFeatures) {
        this.kOfferingVFeaturesOfInterest = offFeatures;
    }
    
    /**
     * 
     * @return Returns Map containing offeringIDs as keys and list of
     *         corresponding features as values
     */
    protected Map<String, Collection<String>> getOffFeatures() {
        return kOfferingVFeaturesOfInterest;
    }

    @Override
	public void setKProcedureVOfferings(Map<String, Collection<String>> procOffs) {
        this.kProcedureVOfferings = procOffs;
    }
    
    @Override
	public Map<String, Collection<String>> getKProcedureVOffering() {
    	return kProcedureVOfferings;
    }
    
    /**
     * returns the offerings for the passed procedure id
     * 
     * @param procID
     *            id of procedure, for which related offerings should be
     *            returned
     * @return Returns offerings, for which passed procedure produces data
     */
    protected Collection<String> getOfferings4Procedure(String procID) {
        List<String> result = new ArrayList<String>();
        if (kProcedureVOfferings.containsKey(procID)) {
            result.addAll(kProcedureVOfferings.get(procID));
        }
        return result;
    }

    @Override
	public void setUnit4ObservableProperty(Map<String, String> unit4Phen) {
        this.unit4Phen = unit4Phen;
    }

    @Override
	public void setKObservablePropertyVOfferings(Map<String, List<String>> phenOffs) {
        this.kObservablePropertyVOfferings = phenOffs;
    }
    
    @Override
	public Map<String, List<String>> getKObservablePropertyVOffering()
    {
    	return kObservablePropertyVOfferings;
    }
    
    /**
     * Returns the phenomenons of all offerings
     * 
     * @return List<String> containing the phenomenons of all offerings
     */
    protected Collection<String> getObservableProperties() {
        Set<String> observableProperties = new HashSet<String>(0);
        if (kObservablePropertyVOfferings != null && kObservablePropertyVOfferings.keySet() != null) {
            observableProperties.addAll(kObservablePropertyVOfferings.keySet());
        }
        return observableProperties;
    }
    
    /**
     * returns the offerings for the passed phenomenon
     * 
     * @param phenID
     *            id of procedure, for which related offerings should be
     *            returned
     * @return Returns offerings, to which passed phenomenon belongs to
     */
    protected Collection<String> getOfferings4Phenomenon(String phenID) {
        List<String> result = new ArrayList<String>();
        if (kObservablePropertyVOfferings.containsKey(phenID)) {
            result.addAll(kObservablePropertyVOfferings.get(phenID));
        }
        return result;
    }

    /**
     * @deprecated use {@link #getKProcedureVObservableProperties()}
     */
    protected Map<String, Collection<String>> getProcPhens() {
        return kProcedureVObservableProperties;
    }
    
    /**
     * returns the procedure phenomenon relations
     * 
     * @return Returns the procPhens.
     */
    protected Map<String, Collection<String>> getKProcedureVObservableProperties() {
    	return kProcedureVObservableProperties;
    }

    @Override
	public void setProcPhens(Map<String, Collection<String>> procPhens) {
        this.kProcedureVObservableProperties = procPhens;
    }

    /**
     * returns the SRID
     * 
     * @return Returns Srid of coordinates stored in SOS database
     */
    protected int getSrid() {
        return srid;
    }

    /**
     * methods for adding relationships in Cache for recently received new
     * observation
     * 
     * @param observation
     *            recently received observation which has been inserted into SOS
     *            db and whose relationships have to be maintained in cache
     */
    protected void refreshMetadata4newObservation(SosObservation observation) {

        // create local variables for better readable code
        String procID = observation.getObservationConstellation().getProcedure().getProcedureIdentifier();
        ArrayList<String> procs = new ArrayList<String>(1);
        procs.add(procID);

        String foiID = observation.getObservationConstellation().getFeatureOfInterest().getIdentifier().getValue();
        ArrayList<String> features = new ArrayList<String>(1);
        features.add(foiID);

        // if foi id is NOT contained add foi
        if (!this.featureOfInterestIdentifiers.contains(foiID)) {
            this.featureOfInterestIdentifiers.add(foiID);
        }

        // get offerings for phenomenon of observation
        Collection<String> offs =
                this.getOfferings4Phenomenon(observation.getObservationConstellation().getObservableProperty()
                        .getIdentifier());

        // insert foi_off relationsship for each offering
        for (String offering_id : offs) {

            // check whether offering foi relationship is already contained in
            // DB
            if (!this.getOffFeatures().containsKey(offering_id)) {

                // Case 1: offering is NOT contained in foi_off -> insert
                // relationsship
                this.getOffFeatures().put(offering_id, features);
            } else if (!this.getOffFeatures().get(offering_id).contains(foiID)) {

                // Case 2: offering is already stored in foi_off -> insert
                // relationsship if
                // offering NOT contains foi id
                this.getOffFeatures().get(offering_id).add(foiID);
            }

        }

        // insert proc_foi relationsship
        if (this.kFeatureOfInterestVProcedures.get(foiID) != null) {
            this.kFeatureOfInterestVProcedures.get(foiID).add(procID);
        } else {
            this.kFeatureOfInterestVProcedures.put(foiID, procs);
        }
    }

    @Override
	public void setKOfferingVRelatedFeatures(Map<String, Collection<String>> offRelatedFeatures) {
        this.kOfferingVRelatedFeatures = offRelatedFeatures;
    }

    /**
     * method to get the related features for offerings
     * 
     * @return the relatedFeatures Map with related features for offerings
     */
    protected Map<String, Collection<String>> getKOfferingVRelatedFeatures() {
        return kOfferingVRelatedFeatures;
    }

    protected Map<String, Collection<String>> getKRelatedFeatureVRole() {
        return kRelatedFeatureVRole;
    }

    @Override
	public void setKRelatedFeaturesVRole(Map<String, Collection<String>> kRelatedFeatureVRole) {
        this.kRelatedFeatureVRole = kRelatedFeatureVRole;
    }

    protected Collection<String> getParentFeatures(String featureID, boolean fullHierarchy, boolean includeSelf) {
        return SosHelper.getHierarchy(parentFeatures, featureID, fullHierarchy, includeSelf);
    }

    protected Collection<String> getParentFeatures(Collection<String> featureIDs, boolean fullHierarchy,
            boolean includeSelves) {
        Collection<String> collectionParentFeatures = new HashSet<String>();

        for (String featureID : featureIDs) {
            collectionParentFeatures.addAll(SosHelper.getHierarchy(parentFeatures, featureID, fullHierarchy,
                    includeSelves));
        }

        List<String> cppList = new ArrayList<String>(collectionParentFeatures);
        Collections.sort(cppList);
        return cppList;
    }

    protected Collection<String> getChildFeatures(String featureID, boolean fullHierarchy, boolean includeSelf) {
        return SosHelper.getHierarchy(childFeatures, featureID, fullHierarchy, includeSelf);
    }

    protected Collection<String> getChildPFeatures(Collection<String> featureIDs, boolean fullHierarchy,
            boolean includeSelves) {
        Collection<String> collectionChildFeatures = new HashSet<String>();

        for (String featureID : featureIDs) {
            collectionChildFeatures
                    .addAll(SosHelper.getHierarchy(childFeatures, featureID, fullHierarchy, includeSelves));
        }

        List<String> ccpList = new ArrayList<String>(collectionChildFeatures);
        Collections.sort(ccpList);
        return ccpList;
    }

    protected Map<String, Collection<String>> getAllowedKOfferingVObservationType() {
        return allowedKOfferingVObservationType;
    }

    @Override
	public void setAllowedKOfferingVObservationType(Map<String, Collection<String>> allowedKOfferingVObservationType) {
        this.allowedKOfferingVObservationType = allowedKOfferingVObservationType;
    }

    @Override
	public void setObservationTypes(Collection<String> observationTypes) {
        this.observationTypes = observationTypes;
    }

    @Override
	public Collection<String> getObservationTypes() {
        return observationTypes;
    }
    
    @Override
	public void setFeatureOfInterestTypes(Collection<String> featureOfInterestTypes) {
        this.featureOfInterestTypes = featureOfInterestTypes;
    }

    @Override
	public Collection<String> getFeatureOfInterestTypes() {
        return featureOfInterestTypes;
    }

    @Override
	public void setResultTemplates(Collection<String> resultTemplates) {
        this.resultTemplates = resultTemplates;
    }

    @Override
	public Collection<String> getResultTemplates() {
        return resultTemplates;
    }
	
	@Override
	public Map<String, SosEnvelope> getKOfferingVEnvelope() {
		return kOfferingVEnvelope;
	}
	
	@Override
	public void setKOfferingVEnvelope(Map<String, SosEnvelope> kOfferingVEnvelope) {
		this.kOfferingVEnvelope = kOfferingVEnvelope;
	}
	
	@Override
	public Map<String, DateTime> getKOfferingVMinTime() {
		return kOfferingVMinTime;
	}
	
	@Override
	public void setKOfferingVMinTime(Map<String, DateTime> kOfferingVMinTime) {
		this.kOfferingVMinTime = kOfferingVMinTime;
	}


	@Override
	public Map<String, DateTime> getKOfferingVMaxTime() {
		return kOfferingVMaxTime;
	}
	
	@Override
	public void setKOfferingVMaxTime(Map<String, DateTime> kOfferingVMaxTime) {
		this.kOfferingVMaxTime = kOfferingVMaxTime;
	}
			
	@Override
	public SosEnvelope getGlobalEnvelope() {
		return globalEnvelope;
	}

	@Override
	public void setGlobalEnvelope(SosEnvelope globalEnvelope) {
		this.globalEnvelope = globalEnvelope;
	}

	@Override
	public DateTime getMinEventTime() {
		return globalTemporalBoundingBox.getStart();
	}
	
	@Override
	public void setMinEventTime(DateTime minEventTime) {
		globalTemporalBoundingBox.setStart(minEventTime);
	}
	
	@Override
	public DateTime getMaxEventTime() {
		return globalTemporalBoundingBox.getEnd();
	}
	
	@Override
	public void setMaxEventTime(DateTime maxEventTime) {
		globalTemporalBoundingBox.setEnd(maxEventTime);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((kOfferingVProcedures == null) ? 0 : kOfferingVProcedures.hashCode());
		result = prime * result + ((allowedKOfferingVObservationType == null) ? 0 : allowedKOfferingVObservationType.hashCode());
		result = prime * result + ((childFeatures == null) ? 0 : childFeatures.hashCode());
		result = prime * result + ((childProcs == null) ? 0 : childProcs.hashCode());
		result = prime * result + ((globalEnvelope == null) ? 0 : globalEnvelope.hashCode());
		result = prime * result + ((featureOfInterestTypes == null) ? 0 : featureOfInterestTypes.hashCode());
		result = prime * result + ((featureOfInterestIdentifiers == null) ? 0 : featureOfInterestIdentifiers.hashCode());
		result = prime * result + ((kFeatureOfInterestVProcedures == null) ? 0 : kFeatureOfInterestVProcedures.hashCode());
		result = prime * result + ((kObservablePropertyVProcedures == null) ? 0 : kObservablePropertyVProcedures.hashCode());
		result = prime * result + ((kObservablePropertyVOfferings == null) ? 0 : kObservablePropertyVOfferings.hashCode());
		result = prime * result + ((kOfferingVCompositePhenomenon == null) ? 0 : kOfferingVCompositePhenomenon.hashCode());
		result = prime * result + ((kOfferingVEnvelope == null) ? 0 : kOfferingVEnvelope.hashCode());
		result = prime * result + ((kOfferingVFeaturesOfInterest == null) ? 0 : kOfferingVFeaturesOfInterest.hashCode());
		result = prime * result + ((kOfferingVMaxTime == null) ? 0 : kOfferingVMaxTime.hashCode());
		result = prime * result + ((kOfferingVMinTime == null) ? 0 : kOfferingVMinTime.hashCode());
		result = prime * result + ((kOfferingVObservableProperties == null) ? 0 : kOfferingVObservableProperties.hashCode());
		result = prime * result + ((kOfferingVRelatedFeatures == null) ? 0 : kOfferingVRelatedFeatures.hashCode());
		result = prime * result + ((kOfferingVObservationTypes == null) ? 0 : kOfferingVObservationTypes.hashCode());
		result = prime * result + ((kProcedureVObservableProperties == null) ? 0 : kProcedureVObservableProperties.hashCode());
		result = prime * result + ((kProcedureVOfferings == null) ? 0 : kProcedureVOfferings.hashCode());
		result = prime * result + ((kRelatedFeatureVRole == null) ? 0 : kRelatedFeatureVRole.hashCode());
		result = prime * result + ((globalTemporalBoundingBox == null) ? 0 : globalTemporalBoundingBox.hashCode());
		result = prime * result + ((observationIdentifiers == null) ? 0 : observationIdentifiers.hashCode());
		result = prime * result + ((observationTypes == null) ? 0 : observationTypes.hashCode());
		result = prime * result + ((kOfferingVName == null) ? 0 : kOfferingVName.hashCode());
		result = prime * result + ((parentFeatures == null) ? 0 : parentFeatures.hashCode());
		result = prime * result + ((parentProcs == null) ? 0 : parentProcs.hashCode());
		result = prime * result + ((phens4CompPhens == null) ? 0 : phens4CompPhens.hashCode());
		result = prime * result + ((procedures == null) ? 0 : procedures.hashCode());
		result = prime * result + ((resultTemplates == null) ? 0 : resultTemplates.hashCode());
		result = prime * result + srid;
		result = prime * result + ((srids == null) ? 0 : srids.hashCode());
		result = prime * result + ((unit4Phen == null) ? 0 : unit4Phen.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CacheImpl))
			return false;
		CacheImpl other = (CacheImpl) obj;
		if (kOfferingVProcedures == null) {
			if (other.kOfferingVProcedures != null)
				return false;
		} else if (!kOfferingVProcedures.equals(other.kOfferingVProcedures))
			return false;
		if (allowedKOfferingVObservationType == null) {
			if (other.allowedKOfferingVObservationType != null)
				return false;
		} else if (!allowedKOfferingVObservationType.equals(other.allowedKOfferingVObservationType))
			return false;
		if (childFeatures == null) {
			if (other.childFeatures != null)
				return false;
		} else if (!childFeatures.equals(other.childFeatures))
			return false;
		if (childProcs == null) {
			if (other.childProcs != null)
				return false;
		} else if (!childProcs.equals(other.childProcs))
			return false;
		if (globalEnvelope == null) {
			if (other.globalEnvelope != null)
				return false;
		} else if (!globalEnvelope.equals(other.globalEnvelope))
			return false;
		if (featureOfInterestTypes == null) {
			if (other.featureOfInterestTypes != null)
				return false;
		} else if (!featureOfInterestTypes.equals(other.featureOfInterestTypes))
			return false;
		if (featureOfInterestIdentifiers == null) {
			if (other.featureOfInterestIdentifiers != null)
				return false;
		} else if (!featureOfInterestIdentifiers.equals(other.featureOfInterestIdentifiers))
			return false;
		if (kFeatureOfInterestVProcedures == null) {
			if (other.kFeatureOfInterestVProcedures != null)
				return false;
		} else if (!kFeatureOfInterestVProcedures.equals(other.kFeatureOfInterestVProcedures))
			return false;
		if (kObservablePropertyVProcedures == null) {
			if (other.kObservablePropertyVProcedures != null)
				return false;
		} else if (!kObservablePropertyVProcedures.equals(other.kObservablePropertyVProcedures))
			return false;
		if (kObservablePropertyVOfferings == null) {
			if (other.kObservablePropertyVOfferings != null)
				return false;
		} else if (!kObservablePropertyVOfferings.equals(other.kObservablePropertyVOfferings))
			return false;
		if (kOfferingVCompositePhenomenon == null) {
			if (other.kOfferingVCompositePhenomenon != null)
				return false;
		} else if (!kOfferingVCompositePhenomenon.equals(other.kOfferingVCompositePhenomenon))
			return false;
		if (kOfferingVEnvelope == null) {
			if (other.kOfferingVEnvelope != null)
				return false;
		} else if (!kOfferingVEnvelope.equals(other.kOfferingVEnvelope))
			return false;
		if (kOfferingVFeaturesOfInterest == null) {
			if (other.kOfferingVFeaturesOfInterest != null)
				return false;
		} else if (!kOfferingVFeaturesOfInterest.equals(other.kOfferingVFeaturesOfInterest))
			return false;
		if (kOfferingVMaxTime == null) {
			if (other.kOfferingVMaxTime != null)
				return false;
		} else if (!kOfferingVMaxTime.equals(other.kOfferingVMaxTime))
			return false;
		if (kOfferingVMinTime == null) {
			if (other.kOfferingVMinTime != null)
				return false;
		} else if (!kOfferingVMinTime.equals(other.kOfferingVMinTime))
			return false;
		if (kOfferingVObservableProperties == null) {
			if (other.kOfferingVObservableProperties != null)
				return false;
		} else if (!kOfferingVObservableProperties.equals(other.kOfferingVObservableProperties))
			return false;
		if (kOfferingVRelatedFeatures == null) {
			if (other.kOfferingVRelatedFeatures != null)
				return false;
		} else if (!kOfferingVRelatedFeatures.equals(other.kOfferingVRelatedFeatures))
			return false;
		if (kOfferingVObservationTypes == null) {
			if (other.kOfferingVObservationTypes != null)
				return false;
		} else if (!kOfferingVObservationTypes.equals(other.kOfferingVObservationTypes))
			return false;
		if (kProcedureVObservableProperties == null) {
			if (other.kProcedureVObservableProperties != null)
				return false;
		} else if (!kProcedureVObservableProperties.equals(other.kProcedureVObservableProperties))
			return false;
		if (kProcedureVOfferings == null) {
			if (other.kProcedureVOfferings != null)
				return false;
		} else if (!kProcedureVOfferings.equals(other.kProcedureVOfferings))
			return false;
		if (kRelatedFeatureVRole == null) {
			if (other.kRelatedFeatureVRole != null)
				return false;
		} else if (!kRelatedFeatureVRole.equals(other.kRelatedFeatureVRole))
			return false;
		if (globalTemporalBoundingBox == null) {
			if (other.globalTemporalBoundingBox != null)
				return false;
		} else if (!globalTemporalBoundingBox.equals(other.globalTemporalBoundingBox))
			return false;
		if (observationIdentifiers == null) {
			if (other.observationIdentifiers != null)
				return false;
		} else if (!observationIdentifiers.equals(other.observationIdentifiers))
			return false;
		if (observationTypes == null) {
			if (other.observationTypes != null)
				return false;
		} else if (!observationTypes.equals(other.observationTypes))
			return false;
		if (kOfferingVName == null) {
			if (other.kOfferingVName != null)
				return false;
		} else if (!kOfferingVName.equals(other.kOfferingVName))
			return false;
		if (parentFeatures == null) {
			if (other.parentFeatures != null)
				return false;
		} else if (!parentFeatures.equals(other.parentFeatures))
			return false;
		if (parentProcs == null) {
			if (other.parentProcs != null)
				return false;
		} else if (!parentProcs.equals(other.parentProcs))
			return false;
		if (phens4CompPhens == null) {
			if (other.phens4CompPhens != null)
				return false;
		} else if (!phens4CompPhens.equals(other.phens4CompPhens))
			return false;
		if (procedures == null) {
			if (other.procedures != null)
				return false;
		} else if (!procedures.equals(other.procedures))
			return false;
		if (resultTemplates == null) {
			if (other.resultTemplates != null)
				return false;
		} else if (!resultTemplates.equals(other.resultTemplates))
			return false;
		if (srid != other.srid)
			return false;
		if (srids == null) {
			if (other.srids != null)
				return false;
		} else if (!srids.equals(other.srids))
			return false;
		if (unit4Phen == null) {
			if (other.unit4Phen != null)
				return false;
		} else if (!unit4Phen.equals(other.unit4Phen))
			return false;
		return true;
	}

	protected Map<String,Collection<String>> getKOfferingVObservationIdentifiers()
	{
		return kOfferingVObservationIdentifiers;
	}

}
