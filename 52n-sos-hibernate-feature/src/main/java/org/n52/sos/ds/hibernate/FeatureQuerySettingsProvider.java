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
package org.n52.sos.ds.hibernate;

import java.util.Collections;
import java.util.Set;

import org.n52.sos.config.ISettingDefinition;
import org.n52.sos.config.ISettingDefinitionProvider;
import org.n52.sos.config.settings.IntegerSettingDefinition;
import org.n52.sos.config.settings.StringSettingDefinition;
import org.n52.sos.service.MiscSettingDefinitions;
import org.n52.sos.service.ServiceSettingDefinitions;
import org.n52.sos.util.CollectionHelper;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class FeatureQuerySettingsProvider implements ISettingDefinitionProvider {
    
    public static final String EPSG_CODES_WITH_REVERSED_AXIS_ORDER = "misc.switchCoordinatesForEpsgCodes";
    public static final String DEFAULT_EPSG = "service.defaultEpsg";
    
    public static final StringSettingDefinition EPSG_CODES_WITH_REVERSED_AXIS_ORDER_DEFINITION = new StringSettingDefinition()
            .setGroup(MiscSettingDefinitions.GROUP)
            .setOrder(6)
            .setKey(EPSG_CODES_WITH_REVERSED_AXIS_ORDER)
            .setOptional(true)
            .setDefaultValue("2044-2045;2081-2083;2085-2086;2093;2096-2098;2105-2132;2169-2170;2176-2180;"
                            + "2193;2200;2206-2212;2319;2320-2462;2523-2549;2551-2735;2738-2758;2935-2941;"
                            + "2953;3006-3030;3034-3035;3058-3059;3068;3114-3118;3126-3138;3300-3301;3328-3335;"
                            + "3346;3350-3352;3366;3416;4001-4999;20004-20032;20064-20092;21413-21423;21473-21483;"
                            + "21896-21899;22171;22181-22187;22191-22197;25884;27205-27232;27391-27398;27492;"
                            + "28402-28432;28462-28492;30161-30179;30800;31251-31259;31275-31279;31281-31290;31466-31700")
            .setTitle("EPSG Codes with Switched Coordinates")
            .setDescription("A list of all EPSG codes for which the SOS has to switch coordinate order, "
                            + "for example from lat/lon to lon/lat, or from x/y to y/x.");
    
    
    public static final IntegerSettingDefinition DEFAULT_EPSG_DEFINITION = new IntegerSettingDefinition()
            .setGroup(ServiceSettingDefinitions.GROUP)
            .setOrder(1)
            .setKey(DEFAULT_EPSG)
            .setDefaultValue(4326)
            .setTitle("Default EPSG Code")
            .setDescription("The EPSG code in which the geometries are stored.");
    
    
    private static final Set<ISettingDefinition<?, ?>> DEFINITIONS = CollectionHelper.<ISettingDefinition<?, ?>>set(
            EPSG_CODES_WITH_REVERSED_AXIS_ORDER_DEFINITION, DEFAULT_EPSG_DEFINITION);
    
    @Override
    public Set<ISettingDefinition<?, ?>> getSettingDefinitions() {
        return Collections.unmodifiableSet(DEFINITIONS);
    }
}
