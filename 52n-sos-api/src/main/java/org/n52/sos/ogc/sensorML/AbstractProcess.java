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
package org.n52.sos.ogc.sensorML;

import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ogc.sensorML.elements.SosSMLIo;

public class AbstractProcess extends AbstractSensorML {

    private List<String> descriptions = new ArrayList<String>(0);

    private List<String> names = new ArrayList<String>(0);

    private List<SosSMLIo> inputs = new ArrayList<SosSMLIo>(0);

    private List<SosSMLIo> outputs = new ArrayList<SosSMLIo>(0);

    private List<String> parameters = new ArrayList<String>(0);

    public List<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public List<SosSMLIo> getInputs() {
        return inputs;
    }

    public void setInputs(List<SosSMLIo> inputs) {
        this.inputs = inputs;
    }

    public List<SosSMLIo> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<SosSMLIo> outputs) {
        this.outputs = outputs;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }
    
    public boolean isSetDescriptions() {
        return descriptions != null && !descriptions.isEmpty();
    }
    
    public boolean isSetNames() {
        return names != null && !names.isEmpty();
    }
    
    public boolean isSetInputs() {
        return inputs != null && !inputs.isEmpty();
    }
    
    public boolean isSetOutputs() {
        return outputs != null && !outputs.isEmpty();
    }
    
    public boolean isSetParameters() {
        return parameters != null && !parameters.isEmpty();
    }

}
