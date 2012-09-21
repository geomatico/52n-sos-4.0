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
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.n52.sos.ogc.sos.SosConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add generic field for headers<br />
 * v0.1 with enumeration and set of default headers<br />
 * private Map<HeaderCode, String> httpHeaders;<br />
 * could be extended in future versions
 */
public class ServiceResponse {

    /**
     * the logger, used to log exceptions and additonaly information
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceResponse.class);

    /**
     * output stream of document
     */
    private ByteArrayOutputStream byteArrayOutputStream;
    
    /**
     * the HTTP response code as specified in {@link HttpServletResponse}
     */
    private int httpResponseCode;
    
    /**
     * the content type of this response
     */
    private String contentType;
    
    /**
     * the header field and values to be set in the {@link HttpServletResponse}
     */
    private Map<String,String> headerMap;

    /**
     * indicator for compression usage
     */
    private boolean applyZipCompression;

    /**
     * indicator for being xml encoded response
     */
    private boolean xmlResponse;

    /**
     * constructor with content and response code
     * 
     * @param byteArrayOutputStream
     *          Output stream of the SOS response
     * @param contentType
     *          Content type
     * @param applyZipCompression
     *          indicator for compressing the output
     * @param isXmlResponse
     * 			indicator for being XML encoded or not
     * @param httpResponseCode
     * 			the HTTP response code as specified in {@link HttpServletResponse}
     */
    public ServiceResponse(ByteArrayOutputStream byteArrayOutputStream,
    		String contentType,
            boolean applyZipCompression,
            boolean isXmlResponse,
            int httpResponseCode) {
        super();
        this.byteArrayOutputStream = byteArrayOutputStream;
        this.contentType = contentType;
        this.applyZipCompression = applyZipCompression;
        this.xmlResponse = isXmlResponse;
        this.httpResponseCode = httpResponseCode;
    }
    
    /**
     * constructor with content but not specified response code
     * 
     * @param byteArrayOutputStream
     *          Output stream of the SOS response
     * @param contentType
     *          Content type
     * @param applyZipCompression
     *          indicator for compressing the output
     * @param isXmlResponse
     * 			indicator for being XML encoded or not
     */
    public ServiceResponse(ByteArrayOutputStream byteArrayOutputStream,
    		String contentType,
            boolean applyZipCompression,
            boolean isXmlResponse) {
    	this(byteArrayOutputStream, contentType, applyZipCompression, isXmlResponse, -1);
    }
    
    /**
     * constructor without content type but with specified response code
     * 
     * @param contentType
     * 			Content type
     * @param httpResponseCode
     * 			the HTTP response code as specified in {@link HttpServletResponse}
     */
    public ServiceResponse(String contentType, int httpResponseCode) {
    	this(null, contentType, false, false, httpResponseCode);
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
     * @return Returns the number of valid bytes in the content or<br />
     * 		<code>-1</code>, if this response is content less
     * @see {@link #isContentLess()} 
     * @see {@link ByteArrayOutputStream#size()}
     */
    public int getContentLength() {
    	if (byteArrayOutputStream != null) {
    		return byteArrayOutputStream.size();
    	}
    	return -1;
    }
    
    /**
     * @return <b>true</b> if as minimum one header value is contained in the map
     */
    public boolean isSetHeaderMap()
    {
        return headerMap != null && headerMap.size() > 0;
    }
    
    public void addHeaderKvpEntry(String headerIdentifier, String headerValue)
    {
        if (headerMap == null) {
            headerMap = new HashMap<String, String>();
        }
        headerMap.put(headerIdentifier, headerValue);
    }

    public Map<String, String> getHeaderMap()
    {
        return headerMap;
    }

    /**
     * @param outputStream
     * 			The stream the content of this response is written to
     * @see {@link #isContentLess()}
     */
    public void writeToOutputStream(OutputStream outputStream) {
        GZIPOutputStream gzip = null;
        if (byteArrayOutputStream == null ) {
        	LOGGER.error("no response to write to.");
        	return;
        }
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
     * @return <code>true</code>, if the response should compressed using gzip,<br />
     * 			else <code>false</code>
     */
    public boolean getApplyGzipCompression() {
        return applyZipCompression;
    }

    /**
     * @return the content of this response as <code>byte[]</code>, or<br />
     * 			<code>null</code>, if this response is content less.
     * @see {@link #isContentLess()}
     */
    public byte[] getByteArray() {
    	if (byteArrayOutputStream != null) {
    		return byteArrayOutputStream.toByteArray();
    	}
    	return null;
    }

    /**
     * 
     * @return <code>true</code>, if the response is encoded in XML,<br />
     * 			else <code>false</code>
     */
    public boolean isXmlResponse() {
        return xmlResponse;
    }
    
    /**
     * Check, if this response contains content to be written.
     * 
     * @return <code>true</code>, if content is <b>NOT</b> available,<br />
     * 			else<br />
     * 			 <code>false</code>, if content is available
     * @see {@link #writeToOutputStream(OutputStream)}.
     */
    public boolean isContentLess() {
    	return byteArrayOutputStream == null;
    }

	/**
	 * @return if set, should be used as specified in {@link HttpServletResponse}, <br />
	 * 			else <code>-1</code>
	 * @see {@link HttpServletResponse}
	 */
	public int getHttpResponseCode() {
		return httpResponseCode;
	}

}
