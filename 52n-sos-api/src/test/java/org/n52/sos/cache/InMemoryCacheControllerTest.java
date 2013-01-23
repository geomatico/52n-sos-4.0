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

import static org.junit.Assert.assertEquals;
import static org.n52.sos.util.builder.InsertObservationRequestBuilder.aInsertObservationRequest;
import static org.n52.sos.util.builder.ObservablePropertyBuilder.aObservableProperty;
import static org.n52.sos.util.builder.ObservationBuilder.anObservation;
import static org.n52.sos.util.builder.ObservationConstellationBuilder.aObservationConstellation;
import static org.n52.sos.util.builder.ProcedureDescriptionBuilder.aSensorMLProcedureDescription;
import static org.n52.sos.util.builder.QuantityObservationValueBuilder.aQuantityValue;
import static org.n52.sos.util.builder.QuantityValueBuilder.aQuantitiy;
import static org.n52.sos.util.builder.SamplingFeatureBuilder.aSamplingFeature;

import org.junit.Ignore;
import org.junit.Test;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.request.InsertObservationRequest;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class InMemoryCacheControllerTest
{
	@Ignore
	@Test (expected=IllegalArgumentException.class)
	public void shoudlThrowIllegalArgumentExceptionWhenReceivingNullArgument() throws OwsExceptionReport
	{
		InMemoryCacheController controller = new TestableInMemoryCacheController();
		controller.updateAfterObservationInsertion(null);
	}
	
	@Ignore
	@Test
	public void shouldUpdateGlobalTemporalBoundingBox() throws OwsExceptionReport
	{
		InsertObservationRequest request = aInsertObservationRequest()
				.setProcedureId("test-procedure")
				.addOffering("test-offering")
				.addObservation(anObservation()
					.setObservationConstellation(aObservationConstellation()
						.setFeature(aSamplingFeature()
							.setIdentifier("test-feature")
							.setGeometry(52.0,7.5,4326)
							.build())
						.setProcedure(aSensorMLProcedureDescription()
							.setIdentifier("test-sensor")
							.build())
						.setObservationType(OMConstants.OBS_TYPE_MEASUREMENT)
						.setObservableProperty(aObservableProperty()
							.setIdentifier("test-observed-property")
							.build())
						.build())
					.setValue(aQuantityValue()
							.setValue(
								aQuantitiy()
									.setValue(2.0)
									.setUnit("m")
									.build())
							.setPhenomenonTime(System.currentTimeMillis())
							.build()
							)
					.build())
				.build();
		InMemoryCacheController controller = new TestableInMemoryCacheController();
		controller.updateAfterObservationInsertion(request);
		assertEquals("maxtime",controller.getMaxEventTime(),((TimeInstant)request.getObservations().get(0).getPhenomenonTime()).getValue());
		assertEquals("mintime",controller.getMinEventTime(),((TimeInstant)request.getObservations().get(0).getPhenomenonTime()).getValue());
	}
	
	private class TestableInMemoryCacheController extends InMemoryCacheController
	{
		protected long getUpdateInterval()
		{
			return 60000;
		}
		
		protected int getDefaultEPSG()
		{
			return 4326;
		}
	}
}
