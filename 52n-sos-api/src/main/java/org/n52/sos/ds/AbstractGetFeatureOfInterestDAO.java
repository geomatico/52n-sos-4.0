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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.request.GetFeatureOfInterestRequest;
import org.n52.sos.response.GetFeatureOfInterestResponse;
import org.n52.sos.util.SosHelper;

public abstract class AbstractGetFeatureOfInterestDAO extends AbstractOperationDAO {
    public AbstractGetFeatureOfInterestDAO() {
        super(SosConstants.Operations.GetFeatureOfInterest.name());
    }

    @Override
    protected void setOperationsMetadata(OWSOperation opsMeta, String service, String version) throws OwsExceptionReport {

        Collection<String> featureIDs = SosHelper.getFeatureIDs(getCache().getFeaturesOfInterest(), version);

        if (getConfigurator().getActiveProfile().isShowFullOperationsMetadataForObservations()) {
            opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.procedure, getCache()
                    .getProcedures());
            opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.observedProperty,
                    getCache().getObservableProperties());
            opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.featureOfInterest, featureIDs);
        } else {
            opsMeta.addAnyParameterValue(SosConstants.GetObservationParams.procedure);
            opsMeta.addAnyParameterValue(SosConstants.GetObservationParams.observedProperty);
            opsMeta.addAnyParameterValue(SosConstants.GetObservationParams.featureOfInterest);
        }

        // TODO constraint srid
        String parameterName = Sos2Constants.GetFeatureOfInterestParams.spatialFilter.name();
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            parameterName = Sos1Constants.GetFeatureOfInterestParams.location.name();
        }

        SosEnvelope envelope = null;
        if (featureIDs != null && !featureIDs.isEmpty()) {
            envelope = getCache().getGlobalEnvelope();
        }

        if (envelope != null) {
            opsMeta.addRangeParameterValue(parameterName, SosHelper.getMinMaxFromEnvelope(envelope.getEnvelope()));
        } else {
            opsMeta.addAnyParameterValue(parameterName);
        }
    }
    
    public abstract GetFeatureOfInterestResponse getFeatureOfInterest(GetFeatureOfInterestRequest request)
            throws OwsExceptionReport;
    
    protected boolean isRelatedFeature(String featureIdentifier) {
        Set<String> relatedFeatures = getCache().getRelatedFeatures();
        return relatedFeatures.contains(featureIdentifier);
    }
    
    protected Set<String> checkFeatureIdentifiersForRelatedFeatures(List<String> featureIdentifiers) {
        Set<String> allFeatureIdentifiers = new HashSet<String>();
        for (String featureIdentifier : featureIdentifiers) {
            if (isRelatedFeature(featureIdentifier)) {
                allFeatureIdentifiers.addAll(getFeatureIdentifierForRelatedFeature(featureIdentifier));
            } else {
                allFeatureIdentifiers.add(featureIdentifier);
            }
        }
        return allFeatureIdentifiers;
    }

    protected Collection<? extends String> getFeatureIdentifierForRelatedFeature(String featureIdentifier) {
        Set<String> featureIdentifiers = new HashSet<String>();
        // TODO change to get only the features related to related feature, e.g. feature hierarchy
//        Set<String> offerings = getCache().getOfferings();
//        for (String offering : offerings) {
//            if (getCache().getRelatedFeaturesForOffering(offering).contains(featureIdentifier)) {
//                featureIdentifiers.addAll(getCache().getFeaturesOfInterestForOffering(offering));
//            }
//        }
        return featureIdentifiers;
    }
}
