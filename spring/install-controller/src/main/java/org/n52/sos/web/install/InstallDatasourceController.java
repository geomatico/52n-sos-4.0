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

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.n52.sos.config.SettingDefinition;
import org.n52.sos.config.SettingValue;
import org.n52.sos.ds.Datasource;
import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.web.ControllerConstants;
import org.n52.sos.web.install.InstallConstants.Step;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(ControllerConstants.Paths.INSTALL_DATASOURCE)
public class InstallDatasourceController extends AbstractProcessingInstallationController {

    @Override
    protected Step getStep() {
        return Step.DATASOURCE;
    }

    @Override
    protected void process(Map<String, String> parameters,
                           InstallationConfiguration c)
            throws InstallationSettingsError {
        boolean overwriteTables;
        boolean alreadyExistent;
        boolean createTables;
        Datasource datasource;

        try {
            datasource = checkDatasource(parameters, c);
            overwriteTables = checkOverwrite(datasource, parameters, c);
            createTables =
                    checkCreate(datasource, parameters, overwriteTables, c);
            checkCreateTestData(datasource, parameters, c);
            c.setDatabaseSettings(parseDatasourceSettings(datasource,
                                                          parameters));
            datasource.validateConnection(c.getDatabaseSettings());
            datasource.validatePrerequisites(c.getDatabaseSettings());



            if (datasource.needsSchema()) {
                alreadyExistent = datasource.checkIfSchemaExists(c
                        .getDatabaseSettings());

                if (createTables) {
                    if (alreadyExistent && !overwriteTables) {
                        throw new InstallationSettingsError(c, ErrorMessages.TABLES_ALREADY_CREATED_BUT_SHOULD_NOT_OVERWRITE);
                    }
                    datasource.checkSchemaCreation(c.getDatabaseSettings());
                } else if (!alreadyExistent) {
                    throw new InstallationSettingsError(c, ErrorMessages.NO_TABLES_AND_SHOULD_NOT_CREATE);
                } else {
                    try {
                        datasource.validateSchema(c.getDatabaseSettings());
                    } catch (ConfigurationException e) {
                        throw new InstallationSettingsError(c, String.format(
                                "The installed schema is corrupt/invalid (%s). Try to delete the existing tables.", e
                                .getMessage()), e);
                    }
                }
            }
        } catch (ConfigurationException e) {
            throw new InstallationSettingsError(c, e.getMessage(), e);
        }
    }

    protected boolean checkOverwrite(Datasource datasource,
                                     Map<String, String> parameters,
                                     InstallationConfiguration settings) {
        boolean overwriteTables = false;
        if (datasource.needsSchema()) {
            Boolean overwriteTablesParameter =
                    parseBoolean(parameters,
                                 InstallConstants.OVERWRITE_TABLES_PARAMETER);
            if (overwriteTablesParameter != null) {
                overwriteTables = overwriteTablesParameter.booleanValue();
            }
        }
        parameters.remove(InstallConstants.OVERWRITE_TABLES_PARAMETER);
        settings.setDropSchema(overwriteTables);
        return overwriteTables;
    }

    protected boolean checkCreate(Datasource datasource,
                                  Map<String, String> parameters,
                                  boolean overwriteTables,
                                  InstallationConfiguration settings) {
        boolean createTables = false;
        if (datasource.needsSchema()) {
            Boolean createTablesParameter =
                    parseBoolean(parameters,
                                 InstallConstants.CREATE_TABLES_PARAMETER);
            if (createTablesParameter != null) {
                createTables = (overwriteTables) ? overwriteTables
                               : createTablesParameter.booleanValue();
            }
        }
        parameters.remove(InstallConstants.CREATE_TABLES_PARAMETER);
        settings.setCreateSchema(createTables);
        return createTables;
    }

    protected boolean checkCreateTestData(Datasource datasource,
                                          Map<String, String> parameters,
                                          InstallationConfiguration settings) {

        boolean createTestData = false;
        if (datasource.supportsTestData()) {
            Boolean createTestDataParameter =
                    parseBoolean(parameters,
                                 InstallConstants.CREATE_TEST_DATA_PARAMETER);
            if (createTestDataParameter != null) {
                createTestData = createTestDataParameter.booleanValue();
            }
        }
        parameters.remove(InstallConstants.CREATE_TEST_DATA_PARAMETER);
        settings.setCreateTestData(createTestData);
        return createTestData;
    }

    protected Map<String, Object> parseDatasourceSettings(
            Datasource datasource, Map<String, String> parameters) {
        Set<SettingDefinition<?, ?>> defs = datasource.getSettingDefinitions();
        Map<String, Object> parsedSettings =
                new HashMap<String, Object>(parameters.size());
        for (SettingDefinition<?, ?> def : defs) {
            SettingValue<?> newValue = getSettingsManager().getSettingFactory()
                    .newSettingValue(def, parameters.get(def.getKey()));
            parsedSettings.put(def.getKey(), newValue.getValue());
        }
        return parsedSettings;
    }

    protected Map<String, Datasource> getDatasources() {
        ServiceLoader<Datasource> load =
                ServiceLoader.load(Datasource.class);
        Map<String, Datasource> dialects = CollectionHelper.map();
        for (Datasource dd : load) {
            dialects.put(dd.getDialectName(), dd);
        }
        return dialects;
    }

    protected Datasource checkDatasource(
            Map<String, String> parameters,
            InstallationConfiguration settings)
            throws InstallationSettingsError {
        String datasourceName = parameters
                .get(InstallConstants.DATASOURCE_PARAMETER);
        parameters.remove(InstallConstants.DATASOURCE_PARAMETER);
        Datasource datasource = getDatasources().get(datasourceName);
        if (datasource == null) {
            throw new InstallationSettingsError(settings, String
                    .format(ErrorMessages.INVALID_DATASOURCE, datasourceName));
        } else {
            settings.setDatasource(datasource);
        }
        return datasource;
    }
}
