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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.n52.sos.ogc.ows.SosServiceIdentificationFactorySettings.*;
import static org.n52.sos.ogc.ows.SosServiceProviderFactorySettings.*;

import javax.xml.namespace.NamespaceContext;

import org.junit.Test;
import org.n52.sos.exception.ows.OwsExceptionCode;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.sos.SosConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * TODO JavaDoc
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class GetCapabilitiesTest extends AbstractSosServiceTest {
    private NamespaceContext ctx = new SosNamespaceContext();

    @Test
    public void missingServiceParameter() {
        Node node = getResponseAsNode(execute(RequestBuilder.get("/sos/kvp")
                .query(OWSConstants.RequestParams.request,
                       SosConstants.Operations.GetCapabilities)
                .accept(SosConstants.CONTENT_TYPE_XML)));
        assertThat(node, hasXPath("//sos:Capabilities", ctx));
    }

    @Test
    public void invalidServiceParameter() {
        Node node = getResponseAsNode(execute(RequestBuilder.get("/sos/kvp")
                .query(OWSConstants.RequestParams.request,
                       SosConstants.Operations.GetCapabilities)
                .query(OWSConstants.RequestParams.service,
                       "INVALID")
                .accept(SosConstants.CONTENT_TYPE_XML)));
        assertThat(node, hasXPath("//ows:ExceptionReport/ows:Exception/@exceptionCode", ctx,
                                  is(OwsExceptionCode.InvalidParameterValue.name())));
        assertThat(node, hasXPath("//ows:ExceptionReport/ows:Exception/@locator", ctx,
                                  is(OWSConstants.RequestParams.service.name())));
        assertThat(node, hasXPath("//ows:ExceptionReport/ows:Exception/ows:ExceptionText", ctx,
                                  is("The value of the mandatory parameter 'service' must be 'SOS'. Delivered value was: INVALID")));
    }

    @Test
    public void checkServiceIdentification() {
        Element node = getCapabilities();
        assertThat(node, hasXPath("//sos:Capabilities/ows:ServiceIdentification/ows:Title", ctx,
                                  is(TITLE_DEFINITION.getDefaultValue())));
        assertThat(node, hasXPath("//sos:Capabilities/ows:ServiceIdentification/ows:Abstract", ctx,
                                  is(ABSTRACT_DEFINITION.getDefaultValue())));
        assertThat(node, hasXPath("//sos:Capabilities/ows:ServiceIdentification/ows:ServiceType", ctx,
                                  is(SERVICE_TYPE_DEFINITION.getDefaultValue())));
        assertThat(node, hasXPath("//sos:Capabilities/ows:ServiceIdentification/ows:Fees", ctx,
                                  is(FEES_DEFINITION.getDefaultValue())));
        assertThat(node, hasXPath("//sos:Capabilities/ows:ServiceIdentification/ows:AccessConstraints", ctx,
                                  is(ACCESS_CONSTRAINTS_DEFINITION.getDefaultValue())));
    }

    @Test
    public void checkServiceProvider() {
        Element node = getCapabilities();
        assertThat(node, hasXPath("//sos:Capabilities/ows:ServiceProvider/ows:ProviderName", ctx,
                                  is(NAME_DEFINITION.getDefaultValue())));
        assertThat(node, hasXPath("//sos:Capabilities/ows:ServiceProvider/ows:ProviderSite/@xlink:href", ctx,
                                  is(SITE_DEFINITION.getDefaultValue().toString())));
        assertThat(node, hasXPath("//sos:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:IndividualName", ctx,
                                  is(INDIVIDUAL_NAME_DEFINITION.getDefaultValue())));
        assertThat(node, hasXPath("//sos:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:PositionName", ctx,
                                  is(POSITION_NAME_DEFINITION.getDefaultValue())));
        assertThat(node, hasXPath("//sos:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Phone/ows:Voice", ctx,
                                  is(PHONE_DEFINITION.getDefaultValue())));
        assertThat(node, hasXPath("//sos:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:DeliveryPoint", ctx,
                                  is(DELIVERY_POINT_DEFINITION.getDefaultValue())));
        assertThat(node, hasXPath("//sos:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:City", ctx,
                                  is(CITY_DEFINITION.getDefaultValue())));
        assertThat(node, hasXPath("//sos:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:AdministrativeArea", ctx,
                                  is(ADMINISTRATIVE_AREA_DEFINITION.getDefaultValue())));
        assertThat(node, hasXPath("//sos:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:PostalCode", ctx,
                                  is(POSTAL_CODE_DEFINITION.getDefaultValue())));
        assertThat(node, hasXPath("//sos:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:Country", ctx,
                                  is(COUNTRY_DEFINITION.getDefaultValue())));
        assertThat(node, hasXPath("//sos:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:ElectronicMailAddress", ctx,
                                  is(MAIL_ADDRESS_DEFINITION.getDefaultValue())));
    }

    protected Element getCapabilities() {
        return getResponseAsNode(execute(RequestBuilder.get("/sos/kvp")
                .query(OWSConstants.RequestParams.request,
                       SosConstants.Operations.GetCapabilities)
                .query(OWSConstants.RequestParams.service,
                       SosConstants.SOS)
                .accept(SosConstants.CONTENT_TYPE_XML)));
    }
}
