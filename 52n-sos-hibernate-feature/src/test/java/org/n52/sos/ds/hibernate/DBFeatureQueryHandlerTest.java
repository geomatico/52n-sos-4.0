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
package org.n52.sos.ds.hibernate;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.n52.sos.util.ReverseOf.reverseOf;

import org.junit.Before;
import org.junit.Test;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterestType;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.config.ConfigurationException;
import org.n52.sos.util.JTSHelper;
import org.n52.sos.util.JTSHelperTest;
import org.n52.sos.util.builder.SamplingFeatureBuilder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class DBFeatureQueryHandlerTest extends DBFeatureQueryHandler {

    @Before
    public void setUp() throws ConfigurationException {
        setDefaultEpsg(FeatureQuerySettingsProvider.DEFAULT_EPSG_DEFINITION.getDefaultValue());
        setEpsgCodesWithReversedAxisOrder(FeatureQuerySettingsProvider.EPSG_CODES_WITH_REVERSED_AXIS_ORDER_DEFINITION
                .getDefaultValue());
    }

    @Test
    public void shouldCreateValidModelDomainFeature() throws OwsExceptionReport {
        final String id = "id";
        final String type = SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT;
        FeatureOfInterest feature = create(1, id, null, "name", "url", createFeatureOfInterestType(1, type));
        String version = Sos2Constants.SERVICEVERSION;
        SosAbstractFeature result = createSosAbstractFeature(feature, version);
        final SosAbstractFeature expectedResult = SamplingFeatureBuilder.aSamplingFeature().setFeatureType(
                type).setIdentifier(id).build();
        assertThat(expectedResult, is(result));
    }

    public FeatureOfInterest create(long id, String identifier, Geometry geom, String name, String url,
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

    @Test
    public void shouldSwitchCoordinatesForEpsg4326() throws OwsExceptionReport {
        GeometryFactory factory = JTSHelper.getGeometryFactoryForSRID(4326);
        Geometry geometry = factory.createPoint(JTSHelperTest.randomCoordinate());
        Geometry switched = switchCoordinateAxisOrderIfNeeded(geometry);

        assertThat(isAxisOrderSwitchRequired(4326), is(true));
        assertThat(switched, is(notNullValue()));
        assertThat(switched, is(instanceOf(geometry.getClass())));
        assertThat(switched, is(not(sameInstance(geometry))));
        assertThat(switched, is(reverseOf(geometry)));
    }

    @Test
    public void shouldSwitchCoordinatesForSosAbstractFeature() throws OwsExceptionReport {
        GeometryFactory factory = JTSHelper.getGeometryFactoryForSRID(4326);
        Geometry geometry = factory.createPoint(JTSHelperTest.randomCoordinate());
        FeatureOfInterest feature = create(1, "id", geometry, "name", "url", createFeatureOfInterestType(1, "type"));
        SosAbstractFeature sosFeature = createSosAbstractFeature(feature, Sos2Constants.SERVICEVERSION);

        assertThat(isAxisOrderSwitchRequired(4326), is(true));
        assertThat(sosFeature, is(notNullValue()));
        assertThat(sosFeature, is(instanceOf(SosSamplingFeature.class)));

        SosSamplingFeature ssf = (SosSamplingFeature) sosFeature;

        assertThat(ssf.getGeometry(), is(notNullValue()));
        assertThat(ssf.getGeometry(), is(instanceOf(geometry.getClass())));
        assertThat(ssf.getGeometry(), is(not(sameInstance(geometry))));
        assertThat(ssf.getGeometry(), is(reverseOf(geometry)));
    }

    @Test
    public void shouldNotSwitchCoordinatesForEpsg2181() throws OwsExceptionReport {
        GeometryFactory factory = JTSHelper.getGeometryFactoryForSRID(2181);
        Geometry geometry = factory.createPoint(JTSHelperTest.randomCoordinate());
        Geometry switched = switchCoordinateAxisOrderIfNeeded(geometry);

        assertThat(isAxisOrderSwitchRequired(2181), is(false));
        assertThat(switched, is(notNullValue()));
        assertThat(switched, is(instanceOf(geometry.getClass())));
        assertThat(switched, is(sameInstance(geometry)));
        assertThat(switched, is(not(reverseOf(geometry))));
    }

    @Test
    public void shouldNotSwitchCoordinatesForSosAbstractFeature() throws OwsExceptionReport {
        GeometryFactory factory = JTSHelper.getGeometryFactoryForSRID(2181);
        Geometry geometry = factory.createPoint(JTSHelperTest.randomCoordinate());

        assertThat(isAxisOrderSwitchRequired(2181), is(false));

        FeatureOfInterest feature = create(1, "id", geometry, "name", "url", createFeatureOfInterestType(1, "type"));
        SosAbstractFeature sosFeature = createSosAbstractFeature(feature, Sos2Constants.SERVICEVERSION);

        assertThat(isAxisOrderSwitchRequired(4326), is(true));
        assertThat(sosFeature, is(notNullValue()));
        assertThat(sosFeature, is(instanceOf(SosSamplingFeature.class)));

        SosSamplingFeature ssf = (SosSamplingFeature) sosFeature;

        assertThat(ssf.getGeometry(), is(notNullValue()));
        assertThat(ssf.getGeometry(), is(instanceOf(geometry.getClass())));
        assertThat(ssf.getGeometry(), is(sameInstance(geometry)));
        assertThat(ssf.getGeometry(), is(not(reverseOf(geometry))));
    }
}
