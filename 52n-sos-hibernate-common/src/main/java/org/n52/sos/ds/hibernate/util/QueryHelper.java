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
package org.n52.sos.ds.hibernate.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.n52.sos.ogc.filter.SpatialFilter;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.service.Configurator;

public class QueryHelper {
    
    public static Set<String> getFeatureIdentifier(SpatialFilter spatialFilter, List<String> featureIdentifier,
                                                   Session session) throws OwsExceptionReport {
        Set<String> foiIDs = null;
        // spatial filter
        if (spatialFilter != null) {
            if (spatialFilter.getValueReference().contains("om:featureOfInterest")
                    && spatialFilter.getValueReference().contains("sams:shape")) {
                foiIDs =
                        new HashSet<String>(Configurator.getInstance().getFeatureQueryHandler()
                                .getFeatureIDs(spatialFilter, session));
            } else {
                throw new NoApplicableCodeException()
                        .withMessage("The requested valueReference for spatial filters is not supported by this server!");
            }
        }
        // feature of interest
        if (featureIdentifier != null && !featureIdentifier.isEmpty()) {
            if (foiIDs == null) {
                foiIDs = new HashSet<String>(featureIdentifier);
            } else {
                Set<String> tempFoiIDs = new HashSet<String>();
                for (String foiID : featureIdentifier) {
                    if (foiIDs.contains(foiID)) {
                        tempFoiIDs.add(foiID);
                    }
                }
                foiIDs = tempFoiIDs;
            }
        }
        return foiIDs;
    }

}
