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
package org.n52.sos.web;

import javax.servlet.ServletContext;

import org.n52.sos.util.AbstractEnumPropertiesFileHandler;

public class MetaDataHandler extends AbstractEnumPropertiesFileHandler<MetaDataHandler.Metadata> {

    public enum Metadata {
        SVN_VERSION,
        VERSION,
        BUILD_DATE,
        INSTALL_DATE;
    }
    
    private static final String PROPERTIES = "/WEB-INF/classes/meta.properties";
    private static MetaDataHandler instance;

    static synchronized MetaDataHandler getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Not yet initialized");
        }
        return instance;
    }
    
    static synchronized MetaDataHandler getInstance(ServletContext ctx) {
        if (instance == null) {
            instance = new MetaDataHandler(ctx);
        }
        return instance;
    }

    private MetaDataHandler(ServletContext ctx) {
        super(ctx, PROPERTIES);
    }

}
