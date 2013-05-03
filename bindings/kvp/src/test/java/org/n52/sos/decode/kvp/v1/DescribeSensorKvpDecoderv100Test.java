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
package org.n52.sos.decode.kvp.v1;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.n52.sos.decode.kvp.v2.DeleteSensorKvpDecoderv20;
import org.n52.sos.ogc.ows.OWSConstants.RequestParams;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.DescribeSensorRequest;
import org.n52.sos.request.GetCapabilitiesRequest;

/**
 * @author Shane StClair <shane@axiomalaska.com>
 */
public class DescribeSensorKvpDecoderv100Test extends DeleteSensorKvpDecoderv20 {
    private static final String PROCEDURE = "testprocedure";	
    private static final String OUTPUT_FORMAT = "testOutputFormat";    
    private static final String ADDITIONAL_PARAMETER = "additionalParameter";    
    private static final String EMPTY_STRING = "";

    private DescribeSensorKvpDecoderv100 decoder;

    @Before
    public void setUp() {
        this.decoder = new DescribeSensorKvpDecoderv100();
    }

    @Test
    public void basic() throws OwsExceptionReport {
        DescribeSensorRequest req = decoder.decode(createMap(SosConstants.SOS, Sos1Constants.SERVICEVERSION,
        		PROCEDURE, OUTPUT_FORMAT));
        assertThat(req, is(notNullValue()));
        assertThat(req.getOperationName(), is(SosConstants.Operations.DescribeSensor.name()));
        assertThat(req.getService(), is(SosConstants.SOS));
        assertThat(req.getVersion(), is(Sos1Constants.SERVICEVERSION));
        assertThat(req.getProcedure(), is(PROCEDURE));
    }

    @Test(expected = OwsExceptionReport.class)
    public void missingService() throws OwsExceptionReport {    	
        decoder.decode(createMap(EMPTY_STRING, Sos1Constants.SERVICEVERSION, PROCEDURE, OUTPUT_FORMAT));
    }

    @Test(expected = OwsExceptionReport.class)
    public void missingVersion() throws OwsExceptionReport {    	
        decoder.decode(createMap(SosConstants.SOS, EMPTY_STRING, PROCEDURE, OUTPUT_FORMAT));
    }

    @Test(expected = OwsExceptionReport.class)
    public void missingProcedure() throws OwsExceptionReport {    	
        decoder.decode(createMap(SosConstants.SOS, Sos1Constants.SERVICEVERSION, EMPTY_STRING, OUTPUT_FORMAT));
    }

    @Test(expected = OwsExceptionReport.class)
    public void missingOutputFormat() throws OwsExceptionReport {    	
        decoder.decode(createMap(SosConstants.SOS, Sos1Constants.SERVICEVERSION, PROCEDURE, EMPTY_STRING));
    }

    @Test(expected = OwsExceptionReport.class)
    public void additionalParameter() throws OwsExceptionReport {
    	final Map<String, String> map = createMap(SosConstants.SOS, Sos1Constants.SERVICEVERSION, PROCEDURE, OUTPUT_FORMAT);
    	map.put(ADDITIONAL_PARAMETER,ADDITIONAL_PARAMETER);
        decoder.decode(map);
    }

    @Test(expected = OwsExceptionReport.class)
    public void emptyParam() throws OwsExceptionReport {
    	final Map<String, String> map = createMap(SosConstants.SOS, Sos1Constants.SERVICEVERSION, PROCEDURE, OUTPUT_FORMAT);
    	map.put(SosConstants.GetCapabilitiesParams.AcceptVersions.name(), EMPTY_STRING);
        decoder.decode(map);
    }    

    private Map<String, String> createMap(String service, String version, String procedure, String outputFormat) {
        Map<String, String> map = new HashMap<String, String>(1);
        map.put(RequestParams.service.name(),service);
        map.put(RequestParams.request.name(),SosConstants.Operations.DescribeSensor.name());        
        map.put(RequestParams.version.name(),version);        
        map.put(SosConstants.DescribeSensorParams.procedure.name(), procedure);
        map.put(Sos1Constants.DescribeSensorParams.outputFormat.name(), outputFormat);        
        return map;
    }    
}
