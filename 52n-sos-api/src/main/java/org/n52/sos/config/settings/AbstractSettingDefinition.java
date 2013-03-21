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

import org.n52.sos.config.AbstractOrdered;
import org.n52.sos.config.SettingDefinition;
import org.n52.sos.config.SettingDefinitionGroup;
import org.n52.sos.config.SettingType;

/**
 * Generic implementation of
 * <code>SettingDefinition</code>.
 *
 * @param <S> the type of the class extending this class
 * @param <T> the type of the value
 *
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
abstract class AbstractSettingDefinition<S extends AbstractSettingDefinition<S, T>, T> extends AbstractOrdered<S>
        implements SettingDefinition<S, T> {

    private boolean optional = false;
    private String identifier;
    private String title;
    private String description;
    private SettingDefinitionGroup group;
    private final SettingType type;
    private T defaultValue;

    /**
     * @param type the <code>SettingType</code> of this setting definition
     */
    protected AbstractSettingDefinition(SettingType type) {
        this.type = type;
    }

    @Override
    public String getKey() {
        return identifier;
    }

    @Override
    @SuppressWarnings("unchecked")
    public S setKey(String key) {
        this.identifier = key;
        return (S) this;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean hasDescription() {
        return hasStringProperty(getDescription());
    }

    @Override
    @SuppressWarnings("unchecked")
    public S setDescription(String description) {
        this.description = description;
        return (S) this;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public boolean hasTitle() {
        return hasStringProperty(getTitle());
    }

    @Override
    @SuppressWarnings("unchecked")
    public S setTitle(String title) {
        this.title = title;
        return (S) this;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    @SuppressWarnings("unchecked")
    public S setOptional(boolean optional) {
        this.optional = optional;
        return (S) this;
    }

    @Override
    public SettingDefinitionGroup getGroup() {
        return group;
    }

    @Override
    public boolean hasGroup() {
        return getGroup() != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public S setGroup(SettingDefinitionGroup group) {
        this.group = group;
        return (S) this;
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
    @SuppressWarnings("unchecked")
    public S setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return (S) this;
    }

    protected boolean hasStringProperty(String s) {
        return s != null && !s.isEmpty();
    }

    @Override
    public int hashCode() {
        return (getKey() != null) ? getKey().hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AbstractSettingDefinition) {
            AbstractSettingDefinition<?, ?> o = (AbstractSettingDefinition<?, ?>) obj;
            return getKey() == null ? o.getKey() == null : getKey().equals(o.getKey()) && getTitle() == null ? o
                    .getTitle() == null : getTitle().equals(o.getTitle()) && getDescription() == null ? o
                    .getDescription() == null : getDescription().equals(o.getDescription()) && getGroup() == null ? o
                    .getGroup() == null : getGroup().equals(o.getGroup()) && getDefaultValue() == null ? o
                    .getDefaultValue() == null : getDefaultValue().equals(o.getDefaultValue()) && getType() == o
                    .getType() && isOptional() == o.isOptional();
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s[key=%s]", getClass().getSimpleName(), getKey());
    }

    @Override
    public SettingType getType() {
        return this.type;
    }

    @Override
    protected String getSuborder() {
        return getTitle();
    }
}
