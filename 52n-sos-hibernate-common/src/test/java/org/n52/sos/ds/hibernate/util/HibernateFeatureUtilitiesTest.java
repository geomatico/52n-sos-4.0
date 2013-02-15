/*
 * Copyright (C) 2013 52north.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.n52.sos.ds.hibernate.util;

import static org.junit.Assert.*;

import static org.hamcrest.core.Is.is;

import org.junit.Test;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterestType;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.util.builder.SamplingFeatureBuilder;

import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class HibernateFeatureUtilitiesTest {

    @Test
    public void shouldCreateValidModelDomainFeature() {
        final String id = "id";
        final String type = SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT;
        FeatureOfInterest feature = create(1, id, null, "name", "url", createFeatureOfInterestType(1, type));
        String version = Sos2Constants.SERVICEVERSION;
        SosAbstractFeature result = HibernateFeatureUtilities.createSosAbstractFeatureFromResult(feature, version);
        final SosAbstractFeature expectedResult = SamplingFeatureBuilder.aSamplingFeature().setFeatureType(
                type).setIdentifier(id).build();
        assertThat(expectedResult, is(result));
    }

    FeatureOfInterest create(long id, String identifier, Geometry geom, String name, String url,
                             FeatureOfInterestType type) {
        FeatureOfInterest featureOfInterest = new FeatureOfInterest();
        featureOfInterest.setIdentifier(identifier);
        featureOfInterest.setFeatureOfInterestId(id);
        featureOfInterest.setName(name);
        featureOfInterest.setGeom(geom);
        featureOfInterest.setUrl(url);
        featureOfInterest.setFeatureOfInterestType(type);
        return featureOfInterest;
    }

    private FeatureOfInterestType createFeatureOfInterestType(int id, String type) {
        FeatureOfInterestType featureOfInterestType = new FeatureOfInterestType();
        featureOfInterestType.setFeatureOfInterestTypeId(id);
        featureOfInterestType.setFeatureOfInterestType(type);
        return featureOfInterestType;
    }
}
