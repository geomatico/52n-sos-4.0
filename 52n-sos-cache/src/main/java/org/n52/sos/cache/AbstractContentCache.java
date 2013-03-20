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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.DateTime;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.SetMultiMap;
import org.n52.sos.util.SynchonizedHashSetMultiMap;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Abstract {@code ContentCache} implementation that encapsulates the needed {@code Map}s.
 *
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0.0
 */
public abstract class AbstractContentCache extends AbstractStaticContentCache {
    /**
     * Convenient function to check if two objects are equal (including null values).
     *
     * @param a the first object
     * @param b the second object
     *
     * @return if a equals b
     */
    protected static boolean eq(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    /**
     * Convenient function to create a hash for the specified objects. For every object in a it calculates
     * <pre>
     * hashCode = multiplier * hashCode + (o == null ? 0 : o.hashCode());
     * </pre>
     *
     * @param initial   the initial hash code
     * @param multiplier the multiplier
     * @param a          the objects to include in the hash code
     *
     * @return the hash code
     */
    protected static int hash(int initial, int multiplier, Object... a) {
        int hashCode = initial;
        for (Object o : a) {
            hashCode = multiplier * hashCode + (o == null ? 0 : o.hashCode());
        }
        return hashCode;
    }

    /**
     * Creates a new synchronized map from the specified map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     *
     * @return the synchronized map
     */
    protected static <K, V> Map<K, V> synchronizedMap(Map<K, V> map) {
        if (map == null) {
            return CollectionHelper.synchronizedMap(0);
        } else {
            return Collections.synchronizedMap(new HashMap<K, V>(map));
        }
    }

    /**
     * Creates a new synchronized set from the specified elements.
     *
     * @param <T> the element type
     * @param elements the elements
     *
     * @return the synchronized set
     */
    protected static <T> Set<T> synchronizedSet(Iterable<T> elements) {
        if (elements == null) {
            return CollectionHelper.synchronizedSet(0);
        } else {
            if (elements instanceof Collection) {
                return Collections.synchronizedSet(new HashSet<T>((Collection<T>) elements));
            } else {
                HashSet<T> hashSet = new HashSet<T>();
                for (T t : elements) {
                    hashSet.add(t);
                }
                return Collections.synchronizedSet(hashSet);
            }
        }
    }

    /**
     * Creates a new empty synchronized map.
     *
     * @param <K> the key type
     * @param <V> the value type
     *
     * @return the synchronized map
     */
    protected static <K, V> Map<K, V> synchronizedMap() {
        return synchronizedMap(null);
    }

    /**
     * Creates a new empty synchronized set.
     *
     * @param <T> the element type
     *
     * @return a synchronized set
     */
    protected static <T> Set<T> synchronizedSet() {
        return synchronizedSet(null);
    }

    /**
     * Creates a unmodifiable copy of the specified set.
     *
     * @param <T> the element type
     * @param set the set
     *
     * @return a unmodifiable copy
     */
    protected static <T> Set<T> copyOf(Set<T> set) {
        if (set == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(new HashSet<T>(set));
        }
    }

    /**
     * Creates a copy of the specified envelope.
     *
     * @param e the envelope
     *
     * @return a coyp
     */
    protected static SosEnvelope copyOf(SosEnvelope e) {
        if (e == null) {
            // TODO empty envelope
            return null;
        } else {
            return new SosEnvelope(e.getEnvelope() == null ? null : new Envelope(e.getEnvelope()), e.getSrid());
        }
    }

    /**
     * Throws a {@code NullPointerExceptions} if value is null or a {@code IllegalArgumentException} if value is <= 0.
     *
     * @param name  the name of the value
     * @param value the value to check
     *
     * @throws NullPointerException     if value is null
     * @throws IllegalArgumentException if value is <= 0
     */
    protected static void greaterZero(String name, Integer value) throws NullPointerException,
                                                                         IllegalArgumentException {
        notNull(name, value);
        if (value.intValue() <= 0) {
            throw new IllegalArgumentException(name + " may not less or equal 0!");
        }
    }

    /**
     * Throws a {@code NullPointerExceptions} if value is null or a {@code IllegalArgumentException} if value is empty.
     *
     * @param name  the name of the value
     * @param value the value to check
     *
     * @throws NullPointerException     if value is null
     * @throws IllegalArgumentException if value is empty
     */
    protected static void notNullOrEmpty(String name, String value) throws NullPointerException,
                                                                           IllegalArgumentException {
        notNull(name, value);
        if (value.isEmpty()) {
            throw new IllegalArgumentException(name + " may not be empty!");
        }
    }

    /**
     * Throws a {@code NullPointerExceptions} if value is null or any value within is null.
     *
     * @param name  the name of the value
     * @param value the value to check
     *
     * @throws NullPointerException if value == null or value contains null
     */
    protected static void noNullValues(String name, Collection<?> value) throws NullPointerException {
        notNull(name, value);
        for (Object o : value) {
            if (o == null) {
                throw new NullPointerException(name + " may not contain null elements!");
            }
        }
    }

    /**
     * Throws a {@code NullPointerExceptions} if value is null or any value within is null or empty.
     *
     * @param name  the name of the value
     * @param value the value to check
     *
     * @throws NullPointerException     if value == null or value contains null
     * @throws IllegalArgumentException if any value is empty
     */
    protected static void noNullOrEmptyValues(String name, Collection<String> value) throws NullPointerException,
                                                                                            IllegalArgumentException {
        notNull(name, value);
        for (String o : value) {
            if (o == null) {
                throw new NullPointerException(name + " may not contain null elements!");
            }
            if (o.isEmpty()) {
                throw new IllegalArgumentException(name + " may not contain empty elements!");
            }
        }
    }

    /**
     * Throws a {@code NullPointerExceptions} if value is null or any key or value within is null.
     *
     * @param name  the name of the value
     * @param value the value to check
     *
     * @throws NullPointerException if value == null or value contains null values
     */
    protected static void noNullValues(String name, Map<?, ?> value) throws NullPointerException {
        notNull(name, value);
        for (Entry<?, ?> e : value.entrySet()) {
            if (e == null || e.getKey() == null || e.getValue() == null) {
                throw new NullPointerException(name + " may not contain null elements!");
            }
        }
    }

    /**
     * Throws a {@code NullPointerExceptions} if value is null.
     *
     * @param name  the name of the value
     * @param value the value to check
     *
     * @throws NullPointerException if value == null
     */
    protected static void notNull(String name, Object value) throws NullPointerException {
        if (value == null) {
            throw new NullPointerException(name + " may not be null!");
        }
    }

    private int defaultEpsgCode = 4326;
    private Map<String, DateTime> maxPhenomenonTimeForOfferings = synchronizedMap();
    private Map<String, DateTime> minPhenomenonTimeForOfferings = synchronizedMap();
    private Map<String, DateTime> maxResultTimeForOfferings = synchronizedMap();
    private Map<String, DateTime> minResultTimeForOfferings = synchronizedMap();
    private SetMultiMap<String, String> allowedObservationTypeForOfferings = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> childFeaturesForFeatureOfInterest = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> childProceduresForProcedures = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> compositePhenomenonForOfferings = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> featuresOfInterestForOfferings = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> featuresOfInterestForResultTemplates = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> observablePropertiesForCompositePhenomenons =
                                   new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> observablePropertiesForOfferings = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> observablePropertiesForProcedures = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> observationIdentifiersForProcedures = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> observationTypesForOfferings = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> observedPropertiesForResultTemplates = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> offeringsForObservableProperties = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> offeringsForProcedures = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> parentFeaturesForFeaturesOfInterest = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> parentProceduresForProcedures = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> proceduresForFeaturesOfInterest = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> proceduresForObservableProperties = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> proceduresForOfferings = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> relatedFeaturesForOfferings = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> resultTemplatesForOfferings = new SynchonizedHashSetMultiMap<String, String>();
    private SetMultiMap<String, String> rolesForRelatedFeatures = new SynchonizedHashSetMultiMap<String, String>();
    private Map<String, SosEnvelope> envelopeForOfferings = synchronizedMap();
    private Map<String, String> nameForOfferings = synchronizedMap();
    private Set<Integer> epsgCodes = synchronizedSet();
    private Set<String> featuresOfInterest = synchronizedSet();
    private Set<String> observationIdentifiers = synchronizedSet();
    private Set<String> procedures = synchronizedSet();
    private Set<String> resultTemplates = synchronizedSet();
    private SosEnvelope globalEnvelope = new SosEnvelope(null, defaultEpsgCode);
    private TimePeriod globalPhenomenonTimeEnvelope = new TimePeriod();
    private TimePeriod globalResultTimeEnvelope = new TimePeriod();

    /**
     * @return the relating offering -> max phenomenon time
     */
    protected Map<String, DateTime> getMaxPhenomenonTimeForOfferingsMap() {
        return this.maxPhenomenonTimeForOfferings;
    }

    /**
     * @return the relating offering -> min phenomenon time
     */
    protected Map<String, DateTime> getMinPhenomenonTimeForOfferingsMap() {
        return this.minPhenomenonTimeForOfferings;
    }

    /**
     * @return the relating offering -> max result time
     */
    protected Map<String, DateTime> getMaxResultTimeForOfferingsMap() {
        return this.maxResultTimeForOfferings;
    }

    /**
     * @return the relating offering -> min result time
     */
    protected Map<String, DateTime> getMinResultTimeForOfferingsMap() {
        return this.minResultTimeForOfferings;
    }

    /**
     * @return the relating offering -> allowed observation type
     */
    protected SetMultiMap<String, String> getAllowedObservationTypesForOfferingsMap() {
        return this.allowedObservationTypeForOfferings;
    }

    /**
     * @return the relating feature -> child feature
     */
    protected SetMultiMap<String, String> getChildFeaturesForFeaturesOfInterestMap() {
        return this.childFeaturesForFeatureOfInterest;
    }

    /**
     * @return the relating offering -> composite phenomenons
     */
    protected SetMultiMap<String, String> getCompositePhenomenonsForOfferingsMap() {
        return this.compositePhenomenonForOfferings;
    }

    /**
     * @return the relating offering -> feature
     */
    protected SetMultiMap<String, String> getFeaturesOfInterestForOfferingMap() {
        return this.featuresOfInterestForOfferings;
    }

    /**
     * @return the relating result template -> feature
     */
    protected SetMultiMap<String, String> getFeaturesOfInterestForResultTemplatesMap() {
        return this.featuresOfInterestForResultTemplates;
    }

    /**
     * @return the relating composite phenomenon -> observable property
     */
    protected SetMultiMap<String, String> getObservablePropertiesForCompositePhenomenonsMap() {
        return this.observablePropertiesForCompositePhenomenons;
    }

    /**
     * @return the relating offering -> observable property
     */
    protected SetMultiMap<String, String> getObservablePropertiesForOfferingsMap() {
        return this.observablePropertiesForOfferings;
    }

    /**
     * @return the relating procedure -> observation identifier
     */
    protected SetMultiMap<String, String> getObservationIdentifiersForProceduresMap() {
        return this.observationIdentifiersForProcedures;
    }

    /**
     * @return the relating offering -> observation types
     */
    protected SetMultiMap<String, String> getObservationTypesForOfferingsMap() {
        return this.observationTypesForOfferings;
    }

    /**
     * @return the relating result template -> obsevable properties
     */
    protected SetMultiMap<String, String> getObservablePropertiesForResultTemplatesMap() {
        return this.observedPropertiesForResultTemplates;
    }

    /**
     * @return the relating observable property -> offerings
     */
    protected SetMultiMap<String, String> getOfferingsForObservablePropertiesMap() {
        return this.offeringsForObservableProperties;
    }

    /**
     * @return the relating procedure -> offerings
     */
    protected SetMultiMap<String, String> getOfferingsForProceduresMap() {
        return this.offeringsForProcedures;
    }

    /**
     * @return the relating feature -> parent feature
     */
    protected SetMultiMap<String, String> getParentFeaturesForFeaturesOfInterestMap() {
        return this.parentFeaturesForFeaturesOfInterest;
    }

    /**
     * @return the relating feature -> procedure
     */
    protected SetMultiMap<String, String> getProceduresForFeaturesOfInterestMap() {
        return this.proceduresForFeaturesOfInterest;
    }

    /**
     * @return the relating observable property -> procedure
     */
    protected SetMultiMap<String, String> getProceduresForObservablePropertiesMap() {
        return this.proceduresForObservableProperties;
    }

    /**
     * @return the relating offering -> procedure
     */
    protected SetMultiMap<String, String> getProceduresForOfferingsMap() {
        return this.proceduresForOfferings;
    }

    /**
     * @return the relating offering -> related features
     */
    protected SetMultiMap<String, String> getRelatedFeaturesForOfferingsMap() {
        return this.relatedFeaturesForOfferings;
    }

    /**
     * @return the relating offering -> resulte templates
     */
    protected SetMultiMap<String, String> getResultTemplatesForOfferingsMap() {
        return this.resultTemplatesForOfferings;
    }

    /**
     * @return the relating related feature -> roles
     */
    protected SetMultiMap<String, String> getRolesForRelatedFeaturesMap() {
        return this.rolesForRelatedFeatures;
    }

    /**
     * @return the relating offering -> envelope
     */
    protected Map<String, SosEnvelope> getEnvelopeForOfferingsMap() {
        return this.envelopeForOfferings;
    }

    /**
     * @return the relating offering -> offering name
     */
    protected Map<String, String> getNameForOfferingsMap() {
        return this.nameForOfferings;
    }

    /**
     * @return the relating procedure -> observable properties
     */
    protected SetMultiMap<String, String> getObservablePropertiesForProceduresMap() {
        return this.observablePropertiesForProcedures;
    }

    /**
     * @return the relating procedure -> parent procedure
     */
    protected SetMultiMap<String, String> getParentProceduresForProceduresMap() {
        return this.parentProceduresForProcedures;
    }

    /**
     * @return the relating procedure -> child procedure
     */
    protected SetMultiMap<String, String> getChildProceduresForProceduresMap() {
        return this.childProceduresForProcedures;
    }

    /**
     * @return the epsg codes
     */
    protected Set<Integer> getEpsgCodesSet() {
        return this.epsgCodes;
    }

    /**
     * @return the features of interest
     */
    protected Set<String> getFeaturesOfInterestSet() {
        return this.featuresOfInterest;
    }

    /**
     * @return the observation identifiers
     */
    protected Set<String> getObservationIdentifiersSet() {
        return this.observationIdentifiers;
    }

    /**
     * @return the procedures
     */
    protected Set<String> getProceduresSet() {
        return this.procedures;
    }

    /**
     * @return the result templates
     */
    protected Set<String> getResultTemplatesSet() {
        return this.resultTemplates;
    }

    /**
     * @return the global phenomenon time envelope
     */
    protected TimePeriod getGlobalPhenomenonTimeEnvelope() {
        return this.globalPhenomenonTimeEnvelope;
    }

    /**
     * @return the global result time envelope
     */
    protected TimePeriod getGlobalResultTimeEnvelope() {
        return this.globalPhenomenonTimeEnvelope;
    }

    /**
     * @return the global spatial envelope
     */
    protected SosEnvelope getGlobalSpatialEnvelope() {
        return this.globalEnvelope;
    }

    /**
     * @param envelope the new global spatial envelope
     */
    protected void setGlobalSpatialEnvelope(SosEnvelope envelope) {
        if (envelope == null) {
            throw new NullPointerException();
        }
        this.globalEnvelope = envelope;
    }

    /**
     * @param defaultEpsgCode the new default EPSG code
     */
    public void setDefaultEPSGCode(int defaultEpsgCode) {
        this.defaultEpsgCode = defaultEpsgCode;
    }

    @Override
    public int getDefaultEPSGCode() {
        return this.defaultEpsgCode;
    }

    @Override
    public int hashCode() {
        return hash(7, 41,
                    defaultEpsgCode,
                    maxPhenomenonTimeForOfferings,
                    minPhenomenonTimeForOfferings,
                    maxResultTimeForOfferings,
                    minResultTimeForOfferings,
                    allowedObservationTypeForOfferings,
                    childFeaturesForFeatureOfInterest,
                    childProceduresForProcedures,
                    compositePhenomenonForOfferings,
                    featuresOfInterestForOfferings,
                    featuresOfInterestForResultTemplates,
                    observablePropertiesForCompositePhenomenons,
                    observablePropertiesForOfferings,
                    observablePropertiesForProcedures,
                    observationIdentifiersForProcedures,
                    observationTypesForOfferings,
                    observedPropertiesForResultTemplates,
                    offeringsForObservableProperties,
                    offeringsForProcedures,
                    parentFeaturesForFeaturesOfInterest,
                    parentProceduresForProcedures,
                    proceduresForFeaturesOfInterest,
                    proceduresForObservableProperties,
                    proceduresForOfferings,
                    relatedFeaturesForOfferings,
                    resultTemplatesForOfferings,
                    rolesForRelatedFeatures,
                    envelopeForOfferings,
                    nameForOfferings,
                    epsgCodes,
                    featuresOfInterest,
                    observationIdentifiers,
                    procedures,
                    resultTemplates,
                    globalEnvelope,
                    globalResultTimeEnvelope,
                    globalPhenomenonTimeEnvelope);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AbstractContentCache)) {
            return false;
        }
        final AbstractContentCache other = (AbstractContentCache) obj;
        if (!eq(this.defaultEpsgCode, other.defaultEpsgCode)) {
            return false;
        }
        if (!eq(this.maxPhenomenonTimeForOfferings, other.maxPhenomenonTimeForOfferings)) {
            return false;
        }
        if (!eq(this.minPhenomenonTimeForOfferings, other.minPhenomenonTimeForOfferings)) {
            return false;
        }
        if (!eq(this.maxResultTimeForOfferings, other.maxResultTimeForOfferings)) {
            return false;
        }
        if (!eq(this.minResultTimeForOfferings, other.minResultTimeForOfferings)) {
            return false;
        }
        if (!eq(this.allowedObservationTypeForOfferings, other.allowedObservationTypeForOfferings)) {
            return false;
        }
        if (!eq(this.childFeaturesForFeatureOfInterest, other.childFeaturesForFeatureOfInterest)) {
            return false;
        }
        if (!eq(this.childProceduresForProcedures, other.childProceduresForProcedures)) {
            return false;
        }
        if (!eq(this.compositePhenomenonForOfferings, other.compositePhenomenonForOfferings)) {
            return false;
        }
        if (!eq(this.featuresOfInterestForOfferings, other.featuresOfInterestForOfferings)) {
            return false;
        }
        if (!eq(this.featuresOfInterestForResultTemplates, other.featuresOfInterestForResultTemplates)) {
            return false;
        }
        if (!eq(this.observablePropertiesForCompositePhenomenons, other.observablePropertiesForCompositePhenomenons)) {
            return false;
        }
        if (!eq(this.observablePropertiesForOfferings, other.observablePropertiesForOfferings)) {
            return false;
        }
        if (!eq(this.observablePropertiesForProcedures, other.observablePropertiesForProcedures)) {
            return false;
        }
        if (!eq(this.observationIdentifiersForProcedures, other.observationIdentifiersForProcedures)) {
            return false;
        }
        if (!eq(this.observationTypesForOfferings, other.observationTypesForOfferings)) {
            return false;
        }
        if (!eq(this.observedPropertiesForResultTemplates, other.observedPropertiesForResultTemplates)) {
            return false;
        }
        if (!eq(this.offeringsForObservableProperties, other.offeringsForObservableProperties)) {
            return false;
        }
        if (!eq(this.offeringsForProcedures, other.offeringsForProcedures)) {
            return false;
        }
        if (!eq(this.parentFeaturesForFeaturesOfInterest, other.parentFeaturesForFeaturesOfInterest)) {
            return false;
        }
        if (!eq(this.parentProceduresForProcedures, other.parentProceduresForProcedures)) {
            return false;
        }
        if (!eq(this.proceduresForFeaturesOfInterest, other.proceduresForFeaturesOfInterest)) {
            return false;
        }
        if (!eq(this.proceduresForObservableProperties, other.proceduresForObservableProperties)) {
            return false;
        }
        if (!eq(this.proceduresForOfferings, other.proceduresForOfferings)) {
            return false;
        }
        if (!eq(this.relatedFeaturesForOfferings, other.relatedFeaturesForOfferings)) {
            return false;
        }
        if (!eq(this.resultTemplatesForOfferings, other.resultTemplatesForOfferings)) {
            return false;
        }
        if (!eq(this.rolesForRelatedFeatures, other.rolesForRelatedFeatures)) {
            return false;
        }
        if (!eq(this.envelopeForOfferings, other.envelopeForOfferings)) {
            return false;
        }
        if (!eq(this.nameForOfferings, other.nameForOfferings)) {
            return false;
        }
        if (!eq(this.epsgCodes, other.epsgCodes)) {
            return false;
        }
        if (!eq(this.featuresOfInterest, other.featuresOfInterest)) {
            return false;
        }
        if (!eq(this.observationIdentifiers, other.observationIdentifiers)) {
            return false;
        }
        if (!eq(this.procedures, other.procedures)) {
            return false;
        }
        if (!eq(this.resultTemplates, other.resultTemplates)) {
            return false;
        }
        if (!eq(this.globalEnvelope, other.globalEnvelope)) {
            return false;
        }
        if (!eq(this.globalPhenomenonTimeEnvelope, other.globalPhenomenonTimeEnvelope)) {
            return false;
        }
        if (!eq(this.globalResultTimeEnvelope, other.globalResultTimeEnvelope)) {
            return false;
        }
        return true;
    }
}
