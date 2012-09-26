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
package org.n52.sos.ogc.ows;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.n52.sos.exception.IExceptionCode;
import org.n52.sos.ogc.ows.OWSConstants.ExceptionLevel;
import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;

/**
 * Implementation of the ows service exception. The exception codes are defined
 * according the ows common spec. version 1.1.0
 * 
 */
public class OwsExceptionReport extends Exception {

    private static final long serialVersionUID = 9069373009339881302L;

    /** logger */
    private static final Logger LOGGER = Logger.getLogger(OwsExceptionReport.class.getName());

    /** Exception types */
    private List<OwsException> owsExceptions = new ArrayList<OwsException>();

    /** exception level */
    private ExceptionLevel excLevel = null;
    
    private final String namespace = OWSConstants.NS_OWS;

    /**
     * SOS version
     */
    private String version;

    /**
     * standard constructor without parameters, sets the ExceptionLevel on
     * PlainExceptions
     * 
     */
    public OwsExceptionReport() {
        this.excLevel = ExceptionLevel.DetailedExceptions;
    }

//    /**
//     * constructor with message and cause as parameters
//     * 
//     * @param message
//     *            String containing the message of this exception
//     * @param cause
//     *            Throwable cause of this exception
//     */
//    public OwsExceptionReport(String message, Throwable cause) {
//        super(message, cause);
//        
//        this.excLevel = ExceptionLevel.DetailedExceptions;
//    }
//
//    /**
//     * constructor with cause as parameter
//     * 
//     * @param cause
//     *            Throwable cause of this exception
//     */
//    public OwsExceptionReport(Throwable cause) {
//        super(cause);
//        this.excLevel = ExceptionLevel.DetailedExceptions;
//    }

    /**
     * constructor with exceptionLevel as parameter
     * 
     * @param excLevelIn
     */
    public OwsExceptionReport(ExceptionLevel excLevelIn) {
        this.excLevel = excLevelIn;
    }

    /**
     * adds a coded Exception with ExceptionCode,locator and a single String
     * message to this exception
     * 
     * @param code
     *            Exception code of the exception to add
     * @param locator
     *            String locator of the exception to add
     * @param message
     *            String message of the exception to add
     */
    public void addCodedException(IExceptionCode code, String locator, String message) {
        String[] messages = { message };
        owsExceptions.add(new OwsException(code, locator, messages));
    }

    /**
     * adds a coded exception to this exception with code, locator and messages
     * as parameters
     * 
     * @param code
     *            ExceptionCode of the added exception
     * @param locator
     *            String locator of this exception
     * @param messages
     *            String[] messages of this exception
     */
    public void addCodedException(IExceptionCode code, String locator, String[] messages) {
        owsExceptions.add(new OwsException(code, locator, messages));
    }

    /**
     * adds a coded Exception to this service exception with code, locator and
     * the exception itself as parameters
     * 
     * @param code
     *            ExceptionCode of the added exception
     * @param locator
     *            String locator of the added exception
     * @param e
     *            Exception which should be added
     */
    public void addCodedException(IExceptionCode code, String locator, String[] messages, Exception exception) {
        owsExceptions.add(new OwsException(code, locator, messages, exception));
    }

    public void addCodedException(IExceptionCode code, String locator, String message,
            Exception exception) {
        String[] messages = { message };
        owsExceptions.add(new OwsException(code, locator, messages, exception));
    }

    /**
     * adds a ServiceException to this exception
     * 
     * @param seIn
     *            ServiceException which should be added
     */
    public void addOwsExceptionReport(OwsExceptionReport owsExceptionReport) {
        this.owsExceptions.addAll(owsExceptionReport.getExceptions());
    }

    /**
     * 
     * @return Returns the ExceptionTypes of this exception
     */
    public List<OwsException> getExceptions() {
        return owsExceptions;
    }

    public ExceptionLevel getExcLevel() {
        return excLevel;
    }

    public void setExcLevel(ExceptionLevel excLevel) {
        this.excLevel = excLevel;
    }

    /**
     * Set SOS version
     * 
     * @param version
     *            the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get SOS version
     * 
     * @return SOS version
     */
    public String getVersion() {
        return this.version;
    }

    public String getNamespace() {
        return namespace;
    }

}
