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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.util.SosHelper;

/**
 * This singleton class encapsulates HashMaps, which store relationships between
 * the different metadata components of this SOS (e.g. fois 4 offerings). The
 * intention is to achieve better performance in getting this information from
 * this cache than to query always the DB for this information. (Usually the
 * informations stored here do not often change)
 * 
 */
public class CapabilitiesCache extends ACapabilitiesCache {

    /**
     * contains the supported ids of SRS, which are supported by this SOS
     * instance
     */
    private Collection<Integer> srids;

    /** contains the procedure IDs offered in the database */
    private Collection<String> procedures;

    /** contains the feature IDs offered in the database */
    private Collection<String> featuresOfInterest;

    /** contains the observation IDs offered in the database */
    private Collection<String> obsIds;

    /** hash map containing the phenomenons for each offering */
    private Map<String, Collection<String>> offPhenomenons;

    /** hash map containing the name for each offering */
    private Map<String, String> offName;

    /** hash map containing the name for each offering */
    private Map<String, Collection<String>> kOffferingVObservationTypes;

    /** hash map containing the procedures for each offering */
    private Map<String, Collection<String>> offProcedures;

    /** hash map containing the features of interest for each offering */
    private Map<String, Collection<String>> offFeatures;

    /** hash map containing the procedures for each feature of interest */
    private Map<String, Collection<String>> foiProcedures;

    /**
     * hash map containing the phenomenon components of each compositePhenomenon
     */
    private Map<String, Collection<String>> phens4CompPhens;

    /**
     * hash map containing the offering IDs as keys and the corresponding
     * composite phenomena ids as values
     */
    private Map<String, Collection<String>> offCompPhens;

    /** hash map containing parent procedures for each procedure */
    private Map<String, Collection<String>> parentProcs;

    /** hash map containing child procedures for each procedure */
    private Map<String, Collection<String>> childProcs;

    /** hash map containing the corresponding phenomena for each procedure */
    private Map<String, Collection<String>> procPhens;

    /** hash map containing the offerings(values) for each procedure (key) */
    private Map<String, Collection<String>> procOffs;

    private Map<String, Collection<String>> parentFeatures;

    private Map<String, Collection<String>> childFeatures;

    /**
     * hash map containing the phenomenon IDs as keys and the corresponding
     * procedure ids as values
     */
    private Map<String, Collection<String>> phenProcs;

    /** map contains the offerings for each phenomenon */
    private Map<String, Collection<String>> phenOffs;

    private Map<String, String[]> times4Offerings;

    /** contains the unit (value) for each phenomenon (key) */
    private Map<String, String> unit4Phen;

    /** EPSG code of coordinates contained in the database */
    private int srid;

    /** hash map containing the related features for each offering */
    private Map<String, Collection<String>> offRelatedFeatures;

    /** hash map containing the roles for each related feature */
    private Map<String, Collection<String>> kRelatedFeatureVRole;

    private Map<String, Collection<String>> allowedKOfferingVObservationType;

    private Collection<String> observationTypes;

    /**
     * constructor
     * 
     * @throws OwsExceptionReport
     */
    public CapabilitiesCache() {
        super();
    }

    /**
     * Returns the observedProperties (phenomenons) for the requested offering
     * 
     * @param offering
     *            the offering for which observedProperties should be returned
     * @return Returns String[] containing the phenomenons of the requested
     *         offering
     */
    protected Collection<String> getPhenomenons4Offering(String offering) {
        return this.offPhenomenons.get(offering);
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
    protected Collection<String> getAllPhenomenons4Offering(String offering) {
        List<String> result = new ArrayList<String>();

        // single phenomena
        if (this.offPhenomenons.containsKey(offering)) {

            result.addAll(this.offPhenomenons.get(offering));

            // components of composite phenomena
            if (this.offCompPhens.containsKey(offering)) {
                Collection<String> compPhens = this.offCompPhens.get(offering);
                for (String cp : compPhens) {
                    if (this.phens4CompPhens.containsKey(cp)) {
                        result.addAll(this.phens4CompPhens.get(cp));
                    }
                }

            }
        }

        // only components of composite phenomena
        else {
            if (this.offCompPhens.containsKey(offering)) {
                Collection<String> compPhens = this.offCompPhens.get(offering);
                for (String cp : compPhens) {
                    if (this.phens4CompPhens.containsKey(cp)) {
                        result.addAll(this.phens4CompPhens.get(cp));
                    }
                }

            }
        }
        return result;
    }

    /**
     * Returns the phenomenons of all offerings
     * 
     * @return List<String> containing the phenomenons of all offerings
     */
    protected Collection<String> getAllPhenomenons() {
        List<String> phenomenons = new ArrayList<String>();
        for (String s : this.offName.keySet()) {

            // get single phenomena
            if (this.offPhenomenons.containsKey(s)) {
                Collection<String> phen = this.offPhenomenons.get(s);
                for (String p : phen) {
                    if (!phenomenons.contains(p)) {
                        phenomenons.add(p);
                    }
                }
            }

            // // get composite phenomena
            // if (this.offCompPhens.containsKey(s)) {
            // Collection<String> phen = this.offCompPhens.get(s);
            // for (String p : phen) {
            //
            // // add id of the composite phenomenon to the result
            // if (!phenomenons.contains(p)) {
            // phenomenons.add(p);
            // }
            //
            // // add components of composite phenomenon to the result
            // if (phens4CompPhens.containsKey(p)) {
            //
            // Collection<String> components = phens4CompPhens.get(p);
            // for (String phenComp : components) {
            // if (!phenomenons.contains(phenComp)) {
            // phenomenons.add(phenComp);
            // }
            // }
            //
            // }
            // }
            // }
        }
        return phenomenons;
    }

    /**
     * returns the offerings of this SOS
     * 
     * @return List<String> containing the offerings of this SOS
     */
    protected Collection<String> getOfferings() {
        if (this.offName != null) {
            return this.offName.keySet();
        }
        return new ArrayList<String>();
    }

    /**
     * returns the observation ids of this SOS
     * 
     * @return List<String> containing the observation ids of this SOS
     */
    protected Collection<String> getObservationIds() {
        return this.obsIds;
    }

    /**
     * returns the observation ids of this SOS
     * 
     * @return List<String> containing the observation ids of this SOS
     */
    protected Collection<String> getObsIds() {
        return obsIds;
    }

    /**
     * returns relationships between offerings and phenomena
     * 
     * @return
     */
    protected Map<String, Collection<String>> getOffPhenomenons() {
        return offPhenomenons;
    }

    /**
     * returns relationships between names and offerings
     * 
     * @return
     */
    protected Map<String, String> getOffName() {
        return offName;
    }

    /**
     * returns relationships between offerings and result models
     * 
     * @return
     */
    protected Map<String, Collection<String>> getKOfferingVObservationTypes() {
        return kOffferingVObservationTypes;
    }

    /**
     * returns relationships between offerings and procedures
     * 
     * @return
     */
    protected Map<String, Collection<String>> getOffProcedures() {
        return offProcedures;
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
     * returns the observedProperties for each offering
     * 
     * @return Map<String, String[]> containing the offerings with its
     *         observedProperties
     */
    protected Map<String, Collection<String>> getObsPhenomenons() {
        return offPhenomenons;
    }

    /**
     * returns the name of the requested offering
     * 
     * @param offering
     *            the offering for which the name should be returned
     * @return String containing the name of the offering
     */
    protected String getOfferingName(String offering) {
        return this.offName.get(offering);
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

    /**
     * returns the procedures for the requested offering
     * 
     * @param offering
     *            the offering for which the procedures should be returned
     * @return String[] containing the procedures for the requested offering
     */
    protected Collection<String> getProcedures4Offering(String offering) {
        return this.offProcedures.get(offering);
    }

    /**
     * returns the procedureID for the feature of interest (station)
     * 
     * @param foiID
     *            the foiID for which the procedureID should returned
     * @return String representing the procedureID
     */
    protected Collection<String> getProc4FOI(String foiID) {
        return this.foiProcedures.get(foiID);
    }

    /**
     * returns the foiProcedures
     * 
     * @return Map<String, List<String>> foiProcedures
     */
    protected Map<String, Collection<String>> getFoiProcedures() {
        return foiProcedures;
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
        return this.unit4Phen.get(observedProperty);
    }


    /**
     * @return Returns only FOIs which are sampling features
     */
    protected Collection<String> getFeatureOfInterest() {
        return featuresOfInterest;
    }
    
    public void setFeatureOfInterest(Collection<String> featuresOfInterest) {
        this.featuresOfInterest = featuresOfInterest;
    }

    /**
     * @return Returns the phens4CompPhens.
     */
    protected Map<String, Collection<String>> getPhens4CompPhens() {
        return phens4CompPhens;
    }

    /**
     * @return Returns the offCompPhens.
     */
    protected Map<String, Collection<String>> getOffCompPhens() {
        return offCompPhens;
    }

    /**
     * @return Returns the phenProcs.
     */
    protected Map<String, Collection<String>> getPhenProcs() {
        return phenProcs;
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

    /**
     * sets the parent procedure map, then inverts it and sets child procedure
     * map
     * 
     * @param parentProcs
     *            The parentProcs map to set.
     */
    public void setProcedureHierarchies(Map<String, Collection<String>> parentProcs) {
        this.parentProcs = parentProcs;
        this.childProcs = SosHelper.invertHierarchy(parentProcs);
    }

    /**
     * sets the observation ids
     * 
     * @param obsIds
     */
    public void setObsIds(Collection<String> obsIds) {
        this.obsIds = obsIds;
    }

    /**
     * sets the SRIDs
     * 
     * @param srids
     */
    public void setSrids(Collection<Integer> srids) {
        this.srids = srids;
    }

    protected Map<String, Collection<String>> getParentFeatures() {
        return parentFeatures;
    }

    protected void setParentFeatures(Map<String, Collection<String>> parentFeatures) {
        this.parentFeatures = parentFeatures;
    }

    protected Map<String, Collection<String>> getChildFeatures() {
        return childProcs;
    }

    protected void setChildFeatures(Map<String, Collection<String>> childFeatures) {
        this.childFeatures = childFeatures;
    }

    public void setFeatureHierarchies(Map<String, Collection<String>> parentFeatures) {
        this.parentFeatures = parentFeatures;
        this.childFeatures = SosHelper.invertHierarchy(parentFeatures);
    }

    /**
     * sets relationships between offerings and FOIs
     * 
     * @param offFeatures
     */
    public void setKOffrtingVFeatures(Map<String, Collection<String>> offFeatures) {
        this.offFeatures = offFeatures;
    }

    /**
     * sets relationships between offerings and phenomena
     * 
     * @param offPhenomenons
     */
    public void setKOfferingVObservableProperties(Map<String, Collection<String>> offPhenomenons) {
        this.offPhenomenons = offPhenomenons;
    }

    /**
     * sets relationships between names and offerings
     * 
     * @param offName
     */
    public void setKOfferingVName(Map<String, String> offName) {
        this.offName = offName;
    }

    /**
     * set procedures with SensorML
     * 
     * @param procedures
     */
    public void setProcedures(Collection<String> procedures) {
        this.procedures = procedures;
    }

    /**
     * sets relationships between offerings and result models
     * 
     * @param offResultModels
     */
    public void setKOfferingVObservationTypes(Map<String, Collection<String>> offferingObservationTypes) {
        this.kOffferingVObservationTypes = offferingObservationTypes;
    }

    /**
     * sets relationships between offerings and procedures
     * 
     * @param offProcedures
     */
    public void setKOfferingVProcedures(Map<String, Collection<String>> offProcedures) {
        this.offProcedures = offProcedures;
    }

    /**
     * sets relationships between procedures and offerings
     * 
     * @param procOffs
     */
    public void setKProcedureVOfferings(Map<String, Collection<String>> procOffs) {
        this.procOffs = procOffs;
    }

    /**
     * sets FOIs
     * 
     * @param fois
     */
    public void setAllFeatureOfInterests(Collection<String> featuresOfInterest) {
        this.featuresOfInterest = featuresOfInterest;
    }

    /**
     * sets the feature of interest procedure relations
     * 
     * @param foiProcedures
     */
    public void setKFeatureOfInterestVProcedures(Map<String, Collection<String>> foiProcedures) {
        this.foiProcedures = foiProcedures;
    }

    /**
     * sets relationships between phenomena and composite phenomena
     * 
     * @param phens4CompPhens
     */
    public void setKCompositePhenomenonVObservableProperties(Map<String, Collection<String>> phens4CompPhens) {
        this.phens4CompPhens = phens4CompPhens;
    }

    /**
     * sets relationships between offerings and composite phenomena
     * 
     * @param offCompPhens
     */
    public void setKOfferingVCompositePhenomenon(Map<String, Collection<String>> offCompPhens) {
        this.offCompPhens = offCompPhens;
    }

    /**
     * sets the unit phenomenon relations
     * 
     * @param unit4Phen
     */
    public void setUnit4ObservableProperty(Map<String, String> unit4Phen) {
        this.unit4Phen = unit4Phen;
    }

    /**
     * sets relationships between phenomena and offerings
     * 
     * @param phenOffs
     */
    public void setKObservablePropertyVOfferings(Map<String, Collection<String>> phenOffs) {
        this.phenOffs = phenOffs;
    }

    /**
     * sets phenomenon procedure relations
     * 
     * @param phenProcs
     *            The phenProcs to set.
     */
    public void setKObservablePropertyKProcedures(Map<String, Collection<String>> phenProcs) {
        this.phenProcs = phenProcs;
    }

    /**
     * returns the procedure phenomenon relations
     * 
     * @return Returns the procPhens.
     */
    protected Map<String, Collection<String>> getProcPhens() {
        return procPhens;
    }

    /**
     * sets the procedure phenomenon relations
     * 
     * @param procPhens
     *            The procPhens to set.
     */
    public void setProcPhens(Map<String, Collection<String>> procPhens) {
        this.procPhens = procPhens;
    }

    /**
     * returns the time offering relations
     * 
     * @return the times4Offerings
     */
    protected Map<String, String[]> getTimes4Offerings() {
        return times4Offerings;
    }

    /**
     * sets the time offering relations
     * 
     * @param times4Offerings
     *            the times4Offerings to set
     */
    public void setTimes4Offerings(Map<String, String[]> times4Offerings) {
        this.times4Offerings = times4Offerings;
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
     * 
     * @return Returns Map containing offeringIDs as keys and list of
     *         corresponding features as values
     */
    protected Map<String, Collection<String>> getOffFeatures() {
        return offFeatures;
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
        if (this.procOffs.containsKey(procID)) {
            result.addAll(this.procOffs.get(procID));
        }
        return result;
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
        if (this.phenOffs.containsKey(phenID)) {
            result.addAll(this.phenOffs.get(phenID));
        }
        return result;
    }

    /**
     * Returns srids, which are supported by this SOS
     * 
     * @return Returns srids, which are supported by this SOS
     */
    protected Collection<Integer> getSrids() {
        return this.srids;
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
        String procID = observation.getObservationConstellation().getProcedure();
        ArrayList<String> procs = new ArrayList<String>(1);
        procs.add(procID);

        String foiID = observation.getObservationConstellation().getFeatureOfInterest().getIdentifier();
        ArrayList<String> features = new ArrayList<String>(1);
        features.add(foiID);

        // if foi id is NOT contained add foi
        if (!this.featuresOfInterest.contains(foiID)) {
            this.featuresOfInterest.add(foiID);
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
        if (this.foiProcedures.get(foiID) != null) {
            this.foiProcedures.get(foiID).add(procID);
        } else {
            this.foiProcedures.put(foiID, procs);
        }
    }

    /**
     * method to set the related features for a offering
     * 
     * @param offRelatedFeatures
     *            the relatedFeatures to set
     */
    public void setKOfferingVRelatedFeatures(Map<String, Collection<String>> offRelatedFeatures) {
        this.offRelatedFeatures = offRelatedFeatures;
    }

    /**
     * method to get the related features for offerings
     * 
     * @return the relatedFeatures Map with related features for offerings
     */
    protected Map<String, Collection<String>> getKOfferingVRelatedFeatures() {
        return offRelatedFeatures;
    }

    protected Map<String, Collection<String>> getKRelatedFeatureVRole() {
        return kRelatedFeatureVRole;
    }

    /**
     * method to set the related features for a offering
     * 
     * @param offRelatedFeatures
     *            the relatedFeatures to set
     */
    public void setKRelatedFeaturesVRole(Map<String, Collection<String>> kRelatedFeatureVRole) {
        this.kRelatedFeatureVRole = kRelatedFeatureVRole;
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
    protected Collection<String> getParentProcs(Collection<String> procIds, boolean fullHierarchy, boolean includeSelves) {
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
                    .addAll(SosHelper.getHierarchy(childProcs, featureID, fullHierarchy, includeSelves));
        }

        List<String> ccpList = new ArrayList<String>(collectionChildFeatures);
        Collections.sort(ccpList);
        return ccpList;
    }

    protected Map<String, Collection<String>> getAllowedKOfferingVObservationType() {
        return allowedKOfferingVObservationType;
    }
    
    public void setAllowedKOfferingVObservationType(Map<String, Collection<String>> allowedKOfferingVObservationType) {
        this.allowedKOfferingVObservationType = allowedKOfferingVObservationType;
    }

    public void setObservationTypes(Collection<String> observationTypes) {
        this.observationTypes = observationTypes;
    }

    public Collection<String> getObservationTypes() {
        return observationTypes;
    }
}
