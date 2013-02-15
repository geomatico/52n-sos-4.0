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
package org.n52.sos.web.install;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.n52.sos.config.ISettingDefinition;
import org.n52.sos.config.ISettingValue;
import org.n52.sos.config.SettingsManager;
import org.n52.sos.service.ConfigurationException;
import org.n52.sos.web.ControllerConstants;
import org.n52.sos.web.install.InstallConstants.Step;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(ControllerConstants.Paths.INSTALL_SETTINGS)
public class InstallSettingsController extends AbstractProcessingInstallationController {

    @Override
    protected Step getStep() {
        return Step.SETTINGS;
    }

    @Override
    protected void process(Map<String, String> parameters, InstallationConfiguration c) throws InstallationSettingsError {
        logSettings(parameters);

        for (Entry<String, String> p : parameters.entrySet()) {
            checkSetting(p.getKey(), p.getValue(), c);
        }
    }

    protected void logSettings(Map<String, String> parameters) {
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Processing parameters:\n").append("{\n");
            for (Map.Entry<String, String> e : parameters.entrySet()) {
                sb.append("\t").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
            }
            sb.append("}");
            log.debug(sb.toString());
        }
    }

    protected void checkSetting(String key, String stringValue, InstallationConfiguration c) throws
            InstallationSettingsError {
        SettingsManager sm;
        try {
            sm = SettingsManager.getInstance();
        } catch (ConfigurationException ex) {
            throw new InstallationSettingsError(c, String.format(ErrorMessages.COULD_NOT_INSTANTIATE_SETTINGS_MANAGER,
                                                                 ex.getMessage()), ex);
        }
        ISettingDefinition<?, ?> def = sm.getDefinitionByKey(key);
        if (def == null) {
            throw new InstallationSettingsError(c, String.format(ErrorMessages.NO_DEFINITON_FOUND, key));
        }
        ISettingValue<?> val = createSettingValue(sm, def, stringValue, c);
        checkFileSetting(def, val, c);
        c.setSetting(def, val);
    }

    protected ISettingValue<?> createSettingValue(SettingsManager sm, ISettingDefinition<?, ?> def, String stringValue,
                                                InstallationConfiguration c) throws InstallationSettingsError {
        try {
            return sm.getSettingFactory().newSettingValue(def, stringValue);
        } catch (Exception e) {
            throw new InstallationSettingsError(c, String.format(ErrorMessages.COULD_NOT_VALIDATE_PARAMETER, def
                    .getTitle(), stringValue));
        }
    }

    @SuppressWarnings("unchecked")
    protected void checkFileSetting(ISettingDefinition<?, ?> def, ISettingValue<?> val, InstallationConfiguration c)
            throws InstallationSettingsError {
        if (val.getValue() instanceof File) {
            ISettingValue<File> fileSetting = (ISettingValue<File>) val;
            File f = fileSetting.getValue();
            if (!f.exists() && !def.isOptional()) {
                throw new InstallationSettingsError(c, String
                        .format(ErrorMessages.CANNOT_FIND_FILE, f.getAbsolutePath()));
            }
        }
    }
}
