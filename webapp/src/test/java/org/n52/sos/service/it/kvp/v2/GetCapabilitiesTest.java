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

package org.n52.sos.service.it.kvp.v2;

import static org.n52.sos.ogc.ows.SosServiceIdentificationFactorySettings.ABSTRACT_DEFINITION;
import static org.n52.sos.ogc.ows.SosServiceIdentificationFactorySettings.ACCESS_CONSTRAINTS_DEFINITION;
import static org.n52.sos.ogc.ows.SosServiceIdentificationFactorySettings.FEES_DEFINITION;
import static org.n52.sos.ogc.ows.SosServiceIdentificationFactorySettings.SERVICE_TYPE_DEFINITION;
import static org.n52.sos.ogc.ows.SosServiceIdentificationFactorySettings.TITLE_DEFINITION;
import static org.n52.sos.ogc.ows.SosServiceProviderFactorySettings.ADMINISTRATIVE_AREA_DEFINITION;
import static org.n52.sos.ogc.ows.SosServiceProviderFactorySettings.CITY_DEFINITION;
import static org.n52.sos.ogc.ows.SosServiceProviderFactorySettings.COUNTRY_DEFINITION;
import static org.n52.sos.ogc.ows.SosServiceProviderFactorySettings.DELIVERY_POINT_DEFINITION;
import static org.n52.sos.ogc.ows.SosServiceProviderFactorySettings.INDIVIDUAL_NAME_DEFINITION;
import static org.n52.sos.ogc.ows.SosServiceProviderFactorySettings.MAIL_ADDRESS_DEFINITION;
import static org.n52.sos.ogc.ows.SosServiceProviderFactorySettings.NAME_DEFINITION;
import static org.n52.sos.ogc.ows.SosServiceProviderFactorySettings.PHONE_DEFINITION;
import static org.n52.sos.ogc.ows.SosServiceProviderFactorySettings.POSITION_NAME_DEFINITION;
import static org.n52.sos.ogc.ows.SosServiceProviderFactorySettings.POSTAL_CODE_DEFINITION;
import static org.n52.sos.ogc.ows.SosServiceProviderFactorySettings.SITE_DEFINITION;

import javax.xml.namespace.NamespaceContext;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.n52.sos.exception.ows.OwsExceptionCode;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.GetCapabilitiesParams;
import org.n52.sos.service.it.SosNamespaceContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 * @author Carsten Hollmann <c.hollmann@52north.org>
 * @since 4.0.0
 */
public class GetCapabilitiesTest extends AbstractSosV2KvpTest {
    private NamespaceContext ctx = new SosNamespaceContext();

    @Override
    @Before
    public void setRequest() {
        request = SosConstants.Operations.GetCapabilities.name();
    }

    @Test
    public void missingServiceParameter() {
        Node node = getResponseAsNode(execute(builder().query(OWSConstants.RequestParams.request, request)));
        assertThat(node, hasXPath("//sos:Capabilities", ctx));
    }

    @Override
    @Test
    public void emptyServiceParameter() {
        Node node =
                getResponseAsNode(execute(builder().query(OWSConstants.RequestParams.request, request).query(
                        OWSConstants.RequestParams.service, "")));
        assertThat(node, is(missingServiceParameterValueException()));
    }

    @Override
    @Test
    public void invalidServiceParameter() {
        Node node =
                getResponseAsNode(execute(builder().query(OWSConstants.RequestParams.request,
                        SosConstants.Operations.GetCapabilities).query(OWSConstants.RequestParams.service, "INVALID")));
        assertThat(node, is(invalidServiceParameterValueException("INVALID")));
    }

    @Override
    @Test
    public void missingVersionParameter() {
        // not a GetCapabilities parameter
    }

    @Override
    @Test
    public void emptyVersionParameter() {
        // not a GetCapabilities parameter
    }

    @Override
    @Test
    public void invalidVersionParameter() {
        // not a GetCapabilities parameter
    }

    @Test
    @Ignore
    public void emptySectionParameter() {
        Node node =
                getResponseAsNode(execute(builder().query(OWSConstants.RequestParams.request, request)
                        .query(OWSConstants.RequestParams.service, SERVICE)
                        .query(SosConstants.GetCapabilitiesParams.Sections, "")));
        assertThat(
                node,
                is(exception(OwsExceptionCode.MissingParameterValue, GetCapabilitiesParams.Sections,
                        "The value for the parameter 'sections' is missing in the request!")));
    }

    @Test
    public void invalidSectionParameter() {
        Node node =
                getResponseAsNode(execute(builder()
                        .query(OWSConstants.RequestParams.request, SosConstants.Operations.GetCapabilities)
                        .query(OWSConstants.RequestParams.service, SosConstants.SOS)
                        .query(SosConstants.GetCapabilitiesParams.Sections, "INVALID")));
        assertThat(
                node,
                is(exception(OwsExceptionCode.InvalidParameterValue, GetCapabilitiesParams.Section,
                        "The requested section 'INVALID' does not exist or is not supported!")));
    }

    @Test
    public void checkServiceIdentification() {
        Element node = getCapabilities();
        assertThat(
                node,
                hasXPath("//sos:Capabilities/ows:ServiceIdentification/ows:Title", ctx,
                        is(TITLE_DEFINITION.getDefaultValue())));
        assertThat(
                node,
                hasXPath("//sos:Capabilities/ows:ServiceIdentification/ows:Abstract", ctx,
                        is(ABSTRACT_DEFINITION.getDefaultValue())));
        assertThat(
                node,
                hasXPath("//sos:Capabilities/ows:ServiceIdentification/ows:ServiceType", ctx,
                        is(SERVICE_TYPE_DEFINITION.getDefaultValue())));
        assertThat(
                node,
                hasXPath("//sos:Capabilities/ows:ServiceIdentification/ows:Fees", ctx,
                        is(FEES_DEFINITION.getDefaultValue())));
        assertThat(
                node,
                hasXPath("//sos:Capabilities/ows:ServiceIdentification/ows:AccessConstraints", ctx,
                        is(ACCESS_CONSTRAINTS_DEFINITION.getDefaultValue())));
    }

    @Test
    public void checkServiceProvider() {
        Element node = getCapabilities();
        assertThat(
                node,
                hasXPath("//sos:Capabilities/ows:ServiceProvider/ows:ProviderName", ctx,
                        is(NAME_DEFINITION.getDefaultValue())));
        assertThat(
                node,
                hasXPath("//sos:Capabilities/ows:ServiceProvider/ows:ProviderSite/@xlink:href", ctx,
                        is(SITE_DEFINITION.getDefaultValue().toString())));
        assertThat(
                node,
                hasXPath("//sos:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:IndividualName", ctx,
                        is(INDIVIDUAL_NAME_DEFINITION.getDefaultValue())));
        assertThat(
                node,
                hasXPath("//sos:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:PositionName", ctx,
                        is(POSITION_NAME_DEFINITION.getDefaultValue())));
        assertThat(
                node,
                hasXPath(
                        "//sos:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Phone/ows:Voice",
                        ctx, is(PHONE_DEFINITION.getDefaultValue())));
        assertThat(
                node,
                hasXPath(
                        "//sos:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:DeliveryPoint",
                        ctx, is(DELIVERY_POINT_DEFINITION.getDefaultValue())));
        assertThat(
                node,
                hasXPath(
                        "//sos:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:City",
                        ctx, is(CITY_DEFINITION.getDefaultValue())));
        assertThat(
                node,
                hasXPath(
                        "//sos:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:AdministrativeArea",
                        ctx, is(ADMINISTRATIVE_AREA_DEFINITION.getDefaultValue())));
        assertThat(
                node,
                hasXPath(
                        "//sos:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:PostalCode",
                        ctx, is(POSTAL_CODE_DEFINITION.getDefaultValue())));
        assertThat(
                node,
                hasXPath(
                        "//sos:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:Country",
                        ctx, is(COUNTRY_DEFINITION.getDefaultValue())));
        assertThat(
                node,
                hasXPath(
                        "//sos:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:ElectronicMailAddress",
                        ctx, is(MAIL_ADDRESS_DEFINITION.getDefaultValue())));
    }

    protected Element getCapabilities() {
        return getResponseAsNode(execute(builder().query(OWSConstants.RequestParams.request, request).query(
                OWSConstants.RequestParams.service, SERVICE)));
    }
}
