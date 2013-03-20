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

import org.n52.sos.ogc.ows.SosServiceIdentificationFactorySettings;
import org.n52.sos.ogc.ows.SosServiceProviderFactorySettings;
import org.n52.sos.service.MiscSettings;
import org.n52.sos.service.ServiceSettings;

/**
 * Class to group ISettingDefinitions. Not needed by the service but only for representation in the GUI.
 * <p/>
 * @see ServiceSettings#GROUP
 * @see MiscSettings#GROUP
 * @see SosServiceProviderFactorySettings#GROUP
 * @see SosServiceIdentificationFactorySettings#GROUP
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
public class SettingDefinitionGroup extends AbstractOrdered<SettingDefinitionGroup> {

    private String title;
    private String description;

    /**
     * @return the title of this group
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return if this group has a non empty title
     */
    public boolean hasTitle() {
        return hasStringProperty(getTitle());
    }

    /**
     * Sets the title of this group.
     *
     * @param title the title
     *
     * @return this
     */
    public SettingDefinitionGroup setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * @return the description for this group
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return if this group has a non empty description
     */
    public boolean hasDescription() {
        return hasStringProperty(getDescription());
    }

    /**
     * Sets the description for this group.
     *
     * @param description the description
     *
     * @return this
     */
    public SettingDefinitionGroup setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Checks if the parameter is not null and not empty.
     *
     * @param s the string to test
     *
     * @return if it is not null and not empty
     */
    protected boolean hasStringProperty(String s) {
        return s != null && !s.isEmpty();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.getTitle() != null ? this.getTitle()
                            .hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SettingDefinitionGroup other = (SettingDefinitionGroup) obj;
        if ((this.getTitle() == null) ? (other.getTitle() != null)
            : !this.getTitle()
                .equals(other.getTitle())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s[title=%s, description=%s]", getClass()
                .getSimpleName(), getTitle(), getDescription());
    }

    @Override
    protected String getSuborder() {
        return getTitle();
    }
}
