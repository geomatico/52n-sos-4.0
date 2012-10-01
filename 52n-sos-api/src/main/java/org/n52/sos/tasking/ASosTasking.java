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
package org.n52.sos.tasking;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ASosTasking extends TimerTask {
    
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ASosTasking.class);
    
    private final int DEFAULT_EXECUTION_INTERVALL = 15;
    
    private long executionIntervall = DEFAULT_EXECUTION_INTERVALL;
    
    private String name;

    public ASosTasking(String fileName) throws TaskingException {
        loadConfiguration(fileName);
    }

    private void loadConfiguration(String fileName) throws TaskingException {
//        try {
//            XmlObject configurationFile = XmlHelper.loadXmlDocumentFromFile(new File(fileName));
            setExecutionIntervall(5);
            setName("I_AM_A_TASK");
//        } catch (OwsExceptionReport owse) {
//            throw new TaskingException("Error while loading tasking configuration", owse);
//        }
    }
    
    private void setExecutionIntervall(long executionIntervall) {
        this.executionIntervall = executionIntervall;
    }
    
    private void setName(String name) {
        this.name = name;
    }

    public long getExecutionIntervall() {
        return executionIntervall;
    }
    
    public String getName() {
        return name;
    }

}
