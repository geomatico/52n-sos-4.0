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
package org.n52.sos.ogc.om.features.samplingFeatures;


/**
 * Implementation for sam:SamplingFeatureComplex
 * 
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * 
 * @since 4.0.0
 */
public class SamplingFeatureComplex {

	private final String relatedSamplingFeatureRole;
	private final SamplingFeature relatedSamplingFeature;

	public SamplingFeatureComplex(final String relatedSamplingFeatureRole, final SamplingFeature relatedSamplingFeature) {
		if (relatedSamplingFeatureRole == null || relatedSamplingFeatureRole.isEmpty()) {
			throw new IllegalArgumentException("relatedSamplingFeatureRole is required.");
		}
		if (relatedSamplingFeature == null || !relatedSamplingFeature.isSetIdentifier()) {
			throw new IllegalArgumentException("relatedSamplingFeature is required and MUST have set at least an identifier.");
		}
		this.relatedSamplingFeatureRole = relatedSamplingFeatureRole;
		this.relatedSamplingFeature = relatedSamplingFeature;
	}

	public String getRelatedSamplingFeatureRole()
	{
		return relatedSamplingFeatureRole;
	}

	public SamplingFeature getRelatedSamplingFeature()
	{
		return relatedSamplingFeature;
	}
	
}
