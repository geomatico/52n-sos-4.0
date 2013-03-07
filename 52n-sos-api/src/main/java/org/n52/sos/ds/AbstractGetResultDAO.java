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
package org.n52.sos.ds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.GetResultRequest;
import org.n52.sos.response.GetResultResponse;

public abstract class AbstractGetResultDAO extends AbstractOperationDAO {
    public AbstractGetResultDAO() {
        super(SosConstants.Operations.GetResult.name());
    }

    @Override
    protected void setOperationsMetadata(OWSOperation opsMeta, String service, String version)
            throws OwsExceptionReport {
        Set<String> resultTemplateIdentifier = getCache().getResultTemplates();
        Set<String> offerings = new HashSet<String>(0);
        Collection<String> observableProperties = new ArrayList<String>(0);
        Collection<String> featureOfInterest = new ArrayList<String>(0);
        if (resultTemplateIdentifier != null && !resultTemplateIdentifier.isEmpty()) {
            offerings = getCache().getOfferingsWithResultTemplate();
            observableProperties = getCache().getObservablePropertiesWithResultTemplate();
            featureOfInterest = getCache().getFeaturesOfInterestWithResultTemplate();
        }
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            // TODO set parameter for SOS 1.0
        } else if (version.equals(Sos2Constants.SERVICEVERSION)) {
            opsMeta.addPossibleValuesParameter(Sos2Constants.GetResultParams.offering, offerings);
            opsMeta.addPossibleValuesParameter(Sos2Constants.GetResultParams.observedProperty, observableProperties);
            opsMeta.addPossibleValuesParameter(Sos2Constants.GetResultParams.featureOfInterest, featureOfInterest);
            // TODO get the values for temporal and spatial filtering
            // set param temporalFilter
            // opsMeta.addParameterValue(Sos2Constants.GetResultParams.temporalFilter.name(),
            // new OWSParameterValuePossibleValues(null));
            // // set param spatialFilter
            // opsMeta.addParameterValue(Sos2Constants.GetResultParams.spatialFilter.name(),
            // new OWSParameterValuePossibleValues(null));
        }
    }
    
    public abstract GetResultResponse getResult(GetResultRequest request) throws OwsExceptionReport;

}
