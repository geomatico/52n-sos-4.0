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
package org.n52.sos.cache;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.n52.sos.cache.Existing.existing;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.sos.cache.ctrl.LockingPersistingCacheController;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.request.DeleteSensorRequest;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.request.InsertResultTemplateRequest;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.InsertResultTemplateResponse;
import org.n52.sos.response.InsertSensorResponse;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class PersistingCacheControllerTest {
    public static final String IDENTIFIER = "identifier";
    private static File tempFile;

    @BeforeClass
    public static void setUp() {
        try {
            tempFile = File.createTempFile("TestableInMemoryCacheController", "");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Before
    @After
    public void deleteTempFile() {
        tempFile.delete();
    }

    @Test
    public void testSerialization() {
        tempFile.delete();
        assertThat(tempFile, is(not(existing())));
        LockingPersistingCacheController cc = new TestableController();
        assertThat(cc.getCache().getObservationIdentifiers(), is(empty()));
        ((WritableContentCache) cc.getCache()).addObservationIdentifier(IDENTIFIER);
        assertThat(cc.getCache().getObservationIdentifiers(), contains(IDENTIFIER));
        cc.cleanup();
        assertThat(tempFile, is(existing()));
        cc = new TestableController();
        assertThat(tempFile, is(not(existing())));
        assertThat(cc.getCache().getObservationIdentifiers(), contains(IDENTIFIER));
    }

    private class TestableController extends LockingPersistingCacheController {

        @Override
        protected File getCacheFile() {
            return tempFile;
        }

        @Override
        protected void updateAfterObservationDeletion(WritableContentCache cache) throws OwsExceptionReport {
        }

        @Override
        protected void updateAfterObservationInsertion(WritableContentCache cache, InsertObservationRequest sosRequest) {
        }

        @Override
        protected void updateAfterResultInsertion(WritableContentCache cache, String templateIdentifier,
                                                  SosObservation sosObservation) {
        }

        @Override
        protected void updateAfterResultTemplateInsertion(WritableContentCache cache,
                                                          InsertResultTemplateRequest sosRequest,
                                                          InsertResultTemplateResponse sosResponse) {
        }

        @Override
        protected void updateAfterSensorDeletion(WritableContentCache cache, DeleteSensorRequest sosRequest) {
        }

        @Override
        protected void updateAfterSensorInsertion(WritableContentCache cache, InsertSensorRequest sosRequest,
                                                  InsertSensorResponse sosResponse) {
        }

        @Override
        protected boolean updateCacheFromDatasource(WritableContentCache cache) throws OwsExceptionReport {
            return true;
        }
    }
}
