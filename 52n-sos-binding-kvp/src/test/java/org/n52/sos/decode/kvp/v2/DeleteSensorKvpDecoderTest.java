package org.n52.sos.decode.kvp.v2;

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Test;
import org.n52.sos.ogc.ows.OWSConstants.RequestParams;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.Sos2Constants.DeleteSensorParams;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.DeleteSensorRequest;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class DeleteSensorKvpDecoderTest extends DeleteSensorKvpDecoder {
    private static final String PROCEDURE = "testprocedure";
    private static final String SERVICE = SosConstants.SOS;
    private static final String VERSION = Sos2Constants.SERVICEVERSION;
    private static final String ADDITIONAL_PARAMETER = "additionalParameter";

    @Test
    public void correctMap() throws OwsExceptionReport {
        DeleteSensorKvpDecoder decoder = new DeleteSensorKvpDecoder();
        DeleteSensorRequest req = decoder.decode(createMap(SERVICE, VERSION, PROCEDURE));
        assertThat(req, is(notNullValue()));
        assertThat(req.getOperationName(), is(Sos2Constants.Operations.DeleteSensor.name()));
        assertThat(req.getProcedureIdentifier(), is(PROCEDURE));
        assertThat(req.getService(), is(SERVICE));
        assertThat(req.getVersion(), is(VERSION));
    }

    @Test(expected = OwsExceptionReport.class)
    public void additionalParameter() throws OwsExceptionReport {
        DeleteSensorKvpDecoder decoder = new DeleteSensorKvpDecoder();
        final Map<String, String> map = createMap(SERVICE, VERSION, PROCEDURE);
        map.put(ADDITIONAL_PARAMETER, ADDITIONAL_PARAMETER);
        decoder.decode(map);
    }

    @Test(expected = OwsExceptionReport.class)
    public void missingService() throws OwsExceptionReport {
        DeleteSensorKvpDecoder decoder = new DeleteSensorKvpDecoder();
        decoder.decode(createMap(null, VERSION, PROCEDURE));
    }

    @Test(expected = OwsExceptionReport.class)
    public void missingVersion() throws OwsExceptionReport {
        DeleteSensorKvpDecoder decoder = new DeleteSensorKvpDecoder();
        decoder.decode(createMap(SERVICE, null, PROCEDURE));
    }

    @Test(expected = OwsExceptionReport.class)
    public void missingProcedure() throws OwsExceptionReport {
        DeleteSensorKvpDecoder decoder = new DeleteSensorKvpDecoder();
        decoder.decode(createMap(SERVICE, VERSION, null));
    }

    @Test(expected = OwsExceptionReport.class)
    public void emptyService() throws OwsExceptionReport {
        DeleteSensorKvpDecoder decoder = new DeleteSensorKvpDecoder();
        decoder.decode(createMap("", VERSION, PROCEDURE));
    }

    @Test(expected = OwsExceptionReport.class)
    public void emptyVersion() throws OwsExceptionReport {
        DeleteSensorKvpDecoder decoder = new DeleteSensorKvpDecoder();
        decoder.decode(createMap(SERVICE, "", PROCEDURE));
    }

    @Test(expected = OwsExceptionReport.class)
    public void emptyProcedure() throws OwsExceptionReport {
        DeleteSensorKvpDecoder decoder = new DeleteSensorKvpDecoder();
        decoder.decode(createMap(SERVICE, VERSION, ""));
    }

    private Map<String, String> createMap(String service, String version, String procedure) {
        Map<String, String> map = new HashMap<String, String>(3);
        if (service != null) {
            map.put(RequestParams.service.name(), service);
        }
        if (version != null) {
            map.put(RequestParams.version.name(), version);
        }
        if (procedure != null) {
            map.put(DeleteSensorParams.procedure.name(), procedure);
        }
        return map;
    }
}
