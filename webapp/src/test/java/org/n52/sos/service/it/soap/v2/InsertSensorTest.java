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

package org.n52.sos.service.it.soap.v2;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.n52.sos.service.it.soap.v2.AbstractSoapTest.invalidServiceParameterValueExceptionFault;

import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sensorML.x101.SensorMLDocument.SensorML;
import net.opengis.sensorML.x101.SystemDocument;
import net.opengis.sensorML.x101.SystemType;
import net.opengis.swes.x20.InsertSensorDocument;
import net.opengis.swes.x20.InsertSensorType;

import org.apache.xmlbeans.XmlException;
import org.junit.Test;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class InsertSensorTest extends AbstractSoapTest {
    @Test
    public void invalidServiceParameter() throws XmlException {
        InsertSensorDocument insertSensorDocument = getMinimalRequest();
        insertSensorDocument.getInsertSensor().setService("INVALID");
        MockHttpServletResponse res = execute(insertSensorDocument);
        assertThat(res.getStatus(), is(400));
        assertThat(getResponseAsNode(res), is(invalidServiceParameterValueExceptionFault("INVALID")));
    }

    protected InsertSensorDocument getMinimalRequest() {
        InsertSensorDocument insertSensorDocument = InsertSensorDocument.Factory.newInstance();
        InsertSensorType insertSensorType = insertSensorDocument.addNewInsertSensor();
        insertSensorType.setVersion(Sos2Constants.SERVICEVERSION);
        insertSensorType.setService(SosConstants.SOS);
        insertSensorType.setProcedureDescriptionFormat(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL);
        SensorMLDocument sensorMLDocument = SensorMLDocument.Factory.newInstance();
        SensorML sensorML = sensorMLDocument.addNewSensorML();
        sensorML.setVersion(SensorMLConstants.VERSION_V101);
        SystemDocument systemDocument = SystemDocument.Factory.newInstance();
        SystemType systemType = systemDocument.addNewSystem();
        sensorML.addNewMember().set(systemDocument);
        insertSensorType.addNewProcedureDescription().set(sensorMLDocument);
        insertSensorType.addNewObservableProperty();
        return insertSensorDocument;
    }

}
