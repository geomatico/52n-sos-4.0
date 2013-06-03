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
package org.n52.sos.ogc.swe.simpleType;

import static java.lang.Boolean.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;


/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 * @since 4.0.0
 */
public class SosSweCategoryTest {

	@Test
	public void should_return_false_for_all_isSetMethods_is_nothing_is_set()
	{
		final SweCategory category = new SweCategory();
		
		assertThat(category.isSetCodeSpace(), is(FALSE));
		assertThat(category.isSetDefinition(), is(FALSE));
		assertThat(category.isSetDescription(), is(FALSE));
		assertThat(category.isSetIdentifier(), is(FALSE));
		assertThat(category.isSetLabel(), is(FALSE));
		assertThat(category.isSetQuality(), is(FALSE));
		assertThat(category.isSetUom(), is(FALSE));
		assertThat(category.isSetValue(), is(FALSE));
		assertThat(category.isSetXml(), is(FALSE));
	}
	
	@Test
	public void should_return_true_afterSetting_CodeSpace()
	{
		final SweCategory category = new SweCategory();
		final String codeSpace = "test-code-space";
		category.setCodeSpace(codeSpace);
		
		assertThat(category.isSetCodeSpace(), is(TRUE));
		assertThat(category.getCodeSpace(), is(codeSpace));
	}
}
