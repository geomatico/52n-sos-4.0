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
package org.n52.sos.ds.hibernate.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.util.SosHelper;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class HibernateFeatureUtilities {

    /**
     * Creates a SOS feature from the FeatureOfInterest object
     *
     * @param feature
     * FeatureOfInterest object
     * @param version
     * SOS version
     * <p/>
     * @return SOS feature
     * <p/>
     */
    public static SosAbstractFeature createSosAbstractFeatureFromResult(FeatureOfInterest feature,
                                                                        String version) {
        String checkedFoiID = null;
        if (SosHelper.checkFeatureOfInterestIdentifierForSosV2(feature.getIdentifier(), version)) {
            checkedFoiID = feature.getIdentifier();
        }
        SosSamplingFeature sampFeat = new SosSamplingFeature(new CodeWithAuthority(checkedFoiID));
        if (feature.getName() != null && !feature.getName().isEmpty()) {
            sampFeat.setName(SosHelper.createCodeTypeListFromCSV(feature.getName()));
        }
        sampFeat.setDescription(null);
        sampFeat.setGeometry(feature.getGeom());
        if (feature.getGeom() != null) {
            sampFeat.setEpsgCode(feature.getGeom().getSRID());
        }
        sampFeat.setFeatureType(feature.getFeatureOfInterestType().getFeatureOfInterestType());
        sampFeat.setUrl(feature.getUrl());
        sampFeat.setXmlDescription(feature.getDescriptionXml());
        Set<FeatureOfInterest> parentFeatures = feature.getFeatureOfInterestsForParentFeatureId();
        if (parentFeatures != null && !parentFeatures.isEmpty()) {
            List<SosAbstractFeature> sampledFeatures = new ArrayList<SosAbstractFeature>(parentFeatures.size());
            for (FeatureOfInterest parentFeature : parentFeatures) {
                sampledFeatures.add(createSosAbstractFeatureFromResult(parentFeature, version));
            }
            sampFeat.setSampledFeatures(sampledFeatures);
        }
        return sampFeat;
    }

    private HibernateFeatureUtilities() {
    }
}
