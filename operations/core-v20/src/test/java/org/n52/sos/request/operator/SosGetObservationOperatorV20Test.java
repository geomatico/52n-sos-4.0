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
package org.n52.sos.request.operator;

import static java.lang.Boolean.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.n52.sos.util.CollectionHelper.asList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.n52.sos.cache.ContentCache;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class SosGetObservationOperatorV20Test {
	
	@Test
	public void should_return_empty_list_for_bad_parameters()
	{
		final SosGetObservationOperatorV20 operator = mock(SosGetObservationOperatorV20.class);
		when(operator.addChildFeatures(anyCollectionOf(String.class))).thenCallRealMethod();
		
		// null
		List<String> childFeatures = operator.addChildFeatures(null);
		assertThat(childFeatures.isEmpty(), is(TRUE));
		
		// empty list
		childFeatures = operator.addChildFeatures(new ArrayList<String>(0));
		assertThat(childFeatures.isEmpty(), is(TRUE));
	}
	
	@Test
	public void should_add_childs_for_features()
	{
		final SosGetObservationOperatorV20 operator = mock(SosGetObservationOperatorV20.class);
		final ContentCache cache = mock(ContentCache.class);
		final Set<String> myChildFeatures = new HashSet<String>(1);
		myChildFeatures.add("child-feature");
		when(cache.getChildFeatures(anyString(), anyBoolean(), anyBoolean())).thenReturn(myChildFeatures);
		when(operator.getCache()).thenReturn(cache );
		when(operator.addChildFeatures(anyCollectionOf(String.class))).thenCallRealMethod();
		
		final List<String> childFeatures = operator.addChildFeatures(asList("feature"));
		
		assertThat(childFeatures.isEmpty(), is(FALSE));
		assertThat(childFeatures.size(), is(2));
		assertThat(childFeatures, hasItem("child-feature"));
		assertThat(childFeatures, hasItem("feature"));
	}
	
}
