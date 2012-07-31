/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.ogc.sensorML;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.n52.sos.ogc.AbstractServiceResponseObject;
import org.n52.sos.ogc.sensorML.elements.SosSMLCapabilities;
import org.n52.sos.ogc.sensorML.elements.SosSMLCharacteristics;
import org.n52.sos.ogc.sensorML.elements.SosSMLClassifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLComponent;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLIo;
import org.n52.sos.ogc.sensorML.elements.SosSMLPosition;
import org.n52.sos.ogc.swe.SWEConstants.SensorMLType;
import org.n52.sos.ogc.swe.SWEConstants.SosSensorDescription;

/**
 * SOS internal representation of a sensor description
 */
public class SosSensorML extends AbstractServiceResponseObject {

	/** description type */
	private SosSensorDescription sosSensorDescriptionType;

	/** description as string */
	private String sensorDescriptionString;
	
	private String outputFormat;

	/** process type */
	private SensorMLType sensorMLType;

	/** sensor identifiers */
	private List<SosSMLIdentifier> identifications;

	/** sensor classifications */
	private List<SosSMLClassifier> classifications;

	/** sensor characteristics */
	private SosSMLCharacteristics characteristics;

	/** sensor capabilities */
	private SosSMLCapabilities capabilities;

	/** sensor position */
	private SosSMLPosition position;

	/** sensor inputs */
	private List<SosSMLIo> inputs;

	/** sensor outputs */
	private List<SosSMLIo> outputs;

	/** sensor components */
	private List<SosSMLComponent> components;
	
	private Collection<String> parentProcedureIDs;
	
	private Map<String,SosSensorML> childProcedures;

	/**
	 * default constructor
	 */
	public SosSensorML() {
	}

	/**
	 * Constructor
	 * 
	 * @param sosSensorDescriptionType
	 *            Sensor description type
	 * @param sensorDescriptionString
	 *            Sensor description string
	 */
	public SosSensorML(SosSensorDescription sosSensorDescriptionType,
			String sensorDescriptionString, String outputFormat) {
		super();
		this.sosSensorDescriptionType = sosSensorDescriptionType;
		this.sensorDescriptionString = sensorDescriptionString;
		this.outputFormat = outputFormat;
	}

	/**
	 * constructor
	 * 
	 * @param sosSensorDescriptionType
	 *            Sensor description type
	 * @param sensorMLType
	 *            SensorML type
	 * @param identifications
	 *            Sensor identifications
	 * @param classifications
	 *            Sensor classifications
	 * @param characteristics
	 *            Sensor characteristics
	 * @param capabilities
	 *            Sensor capabilities
	 * @param position
	 *            Sensor position
	 * @param inputs
	 *            Sensor inputs
	 * @param outputs
	 *            Sensor outputs
	 * @param components
	 *            Sensor components
	 */
	public SosSensorML(SosSensorDescription sosSensorDescriptionType,
			SensorMLType sensorMLType, List<SosSMLIdentifier> identifications,
			List<SosSMLClassifier> classifications,
			SosSMLCharacteristics characteristics,
			SosSMLCapabilities capabilities, SosSMLPosition position,
			List<SosSMLIo> inputs, List<SosSMLIo> outputs,
			List<SosSMLComponent> components) {
		super();
		this.sosSensorDescriptionType = sosSensorDescriptionType;
		this.sensorMLType = sensorMLType;
		this.identifications = identifications;
		this.classifications = classifications;
		this.characteristics = characteristics;
		this.capabilities = capabilities;
		this.position = position;
		this.inputs = inputs;
		this.outputs = outputs;
		this.components = components;
	}

	/**
	 * Get sensor description type
	 * 
	 * @return the sosSensorDescriptionType
	 */
	public SosSensorDescription getSosSensorDescriptionType() {
		return sosSensorDescriptionType;
	}

	/**
	 * Set sensor description type
	 * 
	 * @param sosSensorDescriptionType
	 *            the sosSensorDescriptionType to set
	 */
	public void setSosSensorDescriptionType(
			SosSensorDescription sosSensorDescriptionType) {
		this.sosSensorDescriptionType = sosSensorDescriptionType;
	}

	/**
	 * Get sensor description
	 * 
	 * @return the sensorDescriptionString
	 */
	public String getSensorDescriptionString() {
		return sensorDescriptionString;
	}

	/**
	 * Set sensor description
	 * 
	 * @param sensorDescriptionString
	 *            the sensorDescriptionString to set
	 */
	public void setSensorDescriptionString(String sensorDescriptionString) {
		this.sensorDescriptionString = sensorDescriptionString;
	}

	/**
     * @return the outputFormat
     */
    public String getOutputFormat() {
        return outputFormat;
    }

    /**
     * @param outputFormat the outputFormat to set
     */
    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
	 * Get process type
	 * 
	 * @return the sensorMLType
	 */
	public SensorMLType getSensorMLType() {
		return sensorMLType;
	}

	/**
	 * Set process type
	 * 
	 * @param sensorMLType
	 *            the sensorMLType to set
	 */
	public void setSensorMLType(SensorMLType sensorMLType) {
		this.sensorMLType = sensorMLType;
	}

	/**
	 * Get sensor identifications
	 * 
	 * @return the identifications
	 */
	public List<SosSMLIdentifier> getIdentifications() {
		return identifications;
	}

	/**
	 * Set sensor identifications
	 * 
	 * @param identifications
	 *            the identifications to set
	 */
	public void setIdentifications(List<SosSMLIdentifier> identifications) {
		this.identifications = identifications;
	}

	/**
	 * Get sensor classifications
	 * 
	 * @return the classifications
	 */
	public List<SosSMLClassifier> getClassifications() {
		return classifications;
	}

	/**
	 * Set sensor classifications
	 * 
	 * @param classifications
	 *            the classifications to set
	 */
	public void setClassifications(List<SosSMLClassifier> classifications) {
		this.classifications = classifications;
	}

	/**
	 * Get sensor characteristics
	 * 
	 * @return the characteristics
	 */
	public SosSMLCharacteristics getCharacteristics() {
		return characteristics;
	}

	/**
	 * Set sensor characteristics
	 * 
	 * @param characteristics
	 *            the characteristics to set
	 */
	public void setCharacteristics(SosSMLCharacteristics characteristics) {
		this.characteristics = characteristics;
	}

	/**
	 * Get sensor capabilities
	 * 
	 * @return the capabilities
	 */
	public SosSMLCapabilities getCapabilities() {
		return capabilities;
	}

	/**
	 * Set sensor capabilities
	 * 
	 * @param capabilities
	 *            the capabilities to set
	 */
	public void setCapabilities(SosSMLCapabilities capabilities) {
		this.capabilities = capabilities;
	}

	/**
	 * Get sensor position
	 * 
	 * @return the position
	 */
	public SosSMLPosition getPosition() {
		return position;
	}

	/**
	 * Set sensor position
	 * 
	 * @param position
	 *            the position to set
	 */
	public void setPosition(SosSMLPosition position) {
		this.position = position;
	}

	/**
	 * Get sensor inputs
	 * 
	 * @return the inputs
	 */
	public List<SosSMLIo> getInputs() {
		return inputs;
	}

	/**
	 * Set sensor inputs
	 * 
	 * @param inputs
	 *            the inputs to set
	 */
	public void setInputs(List<SosSMLIo> inputs) {
		this.inputs = inputs;
	}

	/**
	 * Get sensor outputs
	 * 
	 * @return the outputs
	 */
	public List<SosSMLIo> getOutputs() {
		return outputs;
	}

	/**
	 * Set sensor outputs
	 * 
	 * @param outputs
	 *            the outputs to set
	 */
	public void setOutputs(List<SosSMLIo> outputs) {
		this.outputs = outputs;
	}

	/**
	 * Get sensor components
	 * 
	 * @return the components
	 */
	public List<SosSMLComponent> getComponents() {
		return components;
	}

	/**
	 * Set sensor components
	 * 
	 * @param components
	 *            the components to set
	 */
	public void setComponents(List<SosSMLComponent> components) {
		this.components = components;
	}

    /**
     * @return the parentProcedureIDs
     */
    public Collection<String> getParentProcedureIDs() {
        return parentProcedureIDs;
    }

    /**
     * @param parentProcedureIDs the parentProcedureIDs to set
     */
    public void setParentProcedureIDs(Collection<String> parentProcedureIDs) {
        this.parentProcedureIDs = parentProcedureIDs;
    }

    /**
     * @return the childProcedures
     */
    public Map<String, SosSensorML> getChildProcedures() {
        return childProcedures;
    }

    /**
     * @param childProcedures the childProcedures to set
     */
    public void setChildProcedures(Map<String, SosSensorML> childProcedures) {
        this.childProcedures = childProcedures;
    }

}
