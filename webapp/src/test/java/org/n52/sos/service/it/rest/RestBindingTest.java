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
package org.n52.sos.service.it.rest;

import static org.n52.sos.service.it.RequestBuilder.get;

import javax.xml.namespace.NamespaceContext;

import org.n52.sos.binding.rest.Constants;
import org.n52.sos.service.it.AbstractSosServiceTest;
import org.n52.sos.service.it.SosNamespaceContext;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 * @since 4.0.0
 */
public class RestBindingTest extends AbstractSosServiceTest {

	protected static final String REST_URL = "/rest";
	protected static final String CONTENT_TYPE = "application/gml+xml";
	protected static final NamespaceContext NS_CTXT = new SosNamespaceContext();
	protected static final Constants CONFIG = Constants.getInstance();
	
	protected String link(final String relType, final String resType)
	{
		return "sosREST:link[" +
				"@rel='" + CONFIG.getEncodingNamespace() + "/" + relType + "'" +
				" and " + 
				"@href='" + CONFIG.getServiceUrl() + REST_URL + "/" + resType + "'" +
				" and " + 
				"@type='" + CONTENT_TYPE + "'" +		
				"]";
	}

	protected MockHttpServletResponse getResource(final String resType)
	{
		return execute(	get(REST_URL + "/" + resType).accept(CONTENT_TYPE));
	}
}