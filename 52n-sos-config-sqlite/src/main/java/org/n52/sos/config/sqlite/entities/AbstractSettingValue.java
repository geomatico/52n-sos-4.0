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
package org.n52.sos.config.sqlite.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.n52.sos.config.SettingValue;

/**
 * @param <T> settings type
 * <p/>
 * @author Christian Autermann <c.autermann@52north.org>
 */
@Entity(name = "settings")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractSettingValue<T> implements SettingValue<T>, Serializable {
    
    @Id
    private String identifier;

    @Override
    public String getKey() {
        return this.identifier;
    }

    @Override
    public SettingValue<T> setKey(String key) {
        this.identifier = key;
        return this;
    }

    @Override
    public String toString() {
        return String.format("%s[key=%s, value=%s]", getClass().getSimpleName(), getKey(), getValue());
    }

    @Override
    public int hashCode() {
        final int prime = 79;
        int hash = 7;
        hash = prime * hash + (this.getKey() != null ? this.getKey().hashCode() : 0);
        hash = prime * hash + (this.getValue() != null ? this.getValue().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof SettingValue) {
            final SettingValue<?> other = (SettingValue<?>) obj;
            return (getKey() == null ? other.getKey() == null : getKey().equals(other.getKey()))
                   && (getValue() == null ? other.getValue() == null : getValue().equals(other.getValue()));
        }
        return false;
    }
}
