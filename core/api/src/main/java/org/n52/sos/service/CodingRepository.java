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
package org.n52.sos.service;

import static org.n52.sos.util.CollectionHelper.map;
import static org.n52.sos.util.CollectionHelper.set;
import static org.n52.sos.util.MultiMaps.newSetMultiMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

import org.n52.sos.config.SettingsManager;
import org.n52.sos.decode.Decoder;
import org.n52.sos.decode.DecoderKey;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.encode.Encoder;
import org.n52.sos.encode.EncoderKey;
import org.n52.sos.encode.ObservationEncoder;
import org.n52.sos.encode.ResponseFormatKeyType;
import org.n52.sos.encode.XmlEncoderKey;
import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.service.operator.ServiceOperatorRepository;
import org.n52.sos.util.Activatable;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.SchemaLocation;
import org.n52.sos.util.SetMultiMap;
import org.n52.sos.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class CodingRepository {
    private static final Logger LOG = LoggerFactory.getLogger(CodingRepository.class);

    private static CodingRepository instance;

    @SuppressWarnings("rawtypes")
    private final ServiceLoader<Decoder> serviceLoaderDecoder;

    @SuppressWarnings("rawtypes")
    private final ServiceLoader<Encoder> serviceLoaderEncoder;

    private final Set<Decoder<?, ?>> decoders;

    private final Set<Encoder<?, ?>> encoders;

    private final SetMultiMap<DecoderKey, Decoder<?, ?>> decoderByKey = newSetMultiMap();

    private final SetMultiMap<EncoderKey, Encoder<?, ?>> encoderByKey = newSetMultiMap();

    private SetMultiMap<SupportedTypeKey, Activatable<String>> typeMap = newSetMultiMap(SupportedTypeKey.class);

    private final Set<ObservationEncoder<?, ?>> observationEncoders = set();

    private final Map<String, Map<String, Set<String>>> responseFormats = map();

    private final Map<ResponseFormatKeyType, Boolean> responseFormatStatus = map();

    private final Map<String, Set<SchemaLocation>> schemaLocations = map();

    /**
     * @return Returns a singleton instance of the CodingRepository.
     */
    public static CodingRepository getInstance() {
        if (instance == null) {
            instance = new CodingRepository();
        }
        return instance;
    }

    /**
     * private constructor for singleton
     */
    private CodingRepository() {
        this.serviceLoaderDecoder = ServiceLoader.load(Decoder.class);
        this.serviceLoaderEncoder = ServiceLoader.load(Encoder.class);
        this.decoders = CollectionHelper.asSet(loadDecoders());
        this.encoders = CollectionHelper.asSet(loadEncoders());
        initDecoderMap();
        initEncoderMap();
        generateTypeMap();
        generateResponseFormatMaps();
        generateSchemaLocationMap();
    }

    @SuppressWarnings("unchecked")
    private <T> T unsafeCast(Object o) {
        return (T) o;
    }

    private <F, T> Decoder<F, T> processDecoderMatches(Set<Decoder<?, ?>> matches, DecoderKey key) {
        if (matches == null || matches.isEmpty()) {
            LOG.debug("No Decoder implementation for {}", key);
            return null;
        } else if (matches.size() > 1) {
            List<Decoder<?, ?>> list = new ArrayList<Decoder<?, ?>>(matches);
            Collections.sort(list, new DecoderComparator(key));
            Decoder<?, ?> dec = list.iterator().next();
            LOG.warn("Requested ambiguous Decoder implementations for {}: Found {}; Choosing {}.", key,
                    StringHelper.join(", ", matches), dec);
            return unsafeCast(dec);
        } else {
            return unsafeCast(matches.iterator().next());
        }
    }

    private <F, T> Encoder<F, T> processEncoderMatches(Set<Encoder<?, ?>> matches, EncoderKey key) {
        if (matches == null || matches.isEmpty()) {
            LOG.debug("No Encoder for {}", key);
            return null;
        } else if (matches.size() > 1) {
            List<Encoder<?, ?>> list = new ArrayList<Encoder<?, ?>>(matches);
            Collections.sort(list, new EncoderComparator(key));
            Encoder<?, ?> enc = list.iterator().next();
            LOG.warn("Requested ambiguous Encoder implementations for {}: Found {}; Choosing {}.", key,
                    StringHelper.join(", ", matches), enc);
            return unsafeCast(enc);
        } else {
            return unsafeCast(matches.iterator().next());
        }
    }

    public void updateDecoders() {
        LOG.debug("Reloading Decoder implementations");
        this.decoders.clear();
        this.decoders.addAll(loadDecoders());
        initDecoderMap();
        generateTypeMap();
        LOG.debug("Reloaded Decoder implementations");
    }

    public void updateEncoders() {
        LOG.debug("Reloading Encoder implementations");
        this.encoders.clear();
        this.encoders.addAll(loadEncoders());
        initEncoderMap();
        generateTypeMap();
        generateResponseFormatMaps();
        generateSchemaLocationMap();
        LOG.debug("Reloaded Encoder implementations");
    }

    private void generateResponseFormatMaps() {
        this.responseFormatStatus.clear();
        this.responseFormats.clear();
        final Set<ServiceOperatorKeyType> serviceOperatorKeyTypes =
                ServiceOperatorRepository.getInstance().getServiceOperatorKeyTypes();
        for (Encoder<?, ?> e : getEncoders()) {
            if (e instanceof ObservationEncoder) {
                final ObservationEncoder<?, ?> oe = (ObservationEncoder<?, ?>) e;
                for (ServiceOperatorKeyType sokt : serviceOperatorKeyTypes) {
                    Set<String> rfs = oe.getSupportedResponseFormats(sokt.getService(), sokt.getVersion());
                    if (rfs != null) {
                        for (String rf : rfs) {
                            addResponseFormat(new ResponseFormatKeyType(sokt, rf));
                        }
                    }
                }
            }
        }
    }

    private void generateSchemaLocationMap() {
        schemaLocations.clear();
        for (Encoder<?, ?> encoder : this.encoders) {
            if (CollectionHelper.isNotEmpty(encoder.getEncoderKeyType())) {
                EncoderKey next = encoder.getEncoderKeyType().iterator().next();
                if (next instanceof XmlEncoderKey) {
                    if (CollectionHelper.isNotEmpty(encoder.getSchemaLocations())) {
                        schemaLocations.put(((XmlEncoderKey) next).getNamespace(), encoder.getSchemaLocations());
                    }
                }
            }
        }
    }

    protected void addResponseFormat(ResponseFormatKeyType rfkt) {
        try {
            this.responseFormatStatus.put(rfkt, SettingsManager.getInstance().isActive(rfkt));
        } catch (ConnectionProviderException ex) {
            throw new ConfigurationException(ex);
        }
        Map<String, Set<String>> byService = this.responseFormats.get(rfkt.getService());
        if (byService == null) {
            this.responseFormats.put(rfkt.getService(), byService = map());
        }
        Set<String> byVersion = byService.get(rfkt.getVersion());
        if (byVersion == null) {
            byService.put(rfkt.getVersion(), byVersion = set());
        }
        byVersion.add(rfkt.getResponseFormat());
    }

    private List<Decoder<?, ?>> loadDecoders() {
        List<Decoder<?, ?>> loadedDecoders = new LinkedList<Decoder<?, ?>>();
        try {
            SettingsManager sm = SettingsManager.getInstance();
            for (Decoder<?, ?> decoder : serviceLoaderDecoder) {
                sm.configure(decoder);
                loadedDecoders.add(decoder);
            }
        } catch (ServiceConfigurationError sce) {
            String text = "An Decoder implementation could not be loaded!";
            LOG.warn(text, sce);
            throw new ConfigurationException(text, sce);
        }
        // if (loadedDecoders.isEmpty()) {
        // String exceptionText = "No Decoder implementations is loaded!";
        // LOG.error(exceptionText);
        // throw new ConfigurationException(exceptionText);
        // }
        return loadedDecoders;
    }

    private List<Encoder<?, ?>> loadEncoders() {
        List<Encoder<?, ?>> loadedEncoders = new LinkedList<Encoder<?, ?>>();
        try {
            SettingsManager sm = SettingsManager.getInstance();
            for (Encoder<?, ?> encoder : serviceLoaderEncoder) {
                sm.configure(encoder);
                loadedEncoders.add(encoder);
            }
        } catch (ServiceConfigurationError sce) {
            String text = "An Encoder implementation could not be loaded!";
            LOG.warn(text, sce);
            throw new ConfigurationException(text, sce);
        }
        // if (loadedEncoders.isEmpty()) {
        // String exceptionText = "No Encoder implementations is loaded!";
        // LOG.error(exceptionText);
        // throw new ConfigurationException(exceptionText);
        // }
        return loadedEncoders;
    }

    public Set<Decoder<?, ?>> getDecoders() {
        return CollectionHelper.unmodifiableSet(decoders);
    }

    public Set<Encoder<?, ?>> getEncoders() {
        return CollectionHelper.unmodifiableSet(encoders);
    }

    public Map<DecoderKey, Set<Decoder<?, ?>>> getDecoderByKey() {
        return CollectionHelper.unmodifiableMap(decoderByKey);
    }

    public Map<EncoderKey, Set<Encoder<?, ?>>> getEncoderByKey() {
        return CollectionHelper.unmodifiableMap(encoderByKey);
    }

    public Set<String> getFeatureOfInterestTypes() {
        return typesFor(SupportedTypeKey.FeatureType);
    }

    public Set<String> getObservationTypes() {
        return typesFor(SupportedTypeKey.ObservationType);
    }

    public Set<String> getProcedureDescriptionFormats() {
        return typesFor(SupportedTypeKey.ProcedureDescriptionFormat);
    }

    public Set<String> getSweTypes() {
        return typesFor(SupportedTypeKey.SweType);
    }

    private Set<String> typesFor(SupportedTypeKey key) {
        if (typeMap == null || !typeMap.containsKey(key) || typeMap.get(key) == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(Activatable.filter(typeMap.get(key)));
    }

    private void generateTypeMap() {
        List<Map<SupportedTypeKey, Set<String>>> list = new LinkedList<Map<SupportedTypeKey, Set<String>>>();
        for (Decoder<?, ?> decoder : getDecoders()) {
            list.add(decoder.getSupportedTypes());
        }
        for (Encoder<?, ?> encoder : getEncoders()) {
            list.add(encoder.getSupportedTypes());
        }

        SetMultiMap<SupportedTypeKey, Activatable<String>> resultMap = newSetMultiMap(SupportedTypeKey.class);
        for (Map<SupportedTypeKey, Set<String>> map : list) {
            if (map != null && !map.isEmpty()) {
                for (SupportedTypeKey type : map.keySet()) {
                    if (map.get(type) != null && !map.get(type).isEmpty()) {
                        resultMap.addAll(type, Activatable.from(map.get(type)));
                    }
                }
            }
        }

        setStateForTypes(resultMap);
        this.typeMap = resultMap;
    }

    private void initEncoderMap() {
        this.encoderByKey.clear();
        for (Encoder<?, ?> encoder : getEncoders()) {
            for (EncoderKey key : encoder.getEncoderKeyType()) {
                encoderByKey.add(key, encoder);
            }
            if (encoder instanceof ObservationEncoder) {
                observationEncoders.add((ObservationEncoder<?, ?>) encoder);
            }
        }
    }

    private void initDecoderMap() {
        this.decoderByKey.clear();
        for (Decoder<?, ?> decoder : getDecoders()) {
            for (DecoderKey key : decoder.getDecoderKeyTypes()) {
                decoderByKey.add(key, decoder);
            }
        }
    }

    public <F, T> Decoder<F, T> getDecoder(DecoderKey key, DecoderKey... keys) {
        if (keys.length == 0) {
            return getDecoderSingleKey(key);
        } else {
            return getDecoderCompositeKey(new CompositeDecoderKey(CollectionHelper.asList(key, keys)));
        }
    }

    public <F, T> Encoder<F, T> getEncoder(EncoderKey key, EncoderKey... keys) {
        if (keys.length == 0) {
            return getEncoderSingleKey(key);
        } else {
            return getEncoderCompositeKey(new CompositeEncoderKey(CollectionHelper.asList(key, keys)));
        }
    }

    public Set<SchemaLocation> getSchemaLocation(String namespace) {
        if (schemaLocations.containsKey(namespace)) {
            return schemaLocations.get(namespace);
        }
        return set();
    }

    private <F, T> Decoder<F, T> getDecoderSingleKey(DecoderKey key) {
        return processDecoderMatches(findDecodersForSingleKey(key), key);
    }

    private <F, T> Decoder<F, T> getDecoderCompositeKey(CompositeDecoderKey key) {
        return processDecoderMatches(findDecodersForCompositeKey(key), key);
    }

    private <F, T> Encoder<F, T> getEncoderSingleKey(EncoderKey key) {
        return processEncoderMatches(findEncodersForSingleKey(key), key);
    }

    private <F, T> Encoder<F, T> getEncoderCompositeKey(CompositeEncoderKey key) {
        return processEncoderMatches(findEncodersForCompositeKey(key), key);
    }

    private Set<Encoder<?, ?>> findEncodersForSingleKey(EncoderKey key) {
        if (!encoderByKey.containsKey(key)) {
            for (Encoder<?, ?> encoder : getEncoders()) {
                for (EncoderKey ek : encoder.getEncoderKeyType()) {
                    if (ek.getSimilarity(key) > 0) {
                        encoderByKey.add(key, encoder);
                    }
                }
            }
        }
        return encoderByKey.get(key);
    }

    private Set<Decoder<?, ?>> findDecodersForSingleKey(DecoderKey key) {
        if (!decoderByKey.containsKey(key)) {
            for (Decoder<?, ?> decoder : getDecoders()) {
                for (DecoderKey dk : decoder.getDecoderKeyTypes()) {
                    if (dk.getSimilarity(key) > 0) {
                        decoderByKey.add(key, decoder);
                    }
                }
            }
        }
        return decoderByKey.get(key);
    }

    private Set<Encoder<?, ?>> findEncodersForCompositeKey(CompositeEncoderKey ck) {
        if (!encoderByKey.containsKey(ck)) {
            // first request; search for matching encoders and save result for
            // later quries
            for (Encoder<?, ?> encoder : encoders) {
                if (ck.matches(encoder.getEncoderKeyType())) {
                    encoderByKey.add(ck, encoder);
                }
            }
            LOG.debug("Found {} Encoders for CompositeKey: {}", encoderByKey.get(ck).size(),
                    StringHelper.join(", ", encoderByKey.get(ck)));
        }
        return encoderByKey.get(ck);
    }

    private Set<Decoder<?, ?>> findDecodersForCompositeKey(CompositeDecoderKey ck) {
        if (!decoderByKey.containsKey(ck)) {
            // first request; search for matching decoders and save result for
            // later queries
            for (Decoder<?, ?> decoder : decoders) {
                if (ck.matches(decoder.getDecoderKeyTypes())) {
                    decoderByKey.add(ck, decoder);
                }
            }
            LOG.debug("Found {} Decoders for CompositeKey: {}", decoderByKey.get(ck).size(),
                    StringHelper.join(", ", decoderByKey.get(ck)));
        }
        return decoderByKey.get(ck);
    }

    public Set<String> getSupportedResponseFormats(String service, String version) {
        Map<String, Set<String>> byService = this.responseFormats.get(service);
        if (byService == null) {
            return Collections.emptySet();
        }
        Set<String> rfs = byService.get(version);
        if (rfs == null) {
            return Collections.emptySet();
        }

        ServiceOperatorKeyType sokt = new ServiceOperatorKeyType(service, version);
        Set<String> result = set();
        for (String a : rfs) {
            ResponseFormatKeyType rfkt = new ResponseFormatKeyType(sokt, a);
            final Boolean status = responseFormatStatus.get(rfkt);
            if (status != null && status.booleanValue()) {
                result.add(a);
            }
        }
        return result;
    }

    public Set<String> getAllSupportedResponseFormats(String service, String version) {
        Map<String, Set<String>> byService = this.responseFormats.get(service);
        if (byService == null) {
            return Collections.emptySet();
        }
        Set<String> rfs = byService.get(version);
        if (rfs == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(rfs);
    }

    public Set<String> getSupportedResponseFormats(ServiceOperatorKeyType sokt) {
        return getSupportedResponseFormats(sokt.getService(), sokt.getVersion());
    }

    public Set<String> getAllSupportedResponseFormats(ServiceOperatorKeyType sokt) {
        return getAllSupportedResponseFormats(sokt.getService(), sokt.getVersion());
    }

    public Map<ServiceOperatorKeyType, Set<String>> getSupportedResponseFormats() {
        Map<ServiceOperatorKeyType, Set<String>> map = map();
        for (ServiceOperatorKeyType sokt : ServiceOperatorRepository.getInstance().getServiceOperatorKeyTypes()) {
            map.put(sokt, getSupportedResponseFormats(sokt));
        }
        return map;
    }

    public Map<ServiceOperatorKeyType, Set<String>> getAllSupportedResponseFormats() {
        Map<ServiceOperatorKeyType, Set<String>> map = map();
        for (ServiceOperatorKeyType sokt : ServiceOperatorRepository.getInstance().getServiceOperatorKeyTypes()) {
            map.put(sokt, getAllSupportedResponseFormats(sokt));
        }
        return map;
    }

    public Set<String> getAllProcedureDescriptionFormats() {
        return Activatable.unfiltered(this.typeMap.get(SupportedTypeKey.ProcedureDescriptionFormat));
    }

    public void setActive(ResponseFormatKeyType rfkt, boolean active) {
        if (this.responseFormatStatus.containsKey(rfkt)) {
            this.responseFormatStatus.put(rfkt, active);
        }
    }

    public void setActive(String procedureDescriptionFormat, boolean active) {
        final Set<Activatable<String>> pdfs = typeMap.get(SupportedTypeKey.ProcedureDescriptionFormat);
        if (pdfs != null && procedureDescriptionFormat != null) {
            for (Activatable<String> a : pdfs) {
                if (procedureDescriptionFormat.equals(a.getInternal())) {
                    a.setActive(active);
                }
            }
        }
    }

    private void setStateForTypes(Map<SupportedTypeKey, Set<Activatable<String>>> map) {
        if (map.get(SupportedTypeKey.ProcedureDescriptionFormat) != null) {
            SettingsManager sm = SettingsManager.getInstance();
            for (Activatable<String> pdf : map.get(SupportedTypeKey.ProcedureDescriptionFormat)) {
                try {
                    pdf.setActive(sm.isActive(pdf.getInternal()));
                } catch (ConnectionProviderException ex) {
                    throw new ConfigurationException(ex);
                }
            }
        }
    }

    private abstract static class SimilarityComparator<T> implements Comparator<T> {
        @Override
        public int compare(T o1, T o2) {
            int s1 = getSimilarity(o1);
            int s2 = getSimilarity(o2);
            if (s1 == s2) {
                if (o1.getClass().isAssignableFrom(o2.getClass())) {
                    return 1;
                } else if (o2.getClass().isAssignableFrom(o1.getClass())) {
                    return -1;
                }
            }
            return (s1 == 0) ? -1 : (s2 == 0) ? 1 : (s1 < 0) ? ((s2 < 0) ? 0 : 1) : (s2 < 0) ? -1 : ((s1 < s2) ? -1
                    : 1);
        }

        protected abstract int getSimilarity(T t);
    }

    private static class DecoderComparator extends SimilarityComparator<Decoder<?, ?>> {
        private DecoderKey key;

        DecoderComparator(DecoderKey key) {
            this.key = key;
        }

        @Override
        protected int getSimilarity(Decoder<?, ?> d) {
            int similarity = -1;
            for (DecoderKey dk : d.getDecoderKeyTypes()) {
                int s = dk.getSimilarity(this.key);
                if (similarity < 0) {
                    similarity = s;
                } else if (s >= 0) {
                    similarity = Math.min(similarity, s);
                }
                if (similarity == 0) {
                    break;
                }
            }
            return similarity;
        }
    }

    private static class EncoderComparator extends SimilarityComparator<Encoder<?, ?>> {
        private EncoderKey key;

        EncoderComparator(EncoderKey key) {
            this.key = key;
        }

        @Override
        protected int getSimilarity(Encoder<?, ?> d) {
            int similarity = -1;
            for (EncoderKey dk : d.getEncoderKeyType()) {
                int s = dk.getSimilarity(this.key);
                if (similarity < 0) {
                    similarity = s;
                } else if (s >= 0) {
                    similarity = Math.min(similarity, s);
                }
                if (similarity == 0) {
                    break;
                }
            }
            return similarity;
        }
    }

    private static class CompositeEncoderKey extends EncoderKey {
        private final Set<EncoderKey> keys;

        CompositeEncoderKey(Iterable<EncoderKey> keys) {
            this.keys = CollectionHelper.asSet(keys);
        }

        private Set<EncoderKey> getKeys() {
            return Collections.unmodifiableSet(this.keys);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj.getClass() == getClass()) {
                CompositeEncoderKey key = (CompositeEncoderKey) obj;
                return keys.size() == key.getKeys().size() && keys.containsAll(key.getKeys())
                        && key.getKeys().containsAll(keys);
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("%s[%s]", getClass().getSimpleName(), StringHelper.join(", ", keys));
        }

        @Override
        public int hashCode() {
            return hash(11, 13, keys.toArray());
        }

        public boolean matches(Set<EncoderKey> toTest) {
            return toTest == null ? keys.isEmpty() : toTest.containsAll(keys);
        }

        @Override
        public int getSimilarity(EncoderKey key) {
            if (key != null && key.getClass() == getClass()) {
                CompositeEncoderKey cek = (CompositeEncoderKey) key;
                if (cek.getKeys().size() != keys.size()) {
                    return -1;
                }
                int similarity = 0;
                for (EncoderKey k1 : keys) {
                    int s = -1;
                    for (EncoderKey k2 : cek.getKeys()) {
                        int ks = k1.getSimilarity(k2);
                        if ((s = (s < 0) ? ks : Math.min(s, ks)) == 0) {
                            break;
                        }
                    }
                    if (s < 0) {
                        return -1;
                    } else {
                        similarity += s;
                    }
                }
                return similarity;
            }
            return -1;
        }
    }

    private static class CompositeDecoderKey extends DecoderKey {
        private final Set<DecoderKey> keys;

        CompositeDecoderKey(Iterable<DecoderKey> keys) {
            this.keys = CollectionHelper.asSet(keys);
        }

        private Set<DecoderKey> getKeys() {
            return Collections.unmodifiableSet(this.keys);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj.getClass() == getClass()) {
                CompositeDecoderKey key = (CompositeDecoderKey) obj;
                return keys.containsAll(key.getKeys()) && key.getKeys().containsAll(keys);
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("%s[%s]", getClass().getSimpleName(), StringHelper.join(", ", keys));
        }

        @Override
        public int hashCode() {
            return hash(11, 13, keys.toArray());
        }

        public boolean matches(Set<DecoderKey> toTest) {
            return toTest == null ? keys.isEmpty() : toTest.containsAll(keys);
        }

        @Override
        public int getSimilarity(DecoderKey key) {
            if (key != null && key.getClass() == getClass()) {
                CompositeDecoderKey cek = (CompositeDecoderKey) key;
                if (cek.getKeys().size() != keys.size()) {
                    return -1;
                }
                int similarity = 0;
                for (DecoderKey k1 : keys) {
                    int s = -1;
                    for (DecoderKey k2 : cek.getKeys()) {
                        int ks = k1.getSimilarity(k2);
                        if ((s = (s < 0) ? ks : Math.min(s, ks)) == 0) {
                            break;
                        }
                    }
                    if (s < 0) {
                        return -1;
                    } else {
                        similarity += s;
                    }
                }
                return similarity;
            }
            return -1;
        }
    }
}
