/**
 * Copyright (C) 2012
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
package org.n52.sos.decode;

import org.n52.sos.decode.kvp.ISosKvpDecoderOperationDelegate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.n52.sos.ogc.ows.OWSConstants.RequestParams;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.KvpHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosKvpDecoder implements IKvpDecoder {

    private static final Logger log = LoggerFactory.getLogger(SosKvpDecoder.class);
    
    private final List<DecoderKeyType> decoderKeyTypes = Collections.unmodifiableList(new ArrayList<DecoderKeyType>(6) {{
        // TODO is this really needed, what about additional namespaces?
        /* catch all SOS request and delegate to IKvpOperationDecoder implementations */
        add(new DecoderKeyType(SosConstants.SOS, Sos1Constants.SERVICEVERSION));
        add(new DecoderKeyType(SosConstants.SOS, Sos2Constants.SERVICEVERSION));
        add(new DecoderKeyType(SosConstants.SOS, null));
        add(new DecoderKeyType(Sos1Constants.NS_SOS));
        add(new DecoderKeyType(Sos2Constants.NS_SOS_20));
        add(new DecoderKeyType(SWEConstants.NS_SWES_20));
    }});
    
    private Set<String> conformanceClasses;
    private Set<RequestDecoderKey> requestDecoderKeys;
    private Set<ISosKvpDecoderOperationDelegate> implementations;
    
    private ISosKvpDecoderOperationDelegate getDecoder(RequestDecoderKey toDecode) {
        log.debug("Searching decoder for {}", toDecode);
        for (ISosKvpDecoderOperationDelegate dec : getDelegates()) {
            for (RequestDecoderKey key : dec.getRequestDecoderKeys()) {
                log.debug("Checking key {}", key);
                if (key.isCompatible(toDecode)) {
                    log.debug("Found KVP decoder: {}", dec);
                    return dec;
                }
            }
        }
        return null;
    }
    
    private Iterable<ISosKvpDecoderOperationDelegate> getDelegates() {
        if (implementations == null) {
            LinkedList<ISosKvpDecoderOperationDelegate> impls = new LinkedList<ISosKvpDecoderOperationDelegate>();
            for (ISosKvpDecoderOperationDelegate dec : ServiceLoader.load(ISosKvpDecoderOperationDelegate.class)) {
                for (RequestDecoderKey key : dec.getRequestDecoderKeys()) {
                    log.debug("Registering KVP decoder {} for {}", dec, key);
                }
                impls.add(dec);
            }
            implementations = Collections.unmodifiableSet(new HashSet<ISosKvpDecoderOperationDelegate>(impls));
        }
        return implementations;
    }

    public SosKvpDecoder() {
        StringBuilder logMsgBuilder = new StringBuilder();
        logMsgBuilder.append("Decoder for the following namespaces initialized successfully: ");
        for (DecoderKeyType decoderKeyType : decoderKeyTypes) {
            logMsgBuilder.append(decoderKeyType.toString());
            logMsgBuilder.append(", ");
        }
        logMsgBuilder.delete(logMsgBuilder.lastIndexOf(", "), logMsgBuilder.length());
        logMsgBuilder.append("!");
        log.info(logMsgBuilder.toString());
    }

    @Override
    public List<DecoderKeyType> getDecoderKeyTypes() {
        return Collections.unmodifiableList(decoderKeyTypes);
    }

    @Override
    public AbstractServiceRequest decode(Map<String, String> element) throws OwsExceptionReport {
        String service = null;
        String version = null;
        String operation = null;

        for (String parameterName : element.keySet()) {
            if (parameterName.equalsIgnoreCase(RequestParams.request.name())) {
                operation = KvpHelper.checkParameterSingleValue(element.get(parameterName), parameterName);
            } else if (parameterName.equalsIgnoreCase(RequestParams.service.name())) {
                service = KvpHelper.checkParameterSingleValue(element.get(parameterName), parameterName);
            } else if (parameterName.equalsIgnoreCase(RequestParams.version.name())) {
                version = KvpHelper.checkParameterSingleValue(element.get(parameterName), parameterName);
            }
        }
        if (service == null && KvpHelper.checkForGetCapabilities(element)) {
            service = SosConstants.SOS;
        }
        
        RequestDecoderKey key = new RequestDecoderKey(service, version, operation);
        ISosKvpDecoderOperationDelegate decoder = getDecoder(key);
        
        if (decoder == null) {
            throw Util4Exceptions.createOperationNotSupportedException(operation);
        }
        
        return decoder.decode(key, element);
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return Collections.emptyMap();
    }
    
    @Override
    public Set<String> getConformanceClasses() {
        if (conformanceClasses == null) {
            conformanceClasses = new HashSet<String>();
            for (ISosKvpDecoderOperationDelegate dec : getDelegates()) {
                conformanceClasses.addAll(dec.getConformanceClasses());
            }
        }
        return conformanceClasses;
    }
    
    @Override
    public boolean isSupported(RequestDecoderKey key) {
        return getDecoder(key) != null;
    }
    
    @Override
    public Set<RequestDecoderKey> getRequestDecoderKeys() {
        if (requestDecoderKeys == null) {
            requestDecoderKeys = new HashSet<RequestDecoderKey>();
            for (ISosKvpDecoderOperationDelegate delegate : getDelegates()) {
                requestDecoderKeys.addAll(delegate.getRequestDecoderKeys());
            }
        }
        return requestDecoderKeys;
    }
}