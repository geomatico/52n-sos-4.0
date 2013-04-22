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
package org.n52.sos.ds.hibernate.util;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ProcedureDescriptionFormat;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.System;
import org.n52.sos.ogc.sos.SosProcedureDescription;


/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 * @since 4.0.0
 */
public class HibernateProcedureConverterTest {

	private HibernateProcedureConverter converter;
	
	@Before
	public void initInstance()
	{
		converter = new HibernateProcedureConverter();
	}
	@Test(expected=NoApplicableCodeException.class)
	public void should_throw_exception_with_null_parameters() throws OwsExceptionReport
	{
		converter.createSosProcedureDescription(null, null, null);
	}
	
	@Ignore
	@Test
	public void should_return_sml_system_for_spatial_procedure() throws OwsExceptionReport
	{
		final SosProcedureDescription description = converter.createSosProcedureDescription((Procedure)(new Procedure()
				.setProcedureDescriptionFormat(new ProcedureDescriptionFormat().
						setProcedureDescriptionFormat(SensorMLConstants.NS_SML))
    			.setSrid(4326)
    			.setAltitude(42.0)
    			.setLongitude(7.2)
    			.setLatitude(52.0)), 
			"test-procedure-identifier",
			SensorMLConstants.NS_SML);
		assertThat(description, is(instanceOf(System.class)));
	}
	

}
