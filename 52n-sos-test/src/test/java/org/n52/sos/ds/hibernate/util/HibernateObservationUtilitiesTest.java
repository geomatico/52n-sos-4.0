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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.junit.Test;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.NumericObservation;
import org.n52.sos.ds.hibernate.entities.NumericValue;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellationOfferingObservationType;
import org.n52.sos.ds.hibernate.entities.ObservationType;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.GetObservationByIdRequest;
import org.n52.sos.service.Configurator;
import org.n52.sos.test.AbstractSosTestCase;
/**
 * The class <code>HibernateObservationUtilitiesTest</code> contains tests for
 * the class {@link <code>HibernateObservationUtilities</code>}
 *
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike HinderkJ&uuml;rrens</a>
 * 
 */
public class HibernateObservationUtilitiesTest extends AbstractSosTestCase{
	

	private static final String proc_id = "junit_test_procedure_id";
	/*
	 * Must be a valid feature identifier in the test data base
	 */
	private static final String featureIdentifier = "1000";
	private static final String observablePropertyIdentifier = "http://sweet.jpl.nasa.gov/2.0/hydroSurface.owl#Discharge";
	private static final String setId = "junit_setId";

	@Test
	public void returnEmptyCollectionIfCalledWithoutAnyParameters() throws OwsExceptionReport 
	{
		List<SosObservation> resultList = HibernateObservationUtilities.createSosObservationsFromObservations(null, null, null);
		assertNotNull("result is null",resultList);
		assertEquals("elements in list",0, resultList.size());
	}
	
	@Test
	public void createSubObservationOfSweArrayObservationViaGetObservationById() throws OwsExceptionReport, ConnectionProviderException
	{
		// PREPARE
		// get connection provider to get session
		Session session = (Session) Configurator.getInstance().getDataConnectionProvider().getConnection();
		AbstractServiceRequest request = new GetObservationByIdRequest();
		request.setVersion(Sos2Constants.SERVICEVERSION);
		Procedure p = new Procedure();
		FeatureOfInterest f = new FeatureOfInterest();
		f.setIdentifier(featureIdentifier);
		p.setIdentifier(proc_id);
		ObservableProperty oP = new ObservableProperty();
		oP.setIdentifier(observablePropertyIdentifier);
		ObservationType obsType = new ObservationType();
		obsType.setObservationType(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);
		ObservationConstellation obsConst = new ObservationConstellation();
		obsConst.setProcedure(p);
		obsConst.setObservableProperty(oP);
		ObservationConstellationOfferingObservationType obsConstOffObType = new ObservationConstellationOfferingObservationType();
		obsConstOffObType.setObservationConstellation(obsConst);
		obsConstOffObType.setObservationType(obsType);
		NumericObservation dbObservation = new NumericObservation();
		NumericValue nVa = new NumericValue();
		nVa.setValue(BigDecimal.valueOf(1.0));
		dbObservation.setValue(nVa);
		Set<ObservationConstellation> obsConsts = new HashSet<ObservationConstellation>(1);
		obsConsts.add(obsConst);
		dbObservation.setObservationConstellation(obsConst);
		dbObservation.setFeatureOfInterest(f);
		dbObservation.setSetId(setId);
		List<Observation> observationsFromDataBase = new ArrayList<Observation>();
		observationsFromDataBase.add(dbObservation);
		// CALL
		List<SosObservation> resultList = HibernateObservationUtilities.createSosObservationsFromObservations(observationsFromDataBase, request.getVersion(), session);
		// TEST RESULTS
		assertNotNull("result is null",resultList);
		assertEquals("elements in list",1,resultList.size());
		assertEquals("result value is ",SosSweDataArray.class.getName(),resultList.get(0).getValue().getValue().getClass().getName());
		assertEquals("result value is 1.0",
				1.0,
				Double.parseDouble(((SosSweDataArray)resultList.get(0).getValue().getValue()).getValues().get(0).get(1)),
				0.00001);
		fail("not finished");
	}

}
