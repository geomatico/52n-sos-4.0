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

package org.n52.sos.web.admin;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.n52.sos.web.AbstractController;
import org.n52.sos.web.ControllerConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
@Controller
public class AdminLibraryListingController extends AbstractController {
    private static final FileFilter FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname != null && pathname.isFile();
        }
    };

    @RequestMapping(ControllerConstants.Paths.ADMIN_LIBRARY_LIST)
    public ModelAndView view() {
        List<String> libs;
        File lib = new File(getContext().getRealPath("WEB-INF/lib"));
        if (lib.exists() && lib.isDirectory() && lib.canRead()) {
            final File[] listFiles = lib.listFiles(FILE_FILTER);
            libs = new ArrayList<String>(listFiles.length);
            for (File f : listFiles) {
                libs.add(f.getName());
            }
        } else {
            libs = Collections.emptyList();
        }
        Collections.sort(libs);
        return new ModelAndView(ControllerConstants.Views.ADMIN_LIBRARY_LIST,
                                ControllerConstants.LIBRARIES_MODEL_ATTRIBUTE,
                                libs);
    }

}
