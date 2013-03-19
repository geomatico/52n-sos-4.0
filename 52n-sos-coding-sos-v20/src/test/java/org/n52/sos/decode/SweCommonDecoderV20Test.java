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
package org.n52.sos.decode;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import net.opengis.swe.x20.BooleanType;
import net.opengis.swe.x20.CategoryType;

import org.apache.xmlbeans.XmlException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.swe.simpleType.SosSweBoolean;
import org.n52.sos.ogc.swe.simpleType.SosSweCategory;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class SweCommonDecoderV20Test {

	private SweCommonDecoderV20 decoder;
	private String definition= "test-definition";
	
	@Before public void
	initDecoder()
	{
		decoder = new SweCommonDecoderV20();
	}
	
	@After public void
	nullDecoder()
	{
		decoder = null;
	}

	@Test public void 
	should_encode_xbBoolean_into_SosSweBoolean_with_correct_value_and_definition()
            throws OwsExceptionReport {
		BooleanType xbBoolean = BooleanType.Factory.newInstance();
		final boolean value = true;
		xbBoolean.setValue(value);
		xbBoolean.setDefinition(definition);
		
		Object decodedObject = decoder.decode(xbBoolean);
		
		assertThat(decodedObject.getClass().getName(), is(SosSweBoolean.class.getName()));
		
		SosSweBoolean sosBoolean = (SosSweBoolean) decodedObject;
		
		assertThat(sosBoolean.getValue(), is(value));
		assertThat(sosBoolean.getDefinition(), is(definition));
	}
	
	@Test public void
	should_encode_xbCategory_into_SosSweCategory_with_correct_value_definition_and_codespace()
            throws OwsExceptionReport, XmlException {
		final String codeSpace = "test-codespace";
		final String value = "test-category-value";
		
		CategoryType xbCategory = CategoryType.Factory.newInstance();
		xbCategory.addNewCodeSpace().setHref(codeSpace);
		xbCategory.setValue(value);
		xbCategory.setDefinition(definition);
		
		Object decodedObject = decoder.decode(xbCategory);
		
		assertThat(decodedObject.getClass().getName(), is(SosSweCategory.class.getName()));
		
		SosSweCategory sosCategory = (SosSweCategory) decodedObject;
		
		assertThat(sosCategory.getValue(), is(value));
		assertThat(sosCategory.getDefinition(), is(definition));
		assertThat(sosCategory.getCodeSpace(), is(codeSpace));
	}

}
