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
package org.n52.sos.ds.hibernate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projection;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IDescribeSensorDAO;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ValidProcedureTime;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ogc.gml.SosGmlMetaDataProperty;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OWSParameterValuePossibleValues;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.sensorML.AbstractMultiProcess;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.elements.SosSMLCapabilities;
import org.n52.sos.ogc.sensorML.elements.SosSMLComponent;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SWEConstants.SweAggregateType;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.request.DescribeSensorRequest;
import org.n52.sos.response.DescribeSensorResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the interface IDescribeSensorDAO
 * 
 */
public class DescribeSensorDAO implements IDescribeSensorDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DescribeSensorDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = SosConstants.Operations.DescribeSensor.name();

    /**
     * Instance of the IConnectionProvider
     */
    private IConnectionProvider connectionProvider;

    /**
     * constructor
     */
    public DescribeSensorDAO() {
        this.connectionProvider = Configurator.getInstance().getConnectionProvider();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.ds.ISosOperationDAO#getOperationName()
     */
    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.ds.ISosOperationDAO#getOperationsMetadata(java.lang.String,
     * java.lang.Object)
     */
    @Override
    public OWSOperation getOperationsMetadata(String service, String version, Object connection)
            throws OwsExceptionReport {
        OWSOperation opsMeta = new OWSOperation();
        // set operation name
        opsMeta.setOperationName(OPERATION_NAME);
        // set param DCP
        DecoderKeyType dkt = null;
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            dkt = new DecoderKeyType(Sos1Constants.NS_SOS);
        } else {
            dkt = new DecoderKeyType(SWEConstants.NS_SWES_20);
        }
        opsMeta.setDcp(SosHelper.getDCP(OPERATION_NAME, dkt,
                Configurator.getInstance().getBindingOperators().values(), Configurator.getInstance().getServiceURL()));
        // set param procedure
        opsMeta.addParameterValue(SosConstants.GetObservationParams.procedure.name(), new OWSParameterValuePossibleValues(Configurator.getInstance()
                .getCapabilitiesCacheController().getProcedures()));
        // set param output format
        List<String> outputFormatValues = new ArrayList<String>(1);
        String parameterName = null;
     // FIXME: getTypes from Decoder
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            outputFormatValues.add(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE);
            parameterName = Sos1Constants.DescribeSensorParams.outputFormat.name();
        } else if (version.equals(Sos2Constants.SERVICEVERSION)) {
            outputFormatValues.add(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL);
           parameterName = Sos2Constants.DescribeSensorParams.procedureDescriptionFormat.name();
        }
        if (parameterName != null) { 
            opsMeta.addParameterValue(parameterName, new OWSParameterValuePossibleValues(outputFormatValues));
        }
        
        return opsMeta;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.ds.IDescribeSensorDAO#getSensorMLDescription(org.n52.sos.
     * request.AbstractSosRequest)
     */
    @Override
    public DescribeSensorResponse getSensorDescription(DescribeSensorRequest request) throws OwsExceptionReport {
        // sensorDocument which should be returned
        Session session = null;
        try {
            session = (Session) connectionProvider.getConnection();
            SensorML result = queryProcedure(request.getProcedure(), request.getOutputFormat(), session);
            AbstractMultiProcess member = new AbstractMultiProcess();
            // fois
            List<String> procedures = new ArrayList<String>(1);
            procedures.add(request.getProcedure());
            SosSMLCapabilities featureCapabilities =
                    getFeatureOfInterestIDsForProcedure(procedures, request.getVersion(), session);
            if (featureCapabilities != null) {
                member.addCapabilities(featureCapabilities);
            }
            // parent procs
            SosSMLCapabilities parentProcCapabilities =
                    getParentProcedures(request.getProcedure(), request.getVersion());
            if (parentProcCapabilities != null) {
                member.addCapabilities(parentProcCapabilities);
            }
            // child procs
            member.addComponents(getChildProcedures(request.getProcedure(), request.getOutputFormat(),
                    request.getVersion(), session));
            result.addMember(member);
            DescribeSensorResponse response = new DescribeSensorResponse();
            response.setService(request.getService());
            response.setVersion(request.getVersion());
            response.setOutputFormat(request.getOutputFormat());
            response.setSensorDescription(result);
            return response;
        } catch (HibernateException he) {
            String exceptionText = "Error while querying data for DescribeSensor document!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            connectionProvider.returnConnection(session);
        }
    }

    private SensorML queryProcedure(String procID, String outputFormat, Session session) throws OwsExceptionReport {
        String filename = null;
        String smlFile = null;
        String descriptionFormat = null;
        // TODO: check and query for validTime parameter 
        Procedure procedure = HibernateCriteriaQueryUtilities.getProcedureForIdentifier(procID, session);
        Set<ValidProcedureTime> validProcedureTimes = procedure.getValidProcedureTimes();
        for (ValidProcedureTime validProcedureTime : validProcedureTimes) {
            if (validProcedureTime.getEndTime() == null) {
                filename = validProcedureTime.getDescriptionUrl();
                smlFile = validProcedureTime.getDescriptionXml();
            }
        }
        descriptionFormat = procedure.getProcedureDescriptionFormat().getProcedureDescriptionFormat();
        // check whether SMLFile or Url is set
        if (filename == null && smlFile == null) {
            String exceptionText = "No sensorML file was found for the requested procedure " + procID;
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(
                    SosConstants.DescribeSensorParams.procedure.toString(), exceptionText);

        } else {

            if (filename != null && descriptionFormat != null && smlFile == null) {
                // return sensorML from folder

                if (!descriptionFormat.equalsIgnoreCase(outputFormat)
                        && !descriptionFormat.equalsIgnoreCase(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE)) {
                    String exceptionText =
                            "The value of the output format is wrong and has to be " + descriptionFormat
                                    + " for procedure " + procID;
                    LOGGER.error(exceptionText);
                    throw Util4Exceptions.createInvalidParameterValueException(
                            SosConstants.DescribeSensorParams.procedure.toString(), exceptionText);
                }
                FileInputStream fis = null;
                try {

                    File sensorFile = null;
                    LOGGER.info(filename);

                    // read in the description file
                    if (filename.startsWith("standard/")) {
                        filename = filename.replace("standard/", "");
                        sensorFile = new File(Configurator.getInstance().getSensorDir(), filename);
                    } else {
                        sensorFile = new File(filename);
                    }

                    fis = new FileInputStream(sensorFile);

                    byte[] b = new byte[fis.available()];
                    fis.read(b);
                    fis.close();

                    // set result
                    SensorML sensorML = new SensorML();
                    sensorML.setSensorDescriptionXmlString(new String(b));
                    return sensorML;

                } catch (FileNotFoundException fnfe) {
                    String exceptionText = "No sensorML file was found for the requested procedure " + procID;
                    LOGGER.error(exceptionText, fnfe);
                    throw Util4Exceptions.createInvalidParameterValueException(
                            SosConstants.DescribeSensorParams.procedure.toString(), exceptionText);
                } catch (IOException ioe) {
                    String exceptionText = "An error occured while parsing the sensor description document!";
                    LOGGER.error(exceptionText, ioe);
                    throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException ioe) {
                            LOGGER.error("Error while closing the sensor description file input stream!", ioe);
                        }
                    }
                }
            } else {
                SensorML sensorML = new SensorML();
                sensorML.setSensorDescriptionXmlString(smlFile);
                return sensorML;
            }
        }
    }

    private SosSMLCapabilities getFeatureOfInterestIDsForProcedure(List<String> proceedures, String version,
            Session session) throws OwsExceptionReport {
        Map<String, String> aliases = new HashMap<String, String>();
        List<Criterion> criterions = new ArrayList<Criterion>();
        List<Projection> projections = new ArrayList<Projection>();
        String obsAlias = HibernateCriteriaQueryUtilities.addObservationAliasToMap(aliases, null);
        String obsConstAlias =
                HibernateCriteriaQueryUtilities.addObservationConstallationAliasToMap(aliases, obsAlias);
        // procedures
        String procAlias = HibernateCriteriaQueryUtilities.addProcedureAliasToMap(aliases, obsConstAlias);
        criterions.add(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                HibernateCriteriaQueryUtilities.getIdentifierParameter(procAlias), proceedures));
        // FIXME: checks for generated IDs and remove them for SOS 2.0
        Collection<String> foiIDs =
                SosHelper.getFeatureIDs(HibernateCriteriaQueryUtilities.getFeatureOfInterestIdentifier(aliases,
                        criterions, projections, session), version);
        if (foiIDs != null && !foiIDs.isEmpty()) {
            SosSMLCapabilities capabilities = new SosSMLCapabilities();
            capabilities.setName("featureOfInterest");
            capabilities.setCapabilitiesType(SweAggregateType.SimpleDataRecord);
            List<SosSweField> fields = new ArrayList<SosSweField>();
            for (String foiID : foiIDs) {
                SosSweText text = new SosSweText();
                text.setDefinition("FeatureOfInterest identifier");
                text.setValue(foiID);
                fields.add(new SosSweField("FeatureOfInterestID", text));
            }
            capabilities.setFields(fields);
            return capabilities;
        }
        return null;
    }

    /**
     * Add parent procedures to a SystemDocument
     * 
     * @param xb_systemDoc
     *            System document to add parent procedures to
     * @param parentProcedureIds
     *            The parent procedures to add
     * @throws OwsExceptionReport
     */
    private SosSMLCapabilities getParentProcedures(String procID, String version) throws OwsExceptionReport {
        Collection<String> parentProcedureIds =
                Configurator.getInstance().getCapabilitiesCacheController().getParentProcedures(procID, false, false);
        if (parentProcedureIds != null && !parentProcedureIds.isEmpty()) {
            SosSMLCapabilities capabilities = new SosSMLCapabilities();
            capabilities.setName(SosConstants.SYS_CAP_PARENT_PROCEDURES_NAME);
            capabilities.setCapabilitiesType(SweAggregateType.SimpleDataRecord);
            String urlPattern =
                    SosHelper.getUrlPatternForHttpGetMethod(Configurator.getInstance().getBindingOperators().values(),
                            SosConstants.Operations.DescribeSensor.name(), new DecoderKeyType(SosConstants.SOS,
                                    version));
            for (String parentProcID : parentProcedureIds) {
                SosGmlMetaDataProperty metadata = new SosGmlMetaDataProperty();
                metadata.setTitle(parentProcID);
                try {
                    metadata.setHref(SosHelper.getDescribeSensorUrl(version, Configurator.getInstance()
                            .getServiceURL(), parentProcID, urlPattern));
                } catch (UnsupportedEncodingException uee) {
                    String exceptionText = "Error while encoding DescribeSensor URL";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createNoApplicableCodeException(uee, exceptionText);
                }
                capabilities.addMetaDataProperties(metadata);
            }
            return capabilities;
        }
        return null;
    }

    /**
     * Add a collection of child procedures to a SystemDocument
     * 
     * @param xb_systemDoc
     *            System document to add child procedures to
     * @param childProcedures
     *            The child procedures to add
     * @throws OwsExceptionReport
     */
    private List<SosSMLComponent> getChildProcedures(String procID, String outputFormat, String version,
            Session session) throws OwsExceptionReport {
        List<SosSMLComponent> smlComponsents = new ArrayList<SosSMLComponent>();
        Collection<String> childProcedureIds =
                Configurator.getInstance().getCapabilitiesCacheController().getChildProcedures(procID, false, false);
        int childCount = 0;
        if (childProcedureIds != null && !childProcedureIds.isEmpty()) {
            String urlPattern =
                    SosHelper.getUrlPatternForHttpGetMethod(Configurator.getInstance().getBindingOperators().values(),
                            SosConstants.Operations.DescribeSensor.name(), new DecoderKeyType(SosConstants.SOS,
                                    version));
            for (String childProcID : childProcedureIds) {
                childCount++;
                SosSMLComponent component = new SosSMLComponent("component" + childCount);
                component.setTitle(childProcID);
                if (Configurator.getInstance().isChildProceduresEncodedInParentsDescribeSensor()) {
                    component.setProcess(queryProcedure(childProcID, outputFormat, session));
                } else {
                    try {
                        component.setHref(SosHelper.getDescribeSensorUrl(Sos2Constants.SERVICEVERSION, Configurator
                                .getInstance().getServiceURL(), childProcID, urlPattern));
                    } catch (UnsupportedEncodingException uee) {
                        String exceptionText = "Error while encoding DescribeSensor URL";
                        LOGGER.debug(exceptionText);
                        throw Util4Exceptions.createNoApplicableCodeException(uee, exceptionText);
                    }
                }
                smlComponsents.add(component);
            }
        }
        return smlComponsents;
    }

    @Override
    public IExtension getExtension(Object connection) throws OwsExceptionReport {
        // TODO Auto-generated method stub
        return null;
    }

}
