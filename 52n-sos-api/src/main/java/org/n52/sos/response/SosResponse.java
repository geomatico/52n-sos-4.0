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

import org.n52.sos.ogc.sos.SosConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the ISosResponse interface.
 * 
 */
public class SosResponse extends AbstractServiceResponse {

    /** the logger, used to log exceptions and additonaly information */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosResponse.class);

    /** output stream of document */
    private ByteArrayOutputStream byteArrayOutputStream;

    private String contentType;

    /** indicator for compression usage */
    private boolean applyZipCompression;

    /**
     * SOS version
     */
    private String version;
    
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
    public SosResponse(ByteArrayOutputStream byteArrayOutputStream, String contentType, boolean applyZipCompression,
            String version, boolean isXmlResponse) {
        super();
        this.byteArrayOutputStream = byteArrayOutputStream;
        this.contentType = contentType;
        this.applyZipCompression = applyZipCompression;
        this.version = version;
        this.xmlResponse = isXmlResponse;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.resp.ISosResponse#getContentType()
     */
    @Override
    public String getContentType() {
        if (applyZipCompression) {
            return SosConstants.CONTENT_TYPE_ZIP;
        }
        return contentType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.resp.ISosResponse#getContentLength()
     */
    @Override
    public int getContentLength() throws IOException {
        return byteArrayOutputStream.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.resp.ISosResponse#getWriteTooutputStream(java.io.OutputStream
     * )
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.resp.ISosResponse#getApplyGzipCompression()
     */
    @Override
    public boolean getApplyGzipCompression() {
        return applyZipCompression;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.resp.ISosResponse#getVersion()
     */
    @Override
    public String getVersion() {
        return version;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.resp.ISosResponse#getByteArray()
     */
    @Override
    public byte[] getByteArray() {
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public boolean isXmlResponse() {
        return xmlResponse;
    }

}
