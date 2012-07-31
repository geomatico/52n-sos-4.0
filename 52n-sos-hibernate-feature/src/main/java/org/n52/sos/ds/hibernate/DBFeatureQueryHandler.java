/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.ds.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Restrictions;
import org.hibernate.spatial.criterion.SpatialProjections;
import org.n52.sos.ds.IFeatureQueryHandler;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.HibernateFeatureCriteriaQueryUtilities;
import org.n52.sos.ogc.filter.SpatialFilter;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OWSConstants.ExceptionLevel;
import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.util.SosHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Feature handler class for features stored in the database
 * 
 */
public class DBFeatureQueryHandler implements IFeatureQueryHandler {

	/**
	 * logger
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DBFeatureQueryHandler.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.sos.ds.IFeatureQueryHandler#getFeatureByID(java.lang.String)
	 */
	@Override
	public SosAbstractFeature getFeatureByID(String featureID,
			Object connection, String version) throws OwsExceptionReport {
		Session session = getSessionFromConnection(connection);
		try {
			Criteria criteria = session.createCriteria(FeatureOfInterest.class);
			// extension
			criteria.add(HibernateCriteriaQueryUtilities
					.getEqualRestriction(
							HibernateConstants.PARAMETER_SAMPLING_FEATURE,
							Boolean.TRUE));
			criteria.add(Restrictions.eq("identifier", featureID));
			return createSosAbstractFeatureFromResult(
					(FeatureOfInterest) criteria.uniqueResult(), version);
		} catch (HibernateException he) {
			String exceptionText = "An error occurs while querying feature data for a featureOfInterest identifier!";
			LOGGER.error(exceptionText, he);
			OwsExceptionReport owse = new OwsExceptionReport(he);
			owse.addCodedException(OwsExceptionCode.NoApplicableCode, null,
					exceptionText);
			throw owse;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.sos.ds.IFeatureQueryHandler#getFeatureIDs(java.util.List,
	 * org.n52.sos.ogc.filter.SpatialFilter)
	 */
	@Override
	public Collection<String> getFeatureIDs(SpatialFilter filter,
			Object connection) throws OwsExceptionReport {
		Session session = getSessionFromConnection(connection);
		Map<String, String> aliases = new HashMap<String, String>();
		List<Criterion> criterions = new ArrayList<Criterion>();
		List<Projection> projections = new ArrayList<Projection>();
		try {
			if (filter != null) {
				String propertyName = HibernateConstants.PARAMETER_GEOMETRY;
				criterions.add(HibernateCriteriaQueryUtilities
						.getCriterionForSpatialFilter(propertyName, filter));
			}
			// extension
			criterions.add(HibernateCriteriaQueryUtilities
					.getEqualRestriction(
							HibernateConstants.PARAMETER_SAMPLING_FEATURE,
							Boolean.TRUE));

			return HibernateCriteriaQueryUtilities
					.getFeatureOfInterestIdentifier(aliases, criterions,
							projections, session);
		} catch (HibernateException he) {
			String exceptionText = "An error occurs while querying feature identifiers for a featureOfInterest identifier!";
			LOGGER.error(exceptionText, he);
			OwsExceptionReport owse = new OwsExceptionReport(he);
			owse.addCodedException(OwsExceptionCode.NoApplicableCode, null,
					exceptionText);
			throw owse;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.sos.ds.IFeatureQueryHandler#getFeatures(java.util.List,
	 * org.n52.sos.ogc.filter.SpatialFilter)
	 */
	@Override
	public Map<String, SosAbstractFeature> getFeatures(List<String> featureIDs,
			List<SpatialFilter> filters, Object connection, String version)
			throws OwsExceptionReport {
		Session session = getSessionFromConnection(connection);
		Map<String, String> aliases = new HashMap<String, String>();
		List<Criterion> criterions = new ArrayList<Criterion>();
		List<Projection> projections = new ArrayList<Projection>();
		try {
			if (featureIDs != null && !featureIDs.isEmpty()) {
				criterions.add(HibernateCriteriaQueryUtilities
						.getDisjunctionCriterionForStringList(
								HibernateConstants.PARAMETER_IDENTIFIER,
								featureIDs));
			}
			if (filters != null && !filters.isEmpty()) {
//				String foiAlias = HibernateCriteriaQueryUtilities
//						.addFeatureOfInterestAliasToMap(aliases, null);
//				String propertyName = foiAlias + "."
//						+  HibernateConstants.PARAMETER_GEOMETRY;
				String propertyName = HibernateConstants.PARAMETER_GEOMETRY;
				Disjunction disjunction = Restrictions.disjunction();
				for (SpatialFilter filter : filters) {
				    disjunction.add(HibernateCriteriaQueryUtilities
                                            .getCriterionForSpatialFilter(propertyName, filter));
                                }
				criterions.add(disjunction);
			}
			// extension
			criterions.add(HibernateCriteriaQueryUtilities
					.getEqualRestriction(
							HibernateConstants.PARAMETER_SAMPLING_FEATURE,
							Boolean.TRUE));
			return createSosAbstractFeaturesFromResult(
					HibernateFeatureCriteriaQueryUtilities.getFeatureOfInterest(
							aliases, criterions, projections, session), version);
		} catch (HibernateException he) {
			String exceptionText = "Error while querying features from data source!";
			LOGGER.error(exceptionText, he);
			OwsExceptionReport owse = new OwsExceptionReport(he);
			owse.addCodedException(OwsExceptionCode.NoApplicableCode, null,
					exceptionText);
			throw owse;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.n52.sos.ds.IFeatureQueryHandler#getEnvelopeforFeatureIDs(java.util
	 * .List)
	 */
	@Override
	public Envelope getEnvelopeforFeatureIDs(List<String> featureIDs,
			Object connection) throws OwsExceptionReport {
		Session session = getSessionFromConnection(connection);
		try {
			Criteria criteria = session.createCriteria(FeatureOfInterest.class);
			criteria.add(Restrictions.in(HibernateConstants.PARAMETER_IDENTIFIER, featureIDs));
			// extension
			criteria.add(HibernateCriteriaQueryUtilities
					.getEqualRestriction(
							HibernateConstants.PARAMETER_SAMPLING_FEATURE,
							Boolean.TRUE));
			criteria.setProjection(SpatialProjections.extent("geom"));
			Geometry geom = (Geometry)criteria.uniqueResult();
			return geom.getEnvelopeInternal();
		} catch (HibernateException he) {
			String exceptionText = "";
			LOGGER.error(exceptionText, he);
			OwsExceptionReport owse = new OwsExceptionReport(he);
			owse.addCodedException(OwsExceptionCode.NoApplicableCode, null,
					exceptionText);
			throw owse;
		}
	}

	/**
	 * Creates an SosAbstractFeature from the given parameters.
	 * 
	 * @param id
	 *            Feature identifier
	 * @param xmlDesc
	 *            Feature description
	 * @param name
	 *            Feature name
	 * @param geom
	 *            Geometry
	 * @param srid
	 *            SRID of the geometry
	 * @param featureType
	 *            Feature type
	 * @param schemaLink
	 *            Link to schema
	 * @return SosAbstractFeature A feature
	 * @throws OwsExceptionReport
	 *             If WKT geometry string is invalid.
	 */
	public static SosAbstractFeature getAbstractFeatureFromValues(String id,
			String description, String xmlDescription, List<String> name,
			Geometry geometry, int srid, String featureType, String url,
			boolean sampling) throws OwsExceptionReport {

		SosSamplingFeature sampFeat = new SosSamplingFeature(id);
		sampFeat.setName(name);
		sampFeat.setDescription(description);
		sampFeat.setGeometry(geometry);
		sampFeat.setEpsgCode(srid);
		sampFeat.setFeatureType(featureType);
		sampFeat.setSampling(sampling);
		sampFeat.setUrl(url);
		sampFeat.setXmlDescription(xmlDescription);
		return sampFeat;
	}

	/**
	 * Creates a map with FOI identifier and SOS feature
	 * 
	 * @param features
	 *            FeatureOfInterest objects
	 * @param version
	 *            SOS version
	 * @return Map with FOI identifier and SOS feature
	 * @throws OwsExceptionReport
	 *             If feature type is not supported
	 */
	private Map<String, SosAbstractFeature> createSosAbstractFeaturesFromResult(
			List<FeatureOfInterest> features, String version)
			throws OwsExceptionReport {
		Map<String, SosAbstractFeature> sosAbstractFois = new HashMap<String, SosAbstractFeature>();
		for (FeatureOfInterest feature : features) {
			sosAbstractFois.put(feature.getIdentifier(),
					createSosAbstractFeatureFromResult(feature, version));
		}

		return sosAbstractFois;
	}

	/**
	 * Creates a SOS feature from the FeatureOfInterest object
	 * 
	 * @param feature
	 *            FeatureOfInterest object
	 * @param version
	 *            SOS version
	 * @return SOS feature
	 * @throws OwsExceptionReport
	 *             If feature type is not supported
	 */
	private SosAbstractFeature createSosAbstractFeatureFromResult(
			FeatureOfInterest feature, String version)
			throws OwsExceptionReport {
		String checkedFoiID = null;
		if (SosHelper.checkFeatureOfInterestIdentifierForSosV2(
				feature.getIdentifier(), version)) {
			checkedFoiID = feature.getIdentifier();
		}
		return getAbstractFeatureFromValues(checkedFoiID, null,
				feature.getDescriptionXml(), null, feature.getGeom(), feature
						.getGeom().getSRID(), feature
						.getFeatureOfInterestType().getFeatureOfInterestType(),
				feature.getUrl(), feature.isSamplingFeature());
	}

	/**
	 * Checks if connection is a Hibernate Session and casts the connection
	 * 
	 * @param connection
	 *            connection
	 * @return Hibernate Session
	 * @throws OwsExceptionReport
	 *             If connection is not a Hibentat Sessrion
	 */
	private Session getSessionFromConnection(Object connection)
			throws OwsExceptionReport {
		if (connection instanceof Session) {
			return (Session) connection;
		} else {
			String exceptionText = "The connection is not a Hibernate Session!";
			LOGGER.error(exceptionText);
			OwsExceptionReport owse = new OwsExceptionReport(
					ExceptionLevel.DetailedExceptions);
			owse.addCodedException(OwsExceptionCode.NoApplicableCode, null,
					exceptionText);
			throw owse;
		}
	}
}
