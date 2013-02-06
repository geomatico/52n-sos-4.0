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
package org.n52.sos.config.settings;

import org.n52.sos.config.ISetting;

/**
 * @param <T> 
 * @author Christian Autermann <c.autermann@52north.org>
 */
abstract class AbstractSetting<T> implements ISetting<T> {
    
    private boolean optional;
    private String identifier;
    private String title;
    private String description;
    private T defaultValue;

    @Override
    public String getKey() {
        return identifier;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean hasDefaultValue() {
        return getDefaultValue() != null;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public AbstractSetting<T> setOptional(boolean optional) {
        this.optional = optional;
        return this;
    }

    public AbstractSetting<T> setKey(String key) {
        this.identifier = key;
        return this;
    }

    public AbstractSetting<T> setTitle(String title) {
        this.title = title;
        return this;
    }

    public AbstractSetting<T> setDescription(String description) {
        this.description = description;
        return this;
    }

    public AbstractSetting<T> setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public int hashCode() {
        return (getKey() == null) ? 0 : getKey().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AbstractSetting) {
            AbstractSetting<?> other = (AbstractSetting<?>) obj;
            return getKey() == null ? other.getKey() == null : getKey().equals(other.getKey());
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s[key=%s]", getClass().getSimpleName(), getKey());
    }

}
