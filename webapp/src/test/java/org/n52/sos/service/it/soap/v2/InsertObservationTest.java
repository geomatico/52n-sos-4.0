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
import static org.n52.sos.service.it.AbstractSoapTest.invalidServiceParameterValueExceptionFault;

import javax.xml.namespace.QName;

import net.opengis.gml.x32.TimeInstantDocument;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.om.x20.OMObservationType;
import net.opengis.sos.x20.InsertObservationDocument;
import net.opengis.sos.x20.InsertObservationType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.junit.Test;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.service.it.AbstractSoapTest;
import org.springframework.mock.web.MockHttpServletResponse;


/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class InsertObservationTest extends AbstractSoapTest {

    @Test
    public void invalidServiceParameter() throws XmlException {
        InsertObservationDocument insertObservationDocument = getMinimalRequest();
        insertObservationDocument.getInsertObservation().setService("INVALID");
        MockHttpServletResponse res = execute(insertObservationDocument);
        assertThat(res.getStatus(), is(400));
        assertThat(getResponseAsNode(res), is(invalidServiceParameterValueExceptionFault("INVALID")));
    }

    protected InsertObservationDocument getMinimalRequest() {
        InsertObservationDocument insertObservationDocument = InsertObservationDocument.Factory.newInstance();
        InsertObservationType insertObservationType = insertObservationDocument.addNewInsertObservation();
        insertObservationType.setVersion(Sos2Constants.SERVICEVERSION);
        insertObservationType.setService(SosConstants.SOS);
        insertObservationType.addOffering("offering");
        OMObservationType observation = insertObservationType.addNewObservation().addNewOMObservation();
        observation.setId("id");
        TimeInstantDocument timeInstantDocument = TimeInstantDocument.Factory.newInstance();
        TimeInstantType timeInstantType = timeInstantDocument.addNewTimeInstant();
        timeInstantType.setId("phenomenonTime");
        timeInstantType.addNewTimePosition().setObjectValue("2000-01-01T00:00:00Z");
        observation.addNewPhenomenonTime().set(timeInstantDocument);
        observation.addNewResultTime().setHref("#phenomenonTime");
        observation.addNewProcedure();
        observation.addNewObservedProperty();
        observation.addNewFeatureOfInterest().setHref("featureOfInterest");
        XmlObject result = observation.addNewResult();
        result = result.substitute(new QName(OMConstants.NS_OM_2, OMConstants.EN_RESULT), XmlString.type);
        result.set(XmlString.Factory.newValue("value"));
        return insertObservationDocument;
    }
}
