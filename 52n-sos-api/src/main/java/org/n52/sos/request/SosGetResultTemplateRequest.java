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

package org.n52.sos.request;

import java.util.List;

import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.operator.RequestOperatorKeyType;

/**
 * SOS GetResultTemplate request
 * 
 */
public class SosGetResultTemplateRequest extends AbstractServiceRequest {

	/**
	 * GetResultTemplate operation name
	 */
	private final String operationName = Sos2Constants.Operations.GetResultTemplate
			.name();
	
	       /**
         * ObservedProperties list
         */
        private String observedProperty;
        
        /**
         * Offerings list
         */
        private String offering;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.sos.request.AbstractSosRequest#getOperationName()
	 */
	@Override
	public String getOperationName() {
		return operationName;
	}

        /**
         * Get observableProperties
         * 
         * @return observableProperties
         */
        public String getObservedProperty() {
                return observedProperty;
        }

        /**
         * Set observableProperties
         * 
         * @param observableProperties
         *            observableProperties
         */
        public void setObservedProperty(String observedProperty) {
                this.observedProperty = observedProperty;
                // CapabilitiesCacheController cache =
                // (CapabilitiesCacheController)SosConfigurator.getInstance().getCapsCacheController();
                // TODO: FIXME
                //
                // List<String> phens = new ArrayList<String>();
                // Map<String, Collection<String>> compPhens =
                // SosConfigurator.getInstance().getCapsCacheController().getPhens4CompPhens();
                //
                // // find phens for comp phens
                // for (String phen : observedProperty) {
                // if (compPhens.keySet().contains(phen)) {
                // phens.addAll(compPhens.get(phen));
                // } else {
                // phens.add(phen);
                // }
                // }
                //
                // this.observedProperty = (String[]) phens.toArray(new
                // String[phens.size()]);
        }

        /**
         * Get offerings
         * 
         * @return offerings
         */
        public String getOffering() {
                return offering;
        }

        /**
         * Set offerings
         * 
         * @param offerings
         *            offerings
         */
        public void setOffering(String offering) {
                this.offering = offering;
        }

}
