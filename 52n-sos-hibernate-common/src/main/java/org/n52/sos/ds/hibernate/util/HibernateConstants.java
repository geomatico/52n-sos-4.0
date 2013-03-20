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

/**
 * Constants class for Hibernate constants. Include SQL query statements, table
 * and column names, ...
 * 
 */
public interface HibernateConstants {

    // table and column names
    // TODO: make this constants configurable
	String PARAMETER_SET_ID = "setId";
	
    String PARAMETER_IDENTIFIER = "identifier";

    String PARAMETER_GEOMETRY = "geom";

    String PARAMETER_FEATURE_OF_INTEREST = "featureOfInterest";

    String PARAMETER_FEATURE_OF_INTEREST_ID = "featureOfInterestId";

    String PARAMETER_PROCEDURE = "procedure";

    String PARAMETER_OBSERVABLE_PROPERTY = "observableProperty";

    String PARAMETER_OBSERVATION_CONSTELLATION = "observationConstellation";
    
    String PARAMETER_OBSERVATION_CONSTELLATIONS = "observationConstellations";

    String PARAMETER_OBSERVATION_CONSTELLATION_OFFERING_OBSERVATION_TYPE = "observationConstellationOfferingObservationType";

    String PARAMETER_OBSERVATION_CONSTELLATION_OFFERING_OBSERVATION_TYPES = "observationConstellationOfferingObservationTypes";

    String PARAMETER_OBSERVATION_CONSTELLATIONS_ID = "observationConstellationId";

    String PARAMETER_PHENOMENON_TIME_START = "phenomenonTimeStart";

    String PARAMETER_PHENOMENON_TIME_END = "phenomenonTimeEnd";
    
    String PARAMETER_RESULT_TIME = "resultTime";

    String PARAMETER_OBSERVATION = "observation";

    String PARAMETER_OBSERVATIONS = "observations";

    String PARAMETER_OBSERVATION_TYPE = "observationType";
    
    String PARAMETER_PROCEDURE_DESCRIPTION_FORMAT = "procedureDescriptionFormat";

    String PARAMETER_UNIT = "unit";
	
	String DELETED = "deleted";

    String PARAMETER_RELATED_FEATURE_ROLE = "relatedFeatureRole";

    String PARAMETER_RESULT_TYPE = "resultType";

    String PARAMETER_VALUE_TYPE = "valueType";

    String PARAMETER_FEATURE_OF_INTEREST_TYPE = "featureOfInterestType";
    
    String PARAMETER_FEATURE_OF_INTEREST_TYPES = "featureOfInterestTypes";

    String PARAMETER_VALUE = "value";

    String PARAMETER_RELATED_FEATURES = "relatedFeatures";

    String PARAMETER_OFFERING = "offering";

    String PARAMETER_OFFERINGS = "offerings";

    String PARAMETER_OBSERVATION_ID = "observationId";

    String PARAMETER_PROCEDURES = "procedures";
    
    String PARAMETER_BOOLEAN_VALUES = "booleanValues";

    String PARAMETER_CATEGORY_VALUES = "categoryValues";

    String PARAMETER_COUNT_VALUES = "countValues";

    String PARAMETER_NUMERIC_VALUES = "numericValues";

    String PARAMETER_GEOMETRY_VALUES = "geometryValues";

    String PARAMETER_TEXT_VALUES = "textValues";

	String PARAMETER_SETTING_KEY = "key";

	String PARAMETER_SETTING_VALUE = "value";
	

}
