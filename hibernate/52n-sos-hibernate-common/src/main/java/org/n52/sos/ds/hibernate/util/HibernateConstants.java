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

import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasCodespace;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasDeletedFlag;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasFeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasFeatureOfInterestType;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasFeatureOfInterestTypes;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasGeometry;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasIdentifier;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasObservableProperty;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasObservationConstellation;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasObservationConstellations;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasObservationType;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasOffering;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasOfferings;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasProcedure;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasProcedureDescriptionFormat;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasRelatedFeatures;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasUnit;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasValue;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.RelatedFeatureRole;

/**
 * Constants class for Hibernate constants. Include SQL query statements, table
 * and column names, ...
 */
public interface HibernateConstants {
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_SET_ID = Observation.SET_ID;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_IDENTIFIER = HasIdentifier.IDENTIFIER;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_GEOMETRY = HasGeometry.GEOMETRY;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_FEATURE_OF_INTEREST = HasFeatureOfInterest.FEATURE_OF_INTEREST;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_FEATURE_OF_INTEREST_ID = FeatureOfInterest.ID;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_PROCEDURE = HasProcedure.PROCEDURE;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_OBSERVABLE_PROPERTY = HasObservableProperty.OBSERVABLE_PROPERTY;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_OBSERVATION_CONSTELLATION = HasObservationConstellation.OBSERVATION_CONSTELLATION;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_OBSERVATION_CONSTELLATIONS = HasObservationConstellations.OBSERVATION_CONSTELLATIONS;
    /**
     * @deprecated no entity has this relation
     */
    @Deprecated
    String PARAMETER_OBSERVATION_CONSTELLATION_OFFERING_OBSERVATION_TYPE =
           "observationConstellationOfferingObservationType";
    /**
     * @deprecated no entity has this relation
     */
    @Deprecated
    String PARAMETER_OBSERVATION_CONSTELLATION_OFFERING_OBSERVATION_TYPES =
           "observationConstellationOfferingObservationTypes";
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_OBSERVATION_CONSTELLATIONS_ID = ObservationConstellation.ID;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_PHENOMENON_TIME_START = Observation.PHENOMENON_TIME_START;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_PHENOMENON_TIME_END = Observation.PHENOMENON_TIME_END;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_RESULT_TIME = Observation.RESULT_TIME;
    /**
     * @deprecated no entity has this relation
     */
    @Deprecated
    String PARAMETER_OBSERVATION = "observation";
    /**
     * @deprecated no entity has this relation
     */
    @Deprecated
    String PARAMETER_OBSERVATIONS = "observations";
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_OBSERVATION_TYPE = HasObservationType.OBSERVATION_TYPE;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_PROCEDURE_DESCRIPTION_FORMAT = HasProcedureDescriptionFormat.PROCEDURE_DESCRIPTION_FORMAT;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_UNIT = HasUnit.UNIT;
    /**
     * @deprecated use {@link #PARAMETER_DELETED}
     */
    @Deprecated
    String DELETED = HasDeletedFlag.DELETED;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_RELATED_FEATURE_ROLE = RelatedFeatureRole.RELATED_FEATURE_ROLE;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_CODESPACE = HasCodespace.CODESPACE;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_DELETED = HasDeletedFlag.DELETED;
    /**
     * @deprecated no entity has this relation
     */
    @Deprecated
    String PARAMETER_RESULT_TYPE = "resultType";
    /**
     * @deprecated no entity has this relation
     */
    @Deprecated
    String PARAMETER_VALUE_TYPE = "valueType";
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_FEATURE_OF_INTEREST_TYPE = HasFeatureOfInterestType.FEATURE_OF_INTEREST_TYPE;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_FEATURE_OF_INTEREST_TYPES = HasFeatureOfInterestTypes.FEATURE_OF_INTEREST_TYPES;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_VALUE = HasValue.VALUE;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_RELATED_FEATURES = HasRelatedFeatures.RELATED_FEATURES;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_OFFERING = HasOffering.OFFERING;
    /**
     * @deprecated use the constant in the respective entity
     */
    @Deprecated
    String PARAMETER_OFFERINGS = HasOfferings.OFFERINGS;
    /**
     * @deprecated not used
     */
    @Deprecated
    String PARAMETER_OBSERVATION_ID = Observation.ID;
    /**
     * @deprecated no entity has this relation
     */
    @Deprecated
    String PARAMETER_PROCEDURES = "procedures";
    /**
     * @deprecated no entity has this relation
     */
    @Deprecated
    String PARAMETER_BOOLEAN_VALUES = "booleanValues";
    /**
     * @deprecated no entity has this relation
     */
    @Deprecated
    String PARAMETER_CATEGORY_VALUES = "categoryValues";
    /**
     * @deprecated no entity has this relation
     */
    @Deprecated
    String PARAMETER_COUNT_VALUES = "countValues";
    /**
     * @deprecated no entity has this relation
     */
    @Deprecated
    String PARAMETER_NUMERIC_VALUES = "numericValues";
    /**
     * @deprecated no entity has this relation
     */
    @Deprecated
    String PARAMETER_GEOMETRY_VALUES = "geometryValues";
    /**
     * @deprecated no entity has this relation
     */
    @Deprecated
    String PARAMETER_TEXT_VALUES = "textValues";
}
