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
	public abstract void setKOfferingVObservableProperties(Map<String, Collection<String>> offPhenomenons);

	/**
	 * sets relationships between names and offerings
	 * 
	 * @param offName
	 */
	public abstract void setKOfferingVName(Map<String, String> offName);

	/**
	 * sets the observation ids
	 * 
	 * @param obsIds
	 */
	public abstract void setObservationIdentifiers(Collection<String> observationIdentifiers);

	/**
	 * sets relationships between offerings and result models
	 * 
	 * @param offResultModels
	 */
	public abstract void setKOfferingVObservationTypes(Map<String, Collection<String>> offferingObservationTypes);

	/**
	 * sets relationships between offerings and procedures
	 * 
	 * @param offProcedures
	 */
	public abstract void setKOfferingVProcedures(Map<String, List<String>> offProcedures);

	/**
	 * set procedures with SensorML
	 * 
	 * @param procedures
	 */
	public abstract void setProcedures(Collection<String> procedures);

	/**
	 * sets the feature of interest procedure relations
	 * 
	 * @param foiProcedures
	 */
	public abstract void setKFeatureOfInterestVProcedures(Map<String, Collection<String>> foiProcedures);

	public abstract void setFeatureOfInterest(Collection<String> featuresOfInterest);

	/**
	 * sets FOIs
	 * 
	 * @param fois
	 * @deprecated use {@link #setFeatureOfInterest(Collection)}
	 */
	public abstract void setAllFeatureOfInterests(Collection<String> featuresOfInterest);

	/**
	 * sets relationships between phenomena and composite phenomena
	 * 
	 * @param phens4CompPhens
	 */
	public abstract void setKCompositePhenomenonVObservableProperties(Map<String, Collection<String>> phens4CompPhens);

	/**
	 * sets relationships between offerings and composite phenomena
	 * 
	 * @param offCompPhens
	 */
	public abstract void setKOfferingVCompositePhenomenon(Map<String, Collection<String>> offCompPhens);

	/**
	 * sets phenomenon procedure relations
	 * 
	 * @param phenProcs
	 *            The phenProcs to set.
	 */
	public abstract void setKObservablePropertyVProcedures(Map<String, List<String>> phenProcs);

	/**
	 * sets the parent procedure map, then inverts it and sets child procedure
	 * map
	 * 
	 * @param parentProcs
	 *            The parentProcs map to set.
	 */
	public abstract void setProcedureHierarchies(Map<String, Collection<String>> parentProcs);

	/**
	 * sets the SRIDs
	 * 
	 * @param srids
	 */
	public abstract void setSrids(Collection<Integer> srids);

	public abstract void setFeatureHierarchies(Map<String, Collection<String>> parentFeatures);

	/**
	 * sets relationships between offerings and FOIs
	 * 
	 * @param offFeatures
	 */
	public abstract void setKOfferingVFeatures(Map<String, Collection<String>> offFeatures);

	/**
	 * sets relationships between procedures and offerings
	 * 
	 * @param procOffs
	 */
	public abstract void setKProcedureVOfferings(Map<String, Collection<String>> procOffs);

	public abstract Map<String, Collection<String>> getKProcedureVOffering();

	/**
	 * sets relationships between phenomena and offerings
	 * 
	 * @param phenOffs
	 */
	public abstract void setKObservablePropertyVOfferings(Map<String, List<String>> phenOffs);

	public abstract Map<String, List<String>> getKObservablePropertyVOffering();

	/**
	 * sets the procedure phenomenon relations
	 * 
	 * @param procPhens
	 *            The procPhens to set.
	 */
	public abstract void setProcPhens(Map<String, Collection<String>> procPhens);

	/**
	 * method to set the related features for a offering
	 * 
	 * @param offRelatedFeatures
	 *            the relatedFeatures to set
	 */
	public abstract void setKOfferingVRelatedFeatures(Map<String, Collection<String>> offRelatedFeatures);

	/**
	 * method to set the related features for a offering
	 * 
	 * @param kOfferingVRelatedFeatures
	 *            the relatedFeatures to set
	 */
	public abstract void setKRelatedFeaturesVRole(Map<String, Collection<String>> kRelatedFeatureVRole);

	public abstract void setAllowedKOfferingVObservationType(Map<String, Collection<String>> allowedKOfferingVObservationType);

	public abstract void setObservationTypes(Collection<String> observationTypes);

	public abstract Collection<String> getObservationTypes();

	public abstract void setFeatureOfInterestTypes(Collection<String> featureOfInterestTypes);

	public abstract Collection<String> getFeatureOfInterestTypes();

	public abstract void setResultTemplates(Collection<String> resultTemplates);

	public abstract Collection<String> getResultTemplates();

	/**
	 * gets relationships between offerings and en
	 * 
	 * @return the envelopes of the offerings
	 *
	 */
	public abstract Map<String, SosEnvelope> getKOfferingVEnvelope();

	/**
	 * sets relationships between offerings and envelopes
	 * 
	 * @param kOfferingVEnvelope
	 */
	public abstract void setKOfferingVEnvelope(Map<String, SosEnvelope> kOfferingVEnvelope);

	/**
	 * gets relationships between offerings and their min time
	 * 
	 * @return the min times of the offerings
	 *
	 */
	public abstract Map<String, DateTime> getKOfferingVMinTime();

	/**
	 * sets relationships between offerings and their min time
	 * 
	 * @param kOfferingVMinTime
	 */
	public abstract void setKOfferingVMinTime(Map<String, DateTime> kOfferingVMinTime);

	/**
	 * gets relationships between offerings and their max time
	 * 
	 * @return the max times of the offerings
	 *
	 */
	public abstract Map<String, DateTime> getKOfferingVMaxTime();

	/**
	 * sets relationships between offerings and their max time
	 * 
	 * @param kOfferingVMaxTime
	 */
	public abstract void setKOfferingVMaxTime(Map<String, DateTime> kOfferingVMaxTime);

	public abstract SosEnvelope getGlobalEnvelope();

	public abstract void setGlobalEnvelope(SosEnvelope globalEnvelope);

	public abstract DateTime getMinEventTime();

	public abstract void setMinEventTime(DateTime minEventTime);

	public abstract DateTime getMaxEventTime();

	public abstract void setMaxEventTime(DateTime maxEventTime);
	
	public abstract Map<String, Collection<String>> getKProcedureVObservationIdentifiers();

	public abstract void setkOfferingVResultTemplates(Map<String, Collection<String>> kOfferingVResultTemplates);

	public abstract Map<String, Collection<String>> getKOfferingVResultTemplates();

}