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
package org.n52.sos.ogc.sensorML.elements;

import static java.lang.Boolean.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;


/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 * @since 4.0.0
 */
public class SmlIdentifierTest {

	@Test
	public void should_return_false_if_name_not_set_correct()
	{
		final SmlIdentifier smlIdentifier = new SmlIdentifier(null, "tmp", "tmp");
		final SmlIdentifier smlId2 = new SmlIdentifier("", "tmp", "tmp");
		
		assertThat(smlIdentifier.isSetName(), is(FALSE));
		assertThat(smlId2.isSetName(), is(FALSE));
	}
	
	@Test
	public void should_return_true_if_name_is_set()
	{
		final String name = "name";
		final SmlIdentifier identifier = new SmlIdentifier(name, null, null);
		
		assertThat(identifier.isSetName(), is(TRUE));
		assertThat(identifier.getName(), is(name));
	}
	
	@Test
	public void should_return_true_if_definition_is_set()
	{
		final String definition = "definition";
		final SmlIdentifier identifier = new SmlIdentifier(null, definition, null);
		
		assertThat(identifier.isSetDefinition(), is(TRUE));
		assertThat(identifier.getDefinition(), is(definition));
	}
	
	@Test
	public void should_return_true_if_value_is_set()
	{
		final String value = "value";
		final SmlIdentifier identifier = new SmlIdentifier(null, null, value);
		
		assertThat(identifier.isSetValue(), is(TRUE));
		assertThat(identifier.getValue(), is(value));
	}
	
	@Test
	public void should_return_false_if_value_not_set_correct()
	{
		final SmlIdentifier smlIdentifier = new SmlIdentifier("tmp", "tmp", null);
		final SmlIdentifier smlId2 = new SmlIdentifier("tmp", "tmp", "");
		
		assertThat(smlIdentifier.isSetValue(), is(FALSE));
		assertThat(smlId2.isSetValue(), is(FALSE));
	}
	
	@Test
	public void should_return_false_if_definition_not_set_correct()
	{
		final SmlIdentifier smlIdentifier = new SmlIdentifier("tmp", null, "tmp");
		final SmlIdentifier smlId2 = new SmlIdentifier("tmp", "", "tmp");
		
		assertThat(smlIdentifier.isSetDefinition(), is(FALSE));
		assertThat(smlId2.isSetDefinition(), is(FALSE));
	}
}
