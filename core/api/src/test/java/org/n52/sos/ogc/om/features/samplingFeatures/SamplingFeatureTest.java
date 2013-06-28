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

import static java.lang.Boolean.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.util.CollectionHelper;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * 
 * @since 4.0.0
 */
public class SamplingFeatureTest {

	@Test
	public final void addRelatedSamplingFeature_should_add_a_relatedSamplingFeature()
	{
		final SamplingFeature feature = new SamplingFeature(null);
		final SamplingFeatureComplex relatedSamplingFeature = new SamplingFeatureComplex("test-role", new SamplingFeature(new CodeWithAuthority("test-feature")));
		feature.addRelatedSamplingFeature(relatedSamplingFeature);
		
		assertThat(feature.isSetRelatedSamplingFeatures(), is(TRUE));
		assertThat(feature.getRelatedSamplingFeatures(), hasSize(1));
		assertThat(feature.getRelatedSamplingFeatures().get(0), is(relatedSamplingFeature));
		
		final SamplingFeatureComplex relatedSamplingFeature2 = new SamplingFeatureComplex("test-role", new SamplingFeature(new CodeWithAuthority("test-feature-2")));
		feature.addRelatedSamplingFeature(relatedSamplingFeature2);
		
		validate(feature, relatedSamplingFeature, relatedSamplingFeature2);
		
		feature.addRelatedSamplingFeature(null);
		
		validate(feature, relatedSamplingFeature, relatedSamplingFeature2);
	}

	@Test
	public final void addAllRelatedSamplingFeatures_should_add_all_elements()
	{
		final SamplingFeature feature = new SamplingFeature(null);
		final SamplingFeatureComplex relatedSamplingFeature = new SamplingFeatureComplex("test-role", new SamplingFeature(new CodeWithAuthority("test-feature")));
		final SamplingFeatureComplex relatedSamplingFeature2 = new SamplingFeatureComplex("test-role", new SamplingFeature(new CodeWithAuthority("test-feature-2")));
		
		List<SamplingFeatureComplex> list = CollectionHelper.asList(relatedSamplingFeature, relatedSamplingFeature2);
		
		feature.addAllRelatedSamplingFeatures(list);
		
		validate(feature, relatedSamplingFeature, relatedSamplingFeature2);
		
		final SamplingFeatureComplex relatedSamplingFeature3 = new SamplingFeatureComplex("test-role", new SamplingFeature(new CodeWithAuthority("test-feature-3")));
		final SamplingFeatureComplex relatedSamplingFeature4 = new SamplingFeatureComplex("test-role", new SamplingFeature(new CodeWithAuthority("test-feature-4")));
		
		list = CollectionHelper.asList(relatedSamplingFeature3, relatedSamplingFeature4);
		
		feature.addAllRelatedSamplingFeatures(list);
		
		validate(feature, relatedSamplingFeature, relatedSamplingFeature2, relatedSamplingFeature3, relatedSamplingFeature4);
	}

	@Test
	public final void setRelatedSamplingFeatures_should_set_all_elements_and_reset_if_set_before()
	{
		final SamplingFeature feature = new SamplingFeature(null);
		final SamplingFeatureComplex relatedSamplingFeature = new SamplingFeatureComplex("test-role", new SamplingFeature(new CodeWithAuthority("test-feature")));
		final SamplingFeatureComplex relatedSamplingFeature2 = new SamplingFeatureComplex("test-role", new SamplingFeature(new CodeWithAuthority("test-feature-2")));
		
		List<SamplingFeatureComplex> list = CollectionHelper.asList(relatedSamplingFeature, relatedSamplingFeature2);
		
		feature.setRelatedSamplingFeatures(list);
		
		validate(feature, relatedSamplingFeature, relatedSamplingFeature2);
		
		final SamplingFeatureComplex relatedSamplingFeature3 = new SamplingFeatureComplex("test-role", new SamplingFeature(new CodeWithAuthority("test-feature-3")));
		final SamplingFeatureComplex relatedSamplingFeature4 = new SamplingFeatureComplex("test-role", new SamplingFeature(new CodeWithAuthority("test-feature-4")));
		
		list = CollectionHelper.asList(relatedSamplingFeature3, relatedSamplingFeature4);
		
		feature.setRelatedSamplingFeatures(list);
		
		validate(feature, relatedSamplingFeature3, relatedSamplingFeature4);
	}

	@Test
	public final void isSetRelatedSamplingFeatures_should_return_false_if_not_set()
	{
		final SamplingFeature feature = new SamplingFeature(null);
		assertThat(feature.isSetRelatedSamplingFeatures(), is(FALSE));
		
		feature.addRelatedSamplingFeature(new SamplingFeatureComplex("test-role", new SamplingFeature(new CodeWithAuthority("test-feature"))));
		assertThat(feature.isSetRelatedSamplingFeatures(), is(TRUE));
		
		feature.setRelatedSamplingFeatures(null);
		assertThat(feature.isSetRelatedSamplingFeatures(), is(FALSE));
		
		feature.setRelatedSamplingFeatures(new ArrayList<SamplingFeatureComplex>());
		assertThat(feature.isSetRelatedSamplingFeatures(), is(FALSE));
	}

	private void validate(final SamplingFeature feature,
			final SamplingFeatureComplex... relatedSamplingFeatures)
	{
		assertThat(feature.isSetRelatedSamplingFeatures(), is(TRUE));
		assertThat(feature.getRelatedSamplingFeatures(), hasSize(relatedSamplingFeatures.length));
		for (final SamplingFeatureComplex relatedSamplingFeature : relatedSamplingFeatures) {
			assertThat(feature.getRelatedSamplingFeatures(), hasItem(relatedSamplingFeature));
		}
	}

}
