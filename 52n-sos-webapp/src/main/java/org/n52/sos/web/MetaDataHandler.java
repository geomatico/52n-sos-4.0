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
package org.n52.sos.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaDataHandler {
    public enum Metadata {
        SVN_VERSION,
        VERSION,
        BUILD_DATE,
        INSTALL_DATE;
    }
    private static final Logger log = LoggerFactory.getLogger(MetaDataHandler.class);
    private static final String PROPERTIES = "/meta.properties";
    private static MetaDataHandler instance = new MetaDataHandler();
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Properties cache;

    public static synchronized MetaDataHandler getInstance() {
        return instance;
    }

    private MetaDataHandler() {
    }

    private Properties load() throws IOException {
        if (this.cache == null) {
            InputStream is = null;
            try {
                is = MetaDataHandler.class.getResourceAsStream(PROPERTIES);
                Properties p = new Properties();
                p.load(is);
                cache = p;
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        log.error("Error closing input stream", e);
                    }
                }
            }
        }
        return cache;
    }

    private void save(Properties p) throws IOException {
        OutputStream os = null;
        try {
            File f = new File(MetaDataHandler.class.getResource(PROPERTIES).toURI());
            os = new FileOutputStream(f);
            p.store(os, null);
            this.cache = p;
        } catch (URISyntaxException ex) {
            throw new FileNotFoundException(ex.getMessage());
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    log.error("Error closing output stream", e);
                }
            }
        }
    }

    public String getMetadata(Metadata m) {
        lock.readLock().lock();
        try {
            return load().getProperty(m.name());
        } catch (IOException e) {
            String message = "Error reading properties";
            throw new RuntimeException(message, e);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void saveMetadata(Metadata m, String value) {
        lock.writeLock().lock();
        try {
            Properties p = load();
            p.setProperty(m.name(), value);
            save(p);
        } catch (IOException e) {
            String message = "Error writing properties";
            throw new RuntimeException(message, e);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
