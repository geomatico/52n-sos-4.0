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

package org.n52.sos.encode;

import java.util.Collection;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.om.SosObservationCollection;
import org.n52.sos.ogc.ows.OwsExceptionReport;

/**
 * Interface, which offers method to encode observation collection for
 * GetObservation response
 * 
 */
public interface IOMEncoder {

    /**
     * builds an XMLBeans ObservationCollectionDocument from
     * SosObservationCollection; the observation types of the observation
     * collection depend upon the implementation of this interface; the
     * implementation must be defined in the conf.sos.omEncoder property of the
     * config.properties file!!
     * 
     * @param sosObsCol
     * @return observation collection
     * @throws OwsExceptionReport
     */
    public XmlObject createObservationCollection(SosObservationCollection sosObsCol) throws OwsExceptionReport;

    /**
     * method creates a compositePhenomenon element
     * 
     * @param compPhenId
     *            id of the composite phenomenon
     * @param phenComponents
     *            components of the composite phenomenon
     * @return Returns PhenomenonPropertyType which represents the composite
     *         phenomenon element
     */
    public XmlObject createCompositePhenomenon(String compPhenId, Collection<String> phenComponents);

}
