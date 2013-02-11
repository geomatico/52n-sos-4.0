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

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class IntegerSettingDefinition extends AbstractSettingDefinition<Integer> {

    private Integer minimum;
    private Integer maximum;
    private boolean exclusiveMaximum = false;
    private boolean exclusiveMinimum = false;

    /**
     * Get the value of minimum
     * <p/>
     * @return the value of minimum
     */
    public Integer getMinimum() {
        return minimum;
    }

    /**
     * @return whether a minimum value is set
     */
    public boolean hasMinimum() {
        return getMinimum() != null;
    }

    /**
     * Set the value of minimum
     * <p/>
     * @param minimum new value of minimum
     * <p/>
     * @return this
     */
    public IntegerSettingDefinition setMinimum(Integer minimum) {
        this.minimum = minimum;
        return this;
    }

    /**
     * Get the value of maximum
     * <p/>
     * @return the value of maximum
     */
    public Integer getMaximum() {
        return maximum;
    }

    /**
     * @return whether a maximum value is set
     */
    public boolean hasMaximum() {
        return getMaximum() != null;
    }

    /**
     * Set the value of maximum
     * <p/>
     * @param maximum new value of maximum
     * <p/>
     * @return this
     */
    public IntegerSettingDefinition setMaximum(Integer maximum) {
        this.maximum = maximum;
        return this;
    }

    /**
     * Get the value of exclusiveMaximum
     * <p/>
     * @return the value of exclusiveMaximum
     */
    public boolean isExclusiveMaximum() {
        return exclusiveMaximum;
    }

    /**
     * Set the value of exclusiveMaximum
     * <p/>
     * @param exclusiveMaximum new value of exclusiveMaximum
     * <p/>
     * @return this
     */
    public IntegerSettingDefinition setExclusiveMaximum(boolean exclusiveMaximum) {
        this.exclusiveMaximum = exclusiveMaximum;
        return this;
    }

    /**
     * Get the value of exclusiveMinimum
     * <p/>
     * @return the value of exclusiveMinimum
     */
    public boolean isExclusiveMinimum() {
        return exclusiveMinimum;
    }

    /**
     * Set the value of exclusiveMinimum
     * <p/>
     * @param exclusiveMinimum new value of exclusiveMinimum
     * <p/>
     * @return this
     */
    public IntegerSettingDefinition setExclusiveMinimum(boolean exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
        return this;
    }
}
