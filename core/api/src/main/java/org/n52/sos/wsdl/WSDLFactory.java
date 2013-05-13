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
package org.n52.sos.wsdl;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import javax.wsdl.WSDLException;

import org.n52.sos.binding.Binding;
import org.n52.sos.binding.BindingConstants;
import org.n52.sos.binding.BindingRepository;
import org.n52.sos.decode.OperationDecoderKey;
import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.exception.HTTPException;
import org.n52.sos.request.operator.RequestOperator;
import org.n52.sos.request.operator.RequestOperatorKeyType;
import org.n52.sos.request.operator.RequestOperatorRepository;
import org.n52.sos.request.operator.WSDLAwareRequestOperator;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.Producer;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class WSDLFactory implements Producer<String> {
    @Override
    public String get() throws ConfigurationException {
        try {
            return getWSDL();
        } catch (final Exception ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    private String getWSDL() throws HTTPException, WSDLException {
        final WSDLBuilder builder = new WSDLBuilder();
        if (Configurator.getInstance() != null) {
            final Map<String, Binding> bindings = BindingRepository.getInstance().getBindings();
            final RequestOperatorRepository repo = RequestOperatorRepository.getInstance();

            final Set<RequestOperatorKeyType> requestOperators = repo.getActiveRequestOperatorKeyTypes();

            final String serviceUrl = Configurator.getInstance().getServiceURL();
            
            if (bindings.containsKey(BindingConstants.SOAP_BINDING_ENDPOINT)) {
                builder.setSoapEndpoint(URI.create(serviceUrl + BindingConstants.SOAP_BINDING_ENDPOINT));
                final Binding b = bindings.get(BindingConstants.SOAP_BINDING_ENDPOINT);
                for (final RequestOperatorKeyType o : requestOperators) {
                    final RequestOperator op = repo.getRequestOperator(o);
                    if (op instanceof WSDLAwareRequestOperator) {
                        final WSDLAwareRequestOperator wop = (WSDLAwareRequestOperator) op;
                        if (wop.getSosOperationDefinition() != null) {
                            if (isHttpPostSupported(b, wop)) {
                                builder.addSoapOperation(wop.getSosOperationDefinition());
                            }
                            addAdditionalPrefixes(wop, builder);
                            addAdditionalSchemaImports(wop, builder);
                        }
                    }
                }
            }
            if (bindings.containsKey(BindingConstants.POX_BINDING_ENDPOINT)) {
                builder.setPoxEndpoint(URI.create(serviceUrl + BindingConstants.POX_BINDING_ENDPOINT));
                final Binding b = bindings.get(BindingConstants.POX_BINDING_ENDPOINT);
                for (final RequestOperatorKeyType o : requestOperators) {
                    final RequestOperator op = repo.getRequestOperator(o);
                    if (op instanceof WSDLAwareRequestOperator) {
                        final WSDLAwareRequestOperator wop = (WSDLAwareRequestOperator) op;
                        if (wop.getSosOperationDefinition() != null) {
                            if (isHttpPostSupported(b, wop)) {
                                builder.addPoxOperation(wop.getSosOperationDefinition());
                            }
                            addAdditionalPrefixes(wop, builder);
                            addAdditionalSchemaImports(wop, builder);
                        }
                    }
                }
            }
            if (bindings.containsKey(BindingConstants.KVP_BINDING_ENDPOINT)) {
                builder.setKvpEndpoint(URI.create(serviceUrl + BindingConstants.KVP_BINDING_ENDPOINT + "?"));
                final Binding b = bindings.get(BindingConstants.KVP_BINDING_ENDPOINT);
                for (final RequestOperatorKeyType o : requestOperators) {
                    final RequestOperator op = repo.getRequestOperator(o);
                    if (op instanceof WSDLAwareRequestOperator) {
                        final WSDLAwareRequestOperator wop = (WSDLAwareRequestOperator) op;
                        if (wop.getSosOperationDefinition() != null) {
                            if (isHttpGetSupported(b, wop)) {
                                builder.addKvpOperation(wop.getSosOperationDefinition());
                            }
                            addAdditionalPrefixes(wop, builder);
                            addAdditionalSchemaImports(wop, builder);
                        }
                    }
                }
            }
        }
        return builder.build();
    }

    private OperationDecoderKey toOperationDecoderKey(final RequestOperatorKeyType requestOperatorKeyType) {
        return new OperationDecoderKey(requestOperatorKeyType.getServiceOperatorKeyType()
                .getService(), requestOperatorKeyType.getServiceOperatorKeyType().getVersion(),
                requestOperatorKeyType.getOperationName());
    }

    private void addAdditionalPrefixes(final WSDLAwareRequestOperator op, final WSDLBuilder builder) {
        final Map<String, String> additionalPrefixes = op.getAdditionalPrefixes();
        if (additionalPrefixes != null) {
            for (final Map.Entry<String,String> ap : additionalPrefixes.entrySet()) {
                builder.addNamespace(ap.getKey(), ap.getValue());
            }
        }
    }

    private void addAdditionalSchemaImports(final WSDLAwareRequestOperator op, final WSDLBuilder builder) throws WSDLException {
        final Map<String, String> additionalSchemaImports = op.getAdditionalSchemaImports();
        if (additionalSchemaImports != null) {
            for (final Map.Entry<String,String> as : additionalSchemaImports.entrySet()) {
                builder.addSchemaImport(as.getKey(), as.getValue());
            }
        }
    }

    private boolean isHttpPostSupported(final Binding b, final RequestOperator ro) throws HTTPException {
        return b.checkOperationHttpPostSupported(toOperationDecoderKey(ro.getRequestOperatorKeyType()));
    }

    private boolean isHttpGetSupported(final Binding b, final RequestOperator ro) throws HTTPException{
        return b.checkOperationHttpGetSupported(toOperationDecoderKey(ro.getRequestOperatorKeyType()));
    }
}
