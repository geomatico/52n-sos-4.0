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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.n52.sos.decode.OperationDecoderKey;
import org.n52.sos.ds.IDescribeSensorDAO;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ValidProcedureTime;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.sos.SosProcedureDescriptionUnknowType;
import org.n52.sos.request.DescribeSensorRequest;
import org.n52.sos.response.DescribeSensorResponse;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the interface IDescribeSensorDAO
 * 
 */
public class DescribeSensorDAO extends AbstractHibernateOperationDao implements IDescribeSensorDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DescribeSensorDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = SosConstants.Operations.DescribeSensor.name();

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.ds.ISosOperationDAO#getOperationName()
     */
    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    @Override
    protected void setOperationsMetadata(OWSOperation opsMeta, String service, String version, Session connection)
            throws OwsExceptionReport {
        opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.procedure, getCache().getProcedures());
        // FIXME: getTypes from Decoder
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            opsMeta.addPossibleValuesParameter(Sos1Constants.DescribeSensorParams.outputFormat,
                    SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE);
        } else if (version.equals(Sos2Constants.SERVICEVERSION)) {
            opsMeta.addPossibleValuesParameter(Sos2Constants.DescribeSensorParams.procedureDescriptionFormat,
                    SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL);
        }
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
            session = getSession();
            SosProcedureDescription result =
                    queryProcedure(request.getProcedure(), request.getProcedureDescriptionFormat(), session);
            
            Collection<String> features = getFeatureOfInterestIDsForProcedure(request.getProcedure(), request.getVersion(), session);
            if (features != null && !features.isEmpty()) {
                result.addFeatureOfInterst(new HashSet<String>(features), request.getProcedure());
            }

            // parent procs
            Collection<String> parentProcedures = getParentProcedures(request.getProcedure(), request.getVersion());
            if (parentProcedures != null && !parentProcedures.isEmpty()) {
                result.addParentProcedures(new HashSet<String>(parentProcedures), request.getProcedure());
            }

            // child procs
            Set<SosProcedureDescription> childProcedures =
                    getChildProcedures(request.getProcedure(), request.getProcedureDescriptionFormat(),
                            request.getVersion(), session);
            if (childProcedures != null && !childProcedures.isEmpty()) {
                result.addChildProcedures(childProcedures, request.getProcedure());
            }
            DescribeSensorResponse response = new DescribeSensorResponse();
            response.setService(request.getService());
            response.setVersion(request.getVersion());
            response.setOutputFormat(request.getProcedureDescriptionFormat());
            response.setSensorDescription(result);
            return response;
        } catch (HibernateException he) {
            String exceptionText = "Error while querying data for DescribeSensor document!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            returnSession(session);
        }
    }

    private SosProcedureDescription queryProcedure(String procID, String outputFormat, Session session)
            throws OwsExceptionReport {
        String filename = null;
        String smlFile = null;
        String descriptionFormat;
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
            try {
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
                    File sensorFile;
                    LOGGER.info(filename);
                    // read in the description file
                    if (filename.startsWith("standard/")) {
                        filename = filename.replace("standard/", "");
                        sensorFile = new File(getConfigurator().getSensorDir(), filename);
                    } else {
                        sensorFile = new File(filename);
                    }
                    XmlObject procedureDescription = XmlObject.Factory.parse(sensorFile);
                    try {
                        return (SosProcedureDescription) CodingHelper.decodeXmlElement(procedureDescription);
                    } catch (OwsExceptionReport owse) {
                        return new SosProcedureDescriptionUnknowType(procID, outputFormat,
                                procedureDescription.xmlText());
                    }
                } else {
                    XmlObject procedureDescription = XmlObject.Factory.parse(smlFile);
                    try {
                        return (SosProcedureDescription) CodingHelper.decodeXmlElement(procedureDescription);
                    } catch (OwsExceptionReport owse) {
                        return new SosProcedureDescriptionUnknowType(procID, outputFormat,
                                procedureDescription.xmlText());
                    }
                }
            } catch (FileNotFoundException fnfe) {
                String exceptionText = "No sensorML file was found for the requested procedure " + procID;
                LOGGER.error(exceptionText, fnfe);
                throw Util4Exceptions.createInvalidParameterValueException(
                        SosConstants.DescribeSensorParams.procedure.toString(), exceptionText);
            } catch (IOException ioe) {
                String exceptionText = "An error occured while parsing the sensor description document!";
                LOGGER.error(exceptionText, ioe);
                throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
            } catch (XmlException xmle) {
                String exceptionText = "An error occured while parsing the sensor description document!";
                LOGGER.error(exceptionText, xmle);
                throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
            }
        }
    }

    private Collection<String> getFeatureOfInterestIDsForProcedure(String procedureIdentifier, String version,
            Session session) throws OwsExceptionReport {
        HibernateQueryObject queryObject = new HibernateQueryObject();
        Map<String, String> aliases = new HashMap<String, String>(3);
        String obsAlias = HibernateCriteriaQueryUtilities.addObservationAliasToMap(aliases, null);
        String obsConstAlias =
                HibernateCriteriaQueryUtilities.addObservationConstallationAliasToMap(aliases, obsAlias);
        // procedures
        String procAlias = HibernateCriteriaQueryUtilities.addProcedureAliasToMap(aliases, obsConstAlias);
        queryObject.setAliases(aliases);
        List<String> list = new ArrayList<String>(1);
        list.add(procedureIdentifier);
        queryObject.addCriterion(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                HibernateCriteriaQueryUtilities.getIdentifierParameter(procAlias), list));
        // FIXME: checks for generated IDs and remove them for SOS 2.0
        return
                SosHelper.getFeatureIDs(
                        HibernateCriteriaQueryUtilities.getFeatureOfInterestIdentifier(queryObject, session), version);
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
    private Collection<String> getParentProcedures(String procID, String version) throws OwsExceptionReport {
        return getCache().getParentProcedures(procID, false, false);
        // if (parentProcedureIds != null && !parentProcedureIds.isEmpty()) {
        // SosSMLCapabilities capabilities = new SosSMLCapabilities();
        // capabilities.setName(SosConstants.SYS_CAP_PARENT_PROCEDURES_NAME);
        // String urlPattern =
        // SosHelper.getUrlPatternForHttpGetMethod(new
        // OperationDecoderKey(SosConstants.SOS, version,
        // SosConstants.Operations.DescribeSensor.name()));
        // for (String parentProcID : parentProcedureIds) {
        // SosGmlMetaDataProperty metadata = new SosGmlMetaDataProperty();
        // metadata.setTitle(parentProcID);
        // try {
        // metadata.setHref(SosHelper.getDescribeSensorUrl(version,
        // getConfigurator().getServiceURL(),
        // parentProcID, urlPattern));
        // } catch (UnsupportedEncodingException uee) {
        // String exceptionText = "Error while encoding DescribeSensor URL";
        // LOGGER.debug(exceptionText);
        // throw Util4Exceptions.createNoApplicableCodeException(uee,
        // exceptionText);
        // }
        // capabilities.addMetaDataProperties(metadata);
        // }
        // capabilities.setDataRecord(new SosSweSimpleDataRecord());
        // return capabilities;
        // }
        // return null;
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
    private Set<SosProcedureDescription> getChildProcedures(String procID, String outputFormat, String version,
            Session session) throws OwsExceptionReport {
        Set<SosProcedureDescription> childProcedures = new HashSet<SosProcedureDescription>(0);
        Collection<String> childProcedureIds = getCache().getChildProcedures(procID, false, false);

        if (childProcedureIds != null && !childProcedureIds.isEmpty()) {
            String urlPattern =
                    SosHelper.getUrlPatternForHttpGetMethod(new OperationDecoderKey(SosConstants.SOS, version,
                            SosConstants.Operations.DescribeSensor.name()));
            for (String childProcID : childProcedureIds) {
                childProcedures.add(queryProcedure(childProcID, outputFormat, session));

                // int childCount = 0;
                // childCount++;
                // SosSMLComponent component = new SosSMLComponent("component" +
                // childCount);
                // component.setTitle(childProcID);
                // if
                // (getConfigurator().isChildProceduresEncodedInParentsDescribeSensor())
                // {
                //
                // } else {
                // try {
                // component.setHref(SosHelper.getDescribeSensorUrl(Sos2Constants.SERVICEVERSION,
                // getConfigurator().getServiceURL(), childProcID, urlPattern));
                // } catch (UnsupportedEncodingException uee) {
                // String exceptionText =
                // "Error while encoding DescribeSensor URL";
                // LOGGER.debug(exceptionText);
                // throw Util4Exceptions.createNoApplicableCodeException(uee,
                // exceptionText);
                // }
                // }
                // smlComponsents.add(component);
            }
        }
        return childProcedures;
    }
}
