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
package org.n52.sos.ogc.ows;

import java.util.Collections;
import java.util.Set;

import org.n52.sos.config.ISettingDefinition;
import org.n52.sos.config.ISettingDefinitionProvider;
import org.n52.sos.util.CollectionHelper;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class SosServiceProviderSettingDefinitionProvider implements ISettingDefinitionProvider {

    private static final Set<ISettingDefinition<?, ?>> DEFINITIONS = CollectionHelper.<ISettingDefinition<?, ?>>set(
            SosServiceProviderSettingDefinitions.NAME_DEFINITION,
            SosServiceProviderSettingDefinitions.SITE_DEFINITION,
            SosServiceProviderSettingDefinitions.INDIVIDUAL_NAME_DEFINITION,
            SosServiceProviderSettingDefinitions.POSITION_NAME_DEFINITION,
            SosServiceProviderSettingDefinitions.PHONE_DEFINITION,
            SosServiceProviderSettingDefinitions.DELIVERY_POINT_DEFINITION,
            SosServiceProviderSettingDefinitions.CITY_DEFINITION,
            SosServiceProviderSettingDefinitions.POSTAL_CODE_DEFINITION,
            SosServiceProviderSettingDefinitions.ADMINISTRATIVE_AREA_DEFINITION,
            SosServiceProviderSettingDefinitions.COUNTRY_DEFINITION,
            SosServiceProviderSettingDefinitions.MAIL_ADDRESS_DEFINITION,
            SosServiceProviderSettingDefinitions.FILE_DEFINITION);

    @Override
    public Set<ISettingDefinition<?, ?>> getSettingDefinitions() {
        return Collections.unmodifiableSet(DEFINITIONS);
    }
}
