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

import static java.lang.Boolean.TRUE;

import java.util.Collections;
import java.util.Set;

import org.n52.sos.config.SettingDefinition;
import org.n52.sos.config.SettingDefinitionGroup;
import org.n52.sos.config.SettingDefinitionProvider;
import org.n52.sos.config.SettingsManager;
import org.n52.sos.config.annotation.Configurable;
import org.n52.sos.config.annotation.Setting;
import org.n52.sos.config.settings.BooleanSettingDefinition;
import org.n52.sos.config.settings.StringSettingDefinition;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.Validation;

/**
 * This class provides all settings to configure the sensor description generation.
 * 
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 * @since 4.0.0
 */
@Configurable
public class ProcedureDescriptionSettings implements SettingDefinitionProvider {

	private static final SettingDefinitionGroup GROUP = new SettingDefinitionGroup().
			setTitle("Procedure Description").
			setDescription("Settings to configure the procedure description generation and enrichment feature.").
			setOrder(4.2023f);

	public static final String IDENTIFIER_LONG_NAME_DEFINITION = "procedureDesc.IDENTIFIER_LONG_NAME_DEFINITION";
	public static final String IDENTIFIER_SHORT_NAME_DEFINITION = "procedureDesc.IDENTIFIER_SHORT_NAME_DEFINITION";
	public static final String DESCRIPTION_TEMPLATE = "procedureDesc.DESCRIPTION_TEMPLATE";
	public static final String GENERATE_CLASSIFICATION = "procedureDesc.GENERATE_CLASSIFICATION";
	public static final String CLASSIFIER_INTENDED_APPLICATION_DEFINITION = "procedureDesc.CLASSIFIER_INTENDED_APPLICATION_DEFINITION";
	public static final String CLASSIFIER_INTENDED_APPLICATION_VALUE = "procedureDesc.CLASSIFIER_INTENDED_APPLICATION_VALUE";
	public static final String CLASSIFIER_SENSOR_TYPE_DEFINITION = "procedureDesc.CLASSIFIER_SENSOR_TYPE_DEFINITION";
	public static final String CLASSIFIER_SENSOR_TYPE_VALUE = "procedureDesc.CLASSIFIER_SENSOR_TYPE_VALUE";
	public static final String LAT_LONG_UOM = "procedureDescLAT_LONG_UOM";
	public static final String ALTITUDE_UOM = "procedureDescALTITUDE_UOM";
	public static final String USE_SERVICE_CONTACT_AS_SENSOR_CONTACT = "procedureDesc.USE_SERVICE_CONTACT_AS_SENSOR_CONTACT";
	public static final String PROCESS_METHOD_RULES_DEFINITION_DESCRIPTION_TEMPLATE = "procedureDesc.PROCESS_METHOD_RULES_DEFINITION_DESCRIPTION_TEMPLATE";

	private static final StringSettingDefinition IDENTIFIER_LONG_NAME_DEFINITION_DEFINITION = new StringSettingDefinition()
	.setGroup(GROUP)
	.setOrder(1)
	.setKey(ProcedureDescriptionSettings.IDENTIFIER_LONG_NAME_DEFINITION)
	.setDefaultValue("urn:ogc:def:identifier:OGC:1.0:longname")
	.setTitle("Identifier 'longname' definition")
	.setDescription("The definition for the sml:identification holding the 'longname'. Used only if the sensor description is <b>generated</b>.");

	private static final StringSettingDefinition IDENTIFIER_SHORT_NAME_DEFINITION_DEFINITION = new StringSettingDefinition()
	.setGroup(GROUP)
	.setOrder(2)
	.setKey(ProcedureDescriptionSettings.IDENTIFIER_SHORT_NAME_DEFINITION)
	.setDefaultValue("urn:ogc:def:identifier:OGC:1.0:shortname")
	.setTitle("Identifier 'shortname' definition")
	.setDescription("The definition for the sml:identification holding the 'shortname'. Used only if the sensor description is <b>generated</b>.");

	private static final StringSettingDefinition DESCRIPTION_TEMPLATE_DEFINITION = new StringSettingDefinition()
	.setGroup(GROUP)
	.setOrder(3)
	.setKey(ProcedureDescriptionSettings.DESCRIPTION_TEMPLATE)
	.setDefaultValue("The '%s' with the id '%s' observes the following properties: '%s'.")
	.setTitle("Description template")
	.setDescription("The template used to generate a description using the sensor identifier and the observed properties " +
			"related. The template MUST contain '%s' three times. The first one will be replaced with 'sensor system' or " +
			"'procedure' depending if it's spatial or non-spatial. The seconde one will be replaced with the sensor id and" +
			" the third with a comma separated list of properties: e.g. <i>The %s with the id '%s' observes the following " +
			"properties: '%s'.</i>.");

	private static final BooleanSettingDefinition GENERATE_CLASSIFICATION_DEFINITION = new BooleanSettingDefinition()
	.setGroup(GROUP)
	.setOrder(4)
	.setKey(ProcedureDescriptionSettings.GENERATE_CLASSIFICATION)
	.setDefaultValue(TRUE)
	.setTitle("Generate classification")
	.setDescription("Should the classifiers for 'intendedApplication' and/or 'sensorType' be generated using the values from the next two settings?");

	private static final StringSettingDefinition CLASSIFIER_INTENDED_APPLICATION_DEFINITION_DEFINITION = new StringSettingDefinition()
	.setGroup(GROUP)
	.setOrder(5)
	.setKey(ProcedureDescriptionSettings.CLASSIFIER_INTENDED_APPLICATION_DEFINITION)
	.setDefaultValue("urn:ogc:def:classifier:OGC:1.0:application")
	.setTitle("IntendedApplication definition")
	.setDescription("The definition that will be used for all procedures/sensors of this SOS instance as definition for the classifier 'intendedApllication' if the classification generation is activated.");

	private static final StringSettingDefinition CLASSIFIER_INTENDED_APPLICATION_VALUE_DEFINITION = new StringSettingDefinition()
	.setGroup(GROUP)
	.setOrder(6)
	.setKey(ProcedureDescriptionSettings.CLASSIFIER_INTENDED_APPLICATION_VALUE)
	.setDefaultValue("")
	.setOptional(true)
	.setTitle("IntendedApplication Value")
	.setDescription("The value that will be used for all procedures/sensors of this SOS instance as term for the classifier 'intendedApllication' if the classification generation is activated. In addition, if this field is <b>empty</b>, the classifier 'intendedApplication' will <b>not</b> be added.");

	private static final StringSettingDefinition CLASSIFIER_SENSOR_TYPE_DEFINITION_DEFINITION = new StringSettingDefinition()
	.setGroup(GROUP)
	.setOrder(7)
	.setKey(ProcedureDescriptionSettings.CLASSIFIER_SENSOR_TYPE_DEFINITION)
	.setDefaultValue("urn:ogc:def:classifier:OGC:1.0:sensorType")
	.setTitle("SensorType definition")
	.setDescription("The definition that will be used for all procedures/sensors of this SOS instance as definition for the classifier 'sensorType' if the classification generation is activated.");

	private static final StringSettingDefinition CLASSIFIER_SENSOR_TYPE_VALUE_DEFINITION = new StringSettingDefinition()
	.setGroup(GROUP)
	.setOrder(8)
	.setKey(ProcedureDescriptionSettings.CLASSIFIER_SENSOR_TYPE_VALUE)
	.setDefaultValue("")
	.setOptional(true)
	.setTitle("SensorType Value")
	.setDescription("The value that will be used for all procedures/sensors of this SOS instance as term for the classifier 'sensorType' if the classification generation is activated. In addition, if this field is <b>empty</b>, the classifier 'sensorType' will <b>not</b> be added.");

	private static final BooleanSettingDefinition USE_SERVICE_CONTACT_AS_SENSOR_CONTACT_DEFINITION = new BooleanSettingDefinition()
	.setGroup(GROUP)
	.setOrder(9)
	.setKey(ProcedureDescriptionSettings.USE_SERVICE_CONTACT_AS_SENSOR_CONTACT)
	.setDefaultValue(TRUE)
	.setTitle("Use service contact as procedure contact")
	.setDescription("Should the service contact be encoded as procedure contact if procedure description generation is activated.");
	
	private static final StringSettingDefinition LAT_LONG_UOM_DEFINITION = new StringSettingDefinition()
	.setGroup(GROUP)
	.setOrder(10)
	.setOptional(true)
	.setKey(LAT_LONG_UOM)
	.setDefaultValue("degree")
	.setTitle("Latitude &amp; Longitude UOM")
	.setDescription("The UOM for the latitude  &amp; longitude values of spatial procedures (e.g. sml:System). Something like 'degree', 'm'.");
	
	private static final StringSettingDefinition ALTITUDE_UOM_DEFINITION = new StringSettingDefinition()
	.setGroup(GROUP)
	.setOrder(10)
	.setOptional(true)
	.setKey(ALTITUDE_UOM)
	.setDefaultValue("m")
	.setTitle("Altitude UOM")
	.setDescription("The UOM for the altitude value of spatial procedures (e.g. sml:System). Something like 'm'.");
	
	private static final StringSettingDefinition PROCESS_METHOD_RULES_DEFINITION_DESCRIPTION_TEMPLATE_DEFINITION = new StringSettingDefinition()
	.setGroup(GROUP)
	.setOrder(11)
	.setKey(PROCESS_METHOD_RULES_DEFINITION_DESCRIPTION_TEMPLATE)
	.setDefaultValue("The procedure '%s' generates the following output(s): '%s'. The input(s) is/are unknown (this description is generated).")
	.setTitle("Description Template for the rules definition")
	.setDescription("The template used to generate a description using the procedure identifier and the observed properties. " +
			"The template MUST contain '%s' two times. The first one will be replaced with the sensor id and" +
			" the second with a comma separated list of properties: e.g. <i>The procedure '%s' generates the following output(s): '%s'. The " +
			"input(s) is/are unknown (this description is generated).</i>");

	private static final Set<? extends SettingDefinition<?, ?>> DEFINITIONS = CollectionHelper.<SettingDefinition<?,?>>set(
			IDENTIFIER_LONG_NAME_DEFINITION_DEFINITION,
			IDENTIFIER_SHORT_NAME_DEFINITION_DEFINITION,
			DESCRIPTION_TEMPLATE_DEFINITION,
			GENERATE_CLASSIFICATION_DEFINITION,
			CLASSIFIER_INTENDED_APPLICATION_DEFINITION_DEFINITION,
			CLASSIFIER_INTENDED_APPLICATION_VALUE_DEFINITION,
			CLASSIFIER_SENSOR_TYPE_DEFINITION_DEFINITION,
			CLASSIFIER_SENSOR_TYPE_VALUE_DEFINITION,
			USE_SERVICE_CONTACT_AS_SENSOR_CONTACT_DEFINITION,
			LAT_LONG_UOM_DEFINITION,
			ALTITUDE_UOM_DEFINITION,
			PROCESS_METHOD_RULES_DEFINITION_DESCRIPTION_TEMPLATE_DEFINITION
			);



	private static ProcedureDescriptionSettings instance = null;
	
    private String descriptionTemplate;
    private boolean generateClassification;
    private String classifierIntendedApplicationValue;
    private String classifierIntendedApplicationDefinition;
    private String classifierSensorTypeValue;
    private String classifierSensorTypeDefinition;
    private boolean useServiceContactAsSensorContact;
    private String identifierShortNameDefinition;
    private String identifierLongNameDefinition;

	private String latLongUom;

	private String altitudeUom;

	private String processMethodRulesDefinitionDescriptionTemplate;

	public static ProcedureDescriptionSettings getInstance()
	{
		if (instance == null) {
			instance = new ProcedureDescriptionSettings();
			SettingsManager.getInstance().configure(instance);
		}
		return instance;
	}


	@Override
	public Set<SettingDefinition<?, ?>> getSettingDefinitions()
	{
		return Collections.unmodifiableSet(DEFINITIONS);
	}
	
	@Setting(DESCRIPTION_TEMPLATE)
    public void setDescriptionTemplate(final String descriptionTemplate) 
	{
        Validation.notNullOrEmpty(DESCRIPTION_TEMPLATE, descriptionTemplate);
        this.descriptionTemplate = descriptionTemplate;
    }

    /**
     * @return Depends on configuration. Something like:<br>"<i>The '%s' with the id '%s' observes the
     *         following properties: '%s'.</i>"
     */
    public String getDescriptionTemplate() 
    {
        return descriptionTemplate;
    }

    public boolean isGenerateClassification()
    {
        return generateClassification;
    }

    @Setting(GENERATE_CLASSIFICATION)
    public void setSmlGenerationGenerateClassification(final boolean generateClassification)
    {
        this.generateClassification = generateClassification;
    }

    public String getClassifierIntendedApplicationValue() {
        return classifierIntendedApplicationValue;
    }

    @Setting(CLASSIFIER_INTENDED_APPLICATION_VALUE)
    public void setClassifierIntendedApplicationValue(final String classifierIntendedApplicationValue)
    {
        this.classifierIntendedApplicationValue =
        (classifierIntendedApplicationValue == null)
        ? "" : classifierIntendedApplicationValue;
    }

    public String getClassifierIntendedApplicationDefinition() 
    {
        return classifierIntendedApplicationDefinition;
    }

    @Setting(CLASSIFIER_INTENDED_APPLICATION_DEFINITION)
    public void setSmlGenerationClassifierIntendedApplicationDefinition(
            final String classifierIntendedApplicationDefinition)
    {
        Validation.notNull(CLASSIFIER_INTENDED_APPLICATION_DEFINITION, classifierIntendedApplicationDefinition);
        this.classifierIntendedApplicationDefinition = classifierIntendedApplicationDefinition;
    }

    public String getClassifierSensorTypeDefinition()
    {
        return classifierSensorTypeDefinition;
    }

    @Setting(CLASSIFIER_SENSOR_TYPE_DEFINITION)
    public void setClassifierSensorTypeDefinition(final String classifierSensorTypeDefinition)
    {
        Validation.notNull(CLASSIFIER_SENSOR_TYPE_DEFINITION, classifierSensorTypeDefinition);
        this.classifierSensorTypeDefinition = classifierSensorTypeDefinition;
    }

    public String getClassifierSensorTypeValue() 
    {
        return classifierSensorTypeValue;
    }

    @Setting(CLASSIFIER_SENSOR_TYPE_VALUE)
    public void setClassifierSensorTypeValue(final String classifierSensorTypeValue) 
    {
        this.classifierSensorTypeValue = (classifierSensorTypeValue == null) ? ""
                                                      : classifierSensorTypeValue;
    }

    public boolean isUseServiceContactAsSensorContact()
    {
        return useServiceContactAsSensorContact;
    }

    @Setting(USE_SERVICE_CONTACT_AS_SENSOR_CONTACT)
    public void setUseServiceContactAsSensorContact(
            final boolean useServiceContactAsSensorContact) 
    {
        Validation.notNull(USE_SERVICE_CONTACT_AS_SENSOR_CONTACT, useServiceContactAsSensorContact);
        this.useServiceContactAsSensorContact = useServiceContactAsSensorContact;
    }
    
    @Setting(IDENTIFIER_SHORT_NAME_DEFINITION)
    public void setIdentifierShortNameDefinition(final String identifierShortNameDefinition)
    {
        Validation.notNullOrEmpty(IDENTIFIER_SHORT_NAME_DEFINITION, identifierShortNameDefinition);
        this.identifierShortNameDefinition = identifierShortNameDefinition;
    }

    @Setting(IDENTIFIER_LONG_NAME_DEFINITION)
    public void setIdentifierLongNameDefinition(final String identifierLongNameDefinition)
    {
        Validation.notNullOrEmpty(IDENTIFIER_LONG_NAME_DEFINITION, identifierLongNameDefinition);
        this.identifierLongNameDefinition = identifierLongNameDefinition;
    }

    public String getIdentifierShortNameDefinition()
    {
        return identifierShortNameDefinition;
    }

    public String getIdentifierLongNameDefinition()
    {
        return identifierLongNameDefinition;
    }

    @Setting(LAT_LONG_UOM)
    public void setLatitudeUom(final String latLongUom)
    {
    	this.latLongUom = latLongUom;
    }

	public String getLatLongUom()
	{
		return latLongUom;
	}
	
	@Setting(ALTITUDE_UOM)
    public void setAltitudeUom(final String altitudeUom)
    {
    	this.altitudeUom = altitudeUom;
    }

	public String getAltitudeUom()
	{
		return altitudeUom;
	}

	@Setting(PROCESS_METHOD_RULES_DEFINITION_DESCRIPTION_TEMPLATE)
	public void setProcessMethodRulesDefinitionDescriptionTemplate(final String processMethodRulesDefinitionDescriptionTemplate)
	{
		Validation.notNullOrEmpty(PROCESS_METHOD_RULES_DEFINITION_DESCRIPTION_TEMPLATE, processMethodRulesDefinitionDescriptionTemplate);
		this.processMethodRulesDefinitionDescriptionTemplate = processMethodRulesDefinitionDescriptionTemplate;
	}

	/**
	 * @return Depends on configuration. Something like:<br>"<i>The procedure '%s' generates the following outputs: '%s'. The inputs are unknown (this description is generated).</i>"
	 */
	public String getProcessMethodRulesDefinitionDescriptionTemplate()
	{
		return processMethodRulesDefinitionDescriptionTemplate;
	}

}
