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
package org.n52.sos.ogc.om.features;

import static java.lang.Boolean.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.om.features.samplingFeatures.SamplingFeature;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class FeatureCollectionTest {

	@Test
	public final void should_remove_member_from_feature_collection()
	{
		final FeatureCollection features = new FeatureCollection();
		final String feature1Id = "feature-1";
		final SamplingFeature feature1 = new SamplingFeature(new CodeWithAuthority(feature1Id));
		features.addMember(feature1);
		final String feature2Id = "feature-2";
		final SamplingFeature feature2 = new SamplingFeature(new CodeWithAuthority(feature2Id));
		features.addMember(feature2);
		
		final SamplingFeature removedFeature = (SamplingFeature) features.removeMember(feature2Id);
		
		assertThat(removedFeature, is(equalTo(feature2)));
		assertThat(features.getMembers().size(), is(1));
		assertThat(features.getMembers().containsKey(feature2Id), is(FALSE));
		assertThat(features.getMembers().containsValue(feature2), is(FALSE));
		assertThat(features.getMembers().containsKey(feature1Id), is(TRUE));
		assertThat(features.getMembers().containsValue(feature1), is(TRUE));
	}

}
