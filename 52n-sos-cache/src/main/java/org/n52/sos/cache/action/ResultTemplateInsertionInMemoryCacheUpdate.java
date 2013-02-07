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
package org.n52.sos.cache.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.n52.sos.request.InsertResultTemplateRequest;
import org.n52.sos.response.InsertResultTemplateResponse;
import org.n52.sos.util.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This &auml;ction (see {@link Action}) adds the 'result template identifer' and the 'offering' &rarr; 'result template' relation to the cache
 * 
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @since 4.0
 */
public class ResultTemplateInsertionInMemoryCacheUpdate extends InMemoryCacheUpdate{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultTemplateInsertionInMemoryCacheUpdate.class);

	private final InsertResultTemplateResponse sosResponse;

	private final InsertResultTemplateRequest sosRequest;
	
	public ResultTemplateInsertionInMemoryCacheUpdate(InsertResultTemplateRequest sosRequest, InsertResultTemplateResponse sosResponse) 
	{
		if (sosRequest == null || sosResponse == null)
		{
			String msg = String.format("Missing argument: '{}': {}; '{}': {}", 
					InsertResultTemplateRequest.class.getName(),
					sosRequest,
					InsertResultTemplateResponse.class.getName(),
					sosResponse);
			LOGGER.error(msg);
			throw new IllegalArgumentException(msg);
		}
		this.sosResponse = sosResponse;
		this.sosRequest = sosRequest;
	}

	@Override
	public void execute()
	{
		addResultTemplateIdentifierToCache();
		addOfferingToResultTemplateRelationToCache();
	}

	private void addResultTemplateIdentifierToCache()
	{
		getCache().getResultTemplates().add(resultTemplateId());
		LOGGER.debug("added result template identifier '{}' to cache? {}",
				resultTemplateId(),
				getCache().getResultTemplates().contains(resultTemplateId()));
	}
	
	private void addOfferingToResultTemplateRelationToCache()
	{
		if (offeringToResultTemplatesMap().get(offeringId()) == null)
		{
			offeringToResultTemplatesMap().put(offeringId(), Collections.synchronizedList(new ArrayList<String>()));
		}
		if (!offeringToResultTemplatesMap().get(offeringId()).contains(resultTemplateId()))
		{
			offeringToResultTemplatesMap().get(offeringId()).add(resultTemplateId());
		}
		LOGGER.debug("offering '{}' to result template '{}' relation in cache? {}",
				offeringId(),
				resultTemplateId(),
				offeringToResultTemplatesMap().get(offeringId()).contains(resultTemplateId()));
	}

	private Map<String, Collection<String>> offeringToResultTemplatesMap()
	{
		return getCache().getKOfferingVResultTemplates();
	}

	private String resultTemplateId()
	{
		return sosResponse.getAcceptedTemplate();
	}

	private String offeringId()
	{
		return sosRequest.getObservationTemplate().getOfferings().iterator().next();
	}

}
