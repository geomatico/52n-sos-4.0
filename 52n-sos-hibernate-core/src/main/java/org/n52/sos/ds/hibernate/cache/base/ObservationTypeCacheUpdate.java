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
package org.n52.sos.ds.hibernate.cache.base;

import static java.util.Collections.synchronizedList;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.n52.sos.ds.hibernate.cache.CacheUpdate;
import org.n52.sos.ds.hibernate.entities.ObservationType;
import org.n52.sos.util.Action;

/**
 * When executing this &auml;ction (see {@link Action}), the following relations are added, settings are updated in cache:<ul>
 * <li>Observation Type</li></ul>
 *
 * @author Christian Autermann <c.autermann@52north.org>
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * 
 * @since 4.0.0
 */
public class ObservationTypeCacheUpdate extends CacheUpdate {

    @Override
    public void execute() {
    	Criteria hObservationTypes = getSession().createCriteria(ObservationType.class);
		List<ObservationType> observationTypes = hObservationTypes.list();
        List<String> obsTypes = synchronizedList(new ArrayList<String>(0));
        if (observationTypes != null) {
            for (ObservationType observationType : observationTypes) {
                obsTypes.add(observationType.getObservationType());
            }
        }
        getCache().setObservationTypes(obsTypes);
    }
    
}
