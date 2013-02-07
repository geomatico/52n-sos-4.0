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

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.n52.sos.config.ISettingDefinition;
import org.n52.sos.config.ISettingDefinitionProvider;
import org.n52.sos.config.ISettingValue;
import org.n52.sos.config.SettingDefinitions;
import org.n52.sos.service.ConfigurationException;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CollectionHelper;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class SosServiceProviderSettingDefinitionProvider implements ISettingDefinitionProvider {

    private static final Set<ISettingDefinition<?>> DEFINITIONS = CollectionHelper.<ISettingDefinition<?>>set(
            SettingDefinitions.SERVICE_PROVIDER_NAME,
            SettingDefinitions.SERVICE_PROVIDER_SITE,
            SettingDefinitions.SERVICE_PROVIDER_INDIVIDUAL_NAME,
            SettingDefinitions.SERVICE_PROVIDER_POSITION_NAME,
            SettingDefinitions.SERVICE_PROVIDER_PHONE,
            SettingDefinitions.SERVICE_PROVIDER_DELIVERY_POINT,
            SettingDefinitions.SERVICE_PROVIDER_CITY,
            SettingDefinitions.SERVICE_PROVIDER_POSTAL_CODE,
            SettingDefinitions.SERVICE_PROVIDER_ADMINISTRATIVE_AREA,
            SettingDefinitions.SERVICE_PROVIDER_COUNTRY,
            SettingDefinitions.SERVICE_PROVIDER_MAIL_ADDRESS,
            SettingDefinitions.SERVICE_PROVIDER_FILE);

    @Override
    public Set<ISettingDefinition<?>> getSettingDefinitions() {
        return Collections.unmodifiableSet(DEFINITIONS);
    }

    @Override
    public void setSettingValues(Map<ISettingDefinition<?>, ISettingValue<?>> settings) throws ConfigurationException {
        for (Map.Entry<ISettingDefinition<?>, ISettingValue<?>> e : settings.entrySet()) {
            changeSettingValue(e.getKey(), null, e.getValue());
        }
    }

    @Override
    public void changeSettingValue(ISettingDefinition<?> definition, ISettingValue<?> oldValue,
                                   ISettingValue<?> newValue) throws ConfigurationException {
        if (Configurator.getInstance() != null && Configurator.getInstance().getServiceProviderFactory() != null) {
            SosServiceProviderFactory fac = Configurator.getInstance().getServiceProviderFactory();
            if (definition.equals(SettingDefinitions.SERVICE_PROVIDER_NAME)) {
                fac.setName((String) newValue.getValue());
            } else if (definition.equals(SettingDefinitions.SERVICE_PROVIDER_SITE)) {
                /* TODO change the site property to URI */
                fac.setSite(((URI) newValue.getValue()).toString());
            } else if (definition.equals(SettingDefinitions.SERVICE_PROVIDER_INDIVIDUAL_NAME)) {
                fac.setIndividualName((String) newValue.getValue());
            } else if (definition.equals(SettingDefinitions.SERVICE_PROVIDER_POSITION_NAME)) {
                fac.setPositionName((String) newValue.getValue());
            } else if (definition.equals(SettingDefinitions.SERVICE_PROVIDER_PHONE)) {
                fac.setPhone((String) newValue.getValue());
            } else if (definition.equals(SettingDefinitions.SERVICE_PROVIDER_DELIVERY_POINT)) {
                fac.setDeliveryPoint((String) newValue.getValue());
            } else if (definition.equals(SettingDefinitions.SERVICE_PROVIDER_CITY)) {
                fac.setCity((String) newValue.getValue());
            } else if (definition.equals(SettingDefinitions.SERVICE_PROVIDER_POSTAL_CODE)) {
                fac.setPostalCode((String) newValue.getValue());
            } else if (definition.equals(SettingDefinitions.SERVICE_PROVIDER_ADMINISTRATIVE_AREA)) {
                fac.setAdministrativeArea((String) newValue.getValue());
            } else if (definition.equals(SettingDefinitions.SERVICE_PROVIDER_COUNTRY)) {
                fac.setCountry((String) newValue.getValue());
            } else if (definition.equals(SettingDefinitions.SERVICE_PROVIDER_MAIL_ADDRESS)) {
                fac.setMailAddress((String) newValue.getValue());
            } else if (definition.equals(SettingDefinitions.SERVICE_PROVIDER_FILE)) {
                fac.setFile((File) newValue.getValue());
            }
            throw new ConfigurationException("Unknown setting: " + definition);
        }

    }
}
