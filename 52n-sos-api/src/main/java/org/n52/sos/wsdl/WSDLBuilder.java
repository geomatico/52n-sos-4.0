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

import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.http.HTTPOperation;
import javax.wsdl.extensions.http.HTTPUrlEncoded;
import javax.wsdl.extensions.mime.MIMEMimeXml;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaReference;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPFault;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.wsdl.WSDLConstants.Operations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class WSDLBuilder {

    private static final String SOAP_LITERAL_USE = "literal";
    private static final String REQUEST_SUFFIX = "RequestMessage";
    private static final String RESPONSE_SUFFIX = "ResponseMessage";
    private static final String SOS_SOAP_PORT = "SosSoapPort";
    private static final String SOS_KVP_PORT = "SosKvpPort";
    private static final String SOS_POX_PORT = "SosPoxPort";
    
    private final WSDLFactory factory;
    private final ExtensionRegistry extensionRegistry;
    private final Definition definitions;
    private Message faultMessage;
    private Service service;
    private Types types;
    private PortType postPortType, getPortType;
    private Binding soapBinding, kvpBinding, poxBinding;
    private Port soapPort, kvpPort, poxPort;
    private URI soapEndpoint, poxEndpoint, kvpEndpoint;

    public WSDLBuilder() throws WSDLException {
        this.factory = WSDLFactory.newInstance();
        this.extensionRegistry = getFactory().newPopulatedExtensionRegistry();
        this.definitions = getFactory().newDefinition();
        this.setDefaultNamespaces();
        this.setDefaultImports();
    }
    
    private WSDLFactory getFactory() {
        return this.factory;
    }

    private ExtensionRegistry getExtensionRegistry() {
        return this.extensionRegistry;
    }

    private Definition getDefinitions() {
        return this.definitions;
    }

    private Input createInput(URI action, Message message) {
        Input input = getDefinitions().createInput();
        input.setExtensionAttribute(WSDLConstants.QN_WSAM_ACTION, action.toString());
        input.setName(message.getQName().getLocalPart());
        input.setMessage(message);
        return input;
    }

    private Output createOutput(URI action, Message message) {
        Output output = getDefinitions().createOutput();
        output.setExtensionAttribute(WSDLConstants.QN_WSAM_ACTION, action.toString());
        output.setName(message.getQName().getLocalPart());
        output.setMessage(message);
        return output;
    }

    private Fault createFault(String name, URI action, Message message) {
        Fault fault = getDefinitions().createFault();
        fault.setExtensionAttribute(WSDLConstants.QN_WSAM_ACTION, action.toString());
        fault.setName(name);
        fault.setMessage(message);
        return fault;
    }

    private Fault createFault(WSDLFault fault) {
        return createFault(fault.getName(), fault.getAction());
    }

    private Fault createFault(String name, URI action) {
        return createFault(name, action, getFaultMessage());
    }

    private Operation addPostOperation(String name, QName request, QName response, URI requestAction, URI responseAction, Collection<Fault> faults) {
        Message requestMessage = createMessage(name + REQUEST_SUFFIX, request);
        Message responseMessage = createMessage(name + RESPONSE_SUFFIX, response);
        Input input = createInput(requestAction, requestMessage);
        Output output = createOutput(responseAction, responseMessage);
        return addOperation(getPostPortType(), name, input, output, faults);
    }
    
    private Operation addGetOperation(String name, QName request, QName response, URI requestAction, URI responseAction, Collection<Fault> faults) {
        Message requestMessage = createMessage(name + REQUEST_SUFFIX, request);
        Message responseMessage = createMessage(name + RESPONSE_SUFFIX, response);
        Input input = createInput(requestAction, requestMessage);
        Output output = createOutput(responseAction, responseMessage);
        return addOperation(getGetPortType(), name, input, output, faults);
    }

    private Operation addOperation(PortType portType, String name, Input input, Output output, Collection<Fault> faults) {
        Operation operation = portType.getOperation(name, input.getName(), output.getName());
        if (operation == null) {
            operation = getDefinitions().createOperation();
            operation.setName(name);
            operation.setInput(input);
            operation.setOutput(output);
            operation.setUndefined(false);
            for (Fault fault : faults) {
                operation.addFault(fault);
            }
            portType.addOperation(operation);
        }
        return operation;
    }

    private PortType getPostPortType() {
        if (this.postPortType == null) {
            this.postPortType = getDefinitions().createPortType();
            this.postPortType.setQName(WSDLConstants.QN_SOSW_POST_PORT_TYPE);
            this.postPortType.setUndefined(false);
            getDefinitions().addPortType(this.postPortType);
        }
        return this.postPortType;
    }
    
    private PortType getGetPortType() {
        if (this.getPortType == null) {
            this.getPortType = getDefinitions().createPortType();
            this.getPortType.setQName(WSDLConstants.QN_SOSW_GET_PORT_TYPE);
            this.getPortType.setUndefined(false);
            getDefinitions().addPortType(this.getPortType);
        }
        return this.getPortType;
    }

    private Types getTypes() {
        if (this.types == null) {
            this.types = getDefinitions().createTypes();
            getDefinitions().setTypes(this.types);
        }
        return this.types;
    }

    private Service getService() {
        if (this.service == null) {
            this.service = getDefinitions().createService();
            this.service.setQName(WSDLConstants.QN_SOSW_SERVICE);
            getDefinitions().addService(this.service);
        }
        return this.service;
    }

    private void setDefaultImports() throws WSDLException {
        addSchemaImport(Sos2Constants.NS_SOS_20, Sos2Constants.SCHEMA_LOCATION_SOS);
        addSchemaImport(OWSConstants.NS_OWS, OWSConstants.SCHEMA_LOCATION_OWS);
        addSchemaImport(SWEConstants.NS_SWES_20, SWEConstants.SCHEMA_LOCATION_SWES_200);
    }

    public WSDLBuilder addSchemaImport(String namespace, String schemaLocation) throws WSDLException {
        getTypes().addExtensibilityElement(createSchemaImport(namespace, schemaLocation));
        return this;
    }

    private void setDefaultNamespaces() {
        getDefinitions().setTargetNamespace(WSDLConstants.NS_SOSW);
        addNamespace(WSDLConstants.NS_SOSW_PREFIX, WSDLConstants.NS_SOSW);
        addNamespace(WSDLConstants.NS_XSD_PREFIX, WSDLConstants.NS_XSD);
        addNamespace(WSDLConstants.NS_WSDL_PREFIX, WSDLConstants.NS_WSDL);
        addNamespace(WSDLConstants.NS_SOAP_PREFIX, WSDLConstants.NS_SOAP);
        addNamespace(WSDLConstants.NS_WSAM_PREFIX, WSDLConstants.NS_WSAM);
        addNamespace(WSDLConstants.NS_MIME_PREFIX, WSDLConstants.NS_MIME);
        addNamespace(WSDLConstants.NS_HTTP_PREFIX, WSDLConstants.NS_HTTP);
        addNamespace(OWSConstants.NS_OWS_PREFIX, OWSConstants.NS_OWS);
        addNamespace(SosConstants.NS_SOS_PREFIX, Sos2Constants.NS_SOS_20);
        addNamespace(SWEConstants.NS_SWES_PREFIX, SWEConstants.NS_SWES_20);
    }

    public WSDLBuilder addNamespace(String prefix, String namespace) {
        getDefinitions().addNamespace(prefix, namespace);
        return this;
    }

    private Message createMessage(String name, QName qname) {
        Message message = getDefinitions().createMessage();
        Part part = getDefinitions().createPart();
        part.setElementName(qname);
        part.setName(WSDLConstants.MESSAGE_PART);
        message.addPart(part);
        message.setQName(new QName(WSDLConstants.NS_SOSW, name));
        message.setUndefined(false);
        getDefinitions().addMessage(message);
        return message;
    }

    private Message getFaultMessage() {
        if (this.faultMessage == null) {
            this.faultMessage = getDefinitions().createMessage();
            Part part = getDefinitions().createPart();
            part.setElementName(OWSConstants.QN_EXCEPTION);
            part.setName("fault");
            this.faultMessage.addPart(part);
            this.faultMessage.setQName(new QName(WSDLConstants.NS_SOSW, "ExceptionMessage"));
            this.faultMessage.setUndefined(false);
            getDefinitions().addMessage(this.faultMessage);
        }
        return this.faultMessage;
    }

    private ExtensibilityElement createSchemaImport(String namespace, String schemaLocation) throws WSDLException {
        Schema schema = (Schema) getExtensionRegistry().createExtension(Types.class, WSDLConstants.QN_XSD_SCHEMA);
        SchemaReference ref = schema.createInclude();
        ref.setReferencedSchema(schema);
        ref.setSchemaLocationURI(schemaLocation);
        ref.setId(namespace);
        schema.setElementType(WSDLConstants.QN_XSD_SCHEMA);
        schema.setElement(buildSchemaImport(namespace, schemaLocation));
        schema.addInclude(ref);
        return schema;
    }

    private Element buildSchemaImport(String namespace, String schemaLocation) throws WSDLException {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = documentFactory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element schema = document.createElementNS(WSDLConstants.NS_XSD, WSDLConstants.EN_XSD_SCHEMA);
            Element include = document.createElementNS(WSDLConstants.NS_XSD, WSDLConstants.EN_XSD_INCLUDE);
            include.setAttribute(WSDLConstants.AN_XSD_SCHEMA_LOCATION, schemaLocation);
            include.setPrefix(WSDLConstants.NS_XSD_PREFIX);
            schema.setAttribute(WSDLConstants.AN_XSD_TARGET_NAMESPACE, namespace);
            schema.setAttribute(WSDLConstants.AN_XSD_ELEMENT_FORM_DEFAULT, WSDLConstants.QUALIFIED_ELEMENT_FORM_DEFAULT);
            schema.setPrefix(WSDLConstants.NS_XSD_PREFIX);
            schema.appendChild(include);
            return schema;
        } catch (ParserConfigurationException ex) {
            throw new WSDLException(WSDLException.CONFIGURATION_ERROR, ex.getMessage(), ex);
        }
    }

    public String build() throws WSDLException {
        WSDLWriter wsdlWriter = getFactory().newWSDLWriter();
        StringWriter writer = new StringWriter();
        wsdlWriter.writeWSDL(getDefinitions(), writer);
        return writer.toString();
    }

    public WSDLBuilder setSoapEndpoint(URI endpoint) {
        this.soapEndpoint = endpoint;
        return this;
    }

    public WSDLBuilder setPoxEndpoint(URI endpoint) {
        this.poxEndpoint = endpoint;
        return this;
    }

    public WSDLBuilder setKvpEndpoint(URI endpoint) {
        this.kvpEndpoint = endpoint;
        return this;
    }

    private URI getSoapEndpoint() {
        return this.soapEndpoint;
    }

    private URI getKvpEndpoint() {
        return this.kvpEndpoint;
    }

    private URI getPoxEndpoint() {
        return this.poxEndpoint;
    }
    
    private String getName(WSDLOperation o) {
        return o.getName() + ((o.getVersion() != null) ? o.getVersion().replace(".", "") : "");
    }

    public WSDLBuilder addPoxOperation(WSDLOperation o) throws WSDLException {
        List<Fault> faults = new ArrayList<Fault>(o.getFaults().size());
        for (WSDLFault f : o.getFaults()) {
            faults.add(createFault(f));
        }
        return addPoxOperation(getName(o), o.getRequest(), o.getResponse(), o.getRequestAction(),
                o.getResponseAction(), faults);
    }

    public WSDLBuilder addKvpOperation(WSDLOperation o) throws WSDLException {
        List<Fault> faults = new ArrayList<Fault>(o.getFaults().size());
        for (WSDLFault f : o.getFaults()) {
            faults.add(createFault(f));
        }
        return addKvpOperation(getName(o), o.getRequest(), o.getResponse(), o.getRequestAction(),
                o.getResponseAction(), faults);
    }

    public WSDLBuilder addSoapOperation(WSDLOperation o) throws WSDLException {
        List<Fault> faults = new ArrayList<Fault>(o.getFaults().size());
        for (WSDLFault f : o.getFaults()) {
            faults.add(createFault(f));
        }
        return addSoapOperation(getName(o), o.getRequest(), o.getResponse(),
                o.getRequestAction(), o.getResponseAction(), faults);
    }

    private WSDLBuilder addSoapOperation(String name, QName request, QName response, URI requestAction, 
            URI responseAction, Collection<Fault> faults) throws WSDLException {
        Operation operation = addPostOperation(name, request, response, requestAction, responseAction, faults);
        addSoapBindingOperation(name, operation, requestAction, faults);
        addSoapPort();
        return this;
    }

    private WSDLBuilder addPoxOperation(String name, QName request, QName response, URI requestAction, 
            URI responseAction, Collection<Fault> faults) throws WSDLException {
        Operation operation = addPostOperation(name, request, response, requestAction, responseAction, faults);
        addPoxBindingOperation(name, operation, faults);
        addPoxPort();
        return this;
    }

    private WSDLBuilder addKvpOperation(String name, QName request, QName response, URI requestAction, 
            URI responseAction, Collection<Fault> faults) throws WSDLException {
        Operation operation = addGetOperation(name, request, response, requestAction, responseAction, faults);
        addKvpBindingOperation(name, operation, faults);
        addKvpPort();
        return this;
    }

    private void addSoapPort() throws WSDLException {
        if (this.soapPort == null) {
            this.soapPort = getDefinitions().createPort();
            this.soapPort.setBinding(getSoapBinding());
            this.soapPort.setName(SOS_SOAP_PORT);
            SOAPAddress soapAddress = (SOAPAddress) getExtensionRegistry()
                    .createExtension(Port.class, WSDLConstants.QN_SOAP_ADDRESS);
            soapAddress.setLocationURI(getSoapEndpoint().toString());
            this.soapPort.addExtensibilityElement(soapAddress);
            getService().addPort(this.soapPort);
        }
    }

    private void addPoxPort() throws WSDLException {
        if (this.poxPort == null) {
            this.poxPort = getDefinitions().createPort();
            this.poxPort.setBinding(getPoxBinding());
            this.poxPort.setName(SOS_POX_PORT);
            HTTPAddress httpAddress = (HTTPAddress) getExtensionRegistry()
                    .createExtension(Port.class, WSDLConstants.QN_HTTP_ADDRESS);
            httpAddress.setLocationURI(getPoxEndpoint().toString());
            this.poxPort.addExtensibilityElement(httpAddress);
            getService().addPort(this.poxPort);
        }
    }

    private void addKvpPort() throws WSDLException {
        if (this.kvpPort == null) {
            this.kvpPort = getDefinitions().createPort();
            this.kvpPort.setBinding(getKvpBinding());
            this.kvpPort.setName(SOS_KVP_PORT);
            HTTPAddress httpAddress = (HTTPAddress) getExtensionRegistry()
                    .createExtension(Port.class, WSDLConstants.QN_HTTP_ADDRESS);
            httpAddress.setLocationURI(getKvpEndpoint().toString());
            this.kvpPort.addExtensibilityElement(httpAddress);
            getService().addPort(this.kvpPort);
        }
    }

    private BindingOperation addSoapBindingOperation(String name, Operation operation, URI action, Collection<Fault> faults) throws WSDLException {
        BindingOperation bindingOperation = getDefinitions().createBindingOperation();
        bindingOperation.setName(name);

        SOAPOperation soapOperation = (SOAPOperation) getExtensionRegistry().
                createExtension(BindingOperation.class, WSDLConstants.QN_SOAP_OPERATION);
        soapOperation.setStyle(WSDLConstants.SOAP_DOCUMENT_STYLE);
        soapOperation.setSoapActionURI(action.toString());
        bindingOperation.addExtensibilityElement(soapOperation);

        bindingOperation.setOperation(operation);

        BindingInput bindingInput = getDefinitions().createBindingInput();
        SOAPBody bindingInputSoapBody = (SOAPBody) getExtensionRegistry()
                .createExtension(BindingInput.class, WSDLConstants.QN_SOAP_BODY);
        bindingInputSoapBody.setUse(SOAP_LITERAL_USE);
        bindingInput.addExtensibilityElement(bindingInputSoapBody);
        bindingOperation.setBindingInput(bindingInput);

        BindingOutput bindingOutput = getDefinitions().createBindingOutput();
        SOAPBody bindingOutputSoapBody = (SOAPBody) getExtensionRegistry()
                .createExtension(BindingInput.class, WSDLConstants.QN_SOAP_BODY);
        bindingOutputSoapBody.setUse(SOAP_LITERAL_USE);
        bindingOutput.addExtensibilityElement(bindingOutputSoapBody);
        bindingOperation.setBindingOutput(bindingOutput);

        for (Fault fault : faults) {
            BindingFault bindingFault = getDefinitions().createBindingFault();
            bindingFault.setName(fault.getName());
            SOAPFault soapFault = (SOAPFault) getExtensionRegistry()
                    .createExtension(BindingFault.class, WSDLConstants.QN_SOAP_FAULT);
            soapFault.setUse(SOAP_LITERAL_USE);
            soapFault.setName(fault.getName());
            bindingFault.addExtensibilityElement(soapFault);    
            bindingOperation.addBindingFault(bindingFault);
        }
        
        getSoapBinding().addBindingOperation(bindingOperation);
        return bindingOperation;
    }

    private BindingOperation addPoxBindingOperation(String name, Operation operation, Collection<Fault> faults) throws WSDLException {
        BindingOperation bindingOperation = getDefinitions().createBindingOperation();
        bindingOperation.setName(name);
        bindingOperation.setOperation(operation);

        HTTPOperation httpOperation = (HTTPOperation) getExtensionRegistry().
                createExtension(BindingOperation.class, WSDLConstants.QN_HTTP_OPERATION);
        httpOperation.setLocationURI("/");
        bindingOperation.addExtensibilityElement(httpOperation);

        BindingInput bindingInput = getDefinitions().createBindingInput();
        MIMEMimeXml inputmime = (MIMEMimeXml) getExtensionRegistry()
                .createExtension(BindingInput.class, WSDLConstants.QN_MIME_MIME_XML);
        bindingInput.addExtensibilityElement(inputmime);

        bindingOperation.setBindingInput(bindingInput);

        BindingOutput bindingOutput = getDefinitions().createBindingOutput();

        MIMEMimeXml outputmime = (MIMEMimeXml) getExtensionRegistry()
                .createExtension(BindingInput.class, WSDLConstants.QN_MIME_MIME_XML);
        bindingOutput.addExtensibilityElement(outputmime);

        bindingOperation.setBindingOutput(bindingOutput);

                
        for (Fault fault : faults) {
            BindingFault bindingFault = getDefinitions().createBindingFault();
            bindingFault.setName(fault.getName());
            bindingOperation.addBindingFault(bindingFault);
        }
        
        getPoxBinding().addBindingOperation(bindingOperation);
        return bindingOperation;
    }

    private BindingOperation addKvpBindingOperation(String name, Operation operation, Collection<Fault> faults) throws WSDLException {
        BindingOperation bindingOperation = getDefinitions().createBindingOperation();
        bindingOperation.setName(name);
        bindingOperation.setOperation(operation);

        HTTPOperation httpOperation = (HTTPOperation) getExtensionRegistry().
                createExtension(BindingOperation.class, WSDLConstants.QN_HTTP_OPERATION);
        httpOperation.setLocationURI("/");
        bindingOperation.addExtensibilityElement(httpOperation);

        BindingInput bindingInput = getDefinitions().createBindingInput();
        HTTPUrlEncoded urlEncoded = (HTTPUrlEncoded) getExtensionRegistry()
                .createExtension(BindingInput.class, WSDLConstants.QN_HTTP_URL_ENCODED);
        bindingInput.addExtensibilityElement(urlEncoded);

        bindingOperation.setBindingInput(bindingInput);

        BindingOutput bindingOutput = getDefinitions().createBindingOutput();

        MIMEMimeXml mimeXml = (MIMEMimeXml) getExtensionRegistry()
                .createExtension(BindingInput.class, WSDLConstants.QN_MIME_MIME_XML);
        bindingOutput.addExtensibilityElement(mimeXml);

        bindingOperation.setBindingOutput(bindingOutput);
        
        for (Fault fault : faults) {
            BindingFault bindingFault = getDefinitions().createBindingFault();
            bindingFault.setName(fault.getName());
            bindingOperation.addBindingFault(bindingFault);
        }

        getKvpBinding().addBindingOperation(bindingOperation);
        return bindingOperation;
    }

    private Binding getSoapBinding() throws WSDLException {
        if (this.soapBinding == null) {
            this.soapBinding = getDefinitions().createBinding();
            SOAPBinding sb = (SOAPBinding) getExtensionRegistry()
                    .createExtension(Binding.class, WSDLConstants.QN_SOAP_BINDING);
            sb.setStyle(WSDLConstants.SOAP_DOCUMENT_STYLE);
            sb.setTransportURI(WSDLConstants.SOAP_BINDING_HTTP_TRANSPORT);
            this.soapBinding.addExtensibilityElement(sb);
            this.soapBinding.setPortType(getPostPortType());
            this.soapBinding.setQName(WSDLConstants.QN_SOSW_SOAP_BINDING);
            this.soapBinding.setUndefined(false);

            getDefinitions().addBinding(this.soapBinding);
        }
        return this.soapBinding;
    }

    private Binding getPoxBinding() throws WSDLException {
        if (this.poxBinding == null) {
            this.poxBinding = getDefinitions().createBinding();
            this.poxBinding.setPortType(getPostPortType());
            this.poxBinding.setQName(WSDLConstants.QN_SOSW_POX_BINDING);
            this.poxBinding.setUndefined(false);
            HTTPBinding hb = (HTTPBinding) getExtensionRegistry()
                    .createExtension(Binding.class, WSDLConstants.QN_HTTP_BINDING);
            hb.setVerb(WSDLConstants.POX_HTTP_VERB);
            this.poxBinding.addExtensibilityElement(hb);
            getDefinitions().addBinding(this.poxBinding);
        }
        return this.poxBinding;
    }

    private Binding getKvpBinding() throws WSDLException {
        if (this.kvpBinding == null) {
            this.kvpBinding = getDefinitions().createBinding();
            this.kvpBinding.setPortType(getGetPortType());
            this.kvpBinding.setQName(WSDLConstants.QN_SOSW_KVP_BINDING);
            this.kvpBinding.setUndefined(false);
            HTTPBinding hb = (HTTPBinding) getExtensionRegistry()
                    .createExtension(Binding.class, WSDLConstants.QN_HTTP_BINDING);
            hb.setVerb(WSDLConstants.KVP_HTTP_VERB);
            this.kvpBinding.addExtensibilityElement(hb);
            getDefinitions().addBinding(this.kvpBinding);
        }
        return this.kvpBinding;
    }

    public static void main(String[] args) throws WSDLException, ParserConfigurationException {
        System.out.println(new WSDLBuilder()
                .setSoapEndpoint(URI.create("http://localhost:8080/52n-sos-webapp/sos/soap"))
                .setKvpEndpoint(URI.create("http://localhost:8080/52n-sos-webapp/sos/kvp"))
                .setPoxEndpoint(URI.create("http://localhost:8080/52n-sos-webapp/sos/pox"))
                .addSoapOperation(Operations.DELETE_SENSOR)
                .addSoapOperation(Operations.DESCRIBE_SENSOR)
                .addSoapOperation(Operations.GET_CAPABILITIES)
                .addSoapOperation(Operations.GET_FEATURE_OF_INTEREST)
                .addSoapOperation(Operations.GET_OBSERVATION)
                .addSoapOperation(Operations.GET_OBSERVATION_BY_ID)
                .addSoapOperation(Operations.GET_RESULT)
                .addSoapOperation(Operations.GET_RESULT_TEMPLATE)
                .addSoapOperation(Operations.INSERT_OBSERVATION)
                .addSoapOperation(Operations.INSERT_RESULT)
                .addSoapOperation(Operations.INSERT_RESULT_TEMPLATE)
                .addSoapOperation(Operations.INSERT_SENSOR)
                .addSoapOperation(Operations.UPDATE_SENSOR_DESCRIPTION)
                .addPoxOperation(Operations.DELETE_SENSOR)
                .addPoxOperation(Operations.DESCRIBE_SENSOR)
                .addPoxOperation(Operations.GET_CAPABILITIES)
                .addPoxOperation(Operations.GET_FEATURE_OF_INTEREST)
                .addPoxOperation(Operations.GET_OBSERVATION)
                .addPoxOperation(Operations.GET_OBSERVATION_BY_ID)
                .addPoxOperation(Operations.GET_RESULT)
                .addPoxOperation(Operations.GET_RESULT_TEMPLATE)
                .addPoxOperation(Operations.INSERT_OBSERVATION)
                .addPoxOperation(Operations.INSERT_RESULT)
                .addPoxOperation(Operations.INSERT_RESULT_TEMPLATE)
                .addPoxOperation(Operations.INSERT_SENSOR)
                .addPoxOperation(Operations.UPDATE_SENSOR_DESCRIPTION)
                .addKvpOperation(Operations.DELETE_SENSOR)
                .addKvpOperation(Operations.DESCRIBE_SENSOR)
                .addKvpOperation(Operations.GET_CAPABILITIES)
                .addKvpOperation(Operations.GET_FEATURE_OF_INTEREST)
                .addKvpOperation(Operations.GET_OBSERVATION)
                .addKvpOperation(Operations.GET_OBSERVATION_BY_ID)
                .addKvpOperation(Operations.GET_RESULT)
                .addKvpOperation(Operations.GET_RESULT_TEMPLATE)
                .addKvpOperation(Operations.INSERT_OBSERVATION)
                .addKvpOperation(Operations.INSERT_RESULT)
                .addKvpOperation(Operations.INSERT_RESULT_TEMPLATE)
                .addKvpOperation(Operations.INSERT_SENSOR)
                .addKvpOperation(Operations.UPDATE_SENSOR_DESCRIPTION)
                .build());
    }
}
