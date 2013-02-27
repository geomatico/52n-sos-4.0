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
import static org.n52.sos.util.CollectionHelper.synchronizedList;

import java.util.Collection;
import java.util.List;

import org.n52.sos.ds.hibernate.cache.CacheUpdate;
import org.n52.sos.ds.hibernate.entities.ResultTemplate;
import org.n52.sos.util.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When executing this &auml;ction (see {@link Action}), the following relations are added, settings are updated in cache:<ul>
 * <li>Result template identifier</li>
 * <li>Procedure &rarr; 'Result template identifier' relation</li>
 * <li>'Result template identifier' &rarr; 'observable property' relation</li>
 * <li>'Result template identifier' &rarr; 'feature of interest' relation</li>
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
            addIdentifierToCache(resultTemplateIdentifier);
            
            String offeringIdentifier = resultTemplateObject.getObservationConstellationOfferingObservationType().getOffering().getIdentifier();
            addOfferingToResultTemplateRelationToCache(resultTemplateIdentifier, offeringIdentifier);

            String observablePropertyIdentifier = resultTemplateObject.getObservationConstellationOfferingObservationType().getObservationConstellation().getObservableProperty().getIdentifier();
            addResultTemplateToObservablePropertyIdentifierToCache(resultTemplateIdentifier,observablePropertyIdentifier);
            
            String featureOfInterestIdentifier = resultTemplateObject.getFeatureOfInterest().getIdentifier();
            addResultTemplateToFeatureOfInterestRelationToCache(resultTemplateIdentifier,featureOfInterestIdentifier);
        }
    }

	private void addResultTemplateToFeatureOfInterestRelationToCache(String resultTemplateIdentifier,
			String featureOfInterestIdentifier)
	{
		if(!getCache().getKResultTemplateVFeaturesOfInterest().containsKey(resultTemplateIdentifier))
		{
            Collection<String> featureOfInterestIdentifiers = synchronizedList(1);
			getCache().getKResultTemplateVFeaturesOfInterest().put(resultTemplateIdentifier, featureOfInterestIdentifiers);
		}
        getCache().getKResultTemplateVFeaturesOfInterest().get(resultTemplateIdentifier)
                .add(featureOfInterestIdentifier);
		LOGGER.debug("Result Template '{}' to feature of interest '{}' relation added to cache? {}",
				resultTemplateIdentifier,
				featureOfInterestIdentifier,
				getCache().getKResultTemplateVFeaturesOfInterest().get(resultTemplateIdentifier).contains(featureOfInterestIdentifier));
	}

	private void addResultTemplateToObservablePropertyIdentifierToCache(String resultTemplateIdentifier,
			String observablePropertyIdentifier)
	{
		if(!getCache().getKResultTemplateVObservedProperties().containsKey(resultTemplateIdentifier))
		{
			Collection<String> observedPropertyIdentifiers = synchronizedList(1);
			getCache().getKResultTemplateVObservedProperties().put(resultTemplateIdentifier, observedPropertyIdentifiers);
		}
		getCache().getKResultTemplateVObservedProperties().get(resultTemplateIdentifier).add(observablePropertyIdentifier);
		LOGGER.debug("Result Template '{}' to observable property '{}' relation added to cache? {}",
				resultTemplateIdentifier,
				observablePropertyIdentifier,
				getCache().getKResultTemplateVObservedProperties().get(resultTemplateIdentifier).contains(observablePropertyIdentifier));
	}

	private void addOfferingToResultTemplateRelationToCache(String resultTemplateIdentifier,
			String offeringIdentifier)
	{
		if (!getCache().getKOfferingVResultTemplates().containsKey(offeringIdentifier))
		{
			Collection<String> resultTemplateIdentifiers = synchronizedList(1);
			getCache().getKOfferingVResultTemplates().put(offeringIdentifier, resultTemplateIdentifiers);
		}
		getCache().getKOfferingVResultTemplates().get(offeringIdentifier).add(resultTemplateIdentifier);
		LOGGER.debug("Offering '{}' to result template '{}' relation added to cache? {}",
				offeringIdentifier,
				resultTemplateIdentifier,
				getCache().getKOfferingVResultTemplates().get(offeringIdentifier).contains(resultTemplateIdentifier));
	}

	private void addIdentifierToCache(String resultTemplateIdentifier)
	{
		getCache().getResultTemplates().add(resultTemplateIdentifier);
		LOGGER.debug("ResultTemplate identifier '{}' added to cache? {}",
				resultTemplateIdentifier,
				getCache().getResultTemplates().contains(resultTemplateIdentifier));
	}
}
