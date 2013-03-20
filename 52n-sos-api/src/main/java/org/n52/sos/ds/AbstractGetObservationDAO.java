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
import java.util.List;

import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OWSParameterValueRange;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.request.GetObservationRequest;
import org.n52.sos.response.GetObservationResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.MinMax;
import org.n52.sos.util.SosHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * interface for getting observations for a passed getObservation request from
 * the data source
 * 
 */
public abstract class AbstractGetObservationDAO extends AbstractOperationDAO {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGetObservationDAO.class);

    public AbstractGetObservationDAO() {
        super(SosConstants.Operations.GetObservation.name());
    }

    @Override
    protected void setOperationsMetadata(OWSOperation opsMeta, String service, String version)
            throws OwsExceptionReport {

        Collection<String> featureIDs = SosHelper.getFeatureIDs(getCache().getFeaturesOfInterest(), version);

        opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.offering, getCache().getOfferings());
        opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.procedure, getCache().getProcedures());
        opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.responseFormat,
                                           Configurator.getInstance().getCodingRepository()
                .getSupportedResponseFormats(SosConstants.SOS, version));

        if (getConfigurator().getProfileHandler().getActiveProfile().isShowFullOperationsMetadataForObservations()) {
            opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.observedProperty, getCache()
                    .getObservableProperties());
            opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.featureOfInterest, featureIDs);
        } else {
            opsMeta.addAnyParameterValue(SosConstants.GetObservationParams.observedProperty);
            opsMeta.addAnyParameterValue(SosConstants.GetObservationParams.featureOfInterest);
        }

        if (version.equals(Sos2Constants.SERVICEVERSION)) {
            // SOS 2.0 parameter
            OWSParameterValueRange temporalFilter = new OWSParameterValueRange(getPhenomenonTime(), "om:phenomenonTime");
            opsMeta.addRangeParameterValue(Sos2Constants.GetObservationParams.temporalFilter, temporalFilter);
            SosEnvelope envelope = null;
            if (featureIDs != null && !featureIDs.isEmpty()) {
                envelope = getCache().getGlobalEnvelope();
            }
            if (envelope != null) {
                opsMeta.addRangeParameterValue(Sos2Constants.GetObservationParams.spatialFilter,
                        SosHelper.getMinMaxFromEnvelope(envelope.getEnvelope()));
            }
        } else if (version.equals(Sos1Constants.SERVICEVERSION)) {
            // SOS 1.0.0 parameter
            opsMeta.addRangeParameterValue(Sos1Constants.GetObservationParams.eventTime, getPhenomenonTime());
            opsMeta.addAnyParameterValue(SosConstants.GetObservationParams.srsName);
            opsMeta.addAnyParameterValue(SosConstants.GetObservationParams.result);
            opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.resultModel, getResultModels());
            opsMeta
                    .addPossibleValuesParameter(SosConstants.GetObservationParams.responseMode, SosConstants.RESPONSE_MODES);
        }
    }
    
    /**
     * Get the min/max phenomenon time of contained observations
     * 
     * @return min/max phenomenon time

     *
     * @throws OwsExceptionReport     *             If an error occurs.
     */
    private MinMax<String> getPhenomenonTime() throws OwsExceptionReport {
        DateTime minDate = getCache().getMinPhenomenonTime();
        DateTime maxDate = getCache().getMaxPhenomenonTime();
        return new MinMax<String>()
                .setMinimum(minDate != null ? DateTimeHelper.formatDateTime2ResponseString(minDate) : null)
                .setMaximum(maxDate != null ? DateTimeHelper.formatDateTime2ResponseString(maxDate) : null);
    }
    
    /**
     * Get the min/max result time of contained observations
     * 
     * @return min/max result time

     *
     * @throws OwsExceptionReport     *             If an error occurs.
     */
    private MinMax<String> getResultTime() throws OwsExceptionReport {
        DateTime minDate = getCache().getMinResultTime();
        DateTime maxDate = getCache().getMaxResultTime();
        return new MinMax<String>()
                .setMinimum(minDate != null ? DateTimeHelper.formatDateTime2ResponseString(minDate) : null)
                .setMaximum(maxDate != null ? DateTimeHelper.formatDateTime2ResponseString(maxDate) : null);
    }
    
    private List<String> getResultModels() {
        List<String> resultModelsList = new ArrayList<String>(OMConstants.RESULT_MODELS.size());
        for (QName qname : OMConstants.RESULT_MODELS) {
            resultModelsList.add(qname.getPrefix() + ":" + qname.getLocalPart());
        }
        return resultModelsList;
    }

    /**
     * process the GetObservation query
     * 
     * @param request
     *            GetObservation object which represents the getObservation
     *            request
     * 
     * @return ObservationDocument representing the requested values in an OGC
     *         conform O&M observation document
     * 
     * @throws OwsExceptionReport     *             if query of the database or creating the O&M document failed
     */
    public abstract GetObservationResponse getObservation(GetObservationRequest request) throws OwsExceptionReport;

}
