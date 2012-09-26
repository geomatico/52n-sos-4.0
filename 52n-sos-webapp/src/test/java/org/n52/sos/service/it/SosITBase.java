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
package org.n52.sos.service.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.sos.ogc.sos.SosConstants;
import org.xml.sax.SAXException;

/**
 * Integration tests for the 52n-sos
 *
 * Normally these tests are run against the local development code as part of the Maven
 * lifecycle via the maven-failsafe-plugin and maven-jetty-plugin during the verify phase
 * in the test profile (see pom).
 *
 * In addition, the tests can be run against an external SOS server by specifying host
 * and port system properties (e.g. in Eclipse, open the debug configuration and add
 * -Dhost=your.sos.server -Dport=80 to the VM arguments). Note that the external server
 * MUST CONTAIN THE NORMAL TEST DATA (see db/test.sql).
 *
 * @author Shane StClair
 *
 */

public class SosITBase {
    private String defaultHost = "localhost";
    //host to run tests against
    private String host = assignHost();

    private int defaultPort = 9090;
    //port to run tests against
    private int port = assignPort();

    //basepath context to run tests against
    private String basepath = assignBasepath();

    //client for http requests to test server
    protected HttpClient client = new DefaultHttpClient();

    //javax.xml.validation.Validator for schema validating xml documents
    private Validator xmlValidator;

    /**
     * Assign test host (use "host" system property or default to localhost)
     *
     * @return test host
     */
    private String assignHost(){
        if( System.getProperty("host") != null ){
            return System.getProperty("host").trim();
        }
        return defaultHost;
    }

    /**
     * Assign test port (use "jetty.port" or "port" system properties or default to 8080)
     *
     * @return test port
     */
    private int assignPort(){
        if( System.getProperty("jetty.port") != null ){
            return Integer.valueOf( System.getProperty("jetty.port").trim() );
        } else if ( System.getProperty("port") != null ){
            return Integer.valueOf( System.getProperty("port").trim() );
        }
        return defaultPort;
    }

    /**
     * Assign test base path context (use "basepath" system property or default to null)
     *
     * @return test base path
     */
    private String assignBasepath(){
        if( System.getProperty("basepath") != null ){
            String basepathStr = System.getProperty("basepath").trim();
            //remove leading and trailing slashes from basepath
            basepathStr.replaceAll("^[/]*","");
            basepathStr.replaceAll("[/]*$","");
            return basepathStr;
        }
        return null;
    }

    /**
     * Combine path argument with basepath (just returns path if basepath is null)
     *
     * @param path
     * @return Combined path
     */
    private String getPath(String path){
        StringBuilder sb = new StringBuilder();
        if( basepath != null ){
            sb.append( basepath );
            sb.append( "/");
        }
        sb.append( path );
        return sb.toString();
    }

    /**
     * Get URI for the relative sos path and query using test host, port, and basepath
     *
     * @param path The relative test endpoint
     * @param query Query parameters to add to the request
     * @return Constructed URI
     * @throws URISyntaxException
     */
    protected URI getURI( String path, String query ) throws URISyntaxException{
        return URIUtils.createURI("http", host, port, getPath(path), query, null);
    }

    /**
     * Get URI for the SOS endpoint with no query parameters
     *
     * @return SOS URI
     * @throws URISyntaxException
     */
    protected URI getSOSURI() throws URISyntaxException{
        return getURI("sos", null);
    }

    /**
     * Get URI for the SOS endpoint with query parameters
     *
     * @return SOS URI
     * @throws URISyntaxException
     */
    protected URI getSOSURI( String query ) throws URISyntaxException{
        return getURI("sos", query);
    }

    /**
     * Simple test to assert that a path exists
     *
     * @param path Path to test
     * @throws IOException
     * @throws URISyntaxException
     */
    protected void verifyPathExists(String path) throws IOException, URISyntaxException{
        HttpGet request = new HttpGet( getURI(path, null) );
        HttpResponse response = client.execute( request );
        assertEquals( HttpStatus.SC_OK, response.getStatusLine().getStatusCode() );
        HttpEntity entity = response.getEntity();
        assertNotNull( response.getEntity() );
        assertTrue( EntityUtils.toString( entity ).length() > 0 );
    }

    /**
     * Get a query param List<NameValuePair> containing the service=SOS KVP
     *
     * @return Base query params
     */
    protected List<NameValuePair> getBaseQueryParams(){
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("service", SosConstants.SOS ));
        return qparams;
    }

    /**
     * Validates and XmlObject using xmlbeans validation
     *
     * Note: this can't be used when an XmlObject uses schemas generated in two different jars,
     * like most of the 52n-sos-xml classes. See https://issues.apache.org/jira/browse/XMLBEANS-299
     *
     * @param xb_xml XmlObject to validate
     */
    protected void validateXmlBean( XmlObject xb_xml ){
        XmlOptions validateOptions = new XmlOptions();
        List<XmlError> errorList = new ArrayList<XmlError>();
        validateOptions.setErrorListener(errorList);
        boolean valid = xb_xml.validate( validateOptions );
        if( !valid ){
            System.out.println( xb_xml.getDomNode().getNodeName() + " is invalid.");
            for( XmlError error : errorList ){
                System.out.println( error.getErrorCode() + " - " + error.getMessage() + " at: " );
                System.out.println( error.getCursorLocation().xmlText() );
            }
        }
        assertTrue( valid );
    }

    /**
     * Returns a javax.xml.validation Validator that validates xml documents against their
     * internally defined schemas. Constructs the validator if necessary.
     *
     * @return xmlValidator
     * @throws SAXException
     */
    protected Validator getXmlValidator() throws SAXException{
        if( xmlValidator == null ){
            xmlValidator = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI )
                    .newSchema().newValidator();
        }
        return xmlValidator;
    }
}