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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.n52.sos.config.settings.BooleanSettingDefinition;
import org.n52.sos.config.settings.FileSettingDefinition;
import org.n52.sos.config.settings.IntegerSettingDefinition;
import org.n52.sos.config.settings.NumericSettingDefinition;
import org.n52.sos.config.settings.StringSettingDefinition;
import org.n52.sos.config.settings.UriSettingDefinition;
import org.n52.sos.util.CollectionHelper;

/**
 * Abstract implementation that does everything exept object instantiation.
 * <p/>
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
public abstract class AbstractSettingValueFactory implements ISettingValueFactory {

    private static final Set<String> VALID_FALSE_VALUES = CollectionHelper.set("false", "no", "off", "0");
    private static final Set<String> VALID_TRUE_VALUES = CollectionHelper.set("true", "yes", "on", "1");

    @Override
    public ISettingValue<Boolean> newBooleanSettingValue(BooleanSettingDefinition setting, String stringValue) {
        return newBooleanSettingValueFromGenericDefinition(setting, stringValue);
    }

    private ISettingValue<Boolean> newBooleanSettingValueFromGenericDefinition(ISettingDefinition<?, ?> setting,
                                                                               String stringValue) {
        return newBooleanSettingValue().setValue(parseBoolean(stringValue)).setKey(setting.getKey());
    }

    @Override
    public ISettingValue<Integer> newIntegerSettingValue(IntegerSettingDefinition setting, String stringValue) {
        return newIntegerSettingValueFromGenericDefinition(setting, stringValue);
    }

    private ISettingValue<Integer> newIntegerSettingValueFromGenericDefinition(ISettingDefinition<?, ?> setting,
                                                                               String stringValue) {
        return newIntegerSettingValue().setValue(parseInteger(stringValue)).setKey(setting.getKey());
    }

    @Override
    public ISettingValue<Double> newNumericSettingValue(NumericSettingDefinition setting, String stringValue) {
        return newNumericSettingValueFromGenericDefinition(setting, stringValue);
    }

    private ISettingValue<Double> newNumericSettingValueFromGenericDefinition(ISettingDefinition<?, ?> setting,
                                                                              String stringValue) {
        return newNumericSettingValue().setValue(parseDouble(stringValue)).setKey(setting.getKey());
    }

    @Override
    public ISettingValue<String> newStringSettingValue(StringSettingDefinition setting, String stringValue) {
        return newStringSettingValueFromGenericDefinition(setting, stringValue);
    }

    private ISettingValue<String> newStringSettingValueFromGenericDefinition(ISettingDefinition<?, ?> setting,
                                                                             String stringValue) {
        return newStringSettingValue().setValue(parseString(stringValue)).setKey(setting.getKey());
    }

    @Override
    public ISettingValue<File> newFileSettingValue(FileSettingDefinition setting, String stringValue) {
        return newFileSettingValueFromGenericDefinition(setting, stringValue);
    }

    private ISettingValue<File> newFileSettingValueFromGenericDefinition(ISettingDefinition<?, ?> setting,
                                                                         String stringValue) {
        return newFileSettingValue().setValue(parseFile(stringValue)).setKey(setting.getKey());
    }

    @Override
    public ISettingValue<URI> newUriSettingValue(UriSettingDefinition setting, String stringValue) {
        return newUriSettingValueFromGenericDefinition(setting, stringValue);
    }

    private ISettingValue<URI> newUriSettingValueFromGenericDefinition(ISettingDefinition<?, ?> setting,
                                                                       String stringValue) {
        return newUriSettingValue().setValue(parseUri(stringValue)).setKey(setting.getKey());
    }

    @Override
    public ISettingValue<?> newSettingValue(ISettingDefinition<?, ?> setting, String value) {
        switch (setting.getType()) {
        case BOOLEAN:
            return newBooleanSettingValueFromGenericDefinition(setting, value);
        case FILE:
            return newFileSettingValueFromGenericDefinition(setting, value);
        case INTEGER:
            return newIntegerSettingValueFromGenericDefinition(setting, value);
        case NUMERIC:
            return newNumericSettingValueFromGenericDefinition(setting, value);
        case STRING:
            return newStringSettingValueFromGenericDefinition(setting, value);
        case URI:
            return newUriSettingValueFromGenericDefinition(setting, value);
        default:
            throw new IllegalArgumentException(String.format("Type %s not supported", setting.getType()));
        }
    }

    /**
     * Parses the a string to a {@code Boolean}.
     * <p/>
     * @param stringValue the string value
     * <p/>
     * @return the parsed value
     * <p/>
     * @throws IllegalArgumentException if the string value is invalid
     */
    protected Boolean parseBoolean(String stringValue) throws IllegalArgumentException {
        if (nullOrEmpty(stringValue)) {
            return Boolean.FALSE;
        }
        stringValue = stringValue.trim().toLowerCase();
        if (VALID_FALSE_VALUES.contains(stringValue)) {
            return Boolean.FALSE;
        } else if (VALID_TRUE_VALUES.contains(stringValue)) {
            return Boolean.TRUE;
        } else {
            throw new IllegalArgumentException(String.format("'%s' is not a valid boolean value", stringValue));
        }
    }

    /**
     * Parses the a string to a {@code File}.
     * <p/>
     * @param stringValue the string value
     * <p/>
     * @return the parsed value
     * <p/>
     * @throws IllegalArgumentException if the string value is invalid
     */
    protected File parseFile(String stringValue) throws IllegalArgumentException {
        return nullOrEmpty(stringValue) ? null : new File(stringValue);
    }

    /**
     * Parses the a string to a {@code Integer}.
     * <p/>
     * @param stringValue the string value
     * <p/>
     * @return the parsed value
     * <p/>
     * @throws IllegalArgumentException if the string value is invalid
     */
    protected Integer parseInteger(String stringValue) throws NumberFormatException {
        return nullOrEmpty(stringValue) ? null : Integer.valueOf(stringValue, 10);
    }

    /**
     * Parses the a string to a {@code Double}.
     * <p/>
     * @param stringValue the string value
     * <p/>
     * @return the parsed value
     * <p/>
     * @throws IllegalArgumentException if the string value is invalid
     */
    protected Double parseDouble(String stringValue) throws NumberFormatException {
        return nullOrEmpty(stringValue) ? null : Double.parseDouble(stringValue);
    }

    /**
     * Parses the a string to a {@code URI}.
     * <p/>
     * @param stringValue the string value
     * <p/>
     * @return the parsed value
     * <p/>
     * @throws IllegalArgumentException if the string value is invalid
     */
    protected URI parseUri(String stringValue) throws IllegalArgumentException {
        if (nullOrEmpty(stringValue)) {
            return null;
        }
        try {
            return new URI(stringValue);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parses the a string to a {@code String}.
     * <p/>
     * @param stringValue the string value
     * <p/>
     * @return the parsed value
     * <p/>
     * @throws IllegalArgumentException if the string value is invalid
     */
    protected String parseString(String stringValue) {
        return nullOrEmpty(stringValue) ? null : stringValue;
    }

    /**
     * @return a implementation specific instance
     */
    protected abstract ISettingValue<Boolean> newBooleanSettingValue();

    /**
     * @return a implementation specific instance
     */
    protected abstract ISettingValue<Integer> newIntegerSettingValue();

    /**
     * @return a implementation specific instance
     */
    protected abstract ISettingValue<String> newStringSettingValue();

    /**
     * @return a implementation specific instance
     */
    protected abstract ISettingValue<File> newFileSettingValue();

    /**
     * @return a implementation specific instance
     */
    protected abstract ISettingValue<URI> newUriSettingValue();

    /**
     * @return a implementation specific instance
     */
    protected abstract ISettingValue<Double> newNumericSettingValue();

    /**
     * @param stringValue
     * <p/>
     * @return <code>stringValue == null || stringValue.trim().isEmpty()</code>
     */
    protected boolean nullOrEmpty(String stringValue) {
        return stringValue == null || stringValue.trim().isEmpty();
    }
}
