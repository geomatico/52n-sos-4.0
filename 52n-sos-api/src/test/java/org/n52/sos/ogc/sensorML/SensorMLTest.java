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
package org.n52.sos.ogc.sensorML;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.n52.sos.ogc.OGCConstants.URN_OFFERING_ID;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.n52.sos.ogc.om.SosOffering;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class SensorMLTest {

	/**
	 * Test method for {@link org.n52.sos.ogc.sensorML.AbstractSensorML#getOfferingIdentifiers()}.
	 */
	@Test public void 
	should_return_offering_identifier_from_members_if_available()
	{
		SensorML sensorML = new SensorML();
		System system = new System();
		
		final String offeringIdentifier = "test-offering-identifier";
		final String offeringName = "test-offering-name";
		final SosSMLIdentifier offeringIdentification = new SosSMLIdentifier(offeringName, URN_OFFERING_ID, offeringIdentifier);
		final List<SosSMLIdentifier> identifications = Collections.singletonList(offeringIdentification);
		system.setIdentifications(identifications);
		sensorML.addMember(system);
		
		assertThat(sensorML.getOfferingIdentifiers().size(), is(equalTo(1)));
		SosOffering sosOffering = sensorML.getOfferingIdentifiers().get(0);
		assertThat(sosOffering.getOfferingIdentifier(), is(equalTo(offeringIdentifier)));
		assertThat(sosOffering.getOfferingName(), is(equalTo(offeringName)));
	}

}
