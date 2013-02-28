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

import org.n52.sos.service.DatabaseSettingsHandler;

public class JstlFunctions {
    public static final boolean HAS_INSTALLER = hasClass("org.n52.sos.web.install.InstallIndexController");
    public static final boolean HAS_CLIENT = hasClass("org.n52.sos.web.ClientController");
    public static final boolean HAS_ADMIN = hasClass("org.n52.sos.web.admin.AdminIndexController");

    public static boolean configurated(ServletContext ctx) {
        return DatabaseSettingsHandler.getInstance(ctx).exists();
    }

    public static boolean hasClient() {
        return HAS_CLIENT;
    }

    public static boolean hasInstaller() {
        return HAS_INSTALLER;
    }

    public static boolean hasAdministrator() {
        return HAS_ADMIN;
    }

    private static boolean hasClass(String c) {
        try {
            Class.forName(c);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    private JstlFunctions() {
    }
}
