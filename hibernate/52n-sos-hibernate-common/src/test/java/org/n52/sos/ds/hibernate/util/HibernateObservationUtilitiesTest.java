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

import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.n52.sos.ds.hibernate.HibernateTestCase.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.junit.Ignore;
import org.junit.Test;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.ds.hibernate.HibernateTestCase;
import org.n52.sos.ds.hibernate.entities.Codespace;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterestType;
import org.n52.sos.ds.hibernate.entities.NumericObservation;
import org.n52.sos.ds.hibernate.entities.NumericValue;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.ObservationType;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ProcedureDescriptionFormat;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.GetObservationByIdRequest;

/**
 * The class
 * <code>HibernateObservationUtilitiesTest</code> contains tests for the class {@link
 * <code>HibernateObservationUtilities</code>}
 *
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike HinderkJ&uuml;rrens</a>
 *
 */
public class HibernateObservationUtilitiesTest extends HibernateTestCase {
    private static final String PROCEDURE = "junit_test_procedure_id";
    /*
     * Must be a valid feature identifier in the test data base
     */
    private static final String FEATURE = "1000";
    private static final String OBSERVABLE_PROPERTY =
                                "http://sweet.jpl.nasa.gov/2.0/hydroSurface.owl#Discharge";
    private static final String SET_ID = "junit_setId";
    private static final String PROCEDURE_DESCRIPTION_FORMAT = "junit_procedure_description_format";
    public static final String FEATURE_OF_INTEREST_TYPE = "junit_feature_of_interest_type";
    public static final String OFFERING = "junit_offering";
    public static final String CODESPACE = "junit_codespace";

    @Test
    public void returnEmptyCollectionIfCalledWithoutAnyParameters() throws OwsExceptionReport {
        List<SosObservation> resultList = HibernateObservationUtilities
                .createSosObservationsFromObservations(null, null, null);
        assertThat("result is null", resultList, is(not(nullValue())));
        assertThat("elements in list", resultList.size(), is(0));
    }

    
    @Test
    @Ignore
    //FIXME this one fails: SWE Array is only returned if a result template is present
    public void createSubObservationOfSweArrayObservationViaGetObservationById() throws OwsExceptionReport,
                                                                                        ConnectionProviderException {
        // PREPARE
        Session session = getSession();
        try {
            AbstractServiceRequest request = new GetObservationByIdRequest();
            request.setVersion(Sos2Constants.SERVICEVERSION);

            ProcedureDescriptionFormat hProcedureDescriptionFormat = new ProcedureDescriptionFormat();
            FeatureOfInterestType hFeatureOfInterestType = new FeatureOfInterestType();
            FeatureOfInterest hFeatureOfInterest = new FeatureOfInterest();
            ObservableProperty hObservableProperty = new ObservableProperty();
            ObservationType hObservationType = new ObservationType();
            Offering hOffering = new Offering();
            ObservationConstellation hObservationConstellation = new ObservationConstellation();
            Codespace hCodespace = new Codespace();
            Procedure hProcedure = new Procedure();
            NumericObservation hObservation = new NumericObservation();
            NumericValue hNumericValue = new NumericValue();


            hProcedureDescriptionFormat.setProcedureDescriptionFormat(PROCEDURE_DESCRIPTION_FORMAT);
            hCodespace.setCodespace(CODESPACE);
            hProcedure.setIdentifier(PROCEDURE);
            hProcedure.setProcedureDescriptionFormat(hProcedureDescriptionFormat);
            hFeatureOfInterestType.setFeatureOfInterestType(FEATURE_OF_INTEREST_TYPE);
            hFeatureOfInterest.setIdentifier(FEATURE);
            hFeatureOfInterest.setFeatureOfInterestType(hFeatureOfInterestType);
            hFeatureOfInterest.setCodespace(hCodespace);
            hObservableProperty.setIdentifier(OBSERVABLE_PROPERTY);
            hObservationType.setObservationType(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);
            hOffering.setIdentifier(OFFERING);
            hObservationConstellation.setProcedure(hProcedure);
            hObservationConstellation.setOffering(hOffering);
            hObservationConstellation.setObservableProperty(hObservableProperty);
            hObservationConstellation.setObservationType(hObservationType);
            hObservationConstellation.setDeleted(false);

            session.save(hProcedureDescriptionFormat);
            session.save(hProcedure);
            session.save(hCodespace);
            session.save(hOffering);
            session.save(hFeatureOfInterestType);
            session.save(hFeatureOfInterest);
            session.save(hObservableProperty);
            session.save(hObservationType);
            session.save(hObservationConstellation);

            session.flush();
            
            hNumericValue.setValue(BigDecimal.valueOf(1.0));
            hObservation.setValue(hNumericValue);
            hObservation.setProcedure(hProcedure);
            hObservation.setOfferings(Collections.singleton(hOffering));
            hObservation.setObservableProperty(hObservableProperty);
            hObservation.setFeatureOfInterest(hFeatureOfInterest);
            hObservation.setSetId(SET_ID);
            hObservation.setDeleted(false);

            List<Observation> observationsFromDataBase = new ArrayList<Observation>();
            observationsFromDataBase.add(hObservation);
            // CALL
            List<SosObservation> resultList = HibernateObservationUtilities
                    .createSosObservationsFromObservations(observationsFromDataBase, request.getVersion(), session);
            // TEST RESULTS
            assertThat(resultList, is(notNullValue()));
            assertThat(resultList.size(), is(1));
            Object value = resultList.get(0).getValue().getValue();
            Double val = Double.parseDouble(((SosSweDataArray) value).getValues().get(0).get(1));
            assertThat(value, is(instanceOf(SosSweDataArray.class)));
            assertThat(val, is(closeTo(1.0, 0.00001)));
        } finally {
            returnSession(session);
        }
    }
}
