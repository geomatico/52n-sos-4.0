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

import static org.n52.sos.cache.AbstractContentCache.notNullOrEmpty;
import static org.n52.sos.cache.AbstractContentCache.synchronizedSet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.SetMultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

/**
 * {@code WritableContentCache} that allows the updating of the underlying maps. All basic CRUD operations are
 * supported.
 *
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0.0
 */
public class WritableCache extends ReadableCache implements WritableContentCache {
    private static final Logger log = LoggerFactory.getLogger(WritableCache.class);
    private static final long serialVersionUID = 6625851272234063808L;

    /**
     * Creates a {@code TimePeriod} for the specified {@code ITime}.
     *
     * @param time the abstract time
     *
     * @return the period describing the abstract time
     */
    protected static TimePeriod toTimePeriod(ITime time) {
        if (time instanceof TimeInstant) {
            final DateTime instant = ((TimeInstant) time).getValue();
            return new TimePeriod(instant, instant);
        } else {
            return (TimePeriod) time;
        }
    }

    @Override
    public void removeResultTemplates(Collection<String> resultTemplates) {
        for (String resultTemplate : resultTemplates) {
            removeResultTemplate(resultTemplate);
        }
    }

    @Override
    public void addEpsgCode(Integer epsgCode) {
        greaterZero("epsgCode", epsgCode);
        log.trace("Adding EpsgCode {}", epsgCode);
        getEpsgCodesSet().add(epsgCode);
    }

    @Override
    public void addFeatureOfInterest(String featureOfInterest) {
        notNullOrEmpty("featureOfInterest", featureOfInterest);
        log.trace("Adding FeatureOfInterest {}", featureOfInterest);
        getFeaturesOfInterestSet().add(featureOfInterest);
    }

    @Override
    public void addObservationIdentifier(String observationIdentifier) {
        notNullOrEmpty("observationIdentifier", observationIdentifier);
        log.trace("Adding ObservationIdentifier {}", observationIdentifier);
        getObservationIdentifiersSet().add(observationIdentifier);
    }

    @Override
    public void addProcedure(String procedure) {
        notNullOrEmpty("procedure", procedure);
        log.trace("Adding procedure {}", procedure);
        getProceduresSet().add(procedure);
    }

    @Override
    public void addResultTemplate(String resultTemplate) {
        notNullOrEmpty("resultTemplate", resultTemplate);
        log.trace("Adding ResultTemplate {}", resultTemplate);
        getResultTemplatesSet().add(resultTemplate);
    }

    @Override
    public void addResultTemplates(Collection<String> resultTemplates) {
        noNullValues("resultTemplates", resultTemplates);
        for (String resultTemplate : resultTemplates) {
            addResultTemplate(resultTemplate);
        }
    }

    @Override
    public void addEpsgCodes(Collection<Integer> epsgCodes) {
        noNullValues("epsgCodes", epsgCodes);
        for (Integer epsgCode : epsgCodes) {
            addEpsgCode(epsgCode);
        }
    }

    @Override
    public void addFeaturesOfInterest(Collection<String> featuresOfInterest) {
        noNullValues("featuresOfInterest", featuresOfInterest);
        for (String featureOfInterest : featuresOfInterest) {
            addFeatureOfInterest(featureOfInterest);
        }
    }

    @Override
    public void addObservationIdentifiers(Collection<String> observationIdentifiers) {
        noNullValues("observationIdentifiers", observationIdentifiers);
        for (String observationIdentifier : observationIdentifiers) {
            addObservationIdentifier(observationIdentifier);
        }
    }

    @Override
    public void addProcedures(Collection<String> procedures) {
        noNullValues("procedures", procedures);
        for (String procedure : procedures) {
            addProcedure(procedure);
        }
    }

    @Override
    public void removeObservationIdentifier(String observationIdentifier) {
        notNullOrEmpty("observationIdentifier", observationIdentifier);
        log.trace("Removing ObservationIdentifier {}", observationIdentifier);
        getObservationIdentifiersSet().remove(observationIdentifier);
    }

    @Override
    public void removeObservationIdentifiers(Collection<String> observationIdentifiers) {
        noNullValues("observationIdentifiers", observationIdentifiers);
        for (String observationIdentifier : observationIdentifiers) {
            removeObservationIdentifier(observationIdentifier);
        }
    }

    @Override
    public void removeFeatureOfInterest(String featureOfInterest) {
        notNullOrEmpty("featureOfInterest", featureOfInterest);
        log.trace("Removing FeatureOfInterest {}", featureOfInterest);
        getFeaturesOfInterestSet().remove(featureOfInterest);
    }

    @Override
    public void removeFeaturesOfInterest(Collection<String> featuresOfInterest) {
        noNullValues("featuresOfInterest", featuresOfInterest);
        for (String featureOfInterest : featuresOfInterest) {
            removeFeatureOfInterest(featureOfInterest);
        }
    }

    @Override
    public void removeProcedure(String procedure) {
        notNullOrEmpty("procedure", procedure);
        log.trace("Removing Procedure {}", procedure);
        getProceduresSet().remove(procedure);
    }

    @Override
    public void removeProcedures(Collection<String> procedures) {
        noNullValues("procedures", procedures);
        for (String procedure : procedures) {
            removeProcedure(procedure);
        }
    }

    @Override
    public void removeResultTemplate(String resultTemplate) {
        notNullOrEmpty("resultTemplate", resultTemplate);
        log.trace("Removing ResultTemplate {}", resultTemplate);
        getResultTemplatesSet().remove(resultTemplate);
    }

    @Override
    public void setObservablePropertiesForCompositePhenomenon(String compositePhenomenon,
                                                              Collection<String> observableProperties) {
        final Set<String> newValue = synchronizedSet(observableProperties);
        log.trace("Setting ObservableProperties for CompositePhenomenon {} to {}", compositePhenomenon, newValue);
        getObservablePropertiesForCompositePhenomenonsMap().put(compositePhenomenon, newValue);
    }

    @Override
    public void setObservablePropertiesForOffering(String offering, Collection<String> observableProperties) {
        final Set<String> newValue = synchronizedSet(observableProperties);
        log.trace("Setting ObservableProperties for Offering {} to {}", offering, observableProperties);
        getObservablePropertiesForOfferingsMap().put(offering, newValue);
    }

    @Override
    public void setObservablePropertiesForProcedure(String procedure, Collection<String> observableProperties) {
        final Set<String> newValue = synchronizedSet(observableProperties);
        log.trace("Setting ObservableProperties for Procedure {} to {}", procedure, newValue);
        getObservablePropertiesForProceduresMap().put(procedure, newValue);
    }

    @Override
    public void setObservationTypesForOffering(String offering, Collection<String> observationTypes) {
        final Set<String> newValue = synchronizedSet(observationTypes);
        log.trace("Setting ObservationTypes for Offering {} to {}", offering, newValue);
        getObservationTypesForOfferingsMap().put(offering, newValue);
    }

    @Override
    public void setOfferingsForObservableProperty(String observableProperty, Collection<String> offerings) {
        final Set<String> newValue = synchronizedSet(offerings);
        log.trace("Setting Offerings for ObservableProperty {} to {}", observableProperty, newValue);
        getOfferingsForObservablePropertiesMap().put(observableProperty, newValue);
    }

    @Override
    public void setOfferingsForProcedure(String procedure, Collection<String> offerings) {
        final Set<String> newValue = synchronizedSet(offerings);
        log.trace("Setting Offerings for Procedure {} to {}", procedure, newValue);
        getOfferingsForProceduresMap().put(procedure, newValue);
    }

    @Override
    public void setProceduresForFeatureOfInterest(String featureOfInterest,
                                                  Collection<String> proceduresForFeatureOfInterest) {
        final Set<String> newValue = synchronizedSet(proceduresForFeatureOfInterest);
        log.trace("Setting Procedures for FeatureOfInterest {} to {}", featureOfInterest, newValue);
        getProceduresForFeaturesOfInterestMap().put(featureOfInterest, newValue);
    }

    @Override
    public void setProceduresForObservableProperty(String observableProperty, Collection<String> procedures) {
        final Set<String> newValue = synchronizedSet(procedures);
        log.trace("Setting Procedures for ObservablePropert {} to {}", observableProperty, procedures);
        getProceduresForObservablePropertiesMap().put(observableProperty, newValue);
    }

    @Override
    public void setProceduresForOffering(String offering, Collection<String> procedures) {
        final Set<String> newValue = synchronizedSet(procedures);
        log.trace("Setting Procedures for Offering {} to {}", offering, newValue);
        getProceduresForOfferingsMap().put(offering, newValue);
    }

    @Override
    public void setRelatedFeaturesForOffering(String offering, Collection<String> relatedFeatures) {
        final Set<String> newValue = synchronizedSet(relatedFeatures);
        log.trace("Setting Related Features for Offering {} to {}", offering, newValue);
        getRelatedFeaturesForOfferingsMap().put(offering, newValue);
    }

    @Override
    public void setResultTemplatesForOffering(String offering, Collection<String> resultTemplates) {
        final Set<String> newValue = synchronizedSet(resultTemplates);
        log.trace("Setting ResultTemplates for Offering {} to {}", offering, newValue);
        getResultTemplatesForOfferingsMap().put(offering, newValue);
    }

    @Override
    public void setRolesForRelatedFeature(String relatedFeature, Collection<String> roles) {
        final Set<String> newValue = synchronizedSet(roles);
        log.trace("Setting Roles for RelatedFeature {} to {}", relatedFeature, newValue);
        getRolesForRelatedFeaturesMap().put(relatedFeature, newValue);
    }

    @Override
    public void setFeaturesOfInterest(Collection<String> featuresOfInterest) {
        log.trace("Clearing FeaturesOfInterest");
        getFeaturesOfInterestSet().clear();
        addFeaturesOfInterest(featuresOfInterest);
    }

    @Override
    public void setPhenomenonTime(DateTime minEventTime, DateTime maxEventTime) {
        setMinPhenomenonTime(minEventTime);
        setMaxPhenomenonTime(maxEventTime);
    }

    @Override
    public void setObservationIdentifiers(Collection<String> observationIdentifiers) {
        log.trace("Clearing ObservationIdentifiers");
        getObservationIdentifiersSet().clear();
        addObservationIdentifiers(observationIdentifiers);
    }

    @Override
    public void setProcedures(Collection<String> procedures) {
        log.trace("Clearing Procedures");
        getProceduresSet().clear();
        addProcedures(procedures);
    }

    @Override
    public void setMaxPhenomenonTimeForOffering(String offering, DateTime maxTime) {
        notNullOrEmpty("offering", offering);
        notNull("maxTime", maxTime);
        log.trace("Setting maximal EventTime for Offering {} to {}", offering, maxTime);
        getMaxPhenomenonTimeForOfferingsMap().put(offering, maxTime);
    }

    @Override
    public void setMinPhenomenonTimeForOffering(String offering, DateTime minTime) {
        notNull("minTime", minTime);
        log.trace("Setting minimal EventTime for Offering {} to {}", offering, minTime);
        getMinPhenomenonTimeForOfferingsMap().put(offering, minTime);
    }

    @Override
    public void setNameForOffering(String offering, String name) {
        notNullOrEmpty("offering", offering);
        notNullOrEmpty("name", name);
        log.trace("Setting Name of Offering {} to {}", offering, name);
        getNameForOfferingsMap().put(offering, name);

    }

    @Override
    public void setEnvelopeForOffering(String offering, SosEnvelope envelope) {
        log.trace("Setting Envelope for Offering {} to {}", offering, envelope);
        getEnvelopeForOfferingsMap().put(offering, copyOf(envelope));
    }

    @Override
    public Set<String> getFeaturesOfInterestWithOffering() {
        return CollectionHelper.unionOfListOfLists(getFeaturesOfInterestForOfferingMap().values());
    }

    @Override
    public void addAllowedObservationTypeForOffering(String offering, String allowedObservationType) {
        notNullOrEmpty("offering", offering);
        notNullOrEmpty("allowedObservationType", allowedObservationType);
        log.trace("Adding AllowedObservationType {} to Offering {}", allowedObservationType, offering);
        getAllowedObservationTypesForOfferingsMap().add(offering, allowedObservationType);
    }

    @Override
    public void addAllowedObservationTypesForOffering(String offering, Collection<String> allowedObservationTypes) {
        notNullOrEmpty("offering", offering);
        noNullValues("allowedObservationTypes", allowedObservationTypes);
        log.trace("Adding AllowedObservationTypes {} to Offering {}", allowedObservationTypes, offering);
        getAllowedObservationTypesForOfferingsMap().addAll(offering, allowedObservationTypes);
    }

    @Override
    public void addCompositePhenomenonForOffering(String offering, String compositePhenomenon) {
        notNullOrEmpty("offering", offering);
        notNullOrEmpty("compositePhenomenon", compositePhenomenon);
        log.trace("Adding compositePhenomenon {} to Offering {}", compositePhenomenon, offering);
        getCompositePhenomenonsForOfferingsMap().add(offering, compositePhenomenon);
    }

    @Override
    public void addFeatureOfInterestForOffering(String offering, String featureOfInterest) {
        notNullOrEmpty("offering", offering);
        notNullOrEmpty("featureOfInterest", featureOfInterest);
        log.trace("Adding featureOfInterest {} to Offering {}", featureOfInterest, offering);
        getFeaturesOfInterestForOfferingMap().add(offering, featureOfInterest);
    }

    @Override
    public void addFeatureOfInterestForResultTemplate(String resultTemplate, String featureOfInterest) {
        notNullOrEmpty("resultTemplate", resultTemplate);
        notNullOrEmpty("featureOfInterest", featureOfInterest);
        log.trace("Adding FeatureOfInterest {} to ResultTemplate {}", featureOfInterest, resultTemplate);
        getFeaturesOfInterestForResultTemplatesMap().add(resultTemplate, featureOfInterest);
    }

    @Override
    public void addFeaturesOfInterestForResultTemplate(String resultTemplate, Collection<String> featuresOfInterest) {
        notNullOrEmpty("resultTemplate", resultTemplate);
        noNullValues("featuresOfInterest", featuresOfInterest);
        log.trace("Adding FeatureOfInterest {} to ResultTemplate {}", featuresOfInterest, resultTemplate);
        getFeaturesOfInterestForResultTemplatesMap().addAll(resultTemplate, featuresOfInterest);
    }

    @Override
    public void addObservablePropertyForCompositePhenomenon(String compositePhenomenon, String observableProperty) {
        notNullOrEmpty("compositePhenomenon", compositePhenomenon);
        notNullOrEmpty("observableProperty", observableProperty);
        log.trace("Adding ObservableProperty {} to CompositePhenomenon {}", observableProperty, compositePhenomenon);
        getObservablePropertiesForCompositePhenomenonsMap().add(compositePhenomenon, observableProperty);
    }

    @Override
    public void addObservablePropertyForOffering(String offering, String observableProperty) {
        notNullOrEmpty("offering", offering);
        notNullOrEmpty("observableProperty", observableProperty);
        log.trace("Adding observableProperty {} to offering {}", observableProperty, offering);
        getObservablePropertiesForOfferingsMap().add(offering, observableProperty);
    }

    @Override
    public void addObservablePropertyForProcedure(String procedure, String observableProperty) {
        notNullOrEmpty("procedure", procedure);
        notNullOrEmpty("observableProperty", observableProperty);
        log.trace("Adding observableProperty {} to procedure {}", observableProperty, procedure);
        getObservablePropertiesForProceduresMap().add(procedure, observableProperty);
    }

    @Override
    public void addObservablePropertyForResultTemplate(String resultTemplate, String observableProperty) {
        notNullOrEmpty("resultTemplate", resultTemplate);
        notNullOrEmpty("observableProperty", observableProperty);
        log.trace("Adding observableProperty {} to resultTemplate {}", observableProperty, resultTemplate);
        getObservablePropertiesForResultTemplatesMap().add(resultTemplate, observableProperty);
    }

    @Override
    public void addObservationIdentifierForProcedure(String procedure, String observationIdentifier) {
        notNullOrEmpty("procedure", procedure);
        notNullOrEmpty("observableProperty", observationIdentifier);
        log.trace("Adding observationIdentifier {} to procedure {}", observationIdentifier, procedure);
        getObservationIdentifiersForProceduresMap().add(procedure, observationIdentifier);
    }

    @Override
    public void addObservationTypesForOffering(String offering, String observationType) {
        notNullOrEmpty("offering", offering);
        notNullOrEmpty("observableProperty", observationType);
        log.trace("Adding observationType {} to offering {}", observationType, offering);
        getObservationTypesForOfferingsMap().add(offering, observationType);
    }

    @Override
    public void addOfferingForObservableProperty(String observableProperty, String offering) {
        notNullOrEmpty("observableProperty", observableProperty);
        notNullOrEmpty("offering", offering);
        log.trace("Adding offering {} to observableProperty {}", offering, observableProperty);
        getOfferingsForObservablePropertiesMap().add(observableProperty, offering);
    }

    @Override
    public void addOfferingForProcedure(String procedure, String offering) {
        notNullOrEmpty("procedure", procedure);
        notNullOrEmpty("offering", offering);
        log.trace("Adding offering {} to procedure {}", offering, procedure);
        getOfferingsForProceduresMap().add(procedure, offering);
    }

    @Override
    public void addProcedureForFeatureOfInterest(String featureOfInterest, String procedure) {
        notNullOrEmpty("featureOfInterest", featureOfInterest);
        notNullOrEmpty("procedure", procedure);
        log.trace("Adding procedure {} to featureOfInterest {}", procedure, featureOfInterest);
        getProceduresForFeaturesOfInterestMap().add(featureOfInterest, procedure);
    }

    @Override
    public void addProcedureForObservableProperty(String observableProperty, String procedure) {
        notNullOrEmpty("featureOfInterest", observableProperty);
        notNullOrEmpty("procedure", procedure);
        log.trace("Adding procedure {} to observableProperty {}", procedure, observableProperty);
        getProceduresForObservablePropertiesMap().add(observableProperty, procedure);
    }

    @Override
    public void addProcedureForOffering(String offering, String procedure) {
        notNullOrEmpty("offering", offering);
        notNullOrEmpty("procedure", procedure);
        log.trace("Adding procedure {} to offering {}", procedure, offering);
        getProceduresForOfferingsMap().add(offering, procedure);
    }

    @Override
    public void addRelatedFeatureForOffering(String offering, String relatedFeature) {
        notNullOrEmpty("offering", offering);
        notNullOrEmpty("relatedFeature", relatedFeature);
        log.trace("Adding relatedFeature {} to offering {}", relatedFeature, offering);
        getRelatedFeaturesForOfferingsMap().add(offering, relatedFeature);
    }

    @Override
    public void addRelatedFeaturesForOffering(String offering, Collection<String> relatedFeature) {
        notNullOrEmpty("offering", offering);
        noNullValues("relatedFeature", relatedFeature);
        log.trace("Adding relatedFeatures {} to offering {}", relatedFeature, offering);
        getRelatedFeaturesForOfferingsMap().addAll(offering, relatedFeature);
    }

    @Override
    public void addResultTemplateForOffering(String offering, String resultTemplate) {
        notNullOrEmpty("offering", offering);
        notNullOrEmpty("resultTemplate", resultTemplate);
        log.trace("Adding resultTemplate {} to offering {}", resultTemplate, offering);
        getResultTemplatesForOfferingsMap().add(offering, resultTemplate);
    }

    @Override
    public void addRoleForRelatedFeature(String relatedFeature, String role) {
        notNullOrEmpty("relatedFeature", relatedFeature);
        notNullOrEmpty("role", role);
        log.trace("Adding role {} to relatedFeature {}", role, relatedFeature);
        getRolesForRelatedFeaturesMap().add(relatedFeature, role);
    }

    @Override
    public void removeAllowedObservationTypeForOffering(String offering, String allowedObservationType) {
        notNullOrEmpty("offering", offering);
        notNullOrEmpty("allowedObservationType", allowedObservationType);
        log.trace("Removing allowedObservationType {} from offering {}", allowedObservationType, offering);
        getAllowedObservationTypesForOfferingsMap().removeWithKey(offering, allowedObservationType);
    }

    @Override
    public void removeAllowedObservationTypesForOffering(String offering) {
        notNullOrEmpty("offering", offering);
        log.trace("Removing allowedObservationTypes for offering {}", offering);
        getAllowedObservationTypesForOfferingsMap().remove(offering);
    }

    @Override
    public void removeCompositePhenomenonForOffering(String offering, String compositePhenomenon) {
        notNullOrEmpty("offering", offering);
        notNullOrEmpty("compositePhenomenon", compositePhenomenon);
        log.trace("Removing compositePhenomenon {} from offering {}", compositePhenomenon, offering);
        getCompositePhenomenonsForOfferingsMap().removeWithKey(offering, compositePhenomenon);
    }

    @Override
    public void removeCompositePhenomenonsForOffering(String offering) {
        notNullOrEmpty("offering", offering);
        log.trace("Removing compositePhenomenons for offering {}", offering);
        getCompositePhenomenonsForOfferingsMap().remove(offering);
    }

    @Override
    public void removeEnvelopeForOffering(String offering) {
        notNullOrEmpty("offering", offering);
        log.trace("Removing envelope for offering {}", offering);
        getEnvelopeForOfferingsMap().remove(offering);
    }

    @Override
    public void removeEpsgCode(Integer epsgCode) {
        notNull("epsgCode", epsgCode);
        log.trace("Removing epsgCode {}", epsgCode);
        getEpsgCodesSet().remove(epsgCode);
    }

    @Override
    public void removeEpsgCodes(Collection<Integer> epsgCodes) {
        noNullValues("epsgCodes", epsgCodes);
        for (Integer code : epsgCodes) {
            removeEpsgCode(code);
        }
    }

    @Override
    public void removeFeatureOfInterestForOffering(String offering, String featureOfInterest) {
        notNullOrEmpty("offering", offering);
        notNullOrEmpty("featureOfInterest", featureOfInterest);
        log.trace("Removing featureOfInterest {} from offering {}", featureOfInterest, offering);
        getFeaturesOfInterestForOfferingMap().removeWithKey(offering, featureOfInterest);
    }

    @Override
    public void removeFeatureOfInterestForResultTemplate(String resultTemplate, String featureOfInterest) {
        notNullOrEmpty("resultTemplate", resultTemplate);
        notNullOrEmpty("featureOfInterest", featureOfInterest);
        log.trace("Removing featureOfInterest {} from resultTemplate {}", featureOfInterest, resultTemplate);
        getFeaturesOfInterestForResultTemplatesMap().removeWithKey(resultTemplate, featureOfInterest);
    }

    @Override
    public void removeFeaturesOfInterestForOffering(String offering) {
        notNullOrEmpty("offering", offering);
        log.trace("Removing featuresOfInterest for offering {}", offering);
        getFeaturesOfInterestForOfferingMap().remove(offering);
    }

    @Override
    public void removeFeaturesOfInterestForResultTemplate(String resultTemplate) {
        notNullOrEmpty("resultTemplate", resultTemplate);
        log.trace("Removing featuresOfInterest for resultTemplate {}", resultTemplate);
        getFeaturesOfInterestForResultTemplatesMap().remove(resultTemplate);
    }

    @Override
    public void removeMaxPhenomenonTimeForOffering(String offering) {
        notNullOrEmpty("offering", offering);
        log.trace("Removing maxEventTime for offering {}", offering);
        getMaxPhenomenonTimeForOfferingsMap().remove(offering);
    }

    @Override
    public void removeMinPhenomenonTimeForOffering(String offering) {
        notNullOrEmpty("offering", offering);
        log.trace("Removing minEventTime for offering {}", offering);
        getMinPhenomenonTimeForOfferingsMap().remove(offering);
    }

    @Override
    public void removeNameForOffering(String offering) {
        notNullOrEmpty("offering", offering);
        log.trace("Removing name for offering {}", offering);
        getNameForOfferingsMap().remove(offering);
    }

    @Override
    public void removeObservablePropertiesForCompositePhenomenon(String compositePhenomenon) {
        notNullOrEmpty("offering", compositePhenomenon);
        log.trace("Removing name observableProperties compositePhenomenon {}", compositePhenomenon);
        getObservablePropertiesForCompositePhenomenonsMap().remove(compositePhenomenon);
    }

    @Override
    public void removeObservablePropertiesForOffering(String offering) {
        notNullOrEmpty("offering", offering);
        log.trace("Removing observableProperties for offering {}", offering);
        getObservablePropertiesForOfferingsMap().remove(offering);
    }

    @Override
    public void removeObservablePropertiesForProcedure(String procedure) {
        notNullOrEmpty("procedure", procedure);
        log.trace("Removing observableProperties for procedure {}", procedure);
        getObservablePropertiesForProceduresMap().remove(procedure);
    }

    @Override
    public void removeObservablePropertiesForResultTemplate(String resultTemplate) {
        notNullOrEmpty("resultTemplate", resultTemplate);
        log.trace("Removing observableProperties for resultTemplate {}", resultTemplate);
        getObservablePropertiesForResultTemplatesMap().remove(resultTemplate);
    }

    @Override
    public void removeObservablePropertyForCompositePhenomenon(String compositePhenomenon, String observableProperty) {
        notNullOrEmpty("compositePhenomenon", compositePhenomenon);
        notNullOrEmpty("observableProperty", observableProperty);
        log.trace("Removing observableProperty {} from compositePhenomenon {}", observableProperty, compositePhenomenon);
        getObservablePropertiesForCompositePhenomenonsMap().removeWithKey(compositePhenomenon, observableProperty);
    }

    @Override
    public void removeObservablePropertyForOffering(String offering, String observableProperty) {
        notNullOrEmpty("offering", offering);
        notNullOrEmpty("observableProperty", observableProperty);
        log.trace("Removing observableProperty {} from offering {}", observableProperty, offering);
        getObservablePropertiesForOfferingsMap().removeWithKey(offering, observableProperty);
    }

    @Override
    public void removeObservablePropertyForProcedure(String procedure, String observableProperty) {
        notNullOrEmpty("procedure", procedure);
        notNullOrEmpty("observableProperty", observableProperty);
        log.trace("Removing observableProperty {} from procedure {}", observableProperty, procedure);
        getObservablePropertiesForProceduresMap().removeWithKey(procedure, observableProperty);
    }

    @Override
    public void removeObservablePropertyForResultTemplate(String resultTemplate, String observableProperty) {
        notNullOrEmpty("resultTemplate", resultTemplate);
        notNullOrEmpty("observableProperty", observableProperty);
        log.trace("Removing observableProperty {} from resultTemplate {}", observableProperty, resultTemplate);
        getObservablePropertiesForResultTemplatesMap().removeWithKey(resultTemplate, observableProperty);
    }

    @Override
    public void removeObservationIdentifierForProcedure(String procedure, String observationIdentifier) {
        notNullOrEmpty("procedure", procedure);
        notNullOrEmpty("observationIdentifier", observationIdentifier);
        log.trace("Removing observationIdentifier {} from procedure {}", observationIdentifier, procedure);
        getObservationIdentifiersForProceduresMap().removeWithKey(procedure, observationIdentifier);
    }

    @Override
    public void removeObservationIdentifiersForProcedure(String procedure) {
        notNullOrEmpty("procedure", procedure);
        log.trace("Removing observationIdentifiers for procedure {}", procedure);
        getObservationIdentifiersForProceduresMap().remove(procedure);
    }

    @Override
    public void removeObservationTypeForOffering(String offering, String observationType) {
        notNullOrEmpty("offering", offering);
        notNullOrEmpty("observationType", observationType);
        log.trace("Removing observationType {} from offering {}", observationType, offering);
        getObservationTypesForOfferingsMap().removeWithKey(offering, observationType);
    }

    @Override
    public void removeObservationTypesForOffering(String offering) {
        notNullOrEmpty("offering", offering);
        log.trace("Removing observationTypes for offering {}", offering);
        getObservationTypesForOfferingsMap().remove(offering);
    }

    @Override
    public void removeOfferingForObservableProperty(String observableProperty, String offering) {
        notNullOrEmpty("observableProperty", observableProperty);
        notNullOrEmpty("offering", offering);
        log.trace("Removing offering {} from observableProperty {}", offering, observableProperty);
        getOfferingsForObservablePropertiesMap().removeWithKey(observableProperty, offering);
    }

    @Override
    public void removeOfferingForProcedure(String procedure, String offering) {
        notNullOrEmpty("procedure", procedure);
        notNullOrEmpty("offering", offering);
        log.trace("Removing offering {} from procedure {}", offering, procedure);
        getOfferingsForProceduresMap().removeWithKey(procedure, offering);
    }

    @Override
    public void removeOfferingsForObservableProperty(String observableProperty) {
        notNullOrEmpty("observableProperty", observableProperty);
        log.trace("Removing offerings for observableProperty {}", observableProperty);
        getOfferingsForObservablePropertiesMap().remove(observableProperty);
    }

    @Override
    public void removeOfferingsForProcedure(String procedure) {
        notNullOrEmpty("procedure", procedure);
        log.trace("Removing offering for procedure {}", procedure);
        getOfferingsForProceduresMap().remove(procedure);
    }

    @Override
    public void removeProcedureForFeatureOfInterest(String featureOfInterest, String procedure) {
        notNullOrEmpty("featureOfInterest", featureOfInterest);
        notNullOrEmpty("procedure", procedure);
        log.trace("Removing procedure {} from featureOfInterest {}", procedure, featureOfInterest);
        getProceduresForFeaturesOfInterestMap().removeWithKey(featureOfInterest, procedure);
    }

    @Override
    public void removeProcedureForObservableProperty(String observableProperty, String procedure) {
        notNullOrEmpty("observableProperty", observableProperty);
        notNullOrEmpty("procedure", procedure);
        log.trace("Removing procedure {} from observableProperty {}", procedure, observableProperty);
        getProceduresForObservablePropertiesMap().removeWithKey(observableProperty, procedure);
    }

    @Override
    public void removeProcedureForOffering(String offering, String procedure) {
        notNullOrEmpty("offering", offering);
        notNullOrEmpty("procedure", procedure);
        log.trace("Removing procedure {} from offering {}", procedure, offering);
        getProceduresForOfferingsMap().removeWithKey(offering, procedure);
    }

    @Override
    public void removeProceduresForFeatureOfInterest(String featureOfInterest) {
        notNullOrEmpty("featureOfInterest", featureOfInterest);
        log.trace("Removing procedures for featureOfInterest {}", featureOfInterest);
        getProceduresForFeaturesOfInterestMap().remove(featureOfInterest);
    }

    @Override
    public void removeProceduresForObservableProperty(String observableProperty) {
        notNullOrEmpty("observableProperty", observableProperty);
        log.trace("Removing procedures for observableProperty {}", observableProperty);
        getProceduresForObservablePropertiesMap().remove(observableProperty);
    }

    @Override
    public void removeProceduresForOffering(String offering) {
        notNullOrEmpty("offering", offering);
        log.trace("Removing procedures for offering {}", offering);
        getProceduresForOfferingsMap().remove(offering);
    }

    @Override
    public void removeRelatedFeatureForOffering(String offering, String relatedFeature) {
        notNullOrEmpty("offering", offering);
        notNullOrEmpty("relatedFeature", relatedFeature);
        log.trace("Removing relatedFeature {} from offering {}", relatedFeature, offering);
        getRelatedFeaturesForOfferingsMap().removeWithKey(offering, relatedFeature);
    }

    @Override
    public void removeRelatedFeaturesForOffering(String offering) {
        notNullOrEmpty("offering", offering);
        log.trace("Removing RelatedFeatures for offering {}", offering);
        getRelatedFeaturesForOfferingsMap().remove(offering);
    }

    @Override
    public void removeResultTemplateForOffering(String offering, String resultTemplate) {
        notNullOrEmpty("offering", offering);
        notNullOrEmpty("resultTemplate", resultTemplate);
        log.trace("Removing resultTemplate {} from offering {}", resultTemplate, offering);
        getResultTemplatesForOfferingsMap().removeWithKey(offering, resultTemplate);
    }

    @Override
    public void removeResultTemplatesForOffering(String offering) {
        notNullOrEmpty("offering", offering);
        log.trace("Removing ResultTemplates for offering {}", offering);
        getResultTemplatesForOfferingsMap().remove(offering);
    }

    @Override
    public void removeRoleForRelatedFeature(String relatedFeature, String role) {
        notNullOrEmpty("relatedFeature", relatedFeature);
        notNullOrEmpty("role", role);
        log.trace("Removing role {} from relatedFeature {}", role, relatedFeature);
        getRolesForRelatedFeaturesMap().removeWithKey(relatedFeature, role);
    }

    @Override
    public void removeRolesForRelatedFeature(String relatedFeature) {
        notNullOrEmpty("relatedFeature", relatedFeature);
        log.trace("Removing roles for relatedFeature {}", relatedFeature);
        getRolesForRelatedFeaturesMap().remove(relatedFeature);
    }

    @Override
    public void removeRolesForRelatedFeatureNotIn(Collection<String> relatedFeatures) {
        notNull("relatedFeatures", relatedFeatures);
        Iterator<String> iter = getRolesForRelatedFeaturesMap().keySet().iterator();
        while (iter.hasNext()) {
            if (!relatedFeatures.contains(iter.next())) {
                iter.remove();
            }
        }
    }

    @Override
    public void setAllowedObservationTypeForOffering(String offering, Collection<String> allowedObservationType) {
        notNullOrEmpty("offering", offering);
        final Set<String> newValue = synchronizedSet(allowedObservationType);
        log.trace("Setting allowedObservationTypes for offering {} to {}", offering, newValue);
        getAllowedObservationTypesForOfferingsMap().put(offering, newValue);
    }

    @Override
    public void setCompositePhenomenonsForOffering(String offering, Collection<String> compositePhenomenons) {
        notNullOrEmpty("offering", offering);
        final Set<String> newValue = synchronizedSet(compositePhenomenons);
        log.trace("Setting compositePhenomenons for offering {} to {}", offering, newValue);
        getCompositePhenomenonsForOfferingsMap().put(offering, newValue);
    }

    @Override
    public void setFeaturesOfInterestForOffering(String offering, Collection<String> featureOfInterest) {
        notNullOrEmpty("offering", offering);
        final Set<String> newValue = synchronizedSet(featureOfInterest);
        log.trace("Setting featureOfInterest for offering {} to {}", offering, newValue);
        getFeaturesOfInterestForOfferingMap().put(offering, newValue);
    }

    @Override
    public void setGlobalEnvelope(SosEnvelope globalEnvelope) {
        log.trace("Global envelope now: '{}'", getGlobalSpatialEnvelope());
        if (globalEnvelope == null) {
            setGlobalSpatialEnvelope(new SosEnvelope(null, getDefaultEPSGCode()));
        } else {
            setGlobalSpatialEnvelope(globalEnvelope);
        }
        log.trace("Global envelope updated to '{}' with '{}'", getGlobalSpatialEnvelope(), globalEnvelope);
    }

    @Override
    public void setMaxPhenomenonTime(DateTime maxEventTime) {
        log.trace("Setting Maximal EventTime to {}", maxEventTime);
        getGlobalPhenomenonTimeEnvelope().setEnd(maxEventTime);
    }

    @Override
    public void setMinPhenomenonTime(DateTime minEventTime) {
        log.trace("Setting Minimal EventTime to {}", minEventTime);
        getGlobalPhenomenonTimeEnvelope().setStart(minEventTime);
    }

    @Override
    public void setObservablePropertiesForResultTemplate(String resultTemplate, Collection<String> observableProperties) {
        notNullOrEmpty("resultTemplate", resultTemplate);
        final Set<String> newValue = synchronizedSet(observableProperties);
        log.trace("Setting observableProperties for resultTemplate {} to {}", resultTemplate, newValue);
        getObservablePropertiesForResultTemplatesMap().put(resultTemplate, newValue);
    }

    @Override
    public void setObservationIdentifiersForProcedure(String procedure, Collection<String> observationIdentifiers) {
        notNullOrEmpty("procedure", procedure);
        final Set<String> newValue = synchronizedSet(observationIdentifiers);
        log.trace("Setting observationIdentifiers for procedure {} to {}", procedure, newValue);
        getObservationIdentifiersForProceduresMap().put(procedure, newValue);
    }

    @Override
    public void setProcedureHierarchy(String procedure, Collection<String> parentProcedures) {
        notNullOrEmpty("procedure", procedure);
        noNullOrEmptyValues("parentProcedures", parentProcedures);
        log.trace("Setting parentProcedures for procedure {} to {}", procedure, parentProcedures);
        updateHierarchy(procedure, parentProcedures,
                        getParentProceduresForProceduresMap(),
                        getChildProceduresForProceduresMap());
    }

    @Override
    public void setFeatureHierarchy(String featureOfInterest, Collection<String> parentFeatures) {
        notNullOrEmpty("featureOfInterest", featureOfInterest);
        noNullOrEmptyValues("parentFeatures", parentFeatures);
        log.trace("Setting parentFeatures for featureOfInterest {} to {}", featureOfInterest, parentFeatures);
        updateHierarchy(featureOfInterest, parentFeatures,
                        getParentFeaturesForFeaturesOfInterestMap(),
                        getChildFeaturesForFeaturesOfInterestMap());
    }

    /**
     * Updates the specified child/parent hierarchy for the specified parent with the specified childs.
     *
     * @param <T>       the parent/childs type
     * @param parent    the parent to update
     * @param newChilds the new childs
     * @param parents   the parents map
     * @param childs    the childs map
     */
    protected <T> void updateHierarchy(T parent, Collection<T> newChilds, SetMultiMap<T, T> parents, SetMultiMap<T, T> childs) {
        Set<T> newChildSet = synchronizedSet(newChilds);
        Set<T> currentParents = parents.put(parent, newChildSet);
        if (currentParents != null) {
            for (T currentParent : currentParents) {
                log.trace("Removing child {} from parent {}", parent, currentParent);
                childs.removeWithKey(currentParent, parent);
            }
        }
        for (T child : newChildSet) {
            log.trace("Adding parent {} to child {}", parent, child);
            childs.add(child, parent);
        }
    }

    @Override
    public void addParentFeature(String featureOfInterest, String parentFeature) {
        notNullOrEmpty("featureOfInterest", featureOfInterest);
        notNullOrEmpty("parentFeature", parentFeature);
        log.trace("Adding parentFeature {} to featureOfInterest {}", parentFeature, featureOfInterest);
        getParentFeaturesForFeaturesOfInterestMap().add(featureOfInterest, parentFeature);
        getChildFeaturesForFeaturesOfInterestMap().add(parentFeature, featureOfInterest);
    }

    @Override
    public void addParentFeatures(String featureOfInterest, Collection<String> parentFeatures) {
        notNullOrEmpty("featureOfInterest", featureOfInterest);
        noNullOrEmptyValues("parentFeatures", parentFeatures);
        log.trace("Adding parentFeature {} to featureOfInterest {}", parentFeatures, featureOfInterest);
        getParentFeaturesForFeaturesOfInterestMap().addAll(featureOfInterest, parentFeatures);
        for (String parentFeature : parentFeatures) {
            getChildFeaturesForFeaturesOfInterestMap().add(parentFeature, featureOfInterest);
        }
    }

    @Override
    public void addParentProcedure(String procedure, String parentProcedure) {
        notNullOrEmpty("procedure", procedure);
        notNullOrEmpty("parentProcedure", parentProcedure);
        log.trace("Adding parentProcedure {} to procedure {}", parentProcedure, procedure);
        getParentProceduresForProceduresMap().add(procedure, parentProcedure);
        getChildProceduresForProceduresMap().add(parentProcedure, procedure);
    }

    @Override
    public void addParentProcedures(String procedure, Collection<String> parentProcedures) {
        notNullOrEmpty("procedure", procedure);
        noNullOrEmptyValues("parentProcedures", parentProcedures);
        log.trace("Adding parentProcedures {} to procedure {}", parentProcedures, procedure);
        getParentProceduresForProceduresMap().addAll(procedure, parentProcedures);
        for (String parentProcedure : parentProcedures) {
            getChildProceduresForProceduresMap().add(parentProcedure, procedure);
        }
    }

    @Override
    public void updateEnvelopeForOffering(String offering, Envelope envelope) {
        notNullOrEmpty("offering", offering);
        notNull("envelope", envelope);
        if (hasEnvelopeForOffering(offering)) {
            final SosEnvelope offeringEnvelope = getEnvelopeForOfferingsMap().get(offering);
            log.trace("Expanding envelope {} for offering {} to include {}", offeringEnvelope, offering, envelope);
            offeringEnvelope.expandToInclude(envelope);
        } else {
            setEnvelopeForOffering(offering, new SosEnvelope(envelope, getDefaultEPSGCode()));
        }
    }

    @Override
    public void updatePhenomenonTime(ITime eventTime) {
        notNull("eventTime", eventTime);
        TimePeriod tp = toTimePeriod(eventTime);
        log.trace("Expanding global EventTime to include {}", tp);
        if (!hasMinPhenomenonTime() || getMinPhenomenonTime().isAfter(tp.getStart())) {
            setMinPhenomenonTime(tp.getStart());
        }
        if (!hasMaxPhenomenonTime() || getMaxPhenomenonTime().isBefore(tp.getEnd())) {
            setMaxPhenomenonTime(tp.getEnd());
        }
    }

    @Override
    public void updateGlobalEnvelope(Envelope envelope) {
        notNull("envelope", envelope);
        if (hasGlobalEnvelope()) {
            log.trace("Expanding envelope {} to include {}", getGlobalSpatialEnvelope(), envelope);
            getGlobalSpatialEnvelope().expandToInclude(envelope);
        } else {
            setGlobalEnvelope(new SosEnvelope(new Envelope(envelope), getDefaultEPSGCode()));
        }
    }

    @Override
    public void updatePhenomenonTimeForOffering(String offering, ITime eventTime) {
        notNullOrEmpty("offering", offering);
        notNull("eventTime", eventTime);
        TimePeriod tp = toTimePeriod(eventTime);
        log.trace("Expanding EventTime of offering {} to include {}", offering, tp);
        if (!hasMaxPhenomenonTimeForOffering(offering)
            || getMaxPhenomenonTimeForOffering(offering).isBefore(tp.getEnd())) {
            setMaxPhenomenonTimeForOffering(offering, tp.getEnd());
        }
        if (!hasMinPhenomenonTimeForOffering(offering)
            || getMinPhenomenonTimeForOffering(offering).isAfter(tp.getStart())) {
            setMinPhenomenonTimeForOffering(offering, tp.getStart());
        }
    }

    @Override
    public void recalculateGlobalEnvelope() {
        log.trace("Recalculating global spatial envelope based on offerings");
        SosEnvelope globalEnvelope = null;
        if (!getOfferings().isEmpty()) {
            for (String offering : getOfferings()) {
                SosEnvelope e = getEnvelopeForOffering(offering);
                if (e != null) {
                    if (globalEnvelope == null) {
                        if (e.isSetEnvelope()) {
                            globalEnvelope = new SosEnvelope(new Envelope(e.getEnvelope()), e.getSrid());
                            log.trace("First envelope '{}' used as starting point", globalEnvelope);
                        }
                    } else {
                        globalEnvelope.getEnvelope().expandToInclude(e.getEnvelope());
                        log.trace("Envelope expanded to include '{}' resulting in '{}'", e, globalEnvelope);
                    }
                }
            }
            if (globalEnvelope == null) {
                log.error("Global envelope could not be resetted");
            }
        } else {
            globalEnvelope = new SosEnvelope(null, getDefaultEPSGCode());
        }
        setGlobalEnvelope(globalEnvelope);
        log.trace("Spatial envelope finally set to '{}'", getGlobalEnvelope());
    }

    @Override
    public void recalculatePhenomenonTime() {
        log.trace("Recalculating global event time based on offerings");
        DateTime globalMax = null, globalMin = null;
        if (!getOfferings().isEmpty()) {
            for (String offering : getOfferings()) {
                if (hasMaxPhenomenonTimeForOffering(offering)) {
                    DateTime offeringMax = getMaxPhenomenonTimeForOffering(offering);
                    if (globalMax == null || offeringMax.isAfter(globalMax)) {
                        globalMax = offeringMax;
                    }
                }
                if (hasMinPhenomenonTimeForOffering(offering)) {
                    DateTime offeringMin = getMinPhenomenonTimeForOffering(offering);
                    if (globalMin == null || offeringMin.isBefore(globalMin)) {
                        globalMin = offeringMin;
                    }
                }
            }
            if (globalMin == null || globalMax == null) {
                log.error("Error in cache! Reset of global temporal bounding box failed. Max: '{}'); Min: '{}'",
                          globalMax, globalMin);
            }
        }
        setPhenomenonTime(globalMin, globalMax);
        log.trace("Global temporal bounding box reset done. Min: '{}'); Max: '{}'",
                  getMinPhenomenonTime(), getMaxPhenomenonTime());
    }

    @Override
    public void removeMaxResultTimeForOffering(String offering) {
        notNullOrEmpty("offering", offering);
        log.trace("Removing maxResultTime for offering {}", offering);
        getMaxResultTimeForOfferingsMap().remove(offering);
    }

    @Override
    public void removeMinResultTimeForOffering(String offering) {
        notNullOrEmpty("offering", offering);
        log.trace("Removing minResultTime for offering {}", offering);
        getMinResultTimeForOfferingsMap().remove(offering);
    }

    @Override
    public void setResultTime(DateTime min, DateTime max) {
        setMinResultTime(min);
        setMaxResultTime(max);
    }

    @Override
    public void updateResultTime(ITime resultTime) {
        if (resultTime == null) {
            return;
        }
        TimePeriod tp = toTimePeriod(resultTime);
        log.trace("Expanding global ResultTime to include {}", tp);
        if (!hasMinResultTime() || getMinResultTime().isAfter(tp.getStart())) {
            setMinResultTime(tp.getStart());
        }
        if (!hasMaxResultTime() || getMaxResultTime().isBefore(tp.getEnd())) {
            setMaxResultTime(tp.getEnd());
        }
    }

    @Override
    public void recalculateResultTime() {
        log.trace("Recalculating global result time based on offerings");
        DateTime globalMax = null, globalMin = null;
        if (!getOfferings().isEmpty()) {
            for (String offering : getOfferings()) {
                if (hasMaxResultTimeForOffering(offering)) {
                    DateTime offeringMax = getMaxResultTimeForOffering(offering);
                    if (globalMax == null || offeringMax.isAfter(globalMax)) {
                        globalMax = offeringMax;
                    }
                }
                if (hasMinResultTimeForOffering(offering)) {
                    DateTime offeringMin = getMinResultTimeForOffering(offering);
                    if (globalMin == null || offeringMin.isBefore(globalMin)) {
                        globalMin = offeringMin;
                    }
                }
            }
        }
        setResultTime(globalMin, globalMax);
        log.trace("Global result time bounding box reset done. Min: '{}'); Max: '{}'",
                  getMinResultTime(), getMaxResultTime());
    }

    @Override
    public void setMaxResultTime(DateTime maxResultTime) {
        log.trace("Setting Maximal ResultTime to {}", maxResultTime);
        getGlobalPhenomenonTimeEnvelope().setEnd(maxResultTime);
    }

    @Override
    public void setMaxResultTimeForOffering(String offering, DateTime maxTime) {
        notNullOrEmpty("offering", offering);
        notNull("maxTime", maxTime);
        log.trace("Setting maximal ResultTime for Offering {} to {}", offering, maxTime);
        getMaxResultTimeForOfferingsMap().put(offering, maxTime);
    }

    @Override
    public void setMinResultTime(DateTime minResultTime) {
        log.trace("Setting Minimal ResultTime to {}", minResultTime);
        getGlobalPhenomenonTimeEnvelope().setStart(minResultTime);
    }

    @Override
    public void setMinResultTimeForOffering(String offering, DateTime minTime) {
        notNullOrEmpty("offering", offering);
        notNull("minTime", minTime);
        log.trace("Setting minimal ResultTime for Offering {} to {}", offering, minTime);
        getMinResultTimeForOfferingsMap().put(offering, minTime);
    }

    @Override
    public void updateResultTimeForOffering(String offering, ITime resultTime) {
        notNullOrEmpty("offering", offering);
        if (resultTime == null) {
            return;
        }
        TimePeriod tp = toTimePeriod(resultTime);
        log.trace("Expanding EventTime of offering {} to include {}", offering, tp);
        if (!hasMaxResultTimeForOffering(offering)
            || getMaxResultTimeForOffering(offering).isBefore(tp.getEnd())) {
            setMaxResultTimeForOffering(offering, tp.getEnd());
        }
        if (!hasMinResultTimeForOffering(offering)
            || getMinResultTimeForOffering(offering).isAfter(tp.getStart())) {
            setMinResultTimeForOffering(offering, tp.getStart());
        }
    }
}
