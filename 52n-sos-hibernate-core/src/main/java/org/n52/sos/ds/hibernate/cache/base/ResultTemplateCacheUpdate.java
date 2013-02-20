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

import static org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities.getResultTemplateObjects;

import java.util.Collection;
import java.util.List;

import org.n52.sos.ds.hibernate.cache.CacheUpdate;
import org.n52.sos.ds.hibernate.entities.ResultTemplate;
import org.n52.sos.util.Action;
import org.n52.sos.util.CollectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When executing this &auml;ction (see {@link Action}), the following relations are added, settings are updated in cache:<ul>
 * <li>Result template identifier</li>
 * <li>Procedure &rarr; 'Result template identifier' relation</li>
 * </ul>
 * @author Christian Autermann <c.autermann@52north.org>
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @since 4.0.0
 */
public class ResultTemplateCacheUpdate extends CacheUpdate {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultTemplateCacheUpdate.class);

    @Override
    public void execute()
    {
        List<ResultTemplate> resultTemplateObjects = getResultTemplateObjects(getSession());
        for (ResultTemplate resultTemplateObject : resultTemplateObjects)
        {
            String resultTemplateIdentifier = resultTemplateObject.getIdentifier();
            getCache().getResultTemplates().add(resultTemplateIdentifier);
            LOGGER.debug("ResultTemplate identifier '{}' added to cache? {}",
            		resultTemplateIdentifier,
            		getCache().getResultTemplates().contains(resultTemplateIdentifier));
            
            String offeringIdentifier = resultTemplateObject.getObservationConstellationOfferingObservationType().getOffering().getIdentifier();
            if (!getCache().getKOfferingVResultTemplates().containsKey(offeringIdentifier))
            {
            	Collection<String> resultTemplateIdentifiers = CollectionHelper.synchronizedArrayList(1);
            	getCache().getKOfferingVResultTemplates().put(offeringIdentifier, resultTemplateIdentifiers);
            }
            getCache().getKOfferingVResultTemplates().get(offeringIdentifier).add(resultTemplateIdentifier);
            LOGGER.debug("Offering '{}' to result template '{}' relation added to cache? {}",
            		offeringIdentifier,
            		resultTemplateIdentifier,
            		getCache().getKOfferingVResultTemplates().get(offeringIdentifier).contains(resultTemplateIdentifier));
        }
    }
}
