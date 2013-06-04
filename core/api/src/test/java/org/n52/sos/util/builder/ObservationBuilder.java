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
package org.n52.sos.util.builder;

import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.om.ObservationValue;
import org.n52.sos.ogc.om.OmObservation;
import org.n52.sos.ogc.om.OmObservationConstellation;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class ObservationBuilder {
	
	public static ObservationBuilder anObservation()
	{
		return new ObservationBuilder();
	}

	private OmObservationConstellation observationConstellation;
	private ObservationValue<?> value;
	private String identifer;
	private String identiferCodespace;

	public ObservationBuilder setObservationConstellation(OmObservationConstellation observationConstellation)
	{
		this.observationConstellation = observationConstellation;
		return this;
	}

	public OmObservation build()
	{
		OmObservation sosObservation = new OmObservation();
		if (observationConstellation != null)
		{
			sosObservation.setObservationConstellation(observationConstellation);
		}
		if (value != null)
		{
			sosObservation.setValue(value);
		}
		if (identifer != null && identiferCodespace != null)
		{
			sosObservation.setIdentifier(new CodeWithAuthority(identifer,identiferCodespace));
		}
		return sosObservation;
	}

	public ObservationBuilder setValue(ObservationValue<?> observationValue)
	{
		this.value = observationValue;
		return this;
	}

	public ObservationBuilder setIdentifier(String identifierCodeSpace,
			String identifier)
	{
		this.identifer = identifier;
		this.identiferCodespace = identifierCodeSpace;
		return this;
	}

}
