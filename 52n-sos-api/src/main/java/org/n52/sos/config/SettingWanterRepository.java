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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.n52.sos.service.ConfigurationException;
import org.n52.sos.util.AbstractServiceLoaderRepository;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
class SettingWanterRepository extends AbstractServiceLoaderRepository<ISettingWanter> {
    private Set<ISetting<?>> wantedSettings = Collections.emptySet();
    private Map<ISetting<?>, Set<ISettingWanter>> settingWantersBySetting = Collections.emptyMap();

    SettingWanterRepository() throws ConfigurationException {
        super(ISettingWanter.class, false);
    }

    public Set<ISetting<?>> getWantedSettings() {
        return Collections.unmodifiableSet(this.wantedSettings);
    }

    public Set<ISettingWanter> getWantersForSetting(ISetting<?> setting) {
        Set<ISettingWanter> set = this.settingWantersBySetting.get(setting);
        if (set == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(set);
        }
    }

    @Override
    protected void processImplementations(Set<ISettingWanter> implementations) throws ConfigurationException {
        this.wantedSettings = new HashSet<ISetting<?>>();
        this.settingWantersBySetting = new HashMap<ISetting<?>, Set<ISettingWanter>>();
        for (ISettingWanter wanter : implementations) {
            Set<ISetting<?>> requiredSettings = wanter.getRequiredSettings();
            this.wantedSettings.addAll(requiredSettings);
            for (ISetting<?> setting : requiredSettings) {
                Set<ISettingWanter> wanters = this.settingWantersBySetting.get(setting);
                if (wanters == null) {
                    this.settingWantersBySetting.put(setting, wanters = new HashSet<ISettingWanter>());
                }
                wanters.add(wanter);
            }
        }
    }
    
}
