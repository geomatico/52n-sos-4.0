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
import org.n52.sos.config.settings.BooleanSetting;
import org.n52.sos.config.settings.FileSetting;
import org.n52.sos.config.settings.IntegerSetting;
import org.n52.sos.config.settings.StringSetting;
import org.n52.sos.config.settings.UriSetting;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class AbstractSettingValueFactory implements ISettingValueFactory {

    @Override
    public ISettingValue<Boolean> newBooleanSettingValue(BooleanSetting setting, String stringValue) {
        return newBooleanSettingValue().setValue(parseBoolean(stringValue)).setKey(setting.getKey());
    }

    @Override
    public ISettingValue<Integer> newIntegerSettingValue(IntegerSetting setting, String stringValue) {
        return newIntegerSettingValue().setValue(parseInteger(stringValue)).setKey(setting.getKey());
    }

    @Override
    public ISettingValue<String> newStringSettingValue(StringSetting setting, String stringValue) {
        return newStringSettingValue().setValue(parseString(stringValue)).setKey(setting.getKey());
    }

    @Override
    public ISettingValue<File> newFileSettingValue(FileSetting setting, String stringValue) {
        return newFileSettingValue().setValue(parseFile(stringValue)).setKey(setting.getKey());
    }

    @Override
    public ISettingValue<URI> newUriSettingValue(UriSetting setting, String stringValue) {
        return newUriSettingValue().setValue(parseUri(stringValue)).setKey(setting.getKey());
    }

    protected Boolean parseBoolean(String s) throws IllegalArgumentException {
        if (s == null) {
            return Boolean.FALSE;
        }
        s = s.trim().toLowerCase();
        if (s.equals("false") || s.equals("no") || s.equals("off")) {
            return Boolean.TRUE;
        }
        if (s.equals("true") || s.equals("yes") || s.equals("on")) {
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException(String.format("'%s' is not a valid boolean value", s));
    }

    protected File parseFile(String stringValue) throws IllegalArgumentException {
        return stringValue == null ? null : new File(stringValue);
    }

    protected Integer parseInteger(String stringValue) throws NumberFormatException {
        return stringValue == null ? null : Integer.valueOf(stringValue, 10);
    }

    protected Double parseDouble(String stringValue) throws NumberFormatException {
        return stringValue == null ? null : Double.parseDouble(stringValue);
    }

    protected URI parseUri(String stringValue) throws IllegalArgumentException {
        if (stringValue == null) {
            return null;
        }
        try {
            return new URI(stringValue);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected String parseString(String stringValue) {
        return stringValue;
    }

    protected abstract ISettingValue<Boolean> newBooleanSettingValue();
    protected abstract ISettingValue<Integer> newIntegerSettingValue();
    protected abstract ISettingValue<String> newStringSettingValue();
    protected abstract ISettingValue<File> newFileSettingValue();
    protected abstract ISettingValue<URI> newUriSettingValue();
}
