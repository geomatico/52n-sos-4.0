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

import static java.lang.Boolean.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.joda.time.DateTime;
import org.junit.Test;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * 
 * @since 4.0.0
 *
 */
public class ReadableCacheTest {

	private static final String OFFERING_IDENTIFIER = "test-offering";

	@Test public final void 
	should_return_true_if_min_resulttime_for_offering_is_available()
	{
		final WritableCache cache = new WritableCache();
		cache.setMinResultTimeForOffering(ReadableCacheTest.OFFERING_IDENTIFIER, new DateTime(52l));
		
		assertThat(cache.hasMinResultTimeForOffering(ReadableCacheTest.OFFERING_IDENTIFIER), is(TRUE));
	}
	
	@Test
	public void should_return_false_if_min_resulttime_for_offering_is_null()
	{
		final ReadableCache readCache = new ReadableCache();
		
		assertThat(readCache.hasMinResultTimeForOffering(OFFERING_IDENTIFIER), is(FALSE));

		final WritableCache cache = new WritableCache();
		cache.setMinResultTimeForOffering(OFFERING_IDENTIFIER, null);
		
		assertThat(cache.hasMinResultTimeForOffering(OFFERING_IDENTIFIER), is(FALSE));
	}

}
