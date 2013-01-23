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
import java.util.Collection;
import java.util.Map;
import javax.wsdl.WSDLException;
import org.n52.sos.binding.Binding;
import org.n52.sos.decode.OperationDecoderKey;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.request.operator.IRequestOperator;
import org.n52.sos.request.operator.IWSDLAwareRequestOperator;
import org.n52.sos.request.operator.RequestOperatorKeyType;
import org.n52.sos.service.Configurator;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class WSDLProvider {

    private static final String SOAP_BINDING_ENDPOINT = "/soap";
    private static final String POX_BINDING_ENDPOINT = "/pox";
    private static final String KVP_BINDING_ENDPOINT = "/kvp";

    public String getWSDL() throws OwsExceptionReport, WSDLException {
        WSDLBuilder builder = new WSDLBuilder();
        if (Configurator.getInstance() != null) {
            Map<String, Binding> bindings = Configurator.getInstance()
                    .getBindingRepository().getBindings();

            Collection<IRequestOperator> requestOperators = Configurator.getInstance()
                    .getRequestOperatorRepository().getRequestOperator().values();
            String serviceUrl = Configurator.getInstance().getServiceURL();
            
            if (bindings.containsKey(SOAP_BINDING_ENDPOINT)) {
                builder.setSoapEndpoint(URI.create(serviceUrl + SOAP_BINDING_ENDPOINT));
                Binding b = bindings.get(SOAP_BINDING_ENDPOINT);
                for (IRequestOperator o : requestOperators) {
                    if (o instanceof IWSDLAwareRequestOperator) {
                        IWSDLAwareRequestOperator op = (IWSDLAwareRequestOperator) o;
                        if (op.getSosOperationDefinition() != null) {
                            if (isHttpPostSupported(b, o)) {
                                builder.addSoapOperation(op.getSosOperationDefinition());
                            }
                            addAdditionalPrefixes(op, builder);
                            addAdditionalSchemaImports(op, builder);
                        }
                    }
                }
            }
            if (bindings.containsKey(POX_BINDING_ENDPOINT)) {
                builder.setPoxEndpoint(URI.create(serviceUrl + POX_BINDING_ENDPOINT));
                Binding b = bindings.get(POX_BINDING_ENDPOINT);
                for (IRequestOperator o : requestOperators) {
                    if (o instanceof IWSDLAwareRequestOperator) {
                        IWSDLAwareRequestOperator op = (IWSDLAwareRequestOperator) o;
                        if (op.getSosOperationDefinition() != null) {
                            if (isHttpPostSupported(b, o)) {
                                builder.addPoxOperation(op.getSosOperationDefinition());
                            }
                            addAdditionalPrefixes(op, builder);
                            addAdditionalSchemaImports(op, builder);
                        }
                    }
                }
            }
            if (bindings.containsKey(KVP_BINDING_ENDPOINT)) {
                builder.setKvpEndpoint(URI.create(serviceUrl + KVP_BINDING_ENDPOINT + "?"));
                Binding b = bindings.get(KVP_BINDING_ENDPOINT);
                for (IRequestOperator o : requestOperators) {
                    if (o instanceof IWSDLAwareRequestOperator) {
                        IWSDLAwareRequestOperator op = (IWSDLAwareRequestOperator) o;
                        if (op.getSosOperationDefinition() != null) {
                            if (isHttpGetSupported(b, o)) {
                                builder.addKvpOperation(op.getSosOperationDefinition());
                            }
                            addAdditionalPrefixes(op, builder);
                            addAdditionalSchemaImports(op, builder);
                        }
                    }
                }
            }
        }
        return builder.build();
    }

    private OperationDecoderKey toOperationDecoderKey(RequestOperatorKeyType requestOperatorKeyType) {
        return new OperationDecoderKey(requestOperatorKeyType.getServiceOperatorKeyType()
                .getService(), requestOperatorKeyType.getServiceOperatorKeyType().getVersion(),
                requestOperatorKeyType.getOperationName());
    }

    private void addAdditionalPrefixes(IWSDLAwareRequestOperator op, WSDLBuilder builder) {
        Map<String, String> additionalPrefixes = op.getAdditionalPrefixes();
        if (additionalPrefixes != null) {
            for (Map.Entry<String,String> ap : additionalPrefixes.entrySet()) {
                builder.addNamespace(ap.getKey(), ap.getValue());
            }
        }
    }

    private void addAdditionalSchemaImports(IWSDLAwareRequestOperator op, WSDLBuilder builder) throws WSDLException {
        Map<String, String> additionalSchemaImports = op.getAdditionalSchemaImports();
        if (additionalSchemaImports != null) {
            for (Map.Entry<String,String> as : additionalSchemaImports.entrySet()) {
                builder.addSchemaImport(as.getKey(), as.getValue());
            }
        }
    }

    private boolean isHttpPostSupported(Binding b, IRequestOperator ro) throws OwsExceptionReport {
        return b.checkOperationHttpPostSupported(toOperationDecoderKey(ro.getRequestOperatorKeyType()));
    }

    private boolean isHttpGetSupported(Binding b, IRequestOperator ro) throws OwsExceptionReport {
        return b.checkOperationHttpGetSupported(toOperationDecoderKey(ro.getRequestOperatorKeyType()));
    }
}
