/**
 * Copyright (C) 2012
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
package org.n52.sos.ds.hibernate;

import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.sos.cache.CapabilitiesCache;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.swe.sos.test.AbstractSosTestCase;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class SosCacheFeederDAOTest extends AbstractSosTestCase {
	
	private static SosCacheFeederDAO instance;
	
	@BeforeClass
	public static void init()
	{
		instance = new SosCacheFeederDAO(); 
	}
	
	@Test
	public void constructorReturnsInstance()
	{
		assertNotNull("instance returned by constructor", instance);
	}
	
	@Test
	public void updateCacheFillsCapabilitiesCache() throws OwsExceptionReport
	{
		CapabilitiesCache cache = new CapabilitiesCache();
		instance.updateCache(cache);
		assertNotNull("cache is null", cache);
		assertNotNull("envelope of features is null",cache.getEnvelopeForFeatureOfInterest());
		assertNotNull("feature types is null",cache.getFeatureOfInterestTypes());
		assertFalse("feature types is empty",cache.getFeatureOfInterestTypes().isEmpty());
		assertNotNull("offering envelopes map is null",cache.getKOfferingVEnvelope());
		assertNotNull("offering max times map is null",cache.getKOfferingVMaxTime());
		assertNotNull("offering min times map is null",cache.getKOfferingVMinTime());
		assertNotNull("max event time is null",cache.getMaxEventTime());
		assertNotNull("min event time is null",cache.getMinEventTime());
		assertNotNull("observation types is null",cache.getObservationTypes());
		assertFalse("observation types is emtpy",cache.getObservationTypes().isEmpty());
		assertNotNull("result templates is null",cache.getResultTemplates());
	}
	
	@Test(expected=NullPointerException.class)
	public void updateNull() throws OwsExceptionReport
	{
		instance.updateCache(null);
	}
}
