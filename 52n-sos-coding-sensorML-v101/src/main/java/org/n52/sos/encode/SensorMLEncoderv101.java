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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import net.opengis.sensorML.x101.OutputsDocument.Outputs;
import net.opengis.sensorML.x101.OutputsDocument.Outputs.OutputList;
import net.opengis.sensorML.x101.PersonDocument.Person;
import net.opengis.sensorML.x101.PositionDocument.Position;
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
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.AbstractProcess;
import org.n52.sos.ogc.sensorML.AbstractSensorML;
import org.n52.sos.ogc.sensorML.ProcessModel;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.SmlContact;
import org.n52.sos.ogc.sensorML.SmlPerson;
import org.n52.sos.ogc.sensorML.SmlResponsibleParty;
import org.n52.sos.ogc.sensorML.System;
import org.n52.sos.ogc.sensorML.elements.AbstractSosSMLDocumentation;
import org.n52.sos.ogc.sensorML.elements.SosSMLCapabilities;
import org.n52.sos.ogc.sensorML.elements.SosSMLCharacteristics;
import org.n52.sos.ogc.sensorML.elements.SosSMLClassifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLComponent;
import org.n52.sos.ogc.sensorML.elements.SosSMLDocumentation;
import org.n52.sos.ogc.sensorML.elements.SosSMLDocumentationList;
import org.n52.sos.ogc.sensorML.elements.SosSMLDocumentationListMember;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLIo;
import org.n52.sos.ogc.sensorML.elements.SosSMLPosition;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.sos.SosProcedureDescriptionUnknowType;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SWEConstants.SweAggregateType;
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.SosSweSimpleDataRecord;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorMLEncoderv101 implements Encoder<XmlObject, Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorMLEncoderv101.class);

    private static final Map<SupportedTypeKey, Set<String>> SUPPORTED_TYPES = Collections.singletonMap(
            SupportedTypeKey.ProcedureDescriptionFormat,
            Collections.singleton(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL));

    private static final Set<EncoderKey> ENCODER_KEYS = CodingHelper.encoderKeysForElements(SensorMLConstants.NS_SML,
            SosProcedureDescription.class, AbstractSensorML.class);

    public static final String FEATURE_OF_INTEREST_CAPABILITIES_NAME = "featureOfInterest";

    // FIXME use a proper URI/URN for this
    public static final String FEATURE_OF_INTEREST_CAPABILITIES_FIELD_DEFINITION = "FeatureOfInterest identifier";

    public static final String FEATURE_OF_INTEREST_CAPABILITIES_FIELD_NAME = "FeatureOfInterestID";

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
        // remove if GML 3.1.1 encoder is available
        nameSpacePrefixMap.put(GMLConstants.NS_GML, GMLConstants.NS_GML_PREFIX);
    }

    @Override
    public String getContentType() {
        return SensorMLConstants.SENSORML_CONTENT_TYPE;
    }

    @Override
    public XmlObject encode(final Object response) throws OwsExceptionReport {
        return encode(response, null);
    }

    @Override
    public XmlObject encode(final Object response, final Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        if (response instanceof AbstractSensorML) {
            return createSensorDescription((AbstractSensorML) response);
        }
        // FIXME workaround? if of type UnknowProcedureType try to parse the
        // description string, UNIT is missing "NOT_DEFINED"?!
        if (response instanceof SosProcedureDescriptionUnknowType) {

            final String procDescXMLString = ((SosProcedureDescription) response).getSensorDescriptionXmlString();
            final AbstractSensorML sensorDesc = new AbstractSensorML();
            sensorDesc.setSensorDescriptionXmlString(procDescXMLString);
            return createSensorDescriptionFromString(sensorDesc);
        }

        throw new UnsupportedEncoderInputException(this, response);
    }

    /**
     * creates sml:System
     * 
     * @param sensorDesc
     *            SensorML encoded system description
     * @param parentProcedureIds
     *            collection of parent procedure ids to include in the response
     * @param childProcedures
     *            map of child procedure SosSensorMLs keyed by child procedure
     *            id, if any
     * 
     * @return Returns XMLBeans representation of sml:System
     * 
     * 
     * @throws OwsExceptionReport
     */
    private XmlObject createSensorDescription(final AbstractSensorML sensorDesc) throws OwsExceptionReport {
        if (sensorDesc.getSensorDescriptionXmlString() != null
                && !sensorDesc.getSensorDescriptionXmlString().isEmpty()) {
            return createSensorDescriptionFromString(sensorDesc);
        } else {
            return createSensorDescriptionFromObject(sensorDesc);
        }
    }

    private XmlObject createSensorDescriptionFromString(final AbstractSensorML sensorDesc) throws OwsExceptionReport {
        try {
            final XmlObject xmlObject = XmlObject.Factory.parse(sensorDesc.getSensorDescriptionXmlString());
            if (xmlObject instanceof SensorMLDocument) {
                final SensorMLDocument sensorML = (SensorMLDocument) xmlObject;
                for (final Member member : sensorML.getSensorML().getMemberArray()) {
                    if (sensorDesc instanceof SensorML) {
                        for (final AbstractProcess absProcess : ((SensorML) sensorDesc).getMembers()) {
                            absProcess.setFeatureOfInterest(sensorDesc.getFeatureOfInterest());
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
                // TODO add values
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

    private XmlObject createSensorMLDescription(final SensorML smlSensorDesc) throws OwsExceptionReport {
        final SensorMLDocument sensorMLDoc =
                SensorMLDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final net.opengis.sensorML.x101.SensorMLDocument.SensorML xb_sensorML = sensorMLDoc.addNewSensorML();
        xb_sensorML.setVersion(SensorMLConstants.VERSION_V101);
        // TODO: Eike: set all other elements
        if (smlSensorDesc.isSetMembers()) {
            for (final AbstractProcess sml : smlSensorDesc.getMembers()) {
                if (sml instanceof System) {
                    // TODO create a method for each type
                    final SystemType xb_system =
                            (SystemType) xb_sensorML
                                    .addNewMember()
                                    .addNewProcess()
                                    .substitute(new QName(SensorMLConstants.NS_SML, SensorMLConstants.EN_SYSTEM),
                                            SystemType.Factory.newInstance().schemaType());
                    final System smlSystem = (System) sml;
                    // TODO howTo without explicit setting
                    smlSystem.setFeatureOfInterest(smlSensorDesc.getFeatureOfInterest());
                    addAbstractProcessValues(xb_system, smlSystem);
                    // set position
                    if (smlSystem.isSetPosition()) {
                        xb_system.setPosition(createPosition(smlSystem.getPosition()));
                    }
                    // set outputs
                    if (smlSystem.isSetOutputs()) {
                        xb_system.setOutputs(createOutputs(smlSystem.getOutputs()));
                    }
                }
            }
        }
        return sensorMLDoc; // projects/internal/2012_TideElbe --> Describe
                            // Sensor DAO
    }

    private ContactList createContactList(final List<SmlContact> contacts) {
        final ContactList xb_contacts = ContactList.Factory.newInstance();
        for (final SmlContact smlContact : contacts) {
            if (smlContact instanceof SmlPerson) {
                xb_contacts.addNewMember().addNewPerson().set(createPerson((SmlPerson) smlContact));
            } else if (smlContact instanceof SmlResponsibleParty) {
                xb_contacts.addNewMember().addNewResponsibleParty()
                        .set(createResponsibleParty((SmlResponsibleParty) smlContact));
            }
        }
        return xb_contacts;
    }

    private XmlObject createResponsibleParty(final SmlResponsibleParty smlRespParty) {
        final ResponsibleParty xb_respParty = ResponsibleParty.Factory.newInstance();
        if (smlRespParty.isSetIndividualName()) {
            xb_respParty.setIndividualName(smlRespParty.getInvidualName());
        }
        if (smlRespParty.isSetOrganizationName()) {
            xb_respParty.setOrganizationName(smlRespParty.getOrganizationName());
        }
        if (smlRespParty.isSetPositionName()) {
            xb_respParty.setPositionName(smlRespParty.getPositionName());
        }
        if (smlRespParty.isSetContactInfo()) {
            xb_respParty.setContactInfo(createContactInfo(smlRespParty));
        }
        return xb_respParty;
    }

    private ContactInfo createContactInfo(final SmlResponsibleParty smlRespParty) {
        final ContactInfo xb_contactInfo = ContactInfo.Factory.newInstance();
        if (smlRespParty.isSetHoursOfService()) {
            xb_contactInfo.setHoursOfService(smlRespParty.getHoursOfService());
        }
        if (smlRespParty.isSetContactInstructions()) {
            xb_contactInfo.setHoursOfService(smlRespParty.getContactInstructions());
        }
        if (smlRespParty.isSetOnlineResources()) {
            for (final String onlineResouce : smlRespParty.getOnlineResources()) {
                xb_contactInfo.addNewOnlineResource().setHref(onlineResouce);
            }
        }
        if (smlRespParty.isSetPhone()) {
            final Phone xb_phone = xb_contactInfo.addNewPhone();
            if (smlRespParty.isSetPhoneFax()) {
                for (final String fax : smlRespParty.getPhoneFax()) {
                    xb_phone.addFacsimile(fax);
                }
            }
            if (smlRespParty.isSetPhoneVoice()) {
                for (final String voice : smlRespParty.getPhoneVoice()) {
                    xb_phone.addVoice(voice);
                }
            }
        }
        if (smlRespParty.isSetAddress()) {
            final Address xb_address = xb_contactInfo.addNewAddress();
            if (smlRespParty.isSetDeliveryPoint()) {
                for (final String deliveryPoint : smlRespParty.getDeliveryPoint()) {
                    xb_address.addDeliveryPoint(deliveryPoint);
                }
            }
            if (smlRespParty.isSetCity()) {
                xb_address.setCity(smlRespParty.getCity());
            }
            if (smlRespParty.isSetAdministrativeArea()) {
                xb_address.setAdministrativeArea(smlRespParty.getAdministrativeArea());
            }
            if (smlRespParty.isSetPostalCode()) {
                xb_address.setPostalCode(smlRespParty.getPostalCode());
            }
            if (smlRespParty.isSetCountry()) {
                xb_address.setCountry(smlRespParty.getCountry());
            }
        }
        return xb_contactInfo;
    }

    private Person createPerson(final SmlPerson smlPerson) {
        final Person xb_person = Person.Factory.newInstance();
        if (smlPerson.isSetAffiliation()) {
            xb_person.setAffiliation(smlPerson.getAffiliation());
        }
        if (smlPerson.isSetEmail()) {
            xb_person.setEmail(smlPerson.getEmail());
        }
        if (smlPerson.isSetName()) {
            xb_person.setName(smlPerson.getName());
        }
        if (smlPerson.isSetPhoneNumber()) {
            xb_person.setPhoneNumber(smlPerson.getPhoneNumber());
        }
        if (smlPerson.isSetSurname()) {
            xb_person.setSurname(smlPerson.getSurname());
        }
        if (smlPerson.isSetUserID()) {
            xb_person.setUserID(smlPerson.getUserID());
        }
        return xb_person;
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

    private Classifier[] createClassifierArray(final List<SosSMLClassifier> classifications) {
        final Classifier[] xb_classifier = new Classifier[classifications.size()];
        for (int i = 0; i < xb_classifier.length; i++) {
            if (classifications.get(i) != null) {
                xb_classifier[i] = Classifier.Factory.newInstance();
                xb_classifier[i].setName(classifications.get(i).getName());
                final Term term = xb_classifier[i].addNewTerm();
                term.setDefinition(classifications.get(i).getDefinition());
                term.setValue(classifications.get(i).getValue());
            }
        }
        return xb_classifier;
    }

    private XmlObject createProcessDescription(final AbstractProcess sensorDesc) throws OwsExceptionReport {
        if (sensorDesc instanceof System) {
            final System system = (System) sensorDesc;
            final SystemDocument xbSystemDoc =
                    SystemDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            final SystemType xbSystem = xbSystemDoc.addNewSystem();
            addSystemValues(xbSystem, system);
            return xbSystemDoc;
        } else if (sensorDesc instanceof ProcessModel) {
            // TODO: set values
            // ProcessModel processModel = (ProcessModel) sensorDesc;
            final ProcessModelDocument xbProcessModel =
                    ProcessModelDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            return xbProcessModel;
        } else {
            throw new NoApplicableCodeException()
                    .withMessage("The sensor description type is not supported by this service!");
        }
    }

    // TODO refactor/rename
    private void addAbstractProcessValues(final AbstractProcessType abstractProcess,
            final AbstractProcess sosAbstractProcess) throws OwsExceptionReport {

        setCapabilitiesForFeaturesOfInterest(sosAbstractProcess, abstractProcess);

        // set description
        if (sosAbstractProcess.isSetDescriptions()) {
            abstractProcess.addNewDescription().setStringValue(createDescription(sosAbstractProcess.getDescriptions()));
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

    private void addSystemValues(final SystemType xbSystem, final System system) throws OwsExceptionReport {
        // set inputs
        if (system.isSetInputs()) {
            xbSystem.setInputs(createInputs(system.getInputs()));
        }
        // set outputs
        if (system.isSetOutputs()) {
            xbSystem.setOutputs(createOutputs(system.getOutputs()));
        }
        // set position
        if (system.isSetPosition()) {
            xbSystem.setPosition(createPosition(system.getPosition()));
        }
        // set components
        if (system.isSetComponents()) {
            final Components components = createComponents(system.getComponents());
            if (components != null && components.getComponentList() != null
                    && components.getComponentList().sizeOfComponentArray() > 0) {
                xbSystem.setComponents(createComponents(system.getComponents()));
            }
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
    }

    /**
     * Creates the identification section of the SensorML description.
     * 
     * @param xbIdentification
     *            Xml identification object
     * @param identifications
     *            SOS SWE representation.
     */
    private Identification[] createIdentification(final List<SosSMLIdentifier> identifications) {
        final Identification xbIdentification =
                Identification.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final IdentifierList xbIdentifierList = xbIdentification.addNewIdentifierList();
        for (final SosSMLIdentifier sosSMLIdentifier : identifications) {
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
    private Classification[] createClassification(final List<SosSMLClassifier> classifications) {
        final Classification xbClassification =
                Classification.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final ClassifierList xbClassifierList = xbClassification.addNewClassifierList();
        for (final SosSMLClassifier sosSMLClassifier : classifications) {
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
    private Characteristics[] createCharacteristics(final List<SosSMLCharacteristics> smlCharacteristics)
            throws OwsExceptionReport {
        final List<Characteristics> characteristicsList = new ArrayList<Characteristics>(smlCharacteristics.size());
        for (final SosSMLCharacteristics sosSMLCharacteristics : smlCharacteristics) {
            if (sosSMLCharacteristics.isSetAbstractDataRecord()) {
                final Characteristics xbCharacteristics =
                        Characteristics.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
                if (sosSMLCharacteristics.getDataRecord() instanceof SosSweSimpleDataRecord) {
                    final SimpleDataRecordType xbSimpleDataRecord =
                            (SimpleDataRecordType) xbCharacteristics.addNewAbstractDataRecord().substitute(
                                    SWEConstants.QN_SIMPLEDATARECORD_SWE_101, SimpleDataRecordType.type);
                    if (sosSMLCharacteristics.isSetTypeDefinition()) {
                        xbSimpleDataRecord.setDefinition(sosSMLCharacteristics.getTypeDefinition());
                    }
                    if (sosSMLCharacteristics.getDataRecord().isSetFields()) {
                        for (final SosSweField field : sosSMLCharacteristics.getDataRecord().getFields()) {
                            final AnyScalarPropertyType xbField = xbSimpleDataRecord.addNewField();
                            xbField.setName(field.getName());
                            addSweSimpleTypeToField(xbField, field.getElement());
                        }
                    }
                } else if (sosSMLCharacteristics.getDataRecord() instanceof SosSweDataRecord) {
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

    private Documentation[] createDocumentationArray(final List<AbstractSosSMLDocumentation> sosDocumentation) {
        final List<Documentation> documentationList = new ArrayList<Documentation>(sosDocumentation.size());
        for (final AbstractSosSMLDocumentation abstractSosSMLDocumentation : sosDocumentation) {
            final Documentation documentation = Documentation.Factory.newInstance();
            if (abstractSosSMLDocumentation instanceof SosSMLDocumentation) {
                documentation.setDocument(createDocument((SosSMLDocumentation) abstractSosSMLDocumentation));
            } else if (abstractSosSMLDocumentation instanceof SosSMLDocumentationList) {
                documentation
                        .setDocumentList(createDocumentationList((SosSMLDocumentationList) abstractSosSMLDocumentation));
            }
            documentationList.add(documentation);
        }
        return documentationList.toArray(new Documentation[documentationList.size()]);
    }

    private Document createDocument(final SosSMLDocumentation sosDocumentation) {
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

    private DocumentList createDocumentationList(final SosSMLDocumentationList sosDocumentationList) {
        final DocumentList documentList = DocumentList.Factory.newInstance();
        if (sosDocumentationList.isSetDescription()) {
            documentList.addNewDescription().setStringValue(sosDocumentationList.getDescription());
        }
        if (sosDocumentationList.isSetMembers()) {
            for (final SosSMLDocumentationListMember sosMmember : sosDocumentationList.getMember()) {
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
    private Position createPosition(final SosSMLPosition position) throws OwsExceptionReport {
        final Position xbPosition = Position.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (position.getName() != null && !position.getName().isEmpty()) {
            xbPosition.setName(position.getName());
        }
        final PositionType xbSwePosition = xbPosition.addNewPosition();
        xbSwePosition.setFixed(position.isFixed());
        xbSwePosition.setReferenceFrame(position.getReferenceFrame());
        final VectorType xbVector = xbSwePosition.addNewLocation().addNewVector();
        for (final SosSweCoordinate<?> coordinate : position.getPosition()) {
            if (coordinate.getValue().getValue() != null
                    && (!coordinate.getValue().isSetValue() || !coordinate.getValue().getValue().equals(Double.NaN))) {
                // FIXME: SWE Common NS
                xbVector.addNewCoordinate().set(CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE, coordinate));
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
    private Inputs createInputs(final List<SosSMLIo<?>> inputs) throws OwsExceptionReport {
        final Inputs xbInputs = Inputs.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final InputList xbInputList = xbInputs.addNewInputList();
        for (final SosSMLIo<?> sosSMLIo : inputs) {
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
    private Outputs createOutputs(final List<SosSMLIo<?>> sosOutputs) throws OwsExceptionReport {
        final Outputs outputs = Outputs.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final OutputList outputList = outputs.addNewOutputList();
        for (final SosSMLIo<?> sosSMLIo : sosOutputs) {
            addIoComponentPropertyType(outputList.addNewOutput(), sosSMLIo);
        }
        return outputs;
    }

    /**
     * Creates the components section of the SensorML description.
     * 
     * @param xbComponents
     *            Xml components object
     * @param sosComponents
     *            SOS SWE representation.
     * @param components
     * 
     * 
     * @throws OwsExceptionReport
     */
    private Components createComponents(final List<SosSMLComponent> sosComponents) throws OwsExceptionReport {
        final Components components = Components.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final ComponentList componentList = components.addNewComponentList();
        for (final SosSMLComponent sosSMLComponent : sosComponents) {
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
            final SosSweAbstractDataComponent sosSweData) throws OwsExceptionReport {
        final Encoder<?, SosSweAbstractDataComponent> encoder =
                Configurator.getInstance().getCodingRepository()
                        .getEncoder(new XmlEncoderKey(SWEConstants.NS_SWE, SosSweDataArray.class));
        if (encoder != null) {
            final XmlObject encoded = (XmlObject) encoder.encode(sosSweData);
            if (sosSweData instanceof SosSweAbstractSimpleType) {
                final SosSweAbstractSimpleType<?> sosSweSimpleType = (SosSweAbstractSimpleType) sosSweData;
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
            final SosSMLIo<?> sosSMLIO) throws OwsExceptionReport {
        ioComponentPopertyType.setName(sosSMLIO.getIoName());
        final XmlObject encodeObjectToXml = CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE, sosSMLIO.getIoValue());
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

    protected void setCapabilitiesForFeaturesOfInterest(final AbstractProcess sosAbstractProcess,
            final AbstractProcessType abstractProcess) throws OwsExceptionReport {
        if (sosAbstractProcess.isSetFeatureOfInterest(sosAbstractProcess.getIdentifier())) {
            final SosSMLCapabilities featureCapabilities =
                    createCapabilitiesFromFeatures(sosAbstractProcess.getFeatureOfInterest(sosAbstractProcess
                            .getIdentifier()));
            final Capabilities xbCapabilities = abstractProcess.addNewCapabilities();
            if (featureCapabilities.getName() != null) {
                xbCapabilities.setName(featureCapabilities.getName());
            }

            final SimpleDataRecordType xbSimpleDataRecord =
                    (SimpleDataRecordType) xbCapabilities.addNewAbstractDataRecord().substitute(
                            SWEConstants.QN_SIMPLEDATARECORD_SWE_101, SimpleDataRecordType.type);
            if (featureCapabilities.getDataRecord().isSetFields()) {
                for (final SosSweField field : featureCapabilities.getDataRecord().getFields()) {
                    final AnyScalarPropertyType xbField = xbSimpleDataRecord.addNewField();
                    xbField.setName(field.getName());
                    addSweSimpleTypeToField(xbField, field.getElement());

                }
            }
        }
    }

    private SosSMLCapabilities createCapabilitiesFromFeatures(final Set<String> featureOfInterest) {
        final SosSMLCapabilities capabilities = new SosSMLCapabilities();
        capabilities.setName(FEATURE_OF_INTEREST_CAPABILITIES_NAME);
        final SosSweSimpleDataRecord simpleDataRecord = new SosSweSimpleDataRecord();
        final List<SosSweField> fields = new ArrayList<SosSweField>(featureOfInterest.size());
        for (final String foiID : featureOfInterest) {
            final SosSweText text = new SosSweText();
            text.setDefinition(FEATURE_OF_INTEREST_CAPABILITIES_FIELD_DEFINITION);
            text.setValue(foiID);
            fields.add(new SosSweField(FEATURE_OF_INTEREST_CAPABILITIES_FIELD_NAME, text));
        }
        simpleDataRecord.setFields(fields);
        capabilities.setDataRecord(simpleDataRecord);
        return capabilities;
    }
}
