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
package org.n52.sos.ds.hibernate.util;

import java.util.Map;

import org.hibernate.Session;
import org.junit.Test;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.service.Configurator;
import org.n52.sos.test.AbstractSosTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class HibernateCriteriaQueryUtilitiesTest extends AbstractSosTestCase {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HibernateCriteriaQueryUtilitiesTest.class);
	
	@Test
	public void getGlobalTemporalBoundingBoxWithNullReturnsNull()
	{
		assertNull("global temporal bounding box",HibernateCriteriaQueryUtilities.getGlobalTemporalBoundingBox(null));
	}
	
	@Test
	public void getGlobalTemporalBoundingBoxEndBeforeStartOrEqual() throws ConnectionProviderException
	{
		Session session = (Session)Configurator.getInstance().getDataConnectionProvider().getConnection();
		TimePeriod temporalBBox = HibernateCriteriaQueryUtilities.getGlobalTemporalBoundingBox(session);
		assertNotNull("global temporal bounding box",temporalBBox);
		assertNotNull("temporal bbox start",temporalBBox.getStart());
		assertNotNull("temporal bbox end",temporalBBox.getEnd());
		timePeriodStartIsBeforeEndOrEqual(temporalBBox);
	}

	private void timePeriodStartIsBeforeEndOrEqual(TimePeriod temporalBBox)
	{
		boolean startBeforeEndOrEqual = temporalBBox.getStart().isEqual(temporalBBox.getEnd()) || temporalBBox.getStart().isBefore( temporalBBox.getEnd() );
		assertTrue("start is before end or equal", startBeforeEndOrEqual );
	}
	
	@Test
	public void getTemporalBoundingBoxForOfferingsWithNullReturnsEmptyList()
	{
		Map<String, TimePeriod> emptyMap = HibernateCriteriaQueryUtilities.getTemporalBoundingBoxesForOfferings(null);
		assertNotNull("empty map", emptyMap);
		assertTrue("map is empty", emptyMap.isEmpty());
	}
	
	@Test
	public void getTemporalBoundingBoxForOfferingsContainsNoNullElements() throws ConnectionProviderException
	{
		Session session = (Session)Configurator.getInstance().getDataConnectionProvider().getConnection();
		Map<String,TimePeriod> tempBBoxMap = HibernateCriteriaQueryUtilities.getTemporalBoundingBoxesForOfferings(session);
		assertFalse("map is empty", tempBBoxMap.isEmpty());
		for (String offeringId : tempBBoxMap.keySet())
		{
			assertNotNull("offering id",offeringId);
			TimePeriod offeringBBox = tempBBoxMap.get(offeringId);
			assertNotNull("offering temp bbox", offeringBBox);
			assertNotNull("offering temporal bbox start", offeringBBox.getStart());
			assertNotNull("offering temporal bbox start", offeringBBox.getEnd());
			timePeriodStartIsBeforeEndOrEqual(offeringBBox);
		}
	}
	
	@Test
	public void runtimeComparisonGetGlobalTemporalBoundingBoxes() throws ConnectionProviderException
	{
		long startOldWay, startNewWay, endOldWay, endNewWay;
		Session session = (Session)Configurator.getInstance().getDataConnectionProvider().getConnection();
		startOldWay = System.currentTimeMillis();
		HibernateCriteriaQueryUtilities.getMinObservationTime(session);
		HibernateCriteriaQueryUtilities.getMaxObservationTime(session);
		endOldWay = System.currentTimeMillis();
		startNewWay = System.currentTimeMillis();
		HibernateCriteriaQueryUtilities.getGlobalTemporalBoundingBox(session);
		endNewWay = System.currentTimeMillis();
		long oldTime = endOldWay - startOldWay, newTime = endNewWay - startNewWay;
		assertTrue(String.format("old way is faster? Old way: %sms\\nNew Way: %sms",oldTime,newTime), newTime < oldTime);
		LOGGER.debug("old way is faster? Old way: {}ms\\nNew Way: {}ms",oldTime,newTime);
	}

}
