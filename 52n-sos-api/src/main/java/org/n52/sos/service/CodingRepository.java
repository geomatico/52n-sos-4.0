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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

import org.n52.sos.decode.DecoderKey;
import org.n52.sos.decode.IDecoder;
import org.n52.sos.encode.EncoderKey;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class CodingRepository {

    private static abstract class SimilarityComparator<T> implements Comparator<T> {

        @Override
        public int compare(T o1, T o2) {
            int s1 = getSimilarity(o1);
            int s2 = getSimilarity(o2);
            return (s1 == s2) ?  0 : (s1 ==  0) ? -1 : (s2 ==  0) ?  1 : (s1  <  0)
                ? ((s2 < 0) ? 0 : 1) : (s2 <  0) ? -1 : ((s1 < s2) ? -1 : 1);
        }

        protected abstract int getSimilarity(T t);
    }

    private static class DecoderComparator extends SimilarityComparator<IDecoder<?,?>> {
        private DecoderKey key;

        DecoderComparator(DecoderKey key) {
            this.key = key;
        }

        @Override
        protected int getSimilarity(IDecoder<?,?> d) {
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

    private static class EncoderComparator extends SimilarityComparator<IEncoder<?,?>> {
        private EncoderKey key;

        EncoderComparator(EncoderKey key) {
            this.key = key;
        }

        @Override
        protected int getSimilarity(IEncoder<?,?> d) {
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
                return keys.size() == key.getKeys().size()
                        && keys.containsAll(key.getKeys())
                        && key.getKeys().containsAll(keys);
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("%s[%s]", getClass().getSimpleName(),
                    StringHelper.join(", ", keys));
        }

        @Override
        public int hashCode() {
            return hash(11, 13, keys.toArray());
        }

        public boolean matches(Set<EncoderKey> toTest) {
            return toTest != null ? 
            		keys.isEmpty() : 
            			toTest.containsAll(keys);  // FIXME fix NPE here: toTest is null
        }

        @Override
        public int getSimilarity(EncoderKey key) {
            if (key != null && key.getClass() == getClass()) {
                CompositeEncoderKey cek = (CompositeEncoderKey) key;
                if (cek.getKeys().size() != keys.size()) { return -1; }
                int similarity = 0;
                for (EncoderKey k1 : keys) {
                    int s = -1;
                    for (EncoderKey k2 : cek.getKeys()) {
                        int ks = k1.getSimilarity(k2);
                        if ((s = (s < 0) ? ks : Math.min(s, ks)) == 0) { break; }
                    }
                    if (s < 0) { return -1; }
                    else { similarity += s; }
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
                return keys.containsAll(key.getKeys())
                        && key.getKeys().containsAll(keys);
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("%s[%s]", getClass().getSimpleName(),
                    StringHelper.join(", ", keys));
        }

        @Override
        public int hashCode() {
            return hash(11, 13, keys.toArray());
        }

        public boolean matches(Set<DecoderKey> toTest) {
            return toTest != null ? 
            		keys.isEmpty() : 
            			toTest.containsAll(keys); // FIXME fix NPE here: toTest is null
        }

        @Override
        public int getSimilarity(DecoderKey key) {
            if (key != null && key.getClass() == getClass()) {
                CompositeDecoderKey cek = (CompositeDecoderKey) key;
                if (cek.getKeys().size() != keys.size()) { return -1; }
                int similarity = 0;
                for (DecoderKey k1 : keys) {
                    int s = -1;
                    for (DecoderKey k2 : cek.getKeys()) {
                        int ks = k1.getSimilarity(k2);
                        if ((s = (s < 0) ? ks : Math.min(s, ks)) == 0) { break; }
                    }
                    if (s < 0) { return -1; }
                    else { similarity += s; }
                }
                return similarity;
            }
            return -1;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(CodingRepository.class);
	private final ServiceLoader<IDecoder> serviceLoaderDecoder;
    private final ServiceLoader<IEncoder> serviceLoaderEncoder;
    private final Set<IDecoder<?, ?>> decoders;
    private final Set<IEncoder<?, ?>> encoders;
    private final Map<DecoderKey, Set<IDecoder<?, ?>>> decoderByKey = CollectionHelper.map();
    private final Map<EncoderKey, Set<IEncoder<?, ?>>> encoderByKey = CollectionHelper.map();

    public CodingRepository() throws ConfigurationException {
		this.serviceLoaderDecoder = ServiceLoader.load(IDecoder.class);
		this.serviceLoaderEncoder = ServiceLoader.load(IEncoder.class);
        this.decoders = CollectionHelper.asSet(loadDecoders());
        this.encoders = CollectionHelper.asSet(loadEncoders());
        initDecoderMap();
        initEncoderMap();
    }

	public void updateDecoders() throws ConfigurationException {
		log.debug("Reloading Decoder implementations");
		this.decoders.clear();
		this.decoders.addAll(loadDecoders());
		initDecoderMap();
		log.debug("Reloaded Decoder implementations");
	}

	public void updateEncoders() throws ConfigurationException {
		log.debug("Reloading Encoder implementations");
		this.encoders.clear();
		this.encoders.addAll(loadEncoders());
		initEncoderMap();
		log.debug("Reloaded Encoder implementations");
	}

	private List<IDecoder<?,?>> loadDecoders() throws ConfigurationException {
		List<IDecoder<?,?>> loadedDecoders = new LinkedList<IDecoder<?, ?>>();
        try {
            for (IDecoder<?,?> decoder : serviceLoaderDecoder) {
                loadedDecoders.add(decoder);
            }
        } catch (ServiceConfigurationError sce) {
            String text = "An IDecoder implementation could not be loaded!";
            log.warn(text, sce);
            throw new ConfigurationException(text, sce);
        }
		if (loadedDecoders.isEmpty()) {
            String exceptionText = "No IDecoder implementations is loaded!";
            log.error(exceptionText);
            throw new ConfigurationException(exceptionText);
        }
		return loadedDecoders;
	}

	private List<IEncoder<?,?>> loadEncoders() throws ConfigurationException {
		List<IEncoder<?,?>> loadedEncoders = new LinkedList<IEncoder<?, ?>>();
        try {
            for (IEncoder<?,?> encoder : serviceLoaderEncoder) {
                loadedEncoders.add(encoder);
            }
        } catch (ServiceConfigurationError sce) {
            String text = "An IEncoder implementation could not be loaded!";
            log.warn(text, sce);
            throw new ConfigurationException(text, sce);
        }
		if (loadedEncoders.isEmpty()) {
            String exceptionText = "No IEncoder implementations is loaded!";
            log.error(exceptionText);
            throw new ConfigurationException(exceptionText);
        }
		return loadedEncoders;
	}

    public Set<IDecoder<?, ?>> getDecoders() {
        return CollectionHelper.unmodifiableSet(decoders);
    }

    public Set<IEncoder<?, ?>> getEncoders() {
        return CollectionHelper.unmodifiableSet(encoders);
    }

    public Map<DecoderKey, Set<IDecoder<?, ?>>> getDecoderByKey() {
        return CollectionHelper.unmodifiableMap(decoderByKey);
    }

    public Map<EncoderKey, Set<IEncoder<?, ?>>> getEncoderByKey() {
        return CollectionHelper.unmodifiableMap(encoderByKey);
    }

    private void initEncoderMap() {
		this.encoderByKey.clear();
        for (IEncoder<?, ?> encoder : getEncoders()) {
            for (EncoderKey key : encoder.getEncoderKeyType()) {
                Set<IEncoder<?, ?>> encodersForKey = encoderByKey.get(key);
                if (encodersForKey == null) {
                    encoderByKey.put(key, encodersForKey = CollectionHelper.set());
                }
                encodersForKey.add(encoder);
            }
        }
    }

    private void initDecoderMap() {
		this.decoderByKey.clear();
        for (IDecoder<?, ?> decoder : getDecoders()) {
            for (DecoderKey key : decoder.getDecoderKeyTypes()) {
                Set<IDecoder<?, ?>> decodersForKey = decoderByKey.get(key);
                if (decodersForKey == null) {
                    decoderByKey.put(key, decodersForKey = CollectionHelper.set());
                }
                decodersForKey.add(decoder);
            }
        }
    }

    public <F, T> IDecoder<F, T> getDecoder(DecoderKey key, DecoderKey... keys) {
        if (keys.length == 0) {
            return getDecoderSingleKey(key);
        } else {
            return getDecoderCompositeKey(new CompositeDecoderKey(CollectionHelper.asList(key, keys)));
        }
    }

    public <F, T> IEncoder<F, T> getEncoder(EncoderKey key, EncoderKey... keys) {
        if (keys.length == 0) {
            return getEncoderSingleKey(key);
        } else {
            return getEncoderCompositeKey(new CompositeEncoderKey(CollectionHelper.asList(key, keys)));
        }
    }

    private <F, T> IDecoder<F, T> getDecoderSingleKey(DecoderKey key) {
        return processDecoderMatches(findDecodersForSingleKey(key), key);
    }

    private <F, T> IDecoder<F, T> getDecoderCompositeKey(CompositeDecoderKey key) {
        return processDecoderMatches(findDecodersForCompositeKey(key), key);
    }

    private <F, T> IEncoder<F, T> getEncoderSingleKey(EncoderKey key) {
        return processEncoderMatches(findEncodersForSingleKey(key), key);
    }

    private <F, T> IEncoder<F, T> getEncoderCompositeKey(CompositeEncoderKey key) {
        return processEncoderMatches(findEncodersForCompositeKey(key), key);
    }


    @SuppressWarnings("unchecked")
    private static <T> T unsafeCast(Object o) {
        return (T) o;
    }

    private Set<IEncoder<?, ?>> findEncodersForSingleKey(EncoderKey key) {
        Set<IEncoder<?, ?>> matches = encoderByKey.get(key);
        if (matches == null) {
            encoderByKey.put(key, matches = CollectionHelper.set());
            for (IEncoder<?,?> encoder : getEncoders()) {
                for (EncoderKey ek : encoder.getEncoderKeyType()) {
                    if (ek.getSimilarity(key) > 0) {
                        matches.add(encoder);
                    }
                }
            }
        }
        return matches;
    }

    private Set<IDecoder<?, ?>> findDecodersForSingleKey(DecoderKey key) {
        Set<IDecoder<?, ?>> matches = decoderByKey.get(key);
        if (matches == null) {
            decoderByKey.put(key, matches = CollectionHelper.set());
            for (IDecoder<?,?> decoder : getDecoders()) {
                for (DecoderKey dk : decoder.getDecoderKeyTypes()) {
                    if (dk.getSimilarity(key) > 0) {
                        matches.add(decoder);
                    }
                }
            }
        }
        return matches;
    }

    private Set<IEncoder<?, ?>> findEncodersForCompositeKey(CompositeEncoderKey ck) {
        Set<IEncoder<?, ?>> matches = encoderByKey.get(ck);
        if (matches == null) {
            // first request; search for matching encoders and save result for later quries
            encoderByKey.put(ck, matches = CollectionHelper.set());
            for (IEncoder<?, ?> encoder : encoders) {
                if (ck.matches(encoder.getEncoderKeyType())) {
                    matches.add(encoder);
                }
            }
            log.debug("Found {} Encoders for CompositeKey: {}", matches.size(),
                    StringHelper.join(", ", matches));
        }
        return matches;
    }

    private Set<IDecoder<?, ?>> findDecodersForCompositeKey(CompositeDecoderKey ck) {
        Set<IDecoder<?, ?>> matches = decoderByKey.get(ck);
        if (matches == null) {
            // first request; search for matching decoders and save result for later queries
            decoderByKey.put(ck, matches = CollectionHelper.set());
            for (IDecoder<?, ?> decoder : decoders) {
                if (ck.matches(decoder.getDecoderKeyTypes())) {
                    matches.add(decoder);
                }
            }
            log.debug("Found {} Decoders for CompositeKey: {}", matches.size(),
                    StringHelper.join(", ", matches));
        }
        return matches;
    }

    private static <F, T> IDecoder<F, T> processDecoderMatches(Set<IDecoder<?, ?>> matches, DecoderKey key) {
        if (matches == null || matches.isEmpty()) {
            log.debug("No Decoder implementation for {}", key);
            return null;
        } else if (matches.size() > 1) {
            List<IDecoder<?, ?>> list = new ArrayList<IDecoder<?, ?>>(matches);
            Collections.sort(list, new DecoderComparator(key));
            IDecoder<?, ?> dec = list.iterator().next();
            log.warn("Requested ambiguous Decoder implementations for {}: Found {}; Choosing {}.",
                    key, StringHelper.join(", ", matches), dec);
            return unsafeCast(dec);
        } else {
            return unsafeCast(matches.iterator().next());
        }
    }

    private static <F, T> IEncoder<F, T> processEncoderMatches(Set<IEncoder<?, ?>> matches, EncoderKey key) {
        if (matches.isEmpty()) {
            log.debug("No Encoder for {}", key);
            return null;
        } else if (matches.size() > 1) {
            List<IEncoder<?, ?>> list = new ArrayList<IEncoder<?, ?>>(matches);
            Collections.sort(list, new EncoderComparator(key));
            IEncoder<?, ?> enc = list.iterator().next();
            log.warn("Requested ambiguous Encoder implementations for {}: Found {}; Choosing {}.",
                    key, StringHelper.join(", ", matches), enc);
            return unsafeCast(enc);
        } else {
            return unsafeCast(matches.iterator().next());
        }
    }
}
