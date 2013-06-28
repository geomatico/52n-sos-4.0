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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.n52.sos.ogc.gml.CodeWithAuthority;


/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class SamplingFeatureComplexTest {
	
	
	@Test(expected=IllegalArgumentException.class)
	public void constructor_should_throw_exception_when_role_is_not_provided_1()
	{
		new SamplingFeatureComplex(null, new SamplingFeature(new CodeWithAuthority("test-feature")));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void constructor_should_throw_exception_when_role_is_not_provided_2()
	{
		new SamplingFeatureComplex("", new SamplingFeature(new CodeWithAuthority("test-feature")));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void constructor_should_throw_exception_when_feature_is_not_provided_1()
	{
		new SamplingFeatureComplex("test-role", null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void constructor_should_throw_exception_when_feature_is_not_provided_2()
	{
		new SamplingFeatureComplex("test-role", new SamplingFeature(null));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void constructor_should_throw_exception_when_feature_is_not_provided_3()
	{
		new SamplingFeatureComplex(null, new SamplingFeature(new CodeWithAuthority(null)));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void constructor_should_throw_exception_when_feature_is_not_provided_4()
	{
		new SamplingFeatureComplex(null, new SamplingFeature(new CodeWithAuthority("")));
	}
	
	@Test
	public void should_set_role_correct()
	{
		final String role = "test-role";
		final SamplingFeatureComplex sfc = new SamplingFeatureComplex(role, new SamplingFeature(new CodeWithAuthority("test-feature")));
		
		assertThat(sfc.getRelatedSamplingFeatureRole(), is(role));
	}
	
	@Test
	public void should_set_relatedSamplingFeature_correct()
	{
		final SamplingFeature feature = new SamplingFeature(new CodeWithAuthority("test-feature"));
		final SamplingFeatureComplex sfc = new SamplingFeatureComplex("test-role", feature);
		
		assertThat(sfc.getRelatedSamplingFeature(), is(feature));
	}
	
}
