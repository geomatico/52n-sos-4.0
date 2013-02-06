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

import org.n52.sos.response.InsertResultTemplateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class ResultTemplateInsertionInMemoryCacheUpdate extends InMemoryCacheUpdate{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultTemplateInsertionInMemoryCacheUpdate.class);

	private final InsertResultTemplateResponse sosResponse;
	
	public ResultTemplateInsertionInMemoryCacheUpdate(InsertResultTemplateResponse sosResponse) 
	{
		this.sosResponse = sosResponse;
	}

	@Override
	public void execute()
	{
		getCache().getResultTemplates().add(sosResponse.getAcceptedTemplate());
		LOGGER.debug("added result template identifier '{}' to cache? {}",
				sosResponse.getAcceptedTemplate(),
				getCache().getResultTemplates().contains(sosResponse.getAcceptedTemplate()));
	}

	
	
}
