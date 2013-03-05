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
package org.n52.sos.util;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.n52.sos.util.CollectionHelper.unionOfListOfLists;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @since 4.0.0
 *
 */
public class CollectionHelperTest {

	private Collection<Object> EMPTY_COLLECTION = new ArrayList<Object>(0);

	@Test public void 
	should_return_empty_list_when_union_receives_null()
	{
		assertThat(unionOfListOfLists(null), is(EMPTY_COLLECTION));
	}
	
	@Test public void 
	should_return_empty_list_when_unionOfListOfLists_receives_empty_list()
	{
		final Collection<Collection<Object>> emptyList = new ArrayList<Collection<Object>>(0);
		assertThat(unionOfListOfLists(emptyList), is(EMPTY_COLLECTION));
	}
	
	@Test
	public void should_return_union_of_values_without_duplicates()
	{
		Collection<String> listA = new ArrayList<String>();
		listA.add("A");
		listA.add("B");
		
		Collection<String> listB = new ArrayList<String>();
		listB.add("B");
		listB.add("C");
		listB.add(null);
		
		Collection<Collection<String>> col = new ArrayList<Collection<String>>();
		col.add(listA);
		col.add(listB);
		col.add(null);
		col.add((Collection<String>)new ArrayList<String>(0));
		
		Collection<String> check = new ArrayList<String>();
		check.add("A");
		check.add("B");
		check.add("C");
		assertThat(unionOfListOfLists(col), is(check));
	}
	
}
