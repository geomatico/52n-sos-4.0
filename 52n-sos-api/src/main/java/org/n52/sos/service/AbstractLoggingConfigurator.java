/**
 * Copyright (C) 2012
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
package org.n52.sos.service;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLoggingConfigurator {
    protected static final Logger log = LoggerFactory.getLogger(AbstractLoggingConfigurator.class);

    public static enum Level {
        TRACE, DEBUG, INFO, WARN, ERROR;
        
        public static Level[] getValues() {
            return values();
        }
    }

    public enum Appender {
        FILE("FILE"), CONSOLE("STDOUT");
        
        public final String name;

        private Appender(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
        
        public static Appender byName(String name) {
            for (Appender a : values()) {
                if (a.name.equals(name)) {
                    return a;
                }
            }
            return null;
        }
    }
    
    private static AbstractLoggingConfigurator instance = null;;
    
    public static synchronized AbstractLoggingConfigurator getInstance() {
        if (instance == null) {
            ServiceLoader<AbstractLoggingConfigurator> serviceLoader = ServiceLoader.load(AbstractLoggingConfigurator.class);
            Iterator<AbstractLoggingConfigurator> i = serviceLoader.iterator();
            if (i.hasNext()) {
                instance = i.next();
                log.debug("Using LoggingConfigurator: {}", instance.getClass());
            } else {
                log.error("No implementation class found!");
            }
        }
        return instance;
    }

    public abstract Set<Appender> getEnabledAppender();

    public abstract boolean isEnabled(Appender a);

    public abstract boolean enableAppender(Appender appender, boolean enabled);

    public abstract Level getRootLogLevel();

    public abstract boolean setRootLogLevel(Level level);

    public abstract Map<String, Level> getLoggerLevels();

    public abstract Level getLoggerLevel(String id);

    public abstract boolean setLoggerLevel(String id, Level level);
    
    public abstract boolean setLoggerLevel(Map<String, Level> levels);

    public abstract int getMaxHistory();

    public abstract boolean setMaxHistory(int days);
    
    public abstract List<String> getLastLogEntries(int maxSize);
    
    public abstract InputStream getLogFile();
    
    public abstract String getMaxFileSize();
    
    public abstract boolean setMaxFileSize(String maxFileSize);
}
