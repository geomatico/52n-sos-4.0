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
package org.n52.sos.ogc.om;

import java.util.List;

/**
 * class represents a composite phenomenon
 * 
 */
public class OmCompositePhenomenon extends AbstractPhenomenon {
    private static final long serialVersionUID = 364153143602078222L;

    /** the components of the composite phenomenon */
    private List<OmObservableProperty> phenomenonComponents;

    /**
     * standard constructor
     * 
     * @param compPhenId
     *            id of the composite phenomenon
     * @param compPhenDesc
     *            description of the composite phenomenon
     * @param phenomenonComponents
     *            components of the composite phenomenon
     */
    public OmCompositePhenomenon(String compPhenId, String compPhenDesc,
            List<OmObservableProperty> phenomenonComponents) {
        super(compPhenId, compPhenDesc);
        this.phenomenonComponents = phenomenonComponents;
    }

    /**
     * Get observableProperties
     * 
     * @return Returns the phenomenonComponents.
     */
    public List<OmObservableProperty> getPhenomenonComponents() {
        return phenomenonComponents;
    }

    /**
     * Set observableProperties
     * 
     * @param phenomenonComponents
     *            The phenomenonComponents to set.
     */
    public void setPhenomenonComponents(List<OmObservableProperty> phenomenonComponents) {
        this.phenomenonComponents = phenomenonComponents;
    }
}
