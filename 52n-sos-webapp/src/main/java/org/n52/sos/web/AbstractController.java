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
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import org.n52.sos.service.DatabaseSettingsHandler;
import javax.servlet.ServletContext;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class AbstractController {

    protected static final Logger log = LoggerFactory.getLogger(AbstractController.class);

    @Autowired
    private ServletContext context;

    public ServletContext getContext() {
        return this.context;
    }

    public void setContext(ServletContext context) {
        this.context = context;
    }

    public String getBasePath() {
        return getContext().getRealPath("/");
    }
    
    public MetaDataHandler getMetaDataHandler() {
        return MetaDataHandler.getInstance(getContext());
    }
    
    public DatabaseSettingsHandler getDatabaseSettingsHandler() {
        return DatabaseSettingsHandler.getInstance(getContext());
    }
    
    protected Boolean parseBoolean(Map<String, String> parameters, String name) {
        return parseBoolean(parameters.get(name));
    }
    protected Boolean parseBoolean(String s) {
        if (s != null && !s.trim().isEmpty()) {
            s = s.trim();
            if (s.equals("true") || s.equals("yes") || s.equals("on")) {
                return Boolean.TRUE;
            }
            if (s.equals("false") || s.equals("no") || s.equals("off")) {
                return Boolean.FALSE;
            }
        }
        return Boolean.FALSE;
    }
}
