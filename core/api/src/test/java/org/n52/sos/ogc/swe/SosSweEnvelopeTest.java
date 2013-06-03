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
package org.n52.sos.ogc.swe;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.ogc.swe.SWEConstants.SweCoordinateName;
import org.n52.sos.ogc.swe.simpleType.SweQuantity;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 * @since 4.0.0
 */
public class SosSweEnvelopeTest {

	@Test public final void 
	should_create_valid_sosSweEnvelope_from_sosEnvelope()
	{
		final int srid = 52;
		final double x1 = 1;
		final double y1 = 2;
		final double y2 = 3;
		final double x2 = 4;
		final String uom = "deg";
		final SosEnvelope sosEnvelope = new SosEnvelope(new Envelope(x1, x2, y1, y2), srid);
		final SweEnvelope sweEnvelope = new SweEnvelope(sosEnvelope,uom);
		
		// srid
		assertThat(sweEnvelope.getReferenceFrame(), is(Integer.toString(srid)));
		// x1
		assertThat((Double)sweEnvelope.getLowerCorner().getCoordinates().get(0).getValue().getValue(), is(new Double(x1)));
		// y1
		assertThat((Double)sweEnvelope.getLowerCorner().getCoordinates().get(1).getValue().getValue(), is(new Double(y1)));
		// x2
		assertThat((Double)sweEnvelope.getUpperCorner().getCoordinates().get(0).getValue().getValue(), is(new Double(x2)));
		// y2
		assertThat((Double)sweEnvelope.getUpperCorner().getCoordinates().get(1).getValue().getValue(), is(new Double(y2)));
		// uom
		assertThat(((SweQuantity)sweEnvelope.getLowerCorner().getCoordinates().get(0).getValue()).getUom(), is(uom));
		assertThat(((SweQuantity)sweEnvelope.getLowerCorner().getCoordinates().get(1).getValue()).getUom(), is(uom));
		assertThat(((SweQuantity)sweEnvelope.getUpperCorner().getCoordinates().get(0).getValue()).getUom(), is(uom));
		assertThat(((SweQuantity)sweEnvelope.getUpperCorner().getCoordinates().get(1).getValue()).getUom(), is(uom));
		// northing
		assertThat(sweEnvelope.getLowerCorner().getCoordinates().get(0).getName(), is(SweCoordinateName.easting));
		assertThat(sweEnvelope.getUpperCorner().getCoordinates().get(0).getName(), is(SweCoordinateName.easting));
		// easting
		assertThat(sweEnvelope.getLowerCorner().getCoordinates().get(1).getName(), is(SweCoordinateName.northing));
		assertThat(sweEnvelope.getUpperCorner().getCoordinates().get(1).getName(), is(SweCoordinateName.northing));
	}

}
