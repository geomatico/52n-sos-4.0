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

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class CapabilitiesCacheTest{
	
	private static CapabilitiesCache instance;

	@Test
	public void defaultConstructorReturnsObject()
	{
		initInstance();
		assertNotNull("instance is null", instance);
		assertTrue("right class", instance instanceof CapabilitiesCache);
	}

	@BeforeClass
	public static void initInstance()
	{
		instance = new CapabilitiesCache();
	}
	
	@Test
	public void equalsWithNewInstances()
	{
		CapabilitiesCache instance2 = new CapabilitiesCache();
		assertEquals("equals failed",instance,instance2);
	}
	
	@Test
	public void equalsWithSelf()
	{
		assertEquals("I am not equal with me",instance,instance);
	}
	
	@Test
	public void equalsWithNull()
	{
		assertNotEquals("equal with null", instance, null);
	}
	
	@Test
	public void equalWithOtherClass()
	{
		assertNotEquals("equal with Object", instance, new Object());
	}

}
