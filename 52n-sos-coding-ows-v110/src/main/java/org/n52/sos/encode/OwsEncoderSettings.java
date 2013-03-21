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

package org.n52.sos.encode;

import java.util.Collections;
import java.util.Set;

import org.n52.sos.config.SettingDefinition;
import org.n52.sos.config.SettingDefinitionProvider;
import org.n52.sos.config.settings.BooleanSettingDefinition;
import org.n52.sos.service.MiscSettings;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class OwsEncoderSettings implements SettingDefinitionProvider {
    public static final String INCLUDE_STACK_TRACE_IN_EXCEPTION_REPORT = "misc.includeStackTraceInExceptionReport";
    public static final BooleanSettingDefinition INCLUDE_STACK_TRACE_IN_EXCEPTION_REPORT_DEFINITON =
                                                 new BooleanSettingDefinition()
            .setKey(INCLUDE_STACK_TRACE_IN_EXCEPTION_REPORT)
            .setTitle("Detailed Error Messages")
            .setDescription("Should OWS ExceptionReports include a complete stack trace for the causing exception?")
            .setDefaultValue(false)
            .setGroup(MiscSettings.GROUP)
            .setOrder(15)
            .setKey(INCLUDE_STACK_TRACE_IN_EXCEPTION_REPORT);

    @Override
    public Set<SettingDefinition<?, ?>> getSettingDefinitions() {
        return Collections.<SettingDefinition<?, ?>>singleton(INCLUDE_STACK_TRACE_IN_EXCEPTION_REPORT_DEFINITON);
    }
}
