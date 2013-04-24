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
package org.n52.sos.decode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import net.opengis.sensorML.x101.AbstractComponentType;
import net.opengis.sensorML.x101.AbstractDerivableComponentType;
import net.opengis.sensorML.x101.AbstractProcessType;
import net.opengis.sensorML.x101.AbstractPureProcessType;
import net.opengis.sensorML.x101.CapabilitiesDocument.Capabilities;
import net.opengis.sensorML.x101.CharacteristicsDocument.Characteristics;
import net.opengis.sensorML.x101.ClassificationDocument.Classification;
import net.opengis.sensorML.x101.ClassificationDocument.Classification.ClassifierList.Classifier;
import net.opengis.sensorML.x101.ComponentType;
import net.opengis.sensorML.x101.ComponentsDocument.Components;
import net.opengis.sensorML.x101.ComponentsDocument.Components.ComponentList;
import net.opengis.sensorML.x101.ComponentsDocument.Components.ComponentList.Component;
import net.opengis.sensorML.x101.ContactDocument.Contact;
import net.opengis.sensorML.x101.DocumentationDocument.Documentation;
import net.opengis.sensorML.x101.HistoryDocument.History;
import net.opengis.sensorML.x101.IdentificationDocument.Identification;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList.Identifier;
import net.opengis.sensorML.x101.InputsDocument.Inputs;
import net.opengis.sensorML.x101.IoComponentPropertyType;
import net.opengis.sensorML.x101.KeywordsDocument.Keywords;
import net.opengis.sensorML.x101.MethodPropertyType;
import net.opengis.sensorML.x101.OutputsDocument.Outputs;
import net.opengis.sensorML.x101.ParametersDocument.Parameters;
import net.opengis.sensorML.x101.PositionDocument.Position;
import net.opengis.sensorML.x101.ProcessModelType;
import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sensorML.x101.SensorMLDocument.SensorML.Member;
import net.opengis.sensorML.x101.SystemType;
import net.opengis.sensorML.x101.ValidTimeDocument.ValidTime;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.concrete.UnsupportedDecoderInputException;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.CodeType;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.AbstractComponent;
import org.n52.sos.ogc.sensorML.AbstractProcess;
import org.n52.sos.ogc.sensorML.AbstractSensorML;
import org.n52.sos.ogc.sensorML.ProcessMethod;
import org.n52.sos.ogc.sensorML.ProcessModel;
import org.n52.sos.ogc.sensorML.RulesDefinition;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.SmlContact;
import org.n52.sos.ogc.sensorML.System;
import org.n52.sos.ogc.sensorML.elements.AbstractSosSMLDocumentation;
import org.n52.sos.ogc.sensorML.elements.SosSMLCapabilities;
import org.n52.sos.ogc.sensorML.elements.SosSMLCharacteristics;
import org.n52.sos.ogc.sensorML.elements.SosSMLClassifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLComponent;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLIo;
import org.n52.sos.ogc.sensorML.elements.SosSMLPosition;
import org.n52.sos.ogc.swe.AbstractDataRecord;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.XmlHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorMLDecoderV101 implements Decoder<AbstractSensorML, XmlObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorMLDecoderV101.class);

    private static final Set<DecoderKey> DECODER_KEYS = CodingHelper.decoderKeysForElements(SensorMLConstants.NS_SML,
            SensorMLDocument.class, SystemType.class, ProcessModelType.class);

    private static final Set<String> SUPPORTED_PROCEDURE_DESCRIPTION_FORMATS = Collections
            .singleton(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL);

    private static final Set<String> REMOVABLE_CAPABILITIES_NAMES = CollectionHelper.set(
            SensorMLConstants.ELEMENT_NAME_PARENT_PROCEDURES, SensorMLConstants.ELEMENT_NAME_FEATURE_OF_INTEREST);

    private static final Set<String> REMOVABLE_COMPONENTS_ROLES = Collections
            .singleton(SensorMLConstants.ELEMENT_NAME_CHILD_PROCEDURES);

    private static final Set<String> REMOVABLE_IDENTIFIERS = Collections
            .singleton(SensorMLConstants.ELEMENT_NAME_OFFERING);

    public SensorMLDecoderV101() {
        LOGGER.debug("Decoder for the following keys initialized successfully: {}!",
                StringHelper.join(", ", DECODER_KEYS));
    }

    @Override
    public Set<DecoderKey> getDecoderKeyTypes() {
        return Collections.unmodifiableSet(DECODER_KEYS);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.emptySet();
    }

    @Override
    public AbstractSensorML decode(final XmlObject element) throws OwsExceptionReport {
        if (element instanceof SensorMLDocument) {
            return parseSensorML((SensorMLDocument) element);
        } else if (element instanceof SystemType) {
            return parseSystem((SystemType) element);
        } else if (element instanceof ProcessModelType) {
            return parseProcessModel((ProcessModelType) element);
        } else {
            throw new UnsupportedDecoderInputException(this, element);
        }
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return Collections.singletonMap(SupportedTypeKey.ProcedureDescriptionFormat,
                SUPPORTED_PROCEDURE_DESCRIPTION_FORMATS);
    }

    private SensorML parseSensorML(final SensorMLDocument xbSensorML) throws OwsExceptionReport {
        final SensorML sensorML = new SensorML();
        // get member process
        for (final Member xbMember : xbSensorML.getSensorML().getMemberArray()) {
            if (xbMember.getProcess() != null) {
                if (xbMember.getProcess() instanceof AbstractProcessType) {
                    final AbstractProcessType xbAbstractProcess = xbMember.getProcess();
                    AbstractProcess abstractProcess = null;
                    if (xbAbstractProcess.schemaType() == SystemType.type) {
                        abstractProcess = parseSystem((SystemType) xbAbstractProcess);
                    } else if (xbAbstractProcess.schemaType() == ProcessModelType.type) {
                        abstractProcess = parseProcessModel((ProcessModelType) xbAbstractProcess);
                    } else if (xbAbstractProcess.schemaType() == ComponentType.type) {
                        abstractProcess = parseComponent((ComponentType) xbAbstractProcess);
                    } else {
                        throw new InvalidParameterValueException().at(XmlHelper.getLocalName(xbMember)).withMessage(
                                "The process of a member of the SensorML Document (%s) is not supported!",
                                xbMember.getProcess().getDomNode().getNodeName());
                    }
                    sensorML.addMember(abstractProcess);
                } else {
                    throw new InvalidParameterValueException().at(XmlHelper.getLocalName(xbMember)).withMessage(
                            "The process of a member of the SensorML Document (%s) is not supported!",
                            xbMember.getProcess().getDomNode().getNodeName());
                }
            } else {
                throw new InvalidParameterValueException().at(XmlHelper.getLocalName(xbMember)).withMessage(
                        "The process of a member of the SensorML Document is null (%s)!", xbMember.getProcess());
            }
        }
        sensorML.setSensorDescriptionXmlString(xbSensorML.xmlText());
        return sensorML;
    }

    private void parseAbstractProcess(final AbstractProcessType xbAbstractProcess,
            final AbstractProcess abstractProcess) throws OwsExceptionReport {
        if (xbAbstractProcess.getIdentificationArray() != null) {
        	final IdentificationsAndIdentifier idsAndId = parseIdentifications(xbAbstractProcess.getIdentificationArray());
        	abstractProcess.setIdentifier(idsAndId.getIdentifier());
            abstractProcess.setIdentifications(idsAndId.getIdentifications());
            final List<Integer> identificationsToRemove =
                    checkIdentificationsForRemoval(xbAbstractProcess.getIdentificationArray());
            for (final Integer integer : identificationsToRemove) {
                xbAbstractProcess.removeIdentification(integer);
            }
        }
        if (xbAbstractProcess.getClassificationArray() != null) {
            abstractProcess.setClassifications(parseClassification(xbAbstractProcess.getClassificationArray()));
        }
        if (xbAbstractProcess.getCharacteristicsArray() != null) {
            abstractProcess.setCharacteristics(parseCharacteristics(xbAbstractProcess.getCharacteristicsArray()));
        }
        if (xbAbstractProcess.getCapabilitiesArray() != null) {
            abstractProcess.addCapabilities(parseCapabilities(xbAbstractProcess.getCapabilitiesArray()));
            final List<Integer> capsToRemove = checkCapabilitiesForRemoval(xbAbstractProcess.getCapabilitiesArray());
            for (final Integer integer : capsToRemove) {
                xbAbstractProcess.removeCapabilities(integer);
            }
        }
        if (xbAbstractProcess.isSetDescription()) {
            abstractProcess.addDescription(xbAbstractProcess.getDescription().getStringValue());
        }
        if (xbAbstractProcess.isSetValidTime()) {
            abstractProcess.setValidTime(parseValidTime(xbAbstractProcess.getValidTime()));
        }
        if (xbAbstractProcess.getContactArray() != null) {
            abstractProcess.setContact(parseContact(xbAbstractProcess.getContactArray()));
        }
        if (xbAbstractProcess.getDocumentationArray() != null) {
            abstractProcess.setDocumentation(parseDocumentation(xbAbstractProcess.getDocumentationArray()));
        }
        if (xbAbstractProcess.getHistoryArray() != null) {
            abstractProcess.setHistory(parseHistory(xbAbstractProcess.getHistoryArray()));
        }
        if (xbAbstractProcess.getKeywordsArray() != null) {
            abstractProcess.setKeywords(parseKeywords(xbAbstractProcess.getKeywordsArray()));
        }
        if (xbAbstractProcess.getNameArray() != null) {
            final int length = xbAbstractProcess.getNameArray().length;
            for (int i = 0; i < length; i++) {
                final Object decodedElement = CodingHelper.decodeXmlElement(xbAbstractProcess.getNameArray(i));
                if (decodedElement instanceof CodeType) {
                    abstractProcess.addName((CodeType) decodedElement);
                }
            }
        }
    }

    private void parseAbstractDerivableComponent(final AbstractDerivableComponentType xbAbstractDerivableComponent,
            final AbstractComponent abstractComponent) throws OwsExceptionReport {
        if (xbAbstractDerivableComponent.isSetPosition()) {
            abstractComponent.setPosition(parsePosition(xbAbstractDerivableComponent.getPosition()));
        }
        // TODO ...
    }

    private void parseAbstractComponent(final AbstractComponentType xbAbstractComponent,
            final AbstractProcess abstractProcess) throws OwsExceptionReport {
        if (xbAbstractComponent.isSetInputs()) {
            abstractProcess.setInputs(parseInputs(xbAbstractComponent.getInputs()));
        }
        if (xbAbstractComponent.isSetOutputs()) {
            abstractProcess.setOutputs(parseOutputs(xbAbstractComponent.getOutputs()));
        }
        if (xbAbstractComponent.isSetParameters()) {
            abstractProcess.setParameters(parseParameters(xbAbstractComponent.getParameters()));
        }
    }

    private void parseAbstractPureProcess(final AbstractPureProcessType xbAbstractPureProcess,
            final ProcessModel processModel) throws OwsExceptionReport {
        if (xbAbstractPureProcess.isSetInputs()) {
            processModel.setInputs(parseInputs(xbAbstractPureProcess.getInputs()));
        }
        if (xbAbstractPureProcess.isSetOutputs()) {
            processModel.setOutputs(parseOutputs(xbAbstractPureProcess.getOutputs()));
        }
        if (xbAbstractPureProcess.isSetParameters()) {
            processModel.setParameters(parseParameters(xbAbstractPureProcess.getParameters()));
        }

    }

    private System parseSystem(final SystemType xbSystemType) throws OwsExceptionReport {
        final System system = new System();
        parseAbstractProcess(xbSystemType, system);
        parseAbstractComponent(xbSystemType, system);
        parseAbstractDerivableComponent(xbSystemType, system);
        if (xbSystemType.isSetComponents() && xbSystemType.getComponents().isSetComponentList()) {
            system.addComponents(parseComponents(xbSystemType.getComponents()));
            final List<Integer> compsToRemove =
                    checkComponentsForRemoval(xbSystemType.getComponents().getComponentList());
            for (final Integer integer : compsToRemove) {
                xbSystemType.getComponents().getComponentList().removeComponent(integer);
            }
            checkAndRemoveEmptyComponents(xbSystemType);
        }
        final String xmlDescription = addSensorMLWrapperForXmlDescription(xbSystemType);
        system.setSensorDescriptionXmlString(xmlDescription);
        return system;
    }

    private AbstractProcess parseComponent(final ComponentType componentType) throws OwsExceptionReport {
        final org.n52.sos.ogc.sensorML.Component component = new org.n52.sos.ogc.sensorML.Component();
        parseAbstractProcess(componentType, component);
        parseAbstractDerivableComponent(componentType, component);
        parseAbstractComponent(componentType, component);
        if (componentType.isSetPosition()) {
            component.setPosition(parsePosition(componentType.getPosition()));
        }
        component.setSensorDescriptionXmlString(addSensorMLWrapperForXmlDescription(componentType));
        return component;
    }

    private ProcessModel parseProcessModel(final ProcessModelType xbProcessModel) throws OwsExceptionReport {
        final ProcessModel processModel = new ProcessModel();
        parseAbstractProcess(xbProcessModel, processModel);
        parseAbstractPureProcess(xbProcessModel, processModel);
        if (xbProcessModel.getMethod() != null) {
            processModel.setMethod(parseProcessMethod(xbProcessModel.getMethod()));
        }
        processModel.setSensorDescriptionXmlString(addSensorMLWrapperForXmlDescription(xbProcessModel));
        return processModel;
    }

    private ProcessMethod parseProcessMethod(final MethodPropertyType method) {
        final ProcessMethod processMethod = new ProcessMethod(parseRulesDefinition(method.getProcessMethod().getRules().getRulesDefinition()));
        // TODO implement parsing of sml:ProcessMethod
        return processMethod;
    }

	private RulesDefinition parseRulesDefinition(final net.opengis.sensorML.x101.ProcessMethodType.Rules.RulesDefinition xbRulesDefinition)
	{
		final RulesDefinition rulesDefinition = new RulesDefinition();
		if (xbRulesDefinition.isSetDescription())
		{
			rulesDefinition.setDescription(xbRulesDefinition.getDescription().getStringValue());
		}
		// TODO add other options if required
		return rulesDefinition;
	}

	private class IdentificationsAndIdentifier{
    	private final List<SosSMLIdentifier> identifications;
    	private String identifier;
    	
    	public IdentificationsAndIdentifier( final int identificationsLength ){
    		identifications = new ArrayList<SosSMLIdentifier>( identificationsLength );
    	}
		public String getIdentifier() {
			return identifier;
		}		
		public void setIdentifier(final String identifier) {
			this.identifier = identifier;
		}
		public List<SosSMLIdentifier> getIdentifications() {
			return identifications;
		}
    }
    
    /**
     * Parses the identification
     * 
     * @param identificationArray
     *            XML identification
     * @return SOS identifications and identifier
     */
    private IdentificationsAndIdentifier parseIdentifications(final Identification[] identificationArray) {
    	final IdentificationsAndIdentifier idsAndId = new IdentificationsAndIdentifier(identificationArray.length);
        for (final Identification xbIdentification : identificationArray) {
            for (final Identifier xbIdentifier : xbIdentification.getIdentifierList().getIdentifierArray()) {
            	final SosSMLIdentifier identification = new SosSMLIdentifier(xbIdentifier.getName(),
                        xbIdentifier.getTerm().getDefinition(), xbIdentifier.getTerm().getValue());
            	idsAndId.getIdentifications().add(identification);
            	if(isIdentificationProcedureIdentifier(identification)){
            		idsAndId.setIdentifier(identification.getValue());
            	}
            }
        }
        return idsAndId;
    }

    /**
     * Determine if an SosSMLIdentifier is the unique identifier for a procedure
     * @param identification SosSMLIdentifier to example for unique identifier
     * @return whether the SosSMLIdentifier contains the unique identifier
     */
    protected boolean isIdentificationProcedureIdentifier(final SosSMLIdentifier identification) {
    	return (identification.getName() != null && identification.getName().equals(OGCConstants.URN_UNIQUE_IDENTIFIER_END))
          || (identification.getDefinition() != null && (identification.getDefinition().equals(
        		  OGCConstants.URN_UNIQUE_IDENTIFIER)
                  || identification.getDefinition().equals(OGCConstants.URN_IDENTIFIER_IDENTIFICATION) || (identification
                  .getDefinition().startsWith(OGCConstants.URN_UNIQUE_IDENTIFIER_START) && identification.getDefinition()
                  .contains(OGCConstants.URN_UNIQUE_IDENTIFIER_END))));
    }    
    
    /**
     * Parses the classification
     * 
     * @param classificationArray
     *            XML classification
     * @return SOS classification
     */
    private List<SosSMLClassifier> parseClassification(final Classification[] classificationArray) {
        final List<SosSMLClassifier> sosClassifiers = new ArrayList<SosSMLClassifier>(classificationArray.length);
        for (final Classification xbClassification : classificationArray) {
            for (final Classifier xbClassifier : xbClassification.getClassifierList().getClassifierArray()) {
                sosClassifiers.add(new SosSMLClassifier(xbClassifier.getName(),
                        xbClassifier.getTerm().getDefinition(), xbClassifier.getTerm().getValue()));
            }
        }
        return sosClassifiers;
    }

    /**
     * Parses the characteristics
     * 
     * @param characteristicsArray
     *            XML characteristics
     * @return SOS characteristics
     * 
     * 
     * @throws OwsExceptionReport
     *             * if an error occurs
     */
    private List<SosSMLCharacteristics> parseCharacteristics(final Characteristics[] characteristicsArray)
            throws OwsExceptionReport {
        final List<SosSMLCharacteristics> sosCharacteristicsList =
                new ArrayList<SosSMLCharacteristics>(characteristicsArray.length);
        final SosSMLCharacteristics sosCharacteristics = new SosSMLCharacteristics();
        for (final Characteristics xbCharacteristics : characteristicsArray) {
            final Object decodedObject = CodingHelper.decodeXmlElement(xbCharacteristics.getAbstractDataRecord());
            if (decodedObject instanceof AbstractDataRecord) {
                sosCharacteristics.setDataRecord((AbstractDataRecord) decodedObject);
            } else {
                throw new InvalidParameterValueException()
                        .at(XmlHelper.getLocalName(xbCharacteristics))
                        .withMessage(
                                "Error while parsing the characteristics of the SensorML (the characteristics' data record is not of type DataRecordPropertyType)!");
            }
        }
        sosCharacteristicsList.add(sosCharacteristics);
        return sosCharacteristicsList;
    }

    /**
     * Parses the capabilities
     * 
     * @param capabilitiesArray
     *            XML capabilities
     * @return SOS capabilities
     * 
     * 
     * @throws OwsExceptionReport
     *             * if an error occurs
     */
    private List<SosSMLCapabilities> parseCapabilities(final Capabilities[] capabilitiesArray)
            throws OwsExceptionReport {
        final List<SosSMLCapabilities> sosCapabilitiesList =
                new ArrayList<SosSMLCapabilities>(capabilitiesArray.length);
        final SosSMLCapabilities sosCapabilities = new SosSMLCapabilities();
        for (final Capabilities xbCpabilities : capabilitiesArray) {
            final Object decodedObject = CodingHelper.decodeXmlElement(xbCpabilities.getAbstractDataRecord());
            if (decodedObject instanceof AbstractDataRecord) {
                sosCapabilities.setDataRecord((AbstractDataRecord) decodedObject);
            } else {
                throw new InvalidParameterValueException()
                        .at(XmlHelper.getLocalName(xbCpabilities))
                        .withMessage(
                                "Error while parsing the capabilities of the SensorML (the capabilities data record is not of type DataRecordPropertyType)!");
            }
        }
        sosCapabilitiesList.add(sosCapabilities);
        return sosCapabilitiesList;
    }

    /**
     * Parses the position
     * 
     * @param position
     *            XML position
     * @return SOS position
     * 
     * 
     * @throws OwsExceptionReport
     *             * if an error occurs
     */
    private SosSMLPosition parsePosition(final Position position) throws OwsExceptionReport {
        SosSMLPosition sosSMLPosition = null;
        if (position.isSetPosition()) {
            final Object pos = CodingHelper.decodeXmlElement(position.getPosition());
            if (pos instanceof SosSMLPosition) {
                sosSMLPosition = (SosSMLPosition) pos;
            }
        } else {
            throw new InvalidParameterValueException().at(XmlHelper.getLocalName(position)).withMessage(
                    "Error while parsing the position of the SensorML (the position is not set)!");
        }
        if (position.getName() != null) {
            sosSMLPosition.setName(position.getName());
        }
        return sosSMLPosition;
    }

    private ITime parseValidTime(final ValidTime validTime) {
        // TODO Auto-generated method stub
        return null;
    }

    private List<String> parseParameters(final Parameters parameters) {
        final List<String> sosParameters = new ArrayList<String>(0);
        // TODO Auto-generated method stub
        return sosParameters;
    }

    private List<SmlContact> parseContact(final Contact[] contactArray) {
        // TODO Auto-generated method stub
        return null;
    }

    private List<AbstractSosSMLDocumentation> parseDocumentation(final Documentation[] documentationArray) {
        final List<AbstractSosSMLDocumentation> abstractDocumentation = new ArrayList<AbstractSosSMLDocumentation>(0);
        // TODO Auto-generated method stub
        return abstractDocumentation;
    }

    private List<String> parseKeywords(final Keywords[] keywordsArray) {
        final List<String> keywords = new ArrayList<String>(0);
        // TODO Auto-generated method stub
        return keywords;
    }

    private String parseHistory(final History[] historyArray) {
        // TODO Auto-generated method stub
        return "";
    }

    /**
     * Parses the inputs
     * 
     * @param inputs
     *            XML inputs
     * @return SOS inputs
     * 
     * 
     * @throws OwsExceptionReport
     *             * if an error occurs
     */
    private List<SosSMLIo<?>> parseInputs(final Inputs inputs) throws OwsExceptionReport {
        final List<SosSMLIo<?>> sosInputs = new ArrayList<SosSMLIo<?>>(inputs.getInputList().getInputArray().length);
        for (final IoComponentPropertyType xbInput : inputs.getInputList().getInputArray()) {
            sosInputs.add(parseIoComponentPropertyType(xbInput));
        }
        return sosInputs;
    }

    /**
     * Parses the outputs
     * 
     * @param outputs
     *            XML outputs
     * @return SOS outputs
     * 
     * 
     * @throws OwsExceptionReport
     *             * if an error occurs
     */
    private List<SosSMLIo<?>> parseOutputs(final Outputs outputs) throws OwsExceptionReport {
        final List<SosSMLIo<?>> sosOutputs =
                new ArrayList<SosSMLIo<?>>(outputs.getOutputList().getOutputArray().length);
        for (final IoComponentPropertyType xbOutput : outputs.getOutputList().getOutputArray()) {
            sosOutputs.add(parseIoComponentPropertyType(xbOutput));
        }
        return sosOutputs;
    }

    private List<SosSMLComponent> parseComponents(final Components components) throws OwsExceptionReport {
        final List<SosSMLComponent> sosSmlComponents = CollectionHelper.list();
        if (components.isSetComponentList() && components.getComponentList().getComponentArray() != null) {
            for (final Component component : components.getComponentList().getComponentArray()) {
                if (component.isSetProcess() || component.isSetHref()) {
                    final SosSMLComponent sosSmlcomponent = new SosSMLComponent(component.getName());
                    final AbstractProcess abstractProcess = new AbstractProcess();
                    if (component.isSetProcess()) {
                        parseAbstractProcess(component.getProcess(), abstractProcess);
                    } else {
                        abstractProcess.setIdentifier(component.getHref());
                    }
                    sosSmlcomponent.setProcess(abstractProcess);
                    sosSmlComponents.add(sosSmlcomponent);
                }
            }
        }
        return sosSmlComponents;
    }

    /**
     * Parses the components
     * 
     * @param xbIoCompPropType
     *            XML components
     * @return SOS components
     * 
     * 
     * @throws OwsExceptionReport
     *             * if an error occurs
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private SosSMLIo<?> parseIoComponentPropertyType(final IoComponentPropertyType xbIoCompPropType)
            throws OwsExceptionReport {
        final SosSMLIo<?> sosIo = new SosSMLIo();
        sosIo.setIoName(xbIoCompPropType.getName());
        XmlObject toDecode = null;
        if (xbIoCompPropType.isSetBoolean()) {
            toDecode = xbIoCompPropType.getBoolean();
        } else if (xbIoCompPropType.isSetCategory()) {
            toDecode = xbIoCompPropType.getCategory();
        } else if (xbIoCompPropType.isSetCount()) {
            toDecode = xbIoCompPropType.getCount();
        } else if (xbIoCompPropType.isSetCountRange()) {
            toDecode = xbIoCompPropType.getCountRange();
        } else if (xbIoCompPropType.isSetObservableProperty()) {
            toDecode = xbIoCompPropType.getObservableProperty();
        } else if (xbIoCompPropType.isSetQuantity()) {
            toDecode = xbIoCompPropType.getQuantity();
        } else if (xbIoCompPropType.isSetQuantityRange()) {
            toDecode = xbIoCompPropType.getQuantityRange();
        } else if (xbIoCompPropType.isSetText()) {
            toDecode = xbIoCompPropType.getText();
        } else if (xbIoCompPropType.isSetTime()) {
            toDecode = xbIoCompPropType.getTime();
        } else if (xbIoCompPropType.isSetTimeRange()) {
            toDecode = xbIoCompPropType.getTimeRange();
        } else if (xbIoCompPropType.isSetAbstractDataArray1()) {
            toDecode = xbIoCompPropType.getAbstractDataArray1();
        } else if (xbIoCompPropType.isSetAbstractDataRecord()) {
            toDecode = xbIoCompPropType.getAbstractDataRecord();
        } else {
            throw new InvalidParameterValueException().at(XmlHelper.getLocalName(xbIoCompPropType)).withMessage(
                    "An \"IoComponentProperty\" is not supported");
        }

        final Object decodedObject = CodingHelper.decodeXmlElement(toDecode);
        if (decodedObject instanceof SosSweAbstractSimpleType) {
            sosIo.setIoValue((SosSweAbstractSimpleType) decodedObject);
        }
        return sosIo;
    }

    private String addSensorMLWrapperForXmlDescription(final AbstractProcessType xbProcessType) {
        final SensorMLDocument xbSensorMLDoc =
                SensorMLDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final net.opengis.sensorML.x101.SensorMLDocument.SensorML xbSensorML = xbSensorMLDoc.addNewSensorML();
        xbSensorML.setVersion(SensorMLConstants.VERSION_V101);
        final Member member = xbSensorML.addNewMember();
        member.setProcess(xbProcessType);
        member.getProcess().substitute(getQnameForType(xbProcessType.schemaType()), xbProcessType.schemaType());
        return xbSensorMLDoc.xmlText(XmlOptionsHelper.getInstance().getXmlOptions());
    }

    private QName getQnameForType(final SchemaType type) {
        if (type == SystemType.type) {
            return SensorMLConstants.SYSTEM_QNAME;
        } else if (type == ProcessModelType.type) {
            return SensorMLConstants.PROCESS_MODEL_QNAME;
        } else if (type == ComponentType.type) {
            return SensorMLConstants.COMPONENT_QNAME;
        }
        return SensorMLConstants.ABSTRACT_PROCESS_QNAME;
    }

    private List<Integer> checkCapabilitiesForRemoval(final Capabilities[] capabilitiesArray) {
        final List<Integer> removeableCaps = new ArrayList<Integer>(capabilitiesArray.length);
        for (int i = 0; i < capabilitiesArray.length; i++) {
            if (capabilitiesArray[i].getName() != null
                    && REMOVABLE_CAPABILITIES_NAMES.contains(capabilitiesArray[i].getName())) {
                removeableCaps.add(i);
            }
        }
        Collections.sort(removeableCaps);
        Collections.reverse(removeableCaps);
        return removeableCaps;
    }

    private List<Integer> checkComponentsForRemoval(final ComponentList componentList) {
        final List<Integer> removeableComponents = new ArrayList<Integer>(0);
        if (componentList != null && componentList.getComponentArray() != null) {
            final Component[] componentArray = componentList.getComponentArray();
            for (int i = 0; i < componentArray.length; i++) {
                if (componentArray[i].getRole() != null
                        && REMOVABLE_COMPONENTS_ROLES.contains(componentArray[i].getRole())) {
                    removeableComponents.add(i);
                }
            }
        }
        return removeableComponents;
    }

    private List<Integer> checkIdentificationsForRemoval(final Identification[] identifications) {
        final List<Integer> removeableIdentification = new ArrayList<Integer>(identifications.length);
        for (int i = 0; i < identifications.length; i++) {
            if (identifications[i].getTitle() != null && REMOVABLE_IDENTIFIERS.contains(identifications[i].getTitle())) {
                removeableIdentification.add(i);
            }
        }
        return removeableIdentification;
    }

    private void checkAndRemoveEmptyComponents(final SystemType system) {
        boolean removeComponents = false;
        final Components components = system.getComponents();
        if (components != null) {
            if (components.getComponentList() == null) {
                removeComponents = true;
            } else if (components.getComponentList().getComponentArray() == null
                    || ((components.getComponentList().getComponentArray() != null && components.getComponentList()
                            .getComponentArray().length == 0))) {
                removeComponents = true;
            }
        }
        if (removeComponents) {
            system.unsetComponents();
        }
    }
}
