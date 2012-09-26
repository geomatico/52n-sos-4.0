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

/**
 * Constants class for Hibernate constants. Include SQL query statements, table
 * and column names, ...
 * 
 */
public class HibernateConstants {

    // SQL query statements
    public static final String SELECT = "SELECT ";

    public static final String DISTINCT = " DISTINCT ";

    public static final String FROM = " FROM ";

    public static final String WHERE = " WHERE ";

    public static final String AND = " AND ";

    public static final String OR = " OR ";

    // table and column names
    // TODO: make this constants configurable
    public static final String PARAMETER_IDENTIFIER = "identifier";

    public static final String PARAMETER_GEOMETRY = "geom";

    public static final String PARAMETER_FEATURE_OF_INTEREST = "featureOfInterest";

    public static final String PARAMETER_FEATURE_OF_INTEREST_ID = "featureOfInterestId";

    public static final String PARAMETER_PROCEDURE = "procedure";

    public static final String PARAMETER_OBSERVABLE_PROPERTY = "observableProperty";

    public static final String PARAMETER_OBSERVATION_CONSTELLATION = "observationConstellation";

    public static final String PARAMETER_PHENOMENON_TIME_START = "phenomenonTimeStart";

    public static final String PARAMETER_PHENOMENON_TIME_END = "phenomenonTimeEnd";

    public static final String PARAMETER_OBSERVATION = "observation";

    public static final String PARAMETER_OBSERVATIONS = "observations";

    public static final String PARAMETER_OBSERVATION_TYPE = "observationType";
    
    public static final String PARAMETER_PROCEDURE_DESCRIPTION_FORMAT = "procedureDescriptionFormat";

    public static final String PARAMETER_UNIT = "unit";

    public static final String PARAMETER_RELATED_FEATURE_ROLE = "relatedFeatureRole";

    public static final String PARAMETER_RESULT_TYPE = "resultType";

    public static final String PARAMETER_VALUE_TYPE = "valueType";

    public static final String PARAMETER_FEATURE_OF_INTEREST_TYPE = "featureOfInterestType";
    
    public static final String PARAMETER_FEATURE_OF_INTEREST_TYPES = "featureOfInterestTypes";

    public static final String PARAMETER_VALUE = "value";

    public static final String PARAMETER_RELATED_FEATURES = "relatedFeatures";

    public static final String PARAMETER_OFFERING = "offering";

    public static final String PARAMETER_OFFERINGS = "offerings";

    public static final String PARAMETER_OBSERVATION_ID = "observationId";

    public static final String PARAMETER_PROCEDURES = "procedures";

}
