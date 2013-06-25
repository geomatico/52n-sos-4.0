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

import static java.lang.Boolean.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.n52.sos.util.CollectionHelper.unionOfListOfLists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @since 4.0.0
 *
 */
public class CollectionHelperTest {
    private final Set<String> EMPTY_COLLECTION = new HashSet<String>(0);

    @Test
    public void should_return_empty_list_when_union_receives_null() {
        assertThat(unionOfListOfLists((Set<Set<String>>) null), is(EMPTY_COLLECTION));
    }

    @Test
    public void should_return_empty_list_when_unionOfListOfLists_receives_empty_list() {
        final Collection<? extends Collection<String>> emptyList = new ArrayList<Set<String>>(0);
        assertThat(unionOfListOfLists(emptyList), is(EMPTY_COLLECTION));
    }

    @Test
    public void should_return_union_of_values_without_duplicates() {
        final Collection<String> listA = new ArrayList<String>(2);
        listA.add("A");
        listA.add("B");

        final Collection<String> listB = new ArrayList<String>(4);
        listB.add("B");
        listB.add("C");
        listB.add(null);

        final Collection<String> listC = new ArrayList<String>(2);
        listC.add("");

        final Collection<Collection<String>> col = new ArrayList<Collection<String>>(4);
        col.add(listA);
        col.add(listB);
        col.add(listC);
        col.add(null);
        col.add(new ArrayList<String>(0));

        final Collection<String> check = new HashSet<String>(4);
        check.add("A");
        check.add("B");
        check.add("C");
        check.add("");
        assertThat(unionOfListOfLists(col), is(check));
    }
    
    @Test
	public void isNotEmpty_should_return_true_if_map_is_not_empty()
	{
    	final Map<String,String> map = new HashMap<String, String>(1);
    	map.put("key", "value");
    	assertThat(CollectionHelper.isNotEmpty(map), is(TRUE));
	}
    
    @Test
    public void isNotEmpty_should_return_false_if_map_is_empty()
    {
    	final Map<String,String> map = new HashMap<String, String>(0);
    	assertThat(CollectionHelper.isNotEmpty(map), is(FALSE));
    }
    
    @Test
	public void isEmpty_should_return_true_if_map_is_empty()
	{
    	final Map<String,String> map = new HashMap<String, String>(0);
    	assertThat(CollectionHelper.isEmpty(map), is(TRUE));
	}
    
    @Test
    public void isEmpty_should_return_false_if_map_is_not_empty()
    {
    	final Map<String,String> map = new HashMap<String, String>(1);
    	map.put("key", "value");
    	assertThat(CollectionHelper.isEmpty(map), is(FALSE));
    }
}
