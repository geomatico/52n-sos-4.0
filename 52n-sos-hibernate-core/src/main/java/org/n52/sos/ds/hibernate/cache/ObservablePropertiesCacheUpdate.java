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
package org.n52.sos.ds.hibernate.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ObservablePropertiesCacheUpdate extends CacheUpdate {

    protected List<String> getProcedureIdentifierFromObservationConstellation(Set<ObservationConstellation> set) {
        Set<String> procedures = new HashSet<String>(set.size());
        for (ObservationConstellation oc : set) {
            procedures.add(oc.getProcedure().getIdentifier());
        }
        return new ArrayList<String>(procedures);
    }
    
    @Override
    public void run() {
        List<ObservableProperty> hObservableProperties =
                HibernateCriteriaQueryUtilities.getObservablePropertyObjects(getSession());
        // fields
        Map<String, List<String>> kObservablePropertyVOffering = new HashMap<String, List<String>>(hObservableProperties.size());
        Map<String, List<String>> kObservablePropertyVProcedures = new HashMap<String, List<String>>(hObservableProperties.size());

        for (ObservableProperty hObservableProperty : hObservableProperties) {
            List<String> offeringList = getAllOfferingIdentifiersFrom(hObservableProperty.getObservationConstellations());
            Collections.sort(offeringList);
            kObservablePropertyVOffering.put(hObservableProperty.getIdentifier(), offeringList);
            List<String> procedureList = getProcedureIdentifierFromObservationConstellation(hObservableProperty.getObservationConstellations());
            Collections.sort(procedureList);
            kObservablePropertyVProcedures.put(hObservableProperty.getIdentifier(), procedureList);
        }

        getCache().setKObservablePropertyVOfferings(kObservablePropertyVOffering);
        getCache().setKObservablePropertyVProcedures(kObservablePropertyVProcedures);
    }
}
