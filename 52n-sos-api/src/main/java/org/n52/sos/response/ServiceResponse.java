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

package org.n52.sos.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.transform.TransformerException;

import org.n52.sos.ogc.sos.SosConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 */
public class ServiceResponse {

    /** the logger, used to log exceptions and additonaly information */
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceResponse.class);

    /** output stream of document */
    private ByteArrayOutputStream byteArrayOutputStream;

    private String contentType;

    /** indicator for compression usage */
    private boolean applyZipCompression;

    private boolean xmlResponse;

    /**
     * constructor
     * 
     * @param byteArrayOutputStream
     *            Output stream of the SOS response
     * @param contentType
     *            Content type
     * @param applyZipCompression
     *            indicator for zipping the output
     * @param version
     *            SOS version
     */
    public ServiceResponse(ByteArrayOutputStream byteArrayOutputStream, String contentType,
            boolean applyZipCompression, boolean isXmlResponse) {
        super();
        this.byteArrayOutputStream = byteArrayOutputStream;
        this.contentType = contentType;
        this.applyZipCompression = applyZipCompression;
        this.xmlResponse = isXmlResponse;
    }

    /**
     * @return Returns the content type of this response
     */
    public String getContentType() {
        if (applyZipCompression) {
            return SosConstants.CONTENT_TYPE_ZIP;
        }
        return contentType;
    }

    /**
     * @return Returns the the length of the content in bytes
     * @throws IOException
     *             if getting the content length failed
     * @throws TransformerException
     *             if getting the content length failed
     */
    public int getContentLength() throws IOException {
        return byteArrayOutputStream.size();
    }

    /**
     * @return Returns the response as byte[]
     * @throws IOException
     *             if getting the response as byte[] failed
     * @throws TransformerException
     *             if getting the response as byte[] failed
     */
    public void writeToOutputStream(OutputStream outputStream) throws IOException {
        GZIPOutputStream gzip = null;
        try {
            if (applyZipCompression) {

                gzip = new GZIPOutputStream(outputStream);
                byteArrayOutputStream.writeTo(gzip);
                gzip.flush();
                gzip.finish();

            } else {
                byteArrayOutputStream.writeTo(outputStream);
                byteArrayOutputStream.flush();
            }
        } catch (IOException ioe) {
            LOGGER.error("doResponse", ioe);
        } finally {
            try {
                if (gzip != null) {
                    gzip.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
            } catch (IOException ioe) {
                LOGGER.error("doSoapResponse, close streams", ioe);
            }
        }
    }

    /**
     * @return Returns true if the response should compressed using gzip,
     *         otherwise false
     */
    public boolean getApplyGzipCompression() {
        return applyZipCompression;
    }

    public byte[] getByteArray() {
        return byteArrayOutputStream.toByteArray();
    }

    public boolean isXmlResponse() {
        return xmlResponse;
    }

}
