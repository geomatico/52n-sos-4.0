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
package org.n52.sos.request.operator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ds.AbstractGetCapabilitiesDAO;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.concrete.ErrorWhileSavingResponseToOutputStreamException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.GetCapabilitiesRequest;
import org.n52.sos.response.GetCapabilitiesResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosGetCapabilitiesOperatorV100 extends AbstractV1RequestOperator<AbstractGetCapabilitiesDAO, GetCapabilitiesRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SosGetCapabilitiesOperatorV100.class);
    // TODO check for SOS 1.0.0
    private static final Set<String> CONFORMANCE_CLASSES = Collections.singleton("http://www.opengis.net/spec/SOS/1.0/conf/core");
    private static final String OPERATION_NAME = SosConstants.Operations.GetCapabilities.name();

    public SosGetCapabilitiesOperatorV100() {
        super(OPERATION_NAME, GetCapabilitiesRequest.class);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }

    @Override
    public ServiceResponse receive(GetCapabilitiesRequest sosRequest) throws OwsExceptionReport {
        /*
         * getting parameter acceptFormats (optional) boolean zipCompr shows
         * whether the response format should be zip (true) or xml (false)
         */
        boolean zipCompr = false;
        List<String> acceptFormats = sosRequest.getAcceptFormats();
        if (acceptFormats != null) {
            zipCompr = checkAcceptFormats(acceptFormats);
        }

        GetCapabilitiesResponse response = getDao().getCapabilities(sosRequest);

        String contentType = SosConstants.CONTENT_TYPE_XML;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // XmlOptions xmlOptions;
        try {
            // TODO check for SOS 1.0.0
            XmlObject encodedObject = CodingHelper.encodeObjectToXml(Sos1Constants.NS_SOS, response);
            encodedObject.save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
            return new ServiceResponse(baos, contentType, zipCompr, true);

        } catch (IOException ioe) {
            throw new ErrorWhileSavingResponseToOutputStreamException(ioe);
        }
    }

    private boolean checkAcceptFormats(List<String> formats) throws OwsExceptionReport {
        boolean zipCompr = false;

        // ints are necessary for getting the priority of the ouptuformats
        int xml = -1;
        int zip = -1;
        for (String format : formats) {
            if (format.equals(SosConstants.CONTENT_TYPE_XML)) {
                xml = formats.indexOf(format);
            } else if (format.equals(SosConstants.CONTENT_TYPE_ZIP)) {
                zip = formats.indexOf(format);
            }
        }
        if (zip == -1 && xml == -1) {
            throw new InvalidParameterValueException().at(SosConstants.GetCapabilitiesParams.AcceptFormats)
                    .withMessage("The parameter '%s' is invalid. The following values are supported: %s, %s",
                                 SosConstants.GetCapabilitiesParams.AcceptFormats.name(), 
                                 SosConstants.CONTENT_TYPE_XML, SosConstants.CONTENT_TYPE_ZIP);
        }

        // if zip is requested testing, whether the priority is bigger than xml
        if (zip != -1 && (zip <= xml || xml == -1)) {
            zipCompr = true;
        }

        return zipCompr;
    }
}
