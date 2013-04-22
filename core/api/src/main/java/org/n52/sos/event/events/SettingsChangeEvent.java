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
package org.n52.sos.event.events;

import org.n52.sos.config.SettingDefinition;
import org.n52.sos.config.SettingValue;
import org.n52.sos.event.SosEvent;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class SettingsChangeEvent implements SosEvent {

    private SettingDefinition<?, ?> setting;
    private SettingValue<?> oldValue;
    private SettingValue<?> newValue;

    public SettingsChangeEvent(SettingDefinition<?, ?> setting, SettingValue<?> oldValue, SettingValue<?> newValue) {
        this.setting = setting;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public SettingDefinition<?, ?> getSetting() {
        return setting;
    }

    public SettingValue<?> getOldValue() {
        return oldValue;
    }

    public SettingValue<?> getNewValue() {
        return newValue;
    }

    public boolean hasNewValue() {
        return getNewValue() != null;
    }

    public boolean hasOldValue() {
        return getOldValue() != null;
    }

    @Override
    public String toString() {
        return String
                .format("SettingsChangeEvent[setting=%s, oldValue=%s, newValue=%s", getSetting(), getOldValue(), getNewValue());
    }
}
