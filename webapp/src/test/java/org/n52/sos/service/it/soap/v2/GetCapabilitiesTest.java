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

import net.opengis.sos.x20.GetCapabilitiesDocument;
import net.opengis.sos.x20.GetCapabilitiesType;

import org.apache.xmlbeans.XmlException;
import org.junit.Test;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class GetCapabilitiesTest extends AbstractSosV2SoapTest {

    @Override
    @Test
    public void missingServiceParameter() throws XmlException {
        GetCapabilitiesDocument getCapabilitiesDocument = GetCapabilitiesDocument.Factory.newInstance();
        GetCapabilitiesType getCapabilitiesType = getCapabilitiesDocument.addNewGetCapabilities2();
        getCapabilitiesType.addNewAcceptVersions().addNewVersion().setStringValue(Sos2Constants.SERVICEVERSION);
        MockHttpServletResponse res = execute(getCapabilitiesDocument);
        assertThat(res.getStatus(), is(200));
        // TODO check if response is a sos:Capabilities document
//        assertThat(getResponseAsNode(res), is(missingServiceParameterValueExceptionFault()));
    }

    @Override
    @Test
    public void emptyServiceParameter() throws XmlException {
        GetCapabilitiesDocument getCapabilitiesDocument = GetCapabilitiesDocument.Factory.newInstance();
        GetCapabilitiesType getCapabilitiesType = getCapabilitiesDocument.addNewGetCapabilities2();
        getCapabilitiesType.addNewAcceptVersions().addNewVersion().setStringValue(Sos2Constants.SERVICEVERSION);
        getCapabilitiesType.setService("");
        MockHttpServletResponse res = execute(getCapabilitiesDocument);
        assertThat(res.getStatus(), is(400));
        assertThat(getResponseAsNode(res), is(missingServiceParameterValueExceptionFault()));
    }

    @Test
    public void invalidServiceParameter() throws XmlException {
        GetCapabilitiesDocument getCapabilitiesDocument = GetCapabilitiesDocument.Factory.newInstance();
        GetCapabilitiesType getCapabilitiesType = getCapabilitiesDocument.addNewGetCapabilities2();
        getCapabilitiesType.addNewAcceptVersions().addNewVersion().setStringValue(Sos2Constants.SERVICEVERSION);
        getCapabilitiesType.setService("INVALID");
        MockHttpServletResponse res = execute(getCapabilitiesDocument);
        assertThat(res.getStatus(), is(400));
        assertThat(getResponseAsNode(res), is(invalidServiceParameterValueExceptionFault("INVALID")));
    }
}
