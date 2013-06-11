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
package org.n52.sos.encode;

import static java.lang.Boolean.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.opengis.swes.x20.DeleteSensorResponseDocument;
import net.opengis.swes.x20.InsertSensorResponseDocument;
import net.opengis.swes.x20.UpdateSensorDescriptionResponseDocument;

import org.apache.xmlbeans.XmlObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.sos.config.SettingsManager;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.response.DeleteSensorResponse;
import org.n52.sos.response.DescribeSensorResponse;
import org.n52.sos.response.InsertSensorResponse;
import org.n52.sos.response.UpdateSensorResponse;
import org.n52.sos.service.CodingRepository;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.SchemaLocation;


/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class SwesEncoderv20Test {
	
	@BeforeClass
	public final static void initDecoders() {
		CodingRepository.getInstance();
	}

    @AfterClass
    public static void cleanUp(){
        SettingsManager.getInstance().cleanup();
    }

	@Test public final void 
	should_return_correct_encoder_keys()
	{
		final Set<EncoderKey> expectedKeySet = CodingHelper
	            .encoderKeysForElements(SWEConstants.NS_SWES_20,
	                    DescribeSensorResponse.class,
	                    InsertSensorResponse.class,
	                    UpdateSensorResponse.class,
	                    DeleteSensorResponse.class);
		final Set<EncoderKey> returnedKeySet = new SwesEncoderv20().getEncoderKeyType();

		assertThat(returnedKeySet.size(), is(4));
		assertThat(returnedKeySet, is(expectedKeySet));
	}

	@Test public final void 
	should_return_emptyMap_for_supportedTypes()
	{
		assertThat(new SwesEncoderv20().getSupportedTypes(), is(not(nullValue())));
		assertThat(new SwesEncoderv20().getSupportedTypes().isEmpty(), is(TRUE));
	}
	
	@Test public final void
	should_return_emptySet_for_conformanceClasses()
	{
		assertThat(new SwesEncoderv20().getConformanceClasses(), is(not(nullValue())));
		assertThat(new SwesEncoderv20().getConformanceClasses().isEmpty(), is(TRUE));
	}

	@Test public final void
	should_add_own_prefix_to_prefixMap()
	{
		final Map<String, String> prefixMap = CollectionHelper.map();
		new SwesEncoderv20().addNamespacePrefixToMap(prefixMap);
		assertThat(prefixMap.isEmpty(), is(FALSE));
		assertThat(prefixMap.containsKey(SWEConstants.NS_SWES_20), is(TRUE));
		assertThat(prefixMap.containsValue(SWEConstants.NS_SWES_PREFIX), is(TRUE));
	}
	
	@Test public final void
	should_not_fail_if_prefixMap_is_null()
	{
		new SwesEncoderv20().addNamespacePrefixToMap(null);
	}

	@Test public final void 
	should_return_contentType_xml()
	{
		assertThat(new SwesEncoderv20().getContentType(), is("text/xml"));
	}
	
	@Test public final void
	should_return_correct_schema_location()
	{
		assertThat(new SwesEncoderv20().getSchemaLocations().size(), is(1));
    	final SchemaLocation schemLoc = new SwesEncoderv20().getSchemaLocations().iterator().next();
    	assertThat(schemLoc.getNamespace(), is("http://www.opengis.net/swes/2.0"));
    	assertThat(schemLoc.getSchemaFileUrl(), is("http://schemas.opengis.net/swes/2.0/swes.xsd")); 
	}

	@Test (expected=UnsupportedEncoderInputException.class) public final void
	should_return_exception_if_received_null()
			throws OwsExceptionReport {
		new SwesEncoderv20().encode(null);
		new SwesEncoderv20().encode(null, null);
		new SwesEncoderv20().encode(null, new HashMap<SosConstants.HelperValues, String>());
	}
	
	@Test public final void
	should_encode_InsertSensor_response()
			throws OwsExceptionReport {
		final String assignedOffering = "assignedOffering";
		final String assignedProcedure = "assignedProcedure";
		final InsertSensorResponse response = new InsertSensorResponse();
		response.setAssignedOffering(assignedOffering);
		response.setAssignedProcedure(assignedProcedure);
		
		final XmlObject encodedResponse = new SwesEncoderv20().encode(response);
		
		assertThat(encodedResponse, is(instanceOf(InsertSensorResponseDocument.class)));
		final InsertSensorResponseDocument doc = (InsertSensorResponseDocument) encodedResponse;
		assertThat(doc.isNil(), is(FALSE));
		assertThat(doc.getInsertSensorResponse().getAssignedOffering(), is(assignedOffering));
		assertThat(doc.getInsertSensorResponse().getAssignedProcedure(), is(assignedProcedure));
		assertThat(doc.validate(), is(TRUE));
	}
	
	@Test public final void
	should_encode_UpdateSensor_response()
			throws OwsExceptionReport {
		final UpdateSensorResponse response = new UpdateSensorResponse();
		final String updatedProcedure = "updatedProcedure";
		response.setUpdatedProcedure(updatedProcedure);
		
		final XmlObject encodedResponse = new SwesEncoderv20().encode(response);
		
		assertThat(encodedResponse, is(instanceOf(UpdateSensorDescriptionResponseDocument.class)));
		final UpdateSensorDescriptionResponseDocument doc = (UpdateSensorDescriptionResponseDocument) encodedResponse;
		assertThat(doc.isNil(), is(FALSE));
		assertThat(doc.getUpdateSensorDescriptionResponse().getUpdatedProcedure(), is(updatedProcedure));
		assertThat(doc.validate(), is(TRUE));
	}
	
	@Test public final void
	should_encode_DeleteSensor_response()
			throws OwsExceptionReport {
		final DeleteSensorResponse response = new DeleteSensorResponse();
		final String deletedProcedure = "deletedProcedure";
		response.setDeletedProcedure(deletedProcedure);
		
		final XmlObject encodedResponse = new SwesEncoderv20().encode(response);
		
		assertThat(encodedResponse, is(instanceOf(DeleteSensorResponseDocument.class)));
		final DeleteSensorResponseDocument doc = (DeleteSensorResponseDocument) encodedResponse;
		assertThat(doc.isNil(), is(FALSE));
		assertThat(doc.getDeleteSensorResponse().getDeletedProcedure(), is(deletedProcedure));
		assertThat(doc.validate(), is(TRUE));
	}

}
