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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.util.CacheHelper;

public class DatasourceCacheUpdateHelper {
    
    public static Set<String> getAllOfferingIdentifiersFrom(Collection<ObservationConstellation> observationConstellations) {
        Set<String> offerings = new HashSet<String>(observationConstellations.size());
        for (ObservationConstellation oc : observationConstellations) {
            offerings.add(CacheHelper.addPrefixOrGetOfferingIdentifier(oc.getOffering().getIdentifier()));
        }
        return offerings;
    }
    
    public static Set<String> getAllProcedureIdentifiersFrom(Collection<ObservationConstellation> observationConstellations) {
        Set<String> procedures = new HashSet<String>(observationConstellations.size());
        for (ObservationConstellation oc : observationConstellations) {
            procedures.add(CacheHelper.addPrefixOrGetProcedureIdentifier(oc.getProcedure().getIdentifier()));
        }
        return procedures;
    }
    
    public static Set<String> getAllObservablePropertyIdentifiersFrom(Collection<ObservationConstellation> observationConstellations) {
        Set<String> observableProperties = new HashSet<String>(observationConstellations.size());
        for (ObservationConstellation oc : observationConstellations) {
            observableProperties.add(CacheHelper.addPrefixOrGetObservablePropertyIdentifier(oc.getObservableProperty().getIdentifier()));
        }
        return observableProperties;
    }

}
