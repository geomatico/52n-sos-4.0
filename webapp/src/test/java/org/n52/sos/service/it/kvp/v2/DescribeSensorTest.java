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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.n52.sos.service.it.AbstractSosServiceTest.missingServiceParameterValueException;

import org.junit.Test;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.service.it.AbstractSosServiceTest;
import org.n52.sos.service.it.RequestBuilder;
import org.w3c.dom.Node;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class DescribeSensorTest extends AbstractSosServiceTest {

    @Test
    public void invalidServiceParameter() {
        Node node = getResponseAsNode(execute(RequestBuilder.get("/kvp")
                .query(OWSConstants.RequestParams.request, SosConstants.Operations.DescribeSensor)
                .query(OWSConstants.RequestParams.service, "INVALID")
                .query(OWSConstants.RequestParams.version, Sos2Constants.SERVICEVERSION)
                .accept(SosConstants.CONTENT_TYPE_XML)));
        assertThat(node, is(invalidServiceParameterValueException("INVALID")));
    }

    @Test
    public void emptyServiceParameter() {
        Node node = getResponseAsNode(execute(RequestBuilder.get("/kvp")
                .query(OWSConstants.RequestParams.request, SosConstants.Operations.DescribeSensor)
                .query(OWSConstants.RequestParams.service, "")
                .query(OWSConstants.RequestParams.version, Sos2Constants.SERVICEVERSION)
                .accept(SosConstants.CONTENT_TYPE_XML)));
        assertThat(node, is(missingServiceParameterValueException()));
    }
}
