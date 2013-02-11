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

import org.n52.sos.service.Setting;
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

        for (Setting p : Setting.values()) {
            checkSetting(p, parameters.get(p.name()), c);
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

    protected void checkSetting(Setting p, String value, InstallationConfiguration c) throws InstallationSettingsError {
        if (p.isAllowedValue(value)) {
            if (p.type() == Setting.Type.FILE && value != null && !value.isEmpty()) {
                checkFileSetting(value, c, p);
            } else {
                c.setSetting(p.name(), p.parse(value));
            }
        } else {
            /* TODO include longer name of parameter */
            throw new InstallationSettingsError(c, String.format(ErrorMessages.COULD_NOT_VALIDATE_PARAMETER, p.name(), value));
        }
    }

    protected void checkFileSetting(String value, InstallationConfiguration c, Setting p) throws InstallationSettingsError {
        if (new File(value).exists() || new File(getBasePath() + value).exists()) {
            c.setSetting(p.name(), p.parse(value));
        } else {
            throw new InstallationSettingsError(c, String.format(ErrorMessages.CANNOT_FIND_FILE, getBasePath(), value, value));
        }
    }
}
