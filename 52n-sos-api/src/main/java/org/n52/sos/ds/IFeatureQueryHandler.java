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
import java.util.List;
import java.util.Map;

import org.n52.sos.ogc.filter.SpatialFilter;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosEnvelope;

/**
 * Interface for querying featurefInterest data from a data source
 * 
 */
public interface IFeatureQueryHandler {
    
    /**
     * Query feature data from data source for an identifier
     * 
     * @param featureID
     *            FOI identifier
     * @param connection
     *            Data source connection
     * @param version
     *            SOS version
     * @param responseSrid
     *            response srid for feature geometry, if negative not transformation  
     * @return SOS representation of the FOI
     * @throws OwsExceptionReport
     */
    public SosAbstractFeature getFeatureByID(String featureID, Object connection, String version, int responseSrid)
            throws OwsExceptionReport;

    /**
     * Query feature identifier from data source for a spatial filter
     * 
     * @param filter
     *            Spatial filter
     * @param connection
     *            Data source connection
     * @return List of FOI identifieres
     * @throws OwsExceptionReport
     */
    Collection<String> getFeatureIDs(SpatialFilter filter, Object connection) throws OwsExceptionReport;

    /**
     * Get feature data for identifiers and/or for a spatial filter
     * 
     * @param foiIDs
     *            FOI identifiers
     * @param list
     *            Spatial filter
     * @param connection
     *            Data source connection
     * @param version
     *            SOS version
     * @param responseSrid
     *            response srid for feature geometry, if negative not transformation       
     * @return Map of identifier and SOS FOI representation
     * @throws OwsExceptionReport
     */
    public Map<String, SosAbstractFeature> getFeatures(List<String> foiIDs, List<SpatialFilter> list,
            Object connection, String version, int responseSrid) throws OwsExceptionReport;

    /**
     * Query the envelope for feature ids
     * 
     * @param featureIDs
     *            FOI identifiers
     * @param connection
     *            Data source connection
     * @return Envelope of requested FOI identifiers
     * @throws OwsExceptionReport
     */
    public SosEnvelope getEnvelopeForFeatureIDs(List<String> featureIDs, Object connection) throws OwsExceptionReport;
    
    public String insertFeature(SosSamplingFeature samplingFeature, Object connection) throws OwsExceptionReport;

}
