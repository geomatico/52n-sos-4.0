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
package org.n52.sos.web.admin;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.n52.sos.service.AbstractLoggingConfigurator;
import org.n52.sos.web.AbstractController;
import org.n52.sos.web.ControllerConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(ControllerConstants.Paths.ADMIN_LOGGING)
public class AdminLoggingController extends AbstractController {

    public static final String IS_CONSOLE_ENABLED = "isConsoleEnabled";
    public static final String IS_FILE_ENABLED = "isFileEnabled";
    public static final String ROOT_LOG_LEVEL = "rootLogLevel";
    public static final String DAYS_TO_KEEP = "daysToKeep";
    public static final String LOGGER_LEVELS = "loggerLevels";
    public static final String ERROR = "error";

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView view() {
        AbstractLoggingConfigurator lc = AbstractLoggingConfigurator.getInstance();
        Map<String, Object> config = new HashMap<String, Object>(5);
        config.put(IS_FILE_ENABLED, lc.isEnabled(AbstractLoggingConfigurator.Appender.FILE));
        config.put(IS_CONSOLE_ENABLED, lc.isEnabled(AbstractLoggingConfigurator.Appender.CONSOLE));
        config.put(ROOT_LOG_LEVEL, lc.getRootLogLevel());
        config.put(DAYS_TO_KEEP, lc.getMaxHistory());
        config.put(LOGGER_LEVELS, lc.getLoggerLevels());
        return new ModelAndView(ControllerConstants.Views.ADMIN_LOGGING, config);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView save(HttpServletRequest req) {
        int daysToKeep = Integer.parseInt(req.getParameter(DAYS_TO_KEEP));
        boolean fileEnabled = parseBoolean(req.getParameter(IS_FILE_ENABLED));
        boolean consoleEnabled = parseBoolean(req.getParameter(IS_CONSOLE_ENABLED));
        AbstractLoggingConfigurator.Level rootLevel = AbstractLoggingConfigurator.Level.valueOf(req.getParameter(ROOT_LOG_LEVEL));
        Map<String, AbstractLoggingConfigurator.Level> levels = new HashMap<String, AbstractLoggingConfigurator.Level>();
        AbstractLoggingConfigurator lc = AbstractLoggingConfigurator.getInstance();
        for (String logger : lc.getLoggerLevels().keySet()) {
            levels.put(logger, AbstractLoggingConfigurator.Level.valueOf(req.getParameter(logger)));
        }
        lc.setMaxHistory(daysToKeep);
        lc.enableAppender(AbstractLoggingConfigurator.Appender.FILE, fileEnabled);
        lc.enableAppender(AbstractLoggingConfigurator.Appender.CONSOLE, consoleEnabled);
        lc.setRootLogLevel(rootLevel);
        for (Map.Entry<String, AbstractLoggingConfigurator.Level> e : levels.entrySet()) {
            lc.setLoggerLevel(e.getKey(), e.getValue());
        }
        return new ModelAndView(new RedirectView(ControllerConstants.Paths.ADMIN_LOGGING, true));
    }

    @ExceptionHandler(Throwable.class)
    public ModelAndView error(Throwable t) {
        ModelAndView mav = view();
        mav.addObject(ERROR, t.getMessage());
        log.error("Error updating the logging configuration.", t);
        return mav;
    }
}
