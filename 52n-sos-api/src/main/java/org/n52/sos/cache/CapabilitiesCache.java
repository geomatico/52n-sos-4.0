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
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.n52.sos.ogc.sos.SosEnvelope;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public interface CapabilitiesCache {

	/**
	 * sets relationships between offerings and phenomena
	 * 
	 * @param offPhenomenons
	 */
	public void setKOfferingVObservableProperties(Map<String, Collection<String>> offPhenomenons);

	/**
	 * sets relationships between names and offerings
	 * 
	 * @param offName
	 */
	public void setKOfferingVName(Map<String, String> offName);

	/**
	 * sets the observation ids
	 * 
	 * @param obsIds
	 */
	public void setObservationIdentifiers(Collection<String> observationIdentifiers);

	/**
	 * sets relationships between offerings and result models
	 * 
	 * @param offResultModels
	 */
	public void setKOfferingVObservationTypes(Map<String, Collection<String>> offferingObservationTypes);

	/**
	 * sets relationships between offerings and procedures
	 * 
	 * @param offProcedures
	 */
	public void setKOfferingVProcedures(Map<String, List<String>> offProcedures);

	/**
	 * set procedures with SensorML
	 * 
	 * @param procedures
	 */
	public void setProcedures(Collection<String> procedures);

	/**
	 * sets the feature of interest procedure relations
	 * 
	 * @param foiProcedures
	 */
	public void setKFeatureOfInterestVProcedures(Map<String, Collection<String>> foiProcedures);

	public void setFeatureOfInterest(Collection<String> featuresOfInterest);

	/**
	 * @param fois
	 * @deprecated use {@link #setFeatureOfInterest(Collection)}
	 */
	public void setAllFeatureOfInterests(Collection<String> featuresOfInterest);

	/**
	 * sets relationships between phenomena and composite phenomena
	 * 
	 * @param phens4CompPhens
	 */
	public void setKCompositePhenomenonVObservableProperties(Map<String, Collection<String>> phens4CompPhens);

	/**
	 * sets relationships between offerings and composite phenomena
	 * 
	 * @param offCompPhens
	 */
	public void setKOfferingVCompositePhenomenon(Map<String, Collection<String>> offCompPhens);

	/**
	 * sets phenomenon procedure relations
	 * 
	 * @param phenProcs
	 *            The phenProcs to set.
	 */
	public void setKObservablePropertyVProcedures(Map<String, List<String>> phenProcs);

	/**
	 * sets the parent procedure map, then inverts it and sets child procedure
	 * map
	 * 
	 * @param parentProcs
	 *            The parentProcs map to set.
	 */
	public void setProcedureHierarchies(Map<String, Collection<String>> parentProcs);

	/**
	 * sets the SRIDs
	 * 
	 * @param srids
	 */
	public void setSrids(Collection<Integer> srids);

	public void setFeatureHierarchies(Map<String, Collection<String>> parentFeatures);

	/**
	 * sets relationships between offerings and FOIs
	 * 
	 * @param offFeatures
	 */
	public void setKOfferingVFeatures(Map<String, Collection<String>> offFeatures);

	/**
	 * sets relationships between procedures and offerings
	 * 
	 * @param procOffs
	 */
	public void setKProcedureVOfferings(Map<String, Collection<String>> procOffs);

	public Map<String, Collection<String>> getKProcedureVOffering();

	/**
	 * sets relationships between phenomena and offerings
	 * 
	 * @param phenOffs
	 */
	public void setKObservablePropertyVOfferings(Map<String, List<String>> phenOffs);

	public Map<String, List<String>> getKObservablePropertyVOffering();

	/**
	 * sets the procedure phenomenon relations
	 * 
	 * @param procPhens
	 *            The procPhens to set.
	 */
	public void setProcPhens(Map<String, Collection<String>> procPhens);

	/**
	 * method to set the related features for a offering
	 * 
	 * @param offRelatedFeatures
	 *            the relatedFeatures to set
	 */
	public void setKOfferingVRelatedFeatures(Map<String, Collection<String>> offRelatedFeatures);

	/**
	 * method to set the related features for a offering
	 * 
	 * @param kOfferingVRelatedFeatures
	 *            the relatedFeatures to set
	 */
	public void setKRelatedFeaturesVRole(Map<String, Collection<String>> kRelatedFeatureVRole);

	public void setAllowedKOfferingVObservationType(Map<String, Collection<String>> allowedKOfferingVObservationType);

	public void setObservationTypes(Collection<String> observationTypes);

	public Collection<String> getObservationTypes();

	public void setFeatureOfInterestTypes(Collection<String> featureOfInterestTypes);

	public Collection<String> getFeatureOfInterestTypes();

	public void setResultTemplates(Collection<String> resultTemplates);

	public Collection<String> getResultTemplates();

	/**
	 * gets relationships between offerings and en
	 * 
	 * @return the envelopes of the offerings
	 *
	 */
	public Map<String, SosEnvelope> getKOfferingVEnvelope();

	/**
	 * sets relationships between offerings and envelopes
	 * 
	 * @param kOfferingVEnvelope
	 */
	public void setKOfferingVEnvelope(Map<String, SosEnvelope> kOfferingVEnvelope);

	/**
	 * gets relationships between offerings and their min time
	 * 
	 * @return the min times of the offerings
	 *
	 */
	public Map<String, DateTime> getKOfferingVMinTime();

	/**
	 * sets relationships between offerings and their min time
	 * 
	 * @param kOfferingVMinTime
	 */
	public void setKOfferingVMinTime(Map<String, DateTime> kOfferingVMinTime);

	/**
	 * gets relationships between offerings and their max time
	 * 
	 * @return the max times of the offerings
	 *
	 */
	public Map<String, DateTime> getKOfferingVMaxTime();

	/**
	 * sets relationships between offerings and their max time
	 * 
	 * @param kOfferingVMaxTime
	 */
	public void setKOfferingVMaxTime(Map<String, DateTime> kOfferingVMaxTime);

	public SosEnvelope getGlobalEnvelope();

	public void setGlobalEnvelope(SosEnvelope globalEnvelope);

	public DateTime getMinEventTime();

	public void setMinEventTime(DateTime minEventTime);

	public DateTime getMaxEventTime();

	public void setMaxEventTime(DateTime maxEventTime);
	
	public Map<String, Collection<String>> getKProcedureVObservationIdentifiers();

	public void setkOfferingVResultTemplates(Map<String, Collection<String>> kOfferingVResultTemplates);

	public Map<String, Collection<String>> getKOfferingVResultTemplates();

	public Collection<String> getProcedures();

	public Map<String, List<String>> getKObservablePropertyVProcedures();

	public Map<String, Collection<String>> getKProcedureVObservableProperties();

	public Collection<String> getObservationIdentifiers();

	/**
     * @return the EPSG code of coordinates stored in SOS database
     */
	public int getDatabaseEPSGCode();

	public Collection<String> getFeatureOfInterest();

	public Map<String, Collection<String>> getKFeatureOfInterestVProcedures();

	public Map<String, Collection<String>> getKOfferingVFeaturesOfInterest();

	public Map<String, Collection<String>> getKOfferingVRelatedFeatures();

	public Map<String, List<String>> getKOfferingVProcedures();

	public Map<String, Collection<String>> getKOfferingVObservableProperties();

	public Map<String, Collection<String>> getKOfferingVObservationTypes();

	public Map<String, Collection<String>> getChildFeatures();

	public Map<String, Collection<String>> getParentFeatures();

	public Map<String, Collection<String>> getAllowedKOfferingVObservationType();

	public Map<String, Collection<String>> getKRelatedFeatureVRole();
	
	public Map<String, String> getKOfferingVName();

}