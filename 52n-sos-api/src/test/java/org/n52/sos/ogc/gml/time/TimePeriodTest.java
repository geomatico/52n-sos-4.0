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
package org.n52.sos.ogc.gml.time;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.junit.Test;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class TimePeriodTest {
	
	@Test
	public void isEmptyTest()
	{
		assertTrue("new Timeperiod is NOT empty", new TimePeriod().isEmpty());
		assertTrue("new TimePeriod(null, null) is NOT empty",new TimePeriod(null, null).isEmpty());
		assertTrue("new TimePeriod(null, null,null) is NOT empty",new TimePeriod(null, null, null).isEmpty());
		assertTrue("new TimePeriod(null, null,\"id\") is NOT empty",new TimePeriod(null, null, "id").isEmpty());
		assertFalse("new TimePeriod(new DateTime(),null) is empty",new TimePeriod(new DateTime(), null).isEmpty());
		assertFalse("new TimePeriod(null,new DateTime()) is empty",new TimePeriod(null,new DateTime()).isEmpty());
	}
	
	@Test
	public void isSetStartTest()
	{
		assertTrue("new TimePeriod(new DateTime(),null).isSetStart() == false",new TimePeriod(new DateTime(),null).isSetStart());
	}
	
	@Test
	public void isSetEndTest()
	{
		assertTrue("new TimePeriod(null,new DateTime()).isSetEnd() == false",new TimePeriod(null,new DateTime()).isSetEnd());
	}

}
