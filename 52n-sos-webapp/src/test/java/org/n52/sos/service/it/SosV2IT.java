package org.n52.sos.service.it;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

public class SosV2IT extends SosITBase {

    /**
     * Verify v2 test client exists
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void verifyTestClientSosV2() throws IOException, URISyntaxException{
        verifyPathExists("testClient-SOSv2.html");
    }
}
