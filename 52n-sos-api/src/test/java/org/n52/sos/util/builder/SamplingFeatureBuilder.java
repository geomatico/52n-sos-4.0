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
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class SamplingFeatureBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(SamplingFeatureBuilder.class);
	
	public static SamplingFeatureBuilder aSamplingFeature()
	{
		return new SamplingFeatureBuilder();
	}

	private String featureIdentifier;
	private String codespace;
	private double xCoord = Integer.MIN_VALUE;
	private double yCoord = Integer.MIN_VALUE;
	private int epsgCode = Integer.MIN_VALUE;
	private String featureType;

	public SamplingFeatureBuilder setIdentifier(String featureIdentifier)
	{
		this.featureIdentifier = featureIdentifier;
		return this;
	}
	
	public SamplingFeatureBuilder setIdentifierCodeSpace(String codespace)
	{
		this.codespace = codespace;
		return this;
	}

	public SamplingFeatureBuilder setGeometry(double yCoord,
			double xCoord,
			int epsgCode)
	{
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.epsgCode = epsgCode;
		return this;
	}

	public SosAbstractFeature build()
	{
		SosSamplingFeature feature = new SosSamplingFeature(new CodeWithAuthority(featureIdentifier));
		if (codespace != null && !codespace.isEmpty())
		{
			feature.getIdentifier().setCodeSpace(codespace);
		}
		if (xCoord != Integer.MIN_VALUE && yCoord != Integer.MIN_VALUE && epsgCode != Integer.MIN_VALUE)
		{
			try {
				Geometry geom = new WKTReader().read(String.format("POINT ( %s %s )", xCoord, yCoord));
				geom.setSRID(epsgCode);
				feature.setGeometry(geom);
			} catch (ParseException e) {
				LOGGER.error(String.format("Exception thrown: %s",
						e.getMessage()),
						e);
			}
		}
		if (featureType != null && !featureType.isEmpty())
		{
			feature.setFeatureType(featureType);
		}
		return feature;
	}

	public SamplingFeatureBuilder setFeatureType(String featureType)
	{
		this.featureType = featureType;
		return this;
	}

}
