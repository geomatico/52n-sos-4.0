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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
@RequestMapping(value = ControllerConstants.Paths.ADMIN_LOGGING)
public class AdminLoggingController extends AbstractController {

    private static final int LOG_MESSAGES = 15;
    private static final String LOG_MESSAGES_MODEL_ATTRIBUTE = "logMessages";
    private static final String IS_CONSOLE_ENABLED_MODEL_ATTRIBUTE = "isConsoleEnabled";
    private static final String IS_FILE_ENABLED_MODEL_ATTRIBUTE = "isFileEnabled";
    private static final String ROOT_LOG_LEVEL_MODEL_ATTRIBUTE = "rootLogLevel";
    private static final String DAYS_TO_KEEP_MDOEL_ATTRIBUTE = "daysToKeep";
    private static final String LOGGER_LEVELS_MODEL_ATTRIBUTE = "loggerLevels";
    private static final String MAX_FILE_SIZE_MODEL_ATTRIBUTE = "maxFileSize";
    private static final String ERROR_MODEL_ATTRIBUTE = "error";

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView view() {
        AbstractLoggingConfigurator lc = AbstractLoggingConfigurator.getInstance();
        Map<String, Object> config = new HashMap<String, Object>(5);
        config.put(IS_FILE_ENABLED_MODEL_ATTRIBUTE, lc.isEnabled(AbstractLoggingConfigurator.Appender.FILE));
        config.put(IS_CONSOLE_ENABLED_MODEL_ATTRIBUTE, lc.isEnabled(AbstractLoggingConfigurator.Appender.CONSOLE));
        config.put(ROOT_LOG_LEVEL_MODEL_ATTRIBUTE, lc.getRootLogLevel());
        config.put(DAYS_TO_KEEP_MDOEL_ATTRIBUTE, lc.getMaxHistory());
        config.put(LOGGER_LEVELS_MODEL_ATTRIBUTE, lc.getLoggerLevels());
        config.put(LOG_MESSAGES_MODEL_ATTRIBUTE, lc.getLastLogEntries(LOG_MESSAGES));
        config.put(MAX_FILE_SIZE_MODEL_ATTRIBUTE, lc.getMaxFileSize());
        return new ModelAndView(ControllerConstants.Views.ADMIN_LOGGING, config);
    }
    

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView save(HttpServletRequest req) {
        
        
        Set<String> parameters = new HashSet<String>(
                Collections.list((Enumeration<String>)req.getParameterNames()));
                
        int daysToKeep = Integer.parseInt(req.getParameter(DAYS_TO_KEEP_MDOEL_ATTRIBUTE));
        parameters.remove(DAYS_TO_KEEP_MDOEL_ATTRIBUTE);
        boolean fileEnabled = parseBoolean(req.getParameter(IS_FILE_ENABLED_MODEL_ATTRIBUTE));
        parameters.remove(IS_FILE_ENABLED_MODEL_ATTRIBUTE);
        boolean consoleEnabled = parseBoolean(req.getParameter(IS_CONSOLE_ENABLED_MODEL_ATTRIBUTE));
        parameters.remove(IS_CONSOLE_ENABLED_MODEL_ATTRIBUTE);
        AbstractLoggingConfigurator.Level rootLevel = AbstractLoggingConfigurator.Level
                .valueOf(req.getParameter(ROOT_LOG_LEVEL_MODEL_ATTRIBUTE));
        parameters.remove(ROOT_LOG_LEVEL_MODEL_ATTRIBUTE);
        String maxFileSize = req.getParameter(MAX_FILE_SIZE_MODEL_ATTRIBUTE);
        parameters.remove(MAX_FILE_SIZE_MODEL_ATTRIBUTE);
        
        Map<String, AbstractLoggingConfigurator.Level> levels 
                = new HashMap<String, AbstractLoggingConfigurator.Level>(parameters.size());
        
       
        for (String logger : parameters) {
            levels.put(logger, AbstractLoggingConfigurator.Level.valueOf(req.getParameter(logger)));
        }
       
        AbstractLoggingConfigurator lc = AbstractLoggingConfigurator.getInstance();
        lc.setMaxHistory(daysToKeep);
        lc.enableAppender(AbstractLoggingConfigurator.Appender.FILE, fileEnabled);
        lc.enableAppender(AbstractLoggingConfigurator.Appender.CONSOLE, consoleEnabled);
        lc.setRootLogLevel(rootLevel);
        lc.setLoggerLevel(levels);
        lc.setMaxFileSize(maxFileSize);
        
        return new ModelAndView(new RedirectView(ControllerConstants.Paths.ADMIN_LOGGING, true));
    }

    @ExceptionHandler(Throwable.class)
    public ModelAndView error(Throwable t) {
        ModelAndView mav = view();
        mav.addObject(ERROR_MODEL_ATTRIBUTE, t.getMessage());
        log.error("Error updating the logging configuration.", t);
        return mav;
    }
}
