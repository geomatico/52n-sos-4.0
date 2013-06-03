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
package org.n52.sos.ds.hibernate.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.ResultTemplate;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosResultEncoding;
import org.n52.sos.ogc.sos.SosResultStructure;
import org.n52.sos.request.InsertResultTemplateRequest;
import org.n52.sos.util.CollectionHelper;

/**
 * Hibernate data access class for featureofInterest types
 * 
 * @author CarstenHollmann
 * @since 4.0.0
 */
public class ResultTemplateDAO {

    /**
     * Get result template object for result template identifier
     * 
     * @param identifier
     *            Result template identifier
     * @param session
     *            Hibernate session
     * @return Result template object
     */
    public ResultTemplate getResultTemplateObject(String identifier, Session session) {
        return (ResultTemplate) session.createCriteria(ResultTemplate.class)
                .add(Restrictions.eq(ResultTemplate.IDENTIFIER, identifier))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).uniqueResult();
    }

    /**
     * Get all result template objects
     * 
     * @param session
     *            Hibernate session
     * @return Result template objects
     */
    public List<ResultTemplate> getResultTemplateObjects(Session session) {
        List<ObservationConstellation> observationConstellations =
                new ObservationConstellationDAO().getObservationConstellations(session);
        List<ResultTemplate> resultTemplates = CollectionHelper.list();
        for (ObservationConstellation observationConstellation : observationConstellations) {
            ResultTemplate resultTemplate =
                    getResultTemplateObjectsForObservationConstellation(observationConstellation, session);
            if (resultTemplate != null) {
                resultTemplates.add(resultTemplate);
            }
        }
        return resultTemplates;
    }

    /**
     * Get result template object for observation constellation
     * 
     * @param observationConstellation
     *            Observation constellation object
     * @param session
     *            Hibernate session
     * @return Result template object
     */
    public ResultTemplate getResultTemplateObjectsForObservationConstellation(
            ObservationConstellation observationConstellation, Session session) {
        return getResultTemplateObject(observationConstellation.getOffering().getIdentifier(),
                observationConstellation.getObservableProperty().getIdentifier(), session);
    }

    /**
     * Get result template objects for observation constellation and
     * featureOfInterest
     * 
     * @param observationConstellation
     *            Observation constellation object
     * @param sosAbstractFeature
     *            FeatureOfInterest
     * @param session
     *            Hibernate session
     * @return Result template objects
     */
    public List<ResultTemplate> getResultTemplateObjectsForObservationConstellationAndFeature(
            ObservationConstellation observationConstellation, SosAbstractFeature sosAbstractFeature, Session session) {
        return getResultTemplateObject(observationConstellation.getOffering().getIdentifier(),
                observationConstellation.getObservableProperty().getIdentifier(),
                CollectionHelper.asList(sosAbstractFeature.getIdentifier().getValue()), session);
    }

    /**
     * Get result template object for offering identifier and observable
     * property identifier
     * 
     * @param offering
     *            Offering identifier
     * @param observedProperty
     *            Observable property identifier
     * @param session
     *            Hibernate session
     * @return Result template object
     */
    @SuppressWarnings("unchecked")
    public ResultTemplate getResultTemplateObject(String offering, String observedProperty, Session session) {
        Criteria rtc = session.createCriteria(ResultTemplate.class).setMaxResults(1);
        rtc.createCriteria(ObservationConstellation.OFFERING).add(Restrictions.eq(Offering.IDENTIFIER, offering));
        rtc.createCriteria(ObservationConstellation.OBSERVABLE_PROPERTY).add(
                Restrictions.eq(ObservableProperty.IDENTIFIER, observedProperty));
        /* there can be multiple but equal result templates... */
        List<ResultTemplate> templates = (List<ResultTemplate>) rtc.list();
        return templates.isEmpty() ? null : templates.iterator().next();
    }

    /**
     * Get result template objects for offering identifier, observable property
     * identifier and featureOfInterest identifier
     * 
     * @param offering
     *            Offering identifier
     * @param observedProperty
     *            Observable property identifier
     * @param featureOfInterest
     *            FeatureOfInterest identifier
     * @param session
     *            Hibernate session
     * @return Result template objects
     */
    @SuppressWarnings("unchecked")
    public List<ResultTemplate> getResultTemplateObject(String offering, String observedProperty,
            Collection<String> featureOfInterest, Session session) {
        Criteria rtc =
                session.createCriteria(ResultTemplate.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        rtc.createCriteria(ObservationConstellation.OFFERING).add(Restrictions.eq(Offering.IDENTIFIER, offering));
        rtc.createCriteria(ObservationConstellation.OBSERVABLE_PROPERTY).add(
                Restrictions.eq(ObservableProperty.IDENTIFIER, observedProperty));
        if (featureOfInterest != null && !featureOfInterest.isEmpty()) {
            rtc.createCriteria(ResultTemplate.FEATURE_OF_INTEREST).add(
                    Restrictions.in(FeatureOfInterest.IDENTIFIER, featureOfInterest));
        }
        return rtc.list();
    }

    /**
     * Check or insert result template
     * 
     * @param request
     *            Insert result template request
     * @param observationConstellation
     *            Observation constellation object
     * @param featureOfInterest
     *            FeatureOfInterest object
     * @param session
     *            Hibernate session
     * @throws OwsExceptionReport
     *             If the requested structure/encoding is invalid
     */
    public void checkOrInsertResultTemplate(InsertResultTemplateRequest request,
            ObservationConstellation observationConstellation, FeatureOfInterest featureOfInterest, Session session)
            throws OwsExceptionReport {
        List<ResultTemplate> resultTemplates =
                new ResultTemplateDAO().getResultTemplateObject(
                        observationConstellation.getOffering().getIdentifier(), observationConstellation
                                .getObservableProperty().getIdentifier(), null, session);
        if (CollectionHelper.isEmpty(resultTemplates)) {
            createAndSaveResultTemplate(request, observationConstellation, featureOfInterest, session);
        } else {
            List<String> storedIdentifiers = new ArrayList<String>(0);
            for (ResultTemplate storedResultTemplate : resultTemplates) {
                storedIdentifiers.add(storedResultTemplate.getIdentifier());
                SosResultStructure storedStructure = new SosResultStructure(storedResultTemplate.getResultStructure());
                SosResultStructure newStructure = new SosResultStructure(request.getResultStructure().getXml());

                if (!storedStructure.equals(newStructure)) {
                    throw new InvalidParameterValueException().at(
                            Sos2Constants.InsertResultTemplateParams.proposedTemplate).withMessage(
                            "The requested resultStructure is different from already inserted result template "
                                    + "for procedure (%s) observedProperty (%s) and offering (%s)!",
                            observationConstellation.getProcedure().getIdentifier(),
                            observationConstellation.getObservableProperty().getIdentifier(),
                            observationConstellation.getOffering().getIdentifier());
                }
                SosResultEncoding storedEncoding = new SosResultEncoding(storedResultTemplate.getResultEncoding());
                SosResultEncoding newEndoding = new SosResultEncoding(request.getResultEncoding().getXml());
                if (!storedEncoding.equals(newEndoding)) {
                    throw new InvalidParameterValueException().at(
                            Sos2Constants.InsertResultTemplateParams.proposedTemplate).withMessage(
                            "The requested resultEncoding is different from already inserted result template "
                                    + "for procedure (%s) observedProperty (%s) and offering (%s)!",
                            observationConstellation.getProcedure().getIdentifier(),
                            observationConstellation.getObservableProperty().getIdentifier(),
                            observationConstellation.getOffering().getIdentifier());
                }
            }
            if (request.getIdentifier() != null && !storedIdentifiers.contains(request.getIdentifier())) {
                /* save it only if the identifier is different */
                createAndSaveResultTemplate(request, observationConstellation, featureOfInterest, session);
            }
        }
    }

    /**
     * Insert result template
     * 
     * @param request
     *            Insert result template request
     * @param observationConstellation
     *            Observation constellation object
     * @param featureOfInterest
     *            FeatureOfInterest object
     * @param session
     *            Hibernate session
     */
    private void createAndSaveResultTemplate(InsertResultTemplateRequest request,
            ObservationConstellation observationConstellation, FeatureOfInterest featureOfInterest, Session session) {
        ResultTemplate resultTemplate = new ResultTemplate();
        resultTemplate.setIdentifier(request.getIdentifier());
        resultTemplate.setProcedure(observationConstellation.getProcedure());
        resultTemplate.setObservableProperty(observationConstellation.getObservableProperty());
        resultTemplate.setOffering(observationConstellation.getOffering());
        resultTemplate.setFeatureOfInterest(featureOfInterest);
        resultTemplate.setResultStructure(request.getResultStructure().getXml());
        resultTemplate.setResultEncoding(request.getResultEncoding().getXml());
        session.save(resultTemplate);
        session.flush();
    }
}
