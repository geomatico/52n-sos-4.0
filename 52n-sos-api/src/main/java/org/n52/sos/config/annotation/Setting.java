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
package org.n52.sos.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.n52.sos.config.SettingDefinition;
import org.n52.sos.config.SettingDefinitionProvider;
import org.n52.sos.config.SettingsManager;

/**
 * Annotation that should be applied to a method that takes a single Setting as a parameter. The parameter of
 * this method should be of the same type as the {@link SettingDefinition} declared with the same {@code key} in a
 * {@code ISettingDefnitionProvider}.
 * <p/>
 * It is needed to apply the {@code Configurable} annotation to a class with a method annotated with this annotations
 * for the {@code SettingsManager} to recognize it.
 * <p/>
 * <b>Example usage:</b>
 * <pre>
 * &#064;Setting(MiscellaneousSettingDefinitions.TOKEN_SEPERATOR_KEY)
 * public void setTokenSeperator(String separator) {
 *     this.separator = separator;
 * }
 * </pre>
 * <p/>
 * @see Configurable
 * @see SettingDefinition
 * @see SettingDefinitionProvider
 * @see SettingsManager
 * <p/>
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Setting {

    /**
     * The key of the setting.
     * <p/>
     * @return the key
     */
    public String value();
}
