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
package org.n52.sos.service.it;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import net.opengis.gml.CodeType;
import net.opengis.gml.TimeInstantType;
import net.opengis.ogc.BinaryTemporalOpType;
import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.om.x10.ObservationCollectionType;
import net.opengis.om.x10.ObservationPropertyType;
import net.opengis.om.x10.ObservationType;
import net.opengis.sos.x10.CapabilitiesDocument;
import net.opengis.sos.x10.CapabilitiesDocument.Capabilities;
import net.opengis.sos.x10.GetCapabilitiesDocument;
import net.opengis.sos.x10.GetCapabilitiesDocument.GetCapabilities;
import net.opengis.sos.x10.GetObservationDocument;
import net.opengis.sos.x10.GetObservationDocument.GetObservation;
import net.opengis.sos.x10.GetResultDocument;
import net.opengis.sos.x10.GetResultDocument.GetResult;
import net.opengis.sos.x10.GetResultDocument.GetResult.EventTime;
import net.opengis.sos.x10.GetResultResponseDocument;
import net.opengis.sos.x10.GetResultResponseDocument.GetResultResponse.Result;
import net.opengis.sos.x10.ResponseModeType;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.filter.FilterConstants;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.xml.sax.SAXException;

public class SosV1IT extends SosITBase {
    
    /*
     * TODO Accommodate tests to this SOS 
     */
    
    /**
     * Verify v1 test client exists
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Ignore
    @Test
    public void verifyTestClientSosV1() throws IOException, URISyntaxException {
        verifyPathExists("testClient-SOSv1.html");
    }

    /**
     * Send a V1 GetCapabilities request via GET and verify the response
     * 
     * @throws IOException
     * @throws URISyntaxException
     * @throws IllegalStateException
     * @throws XmlException
     * @throws SAXException
     */
    @Ignore
    @Test
    public void getCapabilitiesV1Get() throws IOException,
            URISyntaxException,
            IllegalStateException,
            XmlException,
            SAXException {
        List<NameValuePair> qparams = getBaseQueryParams();
        qparams.add(new BasicNameValuePair("request", SosConstants.Operations.GetCapabilities.name()));
        qparams.add(new BasicNameValuePair("acceptVersions", Sos1Constants.SERVICEVERSION));
        HttpGet request = new HttpGet(getSOSURI(URLEncodedUtils.format(qparams, "UTF-8")));
        verifyCapabilitiesV1(request);
    }

    /**
     * Send a V1 GetCapabilities request via POST and verify the response
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws IllegalStateException
     * @throws XmlException
     * @throws SAXException
     */
    @Ignore
    @Test
    public void getCapabilitiesV1Post() throws URISyntaxException,
            IOException,
            IllegalStateException,
            XmlException,
            SAXException {
        GetCapabilitiesDocument xb_getCapDoc = GetCapabilitiesDocument.Factory.newInstance();
        GetCapabilities xb_getCap = xb_getCapDoc.addNewGetCapabilities();
        xb_getCap.setService(SosConstants.SOS);
        xb_getCap.addNewAcceptVersions().addVersion(Sos1Constants.SERVICEVERSION);
        HttpPost httppost = new HttpPost(getSOSURI());
        byte[] xmlBytes = IOUtils.toByteArray(xb_getCapDoc.newInputStream());
        httppost.setEntity(new ByteArrayEntity(xmlBytes));
        verifyCapabilitiesV1(httppost);
    }

    /**
     * Shared verification method for V1 Capabilities requests/responses
     * 
     * @param request
     *        Http request to execute
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws XmlException
     * @throws SAXException
     */
    private void verifyCapabilitiesV1(HttpUriRequest request) throws ClientProtocolException,
            IOException,
            IllegalStateException,
            XmlException,
            SAXException {
        HttpResponse response = client.execute(request);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertNotNull(response.getEntity());
        CapabilitiesDocument xb_capDoc = CapabilitiesDocument.Factory.parse(response.getEntity().getContent());
        assertNotNull(xb_capDoc);

        // validate response using xmlbeans
        validateXmlBean(xb_capDoc);

        // validate response using javax.xml.validation schema validation
        getXmlValidator().validate(new StreamSource(xb_capDoc.newInputStream()));

        Capabilities xb_cap = xb_capDoc.getCapabilities();
        assertNotNull(xb_cap);

        // version
        assertEquals("1.0.0", xb_cap.getVersion());
    }

    /**
     * Send a test V1 GetObservation request with a resultTemplate responseMode. Includes a TM_During temporal
     * filter. V1 GetResult is tested using the returned observation template id.
     * 
     * @throws IOException
     * @throws URISyntaxException
     * @throws IllegalStateException
     * @throws XmlException
     * @throws SAXException
     * @throws OwsExceptionReport
     */
    @Ignore
    @Test
    public void getObservationResultTemplateV1() throws IOException,
            URISyntaxException,
            IllegalStateException,
            XmlException,
            SAXException,
                                                        OwsExceptionReport {
        // create request document
        GetObservationDocument xb_getObsDoc = GetObservationDocument.Factory.newInstance();
        GetObservation xb_getObs = xb_getObsDoc.addNewGetObservation();
        xb_getObs.setService(SosConstants.SOS);
        xb_getObs.setVersion(Sos1Constants.SERVICEVERSION);
        xb_getObs.setSrsName("urn:ogc:def:crs:EPSG::4326");
        xb_getObs.setOffering("GAUGE_HEIGHT");
        xb_getObs.setObservedPropertyArray(new String[] {"urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel"});
        xb_getObs.setResponseFormat(OMConstants.CONTENT_TYPE_OM);
        xb_getObs.setResponseMode(ResponseModeType.RESULT_TEMPLATE);
        TimePeriod tp = new TimePeriod();
        tp.setStart(new DateTime(2008, 3, 27, 0, 0, 0, 0));
        tp.setEnd(new DateTime(2008, 4, 3, 0, 0, 0, 0));
        TemporalFilter tf = new TemporalFilter(FilterConstants.TimeOperator.TM_During, tp, null);
        ITRequestEncoder.encodeTemporalFilter(tf, xb_getObs.addNewEventTime());

        // get response
        HttpPost httppost = new HttpPost(getSOSURI());
        byte[] xmlBytes = IOUtils.toByteArray(xb_getObsDoc.newInputStream());
        httppost.setEntity(new ByteArrayEntity(xmlBytes));
        HttpResponse response = client.execute(httppost);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertNotNull(response.getEntity());
        ObservationCollectionDocument xb_obsColDoc = ObservationCollectionDocument.Factory.parse(response.getEntity().getContent());
        assertNotNull(xb_obsColDoc);

        // validate response using javax.xml.validation schema validation
        getXmlValidator().validate(new StreamSource(xb_obsColDoc.newInputStream()));

        ObservationCollectionType xb_obsCol = xb_obsColDoc.getObservationCollection();
        assertNotNull(xb_obsCol);

        // run GetResult tests for latest observations
        ObservationPropertyType[] xb_observations = xb_obsCol.getMemberArray();
        for (int i = 0; i < xb_observations.length; i++) {
            ObservationType xb_obs = xb_observations[i].getObservation();
            CodeType[] xb_names = xb_obs.getNameArray();
            for (int j = 0; j < xb_names.length; j++) {
                String name = xb_names[j].getStringValue();
                if (name.startsWith(SosConstants.OBS_TEMP_ID_PREFIX)) {
                    getLatestResultV1(name);
                }
            }
        }
    }

    /**
     * Sends a V1 GetResult POST request for the latest observation matching the provided observation template
     * id and verifies the response.
     * 
     * @param templateId
     *        Observation template id for the request

     *
     * @throws OwsExceptionReport
     * @throws URISyntaxException
     * @throws IOException
     * @throws IllegalStateException
     * @throws XmlException
     */
    private void getLatestResultV1(String templateId) throws OwsExceptionReport,
                                                             URISyntaxException,
            IOException,
            IllegalStateException,
            XmlException {
        // make a GetResult request with the returned template id for the latest observation
        GetResultDocument xb_getResultDoc = GetResultDocument.Factory.newInstance();
        GetResult xb_getResult = xb_getResultDoc.addNewGetResult();
        xb_getResult.setService(SosConstants.SOS);
        xb_getResult.setVersion(Sos1Constants.SERVICEVERSION);

        // set template id
        xb_getResult.setObservationTemplateId(templateId);

        // add latest time filter
        EventTime xb_eventTime = xb_getResult.addNewEventTime();
        QName qnTmEquals = new QName(OGCConstants.NS_OGC, "TM_Equals", OGCConstants.NS_OGC_PREFIX);
        BinaryTemporalOpType xb_tmEquals = (BinaryTemporalOpType) xb_eventTime.addNewTemporalOps().substitute(qnTmEquals,
                                                                                                              BinaryTemporalOpType.type);
        xb_tmEquals.addNewPropertyName().newCursor().setTextValue("om:samplingTime");
        QName qnTimeInstant = new QName(GMLConstants.NS_GML, GMLConstants.EN_TIME_INSTANT, GMLConstants.NS_GML_PREFIX);
        TimeInstantType xb_timeInstant = (TimeInstantType) xb_tmEquals.addNewTimeObject().substitute(qnTimeInstant,
                                                                                                     TimeInstantType.type);
        xb_timeInstant.addNewTimePosition().setStringValue(SosConstants.FirstLatest.latest.name());

        // execute request
        HttpPost httppost = new HttpPost(getSOSURI());
        byte[] xmlBytes = IOUtils.toByteArray(xb_getResultDoc.newInputStream());
        httppost.setEntity(new ByteArrayEntity(xmlBytes));
        HttpResponse response = client.execute(httppost);

        // basic response validation
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertNotNull(response.getEntity());
        GetResultResponseDocument xb_getResultResposeDoc = GetResultResponseDocument.Factory.parse(response.getEntity().getContent());
        assertNotNull(xb_getResultResposeDoc);

        // validate result
        Result xb_result = xb_getResultResposeDoc.getGetResultResponse().getResult();
        assertEquals(templateId, xb_result.getRS());
        assertNotNull(xb_result.getStringValue());
        assertTrue(xb_result.getStringValue().length() > 0);

        // TODO: If we're coupling tests tightly to test data, we should validate the results
        // to expected values
    }
}