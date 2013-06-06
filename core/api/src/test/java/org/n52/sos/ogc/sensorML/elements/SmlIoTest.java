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
import org.n52.sos.ogc.swe.simpleType.SweBoolean;


/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 * @since 4.0.0
 */
public class SmlIoTest {

	@Test
	public void should_return_false_if_ioValue_is_not_set()
	{
		final SmlIo<?> smlIo = new SmlIo<Object>();
		assertThat(smlIo.isSetValue(), is(FALSE));
	}
	
	@Test
	public void should_return_true_if_ioValue_is_set()
	{
		final SweBoolean ioValue = new SweBoolean();
		final SmlIo<Boolean> smlIo = new SmlIo<Boolean>(ioValue);
		assertThat(smlIo.isSetValue(), is(TRUE));
	}
	
	@Test
	public void should_return_false_if_ioName_is_not_set()
	{
		final SmlIo<?> smlIo = new SmlIo<Object>();
		assertThat(smlIo.isSetName(), is(FALSE));
		
		smlIo.setIoName("");
		assertThat(smlIo.isSetName(), is(FALSE));
	}
	
	@Test
	public void should_return_true_if_ioName_is_set()
	{
		final SmlIo<Boolean> smlIo = new SmlIo<Boolean>();
		final String inputName = "inputName";
		smlIo.setIoName(inputName);
		assertThat(smlIo.isSetName(), is(TRUE));
	}
}
