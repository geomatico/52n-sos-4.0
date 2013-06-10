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
package org.n52.sos.ds;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.n52.sos.config.SettingDefinition;
import org.n52.sos.config.SettingDefinitionGroup;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public interface Datasource {
    SettingDefinitionGroup BASE_GROUP = new SettingDefinitionGroup()
            .setTitle("Database Configuration").setOrder(1);
    SettingDefinitionGroup ADVANCED_GROUP = new SettingDefinitionGroup()
            .setTitle("Advanced Database Configuration").setOrder(2);

    /**
     * @return the representive name of this dialect
     */
    String getDialectName();

    /**
     * @return the settings needed to connect
     */
    Set<SettingDefinition<?, ?>> getSettingDefinitions();

    /**
     * @return the settings that can be newSettings without schema without
     *         reinstallation
     */
    Set<SettingDefinition<?, ?>> getChangableSettingDefinitions();

    /**
     * Check if a connection is possible.
     *
     * @param settings the settings to connect
     */
    void validateConnection(Map<String, Object> settings);

    /**
     * Check if a connection is still possible with the newSettings settings.
     *
     * @param current the current datasource settings
     * @param newSettings the newSettings settings
     */
    void validateConnection(Properties current,
                            Map<String, Object> newSettings);

    /**
     * Validate if all prerequisites (e.g. datasource version) are met. Will
     * only be called if
     * {@link #validateConnection(java.util.Properties, java.util.Map) validateConnection()}
     * succeeded.
     *
     * @param settings the settings to connect
     */
    void validatePrerequisites(Map<String, Object> settings);

    /**
     * Used to validate prerequisites after the connections settings newSettings
     * in
     * the admin interface.
     *
     * @param current the current datasource settings
     * @param newSettings the newSettings settings
     */
    void validatePrerequisites(Properties current,
                               Map<String, Object> newSettings);

    /**
     * @return if this datasource needs some kind of schema
     */
    boolean needsSchema();

    /**
     * Validate the existing schema. Will only be called if
     * {@link #needsSchema() needsSchema()} and
     * {@link #checkIfSchemaExists(java.util.Map) checkIfSchemaExists()}
     * return {@code true}.
     *
     * @param settings the settings to connect
     */
    void validateSchema(Map<String, Object> settings);

    /**
     * Validate the existing schema. Will only be called if
     * {@link #needsSchema() needsSchema()} and
     * {@link #checkIfSchemaExists(java.util.Properties, java.util.Map) checkIfSchemaExists()}
     * return {@code true}.
     *
     * @param current the current datasource settings
     * @param newSettings the newSettings settings
     */
    void validateSchema(Properties current,
                        Map<String, Object> newSettings);

    /**
     *
     * Check if the schema exists. Should return {@code true} even if parts are
     * missing. Will only be called if {@link #needsSchema()  needsSchema()}
     * returns
     * {@code true}.
     *
     * @param settings the settings to connect
     *
     * @return if the schema (or parts of it) exists
     */
    boolean checkIfSchemaExists(Map<String, Object> settings);

    /**
     * Check if the schema exists. Should return {@code true} even if parts are
     * missing. Will only be called if {@link #needsSchema() needsSchema()}
     * returns
     * {@code true}.
     *
     * @param current the current datasource settings
     * @param newSettings the newSettings settings
     *
     * @return if the schema (or parts of it) exists
     */
    boolean checkIfSchemaExists(Properties current,
                                Map<String, Object> newSettings);

    /**
     * Check if it is possible to create the schema (e.g. test if the privilege
     * are sufficient). Will only be called if
     * {@link #needsSchema() needsSchema()} returns
     * {@code true}.
     *
     * @param settings the settings to connect
     *
     * @return if the creation if the schema is possible
     */
    boolean checkSchemaCreation(Map<String, Object> settings);

    /**
     * Create the schema for the supplied settings. Will only be called if
     * {@link #needsSchema() needsSchema()} and
     * {@link #checkSchemaCreation(java.util.Map) checkSchemaCreation()}
     * return {@code true}. If
     * {@link #checkIfSchemaExists(java.util.Map) checkIfSchemaExists()}
     * returned {@code true}, {@link #dropSchema(java.util.Map) dropSchema()}
     * will be called
     * first.
     *
     * @param settings the settings to connect
     */
    void createSchema(Map<String, Object> settings);

    /**
     * Drop the present schema (or parts of it). Will only be called if
     * {@link #needsSchema() needsSchema()} and
     * {@link #checkIfSchemaExists(java.util.Map) checkIfSchemaExists()} return
     * {@code true}.
     *
     * @param settings the settings to connect
     */
    void dropSchema(Map<String, Object> settings);

    /**
     * @return if this datasource supports the insertion/removal of a test data
     *         set.
     */
    boolean supportsTestData();

    /**
     * Insert the test data set. Should also succeed if the test data set is
     * present. Will only be called if
     * {@link #supportsTestData() supportsTestData()} returns {@code true}.
     *
     * @param settings the settings to connect
     */
    void insertTestData(Map<String, Object> settings);

    /**
     * Insert the test data set into this datasource. Will only be called if
     * {@link #supportsTestData() supportsTestData()} returns {@code true}.
     *
     * @param settings the settings to connect
     */
    void insertTestData(Properties settings);

    /**
     * Check if the test data set is present in this datasource. Will only be
     * called if {@link #supportsTestData() supportsTestData()} returns
     * {@code true}.
     *
     * @param settings the settings to connect
     *
     * @return
     */
    boolean isTestDataPresent(Properties settings);

    /**
     * Remove the test data set from this datasource. Will only be called if
     * {@link #supportsTestData() supportsTestData()} and
     * {@link #isTestDataPresent(java.util.Properties) isTestDataPresent()}
     * return {@code true}.
     *
     * @param settings the settings to connect
     */
    void removeTestData(Properties settings);

    /**
     * Clear the contents of the datasource (if applicable).
     *
     * @param settings the settings to connect
     */
    void clear(Properties settings);

    /**
     * Create the datasource properties used by the {@link ConnectionProvider}
     * to connect.
     *
     * @param settings the settings to connect
     *
     * @return the datasource properties
     */
    Properties getDatasourceProperties(Map<String, Object> settings);

    /**
     * Create the datasource properties used by the {@link ConnectionProvider}
     * to connect.
     *
     * @param current the current datasource settings
     * @param newSettings the newSettings settings
     *
     * @return the new datasource properties
     */
    Properties getDatasourceProperties(Properties current,
                                       Map<String, Object> newSettings);
}
