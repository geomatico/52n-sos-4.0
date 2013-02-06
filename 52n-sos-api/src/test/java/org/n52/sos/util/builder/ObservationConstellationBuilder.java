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

import org.n52.sos.ogc.om.SosObservableProperty;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.sos.SosProcedureDescription;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class ObservationConstellationBuilder {
	
	public static ObservationConstellationBuilder anObservationConstellation()
	{
		return new ObservationConstellationBuilder();
	}

	private SosAbstractFeature featureOfInterest;
	private SosProcedureDescription procedure;
	private String observationType;
	private SosObservableProperty observableProperty;

	public ObservationConstellationBuilder setFeature(SosAbstractFeature featureOfInterest)
	{
		this.featureOfInterest = featureOfInterest;
		return this;
	}

	public ObservationConstellationBuilder setProcedure(SosProcedureDescription procedure)
	{
		this.procedure = procedure;
		return this;
	}

	public ObservationConstellationBuilder setObservationType(String observationType)
	{
		this.observationType = observationType;
		return this;
	}

	public ObservationConstellationBuilder setObservableProperty(SosObservableProperty observableProperty)
	{
		this.observableProperty = observableProperty;
		return this;
	}

	public SosObservationConstellation build()
	{
		SosObservationConstellation sosObservationConstellation = new SosObservationConstellation();
		sosObservationConstellation.setFeatureOfInterest(featureOfInterest);
		sosObservationConstellation.setObservableProperty(observableProperty);
		sosObservationConstellation.setObservationType(observationType);
		sosObservationConstellation.setProcedure(procedure);
		return sosObservationConstellation;
	}

}
