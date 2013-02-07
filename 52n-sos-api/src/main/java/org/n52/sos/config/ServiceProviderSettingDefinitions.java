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
package org.n52.sos.config;

import java.io.File;
import java.net.URI;

import org.n52.sos.config.settings.FileSettingDefinition;
import org.n52.sos.config.settings.StringSettingDefinition;
import org.n52.sos.config.settings.UriSettingDefinition;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ServiceProviderSettingDefinitions {

    public static final SettingDefinitionGroup GROUP = new SettingDefinitionGroup()
            .setTitle("Service Provider");
    public static final ISettingDefinition<File> FILE = new FileSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_PROVIDER_FILE")
            .setTitle("Service Provider File")
            .setDescription("The path to a file containing an ows:ServiceProvider "
                            + "overriding the above settings. It can be either an "
                            + "absolute path (like <code>/home/user/sosconfig/provider.xml</code>) "
                            + "or a path relative to the web application directory (e.g. "
                            + "<code>WEB-INF/provider.xml</code>).")
            .setOptional(true);
    public static final ISettingDefinition<String> ADMINISTRATIVE_AREA = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_PROVIDER_STATE")
            .setTitle("State")
            .setDescription("The state of the responsible person.")
            .setDefaultValue("North Rhine-Westphalia");
    public static final ISettingDefinition<String> PHONE = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_PROVIDER_PHONE")
            .setTitle("Phone")
            .setDescription("The phone number of the responsible person.")
            .setDefaultValue("+49(0)251/396 371-0");
    public static final ISettingDefinition<String> DELIVERY_POINT = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_PROVIDER_ADDRESS")
            .setTitle("Address")
            .setDescription("The street address of the responsible person.")
            .setDefaultValue("Martin-Luther-King-Weg 24");
    public static final ISettingDefinition<URI> SITE = new UriSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_PROVIDER_SITE")
            .setTitle("Website")
            .setDescription("Your website.")
            .setDefaultValue(URI.create("http://52north.org/swe"));
    public static final ISettingDefinition<String> CITY = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_PROVIDER_CITY")
            .setTitle("City")
            .setDescription("The city of the responsible person.")
            .setDefaultValue("M\u00fcnster");
    public static final ISettingDefinition<String> POSITION_NAME = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_PROVIDER_POSITION_NAME")
            .setTitle("Position")
            .setDescription("The position of the responsible person.")
            .setDefaultValue("TBA");
    public static final ISettingDefinition<String> NAME = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_PROVIDER_NAME")
            .setTitle("Name")
            .setDescription("Your or your company's name.")
            .setDefaultValue("52North");
    public static final ISettingDefinition<String> INDIVIDUAL_NAME = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_PROVIDER_INDIVIDUAL_NAME")
            .setTitle("Responsible Person")
            .setDescription("The name of the responsible person of this service.")
            .setDefaultValue("TBA");
    public static final ISettingDefinition<String> POSTAL_CODE = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_PROVIDER_ZIP")
            .setTitle("Postal Code")
            .setDescription("The postal code of the responsible person.")
            .setDefaultValue("48155");
    public static final ISettingDefinition<String> MAIL_ADDRESS = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_PROVIDER_EMAIL")
            .setTitle("Mail-Address")
            .setDescription("The electronic mail address of the responsible person.")
            .setDefaultValue("info@52north.org");
    public static final ISettingDefinition<String> COUNTRY = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_PROVIDER_COUNTRY")
            .setTitle("Country")
            .setDescription("The country of the responsible person.")
            .setDefaultValue("Germany");

    private ServiceProviderSettingDefinitions() {
    }
}
