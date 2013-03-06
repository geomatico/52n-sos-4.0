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

import static org.n52.sos.util.SosHelper.getHierarchy;

import java.util.Set;

import org.joda.time.DateTime;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.util.CollectionHelper;

/**
 * {@code ContentCache} implementation that offers a readable interface to the cache. All methods return unmodifiable
 * views of the cache.
 */
public class ReadableCache extends AbstractContentCache implements ContentCache {
    @Override
    public DateTime getMaxPhenomenonTime() {
        return getGlobalPhenomenonTimeEnvelope().getEnd();
    }

    @Override
    public DateTime getMinPhenomenonTime() {
        return getGlobalPhenomenonTimeEnvelope().getStart();
    }

    @Override
    public Set<Integer> getEpsgCodes() {
        return copyOf(getEpsgCodesSet());
    }

    @Override
    public Set<String> getFeatureOfInterestTypes() {
        return copyOf(getFeatureOfInterestTypesSet());
    }

    @Override
    public Set<String> getFeaturesOfInterest() {
        return copyOf(getFeaturesOfInterestSet());
    }

    @Override
    public Set<String> getObservationIdentifiers() {
        return copyOf(getObservationIdentifiersSet());
    }

    @Override
    public Set<String> getObservationTypes() {
        return copyOf(getObservationTypesSet());
    }

    @Override
    public Set<String> getProcedureDescriptionFormats() {
        return copyOf(getProcedureDescriptionFormatsSet());
    }

    @Override
    public Set<String> getProcedures() {
        return copyOf(getProceduresSet());
    }

    @Override
    public Set<String> getResultTemplates() {
        return copyOf(getResultTemplatesSet());
    }

    @Override
    public SosEnvelope getGlobalEnvelope() {
        return copyOf(getGlobalSpatialEnvelope());
    }

    @Override
    public Set<String> getOfferings() {
        return copyOf(getProceduresForOfferingsMap().keySet());
    }

    @Override
    public Set<String> getOfferingsForObservableProperty(String observableProperty) {
        return copyOf(getOfferingsForObservablePropertiesMap().get(observableProperty));
    }

    @Override
    public Set<String> getOfferingsForProcedure(String procedure) {
        return copyOf(getOfferingsForProceduresMap().get(procedure));
    }

    @Override
    public Set<String> getProceduresForFeatureOfInterest(String featureOfInterest) {
        return copyOf(getProceduresForFeaturesOfInterestMap().get(featureOfInterest));
    }

    @Override
    public Set<String> getProceduresForObservableProperty(String observableProperty) {
        return copyOf(getProceduresForObservablePropertiesMap().get(observableProperty));
    }

    @Override
    public Set<String> getProceduresForOffering(String offering) {
        return copyOf(getProceduresForOfferingsMap().get(offering));
    }

    @Override
    public Set<String> getRelatedFeaturesForOffering(String offering) {
        return copyOf(getRelatedFeaturesForOfferingsMap().get(offering));
    }

    @Override
    public Set<String> getResultTemplatesForOffering(String offering) {
        return copyOf(getResultTemplatesForOfferingsMap().get(offering));
    }

    @Override
    public Set<String> getRolesForRelatedFeature(String relatedFeature) {
        return copyOf(getRolesForRelatedFeaturesMap().get(relatedFeature));
    }

    @Override
    public SosEnvelope getEnvelopeForOffering(String offering) {
        return copyOf(getEnvelopeForOfferingsMap().get(offering));
    }

    @Override
    public String getNameForOffering(String offering) {
        return getNameForOfferingsMap().get(offering);
    }

    @Override
    public Set<String> getCompositePhenomenonsForOffering(String offering) {
        return copyOf(getCompositePhenomenonsForOfferingsMap().get(offering));
    }

    @Override
    public Set<String> getObservablePropertiesForCompositePhenomenon(String compositePhenomenon) {
        return copyOf(getObservablePropertiesForCompositePhenomenonsMap().get(compositePhenomenon));
    }

    @Override
    public DateTime getMaxPhenomenonTimeForOffering(String offering) {
        return getMaxPhenomenonTimeForOfferingsMap().get(offering);
    }

    @Override
    public DateTime getMinPhenomenonTimeForOffering(String offering) {
        return getMinPhenomenonTimeForOfferingsMap().get(offering);
    }

    @Override
    public Set<String> getAllowedObservationTypesForOffering(String offering) {
        return copyOf(getAllowedObservationTypesForOfferingsMap().get(offering));
    }

    @Override
    public Set<String> getFeaturesOfInterestForOffering(String offering) {
        return copyOf(getFeaturesOfInterestForOfferingMap().get(offering));
    }

    @Override
    public Set<String> getFeaturesOfInterestForResultTemplate(String resultTemplate) {
        return copyOf(getFeaturesOfInterestForResultTemplatesMap().get(resultTemplate));
    }

    @Override
    public Set<String> getObservablePropertiesForOffering(String offering) {
        Set<String> result = copyOf(getObservablePropertiesForOfferingsMap().get(offering));
        final Set<String> compositePhenomenonsForOffering = getCompositePhenomenonsForOfferingsMap().get(offering);
        if (compositePhenomenonsForOffering != null) {
            for (String cp : compositePhenomenonsForOffering) {
                result.addAll(getObservablePropertiesForCompositePhenomenon(cp));
            }
        }
        return result;
    }

    @Override
    public Set<String> getObservablePropertiesForProcedure(String procedure) {
        return copyOf(getObservablePropertiesForProceduresMap().get(procedure));
    }

    @Override
    public Set<String> getObservationIdentifiersForProcedure(String procedure) {
        return copyOf(getObservationIdentifiersForProceduresMap().get(procedure));
    }

    @Override
    public Set<String> getObservationTypesForOffering(String offering) {
        return copyOf(getObservationTypesForOfferingsMap().get(offering));
    }

    @Override
    public Set<String> getObservablePropertiesForResultTemplate(String resultTemplate) {
        return copyOf(getObservablePropertiesForResultTemplatesMap().get(resultTemplate));
    }

    @Override
    public Set<String> getParentProcedures(String procedureIdentifier, boolean fullHierarchy, boolean includeSelf) {
        return getHierarchy(getParentProceduresForProceduresMap(), procedureIdentifier, fullHierarchy, includeSelf);
    }

    @Override
    public Set<String> getParentFeatures(String featureIdentifier, boolean fullHierarchy, boolean includeSelf) {
        return getHierarchy(getParentFeaturesForFeaturesOfInterestMap(), featureIdentifier, fullHierarchy, includeSelf);
    }

    @Override
    public Set<String> getChildProcedures(String procedureIdentifier, boolean fullHierarchy, boolean includeSelf) {
        return getHierarchy(getChildProceduresForProceduresMap(), procedureIdentifier, fullHierarchy, includeSelf);
    }

    @Override
    public Set<String> getChildFeatures(String featureIdentifier, boolean fullHierarchy, boolean includeSelf) {
        return getHierarchy(getChildFeaturesForFeaturesOfInterestMap(), featureIdentifier, fullHierarchy, includeSelf);
    }

    @Override
    public Set<String> getParentProcedures(Set<String> procedureIdentifiers, boolean fullHierarchy,
                                           boolean includeSelves) {
        return getHierarchy(getParentProceduresForProceduresMap(), procedureIdentifiers, fullHierarchy, includeSelves);
    }

    @Override
    public Set<String> getParentFeatures(Set<String> featureIdentifiers, boolean fullHierarchy, boolean includeSelves) {
        return getHierarchy(getParentFeaturesForFeaturesOfInterestMap(), featureIdentifiers, fullHierarchy, includeSelves);
    }

    @Override
    public Set<String> getFeaturesOfInterestWithResultTemplate() {
        return CollectionHelper.unionOfListOfLists(getFeaturesOfInterestForResultTemplatesMap().values());
    }

    @Override
    public Set<String> getObservableProperties() {
        return CollectionHelper.unionOfListOfLists(getObservablePropertiesForOfferingsMap().values());
    }

    @Override
    public Set<String> getObservablePropertiesWithResultTemplate() {
        return CollectionHelper.unionOfListOfLists(getObservablePropertiesForResultTemplatesMap().values());
    }

    @Override
    public Set<String> getOfferingsWithResultTemplate() {
        return copyOf(getResultTemplatesForOfferingsMap().keySet());
    }

    @Override
    public Set<String> getRelatedFeatures() {
        return CollectionHelper.unionOfListOfLists(getRelatedFeaturesForOfferingsMap().values());
    }

    @Override
    public boolean hasFeatureOfInterest(String featureOfInterest) {
        return getFeaturesOfInterest().contains(featureOfInterest);
    }

    @Override
    public boolean hasObservableProperty(String observableProperty) {
        return getObservableProperties().contains(observableProperty);
    }

    @Override
    public boolean hasObservationIdentifier(String observationIdentifier) {
        return getObservationIdentifiers().contains(observationIdentifier);
    }

    @Override
    public boolean hasObservationType(String observationType) {
        return getObservationTypes().contains(observationType);
    }

    @Override
    public boolean hasOffering(String offering) {
        return getOfferings().contains(offering);
    }

    @Override
    public boolean hasProcedure(String procedure) {
        return getProcedures().contains(procedure);
    }

    @Override
    public boolean hasRelatedFeature(String relatedFeature) {
        return getRelatedFeatures().contains(relatedFeature);
    }

    @Override
    public boolean hasResultTemplate(String resultTemplate) {
        return getResultTemplates().contains(resultTemplate);
    }

    @Override
    public boolean hasEpsgCode(Integer epsgCode) {
        return getEpsgCodes().contains(epsgCode);
    }

    @Override
    public boolean hasMaxPhenomenonTimeForOffering(String offering) {
        return getMaxPhenomenonTimeForOffering(offering) != null;
    }

    @Override
    public boolean hasMinPhenomenonTimeForOffering(String offering) {
        return getMinPhenomenonTimeForOffering(offering) != null;
    }

    @Override
    public boolean hasEnvelopeForOffering(String offering) {
        final SosEnvelope e = getEnvelopeForOffering(offering);
        return e != null && e.isSetEnvelope();
    }

    @Override
    public boolean hasMaxPhenomenonTime() {
        return getMaxPhenomenonTime() != null;
    }

    @Override
    public boolean hasMinPhenomenonTime() {
        return getMinPhenomenonTime() != null;
    }

    @Override
    public boolean hasGlobalEnvelope() {
        final SosEnvelope e = getGlobalEnvelope();
        return e != null && e.isSetEnvelope();
    }

    @Override
    public DateTime getMaxResultTime() {
        return getGlobalResultTimeEnvelope().getEnd();
    }

    @Override
    public boolean hasMaxResultTime() {
        return getMaxResultTime() != null;
    }

    @Override
    public DateTime getMaxResultTimeForOffering(String offering) {
        return getMaxResultTimeForOfferingsMap().get(offering);
    }

    @Override
    public boolean hasMaxResultTimeForOffering(String offering) {
        return getMaxResultTimeForOffering(offering) != null;
    }

    @Override
    public DateTime getMinResultTime() {
        return getGlobalResultTimeEnvelope().getStart();
    }

    @Override
    public boolean hasMinResultTime() {
        return getMinResultTime() != null;
    }

    @Override
    public DateTime getMinResultTimeForOffering(String offering) {
        return getMinResultTimeForOfferingsMap().get(offering);
    }

    @Override
    public boolean hasMinResultTimeForOffering(String offering) {
        return getMinPhenomenonTimeForOffering(offering) != null;
    }
}
