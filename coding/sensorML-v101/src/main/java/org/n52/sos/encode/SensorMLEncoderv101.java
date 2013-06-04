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
package org.n52.sos.encode;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import net.opengis.sensorML.x101.AbstractProcessType;
import net.opengis.sensorML.x101.CapabilitiesDocument.Capabilities;
import net.opengis.sensorML.x101.CharacteristicsDocument.Characteristics;
import net.opengis.sensorML.x101.ClassificationDocument.Classification;
import net.opengis.sensorML.x101.ClassificationDocument.Classification.ClassifierList;
import net.opengis.sensorML.x101.ClassificationDocument.Classification.ClassifierList.Classifier;
import net.opengis.sensorML.x101.ComponentsDocument.Components;
import net.opengis.sensorML.x101.ComponentsDocument.Components.ComponentList;
import net.opengis.sensorML.x101.ComponentsDocument.Components.ComponentList.Component;
import net.opengis.sensorML.x101.ContactInfoDocument.ContactInfo;
import net.opengis.sensorML.x101.ContactInfoDocument.ContactInfo.Address;
import net.opengis.sensorML.x101.ContactInfoDocument.ContactInfo.Phone;
import net.opengis.sensorML.x101.ContactListDocument.ContactList;
import net.opengis.sensorML.x101.DocumentDocument.Document;
import net.opengis.sensorML.x101.DocumentListDocument.DocumentList;
import net.opengis.sensorML.x101.DocumentationDocument.Documentation;
import net.opengis.sensorML.x101.IdentificationDocument.Identification;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList.Identifier;
import net.opengis.sensorML.x101.InputsDocument.Inputs;
import net.opengis.sensorML.x101.InputsDocument.Inputs.InputList;
import net.opengis.sensorML.x101.IoComponentPropertyType;
import net.opengis.sensorML.x101.MethodPropertyType;
import net.opengis.sensorML.x101.OutputsDocument.Outputs;
import net.opengis.sensorML.x101.OutputsDocument.Outputs.OutputList;
import net.opengis.sensorML.x101.PersonDocument.Person;
import net.opengis.sensorML.x101.PositionDocument.Position;
import net.opengis.sensorML.x101.ProcessMethodType;
import net.opengis.sensorML.x101.ProcessMethodType.Rules.RulesDefinition;
import net.opengis.sensorML.x101.ProcessModelDocument;
import net.opengis.sensorML.x101.ProcessModelType;
import net.opengis.sensorML.x101.ResponsiblePartyDocument.ResponsibleParty;
import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sensorML.x101.SensorMLDocument.SensorML.Member;
import net.opengis.sensorML.x101.SystemDocument;
import net.opengis.sensorML.x101.SystemType;
import net.opengis.sensorML.x101.TermDocument.Term;
import net.opengis.swe.x101.AnyScalarPropertyType;
import net.opengis.swe.x101.PositionType;
import net.opengis.swe.x101.SimpleDataRecordType;
import net.opengis.swe.x101.VectorType;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.binding.BindingConstants;
import org.n52.sos.binding.BindingRepository;
import org.n52.sos.exception.CodedException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.AbstractProcess;
import org.n52.sos.ogc.sensorML.AbstractSensorML;
import org.n52.sos.ogc.sensorML.ProcessMethod;
import org.n52.sos.ogc.sensorML.ProcessModel;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.SmlContact;
import org.n52.sos.ogc.sensorML.SmlPerson;
import org.n52.sos.ogc.sensorML.SmlResponsibleParty;
import org.n52.sos.ogc.sensorML.System;
import org.n52.sos.ogc.sensorML.elements.AbstractSmlDocumentation;
import org.n52.sos.ogc.sensorML.elements.SmlCapabilities;
import org.n52.sos.ogc.sensorML.elements.SmlCharacteristics;
import org.n52.sos.ogc.sensorML.elements.SmlClassifier;
import org.n52.sos.ogc.sensorML.elements.SmlComponent;
import org.n52.sos.ogc.sensorML.elements.SmlDocumentation;
import org.n52.sos.ogc.sensorML.elements.SmlDocumentationList;
import org.n52.sos.ogc.sensorML.elements.SmlDocumentationListMember;
import org.n52.sos.ogc.sensorML.elements.SmlIdentifier;
import org.n52.sos.ogc.sensorML.elements.SmlIo;
import org.n52.sos.ogc.sensorML.elements.SmlPosition;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosOffering;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.sos.SosProcedureDescriptionUnknowType;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SWEConstants.SweAggregateType;
import org.n52.sos.ogc.swe.SweAbstractDataComponent;
import org.n52.sos.ogc.swe.SweCoordinate;
import org.n52.sos.ogc.swe.SweDataArray;
import org.n52.sos.ogc.swe.SweDataRecord;
import org.n52.sos.ogc.swe.SweField;
import org.n52.sos.ogc.swe.SweSimpleDataRecord;
import org.n52.sos.ogc.swe.simpleType.SweAbstractSimpleType;
import org.n52.sos.ogc.swe.simpleType.SweText;
import org.n52.sos.service.CodingRepository;
import org.n52.sos.service.ServiceConfiguration;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.service.operator.ServiceOperatorRepository;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.HTTPConstants.StatusCode;
import org.n52.sos.util.SchemaLocation;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.XmlHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorMLEncoderv101 implements Encoder<XmlObject, Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorMLEncoderv101.class);

    private static final Map<SupportedTypeKey, Set<String>> SUPPORTED_TYPES = Collections.singletonMap(
            SupportedTypeKey.ProcedureDescriptionFormat, CollectionHelper.set(
            		SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL, SensorMLConstants.SENSORML_CONTENT_TYPE));

    @SuppressWarnings("unchecked")
    private static final Set<EncoderKey> ENCODER_KEYS = CollectionHelper.union(
            CodingHelper.encoderKeysForElements(SensorMLConstants.NS_SML,
                    SosProcedureDescription.class, AbstractSensorML.class),
            CodingHelper.encoderKeysForElements(SensorMLConstants.SENSORML_CONTENT_TYPE,
                    SosProcedureDescription.class, AbstractSensorML.class));

    private static final String OUTPUT_PREFIX = "output#";

    public SensorMLEncoderv101() {
        LOGGER.debug("Encoder for the following keys initialized successfully: {}!",
                StringHelper.join(", ", ENCODER_KEYS));
    }
    
    @Override
    public Set<EncoderKey> getEncoderKeyType() {
        return Collections.unmodifiableSet(ENCODER_KEYS);
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return Collections.unmodifiableMap(SUPPORTED_TYPES);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.emptySet();
    }

    @Override
    public void addNamespacePrefixToMap(final Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(SensorMLConstants.NS_SML, SensorMLConstants.NS_SML_PREFIX);
    }

    @Override
    public String getContentType() {
        return SensorMLConstants.SENSORML_CONTENT_TYPE;
    }

    @Override
    public Set<SchemaLocation> getSchemaLocations() {
        return CollectionHelper.set(SensorMLConstants.SML_101_SCHEMA_LOCATION);
    }

    @Override
    public XmlObject encode(final Object response) throws OwsExceptionReport {
        return encode(response, null);
    }

    @Override
    public XmlObject encode(final Object response, final Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        XmlObject encodedObject = null;
        if (response instanceof AbstractSensorML) {
            encodedObject = createSensorDescription((AbstractSensorML) response);
        }
        // FIXME workaround? if of type UnknowProcedureType try to parse the
        // description string, UNIT is missing "NOT_DEFINED"?!
        else if (response instanceof SosProcedureDescriptionUnknowType) {

            final String procDescXMLString = ((SosProcedureDescription) response).getSensorDescriptionXmlString();
            final AbstractSensorML sensorDesc = new AbstractSensorML();
            sensorDesc.setSensorDescriptionXmlString(procDescXMLString);
            encodedObject = createSensorDescriptionFromString(sensorDesc);
        } else {
            throw new UnsupportedEncoderInputException(this, response);
        }
        LOGGER.debug("Encoded object {} is valid: {}", encodedObject.schemaType().toString(),
                XmlHelper.validateDocument(encodedObject));
        return encodedObject;

    }

    /**
     * creates sml:System
     * 
     * @param sensorDesc
     *            SensorML encoded system description
     * 
     * @return Returns XMLBeans representation of sml:System
     * 
     * 
     * @throws OwsExceptionReport
     */
    private XmlObject createSensorDescription(final AbstractSensorML sensorDesc) throws OwsExceptionReport {
        if (sensorDesc.isSetSensorDescriptionXmlString()) {
            return createSensorDescriptionFromString(sensorDesc);
        } else {
            return createSensorDescriptionFromObject(sensorDesc);
        }
    }

    protected XmlObject createSensorDescriptionFromString(final AbstractSensorML sensorDesc) throws OwsExceptionReport {
        try {
            final XmlObject xmlObject = XmlObject.Factory.parse(sensorDesc.getSensorDescriptionXmlString());
            if (xmlObject instanceof SensorMLDocument) {
                final SensorMLDocument sensorML = (SensorMLDocument) xmlObject;
                for (final Member member : sensorML.getSensorML().getMemberArray()) {
                    if (sensorDesc instanceof SensorML) {
                        for (final AbstractProcess absProcess : ((SensorML) sensorDesc).getMembers()) {
                            addAbstractProcessValues(member.getProcess(), absProcess);
                            if (member.getProcess() instanceof SystemType && absProcess instanceof System) {
                                addSystemValues((SystemType) member.getProcess(), (System) absProcess);
                            } else if (member.getProcess() instanceof ProcessModelType
                                    && absProcess instanceof ProcessModel) {
                                addProcessModelValues((ProcessModelType) member.getProcess(),
                                        (ProcessModel) absProcess);
                            }
                        }
                    } else if (sensorDesc instanceof AbstractProcess) {
                        addAbstractProcessValues(member.getProcess(), (AbstractProcess) sensorDesc);
                        if (member.getProcess() instanceof SystemType && sensorDesc instanceof System) {
                            addSystemValues((SystemType) member.getProcess(), (System) sensorDesc);
                        }
                    }
                }

            } else if (xmlObject instanceof AbstractProcessType) {
                final AbstractProcessType abstractProcess = (AbstractProcessType) xmlObject;
                addAbstractProcessValues(abstractProcess, (AbstractProcess)sensorDesc);
                if (abstractProcess instanceof SystemType && sensorDesc instanceof System) {
                    addSystemValues((SystemType) abstractProcess, (System) sensorDesc);
                } else if (abstractProcess instanceof ProcessModelType
                        && sensorDesc instanceof ProcessModel) {
                    addProcessModelValues((ProcessModelType)abstractProcess,
                            (ProcessModel) sensorDesc);
                }
            }
            return xmlObject;
        } catch (final XmlException xmle) {
            throw new NoApplicableCodeException().causedBy(xmle);
        }
    }

    private XmlObject createSensorDescriptionFromObject(final AbstractSensorML sensorDesc) throws OwsExceptionReport {
        if (sensorDesc instanceof SensorML) {
            return createSensorMLDescription((SensorML) sensorDesc);
        } else if (sensorDesc instanceof AbstractProcess) {
            return createProcessDescription((AbstractProcess) sensorDesc);
        } else {
            throw new NoApplicableCodeException()
                    .withMessage("The sensor description type is not supported by this service!");
        }

    }
    
    private XmlObject createProcessDescription(final AbstractProcess sensorDesc) throws OwsExceptionReport {
    	// TODO Review: System -> return doc; ProcessModel -> return type
    	if (sensorDesc instanceof System) {
    		final System system = (System) sensorDesc;
    		final SystemDocument xbSystemDoc =
    				SystemDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
    		final SystemType xbSystem = xbSystemDoc.addNewSystem();
    		addAbstractProcessValues(xbSystem, system);
    		addSystemValues(xbSystem, system);
    		return xbSystem;
    	} else if (sensorDesc instanceof ProcessModel) {
    		// TODO: set values
    		final ProcessModel processModel = (ProcessModel) sensorDesc;
    		final ProcessModelDocument xbProcessModelDoc =
    				ProcessModelDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
    		final ProcessModelType xbProcessModel = xbProcessModelDoc.addNewProcessModel();
    		addAbstractProcessValues(xbProcessModel, processModel);
    		addProcessModelValues(xbProcessModel, processModel);
    		return xbProcessModel;
    	} else {
    		throw new NoApplicableCodeException()
    		.withMessage("The sensor description type is not supported by this service!")
    		.setStatus(StatusCode.INTERNAL_SERVER_ERROR);
    	}
    }

    protected SensorMLDocument createSensorMLDescription(final SensorML smlSensorDesc) throws OwsExceptionReport {
        final SensorMLDocument sensorMLDoc =
                SensorMLDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final net.opengis.sensorML.x101.SensorMLDocument.SensorML xbSensorML = sensorMLDoc.addNewSensorML();
        xbSensorML.setVersion(SensorMLConstants.VERSION_V101);
        if (smlSensorDesc.isSetMembers()) {
            for (final AbstractProcess sml : smlSensorDesc.getMembers()) {
                if (sml instanceof System) {
                    final SystemType xbSystem =
                            (SystemType) xbSensorML
                                    .addNewMember()
                                    .addNewProcess()
                                    .substitute(new QName(SensorMLConstants.NS_SML, SensorMLConstants.EN_SYSTEM),
                                            SystemType.type);
                    final System smlSystem = (System) sml;
                    addAbstractProcessValues(xbSystem, smlSystem);
                    addSystemValues(xbSystem, smlSystem);
                } else if (sml instanceof ProcessModel) {
                    final ProcessModelType xbProcessModel =
                            (ProcessModelType) xbSensorML
                                    .addNewMember()
                                    .addNewProcess()
                                    .substitute(
                                            new QName(SensorMLConstants.NS_SML, SensorMLConstants.EN_PROCESS_MODEL),
                                            ProcessModelType.type);
                    final ProcessModel smlProcessModel = (ProcessModel) sml;
                    addAbstractProcessValues(xbProcessModel, smlProcessModel);
                    addProcessModelValues(xbProcessModel, smlProcessModel);
                }
            }
        }
        return sensorMLDoc;
    }

    private ContactList createContactList(final List<SmlContact> contacts) {
        final ContactList xbContacts = ContactList.Factory.newInstance();
        for (final SmlContact smlContact : contacts) {
            if (smlContact instanceof SmlPerson) {
                xbContacts.addNewMember().addNewPerson().set(createPerson((SmlPerson) smlContact));
            } else if (smlContact instanceof SmlResponsibleParty) {
                xbContacts.addNewMember().addNewResponsibleParty()
                        .set(createResponsibleParty((SmlResponsibleParty) smlContact));
            }
        }
        return xbContacts;
    }

    private XmlObject createResponsibleParty(final SmlResponsibleParty smlRespParty) {
        final ResponsibleParty xbRespParty = ResponsibleParty.Factory.newInstance();
        if (smlRespParty.isSetIndividualName()) {
            xbRespParty.setIndividualName(smlRespParty.getInvidualName());
        }
        if (smlRespParty.isSetOrganizationName()) {
            xbRespParty.setOrganizationName(smlRespParty.getOrganizationName());
        }
        if (smlRespParty.isSetPositionName()) {
            xbRespParty.setPositionName(smlRespParty.getPositionName());
        }
        if (smlRespParty.isSetContactInfo()) {
            xbRespParty.setContactInfo(createContactInfo(smlRespParty));
        }
        return xbRespParty;
    }

    private ContactInfo createContactInfo(final SmlResponsibleParty smlRespParty) {
        final ContactInfo xbContactInfo = ContactInfo.Factory.newInstance();
        if (smlRespParty.isSetHoursOfService()) {
            xbContactInfo.setHoursOfService(smlRespParty.getHoursOfService());
        }
        if (smlRespParty.isSetContactInstructions()) {
            xbContactInfo.setHoursOfService(smlRespParty.getContactInstructions());
        }
        if (smlRespParty.isSetOnlineResources()) {
            for (final String onlineResouce : smlRespParty.getOnlineResources()) {
                xbContactInfo.addNewOnlineResource().setHref(onlineResouce);
            }
        }
        if (smlRespParty.isSetPhone()) {
            final Phone xbPhone = xbContactInfo.addNewPhone();
            if (smlRespParty.isSetPhoneFax()) {
                for (final String fax : smlRespParty.getPhoneFax()) {
                    xbPhone.addFacsimile(fax);
                }
            }
            if (smlRespParty.isSetPhoneVoice()) {
                for (final String voice : smlRespParty.getPhoneVoice()) {
                    xbPhone.addVoice(voice);
                }
            }
        }
        if (smlRespParty.isSetAddress()) {
            final Address xbAddress = xbContactInfo.addNewAddress();
            if (smlRespParty.isSetDeliveryPoint()) {
                for (final String deliveryPoint : smlRespParty.getDeliveryPoint()) {
                    xbAddress.addDeliveryPoint(deliveryPoint);
                }
            }
            if (smlRespParty.isSetCity()) {
                xbAddress.setCity(smlRespParty.getCity());
            }
            if (smlRespParty.isSetAdministrativeArea()) {
                xbAddress.setAdministrativeArea(smlRespParty.getAdministrativeArea());
            }
            if (smlRespParty.isSetPostalCode()) {
                xbAddress.setPostalCode(smlRespParty.getPostalCode());
            }
            if (smlRespParty.isSetCountry()) {
                xbAddress.setCountry(smlRespParty.getCountry());
            }
        }
        return xbContactInfo;
    }

    private Person createPerson(final SmlPerson smlPerson) {
        final Person xbPerson = Person.Factory.newInstance();
        if (smlPerson.isSetAffiliation()) {
            xbPerson.setAffiliation(smlPerson.getAffiliation());
        }
        if (smlPerson.isSetEmail()) {
            xbPerson.setEmail(smlPerson.getEmail());
        }
        if (smlPerson.isSetName()) {
            xbPerson.setName(smlPerson.getName());
        }
        if (smlPerson.isSetPhoneNumber()) {
            xbPerson.setPhoneNumber(smlPerson.getPhoneNumber());
        }
        if (smlPerson.isSetSurname()) {
            xbPerson.setSurname(smlPerson.getSurname());
        }
        if (smlPerson.isSetUserID()) {
            xbPerson.setUserID(smlPerson.getUserID());
        }
        return xbPerson;
    }

    private String createDescription(final List<String> descriptions) {
        if (descriptions != null) {
            if (descriptions.size() == 1) {
                return descriptions.get(0);
            } else {
                return Arrays.toString(descriptions.toArray(new String[descriptions.size()]));
            }
        }
        return "";
    }

    // TODO refactor/rename
    private void addAbstractProcessValues(final AbstractProcessType abstractProcess,
            final AbstractProcess sosAbstractProcess) throws OwsExceptionReport {
    	addSpecialCapabilities(sosAbstractProcess);    	
        if (sosAbstractProcess.isSetCapabilities())
        {
        	for (final SmlCapabilities sosCapability : sosAbstractProcess.getCapabilities()) {
        		abstractProcess.addNewCapabilities().set(createCapability(sosCapability));
			}
        }
        
        // set description
        if (sosAbstractProcess.isSetDescriptions()) {
            if (!abstractProcess.isSetDescription()) {
                abstractProcess.addNewDescription().setStringValue(
                        createDescription(sosAbstractProcess.getDescriptions()));
            }
        }
        // set identification
        if (sosAbstractProcess.isSetIdentifications()) {
            abstractProcess.setIdentificationArray(createIdentification(sosAbstractProcess.getIdentifications()));
        }
        // set classification
        if (sosAbstractProcess.isSetClassifications()) {
            abstractProcess.setClassificationArray(createClassification(sosAbstractProcess.getClassifications()));
        }
        // set characteristics
        if (sosAbstractProcess.isSetCharacteristics()) {
            abstractProcess.setCharacteristicsArray(createCharacteristics(sosAbstractProcess.getCharacteristics()));
        }
        // set documentation
        if (sosAbstractProcess.isSetDocumentation()) {
            abstractProcess.setDocumentationArray(createDocumentationArray(sosAbstractProcess.getDocumentation()));
        }
        // set contact
        if (sosAbstractProcess.isSetContact()) {
            abstractProcess.addNewContact().setContactList(createContactList(sosAbstractProcess.getContact()));
        }
        // set keywords
        if (sosAbstractProcess.isSetKeywords()) {
            abstractProcess.addNewKeywords().addNewKeywordList()
                    .setKeywordArray(sosAbstractProcess.getKeywords().toArray(new String[0]));
        }
    }

	private XmlObject createCapability(final SmlCapabilities capabilities) throws OwsExceptionReport
	{
		final Capabilities xbCapabilities = Capabilities.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
		if (capabilities.isSetName())
		{
			xbCapabilities.setName(capabilities.getName());
		}
		if (capabilities.isSetAbstractDataRecord() && capabilities.getDataRecord().isSetFields())
		{
			final XmlObject encodedDataRecord = CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE_101, capabilities.getDataRecord());
			final XmlObject substituteElement = XmlHelper.substituteElement(xbCapabilities.addNewAbstractDataRecord(), encodedDataRecord);
			substituteElement.set(encodedDataRecord);
		}
		return xbCapabilities;
	}

	private void addSystemValues(final SystemType xbSystem, final System system) throws OwsExceptionReport {
        // set inputs
        if (system.isSetInputs()) {
            xbSystem.setInputs(createInputs(system.getInputs()));
        }
        // set position
        if (system.isSetPosition()) {
            xbSystem.setPosition(createPosition(system.getPosition()));
        }
        // set components
        final List<SmlComponent> smlComponents = new ArrayList<SmlComponent>();
        if (system.isSetComponents() || system.isSetChildProcedures()) {
            if (system.isSetComponents()) {
                smlComponents.addAll(system.getComponents());
            }
            if (system.isSetChildProcedures()) {
                smlComponents.addAll(createComponentsForChildProcedures(system.getChildProcedures()));
            }
            if (!smlComponents.isEmpty()) {
                final Components components = createComponents(smlComponents);
                if (components != null && components.getComponentList() != null
                        && components.getComponentList().sizeOfComponentArray() > 0) {
                    xbSystem.setComponents(components);
                }
            }
        }
        if (!smlComponents.isEmpty()) {
            // TODO check for duplicated outputs
            system.getOutputs().addAll(getOutputsFromChilds(smlComponents));
            // TODO check if necessary
            // system.addFeatureOfInterest(getFeaturesFromChild(smlComponents));
        }
        // set outputs
        if (system.isSetOutputs()) {
            xbSystem.setOutputs(createOutputs(system.getOutputs()));
        }
    }

    private void addProcessModelValues(final ProcessModelType processModel, final ProcessModel sosProcessModel)
            throws OwsExceptionReport {
        // set inputs
        if (sosProcessModel.isSetInputs()) {
            processModel.setInputs(createInputs(sosProcessModel.getInputs()));
        }
        // set outputs
        if (sosProcessModel.isSetOutputs()) {
            processModel.setOutputs(createOutputs(sosProcessModel.getOutputs()));
        }
        // set method
        processModel.setMethod(createMethod(sosProcessModel.getMethod()));
    }

	private MethodPropertyType createMethod(final ProcessMethod method)
	{
		final MethodPropertyType xbMethod = MethodPropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
		final ProcessMethodType xbProcessMethod = xbMethod.addNewProcessMethod();
		final RulesDefinition xbRulesDefinition = xbProcessMethod.addNewRules().addNewRulesDefinition();
		if (method.getRulesDefinition().isSetDescription())
		{
			xbRulesDefinition.addNewDescription().setStringValue(method.getRulesDefinition().getDescription());
		}
		return xbMethod;
	}

	/**
     * Creates the identification section of the SensorML description.
     * 
     * @param xbIdentification
     *            Xml identification object
     * @param identifications
     *            SOS SWE representation.
     */
    private Identification[] createIdentification(final List<SmlIdentifier> identifications) {
        final Identification xbIdentification =
                Identification.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final IdentifierList xbIdentifierList = xbIdentification.addNewIdentifierList();
        for (final SmlIdentifier sosSMLIdentifier : identifications) {
            final Identifier xbIdentifier = xbIdentifierList.addNewIdentifier();
            if (sosSMLIdentifier.getName() != null) {
                xbIdentifier.setName(sosSMLIdentifier.getName());
            }
            final Term xbTerm = xbIdentifier.addNewTerm();
            xbTerm.setDefinition(sosSMLIdentifier.getDefinition());
            xbTerm.setValue(sosSMLIdentifier.getValue());
        }
        return new Identification[] { xbIdentification };
    }

    /**
     * Creates the classification section of the SensorML description.
     * 
     * @param xbClassification
     *            Xml classifications object
     * @param classifications
     *            SOS SWE representation.
     */
    private Classification[] createClassification(final List<SmlClassifier> classifications) {
        final Classification xbClassification =
                Classification.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final ClassifierList xbClassifierList = xbClassification.addNewClassifierList();
        for (final SmlClassifier sosSMLClassifier : classifications) {
            final Classifier xbClassifier = xbClassifierList.addNewClassifier();
            if (sosSMLClassifier.getName() != null) {
                xbClassifier.setName(sosSMLClassifier.getName());
            }
            final Term xbTerm = xbClassifier.addNewTerm();
            xbTerm.setValue(sosSMLClassifier.getValue());
            if (sosSMLClassifier.getDefinition() != null) {
                xbTerm.setDefinition(sosSMLClassifier.getDefinition());
            }
        }
        return new Classification[] { xbClassification };
    }

    /**
     * Creates the characteristics section of the SensorML description.
     * 
     * @param xbCharacteristics
     *            Xml characteristics object
     * @param list
     *            SOS SWE representation.
     * 
     * 
     * @throws OwsExceptionReport
     */
    private Characteristics[] createCharacteristics(final List<SmlCharacteristics> smlCharacteristics)
            throws OwsExceptionReport {
        final List<Characteristics> characteristicsList = new ArrayList<Characteristics>(smlCharacteristics.size());
        for (final SmlCharacteristics sosSMLCharacteristics : smlCharacteristics) {
            if (sosSMLCharacteristics.isSetAbstractDataRecord()) {
                final Characteristics xbCharacteristics =
                        Characteristics.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
                if (sosSMLCharacteristics.getDataRecord() instanceof SweSimpleDataRecord) {
                    final SimpleDataRecordType xbSimpleDataRecord =
                            (SimpleDataRecordType) xbCharacteristics.addNewAbstractDataRecord().substitute(
                                    SWEConstants.QN_SIMPLEDATARECORD_SWE_101, SimpleDataRecordType.type);
                    if (sosSMLCharacteristics.isSetTypeDefinition()) {
                        xbSimpleDataRecord.setDefinition(sosSMLCharacteristics.getTypeDefinition());
                    }
                    if (sosSMLCharacteristics.getDataRecord().isSetFields()) {
                        for (final SweField field : sosSMLCharacteristics.getDataRecord().getFields()) {
                            final AnyScalarPropertyType xbField = xbSimpleDataRecord.addNewField();
                            xbField.setName(field.getName());
                            addSweSimpleTypeToField(xbField, field.getElement());
                        }
                    }
                } else if (sosSMLCharacteristics.getDataRecord() instanceof SweDataRecord) {
                    throw new NoApplicableCodeException()
                            .withMessage(
                                    "The SWE characteristics type '%s' is not supported by this SOS for SensorML characteristics!",
                                    SweAggregateType.DataRecord);
                } else {
                    throw new NoApplicableCodeException()
                            .withMessage(
                                    "The SWE characteristics type '%s' is not supported by this SOS for SensorML characteristics!",
                                    sosSMLCharacteristics.getDataRecord().getClass().getName());
                }
                characteristicsList.add(xbCharacteristics);
            }
        }
        return characteristicsList.toArray(new Characteristics[characteristicsList.size()]);
    }

    private Documentation[] createDocumentationArray(final List<AbstractSmlDocumentation> sosDocumentation) {
        final List<Documentation> documentationList = new ArrayList<Documentation>(sosDocumentation.size());
        for (final AbstractSmlDocumentation abstractSosSMLDocumentation : sosDocumentation) {
            final Documentation documentation = Documentation.Factory.newInstance();
            if (abstractSosSMLDocumentation instanceof SmlDocumentation) {
                documentation.setDocument(createDocument((SmlDocumentation) abstractSosSMLDocumentation));
            } else if (abstractSosSMLDocumentation instanceof SmlDocumentationList) {
                documentation
                        .setDocumentList(createDocumentationList((SmlDocumentationList) abstractSosSMLDocumentation));
            }
            documentationList.add(documentation);
        }
        return documentationList.toArray(new Documentation[documentationList.size()]);
    }

    private Document createDocument(final SmlDocumentation sosDocumentation) {
        final Document document = Document.Factory.newInstance();
        if (sosDocumentation.isSetDescription()) {
            document.addNewDescription().setStringValue(sosDocumentation.getDescription());
        } else {
            document.addNewDescription().setStringValue("");
        }
        if (sosDocumentation.isSetDate()) {
            document.setDate(sosDocumentation.getDate().getValue().toDate());
        }
        if (sosDocumentation.isSetContact()) {
            document.addNewContact().addNewResponsibleParty().setIndividualName(sosDocumentation.getContact());
        }
        if (sosDocumentation.isSetFormat()) {
            document.setFormat(sosDocumentation.getFormat());
        }
        if (sosDocumentation.isSetVersion()) {
            document.setVersion(sosDocumentation.getVersion());
        }
        return document;
    }

    private DocumentList createDocumentationList(final SmlDocumentationList sosDocumentationList) {
        final DocumentList documentList = DocumentList.Factory.newInstance();
        if (sosDocumentationList.isSetDescription()) {
            documentList.addNewDescription().setStringValue(sosDocumentationList.getDescription());
        }
        if (sosDocumentationList.isSetMembers()) {
            for (final SmlDocumentationListMember sosMmember : sosDocumentationList.getMember()) {
                final net.opengis.sensorML.x101.DocumentListDocument.DocumentList.Member member =
                        documentList.addNewMember();
                member.setName(sosMmember.getName());
                member.setDocument(createDocument(sosMmember.getDocumentation()));
            }
        }
        return documentList;
    }

    /**
     * Creates the position section of the SensorML description.
     * 
     * @param xbPosition
     *            Xml position object
     * @param position
     *            SOS SWE representation.
     * 
     * 
     * @throws OwsExceptionReport
     */
    private Position createPosition(final SmlPosition position) throws OwsExceptionReport {
        final Position xbPosition = Position.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (position.getName() != null && !position.getName().isEmpty()) {
            xbPosition.setName(position.getName());
        }
        final PositionType xbSwePosition = xbPosition.addNewPosition();
        xbSwePosition.setFixed(position.isFixed());
        xbSwePosition.setReferenceFrame(position.getReferenceFrame());
        final VectorType xbVector = xbSwePosition.addNewLocation().addNewVector();
        for (final SweCoordinate<?> coordinate : position.getPosition()) {
            if (coordinate.getValue().getValue() != null
                    && (!coordinate.getValue().isSetValue() || !coordinate.getValue().getValue().equals(Double.NaN))) {
                // FIXME: SWE Common NS
                xbVector.addNewCoordinate().set(CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE_101, coordinate));
            }
        }
        return xbPosition;
    }

    /**
     * Creates the inputs section of the SensorML description.
     * 
     * @param xbInputs
     *            Xml inputs object
     * @param inputs
     *            SOS SWE representation.
     * 
     * 
     * @throws OwsExceptionReport
     */
    private Inputs createInputs(final List<SmlIo<?>> inputs) throws OwsExceptionReport {
        final Inputs xbInputs = Inputs.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final InputList xbInputList = xbInputs.addNewInputList();
        int counter = 1;
        for (final SmlIo<?> sosSMLIo : inputs) {
            if (!sosSMLIo.isSetName()) {
                sosSMLIo.setIoName("input_" + counter++);
            }
            addIoComponentPropertyType(xbInputList.addNewInput(), sosSMLIo);
        }
        return xbInputs;
    }

    /**
     * Creates the outputs section of the SensorML description.
     * 
     * @param xbOutputs
     *            Xml outputs object
     * @param outputs
     *            SOS SWE representation.
     * 
     * 
     * @throws OwsExceptionReport
     */
    private Outputs createOutputs(final List<SmlIo<?>> sosOutputs) throws OwsExceptionReport {
        final Outputs outputs = Outputs.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final OutputList outputList = outputs.addNewOutputList();
        final Set<String> definitions = new HashSet<String>();
        int counter = 1;
        final Set<String> outputNames = CollectionHelper.set();
        for (final SmlIo<?> sosSMLIo : sosOutputs) {
            if (!definitions.contains(sosSMLIo.getIoValue().getDefinition())) {
                if (!sosSMLIo.isSetName() || outputNames.contains(sosSMLIo.getIoName())) {
                    sosSMLIo.setIoName(getValidOutputName(counter++, outputNames));
                }
                outputNames.add(sosSMLIo.getIoName());
                addIoComponentPropertyType(outputList.addNewOutput(), sosSMLIo);
                definitions.add(sosSMLIo.getIoValue().getDefinition());
            }
        }
        return outputs;
    }

    private String getValidOutputName(int counter, final Set<String> outputNames) {
        String outputName = OUTPUT_PREFIX + counter;
        while (outputNames.contains(outputName)) {
            outputName =  OUTPUT_PREFIX + (++counter);
        }
        return outputName;
    }

    /**
     * Creates the components section of the SensorML description.
     * 
     * @param sosComponents
     *            SOS SWE representation.
     * @return encoded sml:components
     * @throws OwsExceptionReport
     */
    private Components createComponents(final List<SmlComponent> sosComponents) throws OwsExceptionReport {
        final Components components = Components.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final ComponentList componentList = components.addNewComponentList();
        for (final SmlComponent sosSMLComponent : sosComponents) {
            final Component component = componentList.addNewComponent();
            if (sosSMLComponent.getName() != null) {
                component.setName(sosSMLComponent.getName());
            }
            if (sosSMLComponent.getTitle() != null) {
                component.setTitle(sosSMLComponent.getTitle());
            }
            if (sosSMLComponent.getHref() != null) {
                component.setHref(sosSMLComponent.getHref());
            }
            if (sosSMLComponent.getProcess() != null) {
                XmlObject xmlObject = null;
                if (sosSMLComponent.getProcess().getSensorDescriptionXmlString() != null
                        && !sosSMLComponent.getProcess().getSensorDescriptionXmlString().isEmpty()) {
                    try {
                        xmlObject =
                                XmlObject.Factory.parse(sosSMLComponent.getProcess().getSensorDescriptionXmlString());

                    } catch (final XmlException xmle) {
                        throw new NoApplicableCodeException().causedBy(xmle).withMessage(
                                "Error while encoding SensorML child procedure description "
                                        + "from stored SensorML encoded sensor description with XMLBeans");
                    }
                } else {
                    xmlObject = createSensorDescriptionFromObject(sosSMLComponent.getProcess());
                }
                if (xmlObject != null) {
                    AbstractProcessType xbProcess = null;
                    SchemaType schemaType = null;
                    if (xmlObject instanceof SensorMLDocument) {
                        final SensorMLDocument smlDoc = (SensorMLDocument) xmlObject;
                        for (final Member member : smlDoc.getSensorML().getMemberArray()) {
                            schemaType = member.getProcess().schemaType();
                            xbProcess = member.getProcess();
                        }
                    } else if (xmlObject instanceof AbstractProcessType) {
                        schemaType = xmlObject.schemaType();
                        xbProcess = (AbstractProcessType) xmlObject;
                    } else {
                        throw new NoApplicableCodeException()
                                .withMessage("The sensor type is not supported by this SOS");
                    }
                    // TODO add feature/parentProcs/childProcs to component - is this already done?
                    component.setProcess(xbProcess);
                    if (schemaType == null) {
                        schemaType = xbProcess.schemaType();
                    }
                    component.getProcess().substitute(getQnameForType(schemaType), schemaType);
                }
            }
        }
        return components;
    }

    /**
     * Adds a SOS SWE simple type to a XML SWE field.
     * 
     * @param iSosSweSimpleType
     * @param xbField
     * 
     * @param xbField
     *            XML SWE field
     * @param sosSweData
     *            SOS SWE simple type.
     * 
     * 
     * @throws OwsExceptionReport
     */
    private void addSweSimpleTypeToField(final AnyScalarPropertyType xbField,
            final SweAbstractDataComponent sosSweData) throws OwsExceptionReport {
        final Encoder<?, SweAbstractDataComponent> encoder = CodingRepository.getInstance()
        		.getEncoder(new XmlEncoderKey(SWEConstants.NS_SWE_101, SweDataArray.class));
        if (encoder != null) {
            final XmlObject encoded = (XmlObject) encoder.encode(sosSweData);
            if (sosSweData instanceof SweAbstractSimpleType) {
                final SweAbstractSimpleType<?> sosSweSimpleType = (SweAbstractSimpleType<?>) sosSweData;
                switch (sosSweSimpleType.getSimpleType()) {
                case Boolean:
                    xbField.addNewBoolean().set(encoded);
                    break;
                case Category:
                    xbField.addNewCategory().set(encoded);
                    break;
                case Count:
                    xbField.addNewCount().set(encoded);
                    break;
                case Quantity:
                    xbField.addNewQuantity().set(encoded);
                    break;
                case Text:
                    xbField.addNewText().set(encoded);
                    break;
                case Time:
                    xbField.addNewTime().set(encoded);
                    break;
                default:
                    throw new NoApplicableCodeException().withMessage(
                            "The SWE simpleType '%s' is not supported by this SOS SensorML encoder!", sosSweSimpleType
                                    .getSimpleType().name());
                }
            } else {
                throw new NoApplicableCodeException().withMessage(
                        "The SosSweAbstractDataComponent '%s' is not supported by this SOS SensorML encoder!",
                        sosSweData);
            }
        } else {
            throw new NoApplicableCodeException().withMessage("The %s is not supported by this SOS for SWE fields!",
                    sosSweData.getClass().getSimpleName());
        }
    }

    /**
     * Adds a SOS SWE simple type to a XML SML IO component.
     * 
     * @param ioComponentPopertyType
     *            SML IO component
     * @param sosSMLInput
     *            SOS SWE simple type.
     * 
     * 
     * @throws OwsExceptionReport
     */
    private void addIoComponentPropertyType(final IoComponentPropertyType ioComponentPopertyType,
            final SmlIo<?> sosSMLIO) throws OwsExceptionReport {
        ioComponentPopertyType.setName(sosSMLIO.getIoName());
        final XmlObject encodeObjectToXml = CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE_101, sosSMLIO.getIoValue());
        switch (sosSMLIO.getIoValue().getSimpleType()) {
        case Boolean:
            ioComponentPopertyType.addNewBoolean().set(encodeObjectToXml);
            break;
        case Category:
            ioComponentPopertyType.addNewCategory().set(encodeObjectToXml);
            break;
        case Count:
            ioComponentPopertyType.addNewCount().set(encodeObjectToXml);
            break;
        case CountRange:
            ioComponentPopertyType.addNewCountRange().set(encodeObjectToXml);
            break;
        case ObservableProperty:
            ioComponentPopertyType.addNewObservableProperty().set(encodeObjectToXml);
            break;
        case Quantity:
            ioComponentPopertyType.addNewQuantity().set(encodeObjectToXml);
            break;
        case QuantityRange:
            ioComponentPopertyType.addNewQuantityRange().set(encodeObjectToXml);
            break;
        case Text:
            ioComponentPopertyType.addNewText().set(encodeObjectToXml);
            break;
        case Time:
            ioComponentPopertyType.addNewTime().set(encodeObjectToXml);
            break;
        case TimeRange:
            ioComponentPopertyType.addNewTimeRange().set(encodeObjectToXml);
            break;
        default:

        }
    }

    private QName getQnameForType(final SchemaType type) {
        if (type == SystemType.type) {
            return SensorMLConstants.SYSTEM_QNAME;
        } else if (type == ProcessModelType.type) {
            return SensorMLConstants.PROCESS_MODEL_QNAME;
        }
        return SensorMLConstants.ABSTRACT_PROCESS_QNAME;
    }

    protected void addSpecialCapabilities(final AbstractProcess abstractProcess) {
        if (abstractProcess.isSetFeaturesOfInterest()) {
        	abstractProcess.addCapabilities(createCapabilitiesFrom(                    
            		SensorMLConstants.ELEMENT_NAME_FEATURES_OF_INTEREST,
                    SensorMLConstants.FEATURE_OF_INTEREST_FIELD_DEFINITION,
                    SensorMLConstants.FEATURE_OF_INTEREST_FIELD_NAME,
                    abstractProcess.getFeaturesOfInterest()));
        }

        if (abstractProcess.isSetOfferings()) {
        	abstractProcess.addCapabilities(createCapabilitiesFrom(                    
            		SensorMLConstants.ELEMENT_NAME_OFFERINGS,
            		SensorMLConstants.OFFERING_FIELD_DEFINITION,
            		convertOfferingsToMap(abstractProcess.getOfferings())));
        }

        if (abstractProcess.isSetParentProcedures()) {
        	abstractProcess.addCapabilities(createCapabilitiesFrom(                    
            		SensorMLConstants.ELEMENT_NAME_PARENT_PROCEDURES,
            		SensorMLConstants.PARENT_PROCEDURE_FIELD_DEFINITION,
                    SensorMLConstants.PARENT_PROCEDURE_FIELD_NAME,
                    abstractProcess.getParentProcedures()));
        }        
    }

    protected Map<String,String> convertOfferingsToMap(final Set<SosOffering> offerings) {
    	final Map<String,String> valueNamePairs = new HashMap<String,String>();
    	for (final SosOffering offering : offerings) {
    		valueNamePairs.put(offering.getOfferingIdentifier(), offering.getOfferingName());
    	}
    	return valueNamePairs;
    }

    protected SmlCapabilities createCapabilitiesFrom(final String elementName,
            final String fieldDefinition, final String fieldName, final Set<String> values) {
    	final Map<String,String> valueNamePairs = new HashMap<String,String>();
    	int counter = 0;
    	for (final String value : values) {
    		final String name = values.size() > 1 ? fieldName + ++counter : fieldName; 
    		valueNamePairs.put(value, name);
    	}
    	return createCapabilitiesFrom(elementName, fieldDefinition, valueNamePairs);
    }

    protected SmlCapabilities createCapabilitiesFrom(final String elementName,
            final String fieldDefinition, final Map<String,String> valueNamePairs ) {
        final SmlCapabilities capabilities = new SmlCapabilities();
        capabilities.setName(elementName);
        final SweSimpleDataRecord simpleDataRecord = new SweSimpleDataRecord();
        final List<SweField> fields = new ArrayList<SweField>(valueNamePairs.size());
        final List<String> values = new ArrayList<String>(valueNamePairs.keySet());
        Collections.sort(values);
        for (final String value : values) {
            final SweText text = new SweText();
            text.setDefinition(fieldDefinition);
            text.setValue(value);
            fields.add(new SweField(valueNamePairs.get(value), text));        	
        }
        simpleDataRecord.setFields(fields);
        capabilities.setDataRecord(simpleDataRecord);
        return capabilities;
    }

    protected List<SmlComponent> createComponentsForChildProcedures(
            final Set<SosProcedureDescription> childProcedures) throws CodedException {
        final List<SmlComponent> smlComponents = new LinkedList<SmlComponent>();
        int childCount = 0;
        for (final SosProcedureDescription childProcedure : childProcedures) {
            childCount++;
            final SmlComponent component = new SmlComponent("component" + childCount);
            component.setTitle(childProcedure.getIdentifier());

            if (ServiceConfiguration.getInstance().isEncodeFullChildrenInDescribeSensor()
                    && childProcedure instanceof AbstractSensorML) {
                component.setProcess((AbstractSensorML) childProcedure);
            } else {
                try {
                    if (BindingRepository.getInstance()
                            .isBindingSupported(BindingConstants.KVP_BINDING_ENDPOINT)) {
                        final String version = ServiceOperatorRepository.getInstance()
                        		.getSupportedVersions(SosConstants.SOS).contains(Sos2Constants.SERVICEVERSION) ? Sos2Constants.SERVICEVERSION
                                        : Sos1Constants.SERVICEVERSION;

                        component.setHref(SosHelper.getDescribeSensorUrl(version, ServiceConfiguration.getInstance()
                                .getServiceURL(), childProcedure.getIdentifier(),
                                BindingConstants.KVP_BINDING_ENDPOINT));
                    } else {
                        component.setHref(childProcedure.getIdentifier());
                    }
                } catch (final UnsupportedEncodingException uee) {
                    throw new NoApplicableCodeException().withMessage("Error while encoding DescribeSensor URL")
                            .causedBy(uee);
                }
            }
            smlComponents.add(component);
        }
        return smlComponents;
    }

    protected Collection<? extends SmlIo<?>> getOutputsFromChilds(final List<SmlComponent> smlComponents) {
        final Set<SmlIo<?>> outputs = CollectionHelper.set();
        for (final SmlComponent sosSMLComponent : smlComponents) {
            if (sosSMLComponent.isSetProcess()) {
                if (sosSMLComponent.getProcess() instanceof SensorML) {
                    final SensorML sensorML = (SensorML) sosSMLComponent.getProcess();
                    if (sensorML.isSetMembers()) {
                        for (final AbstractProcess abstractProcess : sensorML.getMembers()) {
                            if (abstractProcess.isSetOutputs()) {
                                outputs.addAll(abstractProcess.getOutputs());
                            }
                        }
                    }
                } else if (sosSMLComponent.getProcess() instanceof AbstractProcess) {
                    final AbstractProcess abstractProcess = (AbstractProcess) sosSMLComponent.getProcess();
                    if (abstractProcess.isSetOutputs()) {
                        outputs.addAll(abstractProcess.getOutputs());
                    }
                }
            }
        }
        return outputs;
    }

    protected Collection<String> getFeaturesFromChild(final List<SmlComponent> smlComponents) {
        final Set<String> features = CollectionHelper.set();
        for (final SmlComponent sosSMLComponent : smlComponents) {
            if (sosSMLComponent.isSetProcess() && sosSMLComponent.getProcess().isSetFeaturesOfInterest()) {
                features.addAll(sosSMLComponent.getProcess().getFeaturesOfInterest());
            }
        }
        return features;
    }
}
