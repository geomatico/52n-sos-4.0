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
package org.n52.sos.encode;

import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import net.opengis.swe.x101.AnyScalarPropertyType;
import net.opengis.swe.x101.SimpleDataRecordType;

import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.Test;
import org.n52.sos.config.SettingsManager;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.SosSweSimpleDataRecord;
import org.n52.sos.ogc.swe.simpleType.SosSweBoolean;
import org.n52.sos.ogc.swe.simpleType.SosSweCategory;
import org.n52.sos.ogc.swe.simpleType.SosSweCount;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.ogc.swe.simpleType.SosSweTime;


/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * 
 * @since 4.0.0
 */
public class SweCommonEncoderv101Test {
	@AfterClass
	public static final void cleanUp(){
		SettingsManager.getInstance().cleanup();
	}

	@Test
	public final void should_encode_simpleDataRecord() throws OwsExceptionReport
	{
		final XmlObject encode = new SweCommonEncoderv101().encode(new SosSweSimpleDataRecord());
		
		assertThat(encode,instanceOf(SimpleDataRecordType.class));
	}
	
	@Test
	public void should_encode_simpleDataRecordWithFields() throws OwsExceptionReport
	{
		final String field0Value = "field-0-value";
		final String field0Name = "field-0";
		final String field1Name = "field-1";
		final Boolean field1Value = Boolean.TRUE;
		
		final XmlObject encode = new SweCommonEncoderv101().encode(
				new SosSweSimpleDataRecord()
				.addField(
						new SosSweField(field0Name, 
								new SosSweText()
						.setValue(field0Value)
						))
				.addField(
						new SosSweField(field1Name,
								new SosSweBoolean()
						.setValue(field1Value)
						))
						);
		
		assertThat(encode,instanceOf(SimpleDataRecordType.class));
		
		final SimpleDataRecordType xbSimpleDataRecord = (SimpleDataRecordType) encode;
		final AnyScalarPropertyType field0 = xbSimpleDataRecord.getFieldArray(0);
		final AnyScalarPropertyType field1 = xbSimpleDataRecord.getFieldArray(1);
		
		assertThat(xbSimpleDataRecord.getFieldArray().length, is(2));
		assertThat(field0.isSetText(), is(TRUE));
		assertThat(field0.getName(), is(field0Name));
		assertThat(field0.getText().getValue(), is(field0Value));
		assertThat(field1.isSetBoolean(), is(TRUE));
		assertThat(field1.getName(), is(field1Name));
		assertThat(field1.getBoolean().getValue(), is(field1Value));
	}
	
	@Test
	public void should_encode_simpleDatarecord_with_fieldBoolean() throws OwsExceptionReport
	{
		final String field1Name = "field-1";
		final Boolean field1Value = Boolean.TRUE;
		
		final XmlObject encode = new SweCommonEncoderv101().encode(
				new SosSweSimpleDataRecord()
				.addField(
						new SosSweField(field1Name,
								new SosSweBoolean()
						.setValue(field1Value)
						))
						);
		
		assertThat(encode,instanceOf(SimpleDataRecordType.class));
		
		final SimpleDataRecordType xbSimpleDataRecord = (SimpleDataRecordType) encode;
		final AnyScalarPropertyType field1 = xbSimpleDataRecord.getFieldArray(0);
		
		assertThat(xbSimpleDataRecord.getFieldArray().length, is(1));
		assertThat(field1.isSetBoolean(), is(TRUE));
		assertThat(field1.getName(), is(field1Name));
		assertThat(field1.getBoolean().getValue(), is(field1Value));
	}
	
	@Test
	public void should_encode_simpleDatarecord_with_fieldText() throws OwsExceptionReport
	{
		final String field1Name = "field-1";
		final String field1Value = "field-1-value";
		
		final XmlObject encode = new SweCommonEncoderv101().encode(
				new SosSweSimpleDataRecord()
				.addField(
						new SosSweField(field1Name,
								new SosSweText()
						.setValue(field1Value)
						))
						);
		
		assertThat(encode,instanceOf(SimpleDataRecordType.class));
		
		final SimpleDataRecordType xbSimpleDataRecord = (SimpleDataRecordType) encode;
		final AnyScalarPropertyType field1 = xbSimpleDataRecord.getFieldArray(0);
		
		assertThat(xbSimpleDataRecord.getFieldArray().length, is(1));
		assertThat(field1.isSetText(), is(TRUE));
		assertThat(field1.getName(), is(field1Name));
		assertThat(field1.getText().getValue(), is(field1Value));
	}

	@Test
	public void should_encode_simpleDatarecord_with_fieldCategory() throws OwsExceptionReport
	{
		final String name = "field-1";
		final String value = "field-1-value";
		
		final String codeSpace = "field-1-codespace";
		final XmlObject encode = new SweCommonEncoderv101().encode(
				new SosSweSimpleDataRecord()
				.addField(
						new SosSweField(name,
								new SosSweCategory()
						.setValue(value)
						.setCodeSpace(codeSpace)
						))
						);
		
		assertThat(encode,instanceOf(SimpleDataRecordType.class));
		
		final SimpleDataRecordType xbSimpleDataRecord = (SimpleDataRecordType) encode;
		final AnyScalarPropertyType field1 = xbSimpleDataRecord.getFieldArray(0);
		
		assertThat(xbSimpleDataRecord.getFieldArray().length, is(1));
		assertThat(field1.isSetCategory(), is(TRUE));
		assertThat(field1.getName(), is(name));
		assertThat(field1.getCategory().getValue(), is(value));
		assertThat(field1.getCategory().isSetCodeSpace(), is(TRUE));
		assertThat(field1.getCategory().getCodeSpace().isSetHref(), is(TRUE));
	}
	
	@Test
	public void should_encode_simpleDatarecord_with_fieldCount() throws OwsExceptionReport
	{
		final String name = "field-1";
		final int value = 42;
		
		final XmlObject encode = new SweCommonEncoderv101().encode(
				new SosSweSimpleDataRecord()
				.addField(
						new SosSweField(name,
								new SosSweCount()
						.setValue(value)
						))
						);
		
		assertThat(encode,instanceOf(SimpleDataRecordType.class));
		
		final SimpleDataRecordType xbSimpleDataRecord = (SimpleDataRecordType) encode;
		final AnyScalarPropertyType field1 = xbSimpleDataRecord.getFieldArray(0);
		
		assertThat(xbSimpleDataRecord.getFieldArray().length, is(1));
		assertThat(field1.getName(), is(name));
		assertThat(field1.isSetCount(), is(TRUE));
		assertThat(field1.getCount().getValue().intValue(), is(value));
	}
	
	@Test
	public void should_encode_simpleDatarecord_with_fieldQuantity() throws OwsExceptionReport
	{
		final String name = "field-1";
		final double value = 42.5;
		
		final XmlObject encode = new SweCommonEncoderv101().encode(
				new SosSweSimpleDataRecord()
				.addField(
						new SosSweField(name,
								new SosSweQuantity()
						.setValue(value)
						))
						);
		
		assertThat(encode,instanceOf(SimpleDataRecordType.class));
		
		final SimpleDataRecordType xbSimpleDataRecord = (SimpleDataRecordType) encode;
		final AnyScalarPropertyType field1 = xbSimpleDataRecord.getFieldArray(0);
		
		assertThat(xbSimpleDataRecord.getFieldArray().length, is(1));
		assertThat(field1.getName(), is(name));
		assertThat(field1.isSetQuantity(), is(TRUE));
		assertThat(field1.getQuantity().getValue(), is(value));
	}
	
	@Test
	public void should_encode_simpleDatarecord_with_fieldTime() throws OwsExceptionReport
	{
		final String name = "field-1";
		final DateTime value = new DateTime();
		
		final XmlObject encode = new SweCommonEncoderv101().encode(
				new SosSweSimpleDataRecord()
				.addField(
						new SosSweField(name,
								new SosSweTime()
						.setValue(value)
						))
						);
		
		assertThat(encode,instanceOf(SimpleDataRecordType.class));
		
		final SimpleDataRecordType xbSimpleDataRecord = (SimpleDataRecordType) encode;
		final AnyScalarPropertyType field1 = xbSimpleDataRecord.getFieldArray(0);
		
		assertThat(xbSimpleDataRecord.getFieldArray().length, is(1));
		assertThat(field1.getName(), is(name));
		assertThat(field1.isSetTime(), is(TRUE));
		assertThat(field1.getTime().getValue().toString(), is(value.toString()));
	}
	
	@Test(expected=NoApplicableCodeException.class)
	public void should_throw_exception_if_received_simpleDataRecord_with_field_with_null_element()
			throws OwsExceptionReport{
		new SweCommonEncoderv101().encode(
				new SosSweSimpleDataRecord()
				.addField(new SosSweField("field-name",null))
				);
	}
}
