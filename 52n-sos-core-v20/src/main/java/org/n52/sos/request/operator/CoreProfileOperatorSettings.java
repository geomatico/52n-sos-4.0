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

package org.n52.sos.request.operator;

import java.util.Collections;
import java.util.Set;

import org.n52.sos.config.SettingDefinition;
import org.n52.sos.config.SettingDefinitionProvider;
import org.n52.sos.config.settings.BooleanSettingDefinition;
import org.n52.sos.service.ServiceSettings;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class CoreProfileOperatorSettings implements SettingDefinitionProvider {
    public static final String BLOCK_GET_OBSERVATION_REQUESTS_WITHOUT_RESTRICTION =
                               "service.blockGetObservationRequestsWithoutRestriction";
    private static final BooleanSettingDefinition BLOCK_GET_OBSERVATION_REQUESTS_WITHOUT_RESTRICTION_DEFINITION =
                                                  new BooleanSettingDefinition()
            .setGroup(ServiceSettings.GROUP)
            .setDefaultValue(Boolean.FALSE)
            .setDescription("Should GetObservation requests without any restriction be blocked")
            .setTitle("Block restrictionless requests")
            .setKey(BLOCK_GET_OBSERVATION_REQUESTS_WITHOUT_RESTRICTION)
            .setOrder(12);

    @Override
    public Set<SettingDefinition<?, ?>> getSettingDefinitions() {
        return Collections
                .<SettingDefinition<?, ?>>singleton(BLOCK_GET_OBSERVATION_REQUESTS_WITHOUT_RESTRICTION_DEFINITION);
    }
}
