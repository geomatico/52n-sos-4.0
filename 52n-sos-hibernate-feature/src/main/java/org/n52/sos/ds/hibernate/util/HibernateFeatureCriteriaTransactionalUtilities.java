/**
 * Copyright (C) 2012
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
package org.n52.sos.ds.hibernate.util;

import org.hibernate.Session;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.JTSHelper;
import org.n52.sos.util.SosHelper;

public class HibernateFeatureCriteriaTransactionalUtilities {

    public static void insertFeatureOfInterest(SosSamplingFeature samplingFeature, Session session) {
        FeatureOfInterest feature =
                HibernateFeatureCriteriaQueryUtilities.getFeatureOfInterest(samplingFeature.getIdentifier(), samplingFeature.getGeometry(), session);
        if (feature == null) {
            feature = new FeatureOfInterest();
            if (samplingFeature.getIdentifier() != null && !samplingFeature.getIdentifier().isEmpty()) {
                feature.setIdentifier(samplingFeature.getIdentifier());
            }
            if (samplingFeature.isSetNames()) {
                feature.setName(SosHelper.createCSVFromList(samplingFeature.getName()));
            }
            if (samplingFeature.getGeometry() != null && !samplingFeature.getGeometry().isEmpty()) {
                // TODO: transform to default EPSG
                if (Configurator.getInstance().switchCoordinatesForEPSG(samplingFeature.getGeometry().getSRID())) {
                    feature.setGeom(JTSHelper.switchCoordinate4Geometry(samplingFeature.getGeometry()));
                } else {
                    feature.setGeom(samplingFeature.getGeometry());
                }
            }
            if (samplingFeature.getXmlDescription() != null && !samplingFeature.getXmlDescription().isEmpty()) {
                feature.setDescriptionXml(samplingFeature.getXmlDescription());
            }
            if (samplingFeature.getFeatureType() != null && !samplingFeature.getFeatureType().isEmpty()) {
                feature.setFeatureOfInterestType(HibernateCriteriaQueryUtilities.getFeatureOfInterestTypeObject(samplingFeature.getFeatureType(), session));
            }
            if (samplingFeature.getSampledFeatures() != null && !samplingFeature.getSampledFeatures().isEmpty()) {
                // TODO: create relationship
            }
            Long id = (Long) session.save(feature);
            session.flush();
            feature.setFeatureOfInterestId(id);
            session.update(feature);
            session.flush();
        }
    }

}
