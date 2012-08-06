/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.opengis.ows.x11.ExceptionReportDocument;

import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.response.ServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author c_hollmann
 * 
 */
public class SosRequestToResponseHelper {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosRequestToResponseHelper.class);

    /**
     * Method to create an exception response document for an OwsExceptionReport
     * 
     * @param exceptionReport
     *            Exception thrown by the SOS
     * @return Exception report document response
     */
    public static ServiceResponse createExceptionResponse(OwsExceptionReport exceptionReport) {
        ExceptionReportDocument erd = exceptionReport.getDocument();
        N52XmlHelper.setSchemaLocationToDocument(erd, N52XmlHelper.getSchemaLocationForOWS110());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            erd.save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
        } catch (IOException ioe) {
            LOGGER.debug("Error while creating Exception response!", ioe);
        }
        return new ServiceResponse(baos, SosConstants.CONTENT_TYPE_XML, false, true);
    }
}
