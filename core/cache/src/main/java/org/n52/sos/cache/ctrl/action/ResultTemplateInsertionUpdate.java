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
package org.n52.sos.cache.ctrl.action;

import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.request.InsertResultTemplateRequest;
import org.n52.sos.response.InsertResultTemplateResponse;
import org.n52.sos.util.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When executing this &auml;ction (see {@link Action}), the following relations are added, settings are updated in
 * cache:<ul>
 * <li>Result template</li>
 * <li>Offering &rarr; Result template</li></ul>
 *
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @since 4.0
 */
public class ResultTemplateInsertionUpdate extends InMemoryCacheUpdate {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultTemplateInsertionUpdate.class);
    private final InsertResultTemplateResponse response;
    private final InsertResultTemplateRequest request;

    public ResultTemplateInsertionUpdate(InsertResultTemplateRequest request,
                                                      InsertResultTemplateResponse response) {
        if (request == null || response == null) {
            String msg = String.format("Missing argument: '%s': %s; '%s': %s",
                                       InsertResultTemplateRequest.class.getName(),
                                       request,
                                       InsertResultTemplateResponse.class.getName(),
                                       response);
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
        this.response = response;
        this.request = request;
    }

    @Override
    public void execute() {
        final WritableContentCache cache = getCache();
        final String resultTemplate = response.getAcceptedTemplate();
        cache.addResultTemplate(resultTemplate);
        for (String offering : request.getObservationTemplate().getOfferings()) {
            cache.addOffering(offering);
            cache.addResultTemplateForOffering(offering, resultTemplate);
        }
    }
}
